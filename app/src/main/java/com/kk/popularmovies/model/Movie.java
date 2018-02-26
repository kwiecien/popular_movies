package com.kk.popularmovies.model;

import java.time.LocalDate;

public class Movie {
    private final String mTitle;
    private final LocalDate mReleaseDate;
    private final String mImageThumbnail;
    private final String mPlotSynopsis;
    private final double mUserRating;

    private Movie(Builder builder) {
        mTitle = builder.mTitle;
        mReleaseDate = builder.mReleaseDate;
        mPlotSynopsis = builder.mPlotSynopsis;
        mUserRating = builder.mUserRating;
        mImageThumbnail = makeImageThumbnail(builder.mPosterPath);
    }

    private String makeImageThumbnail(String filePath) {
        String baseUrl = "https://image.tmdb.org/t/p";
        String fileSize = "/w185";
        return baseUrl + fileSize + filePath;
    }

    public String getImageThumbnail() {
        return mImageThumbnail;
    }

    public String getTitle() {
        return mTitle;
    }

    public static class Builder {
        private final String mTitle;
        private final LocalDate mReleaseDate;
        private String mPosterPath = "";
        private String mPlotSynopsis = "";
        private double mUserRating = 0.0f;

        public Builder(String title, LocalDate releaseDate) {
            mTitle = title;
            mReleaseDate = releaseDate;
        }

        public Movie build() {
            return new Movie(this);
        }

        public Builder withPosterPath(String posterPath) {
            mPosterPath = posterPath;
            return this;
        }

        public Builder withPlotSynopsis(String plotSynopsis) {
            mPlotSynopsis = plotSynopsis;
            return this;
        }

        public Builder withUserRating(double userRating) {
            mUserRating = userRating;
            return this;
        }
    }

}
