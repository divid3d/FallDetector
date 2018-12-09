package com.example.divided.falldetector;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
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
    private final BroadcastReceiver mFallDetected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("broadcast", "StopSamplingService()");
            stopSamplingService();
        }
    };
    List<Entry> entries = new ArrayList<>();
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final float modVal = (float) intent.getDoubleExtra("key", 0);
            final float timestamp = (float) intent.getDoubleExtra("timestamp", 0);
            ChartPoint chartPoint = new ChartPoint(modVal, timestamp);
            ChartUtils.addEntry(chartPoint, chart, "", Color.WHITE);
        }
    };

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MainActivity","onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        mStartStopServiceButton = findViewById(R.id.btn_start_service);
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
            if (SignalService.isServiceRunning(this, SignalService.class)) {
                stopSamplingService();

            } else {
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

    private void startSamplingService() {
        startService(new Intent(this, SignalService.class));
        mStartStopServiceButton.setText(R.string.tap_to_stop);
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
        mStartStopServiceButton.setText(R.string.tap_to_start);
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
        Log.i("MainActivity","onDestroy()");
        if (SignalService.isServiceRunning(this, SignalService.class)) {
            stopSamplingService();
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
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(dialogIntent);
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent("fall_detected"));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
