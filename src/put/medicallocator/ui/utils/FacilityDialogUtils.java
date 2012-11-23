package put.medicallocator.ui.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import put.medicallocator.R;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.route.RoadProvider;
import put.medicallocator.ui.ActivityMain.RouteHandler;
import put.medicallocator.utils.MyLog;
import put.medicallocator.utils.StringUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class FacilityDialogUtils {
	private static final String TAG = FacilityDialogUtils.class.getName();

	private final Context context;
	private final Facility facility;
	private final LayoutInflater inflater;
	private RouteHandler handler;
	private AlertDialog dialog; 
	
	public FacilityDialogUtils(Context context, Facility facility, LayoutInflater inflater) {
		this.context = context;
		this.facility = facility;
		this.inflater = inflater;
	}
	
	public AlertDialog createFacilityDialog(RouteHandler handler) {
		this.handler = handler;
		
		// Create, inflate and populate the dialog layout.
		View layout = inflater.inflate(R.layout.dialog_facility_bubble, null);
		setUIProperties(layout);
		
		// Create the Dialog itself.
		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(layout);
		final AlertDialog dialog = builder.create();
		dialog.setTitle(context.getString(R.string.dialogfacilitybubble_title));

		this.dialog = dialog;
		
		return dialog;
	}
	
	private void setUIProperties(View layout) {
//	    final ViewGroup phoneContainer = (ViewGroup) layout.findViewById(R.id.phone_container);
	    final ViewGroup emailContainer = (ViewGroup) layout.findViewById(R.id.email_container);
	    final ViewGroup webpageContainer = (ViewGroup) layout.findViewById(R.id.webpage_container);
	    
		// Set the primary information
		final TextView nameTextView = (TextView) layout.findViewById(R.id.name_textview);
		final TextView addressTextView = (TextView) layout.findViewById(R.id.address_textview); 
		final TextView phoneTextView = (TextView) layout.findViewById(R.id.phone_textview); 
		final TextView emailTextView = (TextView) layout.findViewById(R.id.email_textview);
		final TextView homepageTextView = (TextView) layout.findViewById(R.id.homepage_textview);

		nameTextView.setText(facility.getName());
		addressTextView.setText(facility.getAddress());
		phoneTextView.setText(facility.getPhone());
		emailTextView.setText(facility.getEmail());
		homepageTextView.setText(facility.getHomepage());
		
		Linkify.addLinks(phoneTextView, Linkify.PHONE_NUMBERS);
		Linkify.addLinks(emailTextView, Linkify.EMAIL_ADDRESSES);
		Linkify.addLinks(homepageTextView, Linkify.WEB_URLS);
		emailTextView.setMovementMethod(LinkMovementMethod.getInstance());
		homepageTextView.setMovementMethod(LinkMovementMethod.getInstance());
		
		final ImageButton callButton = (ImageButton) layout.findViewById(R.id.call_button);
		final ImageButton emailButton = (ImageButton) layout.findViewById(R.id.email_button);
		final ImageButton webpageButton = (ImageButton) layout.findViewById(R.id.www_button);
		final ImageButton routeButton = (ImageButton) layout.findViewById(R.id.route_button);

		callButton.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				try {
				   Intent intent = new Intent(Intent.ACTION_DIAL);
				   intent.setData(Uri.parse("tel:" + facility.getPhone()));
				   context.startActivity(intent);
				} catch (Exception e) {
				   MyLog.e(TAG, "Failed to invoke call", e);
				}
			}
		});
		
		/* Do the appropriate UI actions if the e-mail exists */
		if (!StringUtils.isEmpty(facility.getEmail())) {
			emailButton.setOnClickListener(new OnClickListener() {
				
				@Override
                public void onClick(View v) {
					final Intent emailIntent = new Intent(Intent.ACTION_SEND);
					final String chooserMessage = context.getResources()
					        .getString(R.string.dialogfacilitybubble_email_app_chooser);
					emailIntent.setType("plain/text");
					emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { facility.getEmail() });
					context.startActivity(Intent.createChooser(emailIntent, chooserMessage));
				}
			});
			emailContainer.setVisibility(View.VISIBLE);
		} else {
		    emailContainer.setVisibility(View.GONE);
		}
		
		/* Do the appropriate UI actions if the webpage exists */
		String webpage = facility.getHomepage();
		if (!StringUtils.isEmpty(webpage)) {
			// We have to ensure that proper protocol is specified to make Intent working.
			if (!webpage.startsWith("http")) {
				// If http was omitted at all, assume that http and not https should be used.
				webpage = "http://" + webpage;
			}
			final String webpageAddress = webpage;
			webpageButton.setOnClickListener(new OnClickListener() {
				
				@Override
                public void onClick(View v) {
					Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webpageAddress));
					context.startActivity(webIntent);
				}
			});
			webpageContainer.setVisibility(View.VISIBLE);
		} else {
            webpageContainer.setVisibility(View.GONE);
		}
		
		if (handler != null && handler.getCurrentLocation() != null && isConnectionAvailable()) {
			routeButton.setEnabled(true);
			routeButton.setOnClickListener(new OnClickListener() {
				
				@Override
                public void onClick(View v) {
					double fromLat = handler.getCurrentLocation().getLatitudeE6() / 1E6;
					double fromLong = handler.getCurrentLocation().getLongitudeE6() / 1E6;
					double toLat = facility.getLocation().getLatitudeE6() / 1E6;
					double toLong = facility.getLocation().getLongitudeE6() / 1E6;

					final String url = RoadProvider.getUrl(fromLat, fromLong, toLat, toLong);
					final InputStream is = getInputStreamFromURLConnection(url);
					if (is != null) {
						handler.setRoute(RoadProvider.getRoute(is));
						handler.sendEmptyMessage(0);
						if (dialog != null) { 
							dialog.dismiss();
						}
					} else {
						final String text = context.getResources().
							getString(R.string.dialogfacilitybubble_nointernetroute);
						Toast.makeText(context, text, Toast.LENGTH_SHORT).show(); 
					}
				}
			});
		} else {
			routeButton.setEnabled(false);
		}
	}

    private boolean isConnectionAvailable() {
        final ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
		return conMgr.getActiveNetworkInfo() != null 
		        && conMgr.getActiveNetworkInfo().isAvailable() 
		        && conMgr.getActiveNetworkInfo().isConnected();
    }
	
	private static InputStream getInputStreamFromURLConnection(String url) {
        InputStream is = null;
        try {
                URLConnection conn = new URL(url).openConnection();
                is = conn.getInputStream();
        } catch (MalformedURLException e) {
                // Shouldn't happen.
        } catch (IOException e) {
                // No internet connection available.
        }
        return is;
	}
	
}
