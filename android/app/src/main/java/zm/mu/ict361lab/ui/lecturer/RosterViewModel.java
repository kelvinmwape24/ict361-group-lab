package zm.mu.ict361lab.ui.lecturer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.List;

import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.data.repository.StudentRepository;
import zm.mu.ict361lab.ui.common.UiState;

/**
 * The lecturer roster: combined search, programme filter, group filter and
 * pagination.
 *
 * Where the screen state lives, and why it matters:
 *
 *  - It is NOT in the Activity, so rotating the device keeps the search text,
 *    both filters and the page you were on.
 *  - It is NOT in a plain ViewModel field either. A ViewModel survives a
 *    configuration change but dies with the process, and Android kills
 *    backgrounded processes freely. Every value below is held in
 *    SavedStateHandle, which the platform writes into the Activity's saved
 *    instance state — so the state comes back after a low-memory kill too.
 *  - It is not in the repository, because it is not data. It is where the user
 *    had got to.
 *
 * Holding it here is also what lets this class be tested against a fake
 * repository in a plain JVM test, with no device at all.
 */
public class RosterViewModel extends ViewModel {

    /** Keys for SavedStateHandle. Stable strings: they end up in a Bundle. */
    private static final String K_QUERY     = "roster.query";
    private static final String K_PROGRAMME = "roster.programme";
    private static final String K_GROUP     = "roster.group";
    private static final String K_PAGE      = "roster.page";
    private static final String K_PAGES     = "roster.pages";
    private static final String K_TOTAL     = "roster.total";

    /** One request the list is currently showing. */
    public static final class Filters {
        public final String query, programme, group;
        public final int page;
        Filters(String query, String programme, String group, int page) {
            this.query = query; this.programme = programme; this.group = group; this.page = page;
        }
    }

    private final StudentRepository repository;
    private final SavedStateHandle state;

    private final MutableLiveData<Filters> filters = new MutableLiveData<>();
    private final MutableLiveData<UiState<Dtos.PageDto>> networkState = new MutableLiveData<>();
    private final MutableLiveData<Dtos.GroupsResponse> groups = new MutableLiveData<>();
    private final MutableLiveData<Boolean> programmesReady = new MutableLiveData<>(false);

    private final LiveData<List<LocalStudentEntity>> visible;

    /**
     * Guards against a stale response landing after a newer one. Deliberately
     * NOT saved: an in-flight request does not survive the process, so a
     * restored screen starts its own request from zero.
     */
    private int requestToken = 0;

    public RosterViewModel(StudentRepository repository, SavedStateHandle state) {
        this.repository = repository;
        this.state = state;

        // The list always comes from Room, so it renders instantly and keeps
        // working offline. The network only ever refreshes what Room holds.
        this.visible = Transformations.switchMap(filters,
                f -> repository.cachedRoster(f.query, f.programme, f.group));

        // Restored values if there are any, defaults on a first launch. Either
        // way the screen comes up showing what the user was looking at.
        filters.setValue(new Filters(query(), programme(), group(), page()));
        refresh();
        loadGroups();
        loadProgrammes();
    }

    public LiveData<List<LocalStudentEntity>> students()   { return visible; }
    public LiveData<UiState<Dtos.PageDto>> networkState()  { return networkState; }
    public LiveData<Dtos.GroupsResponse> groups()          { return groups; }
    public LiveData<Boolean> programmesReady()             { return programmesReady; }

    /* ── state, read through SavedStateHandle ── */

    public String query()     { return orEmpty(state.get(K_QUERY)); }
    public String programme() { return orEmpty(state.get(K_PROGRAMME)); }
    public String group()     { return orEmpty(state.get(K_GROUP)); }
    public int page()         { return orOne(state.get(K_PAGE)); }
    public int pages()        { return orOne(state.get(K_PAGES)); }
    public int total()        { Integer v = state.get(K_TOTAL); return v == null ? 0 : v; }

    private static String orEmpty(String v) { return v == null ? "" : v; }
    private static int orOne(Integer v)     { return v == null || v < 1 ? 1 : v; }

    public void setQuery(String value) {
        String next = value == null ? "" : value.trim();
        if (next.equals(query())) return;          // an unchanged filter must not refetch
        state.set(K_QUERY, next);
        state.set(K_PAGE, 1);                      // a new search starts at the beginning
        apply();
    }

    public void setProgramme(String value) {
        String next = value == null ? "" : value;
        if (next.equals(programme())) return;
        state.set(K_PROGRAMME, next);
        state.set(K_PAGE, 1);
        apply();
    }

    public void setGroup(String value) {
        String next = value == null ? "" : value;
        if (next.equals(group())) return;
        state.set(K_GROUP, next);
        state.set(K_PAGE, 1);
        apply();
    }

    public void nextPage() {
        if (page() >= pages()) return;
        state.set(K_PAGE, page() + 1);
        apply();
    }

    public void previousPage() {
        if (page() <= 1) return;
        state.set(K_PAGE, page() - 1);
        apply();
    }

    private void apply() {
        filters.setValue(new Filters(query(), programme(), group(), page()));
        refresh();
    }

    public void refresh() {
        final int token = ++requestToken;
        networkState.setValue(UiState.loading());
        repository.loadRoster(query(), programme(), group(), page(), result -> {
            if (token != requestToken) return;      // a newer search already ran
            if (result.ok) {
                if (result.data != null) {
                    state.set(K_PAGES, Math.max(1, result.data.pages));
                    state.set(K_TOTAL, result.data.total);
                }
                networkState.setValue(UiState.success(result.data));
            } else if (result.offline) {
                networkState.setValue(UiState.offline());
            } else {
                networkState.setValue(UiState.error(result.errorCode));
            }
        });
    }

    public void loadGroups() {
        repository.loadGroups(result -> { if (result.ok) groups.setValue(result.data); });
    }

    /**
     * Programmes are stored reference data on the server. The repository caches
     * them, so this only needs to run when there is a connection; the picker
     * keeps working from the cache afterwards.
     */
    public void loadProgrammes() {
        repository.loadProgrammes(result -> {
            if (result.ok) programmesReady.setValue(Boolean.TRUE);
        });
    }

    public void signOut(Runnable done) {
        repository.signOutAndWipe(result -> done.run());
    }
}
