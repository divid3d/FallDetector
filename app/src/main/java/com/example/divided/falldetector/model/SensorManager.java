package com.example.divided.falldetector.model;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class SensorManager implements SensorEventListener {

    private android.hardware.SensorManager mSensorManager;
    private Sensor mSensorLinearAcceleration;
    private Sensor mSensorGyroscope;
    private Sensor mSensorMagneticField;
    private Sensor mSensorRotationVector;
    private double mSaplingMillis;
    private OnSensorDataListener mListener;
    private Context mContext;

    public SensorManager(Context context, double samplingMillis) {
        mContext = context;
        mSaplingMillis = samplingMillis;
        mSensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void setOnSensorDataListener(OnSensorDataListener listener) {
        if (mListener == null) {
            mListener = listener;
        }
    }

    public void unregisterListeners() {
        mSensorManager.unregisterListener(this, mSensorLinearAcceleration);
        mSensorManager.unregisterListener(this, mSensorGyroscope);
        mSensorManager.unregisterListener(this, mSensorMagneticField);
        mSensorManager.unregisterListener(this, mSensorRotationVector);
    }

    public boolean registerListeners() {
        boolean success = true;
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            mSensorLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            mSensorManager.registerListener(this, mSensorLinearAcceleration, (int) (mSaplingMillis * 1000));
        } else {
            Log.e("Sensor Manager", "Phone don't have linear acceleration sensor");
            success = false;
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mSensorManager.registerListener(this, mSensorGyroscope, (int) (mSaplingMillis * 1000));
        } else {
            Log.e("Sensor Manager", "Phone don't have gyroscope sensor");
            success = false;
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorManager.registerListener(this, mSensorMagneticField, (int) (mSaplingMillis * 1000));
        } else {
            Log.e("Sensor Manager", "Phone don't have magnetic field sensor");
            success = false;
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            mSensorRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(this, mSensorRotationVector, (int) (mSaplingMillis * 1000));
        } else {
            Log.e("Sensor Manager", "Phone don't have rotation vector sensor");
            success = false;
        }
        return success;
    }

    @Override
    final public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                LinearAccelerationData data = new LinearAccelerationData(event.values[0], event.values[1], event.values[2], event.timestamp);
                sendMessageToActivity(data.getModule());
                if (mListener != null) {
                    mListener.onNewSensorData(new SensorData(data, SensorData.SensorType.SENSOR_LINEAR_ACCELERATION));
                }
                break;

            case Sensor.TYPE_GYROSCOPE:
                if (mListener != null) {
                    mListener.onNewSensorData(new SensorData(new GyroscopeData(event.values[0], event.values[1], event.values[2], event.timestamp), SensorData.SensorType.SENSOR_GYROSCOPE));
                }
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                if (mListener != null) {
                    mListener.onNewSensorData(new SensorData(new MagneticFieldData(event.values[0], event.values[1], event.values[2], event.timestamp), SensorData.SensorType.SENSOR_MAGNETIC_FIELD));
                }
                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                if (mListener != null) {
                    mListener.onNewSensorData(new SensorData(new RotationVectorData(event.values[0], event.values[1], event.values[2], event.values[3], event.values[4], event.timestamp), SensorData.SensorType.SENSOR_ROTATION_VECTOR));
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void sendMessageToActivity(double accMod) {
        Intent intent = new Intent("current_acceleration_data");
        intent.putExtra("accMod", accMod);
        mContext.sendBroadcast(intent);
    }

    public interface OnSensorDataListener {
        void onNewSensorData(SensorData sensorData);
    }
}
