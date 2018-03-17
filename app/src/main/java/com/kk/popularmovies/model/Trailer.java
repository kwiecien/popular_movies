package com.kk.popularmovies.model;

public class Trailer {
    private final String mName;
    private final String mKey;

    public Trailer(String name, String key) {
        mName = name;
        mKey = key;
    }

    public String getName() {
        return mName;
    }

    public String getKey() {
        return mKey;
    }

    @Override
    public String toString() {
        return "Trailer{" +
                "mName='" + mName + '\'' +
                ", mKey='" + mKey + '\'' +
                '}';
    }
}
