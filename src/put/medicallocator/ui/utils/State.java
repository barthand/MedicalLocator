package put.medicallocator.ui.utils;

import put.medicallocator.ui.async.DataSourceInitializerAsyncTask;
import put.medicallocator.ui.async.SearchCriteria;
import put.medicallocator.ui.overlay.RouteOverlay;
import put.medicallocator.utils.GeoUtils;

import com.google.android.maps.GeoPoint;

public class State {
    
    /** Defines the start GeoPoint. Yeah, let's all do the Poznan! ;) */
    private static final GeoPoint START_GEOPOINT = GeoUtils.convertToGeoPoint(52.408396, 16.92838);

    /** Defines the start zoom level. */
    private static final int START_ZOOM_LEVEL = 14;

    public GeoPoint currentPoint;
    public int zoomLevel;
    public boolean isTrackingEnabled = false;
    public boolean isGPSEnabled = false;
    public final SearchCriteria criteria;
    public RouteOverlay routeOverlay;
    public DataSourceInitializerAsyncTask daoInitializerAsyncTask;

    public State() {
        currentPoint = START_GEOPOINT;
        zoomLevel = START_ZOOM_LEVEL;
        criteria = new SearchCriteria();
    }
}
