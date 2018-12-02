package com.example.divided.falldetector;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;
import com.example.divided.falldetector.model.SensorData;
import com.example.divided.falldetector.model.SensorDataPack;
import com.google.common.collect.Iterables;


import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.Queue;


public class SignalService extends Service implements com.example.divided.falldetector.model.SensorManager.OnSensorDataListener{

    private static final String TAG = SignalService.class.getSimpleName();
    private final double SAMPLING_PERIOD = 40.0; // przy 25 Hz najstabilniej i SENSOR_DELAY_GAME
    private final int BUFFER_SIZE = 64;
    private final int NOTIFICATION_ID = 69;
    private CircularFifoQueue<SensorData> buffer = new CircularFifoQueue<>(100); // ok 3s , po 75 probek na sensor



    NotificationManager notificationManager;

    com.example.divided.falldetector.model.SensorManager sensorManager;



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
        if (sensorManager != null) {
            sensorManager.unregisterListeners();
        }
        notificationManager.cancel(NOTIFICATION_ID);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();




        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        sensorManager = new com.example.divided.falldetector.model.SensorManager(this,SAMPLING_PERIOD);
        sensorManager.registerListeners();
        sensorManager.setOnSensorDataListener(this);


        setNotification();

        return START_STICKY;
    }


    private void setNotification() {


    }


    private void startAlarmActivity() {
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

    @Override
    public void onNewSensorData(SensorData sensorData) {
        buffer.add(sensorData);
        if(buffer.isAtFullCapacity()){
            final SensorDataPack sensorDataPack = new SensorDataPack(buffer);
            if(Algorithm.fallDetectionAlgorithm(sensorDataPack)){
                startAlarmActivity();
            }
            buffer.clear();
        }
    }
}
