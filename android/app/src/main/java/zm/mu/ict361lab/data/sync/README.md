Mordecai Salim Traore (202403846) — Sync

SyncWorker (WorkManager):
- Network constraint
- Exponential backoff
- Reuse operation_id on retry
- Status labels: SAVED_LOCAL, PENDING, SYNCING, SYNCED, ACTION_REQUIRED
- Pauses on 401 (session expiry)
