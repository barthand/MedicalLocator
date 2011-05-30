package put.medicallocator.ui.utils;

import put.medicallocator.R;
import put.medicallocator.io.Facility;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FacilityDialogUtils {
	private static final String TAG = FacilityDialogUtils.class.getName();

	public static AlertDialog createFacilityDialog(Context context, LayoutInflater inflater, Facility facility) {
		// Create, inflate and populate the dialog layout.
		View layout = inflater.inflate(R.layout.dialog_facility_bubble, null);
		FacilityDialogUtils.setUIProperties(context, layout, facility);
		
		// Create the Dialog itself.
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(layout);
		final AlertDialog dialog = builder.create();
		dialog.setTitle(context.getString(R.string.dialogfacilitybubble_title));
		return dialog;
	}
	
	private static void setUIProperties(final Context context, View layout, final Facility facility) {
		// Set the primary information
		final TextView nameTextView = (TextView) layout.findViewById(R.id.name_textview);
		final TextView addressTextView = (TextView) layout.findViewById(R.id.address_textview); 
		final TextView phoneTextView = (TextView) layout.findViewById(R.id.phone_textview); 
		final TextView emailTextView = (TextView) layout.findViewById(R.id.email_textview); 

		nameTextView.setText(facility.getName());
		addressTextView.setText(facility.getAddress());
		phoneTextView.setText(facility.getPhone());
		emailTextView.setText(facility.getEmail());
		
		// The trick here is that in the e-mail field, we may have WWW page as well.
		String email = null;
		String webpage = null;
		final Button callButton = (Button) layout.findViewById(R.id.call_button);
		final Button emailButton = (Button) layout.findViewById(R.id.email_button); 
		final Button webpageButton = (Button) layout.findViewById(R.id.www_button); 

		/* Check for e-mail and webpage */
		final String facilityEmail = facility.getEmail();
		if (facilityEmail != null) {
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
		}
		
		callButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
				   Intent intent = new Intent(Intent.ACTION_CALL);
				   intent.setData(Uri.parse("tel:" + facility.getPhone()));
				   context.startActivity(intent);
				} catch (Exception e) {
				   Log.e(TAG, "Failed to invoke call", e);
				}
			}
		});
		
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
