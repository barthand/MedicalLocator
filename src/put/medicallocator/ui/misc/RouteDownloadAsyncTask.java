package put.medicallocator.ui.misc;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import put.medicallocator.R;
import put.medicallocator.io.route.base.AbstractRouteOverHttpProvider;
import put.medicallocator.io.route.base.RouteAsyncTask;
import put.medicallocator.io.route.model.RouteSpec;

/**
 * {@link AsyncTask} downloading the route in the background, providing callbacks to the UI thread.
 */
public class RouteDownloadAsyncTask extends RouteAsyncTask {

    public interface DownloadListener {
        void finished(boolean success);
    }

    private final Context context;
    private final RouteOverlayManager routeManager;
    private final DownloadListener listener;

    public RouteDownloadAsyncTask(Context context, AbstractRouteOverHttpProvider routeProvider, RouteOverlayManager routeManager, DownloadListener listener) {
        super(routeProvider);
        this.context = context;
        this.routeManager = routeManager;
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(RouteSpec routeSpec) {
        super.onPostExecute(routeSpec);
        if (routeSpec != null) {
            routeManager.showRoute(routeSpec);
            listener.finished(true);
        } else {
            final String text = context.getResources().
                    getString(R.string.dialogfacilitybubble_nointernetroute);
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            listener.finished(false);
        }
    }

}

