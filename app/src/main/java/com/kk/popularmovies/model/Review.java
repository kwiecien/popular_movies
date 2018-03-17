package com.kk.popularmovies.model;

public class Review {
    private final String mAuthor;
    private final String mContent;

    public Review(String author, String content) {
        mAuthor = author;
        mContent = content;
    }

    @Override
    public String toString() {
        return "Review{" +
                "mAuthor='" + mAuthor + '\'' +
                ", mContent='" + mContent + '\'' +
                '}';
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getContent() {
        return mContent;
    }

}
