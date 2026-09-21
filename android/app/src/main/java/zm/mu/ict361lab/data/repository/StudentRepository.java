package zm.mu.ict361lab.data.repository;

import androidx.lifecycle.LiveData;

import java.util.List;

import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.local.entity.PendingOperationEntity;
import zm.mu.ict361lab.data.remote.Dtos;

/**
 * Everything the screens are allowed to ask for.
 *
 * This is an interface so the ViewModels can be tested against a fake — the
 * competency evidence Activity B asks for. A ViewModel that reaches for Room or
 * Retrofit directly cannot be tested without a device, which is the point.
 *
 * Every write below is offline-first: it lands in Room and in the operation
 * queue, and the server sees it whenever there is a connection. Nothing in the
 * UI needs to know whether the network is up.
 */
public interface StudentRepository {

    /* ═════════ reads from Room — always available ═════════ */

    LiveData<List<LocalStudentEntity>> cachedRoster(String query, String programme, String group);

    LiveData<LocalStudentEntity> cachedStudent(String studentId);

    LiveData<List<PendingOperationEntity>> queue();

    LiveData<Integer> outstandingCount();

    /* ═════════ network reads, cached into Room on the way past ═════════ */

    void loadRoster(String query, String programme, String group, int page,
                    Result.Callback<Dtos.PageDto> callback);

    void loadMe(Result.Callback<Dtos.MeDto> callback);

    void loadGroups(Result.Callback<Dtos.GroupsResponse> callback);

    /** Stored reference data. Cached, so the programme picker survives offline. */
    void loadProgrammes(Result.Callback<Dtos.ProgrammesResponse> callback);

    /* ═════════ writes — all queued, all offline-capable ═════════ */

    /**
     * Creates a student locally and queues the CREATE.
     *
     * The student_id is generated on the phone, so the record keeps one
     * identity whether it was created online or offline — which is what lets a
     * create, an edit and a delete of the same student replay in order.
     * Answers with the new student_id.
     */
    void createStudent(String studentNumber, String name, Integer programId,
                       Result.Callback<String> callback);

    /**
     * Early feedback on a duplicate number, checked against the rows this phone
     * holds. The server's unique index is still the authority — this only saves
     * the user a round trip and a rejection they could have been told about
     * while typing.
     */
    void isNumberFreeLocally(String studentNumber, Result.Callback<Boolean> callback);

    /**
     * Saves the proposed edit and its pending operation in ONE Room
     * transaction, so a crash between the two is impossible.
     */
    void saveEdit(String studentId, String name, Integer programId, int baseVersion,
                  Result.Callback<Void> callback);

    void requestGroup(String studentId, Integer groupId, String groupLabel, int baseVersion,
                      Result.Callback<Void> callback);

    /**
     * Queues a soft delete.
     *
     * If the student's CREATE has not reached the server yet, the whole history
     * is dropped locally instead — there is nothing remote to delete, so
     * sending a create followed by a delete would be two pointless requests.
     * Answers true when it coalesced that way.
     */
    void deleteStudent(String studentId, Result.Callback<Boolean> callback);

    void requestNumberCorrection(String requestedNumber, Result.Callback<Dtos.SimpleResponse> callback);

    /* ═════════ conflict resolution ═════════ */

    /** Re-queue the local proposal against the server's current version. */
    void retryWithServerVersion(String operationId, Result.Callback<Void> callback);

    /** Throw the local proposal away and keep what the server holds. */
    void discardOperation(String operationId, Result.Callback<Void> callback);

    /* ═════════ session ═════════ */

    /** Clears this account's rows and queue. Nothing leaks into the next session. */
    void signOutAndWipe(Result.Callback<Void> callback);
}
