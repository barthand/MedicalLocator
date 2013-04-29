package put.medicallocator.ui.overlay.utils;

import android.graphics.Point;
import android.graphics.Rect;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import put.medicallocator.io.model.Facility;
import put.medicallocator.ui.overlay.DrawableContext;
import put.medicallocator.ui.overlay.FacilityTypeDrawableCache;

import java.util.List;

/**
 * Implementation of {@link FacilityLookupStrategy} iterating over all the {@link Facility}
 * and, based on the {@link DrawableContext}s associated with them, checking whether the selected point is within the
 * area of the marker drawn for certain {@link Facility}.
 */
public class HitTestFacilityLookupStrategy implements FacilityLookupStrategy {

    private final List<Facility> source;
    private final FacilityTypeDrawableCache drawableCache;
    
    private final Point facilityOutPoint = new Point();
    private final Point targetOutPoint = new Point();
    private final Rect rect = new Rect();

    public HitTestFacilityLookupStrategy(List<Facility> source, FacilityTypeDrawableCache drawableCache) {
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
