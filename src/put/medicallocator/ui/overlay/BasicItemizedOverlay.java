package put.medicallocator.ui.overlay;

import java.util.ArrayList;

import put.medicallocator.ui.ActivityMain.RouteHandler;
import put.medicallocator.ui.utils.FacilityDialogUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class BasicItemizedOverlay extends ItemizedOverlay<OverlayItem> {

	protected Context context;
	private ArrayList<FacilityOverlayItem> overlays = new ArrayList<FacilityOverlayItem>();
	private RouteHandler handler;

	public BasicItemizedOverlay(Drawable drawable) {
		super(boundCenterBottom(drawable));
	}

	public BasicItemizedOverlay(Context context, Drawable drawable, RouteHandler handler) {
		this(drawable);
		this.context = context;
		this.handler = handler;
	}

	public void addOverlay(FacilityOverlayItem overlayItem) {
		overlays.add(overlayItem);
	}

	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}

	public void onDataCollected() {
		populate();
	}

	@Override
	public int size() {
		return overlays.size();
	}



	@Override
	protected boolean onTap(int index) {
		FacilityOverlayItem item = overlays.get(index);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final FacilityDialogUtils dialogUtils = new FacilityDialogUtils(context, item.getFacility(), inflater);
		final AlertDialog dialog = dialogUtils.createFacilityDialog(handler);
		dialog.show();

		return true;
	}



}
