package com.example.divided.falldetector;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.example.divided.falldetector.model.GyroscopeData;
import com.example.divided.falldetector.model.LinearAccelerationData;
import com.example.divided.falldetector.model.MagneticFieldData;
import com.example.divided.falldetector.model.RotationVectorData;
import com.example.divided.falldetector.model.SensorData;
import com.example.divided.falldetector.model.SensorDataPack;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    static List<Float> vAccel = new ArrayList<>();
    Button mStartTest;
    List<File> paths = new ArrayList<>();
    RecyclerView mRecyclerView;
    SignalAdapter mSignalsAdapter;
    TextView mLog;
    private boolean isTestRunning = false;

    private static SensorDataPack loadSignal(File path) {
        List<SensorData> sensorData = new ArrayList<>();
        vAccel.clear();
        try (CSVReader csvReader = new CSVReader(new FileReader(path), ';', CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER)) {
            List<String[]> data = csvReader.readAll();
            for (int i = 0; i < data.size(); i++) {
                sensorData.add(new SensorData(new LinearAccelerationData(new float[]{Float.valueOf(data.get(i)[1].replace(',', '.')), Float.valueOf(data.get(i)[2].replace(',', '.')), Float.valueOf(data.get(i)[3].replace(',', '.'))}, Long.valueOf(data.get(i)[0])), SensorData.SensorType.SENSOR_LINEAR_ACCELERATION));
                sensorData.add(new SensorData(new GyroscopeData(new float[]{Float.valueOf(data.get(i)[6].replace(',', '.')), Float.valueOf(data.get(i)[7].replace(',', '.')), Float.valueOf(data.get(i)[8].replace(',', '.'))}, Long.valueOf(data.get(i)[5])), SensorData.SensorType.SENSOR_GYROSCOPE));
                sensorData.add(new SensorData(new MagneticFieldData(new float[]{Float.valueOf(data.get(i)[11].replace(',', '.')), Float.valueOf(data.get(i)[12].replace(',', '.')), Float.valueOf(data.get(i)[13].replace(',', '.'))}, Long.valueOf(data.get(i)[10])), SensorData.SensorType.SENSOR_MAGNETIC_FIELD));
                sensorData.add(new SensorData(new RotationVectorData(new float[]{Float.valueOf(data.get(i)[16].replace(',', '.')), Float.valueOf(data.get(i)[17].replace(',', '.')), Float.valueOf(data.get(i)[18].replace(',', '.'))}, Long.valueOf(data.get(i)[15])), SensorData.SensorType.SENSOR_ROTATION_VECTOR));
                vAccel.add(Float.valueOf(data.get(i)[19].replace(',', '.')));
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
        return new SensorDataPack(sensorData);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Toolbar toolbar = findViewById(R.id.test_activity_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        mStartTest = findViewById(R.id.button_test_start);
        mLog = findViewById(R.id.text_view_log);
        mLog.setMovementMethod(new ScrollingMovementMethod());
        mLog.setVerticalScrollBarEnabled(true);
        mRecyclerView = findViewById(R.id.recycler_view_signals);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        paths = loadSignalsSignatures();
        Collections.sort(paths, (f1, f2) -> f1.getName().compareTo(f2.getName()));

        mSignalsAdapter = new SignalAdapter(paths);
        mRecyclerView.setAdapter(mSignalsAdapter);
        mSignalsAdapter.notifyDataSetChanged();

        MaterialRippleLayout.on(mStartTest)
                .rippleColor(Color.WHITE)
                .rippleAlpha(0.3f)
                .rippleHover(true)
                .rippleOverlay(true)
                .rippleRoundedCorners(2)
                .create();


        mStartTest.setOnClickListener(v -> {
            if (!isTestRunning) {
                startAlgorithmTest(paths);
            }
        });
    }

    private void startAlgorithmTest(List<File> paths) {
        isTestRunning = true;
        for (File path : paths) {
            new TestAlgorithmTask().execute(path);
        }
        isTestRunning = false;
    }

    private List<File> loadSignalsSignatures() {
        List<File> paths = new ArrayList<>();
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "Acceleration signals");
        Log.e("directory exists", String.valueOf(directory.exists()));
        for (File f : directory.listFiles()) {
            if (f.isFile())
                paths.add(f);
        }
        return paths;
    }

    private class TestAlgorithmTask extends AsyncTask<File, String, Boolean> {

        protected Boolean doInBackground(File... paths) {
            final SensorDataPack data = loadSignal(paths[0]);
            Algorithm.init();
            boolean isFallDetected = Algorithm.fallDetectionAlgorithm(data, vAccel);
            Log.e("Test", paths[0].getName() + "\t" + String.valueOf(isFallDetected));
            publishProgress(paths[0].getName() + "\t->\t" + String.valueOf(isFallDetected) + "\n");
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            mLog.append(progress[0]);
        }

        protected void onPostExecute(Boolean result) {
        }
    }
}