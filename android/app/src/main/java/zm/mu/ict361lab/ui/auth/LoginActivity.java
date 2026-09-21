package zm.mu.ict361lab.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import zm.mu.ict361lab.R;
import zm.mu.ict361lab.databinding.ActivityLoginBinding;
import zm.mu.ict361lab.ui.common.ViewModelFactory;
import zm.mu.ict361lab.ui.lecturer.RosterActivity;
import zm.mu.ict361lab.ui.student.StudentProfileActivity;
import zm.mu.ict361lab.util.ApiErrors;
import zm.mu.ict361lab.util.Validators;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.title_sign_in);

        viewModel = new ViewModelProvider(this, new ViewModelFactory(this))
                .get(AuthViewModel.class);

        binding.signInButton.setOnClickListener(v -> attempt());
        binding.goRegisterButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        viewModel.state().observe(this, state -> {
            if (state == null) return;
            binding.loginProgress.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            binding.signInButton.setEnabled(!state.loading);

            if (state.isError()) {
                showError(state.offline
                        ? getString(R.string.err_offline)
                        : getString(ApiErrors.messageFor(state.errorCode)));
                return;
            }
            if (state.data != null) {
                viewModel.consume();
                Class<?> home = "LECTURER".equals(state.data.role)
                        ? RosterActivity.class : StudentProfileActivity.class;
                startActivity(new Intent(this, home)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });
    }

    private void attempt() {
        String email = text(binding.emailInput.getText());
        String password = text(binding.passwordInput.getText());

        // Field-level errors, shown on the field itself. Nothing typed is
        // cleared: the user corrects one field, not the whole form.
        binding.emailField.setError(null);
        binding.passwordField.setError(null);
        boolean valid = true;
        if (!Validators.notEmpty(email)) {
            binding.emailField.setError(getString(R.string.err_email));
            valid = false;
        }
        if (!Validators.notEmpty(password)) {
            binding.passwordField.setError(getString(R.string.err_required));
            valid = false;
        }
        if (!valid) return;

        binding.loginError.setVisibility(View.GONE);
        viewModel.signIn(email, password);
    }

    private void showError(String message) {
        binding.loginError.setText(message);
        binding.loginError.setVisibility(View.VISIBLE);
    }

    private static String text(CharSequence value) {
        return value == null ? "" : value.toString();
    }
}
