package com.example.divided.falldetector.model;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class SensorManager implements SensorEventListener {

    private android.hardware.SensorManager mSensorManager;
    private Sensor sensorLinearAcceleration;
    private Sensor sensorGyroscope;
    private Sensor sensorMagneticField;
    private Sensor sensorRotationVector;
    private double mSaplingMillis;
    private double lastLinearAccelerationSensorTimestamp = 0;
    private double lastGyroscopeSensorTimestamp = 0;
    private double lastMagneticFieldSensorTimestamp = 0;
    private double lastRotationVectorSensorTimestamp = 0;
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
        mSensorManager.unregisterListener(this, sensorLinearAcceleration);
        mSensorManager.unregisterListener(this, sensorGyroscope);
        mSensorManager.unregisterListener(this, sensorMagneticField);
        mSensorManager.unregisterListener(this, sensorRotationVector);
    }

    public boolean registerListeners() {
        boolean success = true;
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensorLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            mSensorManager.registerListener(this, sensorLinearAcceleration, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }else{
            Log.e("Sensor Manager","Phone don't have linear acceleration sensor");
            success = false;
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            sensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mSensorManager.registerListener(this, sensorGyroscope, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }else{
            Log.e("Sensor Manager","Phone don't have gyroscope sensor");
            success = false;
        }


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            sensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorManager.registerListener(this, sensorMagneticField, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }else{
            Log.e("Sensor Manager","Phone don't have magnetic field sensor");
            success = false;
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            sensorRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(this, sensorRotationVector, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }else{
            Log.e("Sensor Manager","Phone don't have rotation vector sensor");
            success = false;
        }

        return success;
    }

    @Override
    final public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                if ((event.timestamp - lastLinearAccelerationSensorTimestamp) / 1000000.0f >= mSaplingMillis) {
                    //Log.e("Sensor period",String.valueOf((event.timestamp - lastLinearAccelerationSensorTimestamp) / 1000000.0f)+" ms");
                    //Log.e("Sensor freq",String.valueOf(1/((event.timestamp - lastLinearAccelerationSensorTimestamp) / 1000000000.0f))+" Hz");
                    LinearAccelerationData data = new LinearAccelerationData(event.values[0], event.values[1], event.values[2], event.timestamp);
                    sendMessageToActivity(data.getModule(), data.getTimestamp());
                    if (mListener != null) {
                        mListener.onNewSensorData(new SensorData(data, SensorData.SensorType.SENSOR_LINEAR_ACCELERATION));
                    }
                    lastLinearAccelerationSensorTimestamp = event.timestamp;
                }
                break;

            case Sensor.TYPE_GYROSCOPE:
                if ((event.timestamp - lastGyroscopeSensorTimestamp) / 1000000.0f >= mSaplingMillis) {
                    if (mListener != null) {
                        mListener.onNewSensorData(new SensorData(new GyroscopeData(event.values[0], event.values[1], event.values[2], event.timestamp), SensorData.SensorType.SENSOR_GYROSCOPE));
                    }
                    lastGyroscopeSensorTimestamp = event.timestamp;
                }
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                if ((event.timestamp - lastMagneticFieldSensorTimestamp) / 1000000.0f >= mSaplingMillis) {
                    if (mListener != null) {
                        mListener.onNewSensorData(new SensorData(new MagneticFieldData(event.values[0], event.values[1], event.values[2], event.timestamp), SensorData.SensorType.SENSOR_MAGNETIC_FIELD));
                    }
                    lastMagneticFieldSensorTimestamp = event.timestamp;
                }
                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                if ((event.timestamp - lastRotationVectorSensorTimestamp) / 1000000.0f >= mSaplingMillis) {
                    if (mListener != null) {
                        mListener.onNewSensorData(new SensorData(new RotationVectorData(event.values[0], event.values[1], event.values[2], event.values[3], event.values[4], event.timestamp), SensorData.SensorType.SENSOR_ROTATION_VECTOR));
                    }
                    lastRotationVectorSensorTimestamp = event.timestamp;
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void sendMessageToActivity(double accMod, double timestamp) {
        Intent intent = new Intent("current_acceleration_data");
        intent.putExtra("accMod", accMod);
        intent.putExtra("timestamp", timestamp);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    public interface OnSensorDataListener {
        void onNewSensorData(SensorData sensorData);
    }

}
