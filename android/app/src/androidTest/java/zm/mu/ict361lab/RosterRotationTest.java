package zm.mu.ict361lab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import zm.mu.ict361lab.ui.lecturer.RosterActivity;

/**
 * Rotation tests for the lecturer roster.
 *
 * This is the evidence Activity B asks for: "a demonstration that form/search
 * state survives rotation". ActivityScenario.recreate() destroys the Activity
 * and rebuilds it from its saved state, which is exactly what a device rotation
 * does — without needing a human to turn a phone on camera.
 *
 * No server is needed. The roster reads from Room and the network call simply
 * fails, which is the offline path these tests happen to exercise as well.
 *
 * Run with:  ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4.class)
public class RosterRotationTest {

    private static final String PREFS = "ict361_session";
    private static final String REFERENCE = "ict361_reference";

    @Before
    public void signInAsLecturer() {
        Context context = ApplicationProvider.getApplicationContext();

        // A token the app accepts locally. The server never sees it in this
        // test; the screen only needs to believe a lecturer is signed in.
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("token", "test-token")
                .putString("role", "LECTURER")
                .putString("account_id", "test-lecturer")
                .apply();

        // Seed the programme reference cache so the filter has real options,
        // as it would after one online session.
        context.getSharedPreferences(REFERENCE, Context.MODE_PRIVATE).edit()
                .putString("programmes",
                        "[{\"program_id\":1,\"code\":\"CS\"},"
                        + "{\"program_id\":2,\"code\":\"IT\"},"
                        + "{\"program_id\":3,\"code\":\"DS\"}]")
                .apply();
    }

    @After
    public void signOut() {
        Context context = ApplicationProvider.getApplicationContext();
        for (String file : new String[]{PREFS, REFERENCE}) {
            SharedPreferences prefs = context.getSharedPreferences(file, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        }
    }

    @Test
    public void searchTextSurvivesRotation() {
        try (ActivityScenario<RosterActivity> scenario =
                     ActivityScenario.launch(RosterActivity.class)) {

            scenario.onActivity(activity -> {
                EditText search = activity.findViewById(R.id.search_input);
                assertNotNull(search);
                search.setText("mwape");
            });
            waitForIdle();

            scenario.recreate();          // the rotation

            scenario.onActivity(activity -> {
                EditText search = activity.findViewById(R.id.search_input);
                assertEquals("the search text must survive a rotation",
                        "mwape", search.getText().toString());
            });
        }
    }

    @Test
    public void programmeFilterSurvivesRotation() {
        try (ActivityScenario<RosterActivity> scenario =
                     ActivityScenario.launch(RosterActivity.class)) {

            scenario.onActivity(activity -> {
                Spinner programme = activity.findViewById(R.id.programme_spinner);
                assertNotNull(programme);
                // Position 0 is "All", so 2 selects the second real programme.
                programme.setSelection(2);
            });
            waitForIdle();

            scenario.recreate();

            scenario.onActivity(activity -> {
                Spinner programme = activity.findViewById(R.id.programme_spinner);
                assertEquals("the programme filter must survive a rotation",
                        2, programme.getSelectedItemPosition());
            });
        }
    }

    @Test
    public void allThreeFiltersSurviveTogether() {
        try (ActivityScenario<RosterActivity> scenario =
                     ActivityScenario.launch(RosterActivity.class)) {

            scenario.onActivity(activity -> {
                ((EditText) activity.findViewById(R.id.search_input)).setText("banda");
                ((Spinner) activity.findViewById(R.id.programme_spinner)).setSelection(1);
                ((Spinner) activity.findViewById(R.id.group_spinner)).setSelection(1);
            });
            waitForIdle();

            scenario.recreate();

            scenario.onActivity(activity -> {
                assertEquals("banda",
                        ((EditText) activity.findViewById(R.id.search_input)).getText().toString());
                assertEquals(1,
                        ((Spinner) activity.findViewById(R.id.programme_spinner)).getSelectedItemPosition());
                assertEquals("Unassigned is position 1 in the group filter",
                        1, ((Spinner) activity.findViewById(R.id.group_spinner)).getSelectedItemPosition());
            });
        }
    }

    @Test
    public void rotatingTwiceIsStillStable() {
        // Two rotations in a row catch the class of bug where a restore writes
        // back a default and the second recreation persists it.
        try (ActivityScenario<RosterActivity> scenario =
                     ActivityScenario.launch(RosterActivity.class)) {

            scenario.onActivity(activity ->
                    ((EditText) activity.findViewById(R.id.search_input)).setText("chanda"));
            waitForIdle();

            scenario.recreate();
            waitForIdle();
            scenario.recreate();

            scenario.onActivity(activity -> assertEquals("chanda",
                    ((EditText) activity.findViewById(R.id.search_input)).getText().toString()));
        }
    }

    /** Let the posted LiveData and listener work settle before recreating. */
    private void waitForIdle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
