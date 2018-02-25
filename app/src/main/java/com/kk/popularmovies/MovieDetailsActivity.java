package com.kk.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MovieDetailsActivity extends AppCompatActivity {

    public static Intent newIntent(Context packageContext, String adapterPosition) {
        Intent intent = new Intent(packageContext, MovieDetailsActivity.class);
        intent.putExtra(Intent.EXTRA_INDEX, adapterPosition);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        String adapterPosition = getIntent().getStringExtra(Intent.EXTRA_INDEX);
        TextView movie = findViewById(R.id.movie_details_title_tv);
        movie.setText(adapterPosition);
    }
}
