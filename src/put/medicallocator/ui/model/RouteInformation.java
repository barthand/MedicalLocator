package put.medicallocator.ui.model;

import put.medicallocator.io.model.Facility;
import put.medicallocator.io.route.model.RouteSpec;

public class RouteInformation {

    private final RouteSpec routeSpec;

    private final Facility targetFacility;

    public RouteInformation(RouteSpec routeSpec, Facility targetFacility) {
        this.routeSpec = routeSpec;
        this.targetFacility = targetFacility;
    }

    public RouteSpec getRouteSpec() {
        return routeSpec;
    }

    public Facility getTargetFacility() {
        return targetFacility;
    }
}
