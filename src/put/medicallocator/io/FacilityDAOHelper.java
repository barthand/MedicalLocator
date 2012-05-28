package put.medicallocator.io;

import put.medicallocator.io.sqlite.DatabaseFacilityDAOHelper;
import android.content.Context;

public abstract class FacilityDAOHelper {

	protected FacilityDAOHelper() { }

	public abstract boolean isDataPrepared();
	public abstract boolean prepareForFirstRun();

	public static FacilityDAOHelper getInstance(Context context) {
		return new DatabaseFacilityDAOHelper(context);
	}

}
