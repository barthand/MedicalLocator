package put.medicallocator.ui;

import java.util.Map;

import put.medicallocator.R;
import put.medicallocator.io.Facility;
import put.medicallocator.io.IFacilityProvider.AsyncQueryListener;
import put.medicallocator.io.IFacilityProviderManager;
import put.medicallocator.io.sqlite.TypeConverter;
import put.medicallocator.ui.utils.FacilityDialogUtils;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ActivitySearchable extends ListActivity implements AsyncQueryListener {
	
	private static final String TAG = ActivitySearchable.class.getName();
	
	private String query;
	private Map<String, Integer> columnMapping;
	private long start, stop;
	
	private ProgressBar progressBar;
	private TextView infoTextView;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        
        progressBar = (ProgressBar) findViewById(R.id.search_progressbar);
        infoTextView = (TextView) findViewById(R.id.search_textview);
        
        /* Initialize the IFacilityProvider */
		if (!IFacilityProviderManager.isInitialized()) {
			IFacilityProviderManager.getInstance(this);
		}
        
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
        	query = intent.getStringExtra(SearchManager.QUERY);
        	doSearch(query);
        }
        
    }

	private void doSearch(final String query) {
		Log.d(TAG, "Performing search with query: " + query);
		start = System.currentTimeMillis();
		
		// Execute the query asynchronously
		new Thread(new Runnable() {
			
			public void run() {
				try {
					IFacilityProviderManager
						.getInstance(ActivitySearchable.this)
						.getFacilities(ActivitySearchable.this, query);
				} catch (Exception e) {
					// It shouldn't happen - even if the query just won't be executed.
				}
			}
		}).start();
	}

	public void onQueryComplete(int token, final Cursor cursor,
			final Map<String, Integer> columnMapping) {
		stop = System.currentTimeMillis();
		Log.d(TAG, "Query took " + (long) (stop-start) + " ms, returned rows: " + cursor.getCount());

		Runnable runnable;
		if (!cursor.moveToFirst()) {
			/* Result set is empty */
			runnable = new Runnable() {
				
				public void run() {
					final String format = getResources().getString(R.string.activitysearch_noresults);
					progressBar.setVisibility(View.GONE);
					infoTextView.setText(String.format(format, query));
				}
			};
		} else {
			// We have the results, so just populate the ListView now!
			this.columnMapping = columnMapping;
		
			runnable = new Runnable() {
			
				public void run() {
					final ListActivity activity = ActivitySearchable.this;
					final View header = getLayoutInflater().inflate(R.layout.list_view_header, null);
					final TextView headerTextView = (TextView) header.findViewById(android.R.id.text1);
					final String format = getResources().getString(R.string.activitysearch_returnedrows);
					headerTextView.setText(String.format(format, query, cursor.getCount()));
					activity.getListView().addHeaderView(header);
					activity.setListAdapter(new SearchCursorAdapter(activity, cursor, columnMapping));
					activity.startManagingCursor(cursor);
				}
			};
		}
		runOnUiThread(runnable);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// Get the cursor for selected item
		Cursor cursor = (Cursor) l.getItemAtPosition(position);
		Facility facility = 
			TypeConverter.getFacilityFromCursorCurrentPosition(columnMapping, cursor); 

		final LayoutInflater inflater = getLayoutInflater();

		final FacilityDialogUtils dialogUtils = 
			new FacilityDialogUtils(this, facility, inflater);
		final AlertDialog dialog = dialogUtils.createFacilityDialog(null);
		dialog.show();
	}
	
	private class SearchCursorAdapter extends CursorAdapter {
		
		private Map<String, Integer> columnMapping;
		
		public SearchCursorAdapter(Context context, Cursor c, Map<String, Integer> columnMapping) {
			super(context, c);
			this.columnMapping = columnMapping;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final String name = cursor.getString(columnMapping.get(Facility.Columns.NAME));
			final String address = cursor.getString(columnMapping.get(Facility.Columns.ADDRESS));
			
			final TextView headerTextView = (TextView) view.findViewById(android.R.id.text1);
			final TextView footerTextView = (TextView) view.findViewById(android.R.id.text2);
			
			if (name != null)
				headerTextView.setText(name);
			if (address != null)
				footerTextView.setText(address);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return getLayoutInflater().inflate(R.layout.list_item_search, parent, false);
		}
	};
   
}