package put.medicallocator.io.sqlite;

import android.provider.BaseColumns;

/**
 * Contract class to be used when interacting with {@link DatabaseProvider}.
 */
public class DatabaseContract {

    protected static final String DATABASE_NAME = "locator.db";
    protected static final int DATABASE_VERSION = 1;
    
	interface Tables {
		String FACILITY = "facility";
	}
	
	interface FacilityColumns extends BaseColumns {
		String NAME = "name";
		String ADDRESS = "address";
		String PHONE = "phone";
		String EMAIL = "email";
		String LONGITUDE = "longitude";
		String LATITUDE = "latitude";
		
		interface FacilityColumnsParams {
			String PARAM_ID = "INTEGER PRIMARY KEY AUTOINCREMENT";
			String PARAM_NAME = "TEXT NOT NULL";
			String PARAM_ADDRESS = "TEXT NOT NULL";
			String PARAM_PHONE = "TEXT";
			String PARAM_EMAIL = "TEXT";
			String PARAM_LONGITUDE  = "REAL";
			String PARAM_LATITUDE = "REAL";
		}
	}
	
	private DatabaseContract() {
	}
}
