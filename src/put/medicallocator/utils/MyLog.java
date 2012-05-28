package put.medicallocator.utils;

/**
 * Internal class for logging. It is a wrapper class for the {@link android.util.Log} class.
 */
public class MyLog {

	/**
	 * In the production release, REMEMBER to change this value.
	 * TODO: Alternatively:
	 * - prepare build script for this project,
	 * - put the {{@link #LOGLEVEL} value in some properties file deployed along with the application.
	 */
	private static final int LOGLEVEL = android.util.Log.VERBOSE;

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
}
