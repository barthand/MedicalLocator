package put.medicallocator.ui.overlay.utils;

import java.util.List;

import put.medicallocator.io.model.Facility;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

public class BasicFacilityLookupStrategy implements FacilityLookupStrategy {

    /**
     * Defines the distance in pixels within which the tapped objects would be accepted.
     */
    private static final int PIXELS_DISTANCE_TO_TAP_ACCEPT = 80;

    private final List<Facility> source;

    private boolean filterDistantTaps = true;

    public BasicFacilityLookupStrategy(List<Facility> source) {
        this.source = source;
    }

    public Facility findNearestFacility(GeoPoint point, Projection projection) {
        // TODO: Sort the facilities and use binary search or some other kind of indexing..
        Facility nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Facility facility : source) {
            final GeoPoint currentGeoPoint = facility.getLocation();

            final float latitudeDistance = point.getLatitudeE6() - currentGeoPoint.getLatitudeE6();
            final float longitudeDistance = point.getLongitudeE6() - currentGeoPoint.getLongitudeE6();
            final float currentDistance = calculateDistance(longitudeDistance, latitudeDistance);

            if (nearest == null || minDistance > currentDistance) {
                nearest = facility;
                minDistance = currentDistance;
            }
        }

        if (filterDistantTaps) {
            if (!isWithinPixelRadius(projection, nearest.getLocation(), point, PIXELS_DISTANCE_TO_TAP_ACCEPT)) {
                return null;
            }
        }

        return nearest;
    }

    private static boolean isWithinPixelRadius(Projection projection, GeoPoint nearest, GeoPoint tapPoint, int pixelRadius) {
        Point firstPoint = new Point(), secondPoint = new Point();
        projection.toPixels(nearest, firstPoint);
        projection.toPixels(tapPoint, secondPoint);

        final float distance = calculateDistance(firstPoint.y - secondPoint.y, firstPoint.x - secondPoint.x);
        return distance <= pixelRadius;
    }

    private static float calculateDistance(float horizontalDistance, float verticalDistance) {
        return (float) Math.sqrt(Math.pow(horizontalDistance, 2) + Math.pow(verticalDistance, 2));
    }

}
