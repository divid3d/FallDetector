package com.example.divided.falldetector.model;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

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

    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
