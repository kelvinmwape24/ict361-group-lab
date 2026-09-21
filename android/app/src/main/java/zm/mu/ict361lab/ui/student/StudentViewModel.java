package zm.mu.ict361lab.ui.student;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.data.repository.StudentRepository;
import zm.mu.ict361lab.ui.common.UiState;

public class StudentViewModel extends ViewModel {

    private static final String K_STUDENT_ID   = "student.id";
    private static final String K_BASE_VERSION = "student.baseVersion";
    private static final String K_PREFILLED    = "student.prefilled";

    private final StudentRepository repository;
    private final SavedStateHandle state;

    private final MutableLiveData<String> studentId = new MutableLiveData<>();
    private final MutableLiveData<UiState<Dtos.MeDto>> refreshState = new MutableLiveData<>();
    private final MutableLiveData<UiState<Void>> saveState = new MutableLiveData<>();
    private final MutableLiveData<Dtos.GroupsResponse> groups = new MutableLiveData<>();
    private final MutableLiveData<Boolean> programmesReady = new MutableLiveData<>(false);
    private final LiveData<LocalStudentEntity> cached;

    public StudentViewModel(StudentRepository repository, SavedStateHandle state) {
        this.repository = repository;
        this.state = state;
        this.cached = Transformations.switchMap(studentId, repository::cachedStudent);

        String restored = state.get(K_STUDENT_ID);
        if (restored != null) studentId.setValue(restored);
    }

    public void bind(String id) {
        if (id == null || id.equals(studentId.getValue())) return;
        state.set(K_STUDENT_ID, id);
        studentId.setValue(id);
    }

    public LiveData<LocalStudentEntity> student()        { return cached; }
    public LiveData<UiState<Dtos.MeDto>> refreshState()  { return refreshState; }
    public LiveData<UiState<Void>> saveState()           { return saveState; }
    public LiveData<Dtos.GroupsResponse> groups()        { return groups; }
    public LiveData<Boolean> programmesReady()           { return programmesReady; }

    public int baseVersion() {
        Integer v = state.get(K_BASE_VERSION);
        return v == null ? 1 : v;
    }

    /** See EditorViewModel.isPrefilled() — the same reasoning applies here. */
    public boolean isPrefilled() { return Boolean.TRUE.equals(state.get(K_PREFILLED)); }

    public void markPrefilled(int version) {
        state.set(K_PREFILLED, Boolean.TRUE);
        state.set(K_BASE_VERSION, version);
    }

    public void refresh() {
        refreshState.setValue(UiState.loading());
        repository.loadMe(result -> {
            if (result.ok) {
                if (result.data != null) bind(result.data.student_id);
                refreshState.setValue(UiState.success(result.data));
            } else if (result.offline) {
                refreshState.setValue(UiState.offline());
            } else {
                refreshState.setValue(UiState.error(result.errorCode));
            }
        });
    }

    public void loadGroups() {
        repository.loadGroups(result -> { if (result.ok) groups.setValue(result.data); });
    }

    /** Programmes are stored reference data; the repository caches them. */
    public void loadProgrammes() {
        repository.loadProgrammes(result -> {
            if (result.ok) programmesReady.setValue(Boolean.TRUE);
        });
    }

    /** Saves locally first; the queue delivers it whenever there is a connection. */
    public void save(String name, Integer programId) {
        saveState.setValue(UiState.loading());
        repository.saveEdit(studentId.getValue(), name, programId, baseVersion(),
                result -> saveState.setValue(result.ok
                        ? UiState.success(null) : UiState.error(result.errorCode)));
    }

    public void requestGroup(Integer groupId, String label) {
        saveState.setValue(UiState.loading());
        repository.requestGroup(studentId.getValue(), groupId, label, baseVersion(),
                result -> saveState.setValue(result.ok
                        ? UiState.success(null) : UiState.error(result.errorCode)));
    }

    public void requestNumberCorrection(String number) {
        saveState.setValue(UiState.loading());
        repository.requestNumberCorrection(number, result -> {
            if (result.ok)           saveState.setValue(UiState.success(null));
            else if (result.offline) saveState.setValue(UiState.offline());
            else                     saveState.setValue(UiState.error(result.errorCode));
        });
    }

    public void signOut(Runnable done) {
        repository.signOutAndWipe(result -> done.run());
    }

    public void consumeSave() { saveState.setValue(null); }
}
