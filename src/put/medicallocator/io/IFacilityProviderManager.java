package put.medicallocator.io;

import android.content.Context;
import put.medicallocator.io.sqlite.DatabaseProvider;

public class IFacilityProviderManager {
	private static IFacilityProvider instance;
	
	public synchronized static IFacilityProvider getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseProvider(context);
		}
		return instance;
	}
	
	private IFacilityProviderManager() {
		
	}

}
