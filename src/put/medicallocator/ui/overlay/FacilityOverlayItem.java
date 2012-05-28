package put.medicallocator.ui.overlay;

import put.medicallocator.io.Facility;

import com.google.android.maps.OverlayItem;

public class FacilityOverlayItem extends OverlayItem {

	private Facility facility;

	public FacilityOverlayItem(Facility facility) {
		super(facility.getGeoPoint(), facility.getName(), facility.getAddress());
		this.facility = facility;
	}

	public Facility getFacility() {
		return facility;
	}

}
