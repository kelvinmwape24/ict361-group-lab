# Section 4 — Android Architecture (Chibesa Mumbi, <your student number>)

Corrected against the code at the time of writing. Every class and path named
here exists in the repository and can be opened during questioning.

---

## 4.1 What I was responsible for

Contribution log row 9: the Activities, the ViewModel wiring, ViewBinding, and
lifecycle handling. Agrippa led the area, Mainza and I built it out.

---

## 4.2 Architecture

MVVM, as the brief requires:

```
Activity + XML layout
    ↓ observes LiveData
ViewModel  (+ SavedStateHandle for screen state)
    ↓ calls
StudentRepository  (interface — single source of truth)
    ↓
Room (durable local records + the pending-operation queue)
Retrofit / OkHttp (the API)
```

The four rules from the brief, and where each is enforced:

| Rule | Enforced by |
|---|---|
| Screens only display state — no direct DB or API access | No Activity imports `AppDatabase`, a DAO or `ApiService`. Checkable with one search. |
| Repositories coordinate data | `StudentRepository` is an interface; `RoomStudentRepository` is the only implementation and the only class that touches both Room and Retrofit. |
| DB and network work off the main thread | `AppExecutors.diskIO()` — deliberately **single-threaded**, so Room writes stay in order and the pending queue cannot be reordered by the scheduler. |
| Room for durable records and drafts | `local_student` for records, `pending_operation` for the queue. |

The repository being an interface is what lets the ViewModel tests run on the
JVM in under a second against `FakeStudentRepository`, with no device and no
server.

---

## 4.3 Components built

**Retrofit client** — `data/remote/RetrofitClient.java`. The base URL is not
hard-coded in the client; it comes from `BuildConfig.API_BASE_URL`, set in
`app/build.gradle.kts` to `http://10.0.2.2:3000/`. `10.0.2.2` is how an emulator
reaches the host machine — `localhost` inside an emulator is the emulator
itself. Keeping it in the build file means a physical device needs a build-config
change, not a code change.

**`ApiService`** — 15 endpoints, matching `docs/api_contract.md` v2:
`login`, `register`, `students`, `createStudent`, `patchStudent`,
`deleteStudent`, `assign`, `me`, `patchMe`, `requestGroup`,
`requestNumberCorrection`, `groups`, `programmes`, `push` (`POST /sync`),
`changes` (`GET /sync/changes`). No method takes a token parameter — the OkHttp
auth interceptor attaches it, so no call site can forget it.

**DTOs** — nested inside `data/remote/Dtos.java`: `LoginRequest`,
`RegisterRequest`, `AuthResponse`, `StudentDto`, `PageDto`, `MeDto`,
`SyncRequest`, `SyncResponse`, `ChangesResponse` and the rest.

**Repositories** — `AuthRepository` (login, register, token storage) and
`RoomStudentRepository` behind the `StudentRepository` interface. Every write is
one Room transaction that records the edit *and* the pending operation together,
so a crash between the two is impossible.

**ViewModels — five, not three:** `AuthViewModel`, `RosterViewModel`,
`EditorViewModel` (lecturer edit), `StudentViewModel` (student's own record),
`SyncViewModel`. All expose `LiveData<UiState>`.

**Activities — eight files, six user-facing screens.** The six are Login,
Register, StudentProfile, Roster, StudentEditor and SyncStatus. Plus
`MainActivity`, which routes by stored role, and `StudentEditActivity`, the
student's own edit screen. All wired to ViewModels through ViewBinding.

**`ViewModelFactory`** extends `AbstractSavedStateViewModelFactory`, which is
what actually hands a `SavedStateHandle` to a ViewModel. A plain
`ViewModelProvider.Factory` cannot.

**Three `DialogFragment`s** — `ConfirmDialogFragment`, `ChoiceDialogFragment`,
`InputDialogFragment`, all returning via `setFragmentResult`.

**Architecture documentation** — `docs/architecture/system_design.md`, plus 14
UML and data diagrams (PlantUML source, rendered PNG and SVG, an editable
`.drawio`, and `ICT361_architecture_diagrams.pdf`).

---

## 4.4 Lifecycle handling — three defects fixed

This is the part worth being precise about, because it is what the individual
questioning targets.

**1. Screen state died with the process.** `RosterViewModel` held the search
text, both filters and the page number in plain fields. A ViewModel is retained
across a configuration change, so that survived rotation — but not the process
being reclaimed, and Android reclaims backgrounded processes freely. Every value
now lives in `SavedStateHandle`, under `roster.query`, `roster.programme`,
`roster.group`, `roster.page`, `roster.pages`, `roster.total`.

`requestToken` is deliberately **not** saved. Restoring an in-flight request
after a restart would apply a response to a screen that has moved on.

**2. The prefill overwrote what the user was typing.** The edit screens fill the
form from the record once, guarded by a `prefilled` flag. That flag was an
Activity field, so it reset on recreation, the LiveData observer re-delivered
the record, and `setText()` ran over half-typed input. It never actually
misbehaved — Android restores view state *after* `onStart()` while observers
fire *during* it, so the restore won. Wrong code with the right outcome, which
breaks the moment the ordering changes. The flag is now in `SavedStateHandle`,
alongside `baseVersion`.

**3. Dialogs vanished on rotation and leaked their Activity.** Every dialog was
`AlertDialog.Builder(this).show()`. Rotate with one open and it was gone, and
`(d, w) -> viewModel.delete()` captured an Activity that is finished by the time
the callback could run. All three are now DialogFragments.

**Where state lives, and why:**

| State | Where | Reason |
|---|---|---|
| Search, filters, page, `baseVersion`, `prefilled` | `SavedStateHandle` | Survives rotation *and* process death |
| In-flight request token | ViewModel field only | Must not be restored |
| Registration form draft | `SharedPreferences`, written in `onPause()` and `onSaveInstanceState()` | Survives the user leaving the app entirely. **The password is never written.** |
| Student records, the pending-operation queue | Room | Durable across reinstall-free restarts; the queue is the offline contract |
| View text, scroll, spinner selection | Android's own view state | Free, and the platform does it better |

---

## 4.5 Tests

| Suite | Count | Needs |
|---|---|---|
| `RosterViewModelTest` | 11 | JVM only |
| `ValidatorsTest` | 7 | JVM only |
| `RosterRotationTest` | 4 | Device or emulator |
| `StudentEditRotationTest` | 3 | Device or emulator |
| `RoomMigrationTest` | 1 | Device or emulator |

`gradlew test` runs the first 18 with no device and no server.
`gradlew connectedDebugAndroidTest` runs the other 8.

The rotation tests use `ActivityScenario.recreate()`, which destroys the
Activity and rebuilds it from saved state — exactly what a rotation does, but
reproducible and in a report rather than a video of someone turning a phone.
They need no server: they seed a session into SharedPreferences and read from
Room, and the network call simply fails, which incidentally exercises the
offline path as well.

The three that carry the lifecycle claim:

- `screenStateIsWrittenToSavedStateHandle` — the state is in the handle, not a field
- `stateSurvivesProcessDeath` — a ViewModel rebuilt from a restored handle resumes on page 3 of the right search
- `anInFlightRequestIsNotRestored` — the omission of `requestToken` is deliberate, not an oversight

---

## 4.6 Files delivered

```
android/app/src/main/java/zm/mu/ict361lab/
├── MainActivity.java
├── ui/auth/          LoginActivity, RegisterActivity, AuthViewModel
├── ui/student/       StudentProfileActivity, StudentEditActivity, StudentViewModel
├── ui/lecturer/      RosterActivity, RosterViewModel, StudentEditorActivity,
│                     EditorViewModel, StudentAdapter
├── ui/sync/          SyncStatusActivity, SyncViewModel, OperationAdapter
├── ui/common/        UiState, ViewModelFactory,
│                     ConfirmDialogFragment, ChoiceDialogFragment, InputDialogFragment
├── data/remote/      RetrofitClient, ApiService, Dtos
├── data/repository/  StudentRepository, RoomStudentRepository, AuthRepository, Result
└── util/             Constants, TokenStore, Validators, ApiErrors,
                      AppExecutors, ProgrammeStore

android/app/src/main/res/layout/     11 layouts, including dialog_input.xml
android/app/src/test/java/…          RosterViewModelTest, ValidatorsTest,
                                     FakeStudentRepository
android/app/src/androidTest/java/…   RosterRotationTest, StudentEditRotationTest,
                                     RoomMigrationTest
docs/lifecycle_and_data_flow.md      the written lifecycle rationale
```

---

## 4.7 How to demonstrate

> "The app is MVVM. The Activity observes LiveData on a ViewModel; the ViewModel
> calls a repository interface; the repository is the only thing that touches
> Room and Retrofit. Six screens, wired through ViewBinding.
>
> Screen state survives rotation *and* process death, and those are two
> different problems. Rotation is free — the ViewModel is retained. Process
> death is not: a ViewModel is gone when the process is reclaimed, so the search
> text, both filters and the page number go into `SavedStateHandle`, which the
> platform writes into the saved instance state. I have a unit test that rebuilds
> the ViewModel from a restored handle and checks it comes back on page 3 of the
> right search, and four instrumented tests that recreate the real Activity."

Live:

1. Roster → type a search, set both filters, page forward → **rotate** →
   everything returns.
2. **Force stop** the app and reopen → everything returns. *This is the one a
   ViewModel alone would fail.*
3. Open a student, type half a name, **rotate** → what you typed is still there.
4. Tap **Delete**, **rotate while the dialog is open** → the dialog is still
   there.

---

## 4.8 Corrections against the earlier draft of this section

Recorded because the earlier draft circulated and a marker comparing the two
should see the difference was deliberate.

| Earlier draft said | Actually |
|---|---|
| `LoginViewModel` | `AuthViewModel` — there is no `LoginViewModel` |
| Three ViewModels | Five: `AuthViewModel`, `RosterViewModel`, `EditorViewModel`, `StudentViewModel`, `SyncViewModel` |
| `data/remote/dto/*.java` | One file, `data/remote/Dtos.java`, with nested classes — there is no `dto` package |
| Base URL in `RetrofitClient.java` | `BuildConfig.API_BASE_URL`, set in `app/build.gradle.kts` |
| `getMyProfile(token)`, `listStudents(token, …)` | `me()`, `students(…)` — no method takes a token; the OkHttp interceptor attaches it |
| "Fake repository unit test" (one) | 18 JVM tests and 8 instrumented tests |
| "drafts are stored in Room" | **Wrong, and the most important correction.** Screen state is in `SavedStateHandle`. The registration draft is in `SharedPreferences`. Room holds records and the sync queue, not screen state. Saying "Room" in the viva invites the examiner to open the code and find nothing there. |
| No mention of `SavedStateHandle`, the DialogFragments, or the rotation tests | These are the substance of the lifecycle work; the draft described the state before it |
