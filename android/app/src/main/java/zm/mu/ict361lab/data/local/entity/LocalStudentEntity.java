package zm.mu.ict361lab.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One student as this phone knows them.
 *
 * Two deliberate choices, both required by the brief:
 *
 * 1. The index on student_number is NOT unique and IS scoped by account.
 *    A soft-deleted student keeps their number reserved on the server, so the
 *    same number can legitimately appear twice locally, and two accounts on
 *    one phone can each cache the same student.
 *
 * 2. Confirmed values and pending changes are separate columns. studentName is
 *    what the server last confirmed; pendingName is what the user typed and we
 *    have not had accepted yet. Overwriting the confirmed value with a proposal
 *    is how a conflict silently destroys someone else's work.
 */
@Entity(
        tableName = "local_student",
        indices = {
                @Index(value = {"account_id"}),
                @Index(value = {"account_id", "student_number"})
        }
)
public class LocalStudentEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "student_id")
    public String studentId = "";

    @ColumnInfo(name = "student_number") public String studentNumber;
    @ColumnInfo(name = "student_name")   public String studentName;
    @ColumnInfo(name = "programme")      public String programme;
    @ColumnInfo(name = "program_id")     public Integer programId;
    @ColumnInfo(name = "lab_group")      public String labGroup;
    @ColumnInfo(name = "group_id")       public Integer groupId;
    @ColumnInfo(name = "version")        public int version;
    @ColumnInfo(name = "is_deleted")     public boolean isDeleted;
    @ColumnInfo(name = "account_id")     public String accountId;

    /** SYNCED · SAVED_LOCAL · PENDING · SYNCING · ACTION_REQUIRED */
    @ColumnInfo(name = "local_status")   public String localStatus;

    @ColumnInfo(name = "pending_name")   public String pendingName;
    @ColumnInfo(name = "pending_group")  public String pendingGroup;
    @ColumnInfo(name = "updated_at")     public String updatedAt;

    /** Added in schema version 2 — see AppDatabase.MIGRATION_1_2. */
    @ColumnInfo(name = "conflict_message") public String conflictMessage;

    /**
     * Added in schema version 3 — see AppDatabase.MIGRATION_2_3.
     *
     * The code the student registers with. The server issues it, so a student
     * the lecturer created while offline has none until the CREATE syncs. It is
     * stored because the lecturer has to read it out afterwards, and a code
     * shown once in a dialog and never again is a code that gets lost.
     *
     * Visible only to a lecturer: the /sync/changes response omits it for a
     * student's own device.
     */
    @ColumnInfo(name = "claim_code")       public String claimCode;

    /** What the list should show: the proposal if there is one, else the confirmed value. */
    public String displayName() {
        return pendingName != null ? pendingName : studentName;
    }

    public String displayGroup() {
        return pendingGroup != null ? pendingGroup : labGroup;
    }
}
