package com.kk.popularmovies.model;

import com.kk.popularmovies.R;

public enum SortOrder {
    TOP_RATED(R.string.pref_sort_label_top_rated) {
        @Override
        public SortOrder swap() {
            return POPULAR;
        }
    },
    POPULAR(R.string.pref_sort_label_popular) {
        @Override
        public SortOrder swap() {
            return FAVORITES;
        }
    },
    FAVORITES(R.string.pref_sort_label_favorites) {
        @Override
        public SortOrder swap() {
            return TOP_RATED;
        }
    };

    private final int resourceId;

    SortOrder(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public abstract SortOrder swap();

}
