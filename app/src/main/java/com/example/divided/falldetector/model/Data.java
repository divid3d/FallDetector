package com.example.divided.falldetector.model;

public class Data {

    private final float mValues[];
    private final long mTimestamp;

    public Data(float values[], long timestamp) {
        mValues = values;
        mTimestamp = timestamp;
    }

    public float[] getValues() {
        return mValues;
    }

    public long getTimestamp() {
        return mTimestamp;
    }
}
