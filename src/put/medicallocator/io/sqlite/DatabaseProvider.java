package put.medicallocator.io.sqlite;

import java.util.HashMap;
import java.util.Map;

import put.medicallocator.io.Facility;
import put.medicallocator.io.IFacilityProvider;
import put.medicallocator.io.sqlite.DatabaseContract.FacilityColumns;
import put.medicallocator.io.sqlite.DatabaseContract.FacilityColumns.FacilityColumnsParams;
import put.medicallocator.io.sqlite.DatabaseContract.Tables;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

/**
 * {@link DatabaseProvider} shall be used for querying, deleting, inserting the medical facilities
 * which are backed in SQLite database. This class implements {@link IFacilityProvider}.  
 */
public class DatabaseProvider implements IFacilityProvider {

	private static final String TAG = "DatabaseProvider";
	
	private DatabaseHelper dbHelper;
	
	public DatabaseProvider(Context context) {
		dbHelper = new DatabaseHelper(context);
	}

	public Cursor getFacilitiesWithinArea(Location upperLeftLocation,
			Location lowerDownLocation) throws Exception {
		final String selection = 
				FacilityColumns.LATITUDE + " < ? AND " +
				FacilityColumns.LONGITUDE + " > ? AND " +
				FacilityColumns.LATITUDE + " > ? AND " +
				FacilityColumns.LONGITUDE + " < ?";
		final String[] selectionArgs = new String[] { 
				Double.toString(upperLeftLocation.getLatitude()),
				Double.toString(upperLeftLocation.getLongitude()),
				Double.toString(lowerDownLocation.getLatitude()),
				Double.toString(lowerDownLocation.getLongitude())
		};

		final Cursor cursor = 
			queryDB(Tables.FACILITY, FacilityQuery.PROJECTION, selection, selectionArgs);
		
		/* Return the Cursor */
		return cursor;
	}
	
	public Cursor getFacilitiesWithinRadius(Location startLocation, int radius) throws Exception {
		/* 
		 * Due to lack of trigonometric function in SQLite side,
		 * this can't be implemented. 
		 */
		throw new Exception("Unsupported operation in this provider.");
	}

	public Cursor getFacilitiesWithinAddress(String address) {
		final String selection = "lower(" + FacilityColumns.ADDRESS +") LIKE ?";
		final String[] selectionArgs = new String[] { address };

		final Cursor cursor = 
			queryDB(Tables.FACILITY, FacilityQuery.PROJECTION, selection, selectionArgs);
		
		/* Return the Cursor */
		return cursor;
	}

	public Facility getFacility(Location location) {
		final String selection = 
				FacilityColumns.LATITUDE + " = ? AND " +
				FacilityColumns.LONGITUDE + " = ?";
		final String[] selectionArgs = new String[] { 
				Double.toString(location.getLatitude()),
				Double.toString(location.getLongitude()),
		};
		
		final Cursor cursor = 
			queryDB(Tables.FACILITY, FacilityQuery.PROJECTION, selection, selectionArgs);
		
		if (!cursor.moveToFirst()) return null;
		if (cursor.getCount() > 1) {
			Log.w(TAG, "Query returned " + cursor.getCount() + " rows, " +
					"although it was meant to return single row. Returning first row.");
		}
		
		final Map<String, Integer> columnMapping = createColumnMapping(FacilityQuery.PROJECTION);
		final Facility result = 
			TypeConverter.getFacilityFromCursorCurrentPosition(columnMapping, cursor);
		
		return result;
	}

	public boolean insertFacility(Facility facility) {
		return false;
	}

	public boolean removeFacility(Facility facility) {
		return false;
	}
	
	public static Map<String, Integer> createColumnMapping(String[] projection) {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		for (int i=0; i<projection.length; i++) {
			result.put(projection[i], i);
		}
		return result;
	}

	private Cursor queryDB(String table, String[] projection, 
			String selection, String[] selectionArgs) {
		Log.d(TAG, "Starting query -- " +
				"table[" + table + "], " +
				"projection[" + projection.toString() + "], " +
				"selection[" + selection + "], selectionArgs[" + selectionArgs.toString() + "]");
		
		/* Query the database */
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		final Cursor cursor = 
			db.query(table, projection, selection, selectionArgs, null, null, null); 
		
		/* Return the Cursor */
		return cursor;
	}
	
	private static class DatabaseHelper extends SQLiteOpenHelper {

		private static final String TAG = "DatabaseHelper";
		
		public DatabaseHelper(Context context) {
			super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + Tables.FACILITY + " ("
	                + FacilityColumns._ID + " " + FacilityColumnsParams.PARAM_ID + ","
	                + FacilityColumns.NAME + " " + FacilityColumnsParams.PARAM_NAME + ","
	                + FacilityColumns.ADDRESS + " " + FacilityColumnsParams.PARAM_ADDRESS + ","
	                + FacilityColumns.PHONE + " " + FacilityColumnsParams.PARAM_PHONE + ","
	                + FacilityColumns.EMAIL + " " + FacilityColumnsParams.PARAM_EMAIL + ","
	                + ")");			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	        Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

			if (oldVersion != DatabaseContract.DATABASE_VERSION) {
		        Log.d(TAG, "onUpgrade(): Dropping all tables!");
				db.execSQL("DROP TABLE IF EXISTS " + Tables.FACILITY);
				onCreate(db);
			}
		}
		
	}

	public interface FacilityQuery {
		String[] PROJECTION = new String[] {
				FacilityColumns._ID,
				FacilityColumns.NAME,
				FacilityColumns.ADDRESS,
				FacilityColumns.PHONE,
				FacilityColumns.EMAIL,
				FacilityColumns.LONGITUDE,
				FacilityColumns.LATITUDE
		};
	}

}
