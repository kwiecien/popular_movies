package com.kk.popularmovies;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kk.popularmovies.model.Movie;

import java.util.List;

public class MoviesAdapter extends ArrayAdapter<Movie> {

    public MoviesAdapter(Activity context, List<Movie> movies) {
        super(context, 0, movies);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie, parent, false);
        }
        Movie movie = getItem(position);

        ImageView posterIv = convertView.findViewById(R.id.list_item_poster_iv);
        int imageThumbnail = movie.getImageThumbnail();
        posterIv.setImageResource(imageThumbnail); // TODO: Correct way of handling imageThumbnails

        TextView titleTv = convertView.findViewById(R.id.list_item_title_tv);
        String title = movie.getTitle();
        titleTv.setText(title);

        return convertView;
    }
}
