package put.medicallocator.io.route.base;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import put.medicallocator.io.route.model.RouteSpec;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * {@link AsyncTask} which purpose is to download the route using the
 * provided {@link AbstractRouteOverHttpProvider} implementation.
 */
public abstract class RouteAsyncTask extends AsyncTask<Void, Void, RouteSpec> {

    private static final String TAG = RouteAsyncTask.class.getSimpleName();

    private final AbstractRouteOverHttpProvider routeProvider;

    protected RouteAsyncTask(AbstractRouteOverHttpProvider routeProvider) {
        this.routeProvider = routeProvider;
    }

    @Override
    protected RouteSpec doInBackground(Void... params) {
        InputStream is = null;
        try {
            final String url = routeProvider.buildUrl();
            Log.d(TAG, "Retrieving route from URL: " + url);
            is = getHttpInputStream(url);
            if (is != null) {
                return routeProvider.parseRoute(is);
            }
        } catch (IOException e) {
            // Catch and return null below.
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the data from provided {@code url} using internally build HTTP connection
     * (either {@link HttpURLConnection} for devices running {@link Build.VERSION_CODES#GINGERBREAD}+
     * or {@link DefaultHttpClient} otherwise).
     */
    protected InputStream getHttpInputStream(String url) throws IOException {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            final HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return urlConnection.getInputStream();
            }
            return null;
        } else {
            final HttpResponse response = new DefaultHttpClient().execute(new HttpGet(url));
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return response.getEntity().getContent();
            }
            return null;
        }
    }

}
