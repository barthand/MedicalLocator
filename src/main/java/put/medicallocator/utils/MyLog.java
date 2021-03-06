package put.medicallocator.utils;

import android.util.Log;
import put.medicallocator.BuildConfig;

/**
 * Internal class for logging. It is a wrapper class for the {@link android.util.Log} class.
 */
public final class MyLog {

    /* TODO: Move to the properties file, so this value may change even at the runtime. */
    public static final boolean ASSERT_ENABLED = true;

    private static final int LOGLEVEL = BuildConfig.DEBUG ? android.util.Log.VERBOSE : Log.ERROR + 1;

    private static boolean VERBOSE = LOGLEVEL <= android.util.Log.VERBOSE;
    private static boolean DEBUG = LOGLEVEL <= android.util.Log.DEBUG;
    private static boolean INFO = LOGLEVEL <= android.util.Log.INFO;
    private static boolean WARN = LOGLEVEL <= android.util.Log.WARN;
    private static boolean ERROR = LOGLEVEL <= android.util.Log.ERROR;

    public static int v(String tag, String msg, Throwable tr) {
        if (VERBOSE) return android.util.Log.v(tag, msg, tr);
        return -1;
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (DEBUG) return android.util.Log.d(tag, msg, tr);
        return -1;
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (INFO) return android.util.Log.i(tag, msg, tr);
        return -1;
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (WARN) return android.util.Log.w(tag, msg, tr);
        return -1;
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (ERROR) return android.util.Log.e(tag, msg, tr);
        return -1;
    }

    public static int v(String tag, String msg) {
        if (VERBOSE) return android.util.Log.v(tag, msg);
        return -1;
    }

    public static int d(String tag, String msg) {
        if (DEBUG) return android.util.Log.d(tag, msg);
        return -1;
    }

    public static int i(String tag, String msg) {
        if (INFO) return android.util.Log.i(tag, msg);
        return -1;
    }

    public static int w(String tag, String msg) {
        if (WARN) return android.util.Log.w(tag, msg);
        return -1;
    }

    public static int e(String tag, String msg) {
        if (ERROR) return android.util.Log.e(tag, msg);
        return -1;
    }

    private MyLog() {
    }
}
