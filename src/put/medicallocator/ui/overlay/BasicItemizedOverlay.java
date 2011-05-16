package put.medicallocator.ui.overlay;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class BasicItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	protected Context context;
	private ArrayList<FacilityOverlayItem> overlays = new ArrayList<FacilityOverlayItem>();

	public BasicItemizedOverlay(Drawable drawable) {
		super(boundCenterBottom(drawable));
	}
	
	public BasicItemizedOverlay(Context context, Drawable drawable) {
		this(drawable);
		this.context = context;
	}

	public void addOverlay(FacilityOverlayItem overlayItem) {
		overlays.add(overlayItem);
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}

	public void populateNow() {
		populate();
	}
	
	@Override
	public int size() {
		return overlays.size();
	}

	@Override
	protected boolean onTap(int index) {
		FacilityOverlayItem item = overlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getFacility().getAddress());
		dialog.show();
		return true;
	}

}
