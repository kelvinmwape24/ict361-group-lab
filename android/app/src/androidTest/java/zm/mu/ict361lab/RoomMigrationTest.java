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
import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.local.entity.PendingOperationEntity;

/**
 * Migrating 1 → 2 → 3 must not lose a thing.
 *
 * The brief asks for one Room migration tested "without deleting existing or
 * pending data". There are now two migrations, and this replays both in one go
 * over a seeded version 1 database — so it also proves the chain works, which
 * is what a real user upgrading two versions at once will hit.
 *
 * Seeded on purpose: a student carrying an unsent edit, AND the queued
 * operation that will deliver it. Both must survive.
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

        // Open at version 3 through Room, with both migrations registered.
        AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, DB_NAME)
                .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
                .allowMainThreadQueries()
                .build();

        // ── The saved work is still there, and still unsent ──
        LocalStudentEntity student = db.studentDao().findSync("s-1", "acct-1");
        assertNotNull("the student row must survive both migrations", student);
        assertEquals("Kelvin Mwape", student.studentName);
        assertEquals("the unsent proposal must survive", "Kelvin M. Mwape", student.pendingName);
        assertEquals(4, student.version);
        assertEquals("SAVED_LOCAL", student.localStatus);

        PendingOperationEntity op = db.operationDao().findSync("op-1");
        assertNotNull("the queued operation must survive both migrations", op);
        assertEquals("s-1", op.studentId);
        assertEquals(4, op.baseVersion);
        assertEquals("PENDING", op.status);
        assertNull("no receipt yet — it has not reached the server", op.resultJson);

        // ── The version 2 column exists and defaults to null ──
        assertNull(student.conflictMessage);

        // ── The version 3 columns exist and default to null ──
        assertNull("claim_code is new in v3", student.claimCode);
        assertNull("server_snapshot is new in v3", op.serverSnapshot);
        assertColumnIsNull(db, "local_student", "claim_code", "student_id", "s-1");
        assertColumnIsNull(db, "pending_operation", "server_snapshot", "operation_id", "op-1");

        // ── And the migrated database still works ──
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

    /** Builds a version 1 database and puts real work in it. */
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
                "('op-1','s-1','acct-1','UPDATE','{\"name\":\"Kelvin M. Mwape\"}',4," +
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
