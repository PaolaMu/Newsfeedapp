package com.example.android.newsfeedapp;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {

    private static final String LOG_TAG = NewsActivity.class.getName();

    /**
     * URL for news data from the Guardian data set
     */
    private static final String THE_GUARDIAN_REQUEST_URL =
            "https://content.guardianapis.com/search?show-fields=all&api-key=8f8eb0c2-284a-4b8a-9a7e-8ec218855867";

    private static final int NEWS_LOADER_ID = 1;

    /**
     * Adapter for the list of news
     */
    private NewsAdapter mAdapter;

    private LoaderManager loaderManager;

    private ProgressBar loadingIndicator;

    /**
     * TextView that is displayed when the list is empty
     */
    private TextView mEmptyStateTextView;
    private String thisDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_activity);

        // Find a reference to the {@link ListView} in the layout
        ListView newsListView = (ListView) findViewById(R.id.list);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes an empty list of news as input
        mAdapter = new NewsAdapter(this, new ArrayList<News>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        newsListView.setAdapter(mAdapter);

        // Obtain a reference to the SharedPreferences file for this app
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);


        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected news.
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current news that was clicked on
                News currentNews = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri newsUri = Uri.parse(currentNews.getUrl());

                // Create a new intent to view the news URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            loaderManager = getLoaderManager();

            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String startDate = sharedPrefs.getString(
                getString(R.string.settings_start_date_key),
                getString(R.string.settings_start_date_default));
        String endDate = sharedPrefs.getString(
                getString(R.string.settings_end_date_key),
                getThisDate());
        Boolean todayValue = sharedPrefs.getBoolean(
                getString(R.string.settings_today_key),
                Boolean.valueOf(getString(R.string.settings_today_default)));
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        Uri baseUri = Uri.parse(THE_GUARDIAN_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        if (!startDate.equals("")) {
            uriBuilder.appendQueryParameter("from-date", startDate);
        }
        if (todayValue) {
            uriBuilder.appendQueryParameter("to-date", getThisDate());
        } else {
            uriBuilder.appendQueryParameter("to-date", endDate);
        }
        uriBuilder.appendQueryParameter("orderBy", orderBy);

        Log.e(LOG_TAG, uriBuilder.toString());
        return new NewsLoader(this, uriBuilder.toString());

    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        // Hide loading indicator because the data has been loaded
        loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No news found."
        mEmptyStateTextView.setText(R.string.no_news);

        /* Clear previous data from adapter */
        mAdapter.clear();

        // If there is a valid list of {@link news}, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (news != null && !news.isEmpty()) {
            //mAdapter.addAll(news)
            mAdapter.addAll(news);
        } else {
            if (hasConnection()) {

                // Set empty text to display no news found
                mEmptyStateTextView.setText(R.string.no_news);
            } else {
                // Update empty state with no connection error
                mEmptyStateTextView.setText(R.string.no_internet_connection);
            }
        }
    }

    private boolean hasConnection() {
        // Get a reference to ConnectivityManager to check state of the network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on currently active default data network
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there's a connection, fetch data
        return networkInfo != null && networkInfo.isConnected();
    }

    public String getThisDate() {
        @SuppressLint("SimpleDateFormat") DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        return dateFormat.format(date);

    }

    public void onLoaderReset(Loader<List<News>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

