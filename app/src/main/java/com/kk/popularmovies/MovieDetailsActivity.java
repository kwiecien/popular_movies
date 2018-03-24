package com.kk.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kk.popularmovies.data.MovieContract;
import com.kk.popularmovies.enums.LoaderId;
import com.kk.popularmovies.model.Movie;
import com.kk.popularmovies.model.Review;
import com.kk.popularmovies.model.Trailer;
import com.kk.popularmovies.utilities.JsonUtils;
import com.kk.popularmovies.utilities.MovieDbUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_IMAGE;
import static com.kk.popularmovies.data.MovieContract.MovieEntry.COLUMN_MOVIE_ID;
import static com.kk.popularmovies.data.MovieDbHelper.deleteMovieFromDb;
import static com.kk.popularmovies.data.MovieDbHelper.insertMovieToDb;
import static com.kk.popularmovies.utilities.NetworkUtils.buildReviewsUrl;
import static com.kk.popularmovies.utilities.NetworkUtils.buildTrailersUrl;
import static com.kk.popularmovies.utilities.NetworkUtils.buildYouTubeTrailerUrl;
import static com.kk.popularmovies.utilities.NetworkUtils.getResponseFromHttpUrl;
import static com.kk.popularmovies.utilities.NetworkUtils.isOnline;
import static com.kk.popularmovies.utilities.ReleaseDateUtils.getReleaseYear;

public class MovieDetailsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks {

    private static final String EXTRA_MOVIE = "com.kk.popularmovies.extra_movie";
    private static final String EXTRA_TRANSITION = "com.kk.popularmovies.extra.transition";
    private static final int LOADER_MOVIE_BY_ID = LoaderId.MovieDetails.MOVIE_BY_ID;
    private static final int LOADER_MOVIE_REVIEWS = LoaderId.MovieDetails.MOVIE_REVIEWS;
    private static final int LOADER_MOVIE_TRAILERS = LoaderId.MovieDetails.MOVIE_TRAILERS;
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
    LinearLayout mTrailersLl;

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

        Bundle extras;
        if (savedInstanceState != null) {
            mMovie = (Movie) savedInstanceState.getSerializable(EXTRA_MOVIE);
        } else {
            Intent intent = getIntent();
            extras = Optional.ofNullable(intent).map(Intent::getExtras).orElse(null);
            mMovie = Optional.ofNullable(extras).map(ext -> (Movie) ext.getSerializable(EXTRA_MOVIE)).orElse(null);
            mTransitionName = Optional.ofNullable(extras).map(ext -> ext.getString(EXTRA_TRANSITION)).orElse(null);
        }
        if (mMovie != null) {
            setViewsContent();
            setOnClickListeners();
            getSupportLoaderManager().initLoader(LOADER_MOVIE_BY_ID, null, this);
        }
    }

    private void setViewsContent() {
        mMovieTv.setText(mMovie.getTitle());
        mReleaseDateTv.setText(String.format(Locale.getDefault(), "(%s)", getReleaseYear(mMovie)));
        mUserRankingTv.setText(String.format(Locale.getDefault(), "%1.1f", mMovie.getUserRating()));
        mPlotSynopsisTv.setText(mMovie.getPlotSynopsis());
        mBackgroundIv.setAlpha(ALPHA);
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

    private void setReviews(List<Review> reviews) {
        if (reviews.isEmpty()) {
            findViewById(R.id.movie_details_reviews_section).setVisibility(View.GONE);
            return;
        }
        for (Review review : reviews) {
            TextView authorView = new TextView(this);
            authorView.setText(review.getAuthor());
            authorView.setTypeface(Typeface.create("sans-serif-smallcaps", Typeface.NORMAL));
            authorView.setTextColor(getColor(android.R.color.white));
            authorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            TextView reviewView = new TextView(this);
            reviewView.setText(String.format("%s%n", review.getContent()));
            reviewView.setPadding(32, 0, 32, 0);
            mReviewsLl.addView(authorView);
            mReviewsLl.addView(reviewView);
        }
    }

    private void setTrailers(List<Trailer> trailers) {
        if (trailers.isEmpty()) {
            findViewById(R.id.movie_details_trailers_section).setVisibility(View.GONE);
            return;
        }
        mTrailersLl.setVisibility(View.VISIBLE);
        for (Trailer trailer : trailers) {
            TextView trailerView = new TextView(this);
            trailerView.setPadding(0, 0, 32, 32);
            trailerView.setText(trailer.getName());
            trailerView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            Drawable play = getDrawable(android.R.drawable.ic_media_play);
            play.setBounds(new Rect(0, 0, 100, 100));
            trailerView.setCompoundDrawablesRelative(play, null, null, null);
            trailerView.setOnClickListener((View v) -> {
                Uri appUri = Uri.parse("vnd.youtube:" + trailer.getKey());
                Intent appIntent = new Intent(Intent.ACTION_VIEW, appUri);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(buildYouTubeTrailerUrl(trailer.getKey())));
                if (appIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(appIntent);
                } else {
                    startActivity(webIntent);
                }
            });
            mTrailersLl.addView(trailerView);
        }
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

    private void setBackgroundImage(Cursor data) {
        mBackgroundIv.setTransitionName(mTransitionName);
        if (mFavorite) {
            fetchImageFromDb(data);
        } else {
            fetchImageFromInternet();
        }
    }

    private void fetchImageFromDb(Cursor data) {
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
    public Loader onCreateLoader(int loaderId, @Nullable Bundle bundle) {
        switch (loaderId) {
            case LOADER_MOVIE_BY_ID:
                Log.d(MovieDetailsActivity.class.getSimpleName(), "Loading movie by id...");
                return newCursorLoader();
            case LOADER_MOVIE_REVIEWS:
                Log.d(MovieDetailsActivity.class.getSimpleName(), "Loading movie reviews...");
                return new ReviewsAsyncTaskLoader(this, mMovie);
            case LOADER_MOVIE_TRAILERS:
                Log.d(MovieDetailsActivity.class.getSimpleName(), "Loading movie trailers...");
                return new TrailersAsyncTaskLoader(this, mMovie);
            default:
                throw new UnsupportedOperationException("LoaderId not implemented: " + loaderId);
        }
    }

    @NonNull
    private Loader newCursorLoader() {
        Uri movieQueryUri = MovieContract.MovieEntry.CONTENT_URI.buildUpon()
                .appendPath(Long.toString(mMovie.getId())).build();
        return new CursorLoader(this,
                movieQueryUri,
                new String[]{COLUMN_MOVIE_ID, COLUMN_IMAGE},
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {
        if (loader.getId() == LOADER_MOVIE_BY_ID) {
            Cursor cursor = (Cursor) data;
            if (cursor == null) {
                return;
            }
            mFavorite = cursor.moveToFirst();
            setCorrectStarImage();
            setBackgroundImage(cursor);
            if (isOnline(this)) {
                fetchReviewsAndTrailers();
            } else {
                findViewById(R.id.movie_details_bonus_info_ll).setVisibility(View.GONE);
            }
        } else {
            if (loader.getId() == LOADER_MOVIE_REVIEWS) {
                setReviews((List<Review>) data);
            } else if (loader.getId() == LOADER_MOVIE_TRAILERS) {
                setTrailers((List<Trailer>) data);
            }
        }
        getSupportLoaderManager().destroyLoader(loader.getId());
    }

    private void fetchReviewsAndTrailers() {
        getSupportLoaderManager().initLoader(LOADER_MOVIE_REVIEWS, null, this);
        getSupportLoaderManager().initLoader(LOADER_MOVIE_TRAILERS, null, this);
    }

    private byte[] getImageFromDb(Cursor data) {
        return data.getBlob(data.getColumnIndex(COLUMN_IMAGE));
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        // Not implemented because not needed yet
    }

    public static class ReviewsAsyncTaskLoader extends AsyncTaskLoader<List<Review>> {

        private final Movie movie;

        ReviewsAsyncTaskLoader(@NonNull Context context, Movie movie) {
            super(context);
            this.movie = movie;
            forceLoad();
        }

        @Nullable
        @Override
        public List<Review> loadInBackground() {
            List<Review> reviews = null;
            String apiKey = getContext().getResources().getString(R.string.API_KEY_TMDB);
            URL reviewsRequestUrl = buildReviewsUrl(movie.getId(), apiKey);
            try {
                String jsonReviewsResponse = getResponseFromHttpUrl(reviewsRequestUrl);
                reviews = JsonUtils.getReviewsFromJson(jsonReviewsResponse);
            } catch (Exception e) {
                Log.e(MovieDetailsActivity.ReviewsAsyncTaskLoader.class.getSimpleName(), e.getLocalizedMessage());
            }
            return reviews;
        }
    }

    public static class TrailersAsyncTaskLoader extends AsyncTaskLoader<List<Trailer>> {

        private final Movie movie;

        TrailersAsyncTaskLoader(@NonNull Context context, Movie movie) {
            super(context);
            this.movie = movie;
            forceLoad();
        }

        @Nullable
        @Override
        public List<Trailer> loadInBackground() {
            List<Trailer> trailers = null;
            String apiKey = getContext().getResources().getString(R.string.API_KEY_TMDB);
            URL trailersRequestUrl = buildTrailersUrl(movie.getId(), apiKey);
            try {
                String jsonTrailersResponse = getResponseFromHttpUrl(trailersRequestUrl);
                trailers = JsonUtils.getTrailersFromJson(jsonTrailersResponse);
            } catch (Exception e) {
                Log.e(MovieDetailsActivity.TrailersAsyncTaskLoader.class.getSimpleName(), e.getLocalizedMessage());
            }
            return trailers;
        }
    }

}
