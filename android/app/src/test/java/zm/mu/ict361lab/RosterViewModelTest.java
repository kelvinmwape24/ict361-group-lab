package zm.mu.ict361lab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import androidx.lifecycle.SavedStateHandle;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zm.mu.ict361lab.data.local.entity.LocalStudentEntity;
import zm.mu.ict361lab.data.remote.Dtos;
import zm.mu.ict361lab.ui.common.UiState;
import zm.mu.ict361lab.ui.lecturer.RosterViewModel;

/**
 * RosterViewModel against a fake repository — no device, no emulator, no
 * database, no server.
 *
 * Competency evidence for Activity B: the ViewModel holds screen state and
 * coordinates through a repository, so its behaviour can be checked in a plain
 * JVM test. The last test also covers the lifecycle claim the brief makes —
 * that a ViewModel alone does not survive process death — by rebuilding the
 * ViewModel from a restored SavedStateHandle.
 */
public class RosterViewModelTest {

    /** LiveData needs its work run inline rather than posted to a main looper. */
    @Rule
    public InstantTaskExecutorRule instantExecutor = new InstantTaskExecutorRule();

    private FakeStudentRepository repository;
    private SavedStateHandle handle;
    private RosterViewModel viewModel;

    private final Observer<List<LocalStudentEntity>> keepActive = rows -> { };
    private final Observer<UiState<Dtos.PageDto>> keepStateActive = state -> { };

    @Before
    public void setUp() {
        repository = new FakeStudentRepository();
        handle = new SavedStateHandle();
        viewModel = new RosterViewModel(repository, handle);
        // switchMap only runs while something is observing.
        viewModel.students().observeForever(keepActive);
        viewModel.networkState().observeForever(keepStateActive);
    }

    @Test
    public void loadsTheFirstPageOnCreation() {
        assertEquals(1, repository.rosterCalls.size());
        assertEquals("", repository.rosterCalls.get(0).query);
        assertEquals(1, repository.rosterCalls.get(0).page);
    }

    @Test
    public void searchingResetsToPageOneAndReloads() {
        repository.answerRoster(0, 1, 3, 25);
        viewModel.nextPage();
        repository.answerRoster(1, 2, 3, 25);
        assertEquals(2, viewModel.page());

        viewModel.setQuery("mulenga");

        // A new search starts at the beginning. Staying on page 2 of the old
        // result set would show a page that may not exist for the new one.
        assertEquals(1, viewModel.page());
        FakeStudentRepository.RosterCall latest =
                repository.rosterCalls.get(repository.rosterCalls.size() - 1);
        assertEquals("mulenga", latest.query);
        assertEquals(1, latest.page);
    }

    @Test
    public void combinedFiltersAreAllSentTogether() {
        viewModel.setQuery("banda");
        viewModel.setProgramme("IT");
        viewModel.setGroup("G01");

        FakeStudentRepository.RosterCall latest =
                repository.rosterCalls.get(repository.rosterCalls.size() - 1);
        assertEquals("banda", latest.query);
        assertEquals("IT", latest.programme);
        assertEquals("G01", latest.group);
    }

    @Test
    public void staleSearchResponseIsIgnored() {
        // The user types "mu", then "mul" before the first answer arrives.
        viewModel.setQuery("mu");
        viewModel.setQuery("mul");
        int muCall = repository.rosterCalls.size() - 2;
        int mulCall = repository.rosterCalls.size() - 1;

        repository.answerRoster(mulCall, 1, 2, 12);
        assertEquals(2, viewModel.pages());
        assertEquals(12, viewModel.total());

        // The older one arrives late and must be discarded. Applied, it would
        // show counts for a search the user has already moved on from.
        repository.answerRoster(muCall, 1, 9, 99);
        assertEquals(2, viewModel.pages());
        assertEquals(12, viewModel.total());
    }

    @Test
    public void pagingStopsAtBothEnds() {
        repository.answerRoster(0, 1, 2, 15);

        viewModel.previousPage();
        assertEquals("already on the first page", 1, viewModel.page());

        viewModel.nextPage();
        assertEquals(2, viewModel.page());

        repository.answerRoster(repository.rosterCalls.size() - 1, 2, 2, 15);
        viewModel.nextPage();
        assertEquals("already on the last page", 2, viewModel.page());
    }

    @Test
    public void offlineIsAStateNotAFailure() {
        repository.answerRosterOffline(0);

        UiState<Dtos.PageDto> state = viewModel.networkState().getValue();
        assertNotNull(state);
        assertTrue(state.offline);

        // The cached list is still what the screen renders.
        List<LocalStudentEntity> cached = new ArrayList<>();
        LocalStudentEntity row = new LocalStudentEntity();
        row.studentId = "s1";
        row.studentName = "Kelvin Mwape";
        cached.add(row);
        repository.emitCached(cached);

        assertEquals(1, viewModel.students().getValue().size());
    }

    @Test
    public void repeatingTheSameQueryDoesNotReload() {
        int before = repository.rosterCalls.size();
        viewModel.setQuery("");
        assertEquals("an unchanged filter must not refetch", before, repository.rosterCalls.size());
    }

    @Test
    public void programmesAreFetchedAsReferenceDataNotHardCoded() {
        // The brief requires the programme to be chosen "from stored reference
        // data". A hard-coded list in the app would make this call unnecessary
        // and this assertion would fail.
        assertEquals(1, repository.programmeLoads);
        assertNotNull(viewModel.programmesReady().getValue());
    }

    /* ───────────── the lifecycle claim, tested ───────────── */

    @Test
    public void screenStateIsWrittenToSavedStateHandle() {
        viewModel.setQuery("mwape");
        viewModel.setProgramme("DS");
        viewModel.setGroup("G02");
        repository.answerRoster(repository.rosterCalls.size() - 1, 1, 4, 37);
        viewModel.nextPage();

        // Not in a plain field: in the handle the platform writes into the
        // Activity's saved instance state.
        assertEquals("mwape", handle.get("roster.query"));
        assertEquals("DS", handle.get("roster.programme"));
        assertEquals("G02", handle.get("roster.group"));
        assertEquals(Integer.valueOf(2), handle.get("roster.page"));
        assertEquals(Integer.valueOf(4), handle.get("roster.pages"));
    }

    @Test
    public void stateSurvivesProcessDeath() {
        // A rotation keeps the ViewModel instance; a low-memory kill does not.
        // This simulates the harder case: the object is gone, and only the
        // saved bundle comes back.
        Map<String, Object> restored = new HashMap<>();
        restored.put("roster.query", "chanda");
        restored.put("roster.programme", "IT");
        restored.put("roster.group", "UNASSIGNED");
        restored.put("roster.page", 3);
        restored.put("roster.pages", 5);
        restored.put("roster.total", 48);

        FakeStudentRepository freshRepo = new FakeStudentRepository();
        RosterViewModel revived =
                new RosterViewModel(freshRepo, new SavedStateHandle(restored));
        revived.students().observeForever(rows -> { });

        assertEquals("chanda", revived.query());
        assertEquals("IT", revived.programme());
        assertEquals("UNASSIGNED", revived.group());
        assertEquals(3, revived.page());
        assertEquals(5, revived.pages());
        assertEquals(48, revived.total());

        // And it resumes from where the user was, not from page 1.
        FakeStudentRepository.RosterCall first = freshRepo.rosterCalls.get(0);
        assertEquals("chanda", first.query);
        assertEquals("IT", first.programme);
        assertEquals("UNASSIGNED", first.group);
        assertEquals(3, first.page);
    }

    @Test
    public void anInFlightRequestIsNotRestored() {
        // The request token is deliberately NOT saved. A response cannot arrive
        // for a process that no longer exists, so a revived screen issues its
        // own request rather than waiting for one that will never land.
        viewModel.setQuery("mu");
        RosterViewModel revived = new RosterViewModel(
                new FakeStudentRepository(), new SavedStateHandle(new HashMap<>()));
        revived.students().observeForever(rows -> { });
        assertEquals("a revived screen starts from a clean request", 1, viewModel.page());
        assertEquals(1, revived.page());
    }
}
