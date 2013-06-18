package put.medicallocator.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import put.medicallocator.R;
import put.medicallocator.application.Application;
import put.medicallocator.io.IFacilityDAO;
import put.medicallocator.io.model.Facility;
import put.medicallocator.io.sqlite.DatabaseFacilityDAO;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler.FacilityQueryExecutor;
import put.medicallocator.ui.async.AsyncFacilityWorkerHandler.FacilityQueryListener;
import put.medicallocator.ui.dialogs.FacilityDialogFactory;
import put.medicallocator.ui.intent.ShowBubbleIntentHandler;
import put.medicallocator.ui.utils.AlphabetArrayIndexer;
import put.medicallocator.utils.MyLog;

/**
 * Activity providing to the user {@link ListView} with {@link Facility} objects.
 */
public class ActivitySearchable extends SherlockListActivity implements FacilityQueryListener {

    private static final String TAG = ActivitySearchable.class.getName();

    private String query;
    private long start, stop;

    private ProgressBar progressBar;
    private TextView infoTextView;

    private IFacilityDAO facilityDao;
    private AsyncFacilityWorkerHandler facilityWorker;

    // TODO: Sorting by types (separators would be required), names?

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        facilityDao = new DatabaseFacilityDAO((Application) this.getApplication());
        facilityWorker = new AsyncFacilityWorkerHandler(this);

        progressBar = (ProgressBar) findViewById(R.id.search_progressbar);
        infoTextView = (TextView) findViewById(R.id.search_textview);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                final Intent homeIntent = new Intent(getApplicationContext(), ActivityMain.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(homeIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAsyncFacilityQueryStarted() {
        // Empty
    }

    @Override
    public void onAsyncFacilityQueryCompleted(List<Facility> result) {
        stop = System.currentTimeMillis();
        MyLog.d(TAG, "Query took " + (stop - start) + " ms, returned rows: " + result.size());

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
            Collections.sort(result, new Comparator<Facility>() {
                @Override
                public int compare(Facility lhs, Facility rhs) {
                    if (lhs == null) {
                        return 1;
                    }
                    if (rhs == null) {
                        return -1;
                    }
                    return lhs.getName().compareTo(rhs.getName());
                }
            });
            setListAdapter(new SearchFacilityAdapter(this, result));
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Facility facility = (Facility) l.getItemAtPosition(position);
        new FacilityDialogFactory(this, facility, null).createDialog(this).show();
    }

    private void doSearch(final String query) {
        MyLog.d(TAG, "Performing search with query: " + query);
        start = System.currentTimeMillis();

        facilityWorker.invokeAsyncQuery(new FacilityQueryExecutor() {
            @Override
            public List<Facility> execute() throws Exception {
                return facilityDao.findWithKeyword(query);
            }
        });
    }

    private static class SearchFacilityAdapter extends ArrayAdapter<Facility> implements SectionIndexer {

        // TODO: This makes me notice that DAO could offer data as both Lists and Cursors..
        // Could give a boost here..
        private final SectionIndexer indexer;

        private static class ViewHolder {
            private TextView headerTextView;
            private TextView footerTextView;
            private ImageButton showOnMapButton;
        }

        private final View.OnClickListener showOnMapClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Facility facility = (Facility) v.getTag();
                final Intent intent = new Intent(getContext(), ActivityMain.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                ShowBubbleIntentHandler.setupIntent(intent, facility);
                getContext().startActivity(intent);
            }
        };

        public SearchFacilityAdapter(Context context, List<Facility> source) {
            super(context, android.R.layout.simple_list_item_1, source);
            indexer = new AlphabetArrayIndexer<Facility>(source, new AlphabetArrayIndexer.IndexedValueReturner<Facility>() {
                @Override
                public String indexedValue(Facility value) {
                    return value.getName();
                }
            }, " ABCDEFGHIJKLMNOPQRSTUVWXYZ"); // TODO: Polish Alphabet?
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_search, parent, false);

                holder = new ViewHolder();
                holder.headerTextView = (TextView) convertView.findViewById(android.R.id.text1);
                holder.footerTextView = (TextView) convertView.findViewById(android.R.id.text2);
                holder.showOnMapButton = (ImageButton) convertView.findViewById(R.id.showOnMapButton);
                holder.showOnMapButton.setOnClickListener(showOnMapClickListener);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Facility facility = getItem(position);
            if (facility.getName() != null) {
                holder.headerTextView.setText(facility.getName());
            }
            if (facility.getAddress() != null) {
                holder.footerTextView.setText(facility.getAddress());
            }
            holder.showOnMapButton.setTag(facility);

            return convertView;
        }

        @Override
        public Object[] getSections() {
            return indexer.getSections();
        }

        @Override
        public int getPositionForSection(int section) {
            return indexer.getPositionForSection(section);
        }

        @Override
        public int getSectionForPosition(int position) {
            return indexer.getSectionForPosition(position);
        }
    }

}