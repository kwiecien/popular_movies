package com.kk.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kk.popularmovies.model.Movie;

import java.time.LocalDate;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    private final MoviesAdapterOnClickHandler mClickHandler;
    private Movie[] mMovies = {
            new Movie.Builder("Test0", LocalDate.now()).build(),
            new Movie.Builder("Test1", LocalDate.now()).build(),
            new Movie.Builder("Test2", LocalDate.now()).build(),
            new Movie.Builder("Test3", LocalDate.now()).build(),
            new Movie.Builder("Test4", LocalDate.now()).build(),
            new Movie.Builder("Test5", LocalDate.now()).build(),
            new Movie.Builder("Test6", LocalDate.now()).build(),
            new Movie.Builder("Test7", LocalDate.now()).build(),
    };

    public MoviesAdapter(MoviesAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MoviesAdapterViewHolder moviesAdapterViewHolder, int position) {
        moviesAdapterViewHolder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (mMovies == null) {
            return 0;
        }
        return mMovies.length;
    }

    public interface MoviesAdapterOnClickHandler {
        void onClick(int adapterPosition);
    }

    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mPosterIv;
        public final TextView mTitleTv;

        public MoviesAdapterViewHolder(View itemView) {
            super(itemView);
            mPosterIv = itemView.findViewById(R.id.list_item_poster_iv);
            mTitleTv = itemView.findViewById(R.id.list_item_title_tv);
            itemView.setOnClickListener(this);
        }

        public void bind(int position) {
            Movie movie = mMovies[position];

            int imageThumbnail = movie.getImageThumbnail();
            mPosterIv.setImageResource(imageThumbnail); // TODO: Correct way of handling imageThumbnails

            String title = movie.getTitle();
            mTitleTv.setText(title);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onClick(adapterPosition);
        }
    }
}
