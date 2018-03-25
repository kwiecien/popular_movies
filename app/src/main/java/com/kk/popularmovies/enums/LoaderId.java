package com.kk.popularmovies.enums;

public class LoaderId {

    private LoaderId() {
        // Utility classes should not have public constructors
    }

    public static class MovieDetails {

        public static final int MOVIE_BY_ID = 100;
        public static final int MOVIE_REVIEWS = 101;
        public static final int MOVIE_TRAILERS = 102;

        private MovieDetails() {
            // Utility classes should not have public constructors
        }
    }

    public static class MoviePosters {
        public static final int FAVORITE_MOVIES = 200;
        public static final int TOP_RATED_MOVIES = 201;
        public static final int POPULAR_MOVIES = 202;

        private MoviePosters() {
            // Utility classes should not have public constructors
        }
    }

}
