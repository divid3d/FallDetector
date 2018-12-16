package com.example.divided.falldetector;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button mStartStopServiceButton;
    LineChart chart;
    final private BroadcastReceiver mAccelerationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final float modVal = (float) intent.getDoubleExtra("accMod", 0);
            final float timestamp = (float) intent.getDoubleExtra("timestamp", 0);
            ChartPoint chartPoint = new ChartPoint(modVal, timestamp);
            ChartUtils.addEntry(chartPoint, chart, Color.WHITE);
        }
    };
    UserSettings userSettings;
    PermissionsManager permissionsManager;
    TimeCounter timeCounter;
    long startTime = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            timeCounter.setTime(millis);
            timerHandler.postDelayed(this, 250);
        }
    };
    private final BroadcastReceiver mServiceStopped = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("broadcast", "Service stopped");
            timerHandler.removeCallbacks(timerRunnable);
            timeCounter.reset();
            timeCounter.hide();
            mStartStopServiceButton.setText(R.string.start_detection);
            Animation chartOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.char_out_anim);
            chartOutAnimation.setFillAfter(true);
            chart.startAnimation(chartOutAnimation);
        }
    };
    private final BroadcastReceiver mServiceStarted = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("broadcast", "Service started");
            timeCounter.show();
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            mStartStopServiceButton.setText(R.string.stop_detection);
            Animation chartInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.chart_in_anim);
            chartInAnimation.setFillAfter(true);
            ChartUtils.clearChart(chart);
            chart.startAnimation(chartInAnimation);
        }
    };
    List<Entry> entries = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("MainActivity", "onCreate()");
        setContentView(R.layout.activity_main);
        userSettings = new UserSettings(this);
        permissionsManager = new PermissionsManager(this);


        Toolbar toolbar = findViewById(R.id.tool_bar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        mStartStopServiceButton = findViewById(R.id.btn_start_service);
        timeCounter = findViewById(R.id.time_counter);
        //timeCounter.setTypeFace(ResourcesCompat.getFont(this,R.font.product_sans_regular));

        chart = findViewById(R.id.chart);

        ChartUtils.setupChart(chart);
        ChartUtils.setupData(chart);

        MaterialRippleLayout.on(mStartStopServiceButton)
                .rippleColor(Color.WHITE)
                .rippleAlpha(0.3f)
                .rippleHover(true)
                .rippleOverlay(true)
                .rippleRoundedCorners(150)
                .create();

        permissionsManager.requestPermissions();

        mStartStopServiceButton.setOnClickListener(v -> {

            if (permissionsManager.checkAllPermissions()) {
                if (userSettings.verifySettings()) {
                    mStartStopServiceButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_tap_anim));
                    if (SignalService.isServiceRunning(this, SignalService.class)) {
                        stopService(new Intent(this, SignalService.class));
                    } else {
                        startService(new Intent(this, SignalService.class));
                    }
                } else {
                    Intent intent = new Intent(this, SettingsActivity.class);
                    startActivity(intent);
                }
            } else {
                permissionsManager.requestPermissions();
            }
        });




        /*rxPermissions
                .request(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M
                        Toast.makeText(this,"Permissions granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,"Permissions not granted",Toast.LENGTH_SHORT).show();
                    }
                })*/


        LocalBroadcastManager.getInstance(this).registerReceiver(mAccelerationReceiver, new IntentFilter("current_acceleration_data"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceStopped, new IntentFilter("service_stopped"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mServiceStarted, new IntentFilter("service_started"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("MainActivity", "onPause()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("MainActivity", "onResume()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("MainActivity", "onStop()");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings) {
            if (SignalService.isServiceRunning(this, SignalService.class)) {
                stopService(new Intent(this, SignalService.class));
            }
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("MainActivity", "onDestroy()");
        if (isFinishing()) {
            if (SignalService.isServiceRunning(this, SignalService.class)) {
                stopService(new Intent(this, SignalService.class));
            }
        }
    }

}
