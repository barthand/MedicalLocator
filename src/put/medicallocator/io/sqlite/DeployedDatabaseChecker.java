package put.medicallocator.io.sqlite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import put.medicallocator.utils.MyLog;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

/**
 * TODO: Consider retrieving data from webservice instead of copying local-stored DB
 * from the assets/ folder.
 */
class DeployedDatabaseChecker {

	private static final String DB_PATH = 	Environment.getDataDirectory() + "/data/put.medicallocator/databases/";
	private static final int MAX_CHUNK_COUNT = 10;
	private static final String TAG = "DatabaseInitializer";

	private final Context context;
	private final SQLiteOpenHelper helper;

	public DeployedDatabaseChecker(Context context, SQLiteOpenHelper helper) {
		this.context = context;
		this.helper = helper;
	}

	/**
     * Checks if the DB exists and is current. If those conditions are not met,
     * the DB is created from scratch.
     */
    public boolean ensureDatabaseIsCurrent() {
    	if (!isDatabaseCurrent()) {
        	SQLiteDatabase db = openDatabase();
    		if (db != null) {
    			MyLog.d(TAG, "Upgrading the DB from version " + db.getVersion()
    					+ " to " + DatabaseContract.DATABASE_VERSION);
    			db.close();
    		}
    		// By calling this method an empty database will be created into the default
    		// application path and we'll be able to overwrite that database with other database.
	    	MyLog.d(TAG, "Forcing the Android to create the empty DB.");
        	db = helper.getReadableDatabase();
        	// Here is the fix applied for Android 2.2.1 (f.e. Desire Z/HD)
        	db.close();

        	try {
    			overwriteDatabaseWithDeployedOne();
    			return true;
    		} catch (IOException e) {
//        		throw new Error("Error during copying the database.");
        	} finally {
        		helper.close();
        	}
    		return false;
    	} else {
    		return true;
    	}
    }

    public boolean isDatabaseCurrent() {
    	SQLiteDatabase db = openDatabase();

    	MyLog.d(TAG, "Does the DB already exist? Result: " + (db != null ? true : false));

    	if (db != null && db.getVersion() == DatabaseContract.DATABASE_VERSION) {
    		db.close();
    		return true;
    	}
    	return false;
    }

    /**
     * Returns the {@link SQLiteDatabase} object if the DB exists. Otherwise, returns null.
     */
	private SQLiteDatabase openDatabase() {
		final String myPath = DB_PATH + DatabaseContract.DATABASE_NAME;
		final File dbFile = new File(myPath);

		if (!dbFile.exists()) {
			return null;
		}

    	SQLiteDatabase currentDB = null;
    	try{
    		currentDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    	} catch (SQLiteException e) {
    		// Database doesn't exist yet.
    	}
    	return currentDB;
    }

    /**
     * Copies your database from your local assets-folder to the recently created empty
     * database in the default application directory. From there it can be managed as always.
     * The whole operation is done by transferring the bytestream.
     * */
    private void overwriteDatabaseWithDeployedOne() throws IOException {
    	MyLog.d(TAG, "Overwriting the empty DB with the deployed along with the application one.");

    	final AssetManager assetManager = context.getAssets();

    	// Path to the recently created empty DB.
    	String outFileName = DB_PATH + DatabaseContract.DATABASE_NAME;

    	// Open the empty DB as the output stream.
    	OutputStream myOutput = new FileOutputStream(outFileName);
    	byte[] buffer = new byte[1024];
    	int length;

    	// Iterate through the DB chunks and append them to the output stream one after another.
    	String[] fileList = assetManager.list("");
    	Arrays.sort(fileList);
    	for (int i=0; i<MAX_CHUNK_COUNT; i++) {
	    	// Open the local-stored DB chunk as the input stream.
    		String fileName = String.format(DatabaseContract.DATABASE_NAME + ".%d", i);
    		if (Arrays.binarySearch(fileList, fileName) < 0) break;

    		InputStream myInput = assetManager.open(fileName);

    		// Transfer bytes from the input-stream to the output-stream.
    		while ((length = myInput.read(buffer))>0){
	    		myOutput.write(buffer, 0, length);
	    	}

    		// Close the input stream.
	    	myInput.close();
    	}

    	// Flush and close the output stream.
    	myOutput.flush();
    	myOutput.close();
    }

}
