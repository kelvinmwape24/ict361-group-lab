package zm.mu.ict361lab;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import zm.mu.ict361lab.databinding.ActivityMainBinding;
import zm.mu.ict361lab.ui.auth.LoginActivity;
import zm.mu.ict361lab.ui.lecturer.RosterActivity;
import zm.mu.ict361lab.ui.student.StudentProfileActivity;
import zm.mu.ict361lab.util.TokenStore;

/**
 * Decides where the app opens: the roster for a lecturer, the profile for a
 * student, the sign-in screen for anyone else.
 *
 * It renders nothing of its own — what the user sees for the moment it exists
 * is the launch theme's splash, which the system has already drawn before any
 * of this code runs.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // The manifest gives this Activity Theme.ICT361Lab.Splash, whose window
        // background is the crest on MU blue (and, on API 31+, the platform
        // splash screen). Swapping to the real theme here means the splash is
        // the launch window itself rather than a separate Activity sitting in
        // the back stack. This must run before super.onCreate.
        setTheme(R.style.Theme_ICT361Lab);
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TokenStore tokens = new TokenStore(this);
        Class<?> destination;
        if (!tokens.signedIn())        destination = LoginActivity.class;
        else if (tokens.isLecturer())  destination = RosterActivity.class;
        else                           destination = StudentProfileActivity.class;

        startActivity(new Intent(this, destination));
        finish();
    }
}
