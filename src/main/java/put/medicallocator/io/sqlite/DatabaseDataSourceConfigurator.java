package put.medicallocator.io.sqlite;

import android.content.Context;
import put.medicallocator.io.helper.DataSourceConfigurator;
import put.medicallocator.io.sqlite.DatabaseFacilityDAO.DatabaseOpenHelper;

/**
 * {@link DataSourceConfigurator} implementation for SQLite DB stored in assets,
 * delivered with application.
 */
public class DatabaseDataSourceConfigurator extends DataSourceConfigurator {

    private final ApplicationDeliveredDatabaseHelper checker;

    public DatabaseDataSourceConfigurator(Context context) {
        super();
        this.checker = new ApplicationDeliveredDatabaseHelper(context, new DatabaseOpenHelper(context));
    }

    @Override
    public boolean isConfigured() {
        return checker.isDatabaseCurrent();
    }

    @Override
    public boolean firstInitialization() {
        return checker.ensureDatabaseIsCurrent();
    }

}
