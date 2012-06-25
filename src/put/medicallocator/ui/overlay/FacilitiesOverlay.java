package put.medicallocator.ui.overlay;

import java.util.List;

import put.medicallocator.io.Facility;
import put.medicallocator.ui.ActivityMain.RouteHandler;
import put.medicallocator.ui.utils.FacilityDialogUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class FacilitiesOverlay extends Overlay {

    /**
     * Defines the distance in pixels within which the tapped objects would be accepted.
     */
    private static final int PIXELS_DISTANCE_TO_TAP_ACCEPT = 80;

    private final Context context;
    private final RouteHandler handler;
    private final List<Facility> source;
    private final Drawable drawable;

    private boolean filterDistantTaps = true;

    public FacilitiesOverlay(Context context, RouteHandler handler, List<Facility> source, Drawable drawable) {
        this.context = context;
        this.handler = handler;
        this.source = source;
        this.drawable = drawable;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (shadow) {
            // To speed-up drawing things, we just omit this phase.
            return;
        }

        final Projection projection = mapView.getProjection();
        final Point point = new Point();

        for (Facility facility : source) {
            projection.toPixels(facility.getGeoPoint(), point);
            drawable.setBounds(point.x, point.y,
                    point.x + drawable.getIntrinsicWidth(), point.y + drawable.getIntrinsicHeight());
            drawable.draw(canvas);
        }
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        final Facility facility = findNearestFacility(p, mapView.getProjection());

        if (facility != null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final FacilityDialogUtils dialogUtils = new FacilityDialogUtils(context, facility, inflater);
            final AlertDialog dialog = dialogUtils.createFacilityDialog(handler);
            dialog.show();

            return true;
        }
        return false;
    }

    protected Facility findNearestFacility(GeoPoint point, Projection projection) {
        // TODO: Sort the facilities and use binary search or some other kind of indexing..
        Facility nearest = null;
        float minDistance = Float.MAX_VALUE;

        for (Facility facility : source) {
            final GeoPoint currentGeoPoint = facility.getGeoPoint();

            final float latitudeDistance = point.getLatitudeE6() - currentGeoPoint.getLatitudeE6();
            final float longitudeDistance = point.getLongitudeE6() - currentGeoPoint.getLongitudeE6();
            final float currentDistance = calculateDistance(longitudeDistance, latitudeDistance);

            if (nearest == null || minDistance > currentDistance) {
                nearest = facility;
                minDistance = currentDistance;
            }
        }

        if (filterDistantTaps) {
            if (!isWithinPixelRadius(projection, nearest.getGeoPoint(), point, PIXELS_DISTANCE_TO_TAP_ACCEPT)) {
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
