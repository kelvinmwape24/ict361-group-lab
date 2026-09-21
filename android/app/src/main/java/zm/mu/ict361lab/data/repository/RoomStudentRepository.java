package zm.mu.ict361lab.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Response;
import zm.mu.ict361lab.data.local.AppDatabase;
import zm.mu.ict361lab.data.local.dao.LocalStudentDao;
import zm.mu.ict361lab.data.local.dao.PendingOperationDao;
import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.local.entity.PendingOperationEntity;
import zm.mu.ict361lab.data.remote.ApiService;
import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.data.remote.RetrofitClient;
import zm.mu.ict361lab.data.sync.SyncScheduler;
import zm.mu.ict361lab.util.ApiErrors;
import zm.mu.ict361lab.util.AppExecutors;
import zm.mu.ict361lab.util.Constants;
import zm.mu.ict361lab.util.ProgrammeStore;
import zm.mu.ict361lab.util.TokenStore;

/**
 * The real repository: Room is the source of truth the screens read, the API is
 * where changes eventually go, and neither is visible to a ViewModel.
 */
public class RoomStudentRepository implements StudentRepository {

    private static final Gson GSON = new Gson();

    private final Context app;
    private final LocalStudentDao students;
    private final PendingOperationDao operations;
    private final AppDatabase db;
    private final ApiService api;
    private final TokenStore tokens;
    private final ProgrammeStore programmes;
    private final AppExecutors executors = AppExecutors.get();

    public RoomStudentRepository(Context context) {
        this.app = context.getApplicationContext();
        this.db = AppDatabase.get(app);
        this.students = db.studentDao();
        this.operations = db.operationDao();
        this.api = RetrofitClient.get(app);
        this.tokens = new TokenStore(app);
        this.programmes = new ProgrammeStore(app);
    }

    private String account() { return tokens.accountId(); }

    /* ═════════════════════════ reads ═════════════════════════ */

    @Override
    public LiveData<List<LocalStudentEntity>> cachedRoster(String query, String programme, String group) {
        return students.observeFiltered(account(),
                query == null ? "" : query,
                programme == null ? "" : programme,
                group == null ? "" : group);
    }

    @Override
    public LiveData<LocalStudentEntity> cachedStudent(String studentId) {
        return students.observe(studentId, account());
    }

    @Override
    public LiveData<List<PendingOperationEntity>> queue() { return operations.observeAll(account()); }

    @Override
    public LiveData<Integer> outstandingCount() { return operations.countOutstanding(account()); }

    /* ═════════════════════════ network reads ═════════════════════════ */

    @Override
    public void loadRoster(String query, String programme, String group, int page,
                           Result.Callback<Dtos.PageDto> callback) {
        executors.networkIO().execute(() -> {
            try {
                Response<Dtos.PageDto> response = api.students(
                        query == null ? "" : query,
                        programme == null ? "" : programme,
                        group == null ? "" : group,
                        page, Constants.PAGE_SIZE).execute();
                if (!response.isSuccessful()) {
                    deliver(callback, Result.failure(ApiErrors.parse(response).error));
                    return;
                }
                Dtos.PageDto body = response.body();
                if (body != null && body.items != null) cache(body.items);
                deliver(callback, Result.success(body));
            } catch (IOException e) {
                // No server. The cached LiveData the screen already shows stays
                // on screen, labelled as cached.
                deliver(callback, Result.offline());
            }
        });
    }

    @Override
    public void loadMe(Result.Callback<Dtos.MeDto> callback) {
        executors.networkIO().execute(() -> {
            try {
                Response<Dtos.MeDto> response = api.me().execute();
                if (!response.isSuccessful()) {
                    deliver(callback, Result.failure(ApiErrors.parse(response).error));
                    return;
                }
                Dtos.MeDto me = response.body();
                if (me != null) {
                    List<Dtos.StudentDto> one = new ArrayList<>();
                    one.add(me);
                    cache(one);
                }
                deliver(callback, Result.success(me));
            } catch (IOException e) {
                deliver(callback, Result.offline());
            }
        });
    }

    @Override
    public void loadGroups(Result.Callback<Dtos.GroupsResponse> callback) {
        executors.networkIO().execute(() -> {
            try {
                Response<Dtos.GroupsResponse> response = api.groups().execute();
                if (!response.isSuccessful()) {
                    deliver(callback, Result.failure(ApiErrors.parse(response).error));
                    return;
                }
                deliver(callback, Result.success(response.body()));
            } catch (IOException e) {
                deliver(callback, Result.offline());
            }
        });
    }

    @Override
    public void loadProgrammes(Result.Callback<Dtos.ProgrammesResponse> callback) {
        executors.networkIO().execute(() -> {
            try {
                Response<Dtos.ProgrammesResponse> response = api.programmes().execute();
                if (!response.isSuccessful()) {
                    deliver(callback, Result.failure(ApiErrors.parse(response).error));
                    return;
                }
                Dtos.ProgrammesResponse body = response.body();
                // Cached so the picker keeps working offline. The app never
                // invents a programme the database has not got.
                if (body != null) programmes.save(body.programmes);
                deliver(callback, Result.success(body));
            } catch (IOException e) {
                deliver(callback, Result.offline());
            }
        });
    }

    /* ═════════════════════════ writes ═════════════════════════ */

    @Override
    public void createStudent(String studentNumber, String name, Integer programId,
                              Result.Callback<String> callback) {
        final String studentId = UUID.randomUUID().toString();
        executors.diskIO().execute(() -> {
            Dtos.SyncPayload payload = new Dtos.SyncPayload();
            payload.student_id = studentId;
            payload.student_number = studentNumber.trim();
            payload.name = name.trim();
            payload.program_id = programId;

            PendingOperationEntity op =
                    newOperation(studentId, Constants.OP_CREATE, payload, 0);

            // The student row and the operation that will deliver it are written
            // together or not at all.
            db.runInTransaction(() -> {
                LocalStudentEntity row = new LocalStudentEntity();
                row.studentId = studentId;
                row.studentNumber = studentNumber.trim();
                row.studentName = name.trim();
                row.programme = programmes.codeFor(programId);
                row.programId = programId;
                row.version = 0;                 // the server assigns version 1
                row.isDeleted = false;
                row.accountId = account();
                row.localStatus = Constants.LOCAL_SAVED_LOCAL;
                students.upsert(row);
                operations.insert(op);
            });

            SyncScheduler.requestSync(app);
            deliver(callback, Result.success(studentId));
        });
    }

    @Override
    public void isNumberFreeLocally(String studentNumber, Result.Callback<Boolean> callback) {
        executors.diskIO().execute(() -> {
            int taken = students.countByNumber(studentNumber.trim(), account());
            deliver(callback, Result.success(taken == 0));
        });
    }

    @Override
    public void saveEdit(String studentId, String name, Integer programId, int baseVersion,
                         Result.Callback<Void> callback) {
        executors.diskIO().execute(() -> {
            Dtos.SyncPayload payload = new Dtos.SyncPayload();
            payload.student_id = studentId;
            payload.name = name.trim();
            payload.program_id = programId;

            PendingOperationEntity op =
                    newOperation(studentId, Constants.OP_UPDATE, payload, baseVersion);

            db.runInTransaction(() -> {
                LocalStudentEntity row = students.findSync(studentId, account());
                if (row != null) {
                    // pendingName, not studentName: the confirmed value stays
                    // untouched until the server accepts the change.
                    row.pendingName = name.trim();
                    row.localStatus = Constants.LOCAL_SAVED_LOCAL;
                    row.conflictMessage = null;
                    students.upsert(row);
                }
                operations.insert(op);
            });

            SyncScheduler.requestSync(app);
            deliver(callback, Result.success(null));
        });
    }

    @Override
    public void requestGroup(String studentId, Integer groupId, String groupLabel, int baseVersion,
                             Result.Callback<Void> callback) {
        executors.diskIO().execute(() -> {
            Dtos.SyncPayload payload = new Dtos.SyncPayload();
            payload.student_id = studentId;
            payload.group_id = groupId;

            PendingOperationEntity op =
                    newOperation(studentId, Constants.OP_ASSIGN, payload, baseVersion);

            db.runInTransaction(() -> {
                LocalStudentEntity row = students.findSync(studentId, account());
                if (row != null) {
                    // A group saved locally is a preference, not a place. The
                    // place is reserved only when the server says so, which is
                    // why labGroup is left alone here.
                    row.pendingGroup = groupLabel;
                    row.localStatus = Constants.LOCAL_SAVED_LOCAL;
                    row.conflictMessage = null;
                    students.upsert(row);
                }
                operations.insert(op);
            });

            SyncScheduler.requestSync(app);
            deliver(callback, Result.success(null));
        });
    }

    @Override
    public void deleteStudent(String studentId, Result.Callback<Boolean> callback) {
        executors.diskIO().execute(() -> {
            // Safely combined, rather than replayed: if the CREATE for this
            // student has not reached the server, the server has never heard of
            // them. Sending a create and then a delete would be two requests
            // that cancel out. Drop the whole local history instead.
            boolean neverSent = operations.unsentCreateCount(studentId) > 0;

            if (neverSent) {
                db.runInTransaction(() -> {
                    operations.deleteForStudent(studentId);
                    students.deleteRow(studentId);
                });
                deliver(callback, Result.success(Boolean.TRUE));
                return;
            }

            Dtos.SyncPayload payload = new Dtos.SyncPayload();
            payload.student_id = studentId;
            PendingOperationEntity op =
                    newOperation(studentId, Constants.OP_DELETE, payload, 0);

            db.runInTransaction(() -> {
                // Applied locally straight away. Deletion is the one operation
                // where that is safe: the server's soft delete is idempotent,
                // it has no conflict case, and the is_deleted=0 guard means a
                // replay releases the group place exactly once.
                students.markDeleted(studentId, 0);
                operations.insert(op);
            });

            SyncScheduler.requestSync(app);
            deliver(callback, Result.success(Boolean.FALSE));
        });
    }

    @Override
    public void requestNumberCorrection(String requestedNumber,
                                        Result.Callback<Dtos.SimpleResponse> callback) {
        executors.networkIO().execute(() -> {
            try {
                Response<Dtos.SimpleResponse> response = api.requestNumberCorrection(
                        new Dtos.NumberCorrectionRequest(requestedNumber.trim())).execute();
                if (!response.isSuccessful()) {
                    deliver(callback, Result.failure(ApiErrors.parse(response).error));
                    return;
                }
                deliver(callback, Result.success(response.body()));
            } catch (IOException e) {
                deliver(callback, Result.offline());
            }
        });
    }

    /* ═════════════════════════ conflict resolution ═════════════════════════ */

    @Override
    public void retryWithServerVersion(String operationId, Result.Callback<Void> callback) {
        executors.diskIO().execute(() -> {
            PendingOperationEntity op = operations.findSync(operationId);
            if (op == null || op.serverVersion == null) {
                deliver(callback, Result.failure("NOT_FOUND"));
                return;
            }
            // A NEW operation id, because the content is now being applied on
            // top of a different base version. Reusing the old id with changed
            // content is exactly what the server rejects, and rightly.
            PendingOperationEntity retry = new PendingOperationEntity();
            retry.operationId = UUID.randomUUID().toString();
            retry.studentId = op.studentId;
            retry.accountId = op.accountId;
            retry.operationType = op.operationType;
            retry.payload = op.payload;
            retry.baseVersion = op.serverVersion;
            retry.status = Constants.LOCAL_PENDING;
            retry.createdAt = System.currentTimeMillis();

            db.runInTransaction(() -> {
                operations.delete(op);
                operations.insert(retry);
                students.setStatus(op.studentId, Constants.LOCAL_PENDING, null);
            });

            SyncScheduler.requestSync(app);
            deliver(callback, Result.success(null));
        });
    }

    @Override
    public void discardOperation(String operationId, Result.Callback<Void> callback) {
        executors.diskIO().execute(() -> {
            PendingOperationEntity op = operations.findSync(operationId);
            if (op == null) {
                deliver(callback, Result.failure("NOT_FOUND"));
                return;
            }
            db.runInTransaction(() -> {
                operations.delete(op);
                students.clearPending(op.studentId);
            });
            deliver(callback, Result.success(null));
        });
    }

    /* ═════════════════════════ session ═════════════════════════ */

    @Override
    public void signOutAndWipe(Result.Callback<Void> callback) {
        final String account = account();
        executors.diskIO().execute(() -> {
            db.runInTransaction(() -> {
                students.clearAccount(account);
                operations.clearAccount(account);
            });
            tokens.clearSession();
            // Reference data is not account data, so the programme cache stays.
            deliver(callback, Result.success(null));
        });
    }

    /* ═════════════════════════ helpers ═════════════════════════ */

    private PendingOperationEntity newOperation(String studentId, String type,
                                                Dtos.SyncPayload payload, int baseVersion) {
        PendingOperationEntity op = new PendingOperationEntity();
        op.operationId = UUID.randomUUID().toString();
        op.studentId = studentId;
        op.accountId = account();
        op.operationType = type;
        op.payload = GSON.toJson(payload);
        op.baseVersion = baseVersion;
        op.status = Constants.LOCAL_PENDING;
        op.createdAt = System.currentTimeMillis();
        return op;
    }

    /** Server rows overwrite the confirmed values; local proposals are kept. */
    private void cache(List<Dtos.StudentDto> incoming) {
        final String account = account();
        executors.diskIO().execute(() -> {
            List<LocalStudentEntity> rows = new ArrayList<>();
            for (Dtos.StudentDto dto : incoming) {
                LocalStudentEntity existing = students.findSync(dto.student_id, account);
                LocalStudentEntity row = existing != null ? existing : new LocalStudentEntity();
                row.studentId = dto.student_id;
                row.studentNumber = dto.student_number;
                row.studentName = dto.name;
                row.programme = dto.program_code;
                row.programId = dto.program_id;
                row.labGroup = dto.group_label;
                row.groupId = dto.group_id;
                row.version = dto.version;
                row.isDeleted = dto.is_deleted;
                row.accountId = account;
                row.updatedAt = dto.updated_at;
                if (dto.claim_code != null) row.claimCode = dto.claim_code;
                if (row.localStatus == null || Constants.LOCAL_SYNCED.equals(row.localStatus)) {
                    row.localStatus = Constants.LOCAL_SYNCED;
                }
                rows.add(row);
            }
            students.upsertAll(rows);
        });
    }

    private <T> void deliver(Result.Callback<T> callback, Result<T> result) {
        if (callback == null) return;
        executors.mainThread().execute(() -> callback.onResult(result));
    }
}
