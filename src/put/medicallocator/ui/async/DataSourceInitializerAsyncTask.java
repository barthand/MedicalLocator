package put.medicallocator.ui.async;

import put.medicallocator.io.helper.FacilityDAOHelper;
import android.os.AsyncTask;

public class DataSourceInitializerAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private final FacilityDAOHelper helper;
	private DataSourceInitializerListener listener;

	public DataSourceInitializerAsyncTask(FacilityDAOHelper helper, DataSourceInitializerListener listener) {
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
		return helper.prepareForFirstRun();
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