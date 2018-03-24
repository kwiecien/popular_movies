package com.kk.popularmovies.model;

public enum SortOrder {
    TOP_RATED {
        @Override
        public String getStringRepresentation() {
            return "TOP RATED";
        }

        @Override
        public SortOrder swap() {
            return POPULAR;
        }
    },
    POPULAR {
        @Override
        public String getStringRepresentation() {
            return "POPULAR";
        }

        @Override
        public SortOrder swap() {
            return FAVORITES;
        }
    },
    FAVORITES {
        @Override
        public String getStringRepresentation() {
            return "FAVORITES";
        }

        @Override
        public SortOrder swap() {
            return TOP_RATED;
        }
    };

    public abstract String getStringRepresentation();

    public abstract SortOrder swap();

}
