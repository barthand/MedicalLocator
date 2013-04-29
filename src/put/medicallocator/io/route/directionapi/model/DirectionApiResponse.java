package put.medicallocator.io.route.directionapi.model;

import put.medicallocator.utils.ArrayUtils;

/**
 * Defines the root element of the response coming from the Google's Direction APIs.
 */
public class DirectionApiResponse {

    private Route[] routes;

    public Route[] getRoutes() {
        return routes;
    }

    public void setRoutes(Route[] routes) {
        this.routes = routes;
    }

    public Route getDefaultRoute() {
        return ArrayUtils.isNotEmpty(routes) ? routes[0] : null;
    }
}
