package put.medicallocator.ui.utils;

import put.medicallocator.R;
import put.medicallocator.io.Facility;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FacilityDialogUtils {
	private static final String TAG = FacilityDialogUtils.class.getName();

	public static void setUIProperties(final Context context, View layout, final Facility facility) {
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
