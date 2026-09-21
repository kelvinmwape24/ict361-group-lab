package zm.mu.ict361lab.ui.sync;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import zm.mu.ict361lab.data.local.entity.PendingOperationEntity;
import zm.mu.ict361lab.data.repository.StudentRepository;

public class SyncViewModel extends ViewModel {

    private final StudentRepository repository;

    public SyncViewModel(StudentRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<PendingOperationEntity>> queue() { return repository.queue(); }

    public LiveData<Integer> outstanding() { return repository.outstandingCount(); }

    /** Re-send the user's proposal on top of what the server now holds. */
    public void keepMine(String operationId) {
        repository.retryWithServerVersion(operationId, result -> { });
    }

    /** Drop the proposal; the server's version stands. */
    public void discard(String operationId) {
        repository.discardOperation(operationId, result -> { });
    }
}
