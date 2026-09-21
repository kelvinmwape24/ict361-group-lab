# ICT361 Group Lab — Contribution Log

**Project:** Android Student Registration & Lab Group Management
**Module:** ICT361 Mobile Application Development
**Institution:** Mulungushi University
**Group Size:** 16 members
**Repo:** https://github.com/kelvinmwape24/ict361-group-lab

---

## Team Assignments

| Team | Members | Responsibility |
|------|---------|----------------|
| UI / UX | Katanga Miti, Mbasela Fabian, Ben Chola | Wireframes, layouts, accessibility |
| Android Architecture | Agrippa C. Hamasukwa, Mainza Muunda, Chibesa Mumbi, Abel Simasiku | Activities, ViewModels, Retrofit |
| Local Storage / Sync | Collins Chanda, Mordecai Salim Traore, Mapalo Chilufya | Room, WorkManager, offline queue |
| Backend / Database | Kelvin Mwape, Kansamba Auxiria, Edwin Makuyu | Node.js, MySQL, API, challenges |
| Testing / Integration | Salima Banda, Lamin Traore, Racheal Daka | 8 checklist tests, evidence |

---

## Contribution Log

| # | Name | Student# | Team | Task | Commit | Evidence |
|---|------|----------|------|------|--------|----------|
| 1 | Kelvin Mwape | 202203897 | Backend / DB | Node.js server, MySQL schema, all API routes, Challenge 1-3, ER diagram, GitHub repo | main | server/, database/*.sql, docs/ER-DIAGRAM.png, docs/api_contract.md, tests/challenge_*/result.txt |
| 2 | Kansamba Auxiria | 202206607 | Backend / DB | Tested all 14 API endpoints + edge cases + SQL injection | pending | tests/checklist_evidence/backend_endpoint_tests.md |
| 3 | Edwin Makuyu | 202408031 | Backend / DB | Setup guide + Git Bash curl examples + reset fixture validation | pending | docs/setup_backend.md, docs/api_contract.md |
| 4 | Katanga Miti | 202403552 | UI / UX | Wireframes 1-2 (Register, Login) + XML layouts | pending | docs/screens/wireframe_01_register.png, wireframe_02_login.png, android/app/src/main/res/layout/activity_register.xml, activity_login.xml |
| 5 | Mbasela Fabian | 202404688 | UI / UX | Wireframes 3-4 (Dashboard, Lab Group) + XML layouts | pending | docs/screens/wireframe_03_dashboard.png, wireframe_04_lab_group.png, android/app/src/main/res/layout/activity_dashboard.xml, activity_lab_group.xml |
| 6 | Ben Chola | 202404172 | UI / UX | Wireframes 5-6 (Courses, Sync) + strings.xml + accessibility | pending | docs/screens/wireframe_05_courses.png, wireframe_06_sync.png, docs/wireframes/accessibility.md, android/app/src/main/res/values/strings.xml |
| 7 | Agrippa C. Hamasukwa | 202403019 | Android Architecture | Retrofit client + ApiService + DTOs + AuthRepository | pending | android/app/src/main/java/zm/mu/ict361lab/data/remote/, data/repository/AuthRepository.java |
| 8 | Mainza Muunda | 202401150 | Android Architecture | StudentRepository + ViewModels + UiState + architecture diagram | pending | android/app/src/main/java/zm/mu/ict361lab/ui/common/, data/repository/StudentRepository.java, docs/architecture/system_design.md |
| 9 | Chibesa Mumbi | 202001699 | Android Architecture | 6 Activities + ViewBinding + ViewModel wiring + AndroidManifest.xml | pending | android/app/src/main/java/zm/mu/ict361lab/ui/, android/app/src/main/AndroidManifest.xml |
| 10 | Abel Simasiku | 202407015 | Android Architecture | Unit tests + lifecycle verification + rotation tests | pending | tests/checklist_evidence/unit_tests.png, lifecycle_tests.png |
| 11 | Collins Chanda | 202204674 | Storage / Sync | Room entities + DAOs + AppDatabase + migration test | pending | android/app/src/main/java/zm/mu/ict361lab/data/local/ |
| 12 | Mordecai Salim Traore | 202403846 | Storage / Sync | SyncWorker + WorkManager + retry backoff + status labels | pending | android/app/src/main/java/zm/mu/ict361lab/data/sync/ |
| 13 | Mapalo Chilufya | 202405943 | Storage / Sync | Pending operation queue + @Transaction + conflict UI + sync screen | pending | android/app/src/main/java/zm/mu/ict361lab/ui/sync/ |
| 14 | Salima Banda | 202305732 | Testing | Test plan + Tests 1, 2, 7 (registration, validation, roles) | pending | tests/test_plan.md, tests/checklist_evidence/test1_*.png, test2_*.png, test7_*.png |
| 15 | Lamin Traore | 202403844 | Testing | Tests 3, 4, 8 (full group, transfer, accessibility) + screenshots | pending | tests/checklist_evidence/test3_*.png, test4_*.png, test8_*.png, screens/ |
| 16 | Racheal Daka | 202303375 | Testing | Tests 5, 6 (offline, conflict) + Challenge 3 + sharesheet | pending | tests/checklist_evidence/test5_*.png, test6_*.png, sharesheet.png, tests/challenge_3_evidence/ |

---

## Testing Evidence Location

All test evidence is committed to tests/:

| Test | Folder | Contents |
|------|--------|----------|
| Challenge 1 — two phones, last place | tests/challenge_1_evidence/ | result.txt |
| Challenge 2 — response disappears | tests/challenge_2_evidence/ | result.txt |
| Challenge 3 — offline conflicts | tests/challenge_3_evidence/ | result.txt |
| Minimum checklist (8 tests) | tests/checklist_evidence/ | One screenshot per test |
| Backend endpoint tests | tests/checklist_evidence/backend_endpoint_tests.md | Kansamba test file |

---

## Per-Member Reflection

Each member writes 1 concept learned + 1 bug solved in REFLECTIONS.md.

---

**Last updated:** 2026-09-21 by Kelvin Mwape
