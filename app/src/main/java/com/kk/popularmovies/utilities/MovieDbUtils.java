package com.kk.popularmovies.utilities;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.kk.popularmovies.data.MovieContract;
import com.kk.popularmovies.model.Movie;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class MovieDbUtils {

    private MovieDbUtils() {
    }

    @NonNull
    public static List<Movie> getFavoriteMoviesAsList(@NonNull Cursor moviesCursor) {
        List<Movie> favoriteMovies = new ArrayList<>();
        int indexMovieId = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        int indexTitle = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
        int indexReleaseDate = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        int indexImageThumbnail = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMAGE_THUMBNAIL);
        int indexImage = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMAGE);
        int indexPlotSynopsis = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS);
        int indexUserRanking = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_USER_RATING);
        for (moviesCursor.moveToFirst(); !moviesCursor.isAfterLast(); moviesCursor.moveToNext()) {
            long id = moviesCursor.getLong(indexMovieId);
            String title = moviesCursor.getString(indexTitle);
            Date releaseDate = ReleaseDateUtils.parseDate(moviesCursor.getString(indexReleaseDate));
            String imageThumbnail = moviesCursor.getString(indexImageThumbnail);
            byte[] image = moviesCursor.getBlob(indexImage);
            String plotSynopsis = moviesCursor.getString(indexPlotSynopsis);
            double userRanking = moviesCursor.getDouble(indexUserRanking);
            Movie movie = new Movie.Builder(id, title, releaseDate)
                    .withPlotSynopsis(plotSynopsis)
                    .withPosterPath(imageThumbnail.substring(imageThumbnail.lastIndexOf('/')))
                    .withUserRating(userRanking)
                    .withImage(image)
                    .build();
            favoriteMovies.add(movie);
        }
        return favoriteMovies;
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, outputStream);
        return outputStream.toByteArray();
    }

}
