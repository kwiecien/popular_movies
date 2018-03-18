package com.kk.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kk.popularmovies.data.MovieDbHelper;
import com.kk.popularmovies.model.Movie;
import com.kk.popularmovies.model.SortOrder;
import com.kk.popularmovies.utilities.JsonUtils;
import com.kk.popularmovies.utilities.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoviePostersFragment extends Fragment
        implements MoviesAdapter.MoviesAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String EXTRA_MOVIES = "com.kk.popularmovies.extra_movies";
    private static final int ID_MOVIE_LOADER = 100;
    private MoviesAdapter mMoviesAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessage;
    private SortOrder mSortOrder;

    public MoviePostersFragment() {
        // Default constructor to suppress lint
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_posters, container, false);
        findViews(rootView);
        setHasOptionsMenu(true);
        prepareRecyclerView();
        showMoviesDataView();
        SortOrder defaultSortOrder = retrieveDefaultSortOrder();
        mSortOrder = defaultSortOrder;
        if (savedInstanceState != null) {
            @SuppressWarnings("unchecked")
            List<Movie> movies = (List<Movie>) savedInstanceState.getSerializable(EXTRA_MOVIES);
            showMoviesOrError(movies);
        } else {
            loadMoviesData(defaultSortOrder);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        if (mSortOrder == SortOrder.FAVORITES) {
            loadMoviesDataFromDatabase();
            mMoviesAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    private void findViews(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.rv_movies);
        mLoadingIndicator = rootView.findViewById(R.id.pb_loading_indicator);
        mErrorMessage = rootView.findViewById(R.id.tv_error_message);
    }

    private void prepareRecyclerView() {
        int spanCount = calculateNumberOfColumns(getActivity());
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMoviesAdapter = new MoviesAdapter(this, getContext());
        mRecyclerView.setAdapter(mMoviesAdapter);
    }

    private int calculateNumberOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingDpFactor = 185;
        int numberOfColumns = (int) (dpWidth / scalingDpFactor);
        return numberOfColumns > 2 ? numberOfColumns : 2;
    }

    private void showMoviesDataView() {
        mErrorMessage.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private SortOrder retrieveDefaultSortOrder() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultSortOrder = preferences.getString(getResources().getString(R.string.sort_order_key), "");
        return defaultSortOrder.equals(getResources().getString(R.string.pref_sort_popular)) ?
                SortOrder.POPULAR :
                SortOrder.TOP_RATED;
    }

    private void loadMoviesData(SortOrder sortOrder) {
        if (sortOrder == SortOrder.FAVORITES) {
            loadMoviesDataFromDatabase();
        } else {
            loadMoviesDataFromInternet(sortOrder);
        }
    }

    private void loadMoviesDataFromDatabase() {
        Cursor moviesCursor = MovieDbHelper.findFavoriteMovies(getContext());
        showMoviesOrError(MovieDbHelper.getFavoriteMoviesAsList(moviesCursor));
    }

    private void loadMoviesDataFromInternet(SortOrder sortOrder) {
        new FetchMoviesAsyncTask().execute(sortOrder);
    }

    private void showMoviesOrError(List<Movie> movies) {
        if (movies == null) {
            showInternetErrorMessage();
        } else if (movies.isEmpty()) {
            showNoFavoritesErrorMessage();
        } else {
            mMoviesAdapter.setMoviesData(movies);
            showMoviesDataView();
        }
    }

    private void showNoFavoritesErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText(getText(R.string.error_no_favorites));
    }

    private void showInternetErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText(getText(R.string.error_no_internet));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_order_item:
                mSortOrder = SortOrder.swap(mSortOrder);
                item.setTitle(mSortOrder.getStringRepresentation());
                loadMoviesData(mSortOrder);
                return true;
            case R.id.action_settings:
                startActivity(SettingsActivity.newIntent(getActivity()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(Movie movie, ImageView sharedImageView) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(), sharedImageView, ViewCompat.getTransitionName(sharedImageView));
        startActivity(MovieDetailsActivity.newIntent(getActivity(), movie), options.toBundle());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(EXTRA_MOVIES, mMoviesAdapter.getMovies() != null ? new ArrayList<>(mMoviesAdapter.getMovies()) : null);
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle args) {
        /*switch (loaderId) {
            case ID_MOVIE_LOADER:
                Uri movie
        }*/
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    private class FetchMoviesAsyncTask extends AsyncTask<SortOrder, Void, Movie[]> {

        private final String TAG = FetchMoviesAsyncTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Movie[] doInBackground(SortOrder... sortOrders) {
            Movie[] movies = null;
            String apiKey = getResources().getString(R.string.API_KEY_TMDB);
            URL moviesRequestUrl = NetworkUtils.buildMoviesUrl(sortOrders[0], apiKey);
            try {
                String jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                movies = JsonUtils.getMoviesFromJson(jsonMoviesResponse);
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            return movies;
        }

        @Override
        protected void onPostExecute(Movie[] movies) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movies != null) {
                showMoviesDataView();
                mMoviesAdapter.setMoviesData(Arrays.asList(movies));
            } else {
                showInternetErrorMessage();
            }
        }
    }
}
