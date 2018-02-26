package com.kk.popularmovies;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kk.popularmovies.model.Movie;
import com.kk.popularmovies.utilities.JsonUtils;
import com.kk.popularmovies.utilities.NetworkUtils;

import java.net.URL;

public class MoviePostersFragment extends Fragment implements MoviesAdapter.MoviesAdapterOnClickHandler {

    private MoviesAdapter mMoviesAdapter;
    private RecyclerView mRecyclerView;

    public MoviePostersFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_posters, container, false);

        mRecyclerView = rootView.findViewById(R.id.rv_movies);
        int spanCount = 2;
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mMoviesAdapter = new MoviesAdapter(this, getContext());
        mRecyclerView.setAdapter(mMoviesAdapter);

        loadMoviesData();

        return rootView;
    }

    private void loadMoviesData() {
        showMoviesDataView();
        new FetchMoviesAsyncTask().execute();
    }

    private void showMoviesDataView() {
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(Movie movie) {
        startActivity(MovieDetailsActivity.newIntent(getActivity(), movie));
    }

    private void showErrorMessage() {
        // TODO: Provide sensible implementation
    }

    private class FetchMoviesAsyncTask extends AsyncTask<Void, Void, Movie[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // TODO: loading indicator
        }

        @Override
        protected Movie[] doInBackground(Void... voids) {
            Movie[] movies = null;
            String api_key = getResources().getString(R.string.API_KEY_TMDB);
            URL moviesRequestUrl = NetworkUtils.buildUrl(NetworkUtils.SortOrder.POPULAR, api_key);
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
            if (movies != null) {
                showMoviesDataView();
                mMoviesAdapter.setMoviesData(movies);
            } else {
                showErrorMessage();
            }
        }
    }
}
