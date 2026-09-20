# ICT361 Group Lab — Contribution Log

**Project:** Android Student Registration & Lab Group Management
**Module:** ICT361 Mobile Application Development
**Institution:** Mulungushi University
**Group Size:** 15 members
**Repo:** https://github.com/kelvinmwape24/ict361-group-lab

---

## How to Read This Log

Each row records one member's work:
- **Task** — what they built or tested
- **Commit** — the git commit hash or branch name
- **Evidence** — file paths in the repo showing the work

Every member must have at least one code contribution and one test they ran.

---

## Team Assignments

| Team | Members | Responsibility |
|------|---------|----------------|
| UI / UX | Mbasela Fabian, Ben Chola, Katanga Miti | Wireframes, layouts, accessibility |
| Android Architecture | Agrippa C. Hamasukwa, Mainza Muunda, Chibesa Mumbi | Activities, ViewModels, Retrofit |
| Local Storage / Sync | Collins Chanda, Mordecai Salim Traore, Mapalo Chilufya | Room, WorkManager, offline queue |
| Backend / Database | Kelvin Mwape, Kansamba Auxiria, Edwin Makuyu | Node.js, MySQL, API, challenges |
| Testing / Integration | Racheal Daka, Lamin Traore, Salima Banda | All 8 checklist tests, evidence |

---

## Contribution Log

| # | Name | Student# | Team | Task | Commit | Evidence |
|---|------|----------|------|------|--------|----------|
| 1 | Kelvin Mwape | 202203897 | Backend / DB | Built Node.js server, MySQL schema, all API routes, Challenge 1 fix, lecturer password hash, Collins number correction, GitHub repo setup | main (initial commit) | server/, database/schema.sql, database/seed.sql, docs/api_contract.md, tests/challenge_1_evidence/result.txt |
| 2 | Kansamba Auxiria | 202206607 | Backend / DB | Test suite for all 16 API endpoints (login, CRUD, assign, sync, edge cases, SQL injection) | pending | tests/checklist_evidence/backend_endpoint_tests.md |
| 3 | Edwin Makuyu | 202408031 | Backend / DB | Backend setup guide + Windows curl examples for every endpoint + test reset_fixture.sql | pending | docs/setup_backend.md, docs/api_contract.md |
| 4 | Mbasela Fabian | 202404688 | UI / UX | Wireframes 3-4 (Student Profile, Lecturer Roster) + XML layouts + classmate feedback | pending | docs/screens/wireframe_03_profile.png, wireframe_04_roster.png, android/.../activity_student_profile.xml, activity_roster.xml |
| 5 | Ben Chola | 202404172 | UI / UX | Wireframes 5-6 (Student Editor, Sync Status) + strings.xml + accessibility checklist | pending | docs/screens/wireframe_05_editor.png, wireframe_06_sync.png, docs/wireframes/accessibility.md, android/.../res/values/strings.xml |
| 6 | Katanga Miti | 202403552 | UI / UX | Wireframes 1-2 (Register, Login) + XML layouts + 3-classmate feedback summary | pending | docs/screens/wireframe_01_register.png, wireframe_02_login.png, docs/wireframes/feedback.md, android/.../activity_register.xml, activity_login.xml |
| 7 | Agrippa C. Hamasukwa | 202403019 | Android Arch | Retrofit client + ApiService + DTOs + AuthRepository + endpoint testing with Postman | pending | android/.../data/remote/RetrofitClient.java, ApiService.java, data/remote/dto/, data/repository/AuthRepository.java |
| 8 | Mainza Muunda | 202401150 | Android Arch | StudentRepository + ViewModels (Login, Roster, Student) + UiState + architecture diagram + fake repository unit test | pending | android/.../data/repository/StudentRepository.java, ui/common/UiState.java, docs/architecture/system_design.md |
| 9 | Chibesa Mumbi | 202001699 | Android Arch | All 6 Activities + ViewModel wiring + ViewBinding + lifecycle handling + rotation tests | pending | android/.../ui/auth/, ui/student/, ui/lecturer/, ui/sync/ |
| 10 | Collins Chanda | 202204674 | Storage / Sync | Room entities (Student, Account, PendingOperation) + DAOs + AppDatabase + migration test | pending | android/.../data/local/entity/, data/local/dao/, data/local/AppDatabase.java |
| 11 | Mordecai Salim Traore | 202403846 | Storage / Sync | SyncWorker + WorkManager constraints + retry backoff + status labels (SAVED_LOCAL, PENDING, SYNCING, SYNCED, ACTION_REQUIRED) | pending | android/.../data/sync/SyncWorker.java |
| 12 | Mapalo Chilufya | 202405943 | Storage / Sync | Pending operation queue + @Transaction save-and-queue + conflict UI + sync status screen | pending | android/.../data/local/entity/PendingOperationEntity.java, ui/sync/SyncStatusActivity.java |
| 13 | Racheal Daka | 202303375 | Testing | Checklist tests 5, 6 (offline save, conflicting edit) + Challenge 3 evidence + Sharesheet test | pending | tests/checklist_evidence/, tests/challenge_3_evidence/result.txt |
| 14 | Lamin Traore | 202403844 | Testing | Checklist tests 3, 4 (full group, failed transfer) + accessibility test + screenshots of every screen | pending | tests/checklist_evidence/ |
| 15 | Salima Banda | 202305732 | Testing | Test plan + Checklist tests 1, 2, 7 (valid registration, invalid fields, roles) + student registration in app | pending | tests/test_plan.md, tests/checklist_evidence/ |

---

## Commit Hashes

Run this command anytime to see the last commit per person:

```cmd
git log --pretty=format:"%h | %an | %s" --all