package zm.mu.ict361lab.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import zm.mu.ict361lab.data.local.entity.PendingOperationEntity;

@Dao
public interface PendingOperationDao {

    @Insert
    void insert(PendingOperationEntity operation);

    @Query("SELECT * FROM pending_operation WHERE account_id = :accountId AND status = 'PENDING' " +
            "ORDER BY created_at ASC")
    List<PendingOperationEntity> getPendingForAccountSync(String accountId);

    @Query("SELECT * FROM pending_operation WHERE account_id = :accountId ORDER BY created_at ASC")
    LiveData<List<PendingOperationEntity>> observeForAccount(String accountId);

    @Query("UPDATE pending_operation SET status = :status WHERE operation_id = :operationId")
    void updateStatus(String operationId, String status);

    @Query("DELETE FROM pending_operation WHERE operation_id = :operationId")
    void deleteById(String operationId);

    @Query("DELETE FROM pending_operation WHERE account_id = :accountId")
    void clearForAccount(String accountId);
}