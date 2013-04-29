package put.medicallocator.ui.overlay;

import android.content.Context;
import put.medicallocator.io.model.Facility;
import put.medicallocator.ui.overlay.utils.FacilityLookupStrategy;
import put.medicallocator.ui.overlay.utils.FacilityTapListener;
import put.medicallocator.ui.overlay.utils.HitTestFacilityLookupStrategy;

import java.util.List;

/**
 * Builder pattern for the {@link FacilitiesOverlay}.
 */
public class FaciltiesOverlayBuilder {

    private final FacilityTypeDrawableCache drawableCache;

    public FaciltiesOverlayBuilder(Context context) {
        this.drawableCache = new FacilityTypeDrawableCache(context);
    }

    /**
     * Builds the {@link FacilitiesOverlay} for provided list of {@link Facility}, associates provided
     * {@link FacilityTapListener} with it.
     */
    public FacilitiesOverlay buildOverlay(List<Facility> source, FacilityTapListener listener) {
        final FacilityLookupStrategy lookupStrategy = new HitTestFacilityLookupStrategy(source, drawableCache);
        return buildOverlay(source, listener, lookupStrategy);
    }

    /**
     * Builds the {@link FacilitiesOverlay} for provided list of {@link Facility}, associates provided
     * {@link FacilityTapListener} and {@link FacilityLookupStrategy} with it.
     */
    public FacilitiesOverlay buildOverlay(List<Facility> source, FacilityTapListener listener, FacilityLookupStrategy lookupStrategy) {
        return new FacilitiesOverlay(source, drawableCache, lookupStrategy, listener);
    }

}
