package com.example.divided.falldetector;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.support.v4.content.LocalBroadcastManager;

import com.example.divided.falldetector.model.GyroscopeData;
import com.example.divided.falldetector.model.LinearAccelerationData;
import com.example.divided.falldetector.model.MagneticFieldData;
import com.example.divided.falldetector.model.RotationVectorData;
import com.example.divided.falldetector.model.SensorData;

public class SensorManager implements SensorEventListener,SensorManager.OnSensorDataListener {

    private static SensorManager instance;
    private android.hardware.SensorManager mSensorManager;
    private Sensor sensorLinearAcceleration;
    private Sensor sensorGyroscope;
    private Sensor sensorMagneticField;
    private Sensor sensorRotationVector;
    private double samplingMillis;
    private double lastLinearAccelerationSensorTimestamp = 0;
    private double lastGyroscopeSensorTimestamp = 0;
    private double lastMagneticFieldSensorTimestamp = 0;
    private double lastRotationVectorSensorTimestamp = 0;
    private OnSensorDataListener listener;
    private Context mContext;

    private SensorManager(Context context, double samplingMillis) {
        mContext = context;
        mSensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.samplingMillis = samplingMillis;

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensorLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            mSensorManager.registerListener(this, sensorLinearAcceleration, android.hardware.SensorManager.SENSOR_DELAY_GAME);

        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            sensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mSensorManager.registerListener(this, sensorGyroscope, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            sensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorManager.registerListener(this, sensorMagneticField, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            sensorRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(this, sensorRotationVector, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }

    }

    public static synchronized SensorManager getInstance(Context context, double samplingMillis) {
        if (instance == null) {
            instance = new SensorManager(context.getApplicationContext(), samplingMillis);
        }

        return instance;
    }

    public void setOnSensorDataListener(OnSensorDataListener listener){
        if(this.listener == null){
            this.listener = listener;
        }
    }



    public void unregisterListiners() {
        if (sensorLinearAcceleration != null) {
            mSensorManager.unregisterListener(this, sensorLinearAcceleration);
        }

        if (sensorGyroscope != null) {
            mSensorManager.unregisterListener(this, sensorGyroscope);
        }

        if (sensorMagneticField != null) {
            mSensorManager.unregisterListener(this, sensorMagneticField);
        }

        if (sensorRotationVector != null) {
            mSensorManager.unregisterListener(this, sensorRotationVector);
        }
    }

    public void registerListeners() {
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensorLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            mSensorManager.registerListener(this, sensorLinearAcceleration, android.hardware.SensorManager.SENSOR_DELAY_GAME);

        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            sensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mSensorManager.registerListener(this, sensorGyroscope, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            sensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorManager.registerListener(this, sensorMagneticField, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            sensorRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(this, sensorRotationVector, android.hardware.SensorManager.SENSOR_DELAY_GAME);
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_LINEAR_ACCELERATION:
                if ((event.timestamp - lastLinearAccelerationSensorTimestamp) / 1000000.0f >= samplingMillis) {
                    LinearAccelerationData data=  new LinearAccelerationData(event.values[0], event.values[1], event.values[2], event.timestamp);
                    sendMessageToActivity(data.getModule(),data.getTimestamp());
                    if(listener!=null){
                        listener.onNewSensorData(new SensorData(data,SensorData.SensorType.SENSOR_LINEAR_ACCELERATION));
                    }
                    lastLinearAccelerationSensorTimestamp = event.timestamp;
                }
                break;

            case Sensor.TYPE_GYROSCOPE:
                if ((event.timestamp - lastGyroscopeSensorTimestamp) / 1000000.0f >= samplingMillis) {
                    if(listener!=null){
                        listener.onNewSensorData(new SensorData(new GyroscopeData(event.values[0], event.values[1], event.values[2], event.timestamp), SensorData.SensorType.SENSOR_GYROSCOPE));
                    }
                    lastGyroscopeSensorTimestamp = event.timestamp;
                }
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                if ((event.timestamp - lastMagneticFieldSensorTimestamp) / 1000000.0f >= samplingMillis) {
                    if(listener!=null){
                        listener.onNewSensorData(new SensorData(new MagneticFieldData(event.values, event.timestamp), SensorData.SensorType.SENSOR_MAGNETIC_FIELD));
                    }
                    lastMagneticFieldSensorTimestamp = event.timestamp;
                }
                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                if ((event.timestamp - lastRotationVectorSensorTimestamp) / 1000000.0f >= samplingMillis) {
                    if(listener!=null){
                        listener.onNewSensorData(new SensorData(new RotationVectorData(event.values, event.timestamp), SensorData.SensorType.SENSOR_ROTATION_VECTOR));
                    }
                    lastRotationVectorSensorTimestamp = event.timestamp;
                }
                break;
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface OnSensorDataListener{
        public void onNewSensorData(SensorData sensorData);
    }

    private void sendMessageToActivity(double modVal, double timestamp) {
        Intent intent = new Intent("intentKey");
// You can also include some extra data.
        intent.putExtra("key", modVal);
        intent.putExtra("timestamp", timestamp);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

}
