package put.medicallocator.ui.intent;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import put.medicallocator.R;
import put.medicallocator.io.model.Facility;

/**
 * {@link IntentHandler}, used internally, to show the bubble-view for selected
 * {@link Facility} when the 'show on map' feature is selected for it on the results list.
 */
public class ShowBubbleIntentHandler implements IntentHandler {

    // TODO: Check behaviour with multiple bubble views.

    /**
     * Indicates the action name for the intent produced by the {@link ShowBubbleIntentHandler}.
     */
    public static final String INTENT_ACTION = "SHOW_BUBBLE";

    /**
     * Indicates the zoom level of the {@link MapView} when {@link Facility} is shown on the map.
     */
    private static final int ZOOM_LEVEL_FOR_TARGET_FACILITY = 18;

    /**
     * An identifier for the {@link Parcelable} object, associated with the {@link Intent}.
     */
    private static final String FACILITY_PARCELABLE = "SHOW_BUBBLE_COORDS";

    private final LayoutInflater inflater;

    private Facility facility;
    private MapView mapView;

    /**
     * Indicates whether this {@link ShowBubbleIntentHandler} should retain its state.
     * It depends on whether the bubble view is still visible to the user.
     */
    private boolean retainState = true;

    public ShowBubbleIntentHandler(Context context, MapView mapView) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mapView = mapView;
    }

    @Override
    public boolean supports(Intent intent) {
        return INTENT_ACTION.equals(intent.getAction());
    }

    @Override
    public void process(Intent intent) {
        if (!supports(intent)) {
            return;
        }

        this.facility = getFacilityFromIntent(intent);

        final View bubbleView = inflateBubbleView(false);
        addViewToTheMap(bubbleView);

        adjustCameraOnMapViewAndAnimate(bubbleView);
    }

    @Override
    public Object retainState() {
        if (retainState) {
            return facility;
        }
        return null;
    }

    @Override
    public void restoreState(Object object) {
        if (object instanceof Facility) {
            this.facility = (Facility) object;
            final View bubbleView = inflateBubbleView(true);
            addViewToTheMap(bubbleView);
        }
    }

    /**
     * Controls how the {@link MapView} presents the provided {@code bubbleView}.
     * Animates the camera to the associated {@link Facility} and animates the bubble view itself.
     */
    private void adjustCameraOnMapViewAndAnimate(final View bubbleView) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final ViewPropertyAnimator animator = ViewPropertyAnimator.animate(bubbleView);
                ViewHelper.setAlpha(bubbleView, 0f);
                ViewHelper.setScaleX(bubbleView, 0f);
                ViewHelper.setScaleY(bubbleView, 0f);
                bubbleView.setVisibility(View.VISIBLE);
                animator.alpha(1f).scaleX(1f).scaleY(1f).start();
            }
        };

        mapView.getController().setZoom(ZOOM_LEVEL_FOR_TARGET_FACILITY);
        mapView.getController().animateTo(facility.getLocation(), runnable);
    }

    /**
     * Restores the {@link Facility} from the provided {@link Intent}.
     */
    private Facility getFacilityFromIntent(Intent intent) {
        final FacilityParcelable parcelable = intent.getParcelableExtra(FACILITY_PARCELABLE);
        return parcelable.facility;
    }

    /**
     * Creates the bubble view and populates its data with currently associated {@link Facility}.
     */
    private View inflateBubbleView(boolean initiallyVisible) {
        final View bubbleView = inflater.inflate(R.layout.bubble_view, mapView, false);
        ((TextView)bubbleView.findViewById(R.id.bubble_title)).setText(facility.getName());
        ((TextView)bubbleView.findViewById(R.id.bubble_subtitle)).setText(facility.getAddress());
        bubbleView.findViewById(R.id.bubble_close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.removeView(bubbleView);
                retainState = false;
            }
        });
        if (!initiallyVisible) {
            bubbleView.setVisibility(View.GONE);
        }
        return bubbleView;
    }

    /**
     * Adds the provided view to the {@link MapView} at the location associated with the current
     * {@link Facility}.
     */
    private void addViewToTheMap(View bubbleView) {
        retainState = true;
        final MapView.LayoutParams params = new MapView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                facility.getLocation(), MapView.LayoutParams.BOTTOM_CENTER);
        mapView.addView(bubbleView, params);
    }

    /**
     * {@link Parcelable} for transferring the information about the {@link Facility}.
     */
    private static class FacilityParcelable implements Parcelable {

        private Facility facility;
        private FacilityParcelable(Facility facility) {
            this.facility = facility;
        }

        private FacilityParcelable(Parcel in) {
            this.facility = new Facility();
            final int[] coordsArray = in.createIntArray();
            facility.setLocation(new GeoPoint(coordsArray[0], coordsArray[1]));
            facility.setName(in.readString());
            facility.setAddress(in.readString());
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            final GeoPoint location = facility.getLocation();
            out.writeIntArray(new int[]{location.getLatitudeE6(), location.getLongitudeE6()});
            out.writeString(facility.getName());
            out.writeString(facility.getAddress());
        }

        public static final Parcelable.Creator<FacilityParcelable> CREATOR
                = new Parcelable.Creator<FacilityParcelable>() {
            public FacilityParcelable createFromParcel(Parcel in) {
                return new FacilityParcelable(in);
            }

            public FacilityParcelable[] newArray(int size) {
                return new FacilityParcelable[size];
            }
        };

    }

    /**
     * Setups the provided {@link Intent} in a way that is going to be supported by {@link ShowBubbleIntentHandler}.
     */
    public static void setupIntent(Intent intent, Facility facility) {
        intent.setAction(INTENT_ACTION);
        intent.putExtra(FACILITY_PARCELABLE, new FacilityParcelable(facility));
    }

}
