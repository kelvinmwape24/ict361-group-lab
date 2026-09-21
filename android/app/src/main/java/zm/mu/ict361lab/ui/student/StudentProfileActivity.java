package zm.mu.ict361lab.ui.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import zm.mu.ict361lab.R;
import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.databinding.ActivityStudentProfileBinding;
import zm.mu.ict361lab.ui.auth.LoginActivity;
import zm.mu.ict361lab.ui.common.ChoiceDialogFragment;
import zm.mu.ict361lab.ui.common.InputDialogFragment;
import zm.mu.ict361lab.ui.common.ViewModelFactory;
import zm.mu.ict361lab.ui.sync.SyncStatusActivity;
import zm.mu.ict361lab.util.ApiErrors;
import zm.mu.ict361lab.util.Constants;
import zm.mu.ict361lab.util.TokenStore;
import zm.mu.ict361lab.util.Validators;

/**
 * My profile, my group, and the two requests a student can make.
 *
 * Lifecycle: both dialogs are DialogFragments, so rotating with one open
 * rebuilds it instead of losing it, and their answers arrive through
 * setFragmentResult rather than a listener that captured a dead Activity.
 * The listeners are registered in onCreate, which is what makes that work
 * across recreation.
 */
public class StudentProfileActivity extends AppCompatActivity {

    private static final String REQ_GROUP      = "profile.chooseGroup";
    private static final String REQ_CORRECTION = "profile.numberCorrection";

    private ActivityStudentProfileBinding binding;
    private StudentViewModel viewModel;
    private Dtos.GroupsResponse groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.title_my_profile);

        viewModel = new ViewModelProvider(this, new ViewModelFactory(this))
                .get(StudentViewModel.class);
        viewModel.bind(new TokenStore(this).studentId());

        binding.profileRefresh.setOnRefreshListener(viewModel::refresh);
        binding.editButton.setOnClickListener(v ->
                startActivity(new Intent(this, StudentEditActivity.class)));
        binding.groupButton.setOnClickListener(v -> chooseGroup());
        binding.correctionButton.setOnClickListener(v -> InputDialogFragment.show(
                getSupportFragmentManager(), REQ_CORRECTION,
                R.string.action_request_correction, R.string.help_student_number,
                R.string.label_student_number));

        getSupportFragmentManager().setFragmentResultListener(REQ_GROUP, this,
                (key, result) -> onGroupChosen(result.getInt(ChoiceDialogFragment.RESULT_INDEX, -1)));

        getSupportFragmentManager().setFragmentResultListener(REQ_CORRECTION, this,
                (key, result) -> {
                    String value = result.getString(InputDialogFragment.RESULT_VALUE, "");
                    if (!Validators.studentNumber(value)) {
                        showError(getString(R.string.err_student_number));
                        return;
                    }
                    viewModel.requestNumberCorrection(value);
                });

        viewModel.student().observe(this, this::render);
        viewModel.groups().observe(this, g -> groups = g);

        viewModel.refreshState().observe(this, state -> {
            if (state == null) return;
            binding.profileRefresh.setRefreshing(state.loading);
            if (state.isError()) {
                if (ApiErrors.isSessionGone(state.errorCode)) { returnToSignIn(); return; }
                showError(state.offline
                        ? getString(R.string.err_offline)
                        : getString(ApiErrors.messageFor(state.errorCode)));
            } else {
                binding.profileError.setVisibility(View.GONE);
            }
        });

        viewModel.saveState().observe(this, state -> {
            if (state == null || state.loading) return;
            if (state.isError()) {
                showError(state.offline
                        ? getString(R.string.err_offline)
                        : getString(ApiErrors.messageFor(state.errorCode)));
            }
            viewModel.consumeSave();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refreshed on every return to the foreground, so coming back from the
        // edit screen or from another device's change shows the current record.
        viewModel.refresh();
        viewModel.loadGroups();
        viewModel.loadProgrammes();
    }

    private void render(LocalStudentEntity student) {
        if (student == null) return;

        binding.profileName.setText(student.displayName());
        binding.profileNumber.setText(student.studentNumber);
        binding.profileProgramme.setText(student.programme == null ? "—" : student.programme);
        binding.profileGroup.setText(student.displayGroup() == null
                ? getString(R.string.label_unassigned) : student.displayGroup());

        // The sync state is always visible, in words, never as a silent icon.
        String status = student.localStatus;
        if (status == null || Constants.LOCAL_SYNCED.equals(status)) {
            binding.syncBanner.setVisibility(View.GONE);
        } else {
            binding.syncBanner.setVisibility(View.VISIBLE);
            binding.syncBanner.setText(labelFor(status));
        }

        binding.profileOccupancy.setText(occupancyFor(student));
    }

    private String occupancyFor(LocalStudentEntity student) {
        if (groups == null || groups.groups == null || student.groupId == null) return "";
        for (Dtos.GroupDto g : groups.groups) {
            if (g.group_id == student.groupId) {
                return getString(R.string.fmt_occupancy, g.members, g.capacity);
            }
        }
        return "";
    }

    private String labelFor(String status) {
        switch (status) {
            case Constants.LOCAL_SAVED_LOCAL:     return getString(R.string.status_saved_local);
            case Constants.LOCAL_PENDING:         return getString(R.string.status_pending);
            case Constants.LOCAL_SYNCING:         return getString(R.string.status_syncing);
            case Constants.LOCAL_ACTION_REQUIRED: return getString(R.string.status_action_required);
            default:                              return getString(R.string.status_synced);
        }
    }

    private void chooseGroup() {
        if (groups == null || groups.groups == null || groups.groups.isEmpty()) return;
        List<String> labels = new ArrayList<>();
        for (Dtos.GroupDto g : groups.groups) {
            labels.add(getString(R.string.fmt_share_line, g.label, g.members, g.capacity));
        }
        ChoiceDialogFragment.show(getSupportFragmentManager(), REQ_GROUP,
                R.string.action_request_group, labels.toArray(new String[0]));
    }

    private void onGroupChosen(int index) {
        if (index < 0 || groups == null || groups.groups == null
                || index >= groups.groups.size()) return;
        Dtos.GroupDto chosen = groups.groups.get(index);
        viewModel.requestGroup(chosen.group_id, chosen.label);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sync) {
            startActivity(new Intent(this, SyncStatusActivity.class));
            return true;
        }
        if (id == R.id.action_sign_out) {
            viewModel.signOut(this::returnToSignIn);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void returnToSignIn() {
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    private void showError(String message) {
        binding.profileError.setText(message);
        binding.profileError.setVisibility(View.VISIBLE);
    }
}
