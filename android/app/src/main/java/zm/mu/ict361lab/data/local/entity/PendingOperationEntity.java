package zm.mu.ict361lab.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One mutation waiting to reach the server.
 *
 * operationId is generated once, on the phone, and reused on every retry. The
 * server stores it as a receipt, so a lost response costs a duplicate request
 * and never a duplicate effect — resultJson below is our durable copy of that
 * receipt.
 *
 * baseVersion is the version the edit was made against. The server compares it
 * before accepting, which is what turns a blind overwrite into a conflict.
 */
@Entity(
        tableName = "pending_operation",
        indices = {
                @Index(value = {"student_id"}),
                @Index(value = {"account_id", "status"}),
                @Index(value = {"created_at"})
        }
)
public class PendingOperationEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "operation_id")
    public String operationId = "";

    @ColumnInfo(name = "student_id")     public String studentId;
    @ColumnInfo(name = "account_id")     public String accountId;

    /** CREATE · UPDATE · ASSIGN · DELETE */
    @ColumnInfo(name = "operation_type") public String operationType;

    @ColumnInfo(name = "payload")        public String payload;
    @ColumnInfo(name = "base_version")   public int baseVersion;

    /** PENDING · SYNCING · SYNCED · ACTION_REQUIRED */
    @ColumnInfo(name = "status")         public String status;

    @ColumnInfo(name = "created_at")     public long createdAt;

    /** The server's answer, stored durably so a repeat request is a no-op. */
    @ColumnInfo(name = "result_json")    public String resultJson;

    @ColumnInfo(name = "last_error")     public String lastError;

    /** Set when the server rejected the edit because the record moved on. */
    @ColumnInfo(name = "server_version") public Integer serverVersion;

    /**
     * Added in schema version 3.
     *
     * The record as the server holds it, captured at the moment of a conflict.
     * Activity E: "On conflict, preserve the local proposal and show the current
     * server record for review." Without this the review screen can only say
     * that something changed, not what.
     */
    @ColumnInfo(name = "server_snapshot") public String serverSnapshot;

    /** True once the server has confirmed the student exists remotely. */
    public boolean isLocalOnlyCreate() {
        return "CREATE".equals(operationType) && resultJson == null;
    }
}
