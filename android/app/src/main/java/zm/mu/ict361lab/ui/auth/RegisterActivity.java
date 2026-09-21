package zm.mu.ict361lab.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;

import zm.mu.ict361lab.R;
import zm.mu.ict361lab.databinding.ActivityRegisterBinding;
import zm.mu.ict361lab.ui.common.ViewModelFactory;
import zm.mu.ict361lab.ui.student.StudentProfileActivity;
import zm.mu.ict361lab.util.ApiErrors;
import zm.mu.ict361lab.util.TokenStore;
import zm.mu.ict361lab.util.Validators;

/**
 * Registration.
 *
 * Account verification needs the server, so registering offline is not
 * possible — but losing what someone typed because the bus went through a
 * dead spot is unacceptable. The form is kept as a draft and restored.
 * The password is never part of that draft.
 */
public class RegisterActivity extends AppCompatActivity {

    /** The draft. Note what is absent: the password. */
    private static final class Draft {
        String claimCode, studentNumber, name;
    }

    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;
    private TokenStore tokens;
    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.title_register);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tokens = new TokenStore(this);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(this))
                .get(AuthViewModel.class);

        restoreDraft();

        binding.createButton.setOnClickListener(v -> attempt());

        viewModel.state().observe(this, state -> {
            if (state == null) return;
            binding.registerProgress.setVisibility(state.loading ? View.VISIBLE : View.GONE);
            binding.createButton.setEnabled(!state.loading);

            if (state.isError()) {
                if (state.offline) {
                    saveDraft();
                    showError(getString(R.string.msg_draft_saved));
                } else {
                    showError(getString(ApiErrors.messageFor(state.errorCode)));
                }
                return;
            }
            if (state.data != null) {
                viewModel.consume();
                startActivity(new Intent(this, StudentProfileActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });
    }

    private void attempt() {
        String claim = text(binding.claimInput.getText());
        String number = text(binding.numberInput.getText());
        String name = text(binding.nameInput.getText());
        String password = text(binding.passwordInput.getText());

        binding.claimField.setError(null);
        binding.numberField.setError(null);
        binding.nameField.setError(null);
        binding.passwordField.setError(null);

        boolean valid = true;
        if (!Validators.notEmpty(claim)) {
            binding.claimField.setError(getString(R.string.err_required)); valid = false;
        }
        if (!Validators.studentNumber(number)) {
            binding.numberField.setError(getString(R.string.err_student_number)); valid = false;
        }
        if (!Validators.name(name)) {
            binding.nameField.setError(getString(R.string.err_name_length)); valid = false;
        }
        if (!Validators.password(password)) {
            binding.passwordField.setError(getString(R.string.err_password_short)); valid = false;
        }
        if (!valid) return;

        binding.registerError.setVisibility(View.GONE);
        viewModel.register(claim, number, name, password);
    }

    private void saveDraft() {
        Draft draft = new Draft();
        draft.claimCode = text(binding.claimInput.getText());
        draft.studentNumber = text(binding.numberInput.getText());
        draft.name = text(binding.nameInput.getText());
        tokens.saveDraft(gson.toJson(draft));
    }

    private void restoreDraft() {
        String json = tokens.draft();
        if (json == null) return;
        Draft draft = gson.fromJson(json, Draft.class);
        if (draft == null) return;
        binding.claimInput.setText(draft.claimCode);
        binding.numberInput.setText(draft.studentNumber);
        binding.nameInput.setText(draft.name);
        Toast.makeText(this, R.string.msg_draft_restored, Toast.LENGTH_SHORT).show();
    }

    /**
     * The draft is written whenever the screen is about to lose the foreground,
     * not only when Back is pressed or a request fails offline.
     *
     * A backgrounded process can be killed at any moment. Saving here means a
     * form half filled in on the bus is still there the next morning, because
     * the draft is in SharedPreferences rather than only in the instance state
     * bundle — which the platform discards once the task is removed.
     */
    @Override
    protected void onPause() {
        super.onPause();
        saveDraft();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // The field contents are restored by the platform from the view ids;
        // this covers the case where the process does not come back at all.
        saveDraft();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Back must be predictable: leaving this screen keeps the draft.
        saveDraft();
        finish();
        return true;
    }

    private void showError(String message) {
        binding.registerError.setText(message);
        binding.registerError.setVisibility(View.VISIBLE);
    }

    private static String text(CharSequence value) {
        return value == null ? "" : value.toString();
    }
}
