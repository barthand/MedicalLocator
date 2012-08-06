package put.medicallocator.ui.overlay.utils;

import put.medicallocator.io.model.Facility;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

public interface FacilityLookupStrategy {

    /**
     * Looks up for the nearest facility for the base provided in the {@code point}.
     */
    Facility findNearestFacility(GeoPoint point, Projection projection);
}