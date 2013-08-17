package put.medicallocator.io.sqlite;

import android.database.Cursor;
import android.provider.BaseColumns;
import com.google.android.maps.GeoPoint;
import put.medicallocator.io.IFacilityDAO;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.model.FacilityType;
import put.medicallocator.utils.GeoUtils;

/**
 * Contract class to be used when interacting with {@link DatabaseFacilityDAO}.
 */
class DatabaseContract {

    protected static final String DATABASE_NAME = "locator.db";

    /**
     * Versions History:
     * <ull>
     * <li><b>ver. 1</b> - first issue</li>
     * <li><b>ver. 2</b> - Android 2.2.1 fix applied</li>
     * <li><b>ver. 3</b> - introducing TYPE column, separated HOMEPAGE and EMAIL. New data set.</li>
     * </ul> 
     */
    protected static final int DATABASE_VERSION = 3;

    /**
     * Stores names of all the tables.
     */
    interface Tables {
        String FACILITY = "facility";
    }

    /**
     * Interface defining all columns which shall be available for the Facility.
     * This information is especially valuable for implementations of {@link IFacilityDAO}.
     */
    interface FacilityColumns extends BaseColumns {
        public static final String NAME = "name";
        public static final String ADDRESS = "address";
        public static final String PHONE = "phone";
        public static final String EMAIL = "email";
        public static final String HOMEPAGE = "www";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String TYPE = "type";
    }

    /**
     * Contains the queries.
     */
    static class Queries {

        interface StandardCheckRawQuery {
            String SQL = "SELECT COUNT(*) FROM " + Tables.FACILITY;
            long EXPECTED_RESULT_GT = 15000;
            int COUNT = 0;
        }

        interface FacilityQuery {
            String[] PROJECTION = new String[]{
                    FacilityColumns._ID,
                    FacilityColumns.NAME,
                    FacilityColumns.ADDRESS,
                    FacilityColumns.PHONE,
                    FacilityColumns.EMAIL,
                    FacilityColumns.HOMEPAGE,
                    FacilityColumns.LATITUDE,
                    FacilityColumns.LONGITUDE,
                    FacilityColumns.TYPE
            };

            int _ID = 0;
            int NAME = 1;
            int ADDRESS = 2;
            int PHONE = 3;
            int EMAIL = 4;
            int HOMEPAGE = 5;
            int LATITUDE = 6;
            int LONGITUDE = 7;
            int TYPE = 8;
        }

        /**
         * This function returns the Facility based on the data from {@code cursor}'s current position.
         */
        public static Facility getFacility(Cursor cursor) {
            /* Read straight from the Cursor */
            final Long id = cursor.getLong(FacilityQuery._ID);
            final String name = cursor.getString(FacilityQuery.NAME);
            final String address = cursor.getString(FacilityQuery.ADDRESS);
            final String phone = cursor.getString(FacilityQuery.PHONE);
            final String email = cursor.getString(FacilityQuery.EMAIL);
            final String homepage = cursor.getString(FacilityQuery.HOMEPAGE);
            final double latitude = cursor.getDouble(FacilityQuery.LATITUDE);
            final double longitude = cursor.getDouble(FacilityQuery.LONGITUDE);
            final int type = cursor.getInt(FacilityQuery.TYPE);

            final GeoPoint geoPoint = GeoUtils.createGeoPoint(latitude, longitude);

            final Facility facility = new Facility();
            facility.setId(id);
            facility.setName(name);
            facility.setAddress(address);
            facility.setPhone(phone);
            facility.setEmail(email);
            facility.setHomepage(homepage);
            facility.setLocation(geoPoint);
            facility.setFacilityType(FacilityType.getById(type));
            return facility;
        }

    }

    private DatabaseContract() {
    }
}
