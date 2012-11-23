package put.medicallocator.ui.overlay.utils;

import java.util.List;

import put.medicallocator.io.model.Facility;
import put.medicallocator.ui.overlay.DrawableCache;
import put.medicallocator.ui.overlay.DrawableContext;
import android.graphics.Point;
import android.graphics.Rect;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

public class HitTestFacilityLookupStrategy implements FacilityLookupStrategy {

    private final List<Facility> source;
    private final DrawableCache drawableCache;
    
    private final Point facilityOutPoint = new Point();
    private final Point targetOutPoint = new Point();
    private final Rect rect = new Rect();

    public HitTestFacilityLookupStrategy(List<Facility> source, DrawableCache drawableCache) {
        this.source = source;
        this.drawableCache = drawableCache;
    }

    @Override
    public Facility findNearestFacility(GeoPoint point, Projection projection) {
        for (Facility facility : source) {
            if (hitTest(facility, point, projection)) {
                return facility;
            }
        }
        
        return null;
    }

    private boolean hitTest(Facility facility, GeoPoint point, Projection projection) {
        final GeoPoint facilityGeoPoint = facility.getLocation();
        final DrawableContext drawableContext = drawableCache.get(facility.getFacilityType());
        
        projection.toPixels(facilityGeoPoint, facilityOutPoint);
        projection.toPixels(point, targetOutPoint);

        rect.set(facilityOutPoint.x - drawableContext.getHalfWidth(), facilityOutPoint.y - drawableContext.getHalfHeight(), 
                facilityOutPoint.x + drawableContext.getHalfWidth(), facilityOutPoint.y + drawableContext.getHalfHeight());
        
        return rect.contains(targetOutPoint.x, targetOutPoint.y);
    }
    

}
