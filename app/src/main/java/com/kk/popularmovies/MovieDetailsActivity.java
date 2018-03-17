package com.kk.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kk.popularmovies.model.Movie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import butterknife.BindView;
import butterknife.ButterKnife;

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
    LinearLayout trailers_ll;

    private Movie movie;

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
            movie = (Movie) savedInstanceState.getSerializable(EXTRA_MOVIE);
        } else {
            Intent intent = getIntent();
            extras = Optional.ofNullable(intent).map(Intent::getExtras).orElse(null);
            movie = Optional.ofNullable(extras).map(ext -> (Movie) ext.getSerializable(EXTRA_MOVIE)).orElse(null);
        }
        if (movie != null) {
            setViewsContent(movie);
            setBackgroundImage(extras, movie);
            setOnClickListeners();
        }
    }

    private void setViewsContent(Movie movie) {
        movieTv.setText(movie.getTitle());
        releaseDateTv.setText(String.format(Locale.getDefault(), "(%s)", getReleaseYear(movie)));
        userRankingTv.setText(String.format(Locale.getDefault(), "%1.1f", movie.getUserRating()));
        plotSynopsisTv.setText(movie.getPlotSynopsis());
        starTv.setImageResource(determineIfFavorite(movie.getTitle()));
        setReviews();
        setTrailers();
    }

    private int determineIfFavorite(String title) {
        return ThreadLocalRandom.current().nextInt(2) % 2 == 0 ?
                android.R.drawable.star_big_on :
                android.R.drawable.star_big_off; // TODO Set star according to true data
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
        trailers_ll.addView(textView1);
        trailers_ll.addView(textView2);
    }

    private void setBackgroundImage(Bundle extras, Movie movie) {
        String imageThumbnail = movie.getImageThumbnail();
        ImageView backgroundImage = findViewById(R.id.movie_details_background_iv);
        displayBackgroundImage(extras, imageThumbnail, backgroundImage);
    }

    private void setOnClickListeners() {
        starTv.setOnClickListener(
                v -> Toast.makeText(this, "Star clicked", Toast.LENGTH_SHORT).show()
        );
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

    private String getReleaseYear(Movie movie) {
        return new SimpleDateFormat("yyyy", Locale.getDefault()).format(movie.getReleaseDate());
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
        outState.putSerializable(EXTRA_MOVIE, movie);
        super.onSaveInstanceState(outState);
    }
}
