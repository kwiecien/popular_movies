package com.kk.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kk.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Optional;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    private final MoviesAdapterOnClickHandler mClickHandler;
    private final Context mContext;
    private List<Movie> mMovies;

    public MoviesAdapter(MoviesAdapterOnClickHandler clickHandler, Context context) {
        mClickHandler = clickHandler;
        mContext = context;
    }

    @NonNull
    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new MoviesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviesAdapterViewHolder moviesAdapterViewHolder, int position) {
        moviesAdapterViewHolder.bind(position, mContext);
    }

    @Override
    public int getItemCount() {
        return Optional.ofNullable(mMovies)
                .map(List::size)
                .orElse(0);
    }

    public void setMoviesData(List<Movie> movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    public interface MoviesAdapterOnClickHandler {
        void onClick(Movie movie, ImageView sharedImageView);
    }

    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView mPosterIv;

        private MoviesAdapterViewHolder(View itemView) {
            super(itemView);
            mPosterIv = itemView.findViewById(R.id.list_item_poster_iv);
            itemView.setOnClickListener(this);
        }

        private void bind(int position, Context context) {
            Movie movie = mMovies.get(position);

            String imageThumbnail = movie.getImageThumbnail();
            Picasso.with(context)
                    .load(imageThumbnail)
                    .placeholder(android.R.drawable.stat_sys_download)
                    .error(android.R.drawable.stat_notify_error)
                    .into(mPosterIv);

            ViewCompat.setTransitionName(mPosterIv, movie.getTitle());
        }

        @Override
        public void onClick(View view) {
            mClickHandler.onClick(mMovies.get(getAdapterPosition()), mPosterIv);
        }
    }
}
