package put.medicallocator.ui;

import java.util.Arrays;
import java.util.List;

import put.medicallocator.R;
import put.medicallocator.io.IFacilityDAO;
import put.medicallocator.io.helper.FacilityDAOHelper;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.route.Route;
import put.medicallocator.io.sqlite.DatabaseFacilityDAO;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler.FacilityQueryExecutor;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler.FacilityQueryListener;
import put.medicallocator.ui.async.DAOInitializerAsyncTask;
import put.medicallocator.ui.async.DAOInitializerAsyncTask.DAOInitializerListener;
import put.medicallocator.ui.overlay.FacilitiesOverlay;
import put.medicallocator.ui.overlay.FaciltiesOverlayBuilder;
import put.medicallocator.ui.overlay.RouteOverlay;
import put.medicallocator.ui.overlay.utils.FacilityTapListener;
import put.medicallocator.ui.utils.FacilityDialogUtils;
import put.medicallocator.utils.GeoUtils;
import put.medicallocator.utils.MyLog;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class ActivityMain extends MapActivity implements DAOInitializerListener, FacilityQueryListener, FacilityTapListener {

	private static final String TAG = ActivityMain.class.getName();

	private static final int DIALOG_INITIALIZE_DAO = 1;

	private static final long TIME_WITHOUT_DESCRIPTOR_CHANGE_TO_QUERY = 500;

	/** Defines the start GeoPoint. Yeah, let's all do the Poznan! ;) */
	private static final GeoPoint START_GEOPOINT = GeoUtils.convertToGeoPoint(52.408396, 16.92838);

	/** Defines the start zoom level. */
	private static final int START_ZOOM_LEVEL = 14;

	private int currentDistanceInKilometers = ActivityFilter.DEFAULT_DISTANCE_IN_KILOMETERS;

	/* UI related */
    private MapView mapView;
    private MyLocationOverlay locationOverlay;
    private RouteOverlay routeOverlay;

    private LocationListener myLocationListener;

    private GeoPoint lastMapCenterGeoPoint;
    private long lastDescriptorChangeTimestamp;
    private boolean queryCompleted = false;

    private Handler handler;
    private RouteHandler routeHandler;
    private State state;

    /* DAO layer */
    private FacilityDAOHelper daoHelper;
    private IFacilityDAO facilityDao;
    private AsyncFacilityWorkerHandler facilityWorker;

	private Runnable doQueryIfRequired = new Runnable() {
		public void run() {
			requery();
		}
	};

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyLog.d(TAG, "onCreate @ " + this.getClass().getSimpleName());

        daoHelper = FacilityDAOHelper.getInstance(getApplicationContext());
        facilityDao = new DatabaseFacilityDAO(getApplicationContext());
        facilityWorker = new AsyncFacilityWorkerHandler(this);

		/* Do we have previous instance data? */
        if (getLastNonConfigurationInstance() instanceof State) {
        	state = (State) getLastNonConfigurationInstance();
        } else {
			state = new State();
		}

    	final DAOInitializerAsyncTask retrievedAsyncTask = state.daoInitializerAsyncTask;
        if (retrievedAsyncTask != null) {
        	if (retrievedAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            	retrievedAsyncTask.setListener(this);
        	}
        } else {
    		if (!daoHelper.isDataPrepared()) {
    			final DAOInitializerAsyncTask daoInitializerAsyncTask = new DAOInitializerAsyncTask(daoHelper, this);
    			state.daoInitializerAsyncTask = daoInitializerAsyncTask;

    			showDialog(DIALOG_INITIALIZE_DAO);

    			daoInitializerAsyncTask.execute();
    		}
        }

        setContentView(R.layout.activity_main);

        /* Set the basic attributes of the MapView */
        this.mapView = (MapView) findViewById(R.id.map_view);
        initializeMapView();

        locationOverlay = new MyLocationOverlay(this, mapView);
        myLocationListener = new MyLocationListener();
        routeOverlay = state.routeOverlay;

        /* Initialize a handlers here to ensure that they're attached to the UI thread */
        handler = new Handler();
        routeHandler = new RouteHandler();
    }

	private void initializeMapView() {
		mapView.setBuiltInZoomControls(true);

		final MapController mapController = mapView.getController();
		mapController.setCenter(state.currentPoint);
		mapController.setZoom(state.zoomLevel);
	}

    @Override
    protected void onResume() {
    	super.onResume();

        MyLog.d(TAG, "onResume @ " + this.getClass().getSimpleName());

		/* Register for the Location updates */
        registerLocationListener();

        /* Enable the MyLocationOverlay as well */
        locationOverlay.enableMyLocation();
        locationOverlay.runOnFirstFix(onFirstFixRunnable);
		mapView.getOverlays().add(locationOverlay);
		if (routeOverlay != null) {
			mapView.getOverlays().add(routeOverlay);
		}

		if (state.daoInitializerAsyncTask == null) {
			/* Post query job */
			makeFirstQuery();
		}
	}

	private void makeFirstQuery() {
		lastDescriptorChangeTimestamp = System.currentTimeMillis();
		handler.post(doQueryIfRequired);
	}

	@Override
    protected void onPause() {
    	super.onPause();

        MyLog.d(TAG, "onPause @ " + this.getClass().getSimpleName());

    	/* Unregister for the Location updates */
    	final LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	locationManager.removeUpdates(myLocationListener);

        /* Disable the MyLocationOverlay as well */
    	locationOverlay.disableMyLocation();

    	/* Disable querying */
    	handler.removeCallbacks(doQueryIfRequired);
    }

	@Override
	protected void onDestroy() {
        MyLog.d(TAG, "onDestroy @ " + this.getClass().getSimpleName());

	    facilityWorker.onDestroy();
		super.onDestroy();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_INITIALIZE_DAO:
				final ProgressDialog dialog = new ProgressDialog(this);
				dialog.setMessage(getString(R.string.activitymain_initializing_provider));
				return dialog;
		}
		return super.onCreateDialog(id);
	}

    @Override
    public Object onRetainNonConfigurationInstance() {
    	state.currentPoint = mapView.getMapCenter();
    	state.zoomLevel = mapView.getZoomLevel();
    	state.routeOverlay = routeOverlay;
    	return state;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	final MenuInflater menuInflater = getMenuInflater();
    	menuInflater.inflate(R.menu.activity_main_menu, menu);
    	return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	final MenuItem trackingItem = menu.findItem(R.id.tracking_menuitem);
    	if (state.isTrackingEnabled) {
    		trackingItem.setTitle(getResources().getString(R.string.activitymain_disabletracking));
    	} else {
    		trackingItem.setTitle(getResources().getString(R.string.activitymain_enabletracking));
    	}
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    		case R.id.filter_menuitem:
    			openFilterOptions();
    			return true;
    		case R.id.tracking_menuitem:
    			state.isTrackingEnabled = !state.isTrackingEnabled;
    			return true;
    		case R.id.search_menuitem:
    			onSearchRequested();
    			return true;
    		case R.id.about_menuitem:
    			showAbout();
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }

	public void onFacilityTap(Facility facility) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final FacilityDialogUtils dialogUtils = new FacilityDialogUtils(this, facility, inflater);
        final AlertDialog dialog = dialogUtils.createFacilityDialog(routeHandler);
        dialog.show();
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ActivityFilter.FILTER_REQUEST_CODE:
				if (resultCode == Activity.RESULT_OK) {
					setFilters(data);
				}
			default:
				break;
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	private void requery() {
		/* Save the exact coordinates of the top-left visible vertex of MapView */
		final GeoPoint currentVisibleGeoPoint = mapView.getMapCenter();
		final int currentLatitude = currentVisibleGeoPoint.getLatitudeE6();
		final int currentLongitude = currentVisibleGeoPoint.getLongitudeE6();

		boolean scheduleQuery = false;

		if (lastMapCenterGeoPoint == null) {
			lastMapCenterGeoPoint = currentVisibleGeoPoint;
		} else {
			final int lastLatitude = lastMapCenterGeoPoint.getLatitudeE6();
			final int lastLongitude = lastMapCenterGeoPoint.getLongitudeE6();

			if (lastLatitude == currentLatitude && lastLongitude == currentLongitude) {
				if (lastDescriptorChangeTimestamp > TIME_WITHOUT_DESCRIPTOR_CHANGE_TO_QUERY) {
					scheduleQuery = !queryCompleted;
				}
			} else {
				lastDescriptorChangeTimestamp = System.currentTimeMillis();
				lastMapCenterGeoPoint = currentVisibleGeoPoint;
				queryCompleted = false;
			}
		}

		if (scheduleQuery) {
			// TODO: Make the distance dependent on the current zoom value.
			final GeoPoint[] boundingGeoPoints =
					GeoUtils.boundingCoordinates(
						currentDistanceInKilometers, currentVisibleGeoPoint);
			final GeoPoint lowerLeft = boundingGeoPoints[0];
			final GeoPoint upperRight = boundingGeoPoints[1];

			facilityWorker.scheduleQuery(new FacilityQueryExecutor() {

				public List<Facility> execute() throws Exception {
					return facilityDao.findNamedWithinArea(lowerLeft, upperRight, state.filters);
				}
			});
		}

		/* Post this job once again after 1000ms */
		handler.postDelayed(doQueryIfRequired, 1000);
	}

	private Runnable onFirstFixRunnable = new Runnable() {

		public void run() {
			runOnUiThread(new Runnable() {

				public void run() {
					final MapController mapController = mapView.getController();
					mapController.animateTo(locationOverlay.getMyLocation());
				}
			});
		}
	};

	private void registerLocationListener() {
		final LocationManager locationManager =
	    	(LocationManager) getSystemService(Context.LOCATION_SERVICE);

	    locationManager.requestLocationUpdates(
	        LocationManager.GPS_PROVIDER,
	        0,
	        0,
	        myLocationListener);

	    locationManager.requestLocationUpdates(
	        LocationManager.NETWORK_PROVIDER,
	        0,
	        0,
	        myLocationListener);
	}

	private void setFilters(Intent intent) {
		currentDistanceInKilometers = intent.getIntExtra(
				ActivityFilter.RESULT_DISTANCE,
				ActivityFilter.DEFAULT_DISTANCE_IN_KILOMETERS);
		state.filters = intent.getStringArrayExtra(ActivityFilter.RESULT_FILTER_ARRAY);

		MyLog.d(TAG, "Received filters: [ " + currentDistanceInKilometers + " km], "
				+ Arrays.toString(state.filters));
	}

	private void openFilterOptions() {
		final Intent intent = new Intent(this, ActivityFilter.class);
		intent.putExtra(ActivityFilter.INPUT_FILTER_ARRAY, state.filters);
		intent.putExtra(ActivityFilter.INPUT_DISTANCE, currentDistanceInKilometers);
		startActivityForResult(intent, ActivityFilter.FILTER_REQUEST_CODE);
	}

	private void showAbout() {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.dialog_about, null);

		try {
			String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			String version = String.format(getString(R.string.app_version), versionName);
			final TextView versionTextView = (TextView) layout.findViewById(R.id.dialogabout_appversion);
			versionTextView.setText(version);
		} catch (NameNotFoundException e) {
			// No version found, not critical..
		}

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(layout);
		final AlertDialog dialog = builder.create();
		dialog.setTitle(getString(R.string.app_name));
		dialog.show();
	}

	private static class State {
		public GeoPoint currentPoint;
		public int zoomLevel;
		public boolean isTrackingEnabled = false;
		public boolean isGPSEnabled = false;
	    public String[] filters;
	    public RouteOverlay routeOverlay;
	    public DAOInitializerAsyncTask daoInitializerAsyncTask;

		private State() {
			currentPoint = START_GEOPOINT;
			zoomLevel = START_ZOOM_LEVEL;
		}
	}

	private class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
			MyLog.d(TAG, "Received the Location update from " + location.getProvider() + ": " +
					location.getLatitude() + "; " + location.getLongitude());
			final GeoPoint currentPoint = GeoUtils.convertToGeoPoint(
					location.getLatitude(), location.getLongitude());
			routeHandler.setCurrentLocation(currentPoint);

			if (mapView != null && state.isTrackingEnabled) {
				final String provider = location.getProvider();

				if (LocationManager.NETWORK_PROVIDER.equals(provider)) {
					// Use the NETWORK_PROVIDER only if GPS is not enabled.
					if (state.isGPSEnabled) return;
				} else if (LocationManager.GPS_PROVIDER.equals(provider)) {
					state.isGPSEnabled = true;
				}

				state.currentPoint = currentPoint;

				runOnUiThread(new Runnable() {
					public void run() {
						final MapController mapController = mapView.getController();
						mapController.animateTo(state.currentPoint);
					}
				});
			}
		}

		public void onProviderDisabled(String provider) {
			if (LocationManager.GPS_PROVIDER.equals(provider))
				state.isGPSEnabled = false;
		}

		public void onProviderEnabled(String provider) {
			if (LocationManager.GPS_PROVIDER.equals(provider))
				state.isGPSEnabled = true;
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// No need to implement this here.
		}
	}

	public class RouteHandler extends Handler {
		private Route route;
		private GeoPoint currentLocation;

		public void setRoute(Route route) {
			this.route = route;
		}

		public void setCurrentLocation(GeoPoint location) {
			currentLocation = location;
		}

		public GeoPoint getCurrentLocation() {
			return currentLocation;
		}

		@Override
		public void handleMessage(Message msg) {
            List<Overlay> listOfOverlays = mapView.getOverlays();
            listOfOverlays.remove(routeOverlay);
            routeOverlay = new RouteOverlay(route, mapView);
            listOfOverlays.add(routeOverlay);
            mapView.invalidate();
		}

	}

	public void onDatabaseIniitialized(boolean success) {
	    state.daoInitializerAsyncTask = null;
		if (success) {
			dismissDialog(DIALOG_INITIALIZE_DAO);
			makeFirstQuery();
		} else {
			// Not good, we encountered a real FATAL error..
			// TODO: Push information to the user.
		}
	}

    public void onAsyncFacilityQueryCompleted(List<Facility> result) {
        MyLog.e(TAG, "Query finished. Returned rows: " + result.size());
        queryCompleted = true;

        if (result.size() == 0) {
            MyLog.d(TAG, "Removing overlays from the MapView");
            final List<Overlay> overlays = mapView.getOverlays();
            overlays.clear();
            overlays.add(locationOverlay);
        } else {
            final Drawable marker = getResources().getDrawable(R.drawable.marker);
            final FacilitiesOverlay overlay = new FaciltiesOverlayBuilder(result, marker).buildOverlay(this);

            final List<Overlay> overlays = mapView.getOverlays();
            overlays.clear();

            overlays.add(overlay);
            overlays.add(locationOverlay);
            if (routeOverlay != null) {
                overlays.add(routeOverlay);
            }
        }

        MyLog.d(TAG, "Posting invalidate on MapView");
        mapView.invalidate();
    }

}