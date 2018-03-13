package com.kk.popularmovies;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoviePostersFragment extends Fragment implements MoviesAdapter.MoviesAdapterOnClickHandler {

    private static final String EXTRA_MOVIES = "com.kk.popularmovies.extra_movies";

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
        setHasOptionsMenu(true);
        prepareRecyclerView();
        showMoviesDataView();
        if (savedInstanceState != null) {
            @SuppressWarnings("unchecked")
            List<Movie> movies = (List<Movie>) savedInstanceState.getSerializable(EXTRA_MOVIES);
            showMoviesOrError(movies);
        } else {
            loadMoviesData();
        }
        return rootView;
    }

    private void showMoviesOrError(List<Movie> movies) {
        if (movies != null) {
            showMoviesDataView();
            mMoviesAdapter.setMoviesData(movies);
        } else {
            showErrorMessage();
        }
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
        new FetchMoviesAsyncTask().execute(mSortOrder);
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(EXTRA_MOVIES, mMoviesAdapter.getMovies() != null ? new ArrayList<>(mMoviesAdapter.getMovies()) : null);
        super.onSaveInstanceState(outState);
    }

    private class FetchMoviesAsyncTask extends AsyncTask<SortOrder, Void, Movie[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Movie[] doInBackground(SortOrder... sortOrders) {
            Movie[] movies = null;
            String api_key = getResources().getString(R.string.API_KEY_TMDB);
            URL moviesRequestUrl = NetworkUtils.buildUrl(sortOrders[0], api_key);
            try {
                String jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                movies = JsonUtils.getMoviesFromJson(jsonMoviesResponse);
            } catch (Exception e) {
                e.printStackTrace();
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
                showErrorMessage();
            }
        }
    }
}
