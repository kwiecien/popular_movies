package com.kk.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.kk.popularmovies.model.Movie;

import static android.provider.BaseColumns._ID;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_IMAGE;
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
    private static final int DATABASE_VERSION = 2;

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static Uri insertMovieToDb(Context context, Movie movie, byte[] image) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_MOVIE_ID, movie.getId());
        contentValues.put(COLUMN_TITLE, movie.getTitle());
        contentValues.put(COLUMN_RELEASE_DATE, getReleaseYear(movie));
        contentValues.put(COLUMN_IMAGE_THUMBNAIL, movie.getImageThumbnail());
        contentValues.put(COLUMN_IMAGE, image);
        contentValues.put(COLUMN_PLOT_SYNOPSIS, movie.getPlotSynopsis());
        contentValues.put(COLUMN_USER_RATING, movie.getUserRating());
        return context.getContentResolver().insert(CONTENT_URI, contentValues);
    }

    public static int deleteMovieFromDb(Context context, Movie movie) {
        Uri uri = CONTENT_URI.buildUpon().appendPath(Long.toString(movie.getId())).build();
        return context.getContentResolver().delete(uri, null, null);
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
                        COLUMN_IMAGE + " BLOB, " +
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
