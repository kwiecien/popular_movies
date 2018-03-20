package com.kk.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import com.kk.popularmovies.data.MovieContract;
import com.kk.popularmovies.enums.LoaderId;
import com.kk.popularmovies.model.Movie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.Optional;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kk.popularmovies.data.MovieDbHelper.deleteMovieFromDb;
import static com.kk.popularmovies.data.MovieDbHelper.insertMovieToDb;
import static com.kk.popularmovies.utilities.ReleaseDateUtils.getReleaseYear;

public class MovieDetailsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String EXTRA_MOVIE = "com.kk.popularmovies.extra_movie";
    private static final String EXTRA_TRANSITION = "com.kk.popularmovies.extra.transition";
    private static final int LOADER_MOVIE_BY_ID = LoaderId.MovieDetails.MOVIE_BY_ID;

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
    @BindView(R.id.reviews_ll)
    LinearLayout mReviewsLl;
    @BindView(R.id.trailers_ll)
    LinearLayout trailersLl;

    private Movie mMovie;
    private boolean mFavorite;

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
        }
        if (mMovie != null) {
            setViewsContent();
            setBackgroundImage(extras);
            setOnClickListeners();
        }
    }

    private void setViewsContent() {
        mMovieTv.setText(mMovie.getTitle());
        mReleaseDateTv.setText(String.format(Locale.getDefault(), "(%s)", getReleaseYear(mMovie)));
        mUserRankingTv.setText(String.format(Locale.getDefault(), "%1.1f", mMovie.getUserRating()));
        mPlotSynopsisTv.setText(mMovie.getPlotSynopsis());
        getSupportLoaderManager().initLoader(LOADER_MOVIE_BY_ID, null, this);
        setReviews();
        setTrailers();
    }

    private void determineIfFavorite(boolean favorite) {
        mFavorite = favorite;
        if (favorite) {
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

    private void setBackgroundImage(Bundle extras) {
        String imageThumbnail = mMovie.getImageThumbnail();
        ImageView backgroundImage = findViewById(R.id.movie_details_background_iv);
        displayBackgroundImage(extras, imageThumbnail, backgroundImage);
    }

    private void setOnClickListeners() {
        mStarTv.setOnClickListener(
                v -> handleFavoriteMovie()
        );
    }

    private void handleFavoriteMovie() {
        if (mFavorite) {
            int deletedMovies = deleteMovieFromDb(this, mMovie);
            if (deletedMovies > 0) {
                mStarTv.setImageResource(swapStar());
            }
        } else {
            Uri insertedUri = insertMovieToDb(this, mMovie);
            if (insertedUri != null) {
                mStarTv.setImageResource(swapStar());
            }
        }
    }

    private void displayBackgroundImage(Bundle extras, String imageThumbnail, ImageView backgroundImage) {
        String transitionName = Optional.ofNullable(extras).map(ext -> ext.getString(EXTRA_TRANSITION)).orElse(null);
        backgroundImage.setTransitionName(transitionName);
        // TODO download or load from memory from favorites
        Picasso.with(this)
                .load(imageThumbnail)
                .noFade()
                .into(backgroundImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        supportStartPostponedEnterTransition();
                        backgroundImage.setAlpha(0.10f);
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
                        new String[]{MovieContract.MovieEntry.COLUMN_MOVIE_ID},
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
        determineIfFavorite(data.moveToFirst());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // TODO should do something?
    }

}
