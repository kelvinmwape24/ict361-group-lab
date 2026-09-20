package zm.mu.ict361lab.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "pending_operation",
        indices = {
                @Index(value = "student_id"),
                @Index(value = "account_id"),
                @Index(value = "created_at")
        }
)
public class PendingOperationEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "operation_id")
    public String operationId;

    @ColumnInfo(name = "student_id")
    public String studentId;

    @ColumnInfo(name = "account_id")
    public String accountId;

    @ColumnInfo(name = "operation_type")
    public String operationType;

    @ColumnInfo(name = "payload")
    public String payload;

    @ColumnInfo(name = "base_version")
    public int baseVersion;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "created_at")
    public long createdAt;
}
