package com.kk.popularmovies.model;

import java.time.LocalDate;

public class Movie {
    private final String mTitle;
    private final LocalDate mReleaseDate;
    private final int mImageThumbnail;
    private final String mPlotSynopsis;
    private final float mUserRating;

    private Movie(Builder builder) {
        mTitle = builder.mPlotSynopsis;
        mReleaseDate = builder.mReleaseDate;
        mImageThumbnail = builder.mImageThumbnail;
        mPlotSynopsis = builder.mPlotSynopsis;
        mUserRating = builder.mUserRating;
    }

    public int getImageThumbnail() {
        return mImageThumbnail;
    }

    public static class Builder {
        private final String mTitle;
        private final LocalDate mReleaseDate;
        private int mImageThumbnail = 0;
        private String mPlotSynopsis = "";
        private float mUserRating = 0.0f;

        public Builder(String title, LocalDate releaseDate) {
            mTitle = title;
            mReleaseDate = releaseDate;
        }

        public Builder withImageThumbnail(int imageThumbnail) {
            mImageThumbnail = imageThumbnail;
            return this;
        }

        public Builder withPlotSynopsis(String plotSynopsis) {
            mPlotSynopsis = plotSynopsis;
            return this;
        }

        public Builder withUserRating(float userRating) {
            mUserRating = userRating;
            return this;
        }
    }

}
