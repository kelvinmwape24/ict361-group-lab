package zm.mu.ict361lab.ui.lecturer;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import zm.mu.ict361lab.R;
import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.databinding.ActivityStudentEditorBinding;
import zm.mu.ict361lab.ui.common.ConfirmDialogFragment;
import zm.mu.ict361lab.ui.common.ViewModelFactory;
import zm.mu.ict361lab.util.ApiErrors;
import zm.mu.ict361lab.util.Constants;
import zm.mu.ict361lab.util.ProgrammeStore;
import zm.mu.ict361lab.util.Validators;

/**
 * Add a student, or edit, assign and delete an existing one.
 *
 * Lifecycle notes, because this screen is where all of them bite at once:
 *
 *  - The mode (creating vs editing) comes from the Intent, which the platform
 *    redelivers on recreation, so it needs no saving of its own.
 *  - The prefill-once flag and the base version live in the ViewModel's
 *    SavedStateHandle, not in a field here. A field reset to false on every
 *    rotation and re-ran the prefill over half-typed input; it only looked
 *    correct because Android happened to restore the EditText afterwards.
 *  - The delete confirmation is a DialogFragment. As a bare AlertDialog it
 *    disappeared on rotation and its listener held a finished Activity.
 *  - Field contents are restored by the platform from the view ids, so this
 *    class saves no text itself.
 */
public class StudentEditorActivity extends AppCompatActivity {

    private static final String REQ_DELETE = "editor.confirmDelete";

    private ActivityStudentEditorBinding binding;
    private EditorViewModel viewModel;
    private ProgrammeStore programmes;
    private String studentId;
    private boolean creating;
    private Dtos.GroupsResponse groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        programmes = new ProgrammeStore(this);
        studentId = getIntent().getStringExtra(Constants.EXTRA_STUDENT_ID);
        creating = studentId == null;
        setTitle(creating ? R.string.title_add_student : R.string.title_edit_student);

        viewModel = new ViewModelProvider(this, new ViewModelFactory(this))
                .get(EditorViewModel.class);

        fillProgrammeSpinner();

        if (!creating) {
            viewModel.bind(studentId);
            viewModel.student().observe(this, this::onStudent);
        } else {
            binding.assignButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.GONE);
            binding.groupSpinner.setVisibility(View.GONE);
        }

        viewModel.groups().observe(this, g -> { groups = g; fillGroupSpinner(); });
        viewModel.programmesReady().observe(this, ready -> fillProgrammeSpinner());
        viewModel.loadReferenceData();

        binding.saveButton.setOnClickListener(v -> save());
        binding.assignButton.setOnClickListener(v -> assign());
        binding.deleteButton.setOnClickListener(v -> ConfirmDialogFragment.show(
                getSupportFragmentManager(), REQ_DELETE,
                R.string.confirm_delete_title, R.string.confirm_delete_body,
                R.string.action_confirm_delete, R.string.action_cancel));

        // Registered in onCreate, so the answer is delivered whether or not the
        // Activity was recreated while the dialog was open.
        getSupportFragmentManager().setFragmentResultListener(REQ_DELETE, this,
                (key, result) -> {
                    if (result.getBoolean(ConfirmDialogFragment.RESULT_CONFIRMED)) viewModel.delete();
                });

        viewModel.state().observe(this, this::onOutcome);
    }

    /** Prefill exactly once, guarded by state that survives recreation. */
    private void onStudent(LocalStudentEntity student) {
        if (student == null) return;

        if (!viewModel.isPrefilled()) {
            viewModel.markPrefilled(student.version);
            binding.numberInput.setText(student.studentNumber);
            binding.nameInput.setText(student.displayName());
            binding.programmeSpinner.setSelection(programmes.positionOf(student.programme));
        }
        // These are safe to re-apply on every emission: they reflect the record,
        // not anything the user is typing.
        binding.numberInput.setEnabled(false);
        showClaimCode(student);
    }

    private void showClaimCode(LocalStudentEntity student) {
        if (student.claimCode != null) {
            binding.editorError.setVisibility(View.VISIBLE);
            binding.editorError.setText(getString(R.string.fmt_claim_code, student.claimCode));
        } else if (Constants.LOCAL_SAVED_LOCAL.equals(student.localStatus)
                || Constants.LOCAL_PENDING.equals(student.localStatus)) {
            binding.editorError.setVisibility(View.VISIBLE);
            binding.editorError.setText(R.string.msg_claim_code_pending);
        }
    }

    private void onOutcome(zm.mu.ict361lab.ui.common.UiState<EditorViewModel.Outcome> state) {
        if (state == null) return;
        binding.saveButton.setEnabled(!state.loading);
        if (state.loading) return;

        if (state.isError()) {
            binding.editorError.setText("DUPLICATE_NUMBER_LOCAL".equals(state.errorCode)
                    ? getString(R.string.err_duplicate_number_local)
                    : getString(ApiErrors.messageFor(state.errorCode)));
            binding.editorError.setVisibility(View.VISIBLE);
            viewModel.consume();
            return;
        }

        EditorViewModel.Outcome outcome = state.data;
        // Consumed before acting, so a rotation cannot replay the same outcome
        // and finish the screen twice.
        viewModel.consume();
        if (outcome == null) { finish(); return; }

        if (outcome.deleted) {
            Toast.makeText(this, outcome.droppedLocally
                    ? R.string.msg_deleted_never_synced
                    : R.string.msg_delete_queued, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (outcome.created) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.msg_student_saved_locally)
                    .setPositiveButton(android.R.string.ok, (d, w) -> finish())
                    .setCancelable(false)
                    .show();
            return;
        }
        Toast.makeText(this, R.string.msg_saved_locally, Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Programmes are stored reference data from the server, cached for offline
     * use. With no cache yet the field is disabled and says why, rather than
     * offering a programme the database may not have.
     */
    private void fillProgrammeSpinner() {
        List<String> codes = programmes.codes();
        if (codes.isEmpty()) {
            binding.programmeSpinner.setEnabled(false);
            binding.programmeSpinner.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item,
                    new String[]{ getString(R.string.label_programme_unknown) }));
            binding.editorError.setVisibility(View.VISIBLE);
            binding.editorError.setText(R.string.err_programmes_unavailable);
            return;
        }
        binding.programmeSpinner.setEnabled(true);
        int keep = binding.programmeSpinner.getSelectedItemPosition();
        binding.programmeSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, codes));
        if (keep >= 0 && keep < codes.size()) binding.programmeSpinner.setSelection(keep);
    }

    private void fillGroupSpinner() {
        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.label_unassigned));
        if (groups != null && groups.groups != null) {
            for (Dtos.GroupDto g : groups.groups) {
                labels.add(getString(R.string.fmt_share_line, g.label, g.members, g.capacity));
            }
        }
        int keep = binding.groupSpinner.getSelectedItemPosition();
        binding.groupSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, labels));
        if (keep >= 0 && keep < labels.size()) binding.groupSpinner.setSelection(keep);
    }

    private Integer selectedProgrammeId() {
        if (!binding.programmeSpinner.isEnabled()) return null;
        Object selected = binding.programmeSpinner.getSelectedItem();
        return selected == null ? null : programmes.idFor(selected.toString());
    }

    private void save() {
        String number = text(binding.numberInput.getText());
        String name = text(binding.nameInput.getText());
        binding.numberField.setError(null);
        binding.nameField.setError(null);

        boolean valid = true;
        if (creating && !Validators.studentNumber(number)) {
            binding.numberField.setError(getString(R.string.err_student_number)); valid = false;
        }
        if (!Validators.name(name)) {
            binding.nameField.setError(getString(R.string.err_name_length)); valid = false;
        }
        if (!valid) return;   // typed values are kept

        binding.editorError.setVisibility(View.GONE);
        if (creating) viewModel.create(number, name, selectedProgrammeId());
        else          viewModel.save(name, selectedProgrammeId());
    }

    private void assign() {
        int position = binding.groupSpinner.getSelectedItemPosition();
        if (position == 0) { viewModel.assign(null, null); return; }
        if (groups == null || groups.groups == null || position - 1 >= groups.groups.size()) return;
        Dtos.GroupDto chosen = groups.groups.get(position - 1);
        viewModel.assign(chosen.group_id, chosen.label);
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }

    private static String text(CharSequence value) {
        return value == null ? "" : value.toString();
    }
}
