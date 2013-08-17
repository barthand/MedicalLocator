package put.medicallocator.ui.misc;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;
import put.medicallocator.R;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.route.base.AbstractRouteOverHttpProvider;
import put.medicallocator.io.route.base.RouteAsyncTask;
import put.medicallocator.io.route.model.RouteSpec;
import put.medicallocator.ui.ActivityMain;
import put.medicallocator.ui.intent.ShowRouteIntentHandler;

/**
 * {@link AsyncTask} downloading the route in the background, providing callbacks to the UI thread.
 */
public class RouteDownloadAsyncTask extends RouteAsyncTask {

    private final Context context;

    private final Facility targetFacility;

    public RouteDownloadAsyncTask(Context context, AbstractRouteOverHttpProvider routeProvider, Facility targetFacility) {
        super(routeProvider);
        this.context = context;
        this.targetFacility = targetFacility;
    }

    @Override
    protected void onPostExecute(RouteSpec routeSpec) {
        super.onPostExecute(routeSpec);
        if (routeSpec != null) {
            final Intent intent = new Intent(context, ActivityMain.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            ShowRouteIntentHandler.setupIntent(intent, routeSpec, targetFacility.getId());
            context.startActivity(intent);
        } else {
            final String text = context.getResources().
                    getString(R.string.dialogfacilitybubble_nointernetroute);
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

}

