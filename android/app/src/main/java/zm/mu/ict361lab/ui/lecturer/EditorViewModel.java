package zm.mu.ict361lab.ui.lecturer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.data.repository.StudentRepository;
import zm.mu.ict361lab.ui.common.UiState;

public class EditorViewModel extends ViewModel {

    private static final String K_STUDENT_ID   = "editor.studentId";
    private static final String K_BASE_VERSION = "editor.baseVersion";
    private static final String K_PREFILLED    = "editor.prefilled";

    /** What the editor finished doing, so the screen knows what to say. */
    public static final class Outcome {
        public final String studentId;
        public final boolean created, deleted, droppedLocally;
        Outcome(String studentId, boolean created, boolean deleted, boolean droppedLocally) {
            this.studentId = studentId; this.created = created;
            this.deleted = deleted; this.droppedLocally = droppedLocally;
        }
    }

    private final StudentRepository repository;
    private final SavedStateHandle state;

    private final MutableLiveData<String> studentId = new MutableLiveData<>();
    private final MutableLiveData<UiState<Outcome>> outcome = new MutableLiveData<>();
    private final MutableLiveData<Dtos.GroupsResponse> groups = new MutableLiveData<>();
    private final MutableLiveData<Boolean> programmesReady = new MutableLiveData<>(false);
    private final LiveData<LocalStudentEntity> student;

    public EditorViewModel(StudentRepository repository, SavedStateHandle state) {
        this.repository = repository;
        this.state = state;
        this.student = Transformations.switchMap(studentId, repository::cachedStudent);

        String restored = state.get(K_STUDENT_ID);
        if (restored != null) studentId.setValue(restored);
    }

    public void bind(String id) {
        if (id == null || id.equals(studentId.getValue())) return;
        state.set(K_STUDENT_ID, id);
        studentId.setValue(id);
    }

    public LiveData<LocalStudentEntity> student()      { return student; }
    public LiveData<UiState<Outcome>> state()          { return outcome; }
    public LiveData<Dtos.GroupsResponse> groups()      { return groups; }
    public LiveData<Boolean> programmesReady()         { return programmesReady; }

    public int baseVersion() {
        Integer v = state.get(K_BASE_VERSION);
        return v == null ? 1 : v;
    }

    /**
     * Whether the form has already been filled from the record.
     *
     * This lived in an Activity field before, which reset to false every time
     * the Activity was recreated — so a rotation re-ran the prefill and was
     * only saved from wiping half-typed input by the accident of Android
     * restoring view state afterwards. Keeping the flag here makes it correct
     * rather than lucky, and it survives process death with the rest.
     */
    public boolean isPrefilled() {
        return Boolean.TRUE.equals(state.get(K_PREFILLED));
    }

    public void markPrefilled(int version) {
        state.set(K_PREFILLED, Boolean.TRUE);
        state.set(K_BASE_VERSION, version);
    }

    public void loadReferenceData() {
        repository.loadGroups(result -> { if (result.ok) groups.setValue(result.data); });
        repository.loadProgrammes(result -> programmesReady.setValue(Boolean.TRUE));
    }

    /**
     * Create, with the duplicate check done locally first so the user hears
     * about it while the form is still in front of them. The server's unique
     * index remains the authority.
     */
    public void create(String number, String name, Integer programId) {
        outcome.setValue(UiState.loading());
        repository.isNumberFreeLocally(number, check -> {
            if (check.ok && Boolean.FALSE.equals(check.data)) {
                outcome.setValue(UiState.error("DUPLICATE_NUMBER_LOCAL"));
                return;
            }
            repository.createStudent(number, name, programId, result -> {
                if (result.ok) outcome.setValue(UiState.success(
                        new Outcome(result.data, true, false, false)));
                else           outcome.setValue(UiState.error(result.errorCode));
            });
        });
    }

    public void save(String name, Integer programId) {
        outcome.setValue(UiState.loading());
        repository.saveEdit(studentId.getValue(), name, programId, baseVersion(),
                result -> outcome.setValue(result.ok
                        ? UiState.success(new Outcome(studentId.getValue(), false, false, false))
                        : UiState.error(result.errorCode)));
    }

    public void assign(Integer groupId, String label) {
        outcome.setValue(UiState.loading());
        repository.requestGroup(studentId.getValue(), groupId, label, baseVersion(),
                result -> outcome.setValue(result.ok
                        ? UiState.success(new Outcome(studentId.getValue(), false, false, false))
                        : UiState.error(result.errorCode)));
    }

    public void delete() {
        outcome.setValue(UiState.loading());
        repository.deleteStudent(studentId.getValue(), result -> {
            if (result.ok) {
                outcome.setValue(UiState.success(new Outcome(
                        studentId.getValue(), false, true, Boolean.TRUE.equals(result.data))));
            } else {
                outcome.setValue(UiState.error(result.errorCode));
            }
        });
    }

    /**
     * Clears a handled one-shot result.
     *
     * Without this, a rotation re-delivers the last value to the new observer
     * and the screen finishes itself again, or shows the same toast twice.
     */
    public void consume() { outcome.setValue(null); }
}
