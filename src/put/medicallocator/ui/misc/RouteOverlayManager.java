package put.medicallocator.ui.misc;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import put.medicallocator.io.route.model.RouteSpec;
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

    private RouteOverlay routeOverlay;

    public RouteOverlayManager(MapView mapView, State state) {
        this.mapView = mapView;
        this.state = state;
    }

    /**
     * Creates the {@link RouteOverlay} and shows it on the {@link MapView}.
     */
    public void showRoute(RouteSpec route) {
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.remove(routeOverlay);
        buildOverlay(route);
        state.routeSpec = route;
        listOfOverlays.add(routeOverlay);
        mapView.invalidate();
    }

    private void buildOverlay(RouteSpec route) {
        this.routeOverlay = new RouteOverlay(route, mapView);
    }

    public RouteOverlay getOrRestoreRoute(RouteSpec route) {
        if (route != null) {
            if (routeOverlay == null) {
                buildOverlay(route);
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
        state.routeSpec = null;
    }
}