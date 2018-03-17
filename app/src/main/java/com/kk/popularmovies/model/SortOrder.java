package com.kk.popularmovies.model;

public enum SortOrder {
    TOP_RATED {
        @Override
        public String getStringRepresentation() {
            return "TOP RATED";
        }
    },
    POPULAR {
        @Override
        public String getStringRepresentation() {
            return "POPULAR";
        }
    },
    FAVORITES {
        @Override
        public String getStringRepresentation() {
            return "FAVORITES";
        }
    };

    public static SortOrder swap(SortOrder sortOrder) {
        switch (sortOrder) {
            case TOP_RATED:
                return POPULAR;
            case POPULAR:
                return FAVORITES;
            case FAVORITES:
                return TOP_RATED;
        }
        throw new IllegalArgumentException("Unknown SortOrder!");
    }

    public abstract String getStringRepresentation();

}
