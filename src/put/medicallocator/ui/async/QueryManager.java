package put.medicallocator.ui.async;

import java.util.List;

import put.medicallocator.io.IFacilityDAO;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.sqlite.DatabaseFacilityDAO;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler.FacilityQueryExecutor;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler.FacilityQueryListener;
import put.medicallocator.utils.GeoUtils;
import android.content.Context;
import android.os.Handler;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

public class QueryManager implements FacilityQueryListener {

    /** Defines the time (in ms) after which query is run if current area descriptor hasn't changed. */
    private static final long TIME_IN_MS_WITHOUT_DESCRIPTOR_CHANGE_TO_QUERY = 500;

    private static final int RADIUS_TO_QUERY_IN_KILOMETERS = 10;

    private final Handler uiThreadHandler;    
    private final MapView mapView;
    private final SearchCriteria criteria;

    private final FacilityQueryListener delegateListener;
    
    /* DAO layer */
    private final IFacilityDAO facilityDao;
    private final AsyncFacilityWorkerHandler queryWorkerHandler;

    private GeoPoint lastMapCenterGeoPoint;
    private long lastDescriptorChangeTimestamp;
    private volatile long lastQueryExecutedTimestamp;
    private boolean queryCompleted = false;

    private final Runnable doQueryIfRequired = new Runnable() {
        @Override
        public void run() {
            requery();
        }
    };

    /**
     * Simple constructor of {@link QueryManager}. Since internally it creates a Handler to post jobs on the UI thread,
     * it <b>has to be invoked in the UI thread</b>.
     * @param context
     * @param listener
     */
    public QueryManager(Context context, MapView mapView, SearchCriteria criteria, FacilityQueryListener listener) {
        this(context, mapView, criteria, listener, new Handler());
    }
    
    public QueryManager(Context context, MapView mapView, SearchCriteria criteria, FacilityQueryListener listener, Handler handler) {
        this.uiThreadHandler = handler;
        this.mapView = mapView;
        this.criteria = criteria;
        this.facilityDao = new DatabaseFacilityDAO(context);
        this.queryWorkerHandler = new AsyncFacilityWorkerHandler(this);
        this.delegateListener = listener;
    }
    
    public void makeFirstQuery() {
        lastDescriptorChangeTimestamp = System.currentTimeMillis();
        uiThreadHandler.post(doQueryIfRequired);
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
            
            queryWorkerHandler.scheduleQuery(new FacilityQueryExecutor() {

                @Override
                public List<Facility> execute() throws Exception {
                    lastQueryExecutedTimestamp = System.currentTimeMillis();
                    return facilityDao.findNamedWithinArea(lowerLeft, upperRight, criteria);
                }
            });
        }

        /* Post this job once again after 1000ms */
        uiThreadHandler.postDelayed(doQueryIfRequired, 1000);
    }
    
    @Override
    public void onAsyncFacilityQueryStarted() {
        this.delegateListener.onAsyncFacilityQueryStarted();
    }
    
    @Override
    public void onAsyncFacilityQueryCompleted(List<Facility> result) {
        this.queryCompleted = true;
        this.delegateListener.onAsyncFacilityQueryCompleted(result);
    }

    public void onPause() {
        uiThreadHandler.removeCallbacks(doQueryIfRequired);
    }
    
    public void onDestroy() {
        this.queryWorkerHandler.onDestroy();
    }
    
}
