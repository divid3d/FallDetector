package com.example.divided.falldetector.model;


import android.hardware.SensorManager;

public class LinearAccelerationData extends Data {

    public LinearAccelerationData(float[] values, long timestamp) {
        super(values, timestamp);
    }

    LinearAccelerationData(float x, float y, float z, long timestamp) {
        super(new float[]{x / SensorManager.GRAVITY_EARTH, y / SensorManager.GRAVITY_EARTH, z / SensorManager.GRAVITY_EARTH}, timestamp);
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
