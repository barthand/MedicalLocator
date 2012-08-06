package put.medicallocator.io.sqlite;

import put.medicallocator.io.model.Facility;
import put.medicallocator.utils.GeoUtils;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.google.android.maps.GeoPoint;

/**
 * Contract class to be used when interacting with {@link DatabaseFacilityDAO}.
 */
class DatabaseContract {

    protected static final String DATABASE_NAME = "locator.db";
    protected static final int DATABASE_VERSION = 2;

	interface Tables {
		String FACILITY = "facility";
	}

	/**
	 * Interface defining all columns which shall be available for the Facility.
	 * This information is especially valuable for implementations of {@link IFacilityProvider}.
	 */
	interface FacilityColumns extends BaseColumns {
		public static final String NAME = "name";
		public static final String ADDRESS = "address";
		public static final String PHONE = "phone";
		public static final String EMAIL = "email";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";
	}

	interface FacilityColumnsDefinitions {
		String PARAM_ID = "INTEGER PRIMARY KEY AUTOINCREMENT";
		String PARAM_NAME = "TEXT NOT NULL";
		String PARAM_ADDRESS = "TEXT NOT NULL";
		String PARAM_PHONE = "TEXT";
		String PARAM_EMAIL = "TEXT";
		String PARAM_LATITUDE = "REAL";
		String PARAM_LONGITUDE  = "REAL";
	}

	static class Queries {

		interface FacilityQuery {
			String[] PROJECTION = new String[] {
					FacilityColumns._ID,
					FacilityColumns.NAME,
					FacilityColumns.ADDRESS,
					FacilityColumns.PHONE,
					FacilityColumns.EMAIL,
					FacilityColumns.LATITUDE,
					FacilityColumns.LONGITUDE,
			};

			int _ID = 0;
			int NAME = 1;
			int ADDRESS = 2;
			int PHONE = 3;
			int EMAIL = 4;
			int LATITUDE = 5;
			int LONGITUDE = 6;
		}

		/**
		 * This function returns the Facility based on the data from {@code cursor}'s current position.
		 */
		public static Facility getFacility(Cursor cursor) {
			/* Read straight from the Cursor */
			final String name = cursor.getString(FacilityQuery.NAME);
			final String address = cursor.getString(FacilityQuery.ADDRESS);
			final String phone = cursor.getString(FacilityQuery.PHONE);
			final String email  = cursor.getString(FacilityQuery.EMAIL);
			final double latitude = cursor.getDouble(FacilityQuery.LATITUDE);
			final double longitude = cursor.getDouble(FacilityQuery.LONGITUDE);

			final GeoPoint geoPoint = GeoUtils.convertToGeoPoint(latitude, longitude);

			final Facility facility = new Facility();
			facility.setName(name);
			facility.setAddress(address);
			facility.setPhone(phone);
			facility.setEmail(email);
			facility.setGeoPoint(geoPoint);
			return facility;
		}

	}

	private DatabaseContract() { }
}
