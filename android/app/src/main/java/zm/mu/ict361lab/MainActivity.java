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
 * student, the sign-in screen for anyone else. It renders nothing of its own.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
