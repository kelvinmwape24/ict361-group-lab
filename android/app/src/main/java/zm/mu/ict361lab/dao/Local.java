package zm.mu.ict361lab.dao;

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
    void insert(LocalStudentEntity student);

    @Update
    void update(LocalStudentEntity student);

    @Query("SELECT * FROM local_student WHERE student_id = :studentId AND account_id = :accountId LIMIT 1")
    LocalStudentEntity getByIdSync(String studentId, String accountId);

    @Query("SELECT * FROM local_student WHERE account_id = :accountId AND is_deleted = 0 ORDER BY student_name")
    LiveData<List<LocalStudentEntity>> getAllForAccount(String accountId);

    @Query("SELECT * FROM local_student WHERE account_id = :accountId AND is_deleted = 0 AND " +
            "(student_name LIKE '%' || :query || '%' OR student_number LIKE '%' || :query || '%')")
    LiveData<List<LocalStudentEntity>> search(String accountId, String query);

    @Query("UPDATE local_student SET local_status = :status WHERE student_id = :studentId")
    void updateLocalStatus(String studentId, String status);

    @Query("SELECT COUNT(*) FROM local_student WHERE student_number = :studentNumber AND is_deleted = 0")
    int countByStudentNumber(String studentNumber);

    @Query("DELETE FROM local_student WHERE account_id = :accountId")
    void clearForAccount(String accountId);
}