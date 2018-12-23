package com.example.divided.falldetector;

import android.util.Log;

import com.example.divided.falldetector.model.SensorDataPack;

import java.util.List;


public class Algorithm {

    private static final double TOTAL_ACC_THRESHOLD = 1.5;
    private static final double VERTICAL_ACC_THRESHOLD = 1.2;
    private static final double ACC_COMPARISION_THRESHOLD_LOW = 0.5;
    private static final double ACC_COMPARISION_THRESHOLD_HIGH = 0.85;


    private static final double VERTICAL_ACC_THRESHOLD_LYING = 0.2;
    private static final double TOTAL_ACC_THRESHOLD_LYING = 0.3;
    private final static int LYING_SAMPLES_COUNT = 50;
    private final static int MAX_SAMPLES_AFTER_PEAK_DETECT = 100;
    private static boolean isFallPeakDetected = false;
    private static boolean isLyingDetected = false;
    private static int currentLyingSamples = 0;
    private static int samplesAfterPeakDetect = 0;
    private static long samplesProcessed = 0;
    private static double lastVerticalAccel = 0;
    private static double nextVerticalAccel = 0;


    synchronized public static boolean fallDetectionAlgorithm(SensorDataPack sensorDataPack, List<Float> vAccel) {


       /* Log.e("BUFFER SIZE", "Acc:\t" + linearAccelerationData.size() + "\tGyro:\t" + gyroscopeData.size() + "\tMagn:\t" + magneticFieldData.size() + "\tRot:\t" + rotationVectorData.size());
        Log.e("TIMESTAMPS", String.format("%.5f", (float)Iterables.getLast(linearAccelerationData).getTimestamp()) + "\t"
                + String.format("%.5f",(float) Iterables.getLast(gyroscopeData).getTimestamp()) + "\t"
                + String.format("%.5f",(float) Iterables.getLast(magneticFieldData).getTimestamp()) + "\t"
                + String.format("%.5f",(float) Iterables.getLast(rotationVectorData).getTimestamp()));*/
        float[] degrees = new float[3];
        float[] rotationMatrix = new float[9];
        double thetaY;
        double thetaZ;

        /*long startTime = System.nanoTime();*/

        for (int i = 0; i < sensorDataPack.getPackSize(); i++) {
            /*SensorManager.getRotationMatrix(rotationMatrix, null, sensorDataPack.getRotationVectorData().get(i).getValues(), sensorDataPack.getMagneticFieldData().get(i).getValues());
            SensorManager.getOrientation(rotationMatrix, degrees);
            thetaY = degrees[2];
            thetaZ = degrees[0];*/

            /*Log.e("Theta Y", String.valueOf(thetaY*(180/Math.PI))); // debug angle in degrees
            Log.e("Theta Z", String.valueOf(thetaZ*(180/Math.PI)));*/


            //final double verticalAcceleration = calculateAccelerationNormal(sensorDataPack.getLinearAccelerationData().get(i).getX(), sensorDataPack.getLinearAccelerationData().get(i).getY(), sensorDataPack.getLinearAccelerationData().get(i).getZ(), thetaY, thetaZ);
            final double verticalAcceleration = vAccel.get(i);
            final double totalAcceleration = sensorDataPack.getLinearAccelerationData().get(i).getModule();

            final double accelerationRatio = calculateAccelerationRatio(verticalAcceleration, totalAcceleration);
            final float accelerationPercentage = (float) accelerationRatio * 100f;

            /*Log.e("ALOGRITHM PROCESS", "Iteration:\t" + String.valueOf(i) + "\t" + String.format("aV:\t%.5f\t", verticalAcceleration)
                    + String.format("aT:\t%.5f\t", totalAcceleration)
                    + String.format("Ratio:\t%.3f\t", accelerationRatio)
                    + String.format("Percentage:\t%.2f", accelerationPercentage));*/

            Log.e("ALOGRITHM PROCESS", "Iteration:\t" + String.valueOf(i) + "\t" + String.format("aV:\t%.5f\t", verticalAcceleration)
                    + String.format("aT:\t%.5f\t", totalAcceleration));


            /*Log.e("Differances","Acc - Gyro"+String.format("%f ms",(sensorDataPack.getLinearAccelerationData().get(i).getTimestamp() - sensorDataPack.getGyroscopeData().get(i).getTimestamp())/1000000.0f)+"\n"
            +"Acc - Magn"+String.format("%f ms",(sensorDataPack.getLinearAccelerationData().get(i).getTimestamp() - sensorDataPack.getMagneticFieldData().get(i).getTimestamp())/1000000.0f)+"\n"
            +"Acc - Rot"+String.format("%f ms",(sensorDataPack.getLinearAccelerationData().get(i).getTimestamp() - sensorDataPack.getRotationVectorData().get(i).getTimestamp())/1000000.0f)+"\n");
*/

            if (i - 1 > 0) {
                lastVerticalAccel = vAccel.get(i - 1);
            }
            if (i + 1 < sensorDataPack.getPackSize()) {
                nextVerticalAccel = vAccel.get(i + 1);
            }

            if (i + 1 < sensorDataPack.getPackSize()) {
                nextVerticalAccel = vAccel.get(i + 1);
            }

            if (isFallPeakDetected) {
                samplesAfterPeakDetect++;
            }

            if (detectPeak(totalAcceleration, verticalAcceleration, accelerationRatio)) {
                if (isLocalMaximum(lastVerticalAccel, nextVerticalAccel, verticalAcceleration)) {
                    currentLyingSamples = 0;
                    samplesAfterPeakDetect = 0;
                    isLyingDetected = false;
                    isFallPeakDetected = true;
                }
            }

            if (detectLying(totalAcceleration, verticalAcceleration)) {
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
            samplesProcessed++;
            debug(i + 1, isFallPeakDetected, isLyingDetected, samplesAfterPeakDetect, currentLyingSamples);
            if (isFallPeakDetected && isLyingDetected) {
                init();
                return true;
            }


/*
            if (detectPeak(totalAcceleration, verticalAcceleration, accelerationRatio)) {
                currentLyingSamples = 0;
                samplesAfterPeakDetect = 0;
                isLyingDetected = false;
                isFallPeakDetected = true;
            }
            if (detectLying(totalAcceleration, verticalAcceleration)) {
                currentLyingSamples++;
                if (currentLyingSamples > LYING_SAMPLES_COUNT) {
                    isLyingDetected = true;
                    currentLyingSamples = 0;
                }
            } else {
                currentLyingSamples = 0;
                isLyingDetected = false;
            }

            if (isFallPeakDetected) {
                samplesAfterPeakDetect++;
            }
            if (samplesAfterPeakDetect > MAX_SAMPLES_AFTER_PEAK_DETECT) {
                isFallPeakDetected = false;
                samplesAfterPeakDetect = 0;
            }

            debug(i + 1, isFallPeakDetected, isLyingDetected, samplesAfterPeakDetect, currentLyingSamples);
            if (isFallPeakDetected && isLyingDetected) {
                return true;
            }*/

            /* zeby odróżnic czynosci ynamicznych tj skakanie a upadek można posiłkować się
            odczytami z zyroskopu, wartością modulo z żyroskopu, przy upadku zawsze jest >= 300 (st/sec).
             */
        }
       /* float execTime = (System.nanoTime() - startTime)/1000000; // in ms
        Log.e("time",String.valueOf(execTime));*/
        return false;
    }

    public static void init() {

        isFallPeakDetected = false;
        isLyingDetected = false;
        currentLyingSamples = 0;
        samplesAfterPeakDetect = 0;
        samplesProcessed = 0;
        lastVerticalAccel = 0;
        nextVerticalAccel = 0;

        Log.e("Algorithm", "Reset to init state");
    }

    public static double calculateAccelerationNormal(double aX, double aY, double aZ, double thetaY, double thetaZ) {
        return Math.abs(aX * Math.sin(thetaZ) + aY * Math.sin(thetaY) - aZ * Math.cos(thetaY) * Math.cos(thetaZ));
    }

    private static double calculateAccelerationRatio(double verticalAcceleration, double totalAcceleration) {
        return verticalAcceleration / totalAcceleration;
    }

    private static boolean detectPeak(double totalAcceleration, double verticalAcceleration, double accelerationRatio) {
        return verticalAcceleration >= VERTICAL_ACC_THRESHOLD && totalAcceleration>=TOTAL_ACC_THRESHOLD && accelerationRatio >= ACC_COMPARISION_THRESHOLD_LOW;
    }

    private static boolean detectLying(double totalAcceleration, double verticalAcceleration) {
        return totalAcceleration <= TOTAL_ACC_THRESHOLD_LYING && verticalAcceleration <= VERTICAL_ACC_THRESHOLD_LYING;
    }

    private static void debug(int sample, boolean isFallPeakDetected, boolean isLyingDetected, int samplesAfterPeakDetect, int currentLyingSamples) {
        Log.e("Algorithm debug", "Sample:\t" + sample + "\t" + "isFallPeakDetected:\t" + String.valueOf(isFallPeakDetected)
                + "\tisLyingDetected:\t" + String.valueOf(isLyingDetected)
                + "\tsamplesAfterPeakDetect:\t" + String.valueOf(samplesAfterPeakDetect)
                + "\tcurrentlyLyingSamples:\t" + String.valueOf(currentLyingSamples));
    }

    private static boolean isLocalMaximum(double lastVerticalAccel, double nextVerticalAccel, double currVerticalAccel) {
        return lastVerticalAccel < currVerticalAccel && nextVerticalAccel < currVerticalAccel;

    }
}
