package zm.mu.ict361lab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;

import androidx.room.Room;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import zm.mu.ict361lab.data.local.AppDatabase;
import zm.mu.ict361lab.data.local.entity.AccountEntity;
import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.local.entity.PendingOperationEntity;

/**
 * Migrating 1 → 2 → 3 → 4 must not lose a thing.
 *
 * There are now three migrations; this replays all of them over a seeded
 * version 1 database in one go, so it also proves the whole chain works —
 * what a user upgrading several versions at once will actually hit.
 *
 * Run with:  ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4.class)
public class RoomMigrationTest {

    private static final String DB_NAME = "migration-test.db";

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase(DB_NAME);
    }

    @Test
    public void migratingFromV1KeepsStudentsAndTheQueue() {
        seedVersion1();

        AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3,
                        AppDatabase.MIGRATION_3_4)
                .allowMainThreadQueries()
                .build();

        LocalStudentEntity student = db.studentDao().findSync("s-1", "acct-1");
        assertNotNull("the student row must survive all three migrations", student);
        assertEquals("Kelvin Mwape", student.studentName);
        assertEquals("the unsent proposal must survive", "Kelvin M. Mwape", student.pendingName);
        assertEquals(4, student.version);
        assertEquals("SAVED_LOCAL", student.localStatus);

        PendingOperationEntity op = db.operationDao().findSync("op-1");
        assertNotNull("the queued operation must survive all three migrations", op);
        assertEquals("s-1", op.studentId);
        assertEquals(4, op.baseVersion);
        assertEquals("PENDING", op.status);
        assertNull("no receipt yet — it has not reached the server", op.resultJson);

        assertNull(student.conflictMessage);
        assertNull("claim_code is new in v3", student.claimCode);
        assertNull("server_snapshot is new in v3", op.serverSnapshot);
        assertColumnIsNull(db, "local_student", "claim_code", "student_id", "s-1");
        assertColumnIsNull(db, "pending_operation", "server_snapshot", "operation_id", "op-1");

        // ── The version 4 account table exists, and starts empty ──
        assertNull("no account row was ever written pre-v4", db.accountDao().findSync("acct-1"));

        student.conflictMessage = "CONFLICT";
        student.claimCode = "MU-000001";
        student.localStatus = "ACTION_REQUIRED";
        db.studentDao().upsert(student);

        LocalStudentEntity reread = db.studentDao().findSync("s-1", "acct-1");
        assertEquals("CONFLICT", reread.conflictMessage);
        assertEquals("MU-000001", reread.claimCode);

        db.operationDao().setConflict("op-1", "CONFLICT", 5,
                "{\"student_id\":\"s-1\",\"name\":\"Kelvin Mwape\",\"version\":5}");
        PendingOperationEntity conflicted = db.operationDao().findSync("op-1");
        assertEquals("ACTION_REQUIRED", conflicted.status);
        assertEquals(Integer.valueOf(5), conflicted.serverVersion);
        assertTrue("the server's record is kept for review",
                conflicted.serverSnapshot.contains("Kelvin Mwape"));
        assertEquals("the local proposal is preserved, not overwritten",
                "Kelvin M. Mwape", db.studentDao().findSync("s-1", "acct-1").pendingName);

        db.close();
    }

    /**
     * The account table is new, not migrated data, so this checks it
     * independently: write through the migrated database, close it, reopen
     * it, and confirm the row is still there.
     */
    @Test
    public void v4AccountTableWorksAndSurvivesReopen() {
        seedVersion1();

        AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3,
                        AppDatabase.MIGRATION_3_4)
                .allowMainThreadQueries()
                .build();

        AccountEntity account = new AccountEntity();
        account.accountId = "acct-1";
        account.role = "STUDENT";
        account.identity = "202203897";
        account.createdAt = 1758355200000L;
        db.accountDao().upsert(account);
        db.close();

        AppDatabase reopened = Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3,
                        AppDatabase.MIGRATION_3_4)
                .allowMainThreadQueries()
                .build();

        AccountEntity reread = reopened.accountDao().findSync("acct-1");
        assertNotNull("the account row must survive closing and reopening the database", reread);
        assertEquals("STUDENT", reread.role);
        assertEquals("202203897", reread.identity);
        assertEquals(1758355200000L, reread.createdAt);
        assertNull("last_sync_at is null until the first successful sync", reread.lastSyncAt);

        assertNotNull(reopened.studentDao().findSync("s-1", "acct-1"));

        reopened.accountDao().setLastSync("acct-1", "2026-09-21T20:00:00Z");
        assertEquals("2026-09-21T20:00:00Z",
                reopened.accountDao().findSync("acct-1").lastSyncAt);

        reopened.close();
    }

    private void seedVersion1() {
        SupportSQLiteOpenHelper.Configuration config =
                SupportSQLiteOpenHelper.Configuration.builder(context)
                        .name(DB_NAME)
                        .callback(new SupportSQLiteOpenHelper.Callback(1) {
                            @Override public void onCreate(SupportSQLiteDatabase db) {
                                for (String statement : AppDatabase.SCHEMA_V1) db.execSQL(statement);
                            }
                            @Override public void onUpgrade(SupportSQLiteDatabase db, int from, int to) { }
                        })
                        .build();

        SupportSQLiteOpenHelper helper = new FrameworkSQLiteOpenHelperFactory().create(config);
        SupportSQLiteDatabase v1 = helper.getWritableDatabase();

        v1.execSQL("INSERT INTO local_student (student_id, student_number, student_name, " +
                "programme, program_id, lab_group, group_id, version, is_deleted, account_id, " +
                "local_status, pending_name, pending_group, updated_at) VALUES " +
                "('s-1','202203897','Kelvin Mwape','CS',1,'G01',1,4,0,'acct-1'," +
                "'SAVED_LOCAL','Kelvin M. Mwape',NULL,'2026-09-20T10:00:00Z')");

        v1.execSQL("INSERT INTO pending_operation (operation_id, student_id, account_id, " +
                "operation_type, payload, base_version, status, created_at, result_json, " +
                "last_error, server_version) VALUES " +
                "('op-1','s-1','acct-1','UPDATE','{\\\"name\\\":\\\"Kelvin M. Mwape\\\"}',4," +
                "'PENDING',1758355200000,NULL,NULL,NULL)");

        v1.close();
        helper.close();
    }

    private void assertColumnIsNull(AppDatabase db, String table, String column,
                                    String keyColumn, String keyValue) {
        Cursor cursor = db.getOpenHelper().getReadableDatabase()
                .query("SELECT " + column + " FROM " + table
                        + " WHERE " + keyColumn + " = '" + keyValue + "'");
        assertTrue(cursor.moveToFirst());
        assertTrue(column + " should default to null on a migrated row", cursor.isNull(0));
        cursor.close();
    }
}