package com.example.divided.falldetector.model;


import android.util.Log;

import com.google.common.collect.Iterables;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.ArrayList;
import java.util.List;

public class SensorDataPack {

    private List<LinearAccelerationData> linearAccelerationData = new ArrayList<>();
    private List<GyroscopeData> gyroscopeData = new ArrayList<>();
    private List<MagneticFieldData> magneticFieldData = new ArrayList<>();
    private List<RotationVectorData> rotationVectorData = new ArrayList<>();
    private int packSize;

    public SensorDataPack(CircularFifoQueue<SensorData> buffer) {
        for (SensorData sensorData : buffer) {
            switch (sensorData.getSensorType()) {
                case SENSOR_LINEAR_ACCELERATION:
                    linearAccelerationData.add((LinearAccelerationData) sensorData.getData());
                    break;

                case SENSOR_GYROSCOPE:
                    gyroscopeData.add((GyroscopeData) sensorData.getData());
                    break;

                case SENSOR_MAGNETIC_FIELD:
                    magneticFieldData.add((MagneticFieldData) sensorData.getData());
                    break;

                case SENSOR_ROTATION_VECTOR:
                    rotationVectorData.add((RotationVectorData) sensorData.getData());
                    break;
            }
        }
        /*
        for (int i = 0; i < linearAccelerationData.size(); i++) {
            Log.e("Linear acceleration", "Index:\t" + i + "\tMod:\t" + linearAccelerationData.get(i).getModule() + "\tTimestamp:\t" + linearAccelerationData.get(i).getTimestamp());
        }
        for (int i = 0; i < gyroscopeData.size(); i++) {
            Log.e("Gyroscope", "Index:\t" + i + "\tMod:\t" + gyroscopeData.get(i).getModule() + "\tTimestamp:\t" + gyroscopeData.get(i).getTimestamp());
        }
        for (int i = 0; i < magneticFieldData.size(); i++) {
            Log.e("Magnetic field", "Index:\t" + i + "\tMod:\t" + magneticFieldData.get(i).getModule() + "\tTimestamp:\t" + magneticFieldData.get(i).getTimestamp());
        }
        for (int i = 0; i < rotationVectorData.size(); i++) {
            Log.e("Rotation Vector", "Index:\t" + i + "\tMod:\t" + rotationVectorData.get(i).getCos() + "\tTimestamp:\t" + rotationVectorData.get(i).getTimestamp());
        }
        */

        Log.e("Sensor data pack", "Acc:\t" + linearAccelerationData.size() + "\tGyro:\t" + gyroscopeData.size() + "\tMagn:\t" + magneticFieldData.size() + "\tRot:\t" + rotationVectorData.size());

        if (!areListsSameSize()) {
            normalizeDataPack();
        }else{
            packSize = linearAccelerationData.size();
        }

/*
        long accTime = (Iterables.getLast(linearAccelerationData).getTimestamp() - linearAccelerationData.get(0).getTimestamp()) / 1000000;
        long gyroTime = (Iterables.getLast(gyroscopeData).getTimestamp() - gyroscopeData.get(0).getTimestamp()) / 1000000;
        long magnTime = (Iterables.getLast(magneticFieldData).getTimestamp() - magneticFieldData.get(0).getTimestamp()) / 1000000;
        long rotTime = (Iterables.getLast(rotationVectorData).getTimestamp() - rotationVectorData.get(0).getTimestamp()) / 1000000;
*/
        /*Log.e("Time differences", "Acc:\t" + String.format("%.3f", (float) accTime)
                + "\tGyro:\t" + String.format("%.3f", (float) gyroTime)
                + "\tMagn:\t" + String.format("%.3f", (float) magnTime)
                + "\tRot:\t" + String.format("%.3f", (float) rotTime));*/

    }

    public List<LinearAccelerationData> getLinearAccelerationData() {
        return linearAccelerationData;
    }

    public List<GyroscopeData> getGyroscopeData() {
        return gyroscopeData;
    }

    public List<MagneticFieldData> getMagneticFieldData() {
        return magneticFieldData;
    }

    public List<RotationVectorData> getRotationVectorData() {
        return rotationVectorData;
    }

    private void normalizeDataPack() {
        final int accelerationDataSize = linearAccelerationData.size();
        final int gyroscopeDataSize = gyroscopeData.size();
        final int magneticFieldDataSize = magneticFieldData.size();
        final int rotationVectorDataSize = rotationVectorData.size();


        packSize = Math.min(Math.min(accelerationDataSize,gyroscopeDataSize),Math.min(magneticFieldDataSize,rotationVectorDataSize));


        if (accelerationDataSize - packSize != 0) {
            for (int i = 0; i < accelerationDataSize - packSize; i++) {
                linearAccelerationData.remove(i);
            }
        }

        if (gyroscopeDataSize - packSize != 0) {
            for (int i = 0; i < gyroscopeDataSize - packSize; i++) {
                gyroscopeData.remove(i);
            }
        }

        if (magneticFieldDataSize - packSize != 0) {
            for (int i = 0; i < magneticFieldDataSize - packSize; i++) {
                magneticFieldData.remove(i);
            }
        }

        if (rotationVectorDataSize - packSize != 0) {
            for (int i = 0; i < rotationVectorDataSize - packSize; i++) {
                rotationVectorData.remove(i);
            }
        }

        Log.e("Sensor data normalized", "Acc:\t" + linearAccelerationData.size() + "\tGyro:\t" + gyroscopeData.size() + "\tMagn:\t" + magneticFieldData.size() + "\tRot:\t" + rotationVectorData.size());


    }

    public int getPackSize() {
        return packSize;
    }

    private boolean areListsSameSize() {
        return this.linearAccelerationData.size() == this.gyroscopeData.size() && this.linearAccelerationData.size() == this.magneticFieldData.size() && this.linearAccelerationData.size() == this.rotationVectorData.size();
    }
}
