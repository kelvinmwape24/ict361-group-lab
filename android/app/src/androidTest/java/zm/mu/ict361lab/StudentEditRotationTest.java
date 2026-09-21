package zm.mu.ict361lab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import zm.mu.ict361lab.data.local.AppDatabase;
import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.ui.student.StudentEditActivity;

/**
 * Rotation test for a form mid-edit.
 *
 * This is the case the old code only passed by accident. The prefill-once flag
 * used to be an Activity field, so recreation reset it to false, the LiveData
 * observer fired again, and setText() ran over what the user had typed. It
 * looked fine only because Android restores view state slightly later.
 *
 * With the flag in the ViewModel's SavedStateHandle the behaviour is correct by
 * construction rather than by ordering luck — and this test would catch a
 * regression either way.
 */
@RunWith(AndroidJUnit4.class)
public class StudentEditRotationTest {

    private static final String PREFS = "ict361_session";
    private static final String STUDENT_ID = "rotation-test-student";

    private AppDatabase db;

    @Before
    public void signInAndSeedARecord() {
        Context context = ApplicationProvider.getApplicationContext();

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("token", "test-token")
                .putString("role", "STUDENT")
                .putString("student_id", STUDENT_ID)
                .putString("account_id", STUDENT_ID)
                .apply();

        // A confirmed record for the screen to prefill from. The edit screen
        // reads Room, not the network, so no server is needed.
        db = AppDatabase.get(context);
        LocalStudentEntity row = new LocalStudentEntity();
        row.studentId = STUDENT_ID;
        row.studentNumber = "202001699";
        row.studentName = "Chibesa Mumbi";
        row.programme = "CS";
        row.programId = 1;
        row.version = 4;
        row.accountId = STUDENT_ID;
        row.localStatus = "SYNCED";
        db.studentDao().upsert(row);
    }

    @After
    public void cleanUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db.studentDao().clearAccount(STUDENT_ID);
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }

    @Test
    public void theFormPrefillsFromTheRecord() {
        try (ActivityScenario<StudentEditActivity> scenario =
                     ActivityScenario.launch(StudentEditActivity.class)) {
            waitForIdle();
            scenario.onActivity(activity -> {
                EditText name = activity.findViewById(R.id.name_input);
                assertNotNull(name);
                assertEquals("Chibesa Mumbi", name.getText().toString());
            });
        }
    }

    @Test
    public void halfTypedInputIsNotOverwrittenByRotation() {
        try (ActivityScenario<StudentEditActivity> scenario =
                     ActivityScenario.launch(StudentEditActivity.class)) {
            waitForIdle();

            scenario.onActivity(activity ->
                    ((EditText) activity.findViewById(R.id.name_input))
                            .setText("Chibesa M. Mum"));   // mid-word, as a user would be
            waitForIdle();

            scenario.recreate();
            waitForIdle();

            scenario.onActivity(activity -> assertEquals(
                    "the prefill must not run again and clobber what was typed",
                    "Chibesa M. Mum",
                    ((EditText) activity.findViewById(R.id.name_input)).getText().toString()));
        }
    }

    @Test
    public void aClearedFieldStaysClearedAfterRotation() {
        // The nastier version of the same bug: an empty field is easy to
        // mistake for "not prefilled yet" and refill.
        try (ActivityScenario<StudentEditActivity> scenario =
                     ActivityScenario.launch(StudentEditActivity.class)) {
            waitForIdle();

            scenario.onActivity(activity ->
                    ((EditText) activity.findViewById(R.id.name_input)).setText(""));
            waitForIdle();

            scenario.recreate();
            waitForIdle();

            scenario.onActivity(activity -> assertEquals(
                    "an emptied field must stay empty, not be refilled from the record",
                    "",
                    ((EditText) activity.findViewById(R.id.name_input)).getText().toString()));
        }
    }

    private void waitForIdle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
