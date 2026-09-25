package zm.mu.ict361lab.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import zm.mu.ict361lab.data.local.entity.AccountEntity;

@Dao
public interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(AccountEntity account);

    @Query("SELECT * FROM account WHERE account_id = :accountId LIMIT 1")
    AccountEntity findSync(String accountId);

    @Query("SELECT * FROM account WHERE account_id = :accountId LIMIT 1")
    LiveData<AccountEntity> observe(String accountId);

    @Query("SELECT * FROM account ORDER BY created_at DESC")
    LiveData<List<AccountEntity>> observeAll();

    @Query("UPDATE account SET last_sync_at = :isoTimestamp WHERE account_id = :accountId")
    void setLastSync(String accountId, String isoTimestamp);

    @Query("DELETE FROM account WHERE account_id = :accountId")
    void delete(String accountId);
}