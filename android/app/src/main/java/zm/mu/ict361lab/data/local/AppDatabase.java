package zm.mu.ict361lab.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import zm.mu.ict361lab.data.local.dao.AccountDao;
import zm.mu.ict361lab.data.local.dao.LocalStudentDao;
import zm.mu.ict361lab.data.local.dao.PendingOperationDao;
import zm.mu.ict361lab.data.local.entity.AccountEntity;
import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.local.entity.PendingOperationEntity;

@Database(
        entities = { LocalStudentEntity.class, PendingOperationEntity.class, AccountEntity.class },
        version = 4,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract LocalStudentDao studentDao();
    public abstract PendingOperationDao operationDao();
    public abstract AccountDao accountDao();

    private static volatile AppDatabase instance;

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "ict361_lab.db")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                            .build();
                }
            }
        }
        return instance;
    }

    public static AppDatabase inMemory(Context context) {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
    }

    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE local_student ADD COLUMN conflict_message TEXT");
        }
    };

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE pending_operation ADD COLUMN server_snapshot TEXT");
            database.execSQL("ALTER TABLE local_student ADD COLUMN claim_code TEXT");
        }
    };

    /**
     * Version 3 to 4.
     *
     * Adds the account table. Nothing about local_student or pending_operation
     * changes — they already carried account_id as a plain scoping column —
     * this migration only adds a new table alongside them, so it is additive
     * in the same spirit as the two before it.
     */
    public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `account` (" +
                            "`account_id` TEXT NOT NULL, `role` TEXT, `identity` TEXT, " +
                            "`created_at` INTEGER NOT NULL, `last_sync_at` TEXT, " +
                            "PRIMARY KEY(`account_id`))"
            );
        }
    };

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