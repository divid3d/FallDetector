package com.example.divided.falldetector;

import android.hardware.SensorManager;
import android.util.Log;

import com.example.divided.falldetector.model.SensorDataPack;


public class Algorithm {

    private static final double TOTAL_ACC_THRESHOLD = 1.5;
    private static final double VERTICAL_ACC_THRESHOLD = 1.3;
    private static final double ACC_COMPARISION_THRESHOLD_LOW = 0.5;
    private static final double VERTICAL_ACC_THRESHOLD_LYING = 0.2;
    private static final double TOTAL_ACC_THRESHOLD_LYING = 0.3;
    private final static int LYING_SAMPLES_COUNT = 50;
    private final static int MAX_SAMPLES_AFTER_PEAK_DETECT = 100;

    private static boolean isFallPeakDetected = false;
    private static boolean isLyingDetected = false;
    private static int currentLyingSamples = 0;
    private static int samplesAfterPeakDetect = 0;
    private static double lastAccelerationModule = 0;
    private static double nextAccelerationModule = 0;


    public static boolean fallDetectionAlgorithm(SensorDataPack sensorDataPack) {

        float[] degrees = new float[3];
        float[] rotationMatrix = new float[9];
        double thetaY;
        double thetaZ;


        for (int i = 0; i < sensorDataPack.getPackSize(); i++) {
            SensorManager.getRotationMatrix(rotationMatrix, null, sensorDataPack.getRotationVectorData().get(i).getValues(), sensorDataPack.getMagneticFieldData().get(i).getValues());
            SensorManager.getOrientation(rotationMatrix, degrees);
            thetaY = degrees[2];
            thetaZ = degrees[0];

            final double verticalAcceleration = calculateAccelerationVertical(sensorDataPack.getLinearAccelerationData().get(i).getX(),
                    sensorDataPack.getLinearAccelerationData().get(i).getY(),
                    sensorDataPack.getLinearAccelerationData().get(i).getZ(),
                    thetaY, thetaZ);

            final double accelerationModule = sensorDataPack.getLinearAccelerationData().get(i).getModule();

            final double accelerationRatio = calculateAccelerationRatio(verticalAcceleration, accelerationModule);

            if (i - 1 > 0) {
                lastAccelerationModule = sensorDataPack.getLinearAccelerationData().get(i - 1).getModule();
            }
            if (i + 1 < sensorDataPack.getPackSize()) {
                nextAccelerationModule = sensorDataPack.getLinearAccelerationData().get(i + 1).getModule();
            }
            if (isFallPeakDetected) {
                samplesAfterPeakDetect++;
            }
            if (detectPeak(accelerationModule, verticalAcceleration, accelerationRatio)) {
                if (isLocalMaximum(lastAccelerationModule, nextAccelerationModule, accelerationModule)) {
                    currentLyingSamples = 0;
                    samplesAfterPeakDetect = 0;
                    isLyingDetected = false;
                    isFallPeakDetected = true;
                }
            }
            if (detectLying(accelerationModule, verticalAcceleration)) {
                currentLyingSamples++;
            } else {
                currentLyingSamples = 0;
            }
            if (isFallPeakDetected && samplesAfterPeakDetect >= MAX_SAMPLES_AFTER_PEAK_DETECT) {
                samplesAfterPeakDetect = 0;
                isFallPeakDetected = false;
            }
            if (currentLyingSamples >= LYING_SAMPLES_COUNT) {
                isLyingDetected = true;
            }
            if (isFallPeakDetected && isLyingDetected) {
                init();
                return true;
            }
        }
        return false;
    }

    public static void init() {

        isFallPeakDetected = false;
        isLyingDetected = false;
        currentLyingSamples = 0;
        samplesAfterPeakDetect = 0;
        lastAccelerationModule = 0;
        nextAccelerationModule = 0;

        Log.e("Algorithm", "Reset to init state");
    }

    private static double calculateAccelerationVertical(double aX, double aY, double aZ, double thetaY, double thetaZ) {
        return Math.abs(aX * Math.sin(thetaZ) + aY * Math.sin(thetaY) - aZ * Math.cos(thetaY) * Math.cos(thetaZ));
    }

    private static double calculateAccelerationRatio(double verticalAcceleration, double totalAcceleration) {
        return verticalAcceleration / totalAcceleration;
    }

    private static boolean detectPeak(double totalAcceleration, double verticalAcceleration, double accelerationRatio) {
        return verticalAcceleration >= VERTICAL_ACC_THRESHOLD && totalAcceleration >= TOTAL_ACC_THRESHOLD && accelerationRatio >= ACC_COMPARISION_THRESHOLD_LOW;
    }

    private static boolean detectLying(double totalAcceleration, double verticalAcceleration) {
        return totalAcceleration <= TOTAL_ACC_THRESHOLD_LYING && verticalAcceleration <= VERTICAL_ACC_THRESHOLD_LYING;
    }

    private static boolean isLocalMaximum(double lastAccelerationModule, double nextAccelerationModule, double currentAccelerationModule) {
        return lastAccelerationModule < currentAccelerationModule && nextAccelerationModule < currentAccelerationModule;

    }
}
