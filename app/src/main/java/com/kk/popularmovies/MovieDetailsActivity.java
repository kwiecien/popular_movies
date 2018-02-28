package com.kk.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.kk.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.Locale;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String EXTRA_MOVIE = "com.kk.popularmovies.extra_movie";

    public static Intent newIntent(Context packageContext, Movie movie) {
        Intent intent = new Intent(packageContext, MovieDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_MOVIE, movie);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Intent intent = getIntent();
        Bundle extras = intent != null ? intent.getExtras() : null;
        Movie movie = extras != null ? (Movie) extras.getSerializable(EXTRA_MOVIE) : null;
        if (movie != null) {
            TextView movieTv = findViewById(R.id.movie_details_title_tv);
            TextView releaseDateTv = findViewById(R.id.movie_details_release_date_tv);
            TextView userRankingTv = findViewById(R.id.movie_details_user_rating_tv);
            TextView plotSynopsisTv = findViewById(R.id.movie_details_plot_synopsis_tv);
            movieTv.setText(movie.getTitle());
            releaseDateTv.setText(String.format(Locale.getDefault(), "(%d)", movie.getReleaseDate().getYear()));
            userRankingTv.setText(String.format(Locale.getDefault(), "%1.1f", movie.getUserRating()));
            plotSynopsisTv.setText(movie.getPlotSynopsis());
            String imageThumbnail = movie.getImageThumbnail();
            ImageView backgroundImage = findViewById(R.id.movie_details_background_iv);
            Picasso.with(this)
                    .load(imageThumbnail)
                    .into(backgroundImage);
            backgroundImage.setAlpha(0.10f);
        }
    }
}
