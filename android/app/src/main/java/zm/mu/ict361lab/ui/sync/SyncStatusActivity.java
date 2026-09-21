package zm.mu.ict361lab.ui.sync;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import zm.mu.ict361lab.R;
import zm.mu.ict361lab.data.sync.SyncScheduler;
import zm.mu.ict361lab.databinding.ActivitySyncStatusBinding;
import zm.mu.ict361lab.ui.common.ViewModelFactory;

/**
 * What is waiting, what failed, and why — plus a manual Sync, because waiting
 * for a background worker with no way to nudge it is its own kind of broken.
 */
public class SyncStatusActivity extends AppCompatActivity {

    private ActivitySyncStatusBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySyncStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(R.string.title_sync);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SyncViewModel viewModel = new ViewModelProvider(this, new ViewModelFactory(this))
                .get(SyncViewModel.class);

        OperationAdapter adapter = new OperationAdapter(new OperationAdapter.Actions() {
            @Override public void keepMine(String operationId) { viewModel.keepMine(operationId); }
            @Override public void discard(String operationId)  { viewModel.discard(operationId); }
        });

        binding.queueList.setLayoutManager(new LinearLayoutManager(this));
        binding.queueList.setAdapter(adapter);
        binding.queueList.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        binding.syncNowButton.setOnClickListener(v -> SyncScheduler.requestSync(this));

        viewModel.queue().observe(this, operations -> {
            adapter.submitList(operations);
            boolean empty = operations == null || operations.isEmpty();
            binding.queueEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
