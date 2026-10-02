# System Architecture

UI (Activities + XML)
    ↓ observes LiveData
ViewModel (Login, Roster, Student)
    ↓ calls
Repository (Auth, Student)
    ↓
Room (local) + Retrofit (remote)
    ↓
Node.js API → MySQL

## Rules
- Screens display state only
- Repositories coordinate data
- DB/network off main thread
- Room = durable records + drafts
- WorkManager = persistent sync
