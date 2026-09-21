package zm.mu.ict361lab.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "account")
public class AccountEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "account_id")
    public String accountId = "";

    @ColumnInfo(name = "role")
    public String role;

    @ColumnInfo(name = "identity")
    public String identity;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "last_sync_at")
    public String lastSyncAt;
}
