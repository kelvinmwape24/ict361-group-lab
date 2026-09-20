package zm.mu.ict361lab.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "local_student",
        indices = {
                @Index(value = "student_number", unique = true),
                @Index(value = "account_id")
        }
)
public class LocalStudentEntity {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "student_id")
    public String studentId;

    @ColumnInfo(name = "student_number")
    public String studentNumber;

    @ColumnInfo(name = "student_name")
    public String studentName;

    @ColumnInfo(name = "programme")
    public String programme;

    @ColumnInfo(name = "lab_group")
    public String labGroup;

    @ColumnInfo(name = "version")
    public int version;

    @ColumnInfo(name = "is_deleted")
    public boolean isDeleted;

    @ColumnInfo(name = "account_id")
    public String accountId;

    @ColumnInfo(name = "local_status")
    public String localStatus;
}