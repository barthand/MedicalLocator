package put.medicallocator.ui.overlay.utils;

import java.util.List;

import put.medicallocator.io.model.Facility;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

public class HitTestFacilityLookupStrategy implements FacilityLookupStrategy {

    private final List<Facility> source;
    
    private int markerHalfWidth;
    private int markerHalfHeight;

    private final Point facilityOutPoint = new Point();
    private final Point targetOutPoint = new Point();
    private final Rect rect = new Rect();

    public HitTestFacilityLookupStrategy(List<Facility> source, Drawable marker) {
        this.source = source;
        this.markerHalfHeight = marker.getIntrinsicHeight() / 2;
        this.markerHalfWidth = marker.getIntrinsicWidth() / 2;
    }

    public Facility findNearestFacility(GeoPoint point, Projection projection) {
        for (Facility facility : source) {
            if (hitTest(facility, point, projection)) {
                return facility;
            }
        }
        
        return null;
    }

    private boolean hitTest(Facility facility, GeoPoint point, Projection projection) {
        final GeoPoint facilityGeoPoint = facility.getGeoPoint();
        
        projection.toPixels(facilityGeoPoint, facilityOutPoint);
        projection.toPixels(point, targetOutPoint);

        rect.set(facilityOutPoint.x - markerHalfWidth, facilityOutPoint.y - markerHalfHeight, 
                facilityOutPoint.x + markerHalfWidth, facilityOutPoint.y + markerHalfHeight);
        
        return rect.contains(targetOutPoint.x, targetOutPoint.y);
    }
    

}
