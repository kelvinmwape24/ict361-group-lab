# Lifecycle and data flow

Chibesa Mumbi · <your student number> · Android Architecture team
Contribution log row 9 — Activities, ViewModel wiring, ViewBinding, lifecycle, rotation tests

Two audiences. The first half is the data flow you have to be able to explain
under questioning. The second half is every lifecycle decision in the code and
the reason for it, because "why did you do it that way?" is the question that
actually gets asked.

---

## 1. The complete data flow, in one pass

Take one concrete action: **a lecturer corrects a student's name while offline.**

```
  1  StudentEditorActivity          the tap
  2  Validators.name(value)         local check, on the spot
  3  EditorViewModel.save(...)      screen hands it over and forgets about it
  4  StudentRepository.saveEdit()   interface — the ViewModel sees nothing else
  5  RoomStudentRepository          the real implementation
  6  AppExecutors.diskIO()          off the main thread, single-threaded
  7  db.runInTransaction {          ONE transaction
       local_student.pendingName      = the proposal
       local_student.localStatus      = SAVED_LOCAL
       INSERT pending_operation       = how it will be delivered
     }
  8  LiveData emits                 → the screen shows "Saved locally"
  9  SyncScheduler.requestSync()    WorkManager, constrained to a connection
 10  ...later...
 11  SyncWorker.doWork()            drains the queue oldest first
 12  ApiService.push(...)           POST /sync, same operation_id as always
 13  Express → auth middleware      identity, role, ownership, on every request
 14  MySQL transaction              SELECT ... FOR UPDATE, compare base_version
 15  operation_receipts INSERT      same transaction as the mutation
 16  200 {new_version}              back down the chain
 17  db.runInTransaction {          proposal becomes the confirmed value
       studentName = pendingName
       result_json = the receipt
       localStatus = SYNCED
     }
 18  LiveData emits                 → the screen shows "Synced"
```

Five things to be ready to defend about that sequence:

**Why does the screen never touch Room or Retrofit?** Because then it could not
be tested. `RosterViewModelTest` runs eight tests on a JVM with no device,
because every ViewModel depends on the `StudentRepository` *interface* and
`FakeStudentRepository` can stand in for it. That seam is `ViewModelFactory`.

**Why one transaction at step 7?** Because two writes can be interrupted between
them. Write the edit and lose the operation, and the change never leaves the
phone. Write the operation and lose the edit, and it delivers nothing. Either or
neither.

**Why `pendingName` and not `studentName`?** Because the server may refuse.
Overwrite the confirmed value on save and there is nothing left to compare
against — a conflict could then only be resolved by guessing. Keeping them in
separate columns is what makes "Action required" show the user both versions.

**Why the same `operation_id` on every retry?** Because a lost response is
indistinguishable from a failed request. The server keeps a receipt keyed on
that id, so a repeat costs a duplicate *request* and never a duplicate *effect*.
That is Challenge 2.

**Why does step 14 lock the group row and not the student row?** For an ASSIGN,
the thing being contended is the group's last place, not the student. Locking
the student row would let two different students into the same last place. That
is Challenge 1.

---

## 2. Where state lives, and why there

Four places, and the choice between them is the whole of the lifecycle work.

| State | Lives in | Survives rotation | Survives process death | Why there |
|---|---|---|---|---|
| Text in a field, scroll position, spinner selection | The view, via its `id` | yes | yes | The platform does this for free. Writing it by hand is code that can only be wrong. |
| Search text, both filters, current page, base version, prefill flag | ViewModel `SavedStateHandle` | yes | yes | Screen state, not data. Needed after a low-memory kill, which is why it is not a plain field. |
| In-flight request token, one-shot outcomes | Plain ViewModel field | yes | **no, deliberately** | A response cannot arrive for a process that no longer exists. |
| Students, the operation queue, the registration draft | Room and SharedPreferences | yes | yes | Real data. It has to outlive the app entirely. |

### The line the brief draws

Unit 1 and Activity B both say it: *ViewModel alone does not survive process
death.* That sentence is why this pack exists. Before it, `RosterViewModel`
held the query and filters in ordinary fields:

```java
private String query = "";
private int page = 1;
```

That passes a rotation, because the ViewModel instance is retained across a
configuration change. It fails the harder case. Put the app in the background,
let Android reclaim the process, come back to the task: the ViewModel is gone,
the fields are gone, and the lecturer is looking at page 1 of an unfiltered
roster with no idea why.

Now:

```java
public String query() { return orEmpty(state.get(K_QUERY)); }

public void setQuery(String value) {
    state.set(K_QUERY, next);
    state.set(K_PAGE, 1);
    apply();
}
```

`SavedStateHandle` is written into the Activity's saved instance state by the
platform, so it comes back either way. `stateSurvivesProcessDeath()` in
`RosterViewModelTest` proves it by building a ViewModel from a restored handle
and checking it resumes on page 3 of the right search.

**What is deliberately not saved:** `requestToken`. Saving it would be worse
than useless — it guards against a stale *network response*, and no response
can arrive for a process that has been killed. A revived screen issues its own
request. There is a test for that too, because "we thought about it and chose
not to" is a better answer than silence.

---

## 3. The prefill bug, which is the one worth explaining

The edit screens fill the form from the record once, then leave it alone. The
guard was:

```java
private boolean prefilled = false;   // an Activity field
```

An Activity field resets on every recreation. So on rotation: `prefilled` is
false again, the LiveData observer re-delivers the cached record, `setText()`
runs, and whatever the user had typed is replaced by the server's value.

It did not actually misbehave — and that is the interesting part. Android
restores view state *after* `onStart()`, and LiveData observers fire *during*
`onStart()`. So the sequence was:

```
onCreate           → observe()
onStart            → observer fires → setText("Chibesa Mumbi")   ← the record
onRestoreInstance  → setText("Chibesa M. Mum")                   ← what was typed
```

The restore won. The code was wrong and the outcome was right, which is the
worst combination: it works until something changes the ordering — a fragment,
a delayed emission, a synchronous load — and then it silently eats user input.

The fix moves the flag into `SavedStateHandle`:

```java
public boolean isPrefilled() { return Boolean.TRUE.equals(state.get(K_PREFILLED)); }
```

It now survives both rotation and process death, so the prefill runs exactly
once per screen visit regardless of ordering. `StudentEditRotationTest` covers
it, including the nastier variant: a field the user deliberately **cleared**
must stay cleared, not be refilled because empty looked like "not yet
prefilled".

---

## 4. Dialogs

Every dialog was a bare `AlertDialog.Builder(this).show()`. Two problems:

1. **It vanishes on rotation.** The Activity is destroyed, the dialog with it,
   and nothing brings it back. The user has to start again.
2. **Its listener holds a dead Activity.** `(d, w) -> viewModel.delete()`
   captures `this`. After recreation that instance is finished; anything the
   callback touches is stale.

All three are now `DialogFragment`s — `ConfirmDialogFragment`,
`ChoiceDialogFragment`, `InputDialogFragment` — and the answer comes back
through `setFragmentResult` instead of a captured lambda:

```java
getSupportFragmentManager().setFragmentResultListener(REQ_DELETE, this,
        (key, result) -> {
            if (result.getBoolean(ConfirmDialogFragment.RESULT_CONFIRMED)) viewModel.delete();
        });
```

Registered in `onCreate` with the Activity as the lifecycle owner, so the
listener is re-attached on recreation and the result is delivered to the *live*
Activity. The FragmentManager rebuilds the dialog itself.

`InputDialogFragment` gives its `EditText` an id, so a half-typed student number
survives a rotation like any other field.

---

## 5. One-shot results

A subtle one. `UiState` is delivered through LiveData, and LiveData re-delivers
its last value to any new observer. So after a rotation:

```
new Activity → observe(state) → the OLD success value arrives → finish() again
```

Depending on what the handler does, that is a double toast, a screen that closes
itself before you can read it, or a duplicated navigation.

Every one-shot channel is therefore consumed before it is acted on:

```java
viewModel.consume();          // clear it first
if (outcome.deleted) { ... }  // then act
```

The alternative is a wrapper type that tracks whether an event has been
handled. For five screens, an explicit `consume()` is less machinery and easier
to explain — which matters when someone is asking.

---

## 6. ViewBinding

Every screen inflates a generated binding:

```java
binding = ActivityRosterBinding.inflate(getLayoutInflater());
setContentView(binding.getRoot());
```

Not for tidiness. `findViewById` returns a view for any id in the whole project
and fails at runtime if it is not in the layout you inflated; ViewBinding only
exposes ids from *this* layout and fails at compile time. On a project where
five people are editing layouts, the difference is a build error instead of a
crash in the demo.

The generated class name comes from the layout file name, so `activity_roster.xml`
gives `ActivityRosterBinding` and `search_input` gives `binding.searchInput`.

One thing to be aware of: the binding is held for the life of the Activity here,
which is fine. In a Fragment it would have to be nulled in `onDestroyView`,
because a Fragment outlives its view.

---

## 7. Threading

`AppExecutors` has three executors and the choice between them matters.

```java
private final ExecutorService diskIO    = Executors.newSingleThreadExecutor();
private final ExecutorService networkIO = Executors.newFixedThreadPool(3);
private final Executor mainThread       = new MainThreadExecutor();
```

**`diskIO` is a single thread on purpose.** Room writes then stay ordered, so a
save-and-queue pair cannot interleave with another save. Three threads would be
faster and would occasionally corrupt the queue order, which is the one thing
Activity E asks us to preserve.

**`networkIO` has three.** Independent reads — roster, groups, programmes — can
overlap harmlessly.

**Results are delivered on `mainThread`,** because `MutableLiveData.setValue()`
must be called there. Every repository callback goes through
`deliver()`, which posts to the main thread, so no ViewModel has to think about
which thread it is on.

The `SyncWorker` is different again: WorkManager already runs `doWork()` on a
background thread, so it calls Room and Retrofit synchronously. Wrapping that in
another executor would only make it harder to reason about.

---

## 8. Lifecycle-aware observation

Every `observe(this, ...)` passes the Activity as the `LifecycleOwner`, which
means:

- Callbacks only fire between `onStart` and `onStop`. The UI is never touched
  while the screen is not visible.
- Observers are removed automatically on `onDestroy`. No manual unsubscribe, and
  no leaked Activity.

`observeForever` appears **only in unit tests**, where there is no lifecycle and
the test removes it itself. In production code it would be a leak.

`onResume` is where each screen refreshes:

```java
@Override protected void onResume() {
    super.onResume();
    viewModel.refresh();
    viewModel.loadGroups();
}
```

Not `onCreate`: coming back from the edit screen does not re-create the roster,
so a refresh in `onCreate` would show stale rows. Not `onStart` either — the
difference rarely matters here, and `onResume` is the conventional place for
"the user is now looking at this".

---

## 9. The six screens

| Screen | ViewModel | State it owns | Lifecycle note |
|---|---|---|---|
| `LoginActivity` | `AuthViewModel` | — | Fields restore themselves; the result is consumed so rotation does not re-navigate |
| `RegisterActivity` | `AuthViewModel` | Draft in SharedPreferences | Saved in `onPause` and `onSaveInstanceState`, not only on Back — a backgrounded process can be killed at any moment. The password is never in the draft. |
| `StudentProfileActivity` | `StudentViewModel` | student id | Two DialogFragments; refreshes in `onResume` |
| `StudentEditActivity` | `StudentViewModel` | prefill flag, base version | Prefill-once guarded by saved state |
| `RosterActivity` | `RosterViewModel` | query, filters, page | All of it in `SavedStateHandle`; `bindingUi` suppresses listeners during restore |
| `StudentEditorActivity` | `EditorViewModel` | student id, prefill flag, base version | Mode comes from the Intent, which the platform redelivers; delete confirmation is a DialogFragment |
| `SyncStatusActivity` | `SyncViewModel` | — | Pure observation of a Room query; nothing to save |

`bindingUi` deserves a line of its own. Restoring a spinner selection fires
`onItemSelected`, which looks exactly like the user changing the filter and
would trigger another network request. The flag suppresses the listener while
the code writes to the control, and is cleared afterwards.

---

## 10. Running the evidence

```cmd
cd android

:: JVM — no device. 8 roster tests + 7 validator tests.
gradlew test

:: On a device or emulator — the rotation and migration tests.
gradlew connectedDebugAndroidTest
```

| Test | Proves |
|---|---|
| `RosterViewModelTest.stateSurvivesProcessDeath` | Filters and page come back from a restored `SavedStateHandle` |
| `RosterViewModelTest.screenStateIsWrittenToSavedStateHandle` | The state really is in the handle, not a field |
| `RosterViewModelTest.anInFlightRequestIsNotRestored` | The deliberate omission is deliberate |
| `RosterViewModelTest.staleSearchResponseIsIgnored` | Typing fast cannot leave older results on screen |
| `RosterRotationTest.searchTextSurvivesRotation` | End to end, on a real Activity |
| `RosterRotationTest.allThreeFiltersSurviveTogether` | Search, programme and group together |
| `RosterRotationTest.rotatingTwiceIsStillStable` | No drift where a restore writes back a default |
| `StudentEditRotationTest.halfTypedInputIsNotOverwrittenByRotation` | The prefill bug, caught |
| `StudentEditRotationTest.aClearedFieldStaysClearedAfterRotation` | Empty is not the same as unfilled |
| `RoomMigrationTest` | 1 → 2 → 3 keeps the saved edit and the queue |

Reports land in `app/build/reports/tests/` and
`app/build/reports/androidTests/connected/`. Screenshot both — they are the
"demonstration that form/search state survives rotation" the brief asks for,
and they are reproducible in a way a video of someone turning a phone is not.

---

## 11. Likely questions, and short answers

**Why not keep the search text in the Activity and save it in
`onSaveInstanceState`?** It would work. But then the ViewModel could not decide
when to refetch, and it could not be tested without an Activity. The state
belongs with the logic that reads it.

**Why `SavedStateHandle` rather than making `Filters` Parcelable and saving
that?** One mechanism instead of two. The handle is already wired into the
Activity's saved state by `ViewModelFactory`; adding a Parcelable would be a
second path to keep in step with the first.

**What happens if the process is killed mid-sync?** Nothing is lost. The
operation is still `PENDING` or `SYNCING` in Room, and WorkManager re-enqueues
it after the reboot. The `operation_id` is unchanged, so if the server had
already applied it, the retry replays the stored receipt instead of applying it
twice.

**Why does the roster list come from Room rather than the API response?** So
the screen has something to draw immediately and keeps working offline. The
network is a refresh of Room, never the source the UI reads. It is also why the
offline banner can say the list *may be incomplete* rather than showing nothing.

**Where would you put a "recently viewed students" list?** `SavedStateHandle` if
it is only meant to survive the session; Room if it should still be there
tomorrow. The question is how long it has to live, not how big it is.

**Show me a change you could make live.** Add a fourth filter — say, sync status
— and the shape is already there: a key in `SavedStateHandle`, a setter that
resets the page, a parameter on `cachedRoster`, and one line in
`observeFiltered`. That is the supervised change worth rehearsing, because it
touches the ViewModel, the repository interface, the DAO and the layout, and you
can talk through each layer as you go.
