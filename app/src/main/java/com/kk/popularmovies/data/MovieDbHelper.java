package com.kk.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_ID;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_IMAGE_THUMBNAIL;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_RELEASE_DATE;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_TITLE;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_USER_RATING;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.TABLE_NAME;

public class MovieDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movie.db";
    private static final int DATABASE_VERSION = 1;


    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                        COLUMN_TITLE + " TEXT NOT NULL, " +
                        COLUMN_RELEASE_DATE + " DATE NOT NULL, " +
                        COLUMN_IMAGE_THUMBNAIL + "TEXT , " +
                        COLUMN_PLOT_SYNOPSIS + "TEXT , " +
                        COLUMN_USER_RATING + "REAL" +
                        ");";
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL_DROP_MOVIE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(SQL_DROP_MOVIE_TABLE);
    }

}
