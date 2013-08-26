package put.medicallocator.ui.misc;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import put.medicallocator.io.route.model.RouteSpec;
import put.medicallocator.ui.model.RouteInformation;
import put.medicallocator.ui.overlay.FacilityTypeDrawableCache;
import put.medicallocator.ui.overlay.RouteOverlay;
import put.medicallocator.ui.utils.State;

import java.util.List;

/**
 * Component allowing to create the {@link RouteOverlay} based on the {@link RouteSpec} and show/remove it
 * on/from the {@link MapView}.
 */
public class RouteOverlayManager {

    private final MapView mapView;
    private final State state;
    private final FacilityTypeDrawableCache drawableCache;

    private RouteOverlay routeOverlay;

    public RouteOverlayManager(MapView mapView, FacilityTypeDrawableCache drawableCache, State state) {
        this.mapView = mapView;
        this.state = state;
        this.drawableCache = drawableCache;
    }

    /**
     * Creates the {@link RouteOverlay} and shows it on the {@link MapView}.
     */
    public void showRoute(RouteInformation routeInfo) {
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.remove(routeOverlay);
        routeOverlay = new RouteOverlay(routeInfo, drawableCache, mapView);
        state.routeInformation = routeInfo;
        listOfOverlays.add(routeOverlay);
        mapView.invalidate();
    }

    public RouteOverlay getOrRestoreOverlay(RouteInformation route) {
        if (route != null) {
            if (routeOverlay == null) {
                this.routeOverlay = new RouteOverlay(route, drawableCache, mapView);
            }
            return routeOverlay;
        }
        return null;
    }

    /**
     * Clears the {@link RouteOverlay}.
     */
    public void clearRoute() {
        this.routeOverlay = null;
        state.routeInformation = null;
    }
}