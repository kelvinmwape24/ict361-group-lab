package zm.mu.ict361lab.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Database and network work never runs on the main thread.
 *
 * diskIO is a single thread on purpose: Room writes stay ordered, so a
 * save-then-queue pair cannot interleave with another save.
 */
public final class AppExecutors {

    private static final AppExecutors INSTANCE = new AppExecutors();

    private final ExecutorService diskIO    = Executors.newSingleThreadExecutor();
    private final ExecutorService networkIO = Executors.newFixedThreadPool(3);
    private final Executor mainThread       = new MainThreadExecutor();

    private AppExecutors() {}

    public static AppExecutors get() { return INSTANCE; }

    public Executor diskIO()    { return diskIO; }
    public Executor networkIO() { return networkIO; }
    public Executor mainThread(){ return mainThread; }

    private static final class MainThreadExecutor implements Executor {
        private final Handler handler = new Handler(Looper.getMainLooper());
        @Override public void execute(Runnable command) { handler.post(command); }
    }
}
