package put.medicallocator.ui.async;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import java.util.List;

import put.medicallocator.application.Application;
import put.medicallocator.io.IFacilityDAO;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.sqlite.DatabaseFacilityDAO;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler.FacilityQueryExecutor;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler.FacilityQueryListener;
import put.medicallocator.ui.async.model.SearchCriteria;
import put.medicallocator.utils.GeoUtils;

/**
 * Controls how the querying is being scheduled based on the {@link MapView} state.
 */
public class QueryController {

    /** Defines the time (in ms) after which query is run if current area descriptor hasn't changed. */
    private static final long TIME_IN_MS_WITHOUT_DESCRIPTOR_CHANGE_TO_QUERY = 500;

    /** Defines the radius (in km) within which objects are searched. */
    private static final int RADIUS_TO_QUERY_IN_KILOMETERS = 10;

    private final Handler uiThreadHandler;
    private final MapView mapView;
    private final SearchCriteria criteria;

    private final FacilityQueryListener listenerDelegate;

    /* DAO layer */
    private final IFacilityDAO facilityDao;
    private final AsyncFacilityWorkerHandler queryWorkerHandler;

    private GeoPoint lastMapCenterGeoPoint;
    private long lastDescriptorChangeTimestamp;
    private volatile long lastQueryExecutedTimestamp;
    private boolean queryCompleted = false;

    private FacilityQueryListener delegatingListener = new FacilityQueryListener() {
        @Override
        public void onAsyncFacilityQueryStarted() {
            listenerDelegate.onAsyncFacilityQueryStarted();
        }

        @Override
        public void onAsyncFacilityQueryCompleted(List<Facility> result) {
            queryCompleted = true;
            listenerDelegate.onAsyncFacilityQueryCompleted(result);
        }
    };

    private final Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            scheduleQueryIfRequired();
        }
    };

    /**
     * Simple constructor of {@link QueryController}. Since it creates internally a Handler to post messages on the UI thread,
     * it <b>has to be invoked in the UI thread</b> (or at least created in the thread running a {@link Looper}).
     */
    public QueryController(Application application, MapView mapView, SearchCriteria criteria, FacilityQueryListener listener) {
        this(application, mapView, criteria, listener, new Handler());
    }

    /**
     * Extended constructor of {@link QueryController}, allowing more custom initialization.
     */
    public QueryController(Application application, MapView mapView, SearchCriteria criteria, FacilityQueryListener listener, Handler handler) {
        this.facilityDao = new DatabaseFacilityDAO(application);
        this.mapView = mapView;
        this.criteria = criteria;
        this.listenerDelegate = listener;
        this.uiThreadHandler = handler;
        this.queryWorkerHandler = new AsyncFacilityWorkerHandler(delegatingListener);
    }

    /**
     * Makes this {@link QueryController} listen for {@link MapView} state and query for objects,
     * if required.
     */
    public void attach() {
        lastDescriptorChangeTimestamp = System.currentTimeMillis();
        uiThreadHandler.post(checkRunnable);
    }

    /**
     * Stops the querying process.
     */
    public void detach() {
        uiThreadHandler.removeCallbacks(checkRunnable);
    }

    /**
     * Invoke this when your {@link Context} (e.g. {@link Activity} is being destroyed.
     * Clears the internal structures (including thread stopping).
     */
    public void onDestroy() {
        this.queryWorkerHandler.onDestroy();
    }

    private void scheduleQueryIfRequired() {
        /* Save the exact coordinates of the top-left visible vertex of MapView */
        final GeoPoint currentVisibleGeoPoint = mapView.getMapCenter();
        final int currentLatitude = currentVisibleGeoPoint.getLatitudeE6();
        final int currentLongitude = currentVisibleGeoPoint.getLongitudeE6();

        boolean scheduleQuery = false;

        if (lastMapCenterGeoPoint == null) {
            lastMapCenterGeoPoint = currentVisibleGeoPoint;
            scheduleQuery = true;
        } else {
            final int lastLatitude = lastMapCenterGeoPoint.getLatitudeE6();
            final int lastLongitude = lastMapCenterGeoPoint.getLongitudeE6();

            if (lastLatitude == currentLatitude && lastLongitude == currentLongitude) {
                final long now = System.currentTimeMillis();
                if (lastQueryExecutedTimestamp < criteria.getLastChangeTimestamp()) {
                    scheduleQuery = true;
                } else if (now > lastDescriptorChangeTimestamp + TIME_IN_MS_WITHOUT_DESCRIPTOR_CHANGE_TO_QUERY) {
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
                    GeoUtils.boundingCoordinates(RADIUS_TO_QUERY_IN_KILOMETERS, currentVisibleGeoPoint);
            final GeoPoint lowerLeft = boundingGeoPoints[0];
            final GeoPoint upperRight = boundingGeoPoints[1];

            queryWorkerHandler.invokeAsyncQuery(new FacilityQueryExecutor() {

                @Override
                public List<Facility> execute() throws Exception {
                    lastQueryExecutedTimestamp = System.currentTimeMillis();
                    return facilityDao.findWithinAreaUsingCriteria(lowerLeft, upperRight, criteria);
                }
            });
        }

        /* Post this job once again after 1000ms */
        uiThreadHandler.postDelayed(checkRunnable, 1000);
    }

}
