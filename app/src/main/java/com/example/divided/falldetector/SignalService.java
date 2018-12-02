package com.example.divided.falldetector;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.divided.falldetector.model.GyroscopeData;
import com.example.divided.falldetector.model.LinearAccelerationData;
import com.example.divided.falldetector.model.MagneticFieldData;
import com.example.divided.falldetector.model.RotationVectorData;
import com.example.divided.falldetector.model.SensorData;
import com.example.divided.falldetector.model.SensorDataPack;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

public class SignalService extends Service {

    private static final String TAG = SignalService.class.getSimpleName();
    private final double SAMPLING_PERIOD = 40.0; // przy 25 Hz najstabilniej i SENSOR_DELAY_GAME
    private final int BUFFER_SIZE = 64;
    private final int NOTIFICATION_ID = 69;
    SensorEventListener accelerometerEventListener;
    SensorEventListener gyroscopeEventListener;
    SensorEventListener magnetometerEvenListener;
    SensorEventListener rotationVectorEventListener;
    NotificationManager notificationManager;

    private SensorManager mSensorManager;

    private Sensor linearAccelerometer;
    private Sensor gyroscope;
    private Sensor magnetometer;
    private Sensor rotationVector;

    private List<LinearAccelerationData> linearAccelerationData = new ArrayList<>();
    private List<GyroscopeData> gyroscopeData = new ArrayList<>();
    private List<MagneticFieldData> magneticFieldData = new ArrayList<>();
    private List<RotationVectorData> rotationVectorData = new ArrayList<>();

    private double lastAccelerometerMeasurement = 0;
    private double lastGyroscopeMeasurement = 0;
    private double lastMagnetometerMeasurement = 0;
    private double lastRotationVectorMeasuremt = 0;

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(accelerometerEventListener, linearAccelerometer);
            mSensorManager.unregisterListener(gyroscopeEventListener, gyroscope);
            mSensorManager.unregisterListener(magnetometerEvenListener, magnetometer);
            mSensorManager.unregisterListener(rotationVectorEventListener, rotationVector);
        }
        notificationManager.cancel(NOTIFICATION_ID);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();



        RingBuffer buffer = new RingBuffer(100);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        accelerometerEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                    final double timeDiffMills = (event.timestamp - lastAccelerometerMeasurement) / 1000000.0f; // time differance in mills
                    if (timeDiffMills >= SAMPLING_PERIOD) {
                        linearAccelerationData.add(new LinearAccelerationData(event.values[0], event.values[1], event.values[2], event.timestamp));
                        buffer.enqueue(new SensorData(new LinearAccelerationData(event.values[0], event.values[1], event.values[2], event.timestamp), SensorData.SensorType.SENSOR_LINEAR_ACCELERATION));
                        //Log.e("Buffer","Buffer size:\t"+buffer.size()+"\tBuffer isFull:\t"+buffer.isAtFullCapacity());

                        //Log.e("Accelerometer", String.valueOf(linearAccelerationData.size() - 1) + "\tperiod:\t" + String.valueOf(timeDiffMills) + "\tmod= " + String.format("%.5f", Iterables.getLast(linearAccelerationData).getModule()));
                        //Log.e("Accelerometer_period",String.valueOf(timeDiffMills));
                        sendMessageToActivity(Iterables.getLast(linearAccelerationData).getModule(), event.timestamp);


                        if (linearAccelerationData.size() >= BUFFER_SIZE && gyroscopeData.size() >= BUFFER_SIZE && magneticFieldData.size() >= BUFFER_SIZE && rotationVectorData.size() >= BUFFER_SIZE) {
                            SensorDataPack sensorDataPack = new SensorDataPack(linearAccelerationData, gyroscopeData, magneticFieldData, rotationVectorData);
                            if (Algorithm.fallDetectionAlgorithm(linearAccelerationData, gyroscopeData, magneticFieldData, rotationVectorData, BUFFER_SIZE)) {
                                startAlarmActovity();
                            }
                            linearAccelerationData.clear();
                            gyroscopeData.clear();
                            magneticFieldData.clear();
                            rotationVectorData.clear();
                        }
                        lastAccelerometerMeasurement = event.timestamp;
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        gyroscopeEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    final double timeDiffMills = (event.timestamp - lastGyroscopeMeasurement) / 1000000.0f; // time differance in mills
                    if (timeDiffMills >= SAMPLING_PERIOD) {
                        gyroscopeData.add(new GyroscopeData(event.values[0], event.values[1], event.values[2], event.timestamp));
                        buffer.enqueue(new SensorData(new GyroscopeData(event.values[0], event.values[1], event.values[2], event.timestamp), SensorData.SensorType.SENSOR_GYROSCOPE));
                        //Log.e("Buffer","Buffer size:\t"+buffer.size()+"\tBuffer isFull:\t"+buffer.isAtFullCapacity());

                        //Log.e("Gyroscope", String.valueOf(gyroscopeData.size() - 1) + "\tperiod:\t" + String.valueOf(timeDiffMills) + "\tmod= " + String.format("%.5f", Iterables.getLast(gyroscopeData).getModule()));

                        lastGyroscopeMeasurement = event.timestamp;
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        magnetometerEvenListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    final double timeDiffMills = (event.timestamp - lastMagnetometerMeasurement) / 1000000.0f; // time differance in mills
                    if (timeDiffMills >= SAMPLING_PERIOD) {
                        magneticFieldData.add(new MagneticFieldData(event.values, event.timestamp));
                        buffer.enqueue(new SensorData(new MagneticFieldData(event.values, event.timestamp), SensorData.SensorType.SENSOR_MAGNETIC_FIELD));
                        //Log.e("Buffer","Buffer size:\t"+buffer.size()+"\tBuffer isFull:\t"+buffer.isAtFullCapacity());

                        //Log.e("Magnetometer", String.valueOf(magneticFieldData.size() - 1) + "\tperiod:\t" + String.valueOf(timeDiffMills) + "\tmod= " + String.format("%.5f", Iterables.getLast(magneticFieldData).getModule()));

                        lastMagnetometerMeasurement = event.timestamp;
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        rotationVectorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                    final double timeDiffMills = (event.timestamp - lastRotationVectorMeasuremt) / 1000000.0f; // time differance in mills
                    if (timeDiffMills >= SAMPLING_PERIOD) {
                        rotationVectorData.add(new RotationVectorData(event.values, event.timestamp));
                        buffer.enqueue(new SensorData(new RotationVectorData(event.values, event.timestamp), SensorData.SensorType.SENSOR_ROTATION_VECTOR));
                        //Log.e("Buffer","Buffer size:\t"+buffer.size()+"\tBuffer isFull\t"+buffer.isAtFullCapacity());
                        //Log.e("Rotation vector", String.valueOf(rotationVectorData.size() - 1) + "\tperiod:\t" + String.valueOf(timeDiffMills) + "\tmod= " + String.format("%.5f", Iterables.getLast(rotationVectorData).getCos()));

                        lastRotationVectorMeasuremt = event.timestamp;
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            linearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            mSensorManager.registerListener(accelerometerEventListener, linearAccelerometer, SensorManager.SENSOR_DELAY_GAME);

        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            mSensorManager.registerListener(gyroscopeEventListener, gyroscope, SensorManager.SENSOR_DELAY_GAME);
        }


        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorManager.registerListener(magnetometerEvenListener, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            rotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            mSensorManager.registerListener(rotationVectorEventListener, rotationVector, SensorManager.SENSOR_DELAY_GAME);
        }

        setNotification();

        return START_STICKY;
    }


    private void setNotification() {


    }


    private void startAlarmActovity() {
        Log.e("Fall detected", "Fall detected");
        Intent dialogIntent = new Intent(this, FallDetectedActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("fall_detected"));
    }

    public void showNotification() {
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        Resources r = getResources();
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker("Fall detector")
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle("Fall detection is now running")
                .setContentText("Click to open application")
                .setContentIntent(pi)
                .setOngoing(true)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void sendMessageToActivity(double modVal, double timestamp) {
        Intent intent = new Intent("intentKey");
// You can also include some extra data.
        intent.putExtra("key", modVal);
        intent.putExtra("timestamp", timestamp);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private double calculateAccelNormal(double aX, double aY, double aZ, double thetaY, double thetaZ) {
        return Math.abs(aX * Math.sin(thetaZ) + aY * Math.sin(thetaY) - aZ * Math.cos(thetaY) * Math.cos(thetaZ));
    }

    public class LocalBinder extends Binder {
        SignalService getService() {
            Log.d(TAG, "getService()");
            return SignalService.this;
        }
    }

}
