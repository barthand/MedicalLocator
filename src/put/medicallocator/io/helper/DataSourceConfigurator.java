package put.medicallocator.io.helper;

import android.content.Context;
import put.medicallocator.io.IFacilityDAO;
import put.medicallocator.io.sqlite.DatabaseDataSourceConfigurator;

/**
 * Configurator class for data sources used by {@link IFacilityDAO} implementations,
 * ensuring that they're properly initialized and ready for serving data.
 */
public abstract class DataSourceConfigurator {

	protected DataSourceConfigurator() { }

    /**
     * Checks whether the initialization has been already done.
     */
	public abstract boolean isConfigured();

    /**
     * Performs the initialization itself.
     * Before it is executed, it should be preceded by {@link #isConfigured()} checks.
     */
	public abstract boolean firstInitialization();

    /**
     * Creates a default instance of {@link DataSourceConfigurator} (currently {@link DatabaseDataSourceConfigurator}).
     */
	public static DataSourceConfigurator getInstance(Context context) {
		return new DatabaseDataSourceConfigurator(context);
	}

}
