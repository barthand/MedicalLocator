package put.medicallocator.utils;

import android.os.AsyncTask;

public final class AsyncTaskUtils {

    /**
     * Checks whether task is not null and if it is running.
     */
    public static boolean isRunning(AsyncTask<?, ?, ?> task) {
        return task != null && task.getStatus() == AsyncTask.Status.RUNNING;
    }

    private AsyncTaskUtils() {
    }

}
