package com.example.divided.falldetector;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.example.divided.falldetector.model.GyroscopeData;
import com.example.divided.falldetector.model.LinearAccelerationData;
import com.example.divided.falldetector.model.MagneticFieldData;
import com.example.divided.falldetector.model.RotationVectorData;
import com.example.divided.falldetector.model.SensorData;
import com.example.divided.falldetector.model.SensorDataPack;
import com.example.divided.falldetector.model.TestSignal;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TestActivity extends AppCompatActivity {

    private Button mStartTest;
    private List<TestSignal> signals = new ArrayList<>();
    private List<TestAlgorithmTask> testAlgorithmTasks = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private SignalAdapter mSignalsAdapter;
    private TextView mTestCount;
    private ProgressBar mTestProgress;

    private static SensorDataPack loadSignal(File path) {
        List<SensorData> sensorData = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(path), ';', CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER)) {
            List<String[]> data = csvReader.readAll();
            for (int i = 0; i < data.size(); i++) {
                sensorData.add(new SensorData(new LinearAccelerationData(new float[]{Float.valueOf(data.get(i)[1].replace(',', '.')), Float.valueOf(data.get(i)[2].replace(',', '.')), Float.valueOf(data.get(i)[3].replace(',', '.'))}, Long.valueOf(data.get(i)[0])), SensorData.SensorType.SENSOR_LINEAR_ACCELERATION));
                sensorData.add(new SensorData(new GyroscopeData(new float[]{Float.valueOf(data.get(i)[6].replace(',', '.')), Float.valueOf(data.get(i)[7].replace(',', '.')), Float.valueOf(data.get(i)[8].replace(',', '.'))}, Long.valueOf(data.get(i)[5])), SensorData.SensorType.SENSOR_GYROSCOPE));
                sensorData.add(new SensorData(new MagneticFieldData(new float[]{Float.valueOf(data.get(i)[11].replace(',', '.')), Float.valueOf(data.get(i)[12].replace(',', '.')), Float.valueOf(data.get(i)[13].replace(',', '.'))}, Long.valueOf(data.get(i)[10])), SensorData.SensorType.SENSOR_MAGNETIC_FIELD));
                sensorData.add(new SensorData(new RotationVectorData(new float[]{Float.valueOf(data.get(i)[16].replace(',', '.')), Float.valueOf(data.get(i)[17].replace(',', '.')), Float.valueOf(data.get(i)[18].replace(',', '.'))}, Long.valueOf(data.get(i)[15])), SensorData.SensorType.SENSOR_ROTATION_VECTOR));
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
        mRecyclerView = findViewById(R.id.recycler_view_signals);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration itemDivider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDivider.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(this, R.drawable.recycler_view_divider)));
        mRecyclerView.addItemDecoration(itemDivider);
        mTestCount = findViewById(R.id.text_view_test_count);
        mTestProgress = findViewById(R.id.toolbar_progress_bar);

        signals = loadSignalsSignatures();

        Collections.sort(signals, (s1, s2) -> s1.getPath().getName().compareTo(s2.getPath().getName()));

        mSignalsAdapter = new SignalAdapter(signals);
        mRecyclerView.setAdapter(mSignalsAdapter);
        resetResults();

        MaterialRippleLayout.on(mStartTest)
                .rippleColor(Color.WHITE)
                .rippleAlpha(0.3f)
                .rippleHover(true)
                .rippleOverlay(true)
                .rippleRoundedCorners(2)
                .create();

        mStartTest.setOnClickListener(v -> {
            if (!isTestRunning(testAlgorithmTasks)) {
                startAlgorithmTest();
                mStartTest.setText("STOP TEST");
            } else {
                stopTest();
                mStartTest.setText("START TEST");
            }
        });
    }

    private void startAlgorithmTest() {
        resetResults();
        TestAlgorithmTask.initCounter();
        testAlgorithmTasks.clear();
        for (int i = 0; i < signals.size(); i++) {
            testAlgorithmTasks.add(new TestAlgorithmTask(this));
            testAlgorithmTasks.get(i).execute(signals.get(i).getPath());
        }
    }

    private List<TestSignal> loadSignalsSignatures() {
        List<TestSignal> signals = new ArrayList<>();
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "Acceleration signals");
        Log.e("directory exists", String.valueOf(directory.exists()));
        for (File f : directory.listFiles()) {
            if (f.isFile())
                signals.add(new TestSignal(f, "----"));
        }
        return signals;
    }

    private void resetResults() {
        for (int i = 0; i < mSignalsAdapter.getItemCount(); i++) {
            signals.get(i).setTestResult("----");
        }
        mSignalsAdapter.notifyDataSetChanged();
        mTestCount.setText("0/" + String.valueOf(mSignalsAdapter.getItemCount()));
        mTestProgress.setMax(signals.size());
        mTestProgress.setProgress(0);
    }

    private boolean isTestRunning(List<TestAlgorithmTask> tasks) {
        for (TestAlgorithmTask task : tasks) {
            if (task.getStatus() == AsyncTask.Status.RUNNING) {
                return true;
            }
        }
        return false;
    }

    private void stopTest() {
        for (TestAlgorithmTask task : testAlgorithmTasks) {
            task.cancel(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTest();
    }

    public void showTestSuccessDialog() {
        AlertDialog successDialog = new AlertDialog.Builder(TestActivity.this).create();
        successDialog.setTitle("Test completed successful");
        successDialog.setIcon(R.drawable.ic_baseline_done_all_24px);
        successDialog.setCancelable(false);
        successDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (dialog, which) -> successDialog.dismiss());
        successDialog.show();
    }

    private static class TestAlgorithmTask extends AsyncTask<File, String, Boolean> {
        private static int counter;
        private static WeakReference<TestActivity> mActivityRef;

        TestAlgorithmTask(TestActivity activity) {
            mActivityRef = new WeakReference<>(activity);
        }

        static void initCounter() {
            counter = 0;
        }

        protected Boolean doInBackground(File... paths) {
            final SensorDataPack data = loadSignal(paths[0]);
            Algorithm.init();
            boolean isFallDetected = Algorithm.fallDetectionAlgorithm(data);
            Log.e("Test", paths[0].getName() + "\t" + String.valueOf(isFallDetected));
            publishProgress(String.valueOf(isFallDetected));
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            if (mActivityRef.get() != null) {
                mActivityRef.get().signals.get(counter).setTestResult(progress[0]);
                mActivityRef.get().mRecyclerView.scrollToPosition(counter);
                mActivityRef.get().mSignalsAdapter.notifyItemChanged(counter);
                mActivityRef.get().runOnUiThread(() -> mActivityRef.get().mTestProgress.setProgress(counter + 1));
                mActivityRef.get().mTestCount.setText(String.valueOf(counter + 1) + "/" + String.valueOf(mActivityRef.get().mSignalsAdapter.getItemCount()));
                if (counter + 1 == mActivityRef.get().mSignalsAdapter.getItemCount()) {
                    mActivityRef.get().mStartTest.setText("START TEST");
                }
            }
        }

        protected void onPostExecute(Boolean result) {
            counter++;
            if (mActivityRef.get() != null && !mActivityRef.get().isFinishing()) {
                if (counter == mActivityRef.get().signals.size()) {
                    mActivityRef.get().showTestSuccessDialog();
                }
            }
        }
    }
}