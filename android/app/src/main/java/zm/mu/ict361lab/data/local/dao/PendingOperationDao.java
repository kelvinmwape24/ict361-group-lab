package zm.mu.ict361lab.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import zm.mu.ict361lab.data.local.entity.PendingOperationEntity;

@Dao
public interface PendingOperationDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(PendingOperationEntity operation);

    @Delete
    void delete(PendingOperationEntity operation);

    /**
     * Order matters. A lecturer's offline create, edit and delete of the same
     * student must replay in the order they were performed, so the queue is
     * drained strictly by creation time and never in parallel.
     */
    @Query("SELECT * FROM pending_operation " +
            "WHERE account_id = :accountId AND status IN ('PENDING','SYNCING') " +
            "ORDER BY created_at ASC")
    List<PendingOperationEntity> dueSync(String accountId);

    @Query("SELECT * FROM pending_operation WHERE account_id = :accountId ORDER BY created_at DESC")
    LiveData<List<PendingOperationEntity>> observeAll(String accountId);

    @Query("SELECT COUNT(*) FROM pending_operation " +
            "WHERE account_id = :accountId AND status IN ('PENDING','SYNCING')")
    LiveData<Integer> countOutstanding(String accountId);

    @Query("SELECT * FROM pending_operation WHERE operation_id = :operationId LIMIT 1")
    PendingOperationEntity findSync(String operationId);

    @Query("SELECT * FROM pending_operation WHERE student_id = :studentId ORDER BY created_at ASC")
    List<PendingOperationEntity> forStudentSync(String studentId);

    /**
     * Is there an unsent CREATE for this student? If so the server has never
     * heard of them, which is what makes local coalescing safe.
     */
    @Query("SELECT COUNT(*) FROM pending_operation " +
            "WHERE student_id = :studentId AND operation_type = 'CREATE' AND result_json IS NULL")
    int unsentCreateCount(String studentId);

    @Query("UPDATE pending_operation SET status = :status WHERE operation_id = :operationId")
    void setStatus(String operationId, String status);

    @Query("UPDATE pending_operation SET status = :status, last_error = :error, " +
            "server_version = :serverVersion WHERE operation_id = :operationId")
    void setFailure(String operationId, String status, String error, Integer serverVersion);

    /** A conflict keeps the local proposal AND records what the server holds. */
    @Query("UPDATE pending_operation SET status = 'ACTION_REQUIRED', last_error = :error, " +
            "server_version = :serverVersion, server_snapshot = :snapshot " +
            "WHERE operation_id = :operationId")
    void setConflict(String operationId, String error, Integer serverVersion, String snapshot);

    /** Stores the receipt with the operation so a repeat cannot double-apply. */
    @Query("UPDATE pending_operation SET status = 'SYNCED', result_json = :resultJson, " +
            "last_error = NULL WHERE operation_id = :operationId")
    void setReceipt(String operationId, String resultJson);

    @Query("DELETE FROM pending_operation WHERE student_id = :studentId")
    void deleteForStudent(String studentId);

    @Query("DELETE FROM pending_operation WHERE account_id = :accountId AND status = 'SYNCED'")
    void pruneSynced(String accountId);

    @Query("DELETE FROM pending_operation WHERE account_id = :accountId")
    void clearAccount(String accountId);
}
