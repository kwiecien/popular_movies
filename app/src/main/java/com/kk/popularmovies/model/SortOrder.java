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
    };

    public static SortOrder swap(SortOrder sortOrder) {
        return sortOrder == TOP_RATED ? POPULAR : TOP_RATED;
    }

    public abstract String getStringRepresentation();

}
