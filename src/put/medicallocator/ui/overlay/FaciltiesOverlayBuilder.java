package put.medicallocator.ui.overlay;

import java.util.List;

import put.medicallocator.io.model.Facility;
import put.medicallocator.ui.overlay.utils.FacilityLookupStrategy;
import put.medicallocator.ui.overlay.utils.FacilityTapListener;
import put.medicallocator.ui.overlay.utils.HitTestFacilityLookupStrategy;
import android.content.Context;

public class FaciltiesOverlayBuilder {

    private final DrawableCache drawableCache; 

    public FaciltiesOverlayBuilder(Context context) {
        this.drawableCache = new DrawableCache(context);
    }
        
    public FacilitiesOverlay buildOverlay(List<Facility> source, FacilityTapListener listener) {
        final FacilityLookupStrategy lookupStrategy = new HitTestFacilityLookupStrategy(source, drawableCache);
        return new FacilitiesOverlay(source, drawableCache, lookupStrategy, listener);
    }

    public FacilitiesOverlay buildOverlay(List<Facility> source, FacilityTapListener listener, FacilityLookupStrategy lookupStrategy) {
        return new FacilitiesOverlay(source, drawableCache, lookupStrategy, listener);
    }

}
