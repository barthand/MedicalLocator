package put.medicallocator.ui.async;

import android.os.AsyncTask;
import put.medicallocator.io.helper.DataSourceConfigurator;

/**
 * {@link AsyncTask>} for the purpose of the {@link DataSourceConfigurator} initialization (which may take time).
 */
public class DataSourceConfigurerAsyncTask extends AsyncTask<Void, Void, Boolean> {

    /**
     * Associated {@link DataSourceConfigurator} instance.
     */
	private final DataSourceConfigurator helper;

    /**
     * Associated {@link DataSourceInitializerListener} instance.
     */
	private DataSourceInitializerListener listener;

	public DataSourceConfigurerAsyncTask(DataSourceConfigurator helper, DataSourceInitializerListener listener) {
		this.helper = helper;
		this.listener = listener;
	}

	public void setListener(DataSourceInitializerListener listener) {
		synchronized (this) {
			this.listener = listener;
		}
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		return helper.firstInitialization();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		synchronized (this) {
			listener.onDatabaseInitialized(result);
		}
	}

	public interface DataSourceInitializerListener {
		void onDatabaseInitialized(boolean success);
	}
}