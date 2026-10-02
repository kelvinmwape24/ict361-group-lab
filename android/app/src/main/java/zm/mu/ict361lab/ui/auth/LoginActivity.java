package zm.mu.ict361lab.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import zm.mu.ict361lab.databinding.ActivityLoginBinding;
import zm.mu.ict361lab.ui.lecturer.RosterActivity;
import zm.mu.ict361lab.ui.student.DashboardActivity;
import zm.mu.ict361lab.util.Constants;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding b;
    private LoginViewModel vm;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        vm = new ViewModelProvider(this).get(LoginViewModel.class);

        b.btnLogin.setOnClickListener(v -> {
            String id = b.inputStudentId.getText().toString().trim();
            String password = b.inputPassword.getText().toString();
            if (id.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            vm.login(id + "@student.mu", password);
        });

        vm.getState().observe(this, state -> {
            if (state == null) return;
            switch (state.status) {
                case LOADING: b.btnLogin.setEnabled(false); break;
                case SUCCESS:
                    b.btnLogin.setEnabled(true);
                    getSharedPreferences(Constants.PREFS, MODE_PRIVATE).edit()
                        .putString(Constants.KEY_TOKEN, state.data.token)
                        .putString(Constants.KEY_ROLE, state.data.role)
                        .apply();
                    if ("LECTURER".equals(state.data.role))
                        startActivity(new Intent(this, RosterActivity.class));
                    else
                        startActivity(new Intent(this, DashboardActivity.class));
                    finish();
                    break;
                case ERROR:
                    b.btnLogin.setEnabled(true);
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show();
                    break;
            }
        });
    }
}
