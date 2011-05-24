package put.medicallocator.ui.overlay;

import java.util.ArrayList;

import put.medicallocator.R;
import put.medicallocator.ui.utils.FacilityDialogUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

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

		LayoutInflater inflater = 
			(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_facility_bubble, null);

		FacilityDialogUtils.setUIProperties(context, layout, item.getFacility());
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(layout);
		final AlertDialog dialog = builder.create();
		dialog.setTitle(context.getString(R.string.dialogfacilitybubble_title));
		dialog.show();
		
		return true;
	}
	


}
