package put.medicallocator.ui.overlay;

import java.util.ArrayList;

import put.medicallocator.R;
import put.medicallocator.io.Facility;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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

		final TextView addressTextView = (TextView) layout.findViewById(R.id.address_textview); 
		final TextView phoneTextView = (TextView) layout.findViewById(R.id.phone_textview); 
		final TextView emailTextView = (TextView) layout.findViewById(R.id.email_textview); 

		addressTextView.setText(item.getFacility().getAddress());
		phoneTextView.setText(item.getFacility().getPhone());
		emailTextView.setText(item.getFacility().getEmail());
		
		setUIProperties(layout, item.getFacility());
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(layout);
		final AlertDialog dialog = builder.create();
		dialog.setTitle(item.getFacility().getName());
		dialog.show();
		
		return true;
	}
	
	private void setUIProperties(View layout, Facility facility) {
		// The trick here is that in the e-mail field, we may have WWW page as well.
		String email = null;
		String webpage = null;
		final Button emailButton = (Button) layout.findViewById(R.id.email_button); 
		final Button webpageButton = (Button) layout.findViewById(R.id.www_button); 

		/* Check for e-mail and webpage */
		final String facilityEmail = facility.getEmail();
		String[] tokens = facilityEmail.split(" ");
		for (int i=0; i<tokens.length; i++) {
			if (tokens[i].contains("@")) {
				// We have e-mail address.
				email = tokens[i];
			} else {
				// We have webpage, check if matches pattern.
				if (tokens[i].matches(".+\\..+"))
					webpage = tokens[i];
			}
		}
		
		/* Do the appropriate UI actions if the e-mail exists */
		if (email != null) {
			final String emailAddress = email;
			emailButton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					final Intent emailIntent = new Intent(Intent.ACTION_SEND);
					final String chooserMessage = context.getResources()
							.getString(R.string.dialogfacilitybubble_email_app_chooser);
					emailIntent.setType("plain/text");
					emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { emailAddress });
					context.startActivity(
							Intent.createChooser(emailIntent, chooserMessage));
				}
			});
			emailButton.setVisibility(View.VISIBLE);
		} else {
			emailButton.setVisibility(View.GONE);
		}
		
		/* Do the appropriate UI actions if the webpage exists */
		if (webpage != null) {
			// We have to ensure that proper protocol is specified to make Intent working.
			if (!webpage.startsWith("http")) {
				// If http was omitted at all, assume that http and not https should be used.
				webpage = "http://" + webpage;
			}
			final String webpageAddress = webpage;
			webpageButton.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					Intent webIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(webpageAddress));
					context.startActivity(webIntent);
				}
			});
			webpageButton.setVisibility(View.VISIBLE);
		} else {
			webpageButton.setVisibility(View.GONE);			
		}
	}

}
