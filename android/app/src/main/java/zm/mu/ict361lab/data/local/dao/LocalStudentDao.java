package zm.mu.ict361lab.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;

@Dao
public interface LocalStudentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(LocalStudentEntity student);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<LocalStudentEntity> students);

    @Update
    void update(LocalStudentEntity student);

    @Query("SELECT * FROM local_student WHERE student_id = :studentId AND account_id = :accountId LIMIT 1")
    LocalStudentEntity findSync(String studentId, String accountId);

    @Query("SELECT * FROM local_student WHERE student_id = :studentId AND account_id = :accountId LIMIT 1")
    LiveData<LocalStudentEntity> observe(String studentId, String accountId);

    @Query("SELECT * FROM local_student WHERE account_id = :accountId AND is_deleted = 0 ORDER BY student_name")
    LiveData<List<LocalStudentEntity>> observeAll(String accountId);

    /**
     * The offline roster view. Combined search + programme + group, matching
     * what the server does, so the same filters work either way.
     * groupFilter 'UNASSIGNED' means no group at all.
     */
    @Query("SELECT * FROM local_student " +
            "WHERE account_id = :accountId AND is_deleted = 0 " +
            "  AND (:query = '' OR student_name LIKE '%' || :query || '%' " +
            "                   OR student_number LIKE '%' || :query || '%') " +
            "  AND (:programme = '' OR programme = :programme) " +
            "  AND (:groupFilter = '' " +
            "       OR (:groupFilter = 'UNASSIGNED' AND lab_group IS NULL) " +
            "       OR lab_group = :groupFilter) " +
            "ORDER BY student_name")
    LiveData<List<LocalStudentEntity>> observeFiltered(
            String accountId, String query, String programme, String groupFilter);

    @Query("UPDATE local_student SET local_status = :status WHERE student_id = :studentId")
    void setStatus(String studentId, String status);

    @Query("UPDATE local_student SET local_status = :status, conflict_message = :message " +
            "WHERE student_id = :studentId")
    void setStatus(String studentId, String status, String message);

    /** Accepting the server's answer: the proposal is cleared, not merged. */
    @Query("UPDATE local_student SET pending_name = NULL, pending_group = NULL, " +
            "conflict_message = NULL, local_status = 'SYNCED' WHERE student_id = :studentId")
    void clearPending(String studentId);

    @Query("SELECT COUNT(*) FROM local_student " +
            "WHERE student_number = :studentNumber AND account_id = :accountId AND is_deleted = 0")
    int countByNumber(String studentNumber, String accountId);

    /** A deletion marker pulled from the server. The row stays, hidden. */
    @Query("UPDATE local_student SET is_deleted = 1, lab_group = NULL, group_id = NULL, " +
            "version = :version WHERE student_id = :studentId")
    void markDeleted(String studentId, int version);

    /**
     * Hard delete, used in exactly one case: a student the lecturer created
     * offline and then deleted before either change ever reached the server.
     * There is nothing on the server to soft-delete, so the local row and its
     * operations are removed outright rather than leaving a tombstone for a
     * record that never existed remotely.
     */
    @Query("DELETE FROM local_student WHERE student_id = :studentId")
    void deleteRow(String studentId);

    @Query("DELETE FROM local_student WHERE account_id = :accountId")
    void clearAccount(String accountId);
}
