package put.medicallocator.ui.utils;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import put.medicallocator.io.route.model.RouteSpec;
import put.medicallocator.ui.ActivityMain;
import put.medicallocator.ui.async.DataSourceConfigurerAsyncTask;
import put.medicallocator.ui.async.model.SearchCriteria;
import put.medicallocator.ui.intent.IntentHandler;
import put.medicallocator.ui.overlay.RouteOverlay;
import put.medicallocator.utils.GeoUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes the state associated with the {@link ActivityMain}.
 */
public class State {

    /** Defines the start GeoPoint. Yeah, let's all do the Poznan! ;) */
    private static final GeoPoint START_GEOPOINT = GeoUtils.createGeoPoint(52.408396, 16.92838);

    /** Defines the start zoom level. */
    private static final int START_ZOOM_LEVEL = 14;

    /** The reference to the {@link GeoPoint} set as the center of the map last time. */
    public GeoPoint currentPoint;

    /** Stores the last used zoom level. */
    public int zoomLevel;

    /** Indicates whether the user-position tracking is enabled on the {@link MapView}. */
    public boolean isTrackingEnabled = false;

    /** Indicates whether position tracking is performed via the GPS provider. */
    public boolean isGPSEnabled = false;

    /** Stores the reference to the last used {@link SearchCriteria}s. */
    public final SearchCriteria criteria;

    /** Stores the {@link RouteSpec} (if present). */
    public RouteSpec routeSpec;

    public Map<Class<? extends IntentHandler>, Object> intentHandlersState = new HashMap<Class<? extends IntentHandler>, Object>();

    /**
     * Stores the reference to the {@link DataSourceConfigurerAsyncTask}.
     * If configuration change occurs while this task is running, this may be helpful.
     */
    public DataSourceConfigurerAsyncTask daoInitializerAsyncTask;

    public State() {
        currentPoint = START_GEOPOINT;
        zoomLevel = START_ZOOM_LEVEL;
        criteria = new SearchCriteria();
    }
}
