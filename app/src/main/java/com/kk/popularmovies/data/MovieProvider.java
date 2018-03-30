package com.kk.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.kk.popularmovies.R;

import java.util.Optional;

import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_MOVIE_ID;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.CONTENT_URI;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.TABLE_NAME;

public class MovieProvider extends ContentProvider {

    public static final int CODE_MOVIES = 100;
    public static final int CODE_MOVIE_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private MovieDbHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = MovieContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, MovieContract.PATH_MOVIES, CODE_MOVIES);
        matcher.addURI(authority, MovieContract.PATH_MOVIES + "/#", CODE_MOVIE_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                cursor = db.query(
                        TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CODE_MOVIE_WITH_ID:
                String id = Long.toString(ContentUris.parseId(uri));
                cursor = db.query(
                        TABLE_NAME,
                        projection,
                        COLUMN_MOVIE_ID + "=?",
                        new String[]{id},
                        null,
                        null,
                        null
                );
                break;
            default:
                throw throwUnknownUriException(uri);
        }
        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    private UnsupportedOperationException throwUnknownUriException(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Unknown URI: " + uri);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                return "vnd.android.cursor.dir" + "/" + MovieContract.CONTENT_AUTHORITY + "/" + MovieContract.PATH_MOVIES;
            case CODE_MOVIE_WITH_ID:
                return "vnd.android.cursor.item" + "/" + MovieContract.CONTENT_AUTHORITY + "/" + MovieContract.PATH_MOVIES;
            default:
                throw throwUnknownUriException(uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri returnUri = null;
        if (sUriMatcher.match(uri) == CODE_MOVIES) {
            long id = db.insert(TABLE_NAME, null, values);
            if (id > 0) {
                returnUri = ContentUris.withAppendedId(CONTENT_URI, id);
            } else {
                Toast.makeText(getContext(), getContext().getString(R.string.failed_to_insert) + uri, Toast.LENGTH_LONG).show();
            }
        } else {
            throw throwUnknownUriException(uri);
        }
        Optional.ofNullable(getContext())
                .map(Context::getContentResolver)
                .ifPresent(cr -> cr.notifyChange(uri, null));
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int moviesDeleted = 0;
        if (sUriMatcher.match(uri) == CODE_MOVIE_WITH_ID) {
            String movieId = uri.getLastPathSegment();
            moviesDeleted = mDbHelper.getWritableDatabase().delete(
                    TABLE_NAME,
                    "movie_id=?",
                    new String[]{movieId});
        } else {
            throw throwUnknownUriException(uri);
        }
        if (moviesDeleted > 0) {
            Optional.ofNullable(getContext()).map(Context::getContentResolver).ifPresent(cr -> cr.notifyChange(uri, null));
        }
        return moviesDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Update not supported!");
    }
}
