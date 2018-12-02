package com.example.divided.falldetector.model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SensorDataPack {

    private List<LinearAccelerationData> linearAccelerationData = new ArrayList<>();
    private List<GyroscopeData> gyroscopeData = new ArrayList<>();
    private List<MagneticFieldData> magneticFieldData = new ArrayList<>();
    private List<RotationVectorData> rotationVectorData = new ArrayList<>();
    private int dataSize;

    public SensorDataPack(List<LinearAccelerationData> linearAccelerationData, List<GyroscopeData> gyroscopeData, List<MagneticFieldData> magneticFieldData, List<RotationVectorData> rotationVectorData) {
        this.linearAccelerationData = linearAccelerationData;
        this.gyroscopeData = gyroscopeData;
        this.magneticFieldData = magneticFieldData;
        this.rotationVectorData = rotationVectorData;

    }
}
