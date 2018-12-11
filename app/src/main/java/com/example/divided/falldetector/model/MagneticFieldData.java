package com.example.divided.falldetector.model;

public class MagneticFieldData extends Data {
    public MagneticFieldData(float[] values, long timestamp) {
        super(values, timestamp);
    }

    MagneticFieldData(float x, float y, float z, long timestamp) {
        super(new float[]{x, y, z}, timestamp);
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
