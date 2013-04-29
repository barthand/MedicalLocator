package put.medicallocator.ui.overlay.utils;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import put.medicallocator.io.model.Facility;

/**
 * Interface describing contract for components which provide searching capabilities through {@link Facility} dataset.
 */
public interface FacilityLookupStrategy {

    /**
     * Looks up for the nearest facility for the base provided in the {@code point}.
     */
    Facility findNearestFacility(GeoPoint point, Projection projection);
}