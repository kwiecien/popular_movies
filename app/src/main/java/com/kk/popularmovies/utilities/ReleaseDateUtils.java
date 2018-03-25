package com.kk.popularmovies.utilities;

import android.util.Log;

import com.kk.popularmovies.model.Movie;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReleaseDateUtils {

    private static final String TAG = ReleaseDateUtils.class.getSimpleName();

    private ReleaseDateUtils() {
        // Utility classes should not have public constructors
    }

    public static String getReleaseYear(Movie movie) {
        return new SimpleDateFormat("yyyy", Locale.getDefault()).format(movie.getReleaseDate());
    }

    public static Date parseDate(String releaseDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(releaseDate);
        } catch (ParseException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        return parsedDate;
    }

}
