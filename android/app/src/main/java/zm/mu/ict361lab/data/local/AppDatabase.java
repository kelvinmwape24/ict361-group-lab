package zm.mu.ict361lab.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import zm.mu.ict361lab.data.local.dao.LocalStudentDao;
import zm.mu.ict361lab.data.local.dao.PendingOperationDao;
import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.local.entity.PendingOperationEntity;

/**
 * The durable store. A ViewModel does not survive process death; this does.
 *
 * Note what is NOT here: fallbackToDestructiveMigration(). Dropping the
 * database on a schema change would throw away exactly what the brief asks us
 * to protect — saved work and the unsent queue. Both migrations below are
 * additive for that reason, and RoomMigrationTest replays 1 → 2 → 3 with real
 * data in place to prove it.
 */
@Database(
        entities = { LocalStudentEntity.class, PendingOperationEntity.class },
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract LocalStudentDao studentDao();
    public abstract PendingOperationDao operationDao();

    private static volatile AppDatabase instance;

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "ict361_lab.db")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .build();
                }
            }
        }
        return instance;
    }

    /** Test seam: an in-memory database for unit and instrumented tests. */
    public static AppDatabase inMemory(Context context) {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    /**
     * Version 1 to 2.
     *
     * Version 1 had nowhere to explain a rejected sync, so a conflicting row
     * could only be flagged ACTION_REQUIRED with no reason attached.
     */
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE local_student ADD COLUMN conflict_message TEXT");
        }
    };

    /**
     * Version 2 to 3.
     *
     * Two columns, both needed by features version 2 could not express:
     *
     *  - pending_operation.server_snapshot. Activity E requires that on
     *    conflict we "preserve the local proposal and show the current server
     *    record for review". Version 2 recorded only the server's version
     *    number, so the review screen could say something had changed but not
     *    what.
     *
     *  - local_student.claim_code. A lecturer can now create a student while
     *    offline, and the server issues the claim code when that CREATE syncs.
     *    The lecturer has to read it out afterwards, so it has to be stored.
     */
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE pending_operation ADD COLUMN server_snapshot TEXT");
            database.execSQL("ALTER TABLE local_student ADD COLUMN claim_code TEXT");
        }
    };

    /**
     * The exact schema Room generated for version 1. The migration test builds
     * a version 1 database from this, seeds it with a student carrying an
     * unsent edit plus the operation that will deliver it, then migrates
     * forward and checks that nothing was lost.
     */
    public static final String[] SCHEMA_V1 = {
            "CREATE TABLE IF NOT EXISTS `local_student` (" +
                    "`student_id` TEXT NOT NULL, `student_number` TEXT, `student_name` TEXT, " +
                    "`programme` TEXT, `program_id` INTEGER, `lab_group` TEXT, `group_id` INTEGER, " +
                    "`version` INTEGER NOT NULL, `is_deleted` INTEGER NOT NULL, `account_id` TEXT, " +
                    "`local_status` TEXT, `pending_name` TEXT, `pending_group` TEXT, " +
                    "`updated_at` TEXT, PRIMARY KEY(`student_id`))",
            "CREATE INDEX IF NOT EXISTS `index_local_student_account_id` " +
                    "ON `local_student` (`account_id`)",
            "CREATE INDEX IF NOT EXISTS `index_local_student_account_id_student_number` " +
                    "ON `local_student` (`account_id`, `student_number`)",
            "CREATE TABLE IF NOT EXISTS `pending_operation` (" +
                    "`operation_id` TEXT NOT NULL, `student_id` TEXT, `account_id` TEXT, " +
                    "`operation_type` TEXT, `payload` TEXT, `base_version` INTEGER NOT NULL, " +
                    "`status` TEXT, `created_at` INTEGER NOT NULL, `result_json` TEXT, " +
                    "`last_error` TEXT, `server_version` INTEGER, PRIMARY KEY(`operation_id`))",
            "CREATE INDEX IF NOT EXISTS `index_pending_operation_student_id` " +
                    "ON `pending_operation` (`student_id`)",
            "CREATE INDEX IF NOT EXISTS `index_pending_operation_account_id_status` " +
                    "ON `pending_operation` (`account_id`, `status`)",
            "CREATE INDEX IF NOT EXISTS `index_pending_operation_created_at` " +
                    "ON `pending_operation` (`created_at`)"
    };
}
