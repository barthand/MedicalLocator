package put.medicallocator.io.route.base;

import put.medicallocator.io.route.model.RoutePoint;
import put.medicallocator.io.route.model.RouteSpec;

import java.io.InputStream;

/**
 * Base class for all implementations of classed providing information about route (calculated
 * based on pair of points (start, finish)) coming from HTTP connections.
 */
public abstract class AbstractRouteOverHttpProvider {

    protected final RoutePoint startPoint;
    protected final RoutePoint finishPoint;

    protected AbstractRouteOverHttpProvider(RoutePoint startPoint, RoutePoint finishPoint) {
        this.startPoint = startPoint;
        this.finishPoint = finishPoint;
    }

    protected abstract String buildUrl();

    protected abstract RouteSpec parseRoute(InputStream is);
}
