package put.medicallocator.ui;

import java.util.List;
import java.util.Map;

import put.medicallocator.R;
import put.medicallocator.io.Facility;
import put.medicallocator.io.IFacilityProvider;
import put.medicallocator.io.IFacilityProvider.AsyncQueryListener;
import put.medicallocator.io.IFacilityProviderManager;
import put.medicallocator.ui.overlay.BasicItemizedOverlay;
import put.medicallocator.ui.overlay.FacilityOverlayItem;
import put.medicallocator.utils.GeoUtils;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class ActivityMain extends MapActivity implements AsyncQueryListener {

	// TODO: Use the savedInstanceBundle to check if Map, etc. was initialized
	
	private static final String TAG = ActivityMain.class.getName(); 
	
	/** Defines the start GeoPoint. Yeah, let's all do the Poznan! ;) */
	private static final GeoPoint START_GEOPOINT = GeoUtils.convertToGeoPoint(52.408396, 16.92838);
	/** Defines the start zoom level. */
	private static final int START_ZOOM_LEVEL = 14;
	
    private MapView mapView;
    private GeoPoint lastVisibleGeoPoint;
    private int noChangeCounter = 0;
    
    private Handler handler;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		/* Initialize a handler here to ensure that it is attached to the UI thread */
		handler = new Handler();

        /* Initialize the IFacilityProvider */
		if (!IFacilityProviderManager.isInitialized()) {
			Toast.makeText(this, 
				getResources().getString(R.string.activitymain_initializing_provider), 
				Toast.LENGTH_SHORT).show();
			IFacilityProviderManager.getInstance(this);
		}
        
        /* Set the basic attributes of the MapView */
		mapView = (MapView) findViewById(R.id.map_view);
		mapView.setBuiltInZoomControls(true);
		
		final MapController mapController = mapView.getController();
		mapController.setCenter(START_GEOPOINT);
		mapController.setZoom(START_ZOOM_LEVEL);
	
		/* Post query job */
		handler.post(doQueryIfRequired);
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private Runnable doQueryIfRequired = new Runnable() {
		public void run() {
			/* Save the exact coordinates of the top-left visible vertex of MapView */
			final GeoPoint currentVisibleGeoPoint = mapView.getMapCenter();
			final int currentLatitude = currentVisibleGeoPoint.getLatitudeE6();
			final int currentLongitude = currentVisibleGeoPoint.getLongitudeE6();

			if (lastVisibleGeoPoint == null) {
				noChangeCounter = 0;
				lastVisibleGeoPoint = currentVisibleGeoPoint;
			} else {
				final int lastLatitude = lastVisibleGeoPoint.getLatitudeE6();
				final int lastLongitude = lastVisibleGeoPoint.getLongitudeE6();
				
				if (lastLatitude == currentLatitude && lastLongitude == currentLongitude) {
					noChangeCounter++;
				} else {
					noChangeCounter = 0;
					lastVisibleGeoPoint = currentVisibleGeoPoint;
				}
			}
			
			if (noChangeCounter == 2) {
				IFacilityProvider provider = IFacilityProviderManager.getInstance(ActivityMain.this);
				
				// TODO: Make this distance dependent on the current zooming.
				final int distanceInKilometers = 10;
				final GeoPoint[] boundingGeoPoints = 
					GeoUtils.boundingCoordinates(distanceInKilometers, currentVisibleGeoPoint);
				final GeoPoint minGeoPoint = boundingGeoPoints[0];
				final GeoPoint maxGeoPoint = boundingGeoPoints[1];
				
				final double minLatitude = GeoUtils.convertToDegrees(minGeoPoint)[0];
				final double minLongitude = GeoUtils.convertToDegrees(minGeoPoint)[1];
				final double maxLatitude = GeoUtils.convertToDegrees(maxGeoPoint)[0];
				final double maxLongitude = GeoUtils.convertToDegrees(maxGeoPoint)[1];
				
				final Location lowerLeftLocation = new Location("");
				final Location upperRightLocation = new Location("");
				lowerLeftLocation.setLatitude(minLatitude);
				lowerLeftLocation.setLongitude(minLongitude);
				upperRightLocation.setLatitude(maxLatitude);
				upperRightLocation.setLongitude(maxLongitude);
				
				try {
					provider.getFacilitiesWithinArea(
							ActivityMain.this, 
							lowerLeftLocation, upperRightLocation);
				} catch (Exception e) {
					// It shouldn't happen - even if the query just won't be executed.
				}
			}
			
			/* Post this job once again after 1000ms */
			handler.postDelayed(doQueryIfRequired, 1000);
		}
	};



	public void onQueryComplete(int token, Cursor cursor,
			Map<String, Integer> columnMapping) {
		Log.e(TAG, "Query finished. Returned rows: " + cursor.getCount());
		if (!cursor.moveToFirst()) {
			// Empty resultset.
			return;
		}
		
		final Drawable marker = getResources().getDrawable(android.R.drawable.btn_star);
		final BasicItemizedOverlay itemizedOverlay = new BasicItemizedOverlay(this, marker);

		synchronized (ActivityMain.class) {
			Log.d(TAG, "Starting going through the results - creating the overlays");
			final double start = System.currentTimeMillis();
			do {
				final String name = cursor.getString(columnMapping.get(Facility.Columns.NAME));
				final String address = cursor.getString(columnMapping.get(Facility.Columns.ADDRESS));
				final String phone = cursor.getString(columnMapping.get(Facility.Columns.PHONE));
				final String email  = cursor.getString(columnMapping.get(Facility.Columns.EMAIL));
				final double latitude = cursor.getDouble(columnMapping.get(Facility.Columns.LATITUDE));
				final double longitude = cursor.getDouble(columnMapping.get(Facility.Columns.LONGITUDE));
				final GeoPoint geoPoint = GeoUtils.convertToGeoPoint(latitude, longitude);
				
				final Facility facility = new Facility();
				facility.setName(name);
				facility.setAddress(address);
				facility.setPhone(phone);
				facility.setEmail(email);
				facility.setLocation(geoPoint);
				final FacilityOverlayItem overlayItem = new FacilityOverlayItem(facility, geoPoint);
				itemizedOverlay.addOverlay(overlayItem);
			} while (cursor.moveToNext());
			Log.d(TAG, "Overlays created. It took: " + (System.currentTimeMillis() - start) + " ms");
		}
		
		// TODO: Find a less hacky way to avoid performance problem.
		itemizedOverlay.populateNow();
		final List<Overlay> overlays = mapView.getOverlays();
		overlays.clear();
		overlays.add(itemizedOverlay);
		Log.d(TAG, "Posting invalidate on MapView");
		mapView.postInvalidate();

/*		runOnUiThread(new Runnable() {
			
			public void run() {
				Log.d(TAG, "Invalidating MapView");
				final List<Overlay> overlays = mapView.getOverlays();
				overlays.clear();
				overlays.add(itemizedOverlay);
				mapView.invalidate();
			}
		});*/
		cursor.close();
	}}