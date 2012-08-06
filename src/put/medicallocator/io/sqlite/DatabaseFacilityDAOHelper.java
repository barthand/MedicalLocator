package put.medicallocator.io.sqlite;

import put.medicallocator.io.helper.FacilityDAOHelper;
import put.medicallocator.io.sqlite.DatabaseFacilityDAO.DatabaseOpenHelper;
import android.content.Context;

public class DatabaseFacilityDAOHelper extends FacilityDAOHelper {

    private final DeployedDatabaseChecker checker;

    public DatabaseFacilityDAOHelper(Context context) {
        super();
        this.checker = new DeployedDatabaseChecker(context, new DatabaseOpenHelper(context));
    }

    @Override
    public boolean isDataPrepared() {
        return checker.isDatabaseCurrent();
    }

    @Override
    public boolean prepareForFirstRun() {
        return checker.ensureDatabaseIsCurrent();
    }

}
