package zm.mu.ict361lab.ui.lecturer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import zm.mu.ict361lab.R;
import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.databinding.ActivityRosterBinding;
import zm.mu.ict361lab.ui.auth.LoginActivity;
import zm.mu.ict361lab.ui.common.ViewModelFactory;
import zm.mu.ict361lab.ui.sync.SyncStatusActivity;
import zm.mu.ict361lab.util.ApiErrors;
import zm.mu.ict361lab.util.Constants;
import zm.mu.ict361lab.util.ProgrammeStore;

public class RosterActivity extends AppCompatActivity {

    private static final String ALL = "";
    private static final String UNASSIGNED = "UNASSIGNED";

    private ActivityRosterBinding binding;
    private RosterViewModel viewModel;
    private StudentAdapter adapter;
    private ProgrammeStore programmes;
    private Dtos.GroupsResponse groups;

    /** Set while the code is writing to a control, so its listener stays quiet. */
    private boolean bindingUi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRosterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.title_roster);

        programmes = new ProgrammeStore(this);
        viewModel = new ViewModelProvider(this, new ViewModelFactory(this))
                .get(RosterViewModel.class);

        adapter = new StudentAdapter(student ->
                startActivity(new Intent(this, StudentEditorActivity.class)
                        .putExtra(Constants.EXTRA_STUDENT_ID, student.studentId)));
        binding.rosterList.setLayoutManager(new LinearLayoutManager(this));
        binding.rosterList.setAdapter(adapter);
        binding.rosterList.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        setUpFilters();

        binding.rosterRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            viewModel.loadGroups();
        });
        binding.prevButton.setOnClickListener(v -> viewModel.previousPage());
        binding.nextButton.setOnClickListener(v -> viewModel.nextPage());
        binding.addButton.setOnClickListener(v ->
                startActivity(new Intent(this, StudentEditorActivity.class)));

        viewModel.students().observe(this, students -> {
            adapter.submitList(students);
            boolean empty = students == null || students.isEmpty();
            binding.emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        });

        viewModel.groups().observe(this, g -> {
            groups = g;
            fillGroupSpinner();
            showGroupTotals();
        });

        // Re-fill the programme filter once the reference list arrives.
        viewModel.programmesReady().observe(this, ready -> fillProgrammeSpinner());

        viewModel.networkState().observe(this, state -> {
            if (state == null) return;
            binding.rosterRefresh.setRefreshing(state.loading);

            // Offline is a labelled state, not a failure: the cached list stays
            // on screen and the banner says it may be incomplete.
            binding.offlineBanner.setVisibility(state.offline ? View.VISIBLE : View.GONE);

            if (state.isError() && !state.offline
                    && ApiErrors.isSessionGone(state.errorCode)) {
                returnToSignIn();
                return;
            }
            binding.pageLabel.setText(getString(R.string.fmt_page,
                    viewModel.page(), viewModel.pages(), viewModel.total()));
            binding.prevButton.setEnabled(viewModel.page() > 1);
            binding.nextButton.setEnabled(viewModel.page() < viewModel.pages());
        });

        restoreControls();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refresh();
        viewModel.loadGroups();
    }

    private void setUpFilters() {
        fillProgrammeSpinner();

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                if (bindingUi) return;
                viewModel.setQuery(s.toString());
            }
        });

        fillGroupSpinner();
    }

    /**
     * The programme filter is built from stored reference data, so it can only
     * offer programmes the database actually has.
     */
    private void fillProgrammeSpinner() {
        List<String> options = new ArrayList<>();
        options.add(getString(R.string.filter_all));
        options.addAll(programmes.codes());

        bindingUi = true;
        binding.programmeSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, options));
        int selected = options.indexOf(viewModel.programme());
        binding.programmeSpinner.setSelection(selected < 0 ? 0 : selected);
        bindingUi = false;

        binding.programmeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (bindingUi) return;
                viewModel.setProgramme(position == 0
                        ? ALL : String.valueOf(parent.getItemAtPosition(position)));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    /**
     * Group totals on the roster. The brief requires the lecturer to "see group
     * totals", and seeing them here means knowing a group is full before
     * opening a student and being told.
     */
    private void showGroupTotals() {
        if (groups == null || groups.groups == null) {
            binding.groupTotals.setVisibility(View.GONE);
            return;
        }
        StringBuilder text = new StringBuilder();
        for (Dtos.GroupDto g : groups.groups) {
            if (text.length() > 0) text.append("   ");
            text.append(getString(R.string.fmt_group_total, g.label, g.members, g.capacity));
        }
        if (text.length() > 0) text.append("   ");
        text.append(getString(R.string.fmt_unassigned_total, groups.unassigned));
        binding.groupTotals.setText(text.toString());
        binding.groupTotals.setVisibility(View.VISIBLE);
    }

    private void fillGroupSpinner() {
        List<String> labels = new ArrayList<>();
        labels.add(getString(R.string.filter_all));
        labels.add(getString(R.string.label_unassigned));
        if (groups != null && groups.groups != null) {
            for (Dtos.GroupDto g : groups.groups) labels.add(g.label);
        }

        bindingUi = true;
        binding.groupSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, labels));
        int selected = labels.indexOf(viewModel.group());
        if (UNASSIGNED.equals(viewModel.group())) selected = 1;
        else if (ALL.equals(viewModel.group())) selected = 0;
        if (selected >= 0) binding.groupSpinner.setSelection(selected);
        bindingUi = false;

        binding.groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (bindingUi) return;
                if (position == 0)      viewModel.setGroup(ALL);
                else if (position == 1) viewModel.setGroup(UNASSIGNED);
                else                    viewModel.setGroup((String) parent.getItemAtPosition(position));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    /**
     * Put the saved filters back on screen.
     *
     * The values come from the ViewModel's SavedStateHandle, so this restores
     * correctly after a rotation AND after the process was killed and the task
     * resumed — a plain ViewModel field would only have survived the first.
     *
     * bindingUi suppresses the spinner and text listeners while we write, so
     * restoring a value does not look like the user changing it and trigger
     * another network request.
     */
    private void restoreControls() {
        bindingUi = true;
        if (!viewModel.query().isEmpty()
                && binding.searchInput.getText() != null
                && binding.searchInput.getText().length() == 0) {
            binding.searchInput.setText(viewModel.query());
        }
        bindingUi = false;
    }

    /**
     * Activity D: share the group summary.
     *
     * Label and counts only. No name, no student number, nothing that
     * identifies a person leaves the app through the Sharesheet.
     */
    private void shareSummary() {
        if (groups == null || groups.groups == null) return;
        StringBuilder text = new StringBuilder(getString(R.string.share_header)).append('\n');
        for (Dtos.GroupDto g : groups.groups) {
            text.append(getString(R.string.fmt_share_line, g.label, g.members, g.capacity))
                    .append('\n');
        }
        new ShareCompat.IntentBuilder(this)
                .setType("text/plain")
                .setChooserTitle(R.string.share_chooser_title)
                .setSubject(getString(R.string.share_header))
                .setText(text.toString())
                .startChooser();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_roster, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share)   { shareSummary(); return true; }
        if (id == R.id.action_sync)    {
            startActivity(new Intent(this, SyncStatusActivity.class)); return true;
        }
        if (id == R.id.action_sign_out){ viewModel.signOut(this::returnToSignIn); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void returnToSignIn() {
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
