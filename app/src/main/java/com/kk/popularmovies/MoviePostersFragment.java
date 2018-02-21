package com.kk.popularmovies;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.kk.popularmovies.model.Movie;

import java.time.LocalDate;
import java.util.Arrays;

public class MoviePostersFragment extends Fragment {

    private MoviesAdapter mMoviesAdapter;

    private Movie[] mMovies = {
            new Movie.Builder("Test0", LocalDate.now()).build(),
            new Movie.Builder("Test1", LocalDate.now()).build(),
            new Movie.Builder("Test2", LocalDate.now()).build(),
            new Movie.Builder("Test3", LocalDate.now()).build(),
            new Movie.Builder("Test4", LocalDate.now()).build(),
            new Movie.Builder("Test5", LocalDate.now()).build(),
            new Movie.Builder("Test6", LocalDate.now()).build(),
            new Movie.Builder("Test7", LocalDate.now()).build(),
    };

    public MoviePostersFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_posters, container, false);
        mMoviesAdapter = new MoviesAdapter(getActivity(), Arrays.asList(mMovies));
        GridView gridView = rootView.findViewById(R.id.movies_gv);
        gridView.setAdapter(mMoviesAdapter);
        return rootView;
    }
}
