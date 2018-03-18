package com.kk.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.widget.Toast;

import com.kk.popularmovies.model.Movie;

import static android.provider.BaseColumns._ID;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_IMAGE_THUMBNAIL;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_MOVIE_ID;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_RELEASE_DATE;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_TITLE;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_USER_RATING;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.CONTENT_URI;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.TABLE_NAME;
import static com.kk.popularmovies.utilities.ReleaseDateUtils.getReleaseYear;

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movie.db";
    private static final int DATABASE_VERSION = 1;


    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void insertMovieToDb(Context context, Movie movie) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_MOVIE_ID, movie.getId());
        contentValues.put(COLUMN_TITLE, movie.getTitle());
        contentValues.put(COLUMN_RELEASE_DATE, getReleaseYear(movie));
        contentValues.put(COLUMN_IMAGE_THUMBNAIL, movie.getImageThumbnail());
        contentValues.put(COLUMN_PLOT_SYNOPSIS, movie.getPlotSynopsis());
        contentValues.put(COLUMN_USER_RATING, movie.getUserRating());
        Uri uri = context.getContentResolver().insert(CONTENT_URI, contentValues);
        if (uri != null) {
            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
        }
    }

    public static void deleteMovieFromDb(Context context, Movie movie) {
        Uri uri = CONTENT_URI.buildUpon().appendPath(Long.toString(movie.getId())).build();
        int deletedMovies = context.getContentResolver().delete(uri, null, null);
        if (deletedMovies > 0) {
            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
        }
    }

    public static Cursor findFavoriteMovies(Context context) {
        return context.getContentResolver().query(CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                        COLUMN_TITLE + " TEXT NOT NULL, " +
                        COLUMN_RELEASE_DATE + " DATE NOT NULL, " +
                        COLUMN_IMAGE_THUMBNAIL + " TEXT, " +
                        COLUMN_PLOT_SYNOPSIS + " TEXT, " +
                        COLUMN_USER_RATING + " REAL, " +
                        "UNIQUE (" + COLUMN_MOVIE_ID + ") ON CONFLICT ROLLBACK" +
                        ");";
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL_DROP_MOVIE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(SQL_DROP_MOVIE_TABLE);
    }

}
