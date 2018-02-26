package com.kk.popularmovies.utilities;

import com.kk.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;

public class JsonUtils {
    private static final String VOTE_AVERAGE = "vote_average";
    private static final String TITLE = "title";
    private static final String OVERVIEW = "overview";
    private static final String RELEASE_DATE = "release_date";
    private static final String POSTER_PATH = "poster_path";

    public static Movie[] getMoviesFromJson(String jsonMoviesResponse) throws JSONException {
        JSONObject moviesJson = new JSONObject(jsonMoviesResponse);
        JSONArray results = moviesJson.getJSONArray("results");
        Movie[] movies = new Movie[results.length()];
        for (int i = 0; i < results.length(); i++) {
            JSONObject movieJson = results.getJSONObject(i);
            double voteAverage = movieJson.getDouble(VOTE_AVERAGE);
            String title = movieJson.getString(TITLE);
            String overview = movieJson.getString(OVERVIEW);
            String releaseDate = movieJson.getString(RELEASE_DATE);
            String posterPath = movieJson.getString(POSTER_PATH);
            Movie movie = new Movie.Builder(title, LocalDate.parse(releaseDate))
                    .withPosterPath(posterPath)
                    .withUserRating(voteAverage)
                    .withPlotSynopsis(overview)
                    .build();
            movies[i] = movie;
        }
        return movies;
    }
}
