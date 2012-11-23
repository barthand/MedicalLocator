package put.medicallocator.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import put.medicallocator.R;
import put.medicallocator.io.helper.FacilityDAOHelper;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.model.FacilityType;
import put.medicallocator.io.route.Route;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler.FacilityQueryListener;
import put.medicallocator.ui.async.DataSourceInitializerAsyncTask;
import put.medicallocator.ui.async.DataSourceInitializerAsyncTask.DataSourceInitializerListener;
import put.medicallocator.ui.async.QueryManager;
import put.medicallocator.ui.location.MapLocationListener;
import put.medicallocator.ui.misc.FacilityTypeInflateStrategy;
import put.medicallocator.ui.misc.LandscapeFacilityTypeInflateStrategy;
import put.medicallocator.ui.misc.PortraitFacilityTypeInflateStrategy;
import put.medicallocator.ui.overlay.FacilitiesOverlay;
import put.medicallocator.ui.overlay.FaciltiesOverlayBuilder;
import put.medicallocator.ui.overlay.RouteOverlay;
import put.medicallocator.ui.overlay.utils.FacilityTapListener;
import put.medicallocator.ui.utils.FacilityDialogUtils;
import put.medicallocator.ui.utils.State;
import put.medicallocator.utils.MyLog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class ActivityMain extends MapActivity implements DataSourceInitializerListener, FacilityQueryListener, FacilityTapListener {

	private static final String TAG = ActivityMain.class.getName();

	/* IDs of the Dialogs */
	private static final int ID_DIALOG_INITIALIZE_DAO = 1;

    private State state;

	/* UI related */
    private MapView mapView;
    private SlidingDrawer slidingDrawer;
    private MyLocationOverlay locationOverlay;
    private RouteOverlay routeOverlay;
    
    private LocationListener myLocationListener;

    private QueryManager queryManager;
    
    private RouteHandler routeHandler;
    
    private final OnCheckedChangeListener filterComboBoxCheckedChangeListener = new OnCheckedChangeListener() {
        
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            final FacilityType type = (FacilityType) buttonView.getTag();
            MyLog.d(TAG, type + " isChecked [" + isChecked + "]");
            if (isChecked) {
                state.criteria.addAllowedType(type);
            } else {
                state.criteria.removeAllowedType(type);
            }
        }
    };
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyLog.d(TAG, "onCreate @ " + this.getClass().getSimpleName());

		/* Do we have previous instance data? */
        if (getLastNonConfigurationInstance() instanceof State) {
        	this.state = (State) getLastNonConfigurationInstance();
        } else {
            final Set<FacilityType> allowedTypes = new HashSet<FacilityType>();
            allowedTypes.addAll(Arrays.asList(FacilityType.values()));

            this.state = new State();
            this.state.criteria.setAllowedTypes(allowedTypes);
		}

        /* Ensure that DataSource is prepared and current */
    	checkDataSource();

    	/* Prepare the UI */
        setContentView(R.layout.activity_main);
        this.slidingDrawer = (SlidingDrawer) findViewById(R.id.drawer);
        this.mapView = (MapView) findViewById(R.id.map_view);
        initializeMapView();

        /* Initialize a handlers here to ensure that they're attached to the UI thread */
        this.queryManager = new QueryManager(getApplicationContext(), mapView, this.state.criteria, this);
        this.routeHandler = new RouteHandler();

        this.locationOverlay = new MyLocationOverlay(this, mapView);
        this.myLocationListener = new MapLocationListener(mapView, routeHandler, state);
        this.routeOverlay = state.routeOverlay;

        final CheckBox trackingCheckBox = (CheckBox) findViewById(R.id.trackingCheckBox);
        trackingCheckBox.setChecked(state.isTrackingEnabled);
        trackingCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleLocationTracking(buttonView);
            }
        });
        
        final EditText queryEditText = (EditText) this.slidingDrawer.findViewById(R.id.queryEditText);
        queryEditText.addTextChangedListener(new TextWatcher() {
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                state.criteria.setQuery(s.toString());
            }
        });
        final ImageButton clearQueryButton = (ImageButton) this.slidingDrawer.findViewById(R.id.clearQueryButton);
        clearQueryButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                queryEditText.setText("");
            }
            
        });
        
        FacilityTypeInflateStrategy typeInflateStrategy;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            typeInflateStrategy = new LandscapeFacilityTypeInflateStrategy(this, R.id.typesGridView);
        } else {
            typeInflateStrategy = new PortraitFacilityTypeInflateStrategy(this, R.id.drawer_filters_viewgroup);
        }

        typeInflateStrategy.inflate(filterComboBoxCheckedChangeListener);
        typeInflateStrategy.updateState(state.criteria);
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
			queryManager.makeFirstQuery();
		}
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
    	queryManager.onPause();
    }

	@Override
	protected void onDestroy() {
        MyLog.d(TAG, "onDestroy @ " + this.getClass().getSimpleName());
        queryManager.onDestroy();
		super.onDestroy();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case ID_DIALOG_INITIALIZE_DAO:
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

    public void onShowAboutClicked(View v) {
        showAbout();
    }
    
    public void onSearchClicked(View v) {
        onSearchRequested();
    }
    
    public void toggleLocationTracking(View v) {
        state.isTrackingEnabled = !state.isTrackingEnabled;
    }
    
	@Override
    public void onFacilityTap(Facility facility) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final FacilityDialogUtils dialogUtils = new FacilityDialogUtils(this, facility, inflater);
        final AlertDialog dialog = dialogUtils.createFacilityDialog(routeHandler);
        dialog.show();
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	private void checkDataSource() {
        final DataSourceInitializerAsyncTask retrievedAsyncTask = state.daoInitializerAsyncTask;
        if (retrievedAsyncTask != null) {
        	if (retrievedAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            	retrievedAsyncTask.setListener(this);
        	}
        } else {
            final FacilityDAOHelper daoHelper = FacilityDAOHelper.getInstance(getApplicationContext());
    		if (!daoHelper.isDataPrepared()) {
                showDialog(ID_DIALOG_INITIALIZE_DAO);
    			state.daoInitializerAsyncTask = new DataSourceInitializerAsyncTask(daoHelper, this);;
    			state.daoInitializerAsyncTask.execute();
    		}
        }
    }

	private final Runnable onFirstFixRunnable = new Runnable() {

		@Override
        public void run() {
			runOnUiThread(new Runnable() {

				@Override
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

//	    locationManager.requestLocationUpdates(
//	        LocationManager.NETWORK_PROVIDER,
//	        0,
//	        0,
//	        myLocationListener);
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
	
	@Override
    public void onDatabaseInitialized(boolean success) {
	    state.daoInitializerAsyncTask = null;
		if (success) {
			dismissDialog(ID_DIALOG_INITIALIZE_DAO);
			queryManager.makeFirstQuery();
		} else {
			// Not good, we encountered a real FATAL error..
			// TODO: Push information to the user.
		}
	}

	@Override
	public void onAsyncFacilityQueryStarted() {
	    findViewById(R.id.loadingViewGroup).setVisibility(View.VISIBLE);	    
	}
	
    @Override
    public void onAsyncFacilityQueryCompleted(List<Facility> result) {
        findViewById(R.id.loadingViewGroup).setVisibility(View.GONE);
        MyLog.e(TAG, "Query finished. Returned rows: " + result.size());

        if (result.size() == 0) {
            MyLog.d(TAG, "Removing overlays from the MapView");
            final List<Overlay> overlays = mapView.getOverlays();
            overlays.clear();
            overlays.add(locationOverlay);
        } else {
            final FacilitiesOverlay overlay = new FaciltiesOverlayBuilder(this).buildOverlay(result, this);

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