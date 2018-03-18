package com.kk.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kk.popularmovies.data.MovieContract;
import com.kk.popularmovies.model.Movie;
import com.kk.popularmovies.utilities.ReleaseDateUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.kk.popularmovies.data.MovieDbHelper.deleteMovieFromDb;
import static com.kk.popularmovies.data.MovieDbHelper.findFavoriteMovies;
import static com.kk.popularmovies.data.MovieDbHelper.insertMovieToDb;
import static com.kk.popularmovies.utilities.ReleaseDateUtils.getReleaseYear;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String EXTRA_MOVIE = "com.kk.popularmovies.extra_movie";
    private static final String EXTRA_TRANSITION = "com.kk.popularmovies.extra.transition";

    @BindView(R.id.movie_details_title_tv)
    TextView movieTv;
    @BindView(R.id.movie_details_release_date_tv)
    TextView releaseDateTv;
    @BindView(R.id.movie_details_user_rating_tv)
    TextView userRankingTv;
    @BindView(R.id.movie_details_plot_synopsis_tv)
    TextView plotSynopsisTv;
    @BindView(R.id.movie_details_star_iv)
    ImageView starTv;
    @BindView(R.id.reviews_ll)
    LinearLayout reviewsLl;
    @BindView(R.id.trailers_ll)
    LinearLayout trailersLl;

    private Movie mMovie;

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
        movieTv.setText(mMovie.getTitle());
        releaseDateTv.setText(String.format(Locale.getDefault(), "(%s)", getReleaseYear(mMovie)));
        userRankingTv.setText(String.format(Locale.getDefault(), "%1.1f", mMovie.getUserRating()));
        plotSynopsisTv.setText(mMovie.getPlotSynopsis());
        starTv.setImageResource(determineStar());
        setReviews();
        setTrailers();
    }

    private int determineStar() {
        return getFavoriteMoviesAsList(findFavoriteMovies(this)).contains(mMovie) ?
                android.R.drawable.star_big_on :
                android.R.drawable.star_big_off;
    }

    private void setReviews() {
        TextView textView1 = new TextView(this);
        textView1.setText("Review 1\nReview 1\nReview 1\nReview 1\n");
        TextView textView2 = new TextView(this);
        textView2.setText("Review 2\nReview 2\nReview 2\nReview 2\n");
        reviewsLl.addView(textView1);
        reviewsLl.addView(textView2);
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
        starTv.setOnClickListener(
                v -> handleFavoriteMovie()
        );
    }

    private void handleFavoriteMovie() {
        Cursor moviesCursor = findFavoriteMovies(this);
        List<Movie> favoriteMovies = getFavoriteMoviesAsList(moviesCursor);
        if (favoriteMovies.contains(mMovie)) {
            deleteMovieFromDb(this, mMovie);
        } else {
            insertMovieToDb(this, mMovie);
        }
        starTv.setImageResource(determineStar());
    }

    @NonNull
    private List<Movie> getFavoriteMoviesAsList(Cursor moviesCursor) {
        List<Movie> favoriteMovies = new ArrayList<>();
        int indexMovieId = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        int indexTitle = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
        int indexReleaseDate = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        int indexImageThumbnail = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_IMAGE_THUMBNAIL);
        int indexPlotSynopsis = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS);
        int indexUserRanking = moviesCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_USER_RATING);
        for (moviesCursor.moveToFirst(); !moviesCursor.isAfterLast(); moviesCursor.moveToNext()) {
            long id = moviesCursor.getLong(indexMovieId);
            String title = moviesCursor.getString(indexTitle);
            Date releaseDate = ReleaseDateUtils.parseDate(moviesCursor.getString(indexReleaseDate));
            String imageThumbnail = moviesCursor.getString(indexImageThumbnail);
            String plotSynopsis = moviesCursor.getString(indexPlotSynopsis);
            double userRanking = moviesCursor.getDouble(indexUserRanking);
            Movie movie = new Movie.Builder(id, title, releaseDate)
                    .withPlotSynopsis(plotSynopsis)
                    .withPosterPath(imageThumbnail.substring(imageThumbnail.lastIndexOf('/')))
                    .withUserRating(userRanking)
                    .build();
            favoriteMovies.add(movie);
        }
        return favoriteMovies;
    }


    private void displayBackgroundImage(Bundle extras, String imageThumbnail, ImageView backgroundImage) {
        String transitionName = Optional.ofNullable(extras).map(ext -> ext.getString(EXTRA_TRANSITION)).orElse(null);
        backgroundImage.setTransitionName(transitionName);
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
}
