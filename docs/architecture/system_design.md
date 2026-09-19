\# ICT361 — System Architecture



\## Layered Structure



UI (Activities + XML) → ViewModel → Repository → Room + Retrofit

&#x20;                                         ↑

&#x20;                                   WorkManager



\## Rules (from PDF)

\- Screens display state only

\- Repositories coordinate data

\- DB/network work off main thread

\- Room = durable records + drafts (ViewModel alone doesn't survive process death)

\- WorkManager = persistent sync with connectivity constraints + retry backoff



\## Android Package Structure



zm.mu.ict361lab/

├── data/

│   ├── local/      Room: AppDatabase, entities, DAOs

│   ├── remote/     Retrofit: ApiService, RetrofitClient, dto/

│   ├── repository/ AuthRepository, StudentRepository

│   └── sync/       SyncWorker, OperationQueue

├── ui/

│   ├── auth/       LoginActivity, RegisterActivity, LoginViewModel

│   ├── student/    StudentProfileActivity, StudentEditActivity

│   ├── lecturer/   RosterActivity, StudentEditorActivity

│   ├── sync/       SyncStatusActivity

│   └── common/     UiState, ViewModelFactory

└── util/           Validators, TokenStore, Constants



\## Backend Structure



server/

├── server.js          Express entry

├── db.js              MySQL pool

├── middleware/auth.js JWT + role check

├── routes/

│   ├── auth.js        /auth/login

│   ├── students.js    /students CRUD + assign

│   └── sync.js        /sync batch with operation receipts

└── .env



\## Dependency Versions

\- Android Studio Hedgehog 2023.1.1+

\- Java 17

\- Room 2.6.1

\- Retrofit 2.9.0

\- OkHttp 4.12.0

\- WorkManager 2.9.0

\- Lifecycle 2.7.0

\- Node.js 20.x

\- Express 4.19.x

\- mysql2 3.11.x

\- bcrypt 5.1.x

\- jsonwebtoken 9.0.x

\- MySQL 8.0 (InnoDB)

