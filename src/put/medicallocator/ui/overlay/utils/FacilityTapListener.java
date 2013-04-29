package put.medicallocator.ui.overlay.utils;

import put.medicallocator.io.model.Facility;

/**
 * Simple listener providing callback for tapping on certain {@link Facility}.
 */
public interface FacilityTapListener {

    /**
     * This listener method is invoked when particular facility is selected.
     * The contract is that non-null facility should be provided to the target.
     */
    void onFacilityTap(Facility facility);
}