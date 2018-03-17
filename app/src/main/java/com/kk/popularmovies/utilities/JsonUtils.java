package com.kk.popularmovies.utilities;

import android.util.Log;

import com.kk.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class JsonUtils {
    private static final String VOTE_AVERAGE = "vote_average";
    private static final String TITLE = "title";
    private static final String OVERVIEW = "overview";
    private static final String RELEASE_DATE = "release_date";
    private static final String POSTER_PATH = "poster_path";
    private static final String TAG = JsonUtils.class.getSimpleName();

    private JsonUtils() {
    }

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
            Date parsedDate = parseDate(releaseDate);
            String posterPath = movieJson.getString(POSTER_PATH);
            Movie movie = new Movie.Builder(title, parsedDate)
                    .withPosterPath(posterPath)
                    .withUserRating(voteAverage)
                    .withPlotSynopsis(overview)
                    .build();
            movies[i] = movie;
        }
        return movies;
    }

    private static Date parseDate(String releaseDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(releaseDate);
        } catch (ParseException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return parsedDate;
    }
}
