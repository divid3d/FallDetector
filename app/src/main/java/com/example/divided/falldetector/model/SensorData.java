package com.example.divided.falldetector.model;

public class SensorData<T> {
    private T data;
    private SensorType sensorType;

    public SensorData(T data, SensorType sensorType) {
        this.data = data;
        this.sensorType = sensorType;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public T getData() {
        return data;
    }

    public enum SensorType {
        SENSOR_LINEAR_ACCELERATION,
        SENSOR_GYROSCOPE,
        SENSOR_MAGNETIC_FIELD,
        SENSOR_ROTATION_VECTOR
    }

}
