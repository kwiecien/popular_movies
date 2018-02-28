package com.kk.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kk.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesAdapterViewHolder> {

    private final MoviesAdapterOnClickHandler mClickHandler;
    private final Context mContext;
    private Movie[] mMovies;

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
        if (mMovies == null) {
            return 0;
        }
        return mMovies.length;
    }

    public void setMoviesData(Movie[] movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }

    public interface MoviesAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mPosterIv;

        public MoviesAdapterViewHolder(View itemView) {
            super(itemView);
            mPosterIv = itemView.findViewById(R.id.list_item_poster_iv);
            itemView.setOnClickListener(this);
        }

        public void bind(int position, Context context) {
            Movie movie = mMovies[position];

            String imageThumbnail = movie.getImageThumbnail();
            Picasso.with(context)
                    .load(imageThumbnail)
                    .into(mPosterIv);
        }

        @Override
        public void onClick(View view) {
            mClickHandler.onClick(mMovies[getAdapterPosition()]);
        }
    }
}
