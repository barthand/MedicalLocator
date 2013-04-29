package put.medicallocator.utils;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

public final class LocationManagerUtils {

    /**
     * Requests updates for the provided {@code listener} in the {@link LocationManager}, asking for updates from
     * specific {@code providers}.
     */
    public static void register(Context context, LocationListener listener, String[] providers) {
        final LocationManager locationManager =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        for (String provider : providers) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0,
                listener);
        }
    }

    /**
     * Unregisters provided {@code listener} from the {@link LocationManager}.
     */
    public static void unregister(Context context, LocationListener listener) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(listener);
    }

    private LocationManagerUtils() {
    }
}
