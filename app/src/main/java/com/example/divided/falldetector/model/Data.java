package com.example.divided.falldetector.model;

public class Data {

    protected final float mValues[];
    protected final long mTimestamp;

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
