package com.example.divided.falldetector.model;

public class GyroscopeData extends Data {

    public GyroscopeData(float[] values, long timestamp) {
        super(values, timestamp);
    }

    GyroscopeData(float x, float y, float z, long timestamp) {
        super(new float[]{(float) (x * (180 / Math.PI)), (float) (y * (180 / Math.PI)), (float) (z * (180 / Math.PI))}, timestamp);
    }

    public float getX() {
        return getValues()[0];
    }

    public float getY() {
        return getValues()[1];
    }

    public float getZ() {
        return getValues()[2];
    }

    public float getModule() {
        return (float) Math.sqrt(Math.pow(getValues()[0], 2) + Math.pow(getValues()[1], 2) + Math.pow(getValues()[2], 2));
    }
}
