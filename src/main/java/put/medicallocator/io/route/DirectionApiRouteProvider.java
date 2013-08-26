package put.medicallocator.io.route;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import put.medicallocator.io.route.base.AbstractRouteOverHttpProvider;
import put.medicallocator.io.route.directionapi.model.DirectionApiResponse;
import put.medicallocator.io.route.directionapi.model.LatLng;
import put.medicallocator.io.route.directionapi.model.Route;
import put.medicallocator.io.route.model.RoutePoint;
import put.medicallocator.io.route.model.RouteSpec;
import put.medicallocator.utils.ArrayUtils;
import put.medicallocator.utils.CollectionUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Implementation of the {@link AbstractRouteOverHttpProvider} using the Google's Direction API
 * as a route provider. JSON response is mapped using GSON.
 */
public class DirectionApiRouteProvider extends AbstractRouteOverHttpProvider {

    private static final String BASE_URL = "http://maps.google.com/maps/api/directions/json?";

    private interface RequestParameters {
        String ORIGIN = "origin";
        String DESTINATION = "destination";
        String SENSOR = "sensor";
    }

    public DirectionApiRouteProvider(RoutePoint startPoint, RoutePoint finishPoint) {
        super(startPoint, finishPoint);
    }

    @Override
    protected String buildUrl() {
        return new StringBuilder(BASE_URL)
                .append(RequestParameters.ORIGIN).append('=').append(startPoint.getLatitude())
                .append(',').append(startPoint.getLongitude())
                .append('&').append(RequestParameters.DESTINATION).append('=').append(finishPoint.getLatitude())
                .append(',').append(finishPoint.getLongitude())
                .append('&').append(RequestParameters.SENSOR).append("=false").toString();
    }

    @Override
    protected RouteSpec parseRoute(InputStream is) {
        DirectionApiResponse response = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create().fromJson(new InputStreamReader(is), DirectionApiResponse.class);

        RouteSpec routeSpec = null;
        if (ArrayUtils.isNotEmpty(response.getRoutes())) {
            final Route route = response.getDefaultRoute();
            final List<LatLng> polylineDecoded = route.getOverviewPolyline().decode();
            if (CollectionUtils.isNotEmpty(polylineDecoded)) {
                routeSpec = new RouteSpec();

                final int polylineSize = polylineDecoded.size();
                final RoutePoint[] points = new RoutePoint[polylineSize];
                for (int i = 0; i < polylineSize; i++) {
                    points[i] = buildRoutePoint(polylineDecoded.get(i));
                }
                routeSpec.setPoints(points);
            }
        }

        return routeSpec;
    }

    private static RoutePoint buildRoutePoint(LatLng latLng) {
        final RoutePoint routePoint = new RoutePoint();
        routePoint.setLatitude(latLng.getLat());
        routePoint.setLongitude(latLng.getLng());
        return routePoint;
    }

}
