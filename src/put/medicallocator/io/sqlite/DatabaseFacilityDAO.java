package put.medicallocator.io.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.android.maps.GeoPoint;
import put.medicallocator.io.DAOException;
import put.medicallocator.io.IFacilityDAO;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.model.FacilityType;
import put.medicallocator.io.sqlite.DatabaseContract.FacilityColumns;
import put.medicallocator.io.sqlite.DatabaseContract.Queries;
import put.medicallocator.io.sqlite.DatabaseContract.Queries.FacilityQuery;
import put.medicallocator.io.sqlite.DatabaseContract.Tables;
import put.medicallocator.ui.async.model.SearchCriteria;
import put.medicallocator.utils.GeoUtils;
import put.medicallocator.utils.MyLog;
import put.medicallocator.utils.StringUtils;

import java.util.*;

/**
 * {@link DatabaseFacilityDAO} shall be used for querying, deleting, inserting the medical facilities
 * which are backed in SQLite database.
 */
public class DatabaseFacilityDAO implements IFacilityDAO {

    /* TODO: Some DB connections/cursors are not properly closed. Investigate. */

    private static final String TAG = "DatabaseProvider";

    private final DatabaseOpenHelper dbHelper;

    public DatabaseFacilityDAO(Context context) {
        dbHelper = new DatabaseOpenHelper(context);
    }

    @Override
    public List<Facility> findWithinArea(GeoPoint lowerLeft, GeoPoint upperRight) throws DAOException {
        return findWithinAreaUsingCriteria(lowerLeft, upperRight, null);
    }

    @Override
    public List<Facility> findWithinAreaUsingCriteria(GeoPoint lowerLeft, GeoPoint upperRight, SearchCriteria criteria) throws DAOException {
        final StringBuilder selection = new StringBuilder();
        final List<String> selectionArgs = new ArrayList<String>();

        selection.append(FacilityColumns.LATITUDE).append(" > ? AND ")
                .append(FacilityColumns.LONGITUDE).append(" > ? AND ")
                .append(FacilityColumns.LATITUDE).append(" < ? AND ")
                .append(FacilityColumns.LONGITUDE).append(" < ?");

        addCoordinatesAsArgs(lowerLeft, upperRight, selectionArgs);

        if (!StringUtils.isEmpty(criteria.getQuery())) {
            selection.append(" AND ").append(FacilityColumns.NAME).append(" LIKE ? ");
            selectionArgs.add('%' + criteria.getQuery() + '%');
        }

        if (criteria.getAllowedTypes() != null) {
            if (criteria.getAllowedTypes().size() > 0) {
                selection.append(" AND ").append(getSqlInClause(FacilityColumns.TYPE, criteria.getAllowedTypes()));
                selectionArgs.addAll(getSqlInArgs(extractIDs(criteria.getAllowedTypes())));
            } else {
                return new ArrayList<Facility>();
            }
        }

        Cursor cursor = null;
        try {
            final String[] args = selectionArgs.toArray(new String[selectionArgs.size()]);
            cursor = queryDB(Tables.FACILITY, FacilityQuery.PROJECTION, selection.toString(), args, null);
            return createResultList(cursor);
        } finally {
            safeCloseCursor(cursor);
        }
    }

    private void addCoordinatesAsArgs(GeoPoint lowerLeft, GeoPoint upperRight, final List<String> selectionArgs) {
        final double[] minCoords = GeoUtils.createLatLngArray(lowerLeft);
        final double[] maxCoords = GeoUtils.createLatLngArray(upperRight);

        final double minLatitude = minCoords[0];
        final double minLongitude = minCoords[1];
        final double maxLatitude = maxCoords[0];
        final double maxLongitude = maxCoords[1];

        selectionArgs.addAll(Arrays.asList(
                Double.toString(minLatitude),
                Double.toString(minLongitude),
                Double.toString(maxLatitude),
                Double.toString(maxLongitude)
        ));
    }

    @Override
    public List<Facility> findWithAddress(String address) {
        final String selection = FacilityColumns.ADDRESS + " LIKE ?";
        final String[] selectionArgs = new String[]{"%" + address + "%"};

        Cursor cursor = null;
        try {
            cursor = queryDB(0, Tables.FACILITY, FacilityQuery.PROJECTION, selection, selectionArgs, null);
            return createResultList(cursor);
        } finally {
            safeCloseCursor(cursor);
        }
    }

    @Override
    public List<Facility> findWithKeyword(String keyword) throws DAOException {
        final String selection =
                FacilityColumns.ADDRESS + " LIKE ? OR "
                        + FacilityColumns.NAME + " LIKE ?";
        final String[] selectionArgs = new String[]{
                "%" + keyword + "%",
                "%" + keyword + "%"
        };

        Cursor cursor = null;
        try {
            cursor = queryDB(0, Tables.FACILITY, FacilityQuery.PROJECTION, selection, selectionArgs, FacilityColumns.ADDRESS);
            return createResultList(cursor);
        } finally {
            safeCloseCursor(cursor);
        }

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

        return db.query(table, projection, selection, selectionArgs, null, null, orderBy);
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

    private static List<Integer> extractIDs(Collection<FacilityType> types) {
        final List<Integer> result = new ArrayList<Integer>();
        for (FacilityType type : types) {
            result.add(type.getId());
        }
        return result;
    }

    private static String getSqlLikeClause(String quailfiedField, Collection<?> values) {
        final StringBuilder builder = new StringBuilder();
        final Iterator<?> iterator = values.iterator();
        builder.append("(");
        while (iterator.hasNext()) {
            iterator.next();
            builder.append(quailfiedField).append(" LIKE ? ");
            if (iterator.hasNext()) {
                builder.append(" OR ");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    private static String getSqlInClause(String quailfiedField, Collection<?> values) {
        final StringBuilder builder = new StringBuilder();
        final Iterator<?> iterator = values.iterator();
        builder.append("(").append(quailfiedField).append(" IN (");
        while (iterator.hasNext()) {
            iterator.next();
            builder.append("?");
            if (iterator.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("))");
        return builder.toString();
    }

    private static List<String> getSqlInArgs(Collection<?> values) {
        final List<String> args = new ArrayList<String>();
        for (Object value : values) {
            args.add(value.toString());
        }
        return args;
    }

    private static void safeCloseCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
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
