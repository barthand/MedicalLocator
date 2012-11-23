package put.medicallocator.ui.overlay;

import java.util.List;

import junit.framework.Assert;
import put.medicallocator.io.model.Facility;
import put.medicallocator.ui.overlay.utils.FacilityLookupStrategy;
import put.medicallocator.ui.overlay.utils.FacilityTapListener;
import put.medicallocator.utils.MyLog;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class FacilitiesOverlay extends Overlay {

    /* TODO (just some loose ideas):
     * + draw all markers within bounding box specified by current MapView
     *   * for lower zooms, look below.
     * + grouping overlays in lower zooms to reduce overhead with each individual drawing:
     *   * deliver number of facilities around on the marker?
     *   * make the dialog for such grouped markers describing count, facilities types, etc.
     *   * for each zoom level, cache the markers being drawn (to speed up queries), 
     *   * but still consider memory constraints,
     * + consider drawing all markers on the MapView, using the solution above,
     *   * query all markers on startup and keep them in the memory,
     *   * if too much memory needed, group more markers in one go,
     */
    
    private static final String TAG = FacilitiesOverlay.class.getSimpleName();
    
    private final List<Facility> source;
    private final DrawableCache drawableCache;
    private final FacilityTapListener tapListener;
    private final FacilityLookupStrategy lookupStrategy;
    
    FacilitiesOverlay(List<Facility> source, DrawableCache drawableCache, FacilityLookupStrategy lookupStrategy, FacilityTapListener listener) {
        this.source = source;
        this.drawableCache = drawableCache;
        this.tapListener = listener;
        this.lookupStrategy = lookupStrategy;

        if (MyLog.ASSERT_ENABLED) {
            Assert.assertNotNull(lookupStrategy);
        }
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
            projection.toPixels(facility.getLocation(), point);
            
            final DrawableContext drawableContext = drawableCache.get(facility.getFacilityType());
            final Drawable drawable = drawableContext.drawable;
            drawable.setBounds(point.x - drawableContext.halfWidth, point.y - drawableContext.halfHeight,
                    point.x + drawableContext.halfWidth, point.y + drawableContext.halfHeight);
            drawable.draw(canvas);
        }
        
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        final long start = System.currentTimeMillis();
        
        final Facility facility = lookupStrategy.findNearestFacility(p, mapView.getProjection());
        
        final long end = System.currentTimeMillis();
        MyLog.d(TAG, "Finding nearest facility took " + (end - start) + "ms");

        if (facility != null) {
            if (tapListener != null) {
                tapListener.onFacilityTap(facility);
            }
        }
        return false;
    }
}
