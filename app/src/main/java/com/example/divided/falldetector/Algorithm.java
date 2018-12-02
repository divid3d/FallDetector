package com.example.divided.falldetector;

import android.hardware.SensorManager;
import android.util.Log;

import com.example.divided.falldetector.model.GyroscopeData;
import com.example.divided.falldetector.model.LinearAccelerationData;
import com.example.divided.falldetector.model.MagneticFieldData;
import com.example.divided.falldetector.model.RotationVectorData;
import com.google.common.collect.Iterables;

import java.util.List;

public class Algorithm {

    private static final double TOTAL_ACC_THRESHOLD = 1.5;
    private static final double VERTICAL_ACC_THRESHOLD = 1.2;
    private static final double ACC_COMPARISION_THRESHOLD_LOW = 0.6;
    private static final double ACC_COMPARISION_THRESHOLD_HIGH = 0.85;

    public static boolean fallDetectionAlgorithm(
            List<LinearAccelerationData> linearAccelerationData
            , List<GyroscopeData> gyroscopeData
            , List<MagneticFieldData> magneticFieldData
            , List<RotationVectorData> rotationVectorData, int BUFFER_SIZE) {
        final int linearAccelerationDataIndex = linearAccelerationData.size() - BUFFER_SIZE;
        final int gyroscopeDataIndex = gyroscopeData.size() - BUFFER_SIZE;
        final int magneticfieldDataIndex = magneticFieldData.size() - BUFFER_SIZE;
        final int rotationVectiorDataIndex = rotationVectorData.size() - BUFFER_SIZE;

       /* Log.e("BUFFER SIZE", "Acc:\t" + linearAccelerationData.size() + "\tGyro:\t" + gyroscopeData.size() + "\tMagn:\t" + magneticFieldData.size() + "\tRot:\t" + rotationVectorData.size());
        Log.e("TIMESTAMPS", String.format("%.5f", (float)Iterables.getLast(linearAccelerationData).getTimestamp()) + "\t"
                + String.format("%.5f",(float) Iterables.getLast(gyroscopeData).getTimestamp()) + "\t"
                + String.format("%.5f",(float) Iterables.getLast(magneticFieldData).getTimestamp()) + "\t"
                + String.format("%.5f",(float) Iterables.getLast(rotationVectorData).getTimestamp()));*/
        float[] degs = new float[3];
        float[] rotationMatrix = new float[9];
        double tetaY;
        double tetaZ;


        for (int i = 0; i < BUFFER_SIZE; i++) {
            SensorManager.getRotationMatrix(rotationMatrix, null, rotationVectorData.get(rotationVectiorDataIndex + i).getValues(), magneticFieldData.get(magneticfieldDataIndex + i).getValues());
            SensorManager.getOrientation(rotationMatrix, degs);
            tetaY = degs[2];
            tetaZ = degs[0];


            final double verticalAccelertion = calculateAccelNormal(linearAccelerationData.get(linearAccelerationDataIndex + i).getX(), linearAccelerationData.get(linearAccelerationDataIndex + i).getY(), linearAccelerationData.get(linearAccelerationDataIndex + i).getZ(), tetaY, tetaZ);
            final double totalAcceleration = linearAccelerationData.get(linearAccelerationDataIndex + i).getModule();

            final double accelerationRation = verticalAccelertion / totalAcceleration;
            final float accelerationPercentage = (float) accelerationRation * 100f;

            /*Log.e("ALOGRITHM PROCESS", "Iteration:\t" + String.valueOf(i) + "\t" + String.format("aV:\t%.5f\t", verticalAccelertion)
                    + String.format("aT:\t%.5f\t", totalAcceleration)
                    + String.format("Ratio:\t%.3f\t", accelerationRation)
                    + String.format("Percentage:\t%.2f", accelerationPercentage));*/

            /*Log.e("Differances","Acc - Gyro"+String.format("%f ms",(linearAccelerationData.get(i).getTimestamp() - gyroscopeData.get(i).getTimestamp())/1000000.0f)+"\n"
            +"Acc - Magn"+String.format("%f ms",(linearAccelerationData.get(i).getTimestamp() - magneticFieldData.get(i).getTimestamp())/1000000.0f)+"\n"
            +"Acc - Rot"+String.format("%f ms",(linearAccelerationData.get(i).getTimestamp() - rotationVectorData.get(i).getTimestamp())/1000000.0f)+"\n");*/

            if (totalAcceleration >= TOTAL_ACC_THRESHOLD && verticalAccelertion >= VERTICAL_ACC_THRESHOLD) {
                if (accelerationRation >= ACC_COMPARISION_THRESHOLD_LOW && accelerationRation <= ACC_COMPARISION_THRESHOLD_HIGH) {
                    return true;
                }
            }
        }
        return false;
    }

    private static double calculateAccelNormal(double aX, double aY, double aZ, double thetaY, double thetaZ) {
        return Math.abs(aX * Math.sin(thetaZ) + aY * Math.sin(thetaY) - aZ * Math.cos(thetaY) * Math.cos(thetaZ));
    }
}
