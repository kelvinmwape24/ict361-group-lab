📚 ICT361 Mobile Application Development — Full Group Lab Report

Project: Android Student Registration \& Lab Group Management

Institution: Mulungushi University

School: Engineering and Technology

Department: Computer Science and IT

Module: ICT361 Mobile Application Development

Submission Date: 21 September 2026

Repository: https://github.com/kelvinmwape24/ict361-group-lab



Table of Contents

Executive Summary



Group Overview



Team 1 — UI / UX Design



Team 2 — Android Architecture



Team 3 — Local Storage \& Sync



Team 4 — Backend \& Database



Team 5 — Testing \& Integration



Integration — How All Teams Connect



Challenges 1–3



Minimum Demonstration Checklist



Setup \& Reproduction



Files Delivered



Compliance with Lecturer Requirements



Conclusion



Sign-Off



1\. Executive Summary

Our group of 15 students built a complete Android application for student registration and lab group management. The application has five integrated layers:



UI/UX — wireframes, XML layouts, accessibility



Android Architecture — MVVM, Activities, ViewModels, Retrofit



Local Storage \& Sync — Room database, WorkManager, offline queue



Backend \& Database — Node.js server, MySQL, JWT auth, 3 challenges



Testing \& Integration — 8 checklist tests, all evidence captured



Data flows from the Android app → REST API → MySQL and back, with offline support and conflict resolution.



Repository: https://github.com/kelvinmwape24/ict361-group-lab



2\. Group Overview

2.1 Team Structure

Team	Members	Responsibility

Team 1 — UI/UX	Katanga Miti, Mbasela Fabian, Ben Chola	Wireframes, layouts, accessibility

Team 2 — Android Architecture	Agrippa C. Hamasukwa, Mainza Muunda, Chibesa Mumbi	MVVM, Retrofit, Activities

Team 3 — Local Storage/Sync	Collins Chanda, Mordecai Salim Traore, Mapalo Chilufya	Room, WorkManager, offline

Team 4 — Backend/Database	Kelvin Mwape, Kansamba Auxiria, Edwin Makuyu	Node.js, MySQL, 3 challenges

Team 5 — Testing/Integration	Salima Banda, Lamin Traore, Racheal Daka	8 checklist tests, evidence

2.2 Roster (15 members)

\#	Name	Student#	Team

1	Kelvin Mwape	202203897	Backend / DB

2	Kansamba Auxiria	202206607	Backend / DB

3	Edwin Makuyu	202408031	Backend / DB

4	Katanga Miti	202403552	UI / UX

5	Mbasela Fabian	202404688	UI / UX

6	Ben Chola	202404172	UI / UX

7	Agrippa C. Hamasukwa	202403019	Android Arch

8	Mainza Muunda	202401150	Android Arch

9	Chibesa Mumbi	202001699	Android Arch

10	Collins Chanda	202204674	Storage / Sync

11	Mordecai Salim Traore	202403846	Storage / Sync

12	Mapalo Chilufya	202405943	Storage / Sync

13	Salima Banda	202305732	Testing

14	Lamin Traore	202403844	Testing

15	Racheal Daka	202303375	Testing

2.3 Required Stack (all used)

Android Studio, Java, XML Views



ViewModel / LiveData



Room



Retrofit / OkHttp



WorkManager



Node.js / Express



MySQL (InnoDB)



3\. Team 1 — UI / UX Design

3.1 Members and Tasks

Member	Task

Katanga Miti (lead)	Wireframes 1–2 (Register, Login) + XML layouts

Mbasela Fabian	Wireframes 3–4 (Profile, Roster) + XML layouts

Ben Chola	Wireframes 5–6 (Editor, Sync) + strings.xml + accessibility

3.2 Design Deliverables

6 screens designed:



Register — claim code, name, 9-digit number, programme dropdown, password



Login — email, password, Sign In button



Student Profile — name, number, programme, group card with occupancy bar (14/15)



Lecturer Roster — search bar, filter chips, student list, pagination, + Add button



Student Editor — number, name, programme, group dropdown, Save/Cancel, Delete



Sync Status — pending operations, status labels, Sync Now



Accessibility rules applied:



All fields have android:labelFor and contentDescription for TalkBack



Touch targets ≥ 48dp



Layout works at 200% text size (ScrollView + ConstraintLayout)



Inline error messages below fields (not dialogs)



Predictable back navigation on every screen



Feedback collected from 3 classmates — recorded in docs/wireframes/feedback.md with 2 documented improvements.



3.3 Files Delivered

text

docs/screens/

├── wireframe\_01\_register.png

├── wireframe\_02\_login.png

├── wireframe\_03\_profile.png

├── wireframe\_04\_roster.png

├── wireframe\_05\_editor.png

└── wireframe\_06\_sync.png



docs/wireframes/

├── feedback.md

└── accessibility.md



android/app/src/main/res/

├── layout/activity\_register.xml

├── layout/activity\_login.xml

├── layout/activity\_student\_profile.xml

├── layout/activity\_roster.xml

├── layout/activity\_student\_editor.xml

├── layout/activity\_sync\_status.xml

└── values/strings.xml

3.4 How to Demonstrate

"Our team designed the user interface for all 6 screens. We used Material Design components, ensured 48dp touch targets for accessibility, and tested with 3 classmates. Based on their feedback we made 2 improvements: moved the Save button above the fold for the editor screen and increased input field padding for easier tapping."



4\. Team 2 — Android Architecture

4.1 Members and Tasks

Member	Task

Agrippa C. Hamasukwa (lead)	Retrofit client, ApiService, DTOs, AuthRepository

Mainza Muunda	StudentRepository, ViewModels, UiState, architecture diagram

Chibesa Mumbi	6 Activities, ViewModel wiring, ViewBinding, lifecycle

4.2 Architecture

The app follows the MVVM pattern:



text

UI (Activity + XML)

&#x20;   ↓ observes

ViewModel (LiveData)

&#x20;   ↓ calls

Repository (single source of truth)

&#x20;   ↓

Room (local)  +  Retrofit (remote)

Rules from the PDF enforced:



Screens only display state (no direct DB/API access)



Repositories coordinate data



DB and network work off the main thread



Room for durable records and drafts



4.3 Components Built

Retrofit client — RetrofitClient.java with base URL http://10.0.2.2:3000/ (emulator reaches host PC).



ApiService interface — endpoints matching docs/api\_contract.md:



login(LoginRequest)



getMyProfile(token)



listStudents(token, query, group, page, size)



assignGroup(token, studentId, groupId)



sync(token, SyncRequest)



DTOs — LoginRequest, LoginResponse, StudentDto, SyncRequest.



Repositories — AuthRepository (login + register), StudentRepository (CRUD + assign).



ViewModels — LoginViewModel, RosterViewModel, StudentViewModel — expose LiveData<UiState>.



6 Activities — Login, Register, StudentProfile, Roster, StudentEditor, SyncStatus — wired to ViewModels via ViewBinding.



Architecture diagram — saved at docs/architecture/system\_design.md.



Fake repository unit test — proves ViewModel logic works without a real network.



4.4 Files Delivered

text

android/app/src/main/java/zm/mu/ict361lab/

├── ui/auth/LoginActivity.java

├── ui/auth/RegisterActivity.java

├── ui/student/StudentProfileActivity.java

├── ui/lecturer/RosterActivity.java

├── ui/lecturer/StudentEditorActivity.java

├── ui/sync/SyncStatusActivity.java

├── ui/common/UiState.java

├── ui/common/ViewModelFactory.java

├── data/remote/RetrofitClient.java

├── data/remote/ApiService.java

├── data/remote/dto/\*.java

├── data/repository/AuthRepository.java

├── data/repository/StudentRepository.java

└── util/Constants.java

4.5 How to Demonstrate

"Our team built the architecture. The app uses MVVM — UI observes ViewModel, ViewModel calls Repository, Repository talks to Room and Retrofit. We have 6 Activities wired to ViewModels via ViewBinding. Form state survives rotation and process death because drafts are stored in Room."



Live demo:



Rotate the emulator → form state preserved



Kill app → reopen → saved draft still present



5\. Team 3 — Local Storage \& Sync

5.1 Members and Tasks

Member	Task

Collins Chanda (lead)	Room entities, DAOs, AppDatabase, migration test

Mordecai Salim Traore	SyncWorker, WorkManager, retry backoff, status labels

Mapalo Chilufya	Pending operation queue, conflict UI, sync status screen

5.2 Room Database

3 entities:



StudentEntity — student\_id (PK), student\_number, name, program\_id, group\_id, version, is\_deleted

AccountEntity — account\_id, email, role, student\_id, token

PendingOperationEntity — operation\_id (PK), student\_id, account\_id, type, payload\_json, base\_version, status, server\_result\_json, created\_at



2 DAOs: StudentDao, PendingOperationDao.



AppDatabase — v1, stored locally on device.



5.3 Sync Engine

SyncWorker (WorkManager):



Network constraint (only runs when online)



Exponential backoff on retry



Reuses same operation\_id on retry (idempotency)



Pauses on session expiry



Pauses if account switches



Status labels implemented:



SAVED\_LOCAL — stored in Room, not yet sent



PENDING — queued for sync



SYNCING — currently being sent



SYNCED — server accepted



ACTION\_REQUIRED — conflict or error needs user review



Conflict handling UI — shows both local proposal and server record side by side. User chooses: keep local, accept server, or cancel.



5.4 Rules from PDF Enforced

Every offline change gets a UUID operation\_id



Save change + queue in ONE Room transaction



On 409 CONFLICT — preserve local proposal



On 410 STUDENT\_DELETED — delete local, don't retry



Stale offline edit never recreates a deleted student



Queue is scoped per account



5.5 Files Delivered

text

android/app/src/main/java/zm/mu/ict361lab/

├── data/local/AppDatabase.java

├── data/local/entity/StudentEntity.java

├── data/local/entity/AccountEntity.java

├── data/local/entity/PendingOperationEntity.java

├── data/local/dao/StudentDao.java

├── data/local/dao/PendingOperationDao.java

├── data/sync/SyncWorker.java

├── data/sync/OperationQueue.java

└── ui/sync/SyncStatusActivity.java

5.6 How to Demonstrate

"Our team built the offline layer. Every change a user makes offline is stored in Room with a unique operation ID. When the phone comes back online, WorkManager triggers SyncWorker which sends each operation to the backend. If the server responds with CONFLICT, the app shows a review screen — it never silently overwrites."



Live demo:



Turn off WiFi on emulator



Make edit → app shows SAVED\_LOCAL



Turn WiFi on → sync fires → status changes to SYNCED



Kill app and reopen → pending operation is still there



6\. Team 4 — Backend \& Database

6.1 Members and Tasks

Member	Task

Kelvin Mwape (lead)	Node.js server, MySQL schema, all routes, 3 challenges, ER diagram

Kansamba Auxiria	Test suite for 16 endpoints, edge cases, SQL injection

Edwin Makuyu	Setup guide, curl examples, reset fixture validation

6.2 Database

5 tables (InnoDB):



programmes — CS, IT, DS



lab\_groups — G01, G02, G03, G04 (capacity 15)



students — all records with claim codes



accounts — login credentials



operation\_receipts — idempotency log



6.3 API Endpoints

Method	Route	Role	Purpose

POST	/auth/login	Public	Login (JWT)

POST	/auth/register	Public	Student registration with claim code

GET	/students	LECTURER	List with search + filters + pagination

POST	/students	LECTURER	Create student + claim code

PATCH	/students/:id	LECTURER	Update with version check

DELETE	/students/:id	LECTURER	Soft delete

POST	/students/:id/assign	LECTURER	Assign group (Challenge 1)

GET	/students/me	STUDENT	View own profile

PATCH	/students/me	STUDENT	Edit own name/programme

POST	/students/me/group-request	STUDENT	Request group change

POST	/students/me/number-correction	STUDENT	Request number fix

POST	/sync	Both	Batch offline ops (Challenge 2 \& 3)

6.4 Security

bcrypt password hashing



JWT authentication (8h expiry)



Role-based access on every endpoint



Parameterised SQL (no injection)



Server-side validation



Students can only access their own record



6.5 Files Delivered

text

server/

├── server.js

├── db.js

├── hash.js

├── package.json

├── .env.example

├── middleware/auth.js

└── routes/auth.js, students.js, me.js, sync.js



database/

├── schema.sql

├── seed.sql

└── reset\_fixture.sql



docs/

├── ER-DIAGRAM.png

└── api\_contract.md

6.6 How to Demonstrate

"Our team built the backend. It's a Node.js/Express server on port 3000 connected to a MySQL database. It handles all authentication, student management, group assignment, and offline sync. We solved all three challenges from the brief."



Live demo:



Show server running: curl http://localhost:3000



Login: curl -X POST /auth/login ... → token returned



List students: 15 returned



Show Challenge 1, 2, 3 evidence files



7\. Team 5 — Testing \& Integration

7.1 Members and Tasks

Member	Task

Salima Banda (lead)	Test plan + tests 1, 2, 7

Lamin Traore	Tests 3, 4, 8 + accessibility + screenshots

Racheal Daka	Tests 5, 6 + Challenge 3 + Sharesheet test

7.2 Minimum Demonstration Checklist

The PDF requires 8 tests. Our team runs them all:



\#	Test	Expected Result	Status

1	Valid registration + lecturer CRUD	Correct on phone + server	Pass

2	Duplicate number + invalid fields	Rejected, form retained	Pass

3	Full group + simultaneous requests	No group > 15	Pass

4	Failed transfer + repeated deletion	Old group kept, one place released	Pass

5	Offline save + interrupted sync	Survives restart, applied once	Pass

6	Conflicting edit + remote deletion	Conflict shown, no resurrection	Pass

7	Roles + account switching	No cross-access	Pass

8	Search filters + accessibility	Works at 200% text	Pass

7.3 Sharesheet (Activity D)

The lecturer roster can share a group summary through the Android Sharesheet:



Only group label + counts (no personal data)



No camera, location, or Bluetooth permission needed



7.4 Files Delivered

text

tests/

├── test\_plan.md

├── challenge\_1\_evidence/

│   └── result.txt

├── challenge\_2\_evidence/

│   └── result.txt

├── challenge\_3\_evidence/

│   └── result.txt

└── checklist\_evidence/

&#x20;   ├── test1\_registration.png

&#x20;   ├── test2\_duplicate.png

&#x20;   ├── test3\_simultaneous.png

&#x20;   ├── test4\_transfer.png

&#x20;   ├── test5\_offline.png

&#x20;   ├── test6\_conflict.png

&#x20;   ├── test7\_roles.png

&#x20;   ├── test8\_accessibility.png

&#x20;   └── backend\_endpoint\_tests.md



docs/defect\_log.md

7.5 How to Demonstrate

"Our team ran all 8 tests from the checklist, plus the 3 challenges. We captured screenshots for each test and documented every bug in the defect log. Every test passed."



Live demo:



Show one test screenshot from each of the 8 tests



Show the challenge result files



Explain one defect and how it was fixed



8\. Integration — How All Teams Connect

8.1 Data Flow

text

Android UI (Team 1)

&#x20;   ↓ observes

ViewModel (Team 2)

&#x20;   ↓ calls

Repository (Team 2)

&#x20;   ↓

Room (Team 3)  +  Retrofit (Team 2)

&#x20;                   ↓ HTTP

&#x20;               Node.js API (Team 4)

&#x20;                   ↓ SQL

&#x20;               MySQL (Team 4)

&#x20;                   ↑

&#x20;               Testing (Team 5) verifies all layers

8.2 Integration Points

From	To	Interface

UI	ViewModel	LiveData / ViewBinding

ViewModel	Repository	Method calls

Repository	Room	DAO methods

Repository	Retrofit	ApiService interface

Retrofit	Backend API	HTTP + JWT (matches docs/api\_contract.md)

Backend API	MySQL	Parameterised SQL

8.3 Verification

UI works with ViewModels (Team 2 tests)



ViewModels work with repositories (fake repo test)



Repositories sync with backend (Team 5 tests)



Backend works with MySQL (Team 4 endpoint tests)



All layers together (end-to-end test with emulator + real backend)



9\. Challenges 1–3

9.1 Challenge 1 — Two Phones Request Last Place

Setup: G01 starts with 14 members.



Test: Two simultaneous POST /students/:id/assign requests.



Result:



One returns {"success":true}



Other returns {"error":"GROUP\_FULL"}



Final G01 count: 15 (never 16)



Rule enforced: MySQL transaction with SELECT ... FOR UPDATE on the group row.



Evidence: tests/challenge\_1\_evidence/result.txt



9.2 Challenge 2 — Response Disappears

Test: Server saves a record, response interrupted, retry with same operation\_id.



Result:



Second call returns stored result (no duplicate)



Only 1 receipt in operation\_receipts



Student version didn't increment twice



Rule enforced: operation\_id primary key in operation\_receipts + payload hash check.



Evidence: tests/challenge\_2\_evidence/result.txt



9.3 Challenge 3 — Offline Changes Become Outdated

Test: Two devices edit same student offline, reconnect.



Result:



First sync: {"success":true,"new\_version":5}



Second sync: {"error":"CONFLICT","current\_version":5}



Edit to deleted student: {"error":"STUDENT\_DELETED"}



Rule enforced: base\_version comparison inside transaction + is\_deleted check.



Evidence: tests/challenge\_3\_evidence/result.txt



10\. Minimum Demonstration Checklist

All 8 tests required by the PDF pass. Evidence saved in tests/checklist\_evidence/:



✅ Valid registration + lecturer CRUD



✅ Duplicate number + invalid fields



✅ Full group + simultaneous requests



✅ Failed transfer + repeated deletion



✅ Offline save + interrupted sync



✅ Conflicting edit + remote deletion



✅ Roles + account switching



✅ Search filters + accessibility



11\. Setup \& Reproduction

11.1 Backend Setup

cmd

git clone https://github.com/kelvinmwape24/ict361-group-lab.git

cd ict361-group-lab



mysql -u root -p < database/schema.sql

mysql -u root -p < database/seed.sql



cd server

copy .env.example .env

notepad .env

Set DB\_PASS to your MySQL password. Save.



cmd

npm install

node hash.js

Copy the hash. Then:



cmd

mysql -u root -p

sql

USE ict361\_lab;

UPDATE accounts SET password\_hash='PASTE\_HASH\_HERE' WHERE email='lecturer@mu.ac.zm';

exit;

cmd

npm run dev

Result: Server running on http://localhost:3000



Verify:



cmd

curl http://localhost:3000

Result: {"status":"ICT361 server running"}



11.2 Android Setup

Open Android Studio



Open android/ folder



Set BASE\_URL in RetrofitClient.java:



Emulator: http://10.0.2.2:3000/



Real device: http://<PC-IP>:3000/



Run on emulator



11.3 Test Credentials

Lecturer: lecturer@mu.ac.zm / admin123



Claim codes: MU-000001 through MU-000015



11.4 Reset Fixture

cmd

mysql -u root -p < database/reset\_fixture.sql

12\. Files Delivered

12.1 Repository Structure

text

ict361-group-lab/

├── android/                  Android Studio project

├── server/                   Node.js backend

├── database/                 SQL scripts

├── docs/                     ER diagram, API contract, wireframes, architecture

├── tests/                    Challenge evidence + checklist results

├── README.md                 Setup instructions

├── CONTRIBUTION\_LOG.md       All 15 members' tasks

├── REFLECTIONS.md            Individual reflections

├── AI\_SOURCE\_LOG.md          AI usage log

└── .gitignore

12.2 Submission Package

✅ Android source code



✅ Node.js source code



✅ Database scripts



✅ Test evidence



✅ Architecture + wireframes



✅ ER diagram



✅ API contract



✅ Setup README



✅ Contribution log



✅ AI/source log



✅ Per-member reflections



Excluded: .env (secrets), node\_modules/, build/



13\. Compliance with Lecturer Requirements

Requirement	Status

Android Studio + Java + XML Views	✅

ViewModel / LiveData	✅

Room	✅

Retrofit / OkHttp	✅

WorkManager	✅

Node.js / Express	✅

MySQL (InnoDB)	✅

5 teams of 3	✅

3 challenges solved	✅

8 checklist tests pass	✅

ER diagram	✅

API contract	✅

Database scripts	✅

Wireframes + accessibility	✅

Sharesheet	✅

Per-member reflection	✅

Contribution log	✅

AI/source log	✅

Setup README	✅

14\. Conclusion

Our group of 15 students built a complete, tested, and documented Android application for student registration and lab group management. The app:



Uses the required stack (Java, XML, Room, Retrofit, WorkManager, Node.js, MySQL)



Follows MVVM architecture



Supports offline-first sync with conflict resolution



Enforces all data integrity rules (15-member limit, 9-digit unique numbers, soft delete)



Solves all 3 challenges with reproducible evidence



Passes all 8 checklist tests



Repository: https://github.com/kelvinmwape24/ict361-group-lab



15\. Sign-Off

\#	Name	Student#	Team	Signature

1	Kelvin Mwape	202203897	Backend / DB	\_\_\_\_\_\_\_\_\_\_

2	Kansamba Auxiria	202206607	Backend / DB	\_\_\_\_\_\_\_\_\_\_

3	Edwin Makuyu	202408031	Backend / DB	\_\_\_\_\_\_\_\_\_\_

4	Katanga Miti	202403552	UI / UX	\_\_\_\_\_\_\_\_\_\_

5	Mbasela Fabian	202404688	UI / UX	\_\_\_\_\_\_\_\_\_\_

6	Ben Chola	202404172	UI / UX	\_\_\_\_\_\_\_\_\_\_

7	Agrippa C. Hamasukwa	202403019	Android Arch	\_\_\_\_\_\_\_\_\_\_

8	Mainza Muunda	202401150	Android Arch	\_\_\_\_\_\_\_\_\_\_

9	Chibesa Mumbi	202001699	Android Arch	\_\_\_\_\_\_\_\_\_\_

10	Collins Chanda	202204674	Storage / Sync	\_\_\_\_\_\_\_\_\_\_

11	Mordecai Salim Traore	202403846	Storage / Sync	\_\_\_\_\_\_\_\_\_\_

12	Mapalo Chilufya	202405943	Storage / Sync	\_\_\_\_\_\_\_\_\_\_

13	Salima Banda	202305732	Testing	\_\_\_\_\_\_\_\_\_\_

14	Lamin Traore	202403844	Testing	\_\_\_\_\_\_\_\_\_\_

15	Racheal Daka	202303375	Testing	\_\_\_\_\_\_\_\_\_\_

End of Report

