package com.kk.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kk.popularmovies.data.MovieContract;
import com.kk.popularmovies.enums.LoaderId;
import com.kk.popularmovies.model.Movie;
import com.kk.popularmovies.utilities.MovieDbUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.Optional;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_IMAGE;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_MOVIE_ID;
import static com.kk.popularmovies.data.MovieDbHelper.deleteMovieFromDb;
import static com.kk.popularmovies.data.MovieDbHelper.insertMovieToDb;
import static com.kk.popularmovies.utilities.ReleaseDateUtils.getReleaseYear;

public class MovieDetailsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String EXTRA_MOVIE = "com.kk.popularmovies.extra_movie";
    private static final String EXTRA_TRANSITION = "com.kk.popularmovies.extra.transition";
    private static final int LOADER_MOVIE_BY_ID = LoaderId.MovieDetails.MOVIE_BY_ID;
    private static final float ALPHA = 0.10f;

    @BindView(R.id.movie_details_title_tv)
    TextView mMovieTv;
    @BindView(R.id.movie_details_release_date_tv)
    TextView mReleaseDateTv;
    @BindView(R.id.movie_details_user_rating_tv)
    TextView mUserRankingTv;
    @BindView(R.id.movie_details_plot_synopsis_tv)
    TextView mPlotSynopsisTv;
    @BindView(R.id.movie_details_star_iv)
    ImageView mStarTv;
    @BindView(R.id.movie_details_background_iv)
    ImageView mBackgroundIv;
    @BindView(R.id.reviews_ll)
    LinearLayout mReviewsLl;
    @BindView(R.id.trailers_ll)
    LinearLayout trailersLl;

    private Movie mMovie;
    private boolean mFavorite;
    private byte[] mImage;
    private String mTransitionName;

    public static Intent newIntent(Context packageContext, Movie movie) {
        Intent intent = new Intent(packageContext, MovieDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_MOVIE, movie);
        intent.putExtras(bundle);
        intent.putExtra(EXTRA_TRANSITION, movie.getTitle());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);
        Optional.ofNullable(getSupportActionBar()).ifPresent(sab -> sab.setDisplayHomeAsUpEnabled(true));
        supportPostponeEnterTransition();

        Bundle extras = null;
        if (savedInstanceState != null) {
            mMovie = (Movie) savedInstanceState.getSerializable(EXTRA_MOVIE);
        } else {
            Intent intent = getIntent();
            extras = Optional.ofNullable(intent).map(Intent::getExtras).orElse(null);
            mMovie = Optional.ofNullable(extras).map(ext -> (Movie) ext.getSerializable(EXTRA_MOVIE)).orElse(null);
            mTransitionName = Optional.ofNullable(extras).map(ext -> ext.getString(EXTRA_TRANSITION)).orElse(null);
        }
        if (mMovie != null) {
            getSupportLoaderManager().initLoader(LOADER_MOVIE_BY_ID, null, this);
            setViewsContent();
            setOnClickListeners();
        }
    }

    private void setViewsContent() {
        mMovieTv.setText(mMovie.getTitle());
        mReleaseDateTv.setText(String.format(Locale.getDefault(), "(%s)", getReleaseYear(mMovie)));
        mUserRankingTv.setText(String.format(Locale.getDefault(), "%1.1f", mMovie.getUserRating()));
        mPlotSynopsisTv.setText(mMovie.getPlotSynopsis());
        mBackgroundIv.setAlpha(ALPHA);
        setReviews();
        setTrailers();
    }

    private void setCorrectStarImage() {
        if (mFavorite) {
            mStarTv.setImageResource(android.R.drawable.star_big_on);
        } else {
            mStarTv.setImageResource(android.R.drawable.star_big_off);
        }
    }

    private int swapStar() {
        if (mFavorite) {
            mFavorite = false;
            return android.R.drawable.star_big_off;
        } else {
            mFavorite = true;
            return android.R.drawable.star_big_on;
        }
    }

    private void setReviews() {
        TextView textView1 = new TextView(this);
        textView1.setText("Review 1\nReview 1\nReview 1\nReview 1\n");
        TextView textView2 = new TextView(this);
        textView2.setText("Review 2\nReview 2\nReview 2\nReview 2\n");
        mReviewsLl.addView(textView1);
        mReviewsLl.addView(textView2);
    }

    private void setTrailers() {
        TextView textView1 = new TextView(this);
        textView1.setText("Trailer 1");
        TextView textView2 = new TextView(this);
        textView2.setText("Trailer 2");
        trailersLl.addView(textView1);
        trailersLl.addView(textView2);
    }

    private void setOnClickListeners() {
        mStarTv.setOnClickListener(
                v -> onStarClicked()
        );
    }

    private void onStarClicked() {
        if (mFavorite) {
            removeMovieFromFavorites();
        } else {
            saveMovieAsFavorite();
        }
        mStarTv.setImageResource(swapStar());
    }

    private void removeMovieFromFavorites() {
        deleteMovieFromDb(this, mMovie);
    }

    private void setBackgroundImage(Cursor data) {
        mBackgroundIv.setTransitionName(mTransitionName);
        if (mFavorite) {
            fetchImageFromDb(data);
        } else {
            fetchImageFromInternet();
        }
    }

    private void fetchImageFromDb(Cursor data) {
        // QUESTION
        // If I click star, the posters are set into recycler view, but their size is weird...
        // Sometimes they are twice as high, as they should be...
        // Why? How to correct it?
        mImage = getImageFromDb(data);
        Glide.with(this)
                .load(mImage)
                .into(mBackgroundIv);
        supportStartPostponedEnterTransition();
    }

    private void fetchImageFromInternet() {
        Picasso.with(this)
                .load(mMovie.getImageThumbnail())
                .noFade()
                .placeholder(android.R.drawable.stat_sys_download)
                .error(android.R.drawable.stat_notify_error)
                .into(mBackgroundIv, new Callback() {
                    @Override
                    public void onSuccess() {
                        supportStartPostponedEnterTransition();
                    }

                    @Override
                    public void onError() {
                        supportStartPostponedEnterTransition();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(EXTRA_MOVIE, mMovie);
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, @Nullable Bundle bundle) {
        switch (loaderId) {
            case LOADER_MOVIE_BY_ID:
                Uri movieQueryUri = MovieContract.MovieEntry.CONTENT_URI.buildUpon()
                        .appendPath(Long.toString(mMovie.getId())).build();
                return new CursorLoader(this,
                        movieQueryUri,
                        new String[]{COLUMN_MOVIE_ID, COLUMN_IMAGE},
                        null,
                        null,
                        null);
            default:
                throw new UnsupportedOperationException("LoaderId not implemented: " + loaderId);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return;
        }
        mFavorite = data.moveToFirst();
        setCorrectStarImage();
        setBackgroundImage(data);
        getSupportLoaderManager().destroyLoader(loader.getId());
    }

    private void saveMovieAsFavorite() {
        Glide.with(this)
                .asBitmap()
                .load(mMovie.getImageThumbnail())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        mImage = MovieDbUtils.getBitmapAsByteArray(resource);
                        insertMovieToDb(MovieDetailsActivity.this, mMovie, mImage);
                    }
                });
    }

    private byte[] getImageFromDb(Cursor data) {
        return data.getBlob(data.getColumnIndex(COLUMN_IMAGE));
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Not implemented because not needed yet
    }

}
