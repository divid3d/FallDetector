package com.example.divided.falldetector;

import android.hardware.SensorManager;
import android.util.Log;

import com.example.divided.falldetector.model.SensorDataPack;

public class Algorithm {

    private static final double TOTAL_ACC_THRESHOLD = 1.5;
    private static final double VERTICAL_ACC_THRESHOLD = 1.2;
    private static final double ACC_COMPARISION_THRESHOLD_LOW = 0.6;
    private static final double ACC_COMPARISION_THRESHOLD_HIGH = 0.85;


    private static final double VERTICAL_ACC_THRESHOLD_LYING = 0.2;
    private static final double TOTAL_ACC_THRESHOLD_LYING = 0.3;
    private final static int LYING_SAMPLES_COUNT = 25;
    private static boolean isFallPeakDetected = false;
    private static boolean isLyingDetected = false;
    private static int currentLyingSamples = 0;

    public static boolean fallDetectionAlgorithm(SensorDataPack sensorDataPack) {


       /* Log.e("BUFFER SIZE", "Acc:\t" + linearAccelerationData.size() + "\tGyro:\t" + gyroscopeData.size() + "\tMagn:\t" + magneticFieldData.size() + "\tRot:\t" + rotationVectorData.size());
        Log.e("TIMESTAMPS", String.format("%.5f", (float)Iterables.getLast(linearAccelerationData).getTimestamp()) + "\t"
                + String.format("%.5f",(float) Iterables.getLast(gyroscopeData).getTimestamp()) + "\t"
                + String.format("%.5f",(float) Iterables.getLast(magneticFieldData).getTimestamp()) + "\t"
                + String.format("%.5f",(float) Iterables.getLast(rotationVectorData).getTimestamp()));*/
        float[] degrees = new float[3];
        float[] rotationMatrix = new float[9];
        double thetaY;
        double thetaZ;


        for (int i = 0; i < sensorDataPack.getPackSize(); i++) {
            SensorManager.getRotationMatrix(rotationMatrix, null, sensorDataPack.getRotationVectorData().get(i).getValues(), sensorDataPack.getMagneticFieldData().get(i).getValues());
            SensorManager.getOrientation(rotationMatrix, degrees);
            thetaY = degrees[2];
            thetaZ = degrees[0];


            final double verticalAcceleration = calculateAccelerationNormal(sensorDataPack.getLinearAccelerationData().get(i).getX(), sensorDataPack.getLinearAccelerationData().get(i).getY(), sensorDataPack.getLinearAccelerationData().get(i).getZ(), thetaY, thetaZ);
            final double totalAcceleration = sensorDataPack.getLinearAccelerationData().get(i).getModule();

            final double accelerationRatio = calculateAccelerationRatio(verticalAcceleration, totalAcceleration);
            final float accelerationPercentage = (float) accelerationRatio * 100f;

            /*Log.e("ALOGRITHM PROCESS", "Iteration:\t" + String.valueOf(i) + "\t" + String.format("aV:\t%.5f\t", verticalAcceleration)
                    + String.format("aT:\t%.5f\t", totalAcceleration)
                    + String.format("Ratio:\t%.3f\t", accelerationRatio)
                    + String.format("Percentage:\t%.2f", accelerationPercentage));*/

            /*Log.e("Differances","Acc - Gyro"+String.format("%f ms",(sensorDataPack.getLinearAccelerationData().get(i).getTimestamp() - sensorDataPack.getGyroscopeData().get(i).getTimestamp())/1000000.0f)+"\n"
            +"Acc - Magn"+String.format("%f ms",(sensorDataPack.getLinearAccelerationData().get(i).getTimestamp() - sensorDataPack.getMagneticFieldData().get(i).getTimestamp())/1000000.0f)+"\n"
            +"Acc - Rot"+String.format("%f ms",(sensorDataPack.getLinearAccelerationData().get(i).getTimestamp() - sensorDataPack.getRotationVectorData().get(i).getTimestamp())/1000000.0f)+"\n");
*/


            if (detectPeak(totalAcceleration, verticalAcceleration, accelerationRatio)) {
                currentLyingSamples = 0;
                isLyingDetected = false;
                isFallPeakDetected = true;
            }
            if (detectLying(totalAcceleration, verticalAcceleration)) {
                currentLyingSamples++;
                if (currentLyingSamples >= LYING_SAMPLES_COUNT) {
                    isLyingDetected = true;
                    currentLyingSamples = 0;
                }
            } else {
                currentLyingSamples = 0;
            }

            Log.e("Algorithm", "isFallDetected:\t" + String.valueOf(isFallPeakDetected));
            Log.e("Algorithm", "isLyingDetected:\t" + String.valueOf(isLyingDetected));
            Log.e("Algorithm", "currentLyingSamples:\t" + String.valueOf(currentLyingSamples));

            if (isFallPeakDetected && isLyingDetected) {
                isLyingDetected = false;
                isFallPeakDetected = false;
                currentLyingSamples = 0;
                return true;
            }

            /* zeby odróżnic czynosci ynamicznych tj skakanie a upadek można posiłkować się
            odczytami z zyroskopu, wartością modulo z żyroskopu, przy upadku zawsze jest >= 300 (st/sec).
             */
        }
        return false;
    }

    private static double calculateAccelerationNormal(double aX, double aY, double aZ, double thetaY, double thetaZ) {
        return Math.abs(aX * Math.sin(thetaZ) + aY * Math.sin(thetaY) - aZ * Math.cos(thetaY) * Math.cos(thetaZ));
    }

    private static double calculateAccelerationRatio(double verticalAcceleration, double totalAcceleration) {
        return verticalAcceleration / totalAcceleration;
    }

    private static boolean detectPeak(double totalAcceleration, double verticalAcceleration, double accelerationRatio) {
        if (totalAcceleration >= TOTAL_ACC_THRESHOLD && verticalAcceleration >= VERTICAL_ACC_THRESHOLD) {
            if (accelerationRatio >= ACC_COMPARISION_THRESHOLD_LOW && accelerationRatio <= ACC_COMPARISION_THRESHOLD_HIGH) {
                return true;
            }
        }
        return false;
    }

    private static boolean detectLying(double totalAcceleration, double verticalAcceleration) {
        if (totalAcceleration <= TOTAL_ACC_THRESHOLD_LYING && verticalAcceleration <= VERTICAL_ACC_THRESHOLD_LYING) {
            return true;
        }
        return false;
    }


}
