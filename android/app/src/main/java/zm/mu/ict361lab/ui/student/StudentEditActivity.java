package zm.mu.ict361lab.ui.student;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import zm.mu.ict361lab.R;
import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.databinding.ActivityStudentEditBinding;
import zm.mu.ict361lab.ui.common.ViewModelFactory;
import zm.mu.ict361lab.util.ApiErrors;
import zm.mu.ict361lab.util.ProgrammeStore;
import zm.mu.ict361lab.util.TokenStore;
import zm.mu.ict361lab.util.Validators;

/**
 * Edit my own name and programme.
 *
 * Lifecycle: the prefill-once flag and the base version are in the ViewModel's
 * SavedStateHandle, so a rotation neither re-runs the prefill over what is
 * being typed nor loses the version the edit is being made against. The text
 * itself is restored by the platform from the view id.
 */
public class StudentEditActivity extends AppCompatActivity {

    private ActivityStudentEditBinding binding;
    private StudentViewModel viewModel;
    private ProgrammeStore programmes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.title_edit_details);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        programmes = new ProgrammeStore(this);
        fillProgrammeSpinner();

        viewModel = new ViewModelProvider(this, new ViewModelFactory(this))
                .get(StudentViewModel.class);
        viewModel.bind(new TokenStore(this).studentId());
        viewModel.programmesReady().observe(this, ready -> fillProgrammeSpinner());
        viewModel.loadProgrammes();

        viewModel.student().observe(this, this::prefillOnce);
        binding.saveButton.setOnClickListener(v -> save());

        viewModel.saveState().observe(this, state -> {
            if (state == null) return;
            binding.saveButton.setEnabled(!state.loading);
            if (state.loading) return;
            if (state.isError()) {
                binding.editError.setText(getString(ApiErrors.messageFor(state.errorCode)));
                binding.editError.setVisibility(View.VISIBLE);
                viewModel.consumeSave();
            } else {
                viewModel.consumeSave();
                Toast.makeText(this, R.string.msg_saved_locally, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /**
     * Fill the form from the record exactly once.
     *
     * The guard is in the ViewModel, so it holds across a rotation. Re-filling
     * on every emission would overwrite whatever the user is halfway through
     * typing — including when a background sync updates the row underneath them.
     */
    private void prefillOnce(LocalStudentEntity student) {
        if (student == null || viewModel.isPrefilled()) return;
        viewModel.markPrefilled(student.version);
        binding.nameInput.setText(student.displayName());
        binding.programmeSpinner.setSelection(programmes.positionOf(student.programme));
    }

    private void fillProgrammeSpinner() {
        List<String> codes = programmes.codes();
        if (codes.isEmpty()) {
            binding.programmeSpinner.setEnabled(false);
            binding.programmeSpinner.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{ getString(R.string.label_programme_unknown) }));
            binding.editError.setVisibility(View.VISIBLE);
            binding.editError.setText(R.string.err_programmes_unavailable);
            return;
        }
        binding.programmeSpinner.setEnabled(true);
        int keep = binding.programmeSpinner.getSelectedItemPosition();
        binding.programmeSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, codes));
        if (keep >= 0 && keep < codes.size()) binding.programmeSpinner.setSelection(keep);
    }

    private void save() {
        String name = binding.nameInput.getText() == null
                ? "" : binding.nameInput.getText().toString();
        binding.nameField.setError(null);
        if (!Validators.name(name)) {
            binding.nameField.setError(getString(R.string.err_name_length));
            return;   // what was typed stays on screen
        }
        binding.editError.setVisibility(View.GONE);
        Integer programId = null;
        if (binding.programmeSpinner.isEnabled()) {
            Object selected = binding.programmeSpinner.getSelectedItem();
            if (selected != null) programId = programmes.idFor(selected.toString());
        }
        viewModel.save(name, programId);
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
