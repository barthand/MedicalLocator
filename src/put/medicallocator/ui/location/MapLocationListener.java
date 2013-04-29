package put.medicallocator.ui.location;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import put.medicallocator.ui.utils.State;
import put.medicallocator.utils.GeoUtils;
import put.medicallocator.utils.MyLog;

/**
 * {@link LocationListener} implementation associated with the {@link MapView} and application {@link State}.
 */
public class MapLocationListener implements LocationListener {

    private static final String TAG = "MapLocationListener";

    private final MapView mapView;
    private final State state;

    private Location lastKnownLocation;
    
    public MapLocationListener(MapView mapView, State state) {
        this.mapView = mapView;
        this.state = state;
    }

    @Override
    public void onLocationChanged(Location location) {
        MyLog.d(TAG, "Received the Location update from " + location.getProvider() + ": "
                + location.getLatitude() + "; " + location.getLongitude());

        final GeoPoint currentPoint = GeoUtils.createGeoPoint(location.getLatitude(), location.getLongitude());
        this.lastKnownLocation = location;

        if (mapView != null && state.isTrackingEnabled) {
            final String provider = location.getProvider();

            if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
                // Use the NETWORK_PROVIDER only if GPS is not enabled.
                if (state.isGPSEnabled)
                    return;
            } else if (LocationManager.GPS_PROVIDER.equals(provider)) {
                state.isGPSEnabled = true;
            }

            state.currentPoint = currentPoint;

            // TODO: It should die together with the Activity, we need to ensure that this listener is deregistered upon finish.
            mapView.post(new Runnable() {
                @Override
                public void run() {
                    final MapController mapController = mapView.getController();
                    mapController.animateTo(state.currentPoint);
                }
            });
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider))
            state.isGPSEnabled = false;
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (LocationManager.GPS_PROVIDER.equals(provider))
            state.isGPSEnabled = true;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // No need to implement this here.
    }

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

}
