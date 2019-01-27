package com.example.divided.falldetector;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.divided.falldetector.model.SensorData;
import com.example.divided.falldetector.model.SensorDataPack;

import org.apache.commons.collections4.queue.CircularFifoQueue;


public class SignalService extends Service implements com.example.divided.falldetector.model.SensorManager.OnSensorDataListener {

    private final int BUFFER_SIZE = 400;
    private final int NOTIFICATION_ID = 1;
    NotificationManager notificationManager;
    com.example.divided.falldetector.model.SensorManager sensorManager;
    private boolean isForegroundStarted = false;
    private CircularFifoQueue<SensorData> buffer = new CircularFifoQueue<>(BUFFER_SIZE);

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
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
        Log.e("SignalService", "onDestroy()");
        if (sensorManager != null) {
            sensorManager.unregisterListeners();
        }
        if (isForegroundStarted) {
            notificationManager.cancel(NOTIFICATION_ID);
            sendBroadcast(new Intent("service_stopped"));
            Toast.makeText(this, "Fall detection is disabled", Toast.LENGTH_LONG).show();
        }
        buffer.clear();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("SignalService", "onCreate()");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        double SAMPLING_PERIOD = 20.0;
        sensorManager = new com.example.divided.falldetector.model.SensorManager(this, SAMPLING_PERIOD);
        sensorManager.setOnSensorDataListener(this);
        isForegroundStarted = true;
        /*
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SignalServiceWakeLock");
        }
        wakeLock.acquire(1000);*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("SignalService", "onStartCommand()");
        if (isForegroundStarted) {
            boolean isSucceeded = sensorManager.registerListeners();
            if (!isSucceeded) {
                isForegroundStarted = false;
                stopSelf();
                Toast.makeText(this, "Smartphone doesn't have sensor necessary for properly work of application", Toast.LENGTH_LONG).show();
            } else {
                showNotification();
                Algorithm.init();
                Toast.makeText(this, "Fall detection is enabled", Toast.LENGTH_SHORT).show();
                sendBroadcast(new Intent("service_started"));
            }
        }
        return START_STICKY;
    }

    private void startAlarmActivity() {
        Log.e("SignalService", "Fall detected");
        Intent fallAlarm = new Intent(getApplicationContext(), FallDetectedActivity.class);
        fallAlarm.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        fallAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(fallAlarm);
    }

    public void showNotification() {

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setTicker("Fall detector")
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_my_launcher_icon))
                .setSmallIcon(R.drawable.ic_11015_falling_man)
                .setContentTitle("Fall detection is now running")
                .setContentText("Click to open application")
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(new Intent(this, MainActivity.class));
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, mBuilder.build());
    }

    @Override
    public void onNewSensorData(SensorData sensorData) {
        buffer.add(sensorData);
        if (buffer.isAtFullCapacity()) {
            final SensorDataPack sensorDataPack = new SensorDataPack(buffer);
            if (Algorithm.fallDetectionAlgorithm(sensorDataPack)) {
                stopSelf();
                startAlarmActivity();
            }
            buffer.clear();
        }
    }
}
