package com.example.divided.falldetector;


import android.Manifest;
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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button mStartStopServiceButton;
    LineChart chart;
    TimeCounter timeCounter;

    private final BroadcastReceiver mFallDetected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("broadcast", "StopSamplingService()");
            stopSamplingService();
            timeCounter.hide();
            timeCounter.reset();
            Intent fallAlarm = new Intent(getApplicationContext(), FallDetectedActivity.class);
            fallAlarm.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            fallAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(fallAlarm);
        }
    };
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
    List<Entry> entries = new ArrayList<>();
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final float modVal = (float) intent.getDoubleExtra("key", 0);
            final float timestamp = (float) intent.getDoubleExtra("timestamp", 0);
            ChartPoint chartPoint = new ChartPoint(modVal, timestamp);
            ChartUtils.addEntry(chartPoint, chart, Color.WHITE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity", "onCreate()");
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        mStartStopServiceButton = findViewById(R.id.btn_start_service);
        timeCounter = findViewById(R.id.time_counter);
        //timeCounter.setTypeFace(ResourcesCompat.getFont(this,R.font.product_sans_regular));

        chart = findViewById(R.id.chart);

        ChartUtils.setupChart(chart, 0, 6);
        ChartUtils.setupData(chart);

        MaterialRippleLayout.on(mStartStopServiceButton)
                .rippleColor(Color.WHITE)
                .rippleAlpha(0.3f)
                .rippleHover(true)
                .rippleOverlay(true)
                .rippleRoundedCorners(150)
                .create();

        mStartStopServiceButton.setOnClickListener(v -> {
            mStartStopServiceButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_tap_anim));
            if (SignalService.isServiceRunning(this, SignalService.class)) {
                stopSamplingService();
                timerHandler.removeCallbacks(timerRunnable);
                timeCounter.hide();
                timeCounter.reset();
            } else {
                startTime = System.currentTimeMillis();
                timerHandler.postDelayed(timerRunnable, 0);
                timeCounter.show();
                startSamplingService();
            }
        });

        RxPermissions rxPermissions = new RxPermissions(this);

        rxPermissions
                .request(Manifest.permission.SEND_SMS)
                .subscribe(granted -> {
                    if (granted) { // Always true pre-M

                    } else {

                    }
                });

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("intentKey"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mFallDetected, new IntentFilter("fall_detected"));


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("MainActivity", "onPause()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("MainActivity", "onResume()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("MainActivity", "onStop()");
    }

    private void startSamplingService() {
        Intent intent = new Intent(this, SignalService.class);
        startService(intent);
        mStartStopServiceButton.setText(R.string.stop_detection);
        Animation chartInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        chartInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ChartUtils.clearChart(chart);
                chart.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        chart.startAnimation(chartInAnimation);
    }

    private void stopSamplingService() {
        stopService(new Intent(this, SignalService.class));
        mStartStopServiceButton.setText(R.string.start_detection);
        Animation chartOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        chartOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                chart.setVisibility(View.INVISIBLE);
                ChartUtils.clearChart(chart);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        chart.startAnimation(chartOutAnimation);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("MainActivity", "onDestroy()");
        if (isFinishing()) {
            if (SignalService.isServiceRunning(this, SignalService.class)) {
                stopSamplingService();
            }
        }
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.test_detection) {
            Log.e("Fall detected", "Fall detected");
            Intent dialogIntent = new Intent(this, FallDetectedActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("fall_detected"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
