package com.kk.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kk.popularmovies.data.MovieContract;
import com.kk.popularmovies.enums.LoaderId;
import com.kk.popularmovies.model.Movie;
import com.kk.popularmovies.model.SortOrder;
import com.kk.popularmovies.utilities.JsonUtils;
import com.kk.popularmovies.utilities.MovieDbUtils;
import com.kk.popularmovies.utilities.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MoviePostersFragment extends Fragment implements
        MoviesAdapter.MoviesAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks {

    private static final String EXTRA_MOVIES = "com.kk.popularmovies.extra_movies";
    private static final String EXTRA_SORT_ORDER = "com.kk.popularmovies.extra_sort_order";
    private static final String EXTRA_RECYCLER_VIEW = "com.kk.popularmovies.extra_recycler_view_position";
    private static final int ID_FAVORITE_MOVIES_LOADER = LoaderId.MoviePosters.FAVORITE_MOVIES;
    private static final int ID_TOP_RATED_MOVIES_LOADER = LoaderId.MoviePosters.TOP_RATED_MOVIES;
    private static final int ID_POPULAR_MOVIES_LOADER = LoaderId.MoviePosters.POPULAR_MOVIES;
    private static final int REQUEST_CODE_ADAPTER_POSITION = 1;
    private MoviesAdapter mMoviesAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
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
        if (savedInstanceState != null) {
            @SuppressWarnings("unchecked")
            List<Movie> movies = (List<Movie>) savedInstanceState.getSerializable(EXTRA_MOVIES);
            mSortOrder = (SortOrder) savedInstanceState.getSerializable(EXTRA_SORT_ORDER);
            showMoviesOrError(movies);
            restoreRecyclerViewPosition(savedInstanceState);
        } else {
            mSortOrder = retrieveDefaultSortOrder();
            loadMoviesData();
        }
        return rootView;
    }

    private void restoreRecyclerViewPosition(@NonNull Bundle savedInstanceState) {
        Parcelable savedLoaderManagerState = savedInstanceState.getParcelable(EXTRA_RECYCLER_VIEW);
        mLayoutManager.onRestoreInstanceState(savedLoaderManagerState);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem sortOrderMenuItem = menu.findItem(R.id.sort_order_item);
        sortOrderMenuItem.setTitle(getString(mSortOrder.getResourceId()));
    }

    private void findViews(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.rv_movies);
        mLoadingIndicator = rootView.findViewById(R.id.pb_loading_indicator);
        mErrorMessage = rootView.findViewById(R.id.tv_error_message);
    }

    private void prepareRecyclerView() {
        int spanCount = calculateNumberOfColumns(getActivity());
        mLayoutManager = new GridLayoutManager(getActivity(), spanCount);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);

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
        if (defaultSortOrder.equals(getResources().getString(R.string.pref_sort_popular))) {
            return SortOrder.POPULAR;
        } else if (defaultSortOrder.equals(getResources().getString(R.string.pref_sort_top_rated))) {
            return SortOrder.TOP_RATED;
        } else {
            return SortOrder.FAVORITES;
        }
    }

    private void loadMoviesData() {
        if (mSortOrder == SortOrder.FAVORITES) {
            loadMoviesDataFromDatabase();
        } else {
            loadMoviesDataFromInternet();
        }
    }

    private void loadMoviesDataFromDatabase() {
        initLoader(ID_FAVORITE_MOVIES_LOADER);
    }

    private void loadMoviesDataFromInternet() {
        if (NetworkUtils.isOnline(getActivity())) {
            if (mSortOrder == SortOrder.TOP_RATED) {
                initLoader(ID_TOP_RATED_MOVIES_LOADER);
            } else {
                initLoader(ID_POPULAR_MOVIES_LOADER);
            }
        } else {
            showInternetErrorMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private void initLoader(int loaderId) {
        getActivity().getSupportLoaderManager().initLoader(loaderId, null, this);
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
                mSortOrder = mSortOrder.swap();
                item.setTitle(getString(mSortOrder.getResourceId()));
                loadMoviesData();
                return true;
            case R.id.action_settings:
                startActivity(SettingsActivity.newIntent(getActivity()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(Movie movie, ImageView sharedImageView, int adapterPosition) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(), sharedImageView, ViewCompat.getTransitionName(sharedImageView));
        Intent intent = MovieDetailsActivity.newIntent(getActivity(), movie);
        intent.putExtra(MovieDetailsActivity.EXTRA_ADAPTER_POSITION, adapterPosition);
        startActivityForResult(intent, REQUEST_CODE_ADAPTER_POSITION, options.toBundle());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == MovieDetailsActivity.RESULT_DELETED && requestCode == REQUEST_CODE_ADAPTER_POSITION) {
            int adapterPosition = MovieDetailsActivity.wasMovieDeleted(data);
            if (mSortOrder == SortOrder.FAVORITES) {
                Log.d(MoviePostersFragment.class.getSimpleName(), String.format("Deleted movie at position: %d", adapterPosition));
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(EXTRA_MOVIES, mMoviesAdapter.getMovies() != null ? new ArrayList<>(mMoviesAdapter.getMovies()) : null);
        outState.putSerializable(EXTRA_SORT_ORDER, mSortOrder);
        outState.putParcelable(EXTRA_RECYCLER_VIEW, mLayoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Loader onCreateLoader(int loaderId, @Nullable Bundle bundle) {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        switch (loaderId) {
            case ID_FAVORITE_MOVIES_LOADER:
                Log.d(MoviePostersFragment.class.getSimpleName(), "Loading favorite movies...");
                return newCursorLoader();
            case ID_TOP_RATED_MOVIES_LOADER:
                Log.d(MoviePostersFragment.class.getSimpleName(), "Loading top rated movies...");
                return new InternetAsyncTaskLoader(getActivity(), SortOrder.TOP_RATED);
            case ID_POPULAR_MOVIES_LOADER:
                Log.d(MoviePostersFragment.class.getSimpleName(), "Loading popular movies...");
                return new InternetAsyncTaskLoader(getActivity(), SortOrder.POPULAR);
            default:
                throw new UnsupportedOperationException("LoaderId not implemented: " + loaderId);
        }
    }

    @NonNull
    private Loader newCursorLoader() {
        Uri movieQueryUri = MovieContract.MovieEntry.CONTENT_URI;
        String sortOrder = MovieContract.MovieEntry._ID + " ASC";
        return new CursorLoader(getContext(),
                movieQueryUri,
                null,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (loader.getId() == ID_FAVORITE_MOVIES_LOADER) {
            if (wereFavoritesUpdatedAfterChoosingFromFavoritesScreen()) {
                showMoviesOrError(MovieDbUtils.getFavoriteMoviesAsList((Cursor) data));
            }
        } else {
            showMoviesOrError(Optional.ofNullable(data).map(movies -> Arrays.asList((Movie[]) movies)).orElse(null));
        }
    }

    private boolean wereFavoritesUpdatedAfterChoosingFromFavoritesScreen() {
        return mSortOrder == SortOrder.FAVORITES;
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        mMoviesAdapter.setMoviesData(null);
    }

    public static class InternetAsyncTaskLoader extends AsyncTaskLoader<Movie[]> {

        private final SortOrder sortOrder;

        public InternetAsyncTaskLoader(@NonNull Context context, SortOrder sortOrder) {
            super(context);
            this.sortOrder = sortOrder;
            forceLoad();
        }

        @Nullable
        @Override
        public Movie[] loadInBackground() {
            Movie[] movies = null;
            String apiKey = getContext().getResources().getString(R.string.API_KEY_TMDB);
            URL moviesRequestUrl = NetworkUtils.buildMoviesUrl(sortOrder, apiKey);
            try {
                String jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(moviesRequestUrl);
                movies = JsonUtils.getMoviesFromJson(jsonMoviesResponse);
            } catch (Exception e) {
                Log.e(InternetAsyncTaskLoader.class.getSimpleName(), e.getLocalizedMessage());
            }
            return movies;
        }

    }

}
