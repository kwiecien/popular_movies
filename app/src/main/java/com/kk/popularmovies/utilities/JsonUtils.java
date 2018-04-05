package com.kk.popularmovies.utilities;

import com.kk.popularmovies.model.Movie;
import com.kk.popularmovies.model.Review;
import com.kk.popularmovies.model.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.kk.popularmovies.utilities.ReleaseDateUtils.parseDate;

public final class JsonUtils {
    private static final String VOTE_AVERAGE = "vote_average";
    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String OVERVIEW = "overview";
    private static final String RELEASE_DATE = "release_date";
    private static final String POSTER_PATH = "poster_path";
    private static final String AUTHOR = "author";
    private static final String CONTENT = "content";
    private static final String RESULTS = "results";
    private static final String NAME = "name";
    private static final String KEY = "key";

    private JsonUtils() {
    }

    public static Movie[] getMoviesFromJson(String jsonMoviesResponse) throws JSONException {
        JSONObject moviesJson = new JSONObject(jsonMoviesResponse);
        JSONArray results = moviesJson.getJSONArray(RESULTS);
        Movie[] movies = new Movie[results.length()];
        for (int i = 0; i < results.length(); i++) {
            JSONObject movieJson = results.getJSONObject(i);
            double voteAverage = movieJson.getDouble(VOTE_AVERAGE);
            long id = movieJson.getLong(ID);
            String title = movieJson.getString(TITLE);
            String overview = movieJson.getString(OVERVIEW);
            String releaseDate = movieJson.getString(RELEASE_DATE);
            Date parsedDate = parseDate(releaseDate);
            String posterPath = movieJson.getString(POSTER_PATH);
            Movie movie = new Movie.Builder(id, title, parsedDate)
                    .withPosterPath(posterPath)
                    .withUserRating(voteAverage)
                    .withPlotSynopsis(overview)
                    .build();
            movies[i] = movie;
        }
        return movies;
    }

    public static List<Review> getReviewsFromJson(String jsonReviewsResponse) throws JSONException {
        JSONObject reviewsJson = new JSONObject(jsonReviewsResponse);
        JSONArray results = reviewsJson.getJSONArray(RESULTS);
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject reviewJson = results.getJSONObject(i);
            String author = reviewJson.getString(AUTHOR);
            String content = reviewJson.getString(CONTENT);
            Review review = new Review(author, content);
            reviews.add(review);
        }
        return reviews;
    }

    public static List<Trailer> getTrailersFromJson(String jsonTrailersResponse) throws JSONException {
        JSONObject trailersJson = new JSONObject(jsonTrailersResponse);
        JSONArray results = trailersJson.getJSONArray(RESULTS);
        List<Trailer> trailers = new ArrayList<>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject reviewJson = results.getJSONObject(i);
            String name = reviewJson.getString(NAME);
            String key = reviewJson.getString(KEY);
            Trailer trailer = new Trailer(name, key);
            trailers.add(trailer);
        }
        return trailers;
    }

}
