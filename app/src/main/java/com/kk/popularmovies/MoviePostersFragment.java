package com.kk.popularmovies;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kk.popularmovies.model.Movie;
import com.kk.popularmovies.model.SortOrder;
import com.kk.popularmovies.utilities.JsonUtils;
import com.kk.popularmovies.utilities.NetworkUtils;

import java.net.URL;
import java.util.Arrays;

public class MoviePostersFragment extends Fragment
        implements MoviesAdapter.MoviesAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Movie[]> {

    private static final int MOVIE_POSTERS_LOADER_ID = 1000;
    private MoviesAdapter mMoviesAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorMessageDisplay;
    private SortOrder mSortOrder = SortOrder.POPULAR;

    public MoviePostersFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_posters, container, false);
        findViews(rootView);
        prepareRecyclerView();
        showMoviesDataView();
        setHasOptionsMenu(true);
        getActivity().getSupportLoaderManager().initLoader(MOVIE_POSTERS_LOADER_ID, null, this);
        return rootView;
    }

    private void findViews(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.rv_movies);
        mLoadingIndicator = rootView.findViewById(R.id.pb_loading_indicator);
        mErrorMessageDisplay = rootView.findViewById(R.id.tv_error_message_display);
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

    private void loadMoviesData() {
        showMoviesDataView();
        getActivity().getSupportLoaderManager().restartLoader(MOVIE_POSTERS_LOADER_ID, null, this);
    }

    private void showMoviesDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_order_item:
                mSortOrder = SortOrder.swap(mSortOrder);
                item.setTitle(mSortOrder.getStringRepresentation());
                loadMoviesData();
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

    @NonNull
    @Override
    public Loader<Movie[]> onCreateLoader(int id, @Nullable Bundle args) {
        return new MoviesAsyncTaskLoader(getActivity(), mLoadingIndicator, mSortOrder);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Movie[]> loader, Movie[] data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mMoviesAdapter.setMoviesData(Arrays.asList(data));
        if (data != null) {
            showMoviesDataView();
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Movie[]> loader) {
        // Don't needed
    }

    private static class MoviesAsyncTaskLoader extends AsyncTaskLoader<Movie[]> {
        private Movie[] mMovies = null;
        private ProgressBar mLoadingIndicator;
        private SortOrder mSortOrder;

        public MoviesAsyncTaskLoader(@NonNull Context context, ProgressBar loadingIndicator, SortOrder sortOrder) {
            super(context);
            mLoadingIndicator = loadingIndicator;
            mSortOrder = sortOrder;
        }

        @Override
        protected void onStartLoading() {
            if (mMovies != null) {
                deliverResult(mMovies);
            } else {
                mLoadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }
        }

        @Override
        public void deliverResult(@Nullable Movie[] data) {
            mMovies = data;
            super.deliverResult(data);
        }

        @Nullable
        @Override
        public Movie[] loadInBackground() {
            String api_key = getContext().getResources().getString(R.string.API_KEY_TMDB);
            URL moviesRequestUrl = NetworkUtils.buildUrl(mSortOrder, api_key);
            try {
                String jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                mMovies = JsonUtils.getMoviesFromJson(jsonMoviesResponse);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mMovies;
        }
    }

}
