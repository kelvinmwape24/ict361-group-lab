package zm.mu.ict361lab.data.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import zm.mu.ict361lab.data.local.AppDatabase;
import zm.mu.ict361lab.data.local.dao.LocalStudentDao;
import zm.mu.ict361lab.data.local.dao.PendingOperationDao;
import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.local.entity.PendingOperationEntity;
import zm.mu.ict361lab.data.remote.ApiService;
import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.data.remote.RetrofitClient;
import zm.mu.ict361lab.util.ApiErrors;
import zm.mu.ict361lab.util.Constants;
import zm.mu.ict361lab.util.TokenStore;

/**
 * Drains the queue, then pulls what changed on the server.
 *
 * Four rules this worker exists to keep:
 *
 *  1. Order. Operations replay oldest first, one at a time. A lecturer's
 *     offline create, then edit, then delete of the same student arrives in
 *     that order, so the delete wins — which is what the user meant. (The one
 *     case that is combined instead of replayed is a delete of a student whose
 *     create never left the phone; the repository drops both, because the
 *     server has never heard of them.)
 *
 *  2. Exactly once. Every operation carries the id it was created with, and
 *     that id is reused on every retry. The server stores a receipt against it,
 *     so a response lost in transit costs a duplicate request, never a
 *     duplicate effect. We store the receipt too, so we never send it again.
 *
 *  3. No silent overwrite. Each operation states the version it was made
 *     against. If the record moved on, the server refuses and hands back its
 *     current copy, which we keep beside the user's proposal for review.
 *
 *  4. Nothing replays under the wrong user. A revoked session pauses the queue
 *     rather than discarding it, and the queue is scoped by account.
 */
public class SyncWorker extends Worker {

    private static final Gson GSON = new Gson();

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context app = getApplicationContext();
        TokenStore tokens = new TokenStore(app);

        // A signed-out or expired session must not replay anything — least of
        // all under whoever signs in next.
        if (!tokens.signedIn()) return Result.success();

        AppDatabase db = AppDatabase.get(app);
        LocalStudentDao students = db.studentDao();
        PendingOperationDao operations = db.operationDao();
        ApiService api = RetrofitClient.get(app);
        String account = tokens.accountId();

        List<PendingOperationEntity> queue = operations.dueSync(account);

        for (PendingOperationEntity op : queue) {

            // Already answered on a previous attempt: the receipt is durable,
            // so there is nothing left to do for this one.
            if (op.resultJson != null) {
                operations.setStatus(op.operationId, "SYNCED");
                continue;
            }

            operations.setStatus(op.operationId, Constants.LOCAL_SYNCING);
            students.setStatus(op.studentId, Constants.LOCAL_SYNCING);

            Dtos.SyncRequest request = new Dtos.SyncRequest();
            request.operation_id = op.operationId;       // the SAME id on every retry
            request.type = op.operationType;
            request.payload = GSON.fromJson(op.payload, Dtos.SyncPayload.class);
            request.base_version = op.baseVersion;

            try {
                Response<Dtos.SyncResponse> response = api.push(request).execute();

                if (response.isSuccessful()) {
                    final String receipt = GSON.toJson(response.body());
                    final Dtos.SyncResponse body = response.body();
                    db.runInTransaction(() -> {
                        operations.setReceipt(op.operationId, receipt);
                        applyAccepted(students, op, body, account);
                    });
                    continue;
                }

                Dtos.ErrorDto error = ApiErrors.parse(response);

                if (ApiErrors.isSessionGone(error.error)) {
                    // Pause, do not discard. The work is still the user's when
                    // they sign back in as themselves.
                    operations.setFailure(op.operationId, Constants.LOCAL_PENDING, error.error, null);
                    students.setStatus(op.studentId, Constants.LOCAL_PENDING);
                    return Result.success();
                }

                switch (String.valueOf(error.error)) {
                    case "CONFLICT":
                        // Keep the local proposal AND the server's record, so
                        // the review screen can show the user both.
                        operations.setConflict(op.operationId, "CONFLICT",
                                error.current_version,
                                error.current == null ? null : GSON.toJson(error.current));
                        students.setStatus(op.studentId, Constants.LOCAL_ACTION_REQUIRED, "CONFLICT");
                        break;

                    case "GROUP_FULL":
                        operations.setFailure(op.operationId, Constants.LOCAL_ACTION_REQUIRED,
                                "GROUP_FULL", null);
                        // The previous group is retained: the new one was never
                        // written into labGroup in the first place.
                        students.setStatus(op.studentId, Constants.LOCAL_ACTION_REQUIRED, "GROUP_FULL");
                        break;

                    case "DUPLICATE_NUMBER":
                        // A CREATE the server refused. The local row is a
                        // student who does not and will not exist remotely, so
                        // it is withdrawn rather than left as a phantom.
                        db.runInTransaction(() -> {
                            operations.setFailure(op.operationId, Constants.LOCAL_ACTION_REQUIRED,
                                    "DUPLICATE_NUMBER", null);
                            students.setStatus(op.studentId, Constants.LOCAL_ACTION_REQUIRED,
                                    "DUPLICATE_NUMBER");
                        });
                        break;

                    case "STUDENT_DELETED":
                        // A stale edit must never resurrect a deleted student.
                        db.runInTransaction(() -> {
                            operations.deleteForStudent(op.studentId);
                            students.markDeleted(op.studentId, op.baseVersion);
                        });
                        break;

                    case "OPERATION_ID_REUSED":
                        operations.setFailure(op.operationId, Constants.LOCAL_ACTION_REQUIRED,
                                "OPERATION_ID_REUSED", null);
                        students.setStatus(op.studentId, Constants.LOCAL_ACTION_REQUIRED,
                                "OPERATION_ID_REUSED");
                        break;

                    default:
                        operations.setFailure(op.operationId, Constants.LOCAL_ACTION_REQUIRED,
                                error.error, null);
                        students.setStatus(op.studentId, Constants.LOCAL_ACTION_REQUIRED, error.error);
                }

            } catch (IOException e) {
                // The connection dropped mid-queue. Put this operation back and
                // let WorkManager retry with backoff — the id is unchanged, so
                // the retry is safe even if the server already applied it.
                operations.setStatus(op.operationId, Constants.LOCAL_PENDING);
                students.setStatus(op.studentId, Constants.LOCAL_PENDING);
                return Result.retry();
            }
        }

        operations.pruneSynced(account);

        /* ── pull: authorised updates and deletion markers ── */
        try {
            Response<Dtos.ChangesResponse> response = api.changes(tokens.lastSync()).execute();
            if (response.isSuccessful() && response.body() != null) {
                Dtos.ChangesResponse changes = response.body();

                if (changes.updates != null) {
                    for (Dtos.StudentDto dto : changes.updates) {
                        LocalStudentEntity row = students.findSync(dto.student_id, account);
                        if (row == null) {
                            row = new LocalStudentEntity();
                            row.studentId = dto.student_id;
                            row.accountId = account;
                            row.localStatus = Constants.LOCAL_SYNCED;
                        }
                        // Confirmed values from the server. A pending proposal
                        // on this row is deliberately left in place.
                        row.studentNumber = dto.student_number;
                        row.studentName = dto.name;
                        row.programme = dto.program_code;
                        row.programId = dto.program_id;
                        row.labGroup = dto.group_label;
                        row.groupId = dto.group_id;
                        row.version = dto.version;
                        row.isDeleted = false;
                        row.updatedAt = dto.updated_at;
                        if (dto.claim_code != null) row.claimCode = dto.claim_code;
                        students.upsert(row);
                    }
                }

                if (changes.deletions != null) {
                    for (Dtos.DeletionMarker marker : changes.deletions) {
                        final Dtos.DeletionMarker m = marker;
                        db.runInTransaction(() -> {
                            students.markDeleted(m.student_id, m.version);
                            operations.deleteForStudent(m.student_id);
                        });
                    }
                }

                if (changes.server_time != null) tokens.setLastSync(changes.server_time);
            }
        } catch (IOException e) {
            return Result.retry();
        }

        return Result.success();
    }

    /** The server accepted the change, so the proposal becomes the confirmed value. */
    private void applyAccepted(LocalStudentDao students, PendingOperationEntity op,
                               Dtos.SyncResponse body, String account) {
        LocalStudentEntity row = students.findSync(op.studentId, account);
        if (row == null) return;

        switch (String.valueOf(op.operationType)) {
            case Constants.OP_CREATE:
                // The claim code is issued by the server. It is stored because
                // the lecturer has to read it out to the student afterwards.
                if (body != null && body.claim_code != null) row.claimCode = body.claim_code;
                row.version = body != null && body.new_version != null ? body.new_version : 1;
                break;

            case Constants.OP_UPDATE:
                if (row.pendingName != null) row.studentName = row.pendingName;
                if (body != null && body.new_version != null) row.version = body.new_version;
                break;

            case Constants.OP_ASSIGN:
                if (row.pendingGroup != null) row.labGroup = row.pendingGroup;
                if (body != null && body.group_id != null) row.groupId = body.group_id;
                row.version = row.version + 1;
                break;

            case Constants.OP_DELETE:
                row.isDeleted = true;
                row.labGroup = null;
                row.groupId = null;
                break;

            default:
                break;
        }

        row.pendingName = null;
        row.pendingGroup = null;
        row.conflictMessage = null;
        row.localStatus = Constants.LOCAL_SYNCED;
        students.upsert(row);
    }
}
