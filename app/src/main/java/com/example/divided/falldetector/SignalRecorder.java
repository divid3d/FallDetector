package com.example.divided.falldetector;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.divided.falldetector.model.GyroscopeData;
import com.example.divided.falldetector.model.LinearAccelerationData;
import com.example.divided.falldetector.model.MagneticFieldData;
import com.example.divided.falldetector.model.RotationVectorData;
import com.example.divided.falldetector.model.SensorData;
import com.example.divided.falldetector.model.SensorDataPack;
import com.example.divided.falldetector.model.SensorManager;
import com.github.mikephil.charting.charts.LineChart;

import org.apache.commons.lang3.time.StopWatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;


public class SignalRecorder extends AppCompatActivity implements DialogInterface.OnClickListener, SensorManager.OnSensorDataListener {
    private final double SAMPLING_PERIOD = 20;
    Menu toolbarMenu;
    AlertDialog saveFileDialog;
    TextView mAccelerationSamplesCount;
    TextView mGyroscopeSamplesCount;
    TextView mMagneticSamplesCount;
    TextView mRotationSamplesCount;
    TextView textViewTimeFromStart;
    EditText saveFileDialogEditText;
    Button mButtonSave;
    LineChart accelerationChart;
    LineChart gyroscopeChart;
    LineChart magneticFieldChart;
    LineChart rotationVectorChart;
    SoundHelper mSoundHelper;
    BluetoothSPP bt;
    List<SensorData> buffer = new ArrayList<>();
    private SensorManager sensorManager;
    private boolean isRunning = true;
    private StopWatch stopWatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_recorder);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAccelerationSamplesCount = findViewById(R.id.accel_samples_count);
        mGyroscopeSamplesCount = findViewById(R.id.gyro_samples_count);
        mMagneticSamplesCount = findViewById(R.id.magnetic_samples_count);
        mRotationSamplesCount = findViewById(R.id.rotation_sampels_count);

        bt = new BluetoothSPP(this);
        stopWatch = new StopWatch();
        mSoundHelper = new SoundHelper(this, R.raw.start_button_sound, false);


        sensorManager = new SensorManager(this, SAMPLING_PERIOD);
        sensorManager.setOnSensorDataListener(this);

        mButtonSave = findViewById(R.id.button_save);
        mButtonSave.setOnClickListener(v -> {
            if (isRunning) {
                mSoundHelper.startSound();
                if (sensorManager != null) {
                    sensorManager.unregisterListeners();
                    toolbarMenu.findItem(R.id.run_pause).setIcon(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24px));
                }
                isRunning = !isRunning;
            }
            saveFileDialog.show();
        });


        textViewTimeFromStart = findViewById(R.id.tv_time);
        accelerationChart = findViewById(R.id.acceleration_chart);
        gyroscopeChart = findViewById(R.id.gyroscope_chart);
        magneticFieldChart = findViewById(R.id.magnetic_field_chart);
        rotationVectorChart = findViewById(R.id.rotation_vector_chart);

        ChartUtils.setupChart(this, accelerationChart, true, false);
        ChartUtils.setupData(accelerationChart);
        ChartUtils.setupChart(this, gyroscopeChart, true, false);
        ChartUtils.setupData(gyroscopeChart);
        ChartUtils.setupChart(this, magneticFieldChart, true, false);
        ChartUtils.setupData(magneticFieldChart);
        ChartUtils.setupChart(this, rotationVectorChart, true, false);
        ChartUtils.setupData(rotationVectorChart);

        @SuppressLint("InflateParams") View saveDialogView = getLayoutInflater().inflate(R.layout.save_dialog_layout, null);
        saveFileDialog = new AlertDialog.Builder(this).create();
        saveFileDialog.setTitle("Enter filename");
        saveFileDialog.setCancelable(true);
        saveFileDialogEditText = saveDialogView.findViewById(R.id.edit_text_filename);
        saveFileDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save", this);
        saveFileDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", this);
        saveFileDialog.setView(saveDialogView);

        sensorManager.registerListeners();
        stopWatch.start();
    }


    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListeners();
        mSoundHelper.release();

        if (bt != null) {
            bt.stopService();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.toolbarMenu = menu;
        getMenuInflater().inflate(R.menu.signal_recorder_menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_charts:
                if(isRunning){
                    if(toolbarMenu!=null) {
                        toolbarMenu.performIdentifierAction(R.id.run_pause, 0);
                    }
                }
                Toast.makeText(getApplicationContext(), "Charts have been cleared", Toast.LENGTH_SHORT).show();
                ChartUtils.clearChart(accelerationChart);
                ChartUtils.clearChart(gyroscopeChart);
                ChartUtils.clearChart(magneticFieldChart);
                ChartUtils.clearChart(rotationVectorChart);
                buffer.clear();

                mAccelerationSamplesCount.setText("-");
                mGyroscopeSamplesCount.setText("-");
                mMagneticSamplesCount.setText("-");
                mRotationSamplesCount.setText("-");

                textViewTimeFromStart.setText(Utils.getTime(0));
                if (bt != null) {
                    if (bt.isServiceAvailable()) {
                        bt.send("params_" + 0 + "_" + 0 + "_" + Utils.getTime(0), true);
                    }
                }
                if (stopWatch.isStarted()) {
                    stopWatch.reset();
                    stopWatch.start();
                }
                return true;

            case R.id.run_pause:
                isRunning = !isRunning;
                mSoundHelper.startSound();
                if (isRunning) {
                    if (sensorManager != null) {
                        sensorManager.registerListeners();
                        item.setIcon(getResources().getDrawable(R.drawable.ic_baseline_pause_24px));
                        if (stopWatch.isSuspended()) {
                            stopWatch.resume();
                        } else if (stopWatch.isStopped()) {
                            stopWatch.start();
                        }
                    }
                } else {
                    if (sensorManager != null) {
                        sensorManager.unregisterListeners();
                        item.setIcon(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24px));
                        if (stopWatch.isStarted()) {
                            stopWatch.suspend();
                        }
                    }
                }
                return true;

            case R.id.save:
                if (isRunning) {
                    mSoundHelper.startSound();
                    if (sensorManager != null) {
                        sensorManager.unregisterListeners();
                        toolbarMenu.findItem(R.id.run_pause).setIcon(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24px));
                    }
                    isRunning = !isRunning;
                }
                saveFileDialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onStart() {
        super.onStart();
        if (bt != null) {
            if (!bt.isBluetoothEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
            } else {
                if (!bt.isServiceAvailable()) {
                    bt.setupService();
                    bt.startService(BluetoothState.DEVICE_ANDROID);
                    bt.setOnDataReceivedListener((data, message) -> {
                        if (message.equals("action_run")) {
                            Log.e("Bt receive", message);
                            isRunning = !isRunning;
                            mSoundHelper.startSound();
                            if (isRunning) {
                                if (sensorManager != null) {
                                    sensorManager.registerListeners();
                                    if (stopWatch.isSuspended()) {
                                        stopWatch.resume();
                                    } else if (stopWatch.isStopped()) {
                                        stopWatch.start();
                                    }
                                }
                            } else {
                                if (sensorManager != null) {
                                    sensorManager.unregisterListeners();
                                    if (stopWatch.isStarted()) {
                                        stopWatch.suspend();
                                    }
                                }
                            }
                        } else if (message.equals("action_clear")) {
                            Log.e("Bt receive", message);
                            SignalRecorder.this.onOptionsItemSelected(toolbarMenu.findItem(R.id.clear_charts));
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case AlertDialog.BUTTON_POSITIVE:
                String filename = saveFileDialogEditText.getText().toString().trim();
                final String date = new SimpleDateFormat("dd_MM_yyy_HH_mm_ss").format(new Date());
                if (filename.isEmpty()) {
                    filename = date;
                }
                SensorDataPack sensorDataPack = new SensorDataPack(buffer);
                Utils.saveAsCsv(this, sensorDataPack, filename);
                break;

            case AlertDialog.BUTTON_NEGATIVE:
                dialog.dismiss();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_POWER || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            this.onOptionsItemSelected(toolbarMenu.findItem(R.id.run_pause));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onNewSensorData(SensorData sensorData) {
        buffer.add(sensorData);
        /*textViewAccSamplesCount.setText(String.valueOf(buffer.size() / 4));*/
        textViewTimeFromStart.setText(Utils.getTime(stopWatch.getTime()));

        if (bt != null) {
            if (bt.isServiceAvailable()) {
                bt.send("params_" + String.valueOf(buffer.size() / 4) + "_" + String.valueOf(buffer.size() / 4) + "_" + Utils.getTime(stopWatch.getTime()), true);
            }
        }

        switch (sensorData.getSensorType()) {
            case SENSOR_LINEAR_ACCELERATION:
                mAccelerationSamplesCount.setText(String.valueOf(accelerationChart.getData().getEntryCount() + 1));
                new ChartDrawTask(accelerationChart).execute(((LinearAccelerationData) sensorData.getData()).getModule(), (float) ((LinearAccelerationData) sensorData.getData()).getTimestamp());
                break;

            case SENSOR_GYROSCOPE:
                mGyroscopeSamplesCount.setText(String.valueOf(gyroscopeChart.getData().getEntryCount() + 1));
                new ChartDrawTask(gyroscopeChart).execute(((GyroscopeData) sensorData.getData()).getModule(), (float) ((GyroscopeData) sensorData.getData()).getTimestamp());
                break;

            case SENSOR_MAGNETIC_FIELD:
                mMagneticSamplesCount.setText(String.valueOf(magneticFieldChart.getData().getEntryCount() + 1));
                new ChartDrawTask(magneticFieldChart).execute(((MagneticFieldData) sensorData.getData()).getModule(), (float) ((MagneticFieldData) sensorData.getData()).getTimestamp());
                break;

            case SENSOR_ROTATION_VECTOR:
                mRotationSamplesCount.setText(String.valueOf(rotationVectorChart.getData().getEntryCount() + 1));
                new ChartDrawTask(rotationVectorChart).execute(((RotationVectorData) sensorData.getData()).getX(), (float) ((RotationVectorData) sensorData.getData()).getTimestamp());
                break;
        }
    }


    private class ChartDrawTask extends AsyncTask<Float, Void, Void> {

        private ChartPoint chartPoint;
        @SuppressLint("StaticFieldLeak")
        private LineChart chart;

        ChartDrawTask(LineChart chart) {
            this.chart = chart;
        }

        @Override
        protected Void doInBackground(Float... params) {
            chartPoint = new ChartPoint(params[0], params[1]);
            ChartUtils.addEntry(chartPoint, chart, Color.WHITE, false, false);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}
