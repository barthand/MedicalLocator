package put.medicallocator.ui.async;

import put.medicallocator.io.FacilityDAOHelper;
import android.os.AsyncTask;

public class DAOInitializerAsyncTask extends AsyncTask<Void, Void, Boolean> {

	private final FacilityDAOHelper helper;
	private DAOInitializerListener listener;

	public DAOInitializerAsyncTask(FacilityDAOHelper helper, DAOInitializerListener listener) {
		this.helper = helper;
		this.listener = listener;
	}

	public void setListener(DAOInitializerListener listener) {
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
			listener.onDatabaseIniitialized(result);
		}
	}

	public interface DAOInitializerListener {
		void onDatabaseIniitialized(boolean success);
	}
}