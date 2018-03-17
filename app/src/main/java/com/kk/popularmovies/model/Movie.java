package com.kk.popularmovies.model;

import java.io.Serializable;
import java.util.Date;

public class Movie implements Serializable {
    private final String mTitle;
    private final Date mReleaseDate;
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

    public Date getReleaseDate() {
        return mReleaseDate;
    }

    public double getUserRating() {
        return mUserRating;
    }

    public String getPlotSynopsis() {
        return mPlotSynopsis;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "mTitle='" + mTitle + '\'' +
                ", mReleaseDate=" + mReleaseDate +
                ", mUserRating=" + mUserRating +
                '}';
    }

    public static class Builder {
        private final String mTitle;
        private final Date mReleaseDate;
        private String mPosterPath = "";
        private String mPlotSynopsis = "";
        private double mUserRating = 0.0f;

        public Builder(String title, Date releaseDate) {
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
