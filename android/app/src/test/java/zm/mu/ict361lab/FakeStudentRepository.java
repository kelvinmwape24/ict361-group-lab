package zm.mu.ict361lab;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.local.entity.PendingOperationEntity;
import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.data.repository.Result;
import zm.mu.ict361lab.data.repository.StudentRepository;

/**
 * A repository that answers from memory.
 *
 * This is what makes the ViewModels testable without a device, an emulator, a
 * database or a running server — the competency evidence Activity B asks for.
 * Roster callbacks are held rather than invoked, so a test can decide when, and
 * in what order, each response arrives.
 */
public class FakeStudentRepository implements StudentRepository {

    /** One call the ViewModel made, recorded for assertions. */
    public static final class RosterCall {
        public final String query, programme, group;
        public final int page;
        public final Result.Callback<Dtos.PageDto> callback;
        RosterCall(String q, String p, String g, int page, Result.Callback<Dtos.PageDto> cb) {
            this.query = q; this.programme = p; this.group = g; this.page = page; this.callback = cb;
        }
    }

    public final List<RosterCall> rosterCalls = new ArrayList<>();
    public final List<String> savedEdits = new ArrayList<>();
    public final List<String> created = new ArrayList<>();
    public final List<String> deleted = new ArrayList<>();
    public int programmeLoads = 0;

    /** Flip to false to simulate a number already taken on this phone. */
    public boolean numberFree = true;
    /** Flip to true to simulate deleting a student whose CREATE never synced. */
    public boolean deleteCoalesces = false;

    private final MutableLiveData<List<LocalStudentEntity>> cached = new MutableLiveData<>();
    private final MutableLiveData<List<PendingOperationEntity>> queue = new MutableLiveData<>();
    private final MutableLiveData<Integer> outstanding = new MutableLiveData<>(0);

    public FakeStudentRepository() {
        cached.setValue(Collections.emptyList());
        queue.setValue(Collections.emptyList());
    }

    public void emitCached(List<LocalStudentEntity> rows) { cached.setValue(rows); }

    public void answerRoster(int index, int page, int pages, int total) {
        Dtos.PageDto dto = new Dtos.PageDto();
        dto.page = page; dto.pages = pages; dto.total = total;
        dto.items = new ArrayList<>();
        rosterCalls.get(index).callback.onResult(Result.success(dto));
    }

    public void answerRosterOffline(int index) {
        rosterCalls.get(index).callback.onResult(Result.offline());
    }

    /* ── reads ── */

    @Override
    public LiveData<List<LocalStudentEntity>> cachedRoster(String query, String programme, String group) {
        return cached;
    }

    @Override
    public LiveData<LocalStudentEntity> cachedStudent(String studentId) {
        return new MutableLiveData<>();
    }

    @Override public LiveData<List<PendingOperationEntity>> queue() { return queue; }

    @Override public LiveData<Integer> outstandingCount() { return outstanding; }

    @Override
    public void loadRoster(String query, String programme, String group, int page,
                           Result.Callback<Dtos.PageDto> callback) {
        rosterCalls.add(new RosterCall(query, programme, group, page, callback));
    }

    @Override public void loadMe(Result.Callback<Dtos.MeDto> callback) { }

    @Override
    public void loadGroups(Result.Callback<Dtos.GroupsResponse> callback) {
        Dtos.GroupsResponse response = new Dtos.GroupsResponse();
        response.groups = new ArrayList<>();
        callback.onResult(Result.success(response));
    }

    @Override
    public void loadProgrammes(Result.Callback<Dtos.ProgrammesResponse> callback) {
        programmeLoads++;
        Dtos.ProgrammesResponse response = new Dtos.ProgrammesResponse();
        response.programmes = new ArrayList<>();
        callback.onResult(Result.success(response));
    }

    /* ── writes ── */

    @Override
    public void createStudent(String studentNumber, String name, Integer programId,
                              Result.Callback<String> callback) {
        created.add(studentNumber + ":" + name);
        callback.onResult(Result.success("generated-id"));
    }

    @Override
    public void isNumberFreeLocally(String studentNumber, Result.Callback<Boolean> callback) {
        callback.onResult(Result.success(numberFree));
    }

    @Override
    public void saveEdit(String studentId, String name, Integer programId, int baseVersion,
                         Result.Callback<Void> callback) {
        savedEdits.add(studentId + ":" + name + ":v" + baseVersion);
        callback.onResult(Result.success(null));
    }

    @Override
    public void requestGroup(String studentId, Integer groupId, String groupLabel, int baseVersion,
                             Result.Callback<Void> callback) {
        callback.onResult(Result.success(null));
    }

    @Override
    public void deleteStudent(String studentId, Result.Callback<Boolean> callback) {
        deleted.add(studentId);
        callback.onResult(Result.success(deleteCoalesces));
    }

    @Override
    public void requestNumberCorrection(String requestedNumber, Result.Callback<Dtos.SimpleResponse> callback) {
        callback.onResult(Result.success(new Dtos.SimpleResponse()));
    }

    @Override
    public void retryWithServerVersion(String operationId, Result.Callback<Void> callback) {
        callback.onResult(Result.success(null));
    }

    @Override
    public void discardOperation(String operationId, Result.Callback<Void> callback) {
        callback.onResult(Result.success(null));
    }

    @Override
    public void signOutAndWipe(Result.Callback<Void> callback) {
        callback.onResult(Result.success(null));
    }
}
