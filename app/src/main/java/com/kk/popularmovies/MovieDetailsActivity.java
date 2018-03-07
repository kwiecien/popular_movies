package com.kk.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.kk.popularmovies.model.Movie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;

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
        supportPostponeEnterTransition();

        Intent intent = getIntent();
        Bundle extras = intent != null ? intent.getExtras() : null;
        String transitionName = Optional.ofNullable(extras).map(e -> e.getString(EXTRA_TRANSITION)).orElse(null);
        Movie movie = extras != null ? (Movie) extras.getSerializable(EXTRA_MOVIE) : null;
        if (movie != null) {
            ButterKnife.bind(this);
            movieTv.setText(movie.getTitle());
            releaseDateTv.setText(String.format(Locale.getDefault(), "(%s)", getReleaseYear(movie)));
            userRankingTv.setText(String.format(Locale.getDefault(), "%1.1f", movie.getUserRating()));
            plotSynopsisTv.setText(movie.getPlotSynopsis());
            String imageThumbnail = movie.getImageThumbnail();
            ImageView backgroundImage = findViewById(R.id.movie_details_background_iv);
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
    }

    private String getReleaseYear(Movie movie) {
        return new SimpleDateFormat("YYYY", Locale.getDefault()).format(movie.getReleaseDate());
    }
}
