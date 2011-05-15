package put.medicallocator.ui;

import put.medicallocator.R;
import put.medicallocator.utils.GeoUtils;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class ActivityMain extends MapActivity {
	/** Defines the start GeoPoint. Yeah, let's all do the Poznan! ;) */
	private static final GeoPoint START_GEOPOINT = GeoUtils.convertToGeoPoint(52.408396, 16.92838);
	/** Defines the start zoom level. */
	private static final int START_ZOOM_LEVEL = 14;
	
    private MapView mapView;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		mapView = (MapView) findViewById(R.id.map_view);
		mapView.setBuiltInZoomControls(true);
		final MapController mapController = mapView.getController();
		mapController.setCenter(START_GEOPOINT);
		mapController.setZoom(START_ZOOM_LEVEL);
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}