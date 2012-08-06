package put.medicallocator.ui.overlay;

import java.util.List;

import put.medicallocator.io.model.Facility;
import put.medicallocator.ui.overlay.utils.BasicFacilityLookupStrategy;
import put.medicallocator.ui.overlay.utils.FacilityLookupStrategy;
import put.medicallocator.ui.overlay.utils.FacilityTapListener;
import android.graphics.drawable.Drawable;

public class FaciltiesOverlayBuilder {

    private final List<Facility> source;
    private Drawable drawable;
    private FacilityLookupStrategy lookupStrategy;

    public FaciltiesOverlayBuilder(List<Facility> source, Drawable drawable) {
        this(source, drawable, new BasicFacilityLookupStrategy(source)); 
    }
    
    public FaciltiesOverlayBuilder(List<Facility> source, Drawable drawable, FacilityLookupStrategy strategy) {
        this.source = source;
        this.drawable = drawable;
        this.lookupStrategy = strategy;
    }

    public FacilitiesOverlay buildOverlay(FacilityTapListener listener) {
        return new FacilitiesOverlay(source, drawable, lookupStrategy, listener);
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }
    
    public FacilityLookupStrategy getLookupStrategy() {
        return lookupStrategy;
    }

    public void setLookupStrategy(FacilityLookupStrategy lookupStrategy) {
        this.lookupStrategy = lookupStrategy;
    }

}
