package put.medicallocator.ui.overlay;

import java.util.List;

import put.medicallocator.io.Facility;
import put.medicallocator.ui.overlay.utils.BasicFacilityLookupStrategy;
import put.medicallocator.ui.overlay.utils.FacilityTapListener;
import android.graphics.drawable.Drawable;

public class FaciltiesOverlayFactory {

    public static FacilitiesOverlay createOverlay(List<Facility> source,
            Drawable drawable, FacilityTapListener listener) {
        return new FacilitiesOverlay(source, drawable, new BasicFacilityLookupStrategy(source), listener);
    }

}
