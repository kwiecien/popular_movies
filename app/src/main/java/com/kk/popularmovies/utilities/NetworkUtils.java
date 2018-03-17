package com.kk.popularmovies.utilities;

import android.net.Uri;
import android.util.Log;

import com.kk.popularmovies.model.SortOrder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final String MOVIEDB_BASE_URL = "https://api.themoviedb.org/3/movie";
    private static final String API_KEY_PARAM = "api_key";
    private static final String TRAILERS_PARAM = "videos";
    private static final String REVIEWS_PARAM = "reviews";

    private NetworkUtils() {
    }

    public static URL buildMoviesUrl(SortOrder sortOrder, String apiKey) {
        Uri uri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                .appendPath(sortOrder.toString().toLowerCase())
                .appendQueryParameter(API_KEY_PARAM, apiKey)
                .build();

        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        logBuiltUri(url);
        return url;
    }

    public static URL buildTrailersUrl(long id, String apiKey) {
        Uri uri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                .appendPath(Long.toString(id))
                .appendPath(TRAILERS_PARAM)
                .appendQueryParameter(API_KEY_PARAM, apiKey)
                .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        logBuiltUri(url);
        return url;
    }

    public static URL buildReviewsUrl(long id, String apiKey) {
        Uri uri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                .appendPath(Long.toString(id))
                .appendPath(REVIEWS_PARAM)
                .appendQueryParameter(API_KEY_PARAM, apiKey)
                .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        logBuiltUri(url);
        return url;
    }

    private static void logBuiltUri(URL url) {
        Log.v(TAG, "Built URI " + url);
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try (InputStream in = urlConnection.getInputStream()) {
            Scanner scanner = new Scanner(in, "UTF-8");
            scanner.useDelimiter("\\A");
            if (scanner.hasNext()) {
                return scanner.next();
            } else {
                return null;
            }
        }
    }

}
