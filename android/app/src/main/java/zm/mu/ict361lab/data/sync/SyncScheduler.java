package zm.mu.ict361lab.data.sync;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Sync is persistent work, not a background thread. WorkManager survives the
 * app being killed and the device rebooting, and it will not even start until
 * there is a connection.
 */
public final class SyncScheduler {

    public static final String WORK_NAME = "ict361-sync";

    private SyncScheduler() {}

    /** Called after every queued change, and by the manual Sync button. */
    public static void requestSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(WORK_NAME)
                .build();

        // APPEND_OR_REPLACE, never REPLACE: operations must reach the server in
        // the order the user made them, and a running sync must finish before
        // the next one starts.
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request);
    }
}
