package put.medicallocator.ui;

import java.util.List;
import java.util.Map;

import put.medicallocator.R;
import put.medicallocator.io.Facility;
import put.medicallocator.io.IFacilityDAO;
import put.medicallocator.io.sqlite.DatabaseFacilityDAO;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler.FacilityQueryExecutor;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler.FacilityQueryListener;
import put.medicallocator.ui.utils.FacilityDialogUtils;
import put.medicallocator.utils.MyLog;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ActivitySearchable extends ListActivity implements FacilityQueryListener {

	private static final String TAG = ActivitySearchable.class.getName();

	private String query;
	private long start, stop;

	private ProgressBar progressBar;
	private TextView infoTextView;

    private IFacilityDAO facilityDao;
    private AsyncFacilityWorkerHandler facilityWorker;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        facilityDao = new DatabaseFacilityDAO(getApplicationContext());
        facilityWorker = new AsyncFacilityWorkerHandler(this);

        progressBar = (ProgressBar) findViewById(R.id.search_progressbar);
        infoTextView = (TextView) findViewById(R.id.search_textview);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        	query = intent.getStringExtra(SearchManager.QUERY);
        	doSearch(query);
        }

    }

	private void doSearch(final String query) {
		MyLog.d(TAG, "Performing search with query: " + query);
		start = System.currentTimeMillis();

		facilityWorker.scheduleQuery(new FacilityQueryExecutor() {
			public List<Facility> execute() throws Exception {
				return facilityDao.findWithKeyword(query);
			}
		});
	}

	public void onQueryComplete(int token, final Cursor cursor, final Map<String, Integer> columnMapping) {
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final LayoutInflater inflater = getLayoutInflater();
		final Facility facility = (Facility) l.getItemAtPosition(position);

		final FacilityDialogUtils dialogUtils = new FacilityDialogUtils(this, facility, inflater);
		final AlertDialog dialog = dialogUtils.createFacilityDialog(null);
		dialog.show();
	}

	public void onAsyncFacilityQueryCompleted(List<Facility> result) {
		stop = System.currentTimeMillis();
		MyLog.d(TAG, "Query took " + (long) (stop-start) + " ms, returned rows: " + result.size());

		if (result.size() == 0) {
			/* Result set is empty */
			final String format = getResources().getString(R.string.activitysearch_noresults);
			progressBar.setVisibility(View.GONE);
			infoTextView.setText(String.format(format, query));
		} else {
			final View header = getLayoutInflater().inflate(R.layout.list_view_header, null);
			final TextView headerTextView = (TextView) header.findViewById(android.R.id.text1);
			final String format = getResources().getString(R.string.activitysearch_returnedrows);

			headerTextView.setText(String.format(format, query, result.size()));

			getListView().addHeaderView(header);
			setListAdapter(new SearchFacilityAdapter(this, result));
		}
	}

	private static class SearchFacilityAdapter extends ArrayAdapter<Facility> {

		public SearchFacilityAdapter(Context context, List<Facility> source) {
			super(context, android.R.layout.simple_list_item_1, source);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
		        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.list_item_search, parent, false);
			}

			final TextView headerTextView = (TextView) convertView.findViewById(android.R.id.text1);
			final TextView footerTextView = (TextView) convertView.findViewById(android.R.id.text2);

			final Facility facility = getItem(position);

			if (facility.getName() != null) {
				headerTextView.setText(facility.getName());
			}
			if (facility.getAddress() != null) {
				footerTextView.setText(facility.getAddress());
			}

			return convertView;
		}

	}



}