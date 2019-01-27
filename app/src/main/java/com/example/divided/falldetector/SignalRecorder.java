package com.example.divided.falldetector;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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


public class SignalRecorder extends AppCompatActivity implements DialogInterface.OnClickListener, SensorManager.OnSensorDataListener {
    Menu toolbarMenu;
    AlertDialog saveFileDialog;
    TextView samplesCount;
    TextView textViewTimeFromStart;
    EditText saveFileDialogEditText;
    Button mButtonSave;
    LineChart accelerationChart;
    LineChart gyroscopeChart;
    LineChart magneticFieldChart;
    LineChart rotationVectorChart;
    SoundHelper mSoundHelper;
    List<SensorData> buffer = new ArrayList<>();
    private SensorManager sensorManager;
    private boolean isRunning = true;
    private StopWatch stopWatch;
    private EditText edittextfilename;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_recorder);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        samplesCount = findViewById(R.id.tv_samples_count);

        stopWatch = new StopWatch();
        mSoundHelper = new SoundHelper(this, R.raw.start_button_sound, false);


        double SAMPLING_PERIOD = 20;
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
        this.edittextfilename = saveDialogView.findViewById(R.id.edit_text_filename);
        saveFileDialog = new AlertDialog.Builder(this).create();
        saveFileDialog.setTitle("Enter filename");
        saveFileDialog.setCancelable(true);
        saveFileDialog.setIcon(R.drawable.ic_baseline_save_blue_24px);
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
                if (isRunning) {
                    if (toolbarMenu != null) {
                        toolbarMenu.performIdentifierAction(R.id.run_pause, 0);
                    }
                }
                Toast.makeText(getApplicationContext(), "Charts have been cleared", Toast.LENGTH_SHORT).show();
                ChartUtils.clearChart(accelerationChart);
                ChartUtils.clearChart(gyroscopeChart);
                ChartUtils.clearChart(magneticFieldChart);
                ChartUtils.clearChart(rotationVectorChart);
                buffer.clear();
                samplesCount.setText("Samples: 0");
                textViewTimeFromStart.setText(Utils.getTime(0));
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
        textViewTimeFromStart.setText(Utils.getTime(stopWatch.getTime()));

        switch (sensorData.getSensorType()) {
            case SENSOR_LINEAR_ACCELERATION:
                new ChartDrawTask(accelerationChart).execute(((LinearAccelerationData) sensorData.getData()).getModule(), (float) ((LinearAccelerationData) sensorData.getData()).getTimestamp());
                break;

            case SENSOR_GYROSCOPE:
                new ChartDrawTask(gyroscopeChart).execute(((GyroscopeData) sensorData.getData()).getModule(), (float) ((GyroscopeData) sensorData.getData()).getTimestamp());
                break;

            case SENSOR_MAGNETIC_FIELD:
                new ChartDrawTask(magneticFieldChart).execute(((MagneticFieldData) sensorData.getData()).getModule(), (float) ((MagneticFieldData) sensorData.getData()).getTimestamp());
                break;

            case SENSOR_ROTATION_VECTOR:
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
            samplesCount.setText("Samples: " + String.valueOf(Utils.findMinValue(
                    accelerationChart.getData().getEntryCount(),
                    gyroscopeChart.getData().getEntryCount(),
                    magneticFieldChart.getData().getEntryCount(),
                    rotationVectorChart.getData().getEntryCount())));
        }
    }
}
