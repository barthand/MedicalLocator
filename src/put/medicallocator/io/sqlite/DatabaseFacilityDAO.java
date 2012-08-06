package put.medicallocator.io.sqlite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import put.medicallocator.io.DAOException;
import put.medicallocator.io.IFacilityDAO;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.sqlite.DatabaseContract.FacilityColumns;
import put.medicallocator.io.sqlite.DatabaseContract.Queries;
import put.medicallocator.io.sqlite.DatabaseContract.Queries.FacilityQuery;
import put.medicallocator.io.sqlite.DatabaseContract.Tables;
import put.medicallocator.utils.GeoUtils;
import put.medicallocator.utils.MyLog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.maps.GeoPoint;

/**
 * {@link DatabaseFacilityDAO} shall be used for querying, deleting, inserting the medical facilities
 * which are backed in SQLite database.
 */
public class DatabaseFacilityDAO implements IFacilityDAO {

    /* TODO: Some DB connections/cursors are not properly closed. Investigate. */

	private static final String TAG = "DatabaseProvider";

	private DatabaseOpenHelper dbHelper;

	public DatabaseFacilityDAO(Context context) {
		dbHelper = new DatabaseOpenHelper(context);
	}

	public List<Facility> findWithinArea(GeoPoint lowerLeft, GeoPoint upperRight) throws DAOException {
		return findNamedWithinArea(lowerLeft, upperRight, null);
	}

	public List<Facility> findNamedWithinArea(GeoPoint lowerLeft, GeoPoint upperRight, String[] names) throws DAOException {
		final StringBuilder selection = new StringBuilder();
		selection.append(FacilityColumns.LATITUDE).append(" > ? AND ")
			.append(FacilityColumns.LONGITUDE).append(" > ? AND ")
			.append(FacilityColumns.LATITUDE).append(" < ? AND ")
			.append(FacilityColumns.LONGITUDE).append(" < ?");

		if (names != null && names.length > 0) {
			selection.append(" AND ").append(getSQLLikeClause(FacilityColumns.NAME, names));
		}

		final double[] minCoords = GeoUtils.convertToDegrees(lowerLeft);
		final double[] maxCoords = GeoUtils.convertToDegrees(upperRight);

		final double minLatitude = minCoords[0];
		final double minLongitude = minCoords[1];
		final double maxLatitude = maxCoords[0];
		final double maxLongitude = maxCoords[1];

		final String[] selectionArgs = new String[] {
				Double.toString(minLatitude),
				Double.toString(minLongitude),
				Double.toString(maxLatitude),
				Double.toString(maxLongitude)
		};

		final Cursor cursor = queryDB(Tables.FACILITY, FacilityQuery.PROJECTION, selection.toString(), selectionArgs, null);
		return createResultList(cursor);
	}

	public List<Facility> findWithAddress(String address) {
		final String selection = FacilityColumns.ADDRESS + " LIKE ?";
		final String[] selectionArgs = new String[] { "%" + address + "%" };

		final Cursor cursor = queryDB(0, Tables.FACILITY, FacilityQuery.PROJECTION, selection, selectionArgs, null);

		return createResultList(cursor);
	}

	public List<Facility> findWithKeyword(String keyword) throws DAOException {
		final String selection =
				FacilityColumns.ADDRESS +" LIKE ? OR "
				+ FacilityColumns.NAME +" LIKE ?";
		final String[] selectionArgs = new String[] {
				"%" + keyword + "%",
				"%" + keyword + "%"
			};

		final Cursor cursor = queryDB(0, Tables.FACILITY, FacilityQuery.PROJECTION,
				selection, selectionArgs, FacilityColumns.ADDRESS);

		return createResultList(cursor);
	}

	private Cursor queryDB(final String table, final String[] projection,
			final String selection, final String[] selectionArgs, final String orderBy) {
		return queryDB(0, table, projection, selection, selectionArgs, orderBy);
	}

	private Cursor queryDB(final int token, final String table, final String[] projection,
			final String selection, final String[] selectionArgs, final String orderBy) {
		MyLog.d(TAG, "Starting query -- " +
				"table[" + table + "], " +
				"projection[" + Arrays.toString(projection) + "], " +
				"selection[" + selection + "], selectionArgs[" + Arrays.toString(selectionArgs) + "]");

		/* Query the database */
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		final Cursor cursor = db.query(table, projection, selection, selectionArgs, null, null, orderBy);

		return cursor;
	}

	private static List<Facility> createResultList(final Cursor cursor) {
		final List<Facility> result = new ArrayList<Facility>();
		if (cursor.moveToFirst()) {
			do {
				result.add(Queries.getFacility(cursor));
			} while (cursor.moveToNext());
		}
		cursor.close();
		return result;
	}

	private static String getSQLLikeClause(String quailfiedField, String[] values) {
		StringBuilder builder = new StringBuilder();
		final int count = values.length;
		builder.append("(");
		for (int i=0; i<count-1; i++) {
			builder.append(quailfiedField).append(" LIKE '% ").append(values[i]).append(" %' OR ");
		}
		builder.append(quailfiedField).append(" LIKE '% ").append(values[count-1]).append(" %')");
		return builder.toString();
	}

	/**
	 * Internal helper class to manage database connections.
	 */
	static class DatabaseOpenHelper extends SQLiteOpenHelper {

		public DatabaseOpenHelper(Context context) {
			super(context, DatabaseContract.DATABASE_NAME, null, DatabaseContract.DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			/* Due to copying the DB from the local assets/ folder, this is commented out. */
			/*
			MyLog.d(TAG, "Executing onCreate()");
			db.execSQL("CREATE TABLE " + Tables.FACILITY + " ("
	                + FacilityColumns._ID + " " + FacilityColumnsParams.PARAM_ID + ","
	                + FacilityColumns.NAME + " " + FacilityColumnsParams.PARAM_NAME + ","
	                + FacilityColumns.ADDRESS + " " + FacilityColumnsParams.PARAM_ADDRESS + ","
	                + FacilityColumns.PHONE + " " + FacilityColumnsParams.PARAM_PHONE + ","
	                + FacilityColumns.EMAIL + " " + FacilityColumnsParams.PARAM_EMAIL + ","
	                + FacilityColumns.LATITUDE + " " + FacilityColumnsParams.PARAM_LATITUDE + ","
	                + FacilityColumns.LONGITUDE + " " + FacilityColumnsParams.PARAM_LONGITUDE + ","
	                + ")");	*/
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/* Due to copying the DB from the local assets/ folder, this is commented out. */
			/*
			MyLog.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
			if (oldVersion != DatabaseContract.DATABASE_VERSION) {
		        Log.d(TAG, "onUpgrade(): Dropping all tables!");
				db.execSQL("DROP TABLE IF EXISTS " + Tables.FACILITY);
				onCreate(db);
			}*/
		}

	}

}
