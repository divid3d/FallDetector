package com.example.divided.falldetector.model;


import com.example.divided.falldetector.Utils;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.LinkedList;
import java.util.List;

public class SensorDataPack {

    private List<LinearAccelerationData> linearAccelerationData = new LinkedList<>();
    private List<GyroscopeData> gyroscopeData = new LinkedList<>();
    private List<MagneticFieldData> magneticFieldData = new LinkedList<>();
    private List<RotationVectorData> rotationVectorData = new LinkedList<>();
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

        if (areListsSameSize()) {
            normalizeDataPack();
        } else {
            packSize = linearAccelerationData.size();
        }
    }

    public SensorDataPack(List<SensorData> buffer) {
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

        if (areListsSameSize()) {
            normalizeDataPack();
        } else {
            packSize = linearAccelerationData.size();
        }

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

        packSize = Utils.findMinValue(linearAccelerationData.size(), gyroscopeData.size(), magneticFieldData.size(), rotationVectorData.size());

        if (linearAccelerationData.size() - packSize > 0) {
            while (linearAccelerationData.size() != packSize) {
                linearAccelerationData.remove(0);
            }
        }

        if (gyroscopeData.size() - packSize > 0) {
            while (gyroscopeData.size() != packSize) {
                gyroscopeData.remove(0);
            }
        }

        if (magneticFieldData.size() - packSize > 0) {
            while (magneticFieldData.size() != packSize) {
                magneticFieldData.remove(0);
            }
        }

        if (rotationVectorData.size() - packSize > 0) {
            while (rotationVectorData.size() != packSize) {
                rotationVectorData.remove(0);
            }
        }
    }

    public int getPackSize() {
        return packSize;
    }

    private boolean areListsSameSize() {
        return this.linearAccelerationData.size() != this.gyroscopeData.size() || this.linearAccelerationData.size() != this.magneticFieldData.size() || this.linearAccelerationData.size() != this.rotationVectorData.size();
    }
}
