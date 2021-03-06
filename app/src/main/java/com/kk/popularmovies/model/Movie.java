package com.kk.popularmovies.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class Movie implements Serializable {
    private final long mId;
    private final String mTitle;
    private final Date mReleaseDate;
    private final String mImageThumbnail;
    private final String mPlotSynopsis;
    private final double mUserRating;
    private final byte[] mImage;

    private Movie(Builder builder) {
        mId = builder.mId;
        mTitle = builder.mTitle;
        mReleaseDate = builder.mReleaseDate;
        mPlotSynopsis = builder.mPlotSynopsis;
        mUserRating = builder.mUserRating;
        mImageThumbnail = makeImageThumbnail(builder.mPosterPath);
        mImage = builder.mImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Movie movie = (Movie) o;
        return mId == movie.mId;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }

    private String makeImageThumbnail(String filePath) {
        String baseUrl = "https://image.tmdb.org/t/p";
        String fileSize = "/w185";
        return baseUrl + fileSize + filePath;
    }

    public String getImageThumbnail() {
        return mImageThumbnail;
    }

    public byte[] getImage() {
        return Arrays.copyOf(mImage, mImage.length);
    }

    public long getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public Date getReleaseDate() {
        return new Date(mReleaseDate.getTime());
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
        private final long mId;
        private final String mTitle;
        private final Date mReleaseDate;
        private String mPosterPath = "";
        private byte[] mImage = new byte[]{};
        private String mPlotSynopsis = "";
        private double mUserRating = 0.0f;

        public Builder(long id, String title, Date releaseDate) {
            mId = id;
            mTitle = title;
            mReleaseDate = new Date(releaseDate.getTime());
        }

        public Movie build() {
            return new Movie(this);
        }

        public Builder withPosterPath(String posterPath) {
            mPosterPath = posterPath;
            return this;
        }

        public Builder withImage(byte[] image) {
            mImage = Arrays.copyOf(image, image.length);
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
