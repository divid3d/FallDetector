package com.example.divided.falldetector.model;


import android.util.Log;

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
        } else {
            packSize = linearAccelerationData.size();
        }


        /*long accTime = (Iterables.getLast(linearAccelerationData).getTimestamp() - linearAccelerationData.get(0).getTimestamp()) / 1000000;
        long gyroTime = (Iterables.getLast(gyroscopeData).getTimestamp() - gyroscopeData.get(0).getTimestamp()) / 1000000;
        long magnTime = (Iterables.getLast(magneticFieldData).getTimestamp() - magneticFieldData.get(0).getTimestamp()) / 1000000;
        long rotTime = (Iterables.getLast(rotationVectorData).getTimestamp() - rotationVectorData.get(0).getTimestamp()) / 1000000;*/

        /*long accTime = (linearAccelerationData.get(1).getTimestamp() - linearAccelerationData.get(0).getTimestamp()) / 1000000;
        long gyroTime = (gyroscopeData.get(1).getTimestamp() - gyroscopeData.get(0).getTimestamp()) / 1000000;
        long magnTime = (magneticFieldData.get(1).getTimestamp() - magneticFieldData.get(0).getTimestamp()) / 1000000;
        long rotTime = (rotationVectorData.get(1).getTimestamp() - rotationVectorData.get(0).getTimestamp()) / 1000000;*/

        /*Log.e("Time differences", "Acc:\t" + String.format("%.3f", (float) accTime)
                + "\tGyro:\t" + String.format("%.3f", (float) gyroTime)
                + "\tMagn:\t" + String.format("%.3f", (float) magnTime)
                + "\tRot:\t" + String.format("%.3f", (float) rotTime));*/

    }

    /*private static long findMaxPeriodDifference(List<LinearAccelerationData> linearAccelerationData, List<GyroscopeData> gyroscopeData,
                                                List<MagneticFieldData> magneticFieldData, List<RotationVectorData> rotationVectorData, int packSize) {
        long maxPeriodDifference = 0;


        for (int i = 0; i < packSize; i++) {
            long[] timeStamps = new long[]{
                    linearAccelerationData.get(i).getTimestamp(), gyroscopeData.get(i).getTimestamp(), magneticFieldData.get(i).getTimestamp(), rotationVectorData.get(i).getTimestamp()
            };

            for (int j = 0; j < 4; j++) {
                if (j == 0) {
                    if (Math.abs(timeStamps[0] - timeStamps[1]) > maxPeriodDifference) {
                        maxPeriodDifference = Math.abs(timeStamps[0] - timeStamps[1]);
                    }

                    if (Math.abs(timeStamps[0] - timeStamps[2]) > maxPeriodDifference) {
                        maxPeriodDifference = Math.abs(timeStamps[0] - timeStamps[2]);
                    }

                    if (Math.abs(timeStamps[0] - timeStamps[3]) > maxPeriodDifference) {
                        maxPeriodDifference = Math.abs(timeStamps[0] - timeStamps[3]);
                    }
                } else if (j == 1) {

                    if (Math.abs(timeStamps[1] - timeStamps[0]) > maxPeriodDifference) {
                        maxPeriodDifference = Math.abs(timeStamps[1] - timeStamps[0]);
                    }

                    if (Math.abs(timeStamps[1] - timeStamps[2]) > maxPeriodDifference) {
                        maxPeriodDifference = Math.abs(timeStamps[1] - timeStamps[2]);
                    }

                    if (Math.abs(timeStamps[1] - timeStamps[3]) > maxPeriodDifference) {
                        maxPeriodDifference = Math.abs(timeStamps[1] - timeStamps[3]);
                    }
                } else if (j == 2) {

                    if (Math.abs(timeStamps[2] - timeStamps[0]) > maxPeriodDifference) {
                        maxPeriodDifference = Math.abs(timeStamps[2] - timeStamps[0]);
                    }

                    if (Math.abs(timeStamps[2] - timeStamps[1]) > maxPeriodDifference) {
                        maxPeriodDifference = Math.abs(timeStamps[2] - timeStamps[1]);
                    }

                    if (Math.abs(timeStamps[2] - timeStamps[3]) > maxPeriodDifference) {
                        maxPeriodDifference = Math.abs(timeStamps[2] - timeStamps[3]);
                    }
                } else if (j == 3) {

                    if (Math.abs(timeStamps[3] - timeStamps[0]) > maxPeriodDifference) {
                        maxPeriodDifference = Math.abs(timeStamps[3] - timeStamps[0]);
                    }

                    if (Math.abs(timeStamps[3] - timeStamps[1]) > maxPeriodDifference) {
                        maxPeriodDifference = Math.abs(timeStamps[3] - timeStamps[1]);
                    }

                    if (Math.abs(timeStamps[3] - timeStamps[2]) > maxPeriodDifference) {
                        maxPeriodDifference = Math.abs(timeStamps[3] - timeStamps[2]);
                    }
                }
            }

        }

        return maxPeriodDifference;
    }*/

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

        packSize = Math.min(Math.min(linearAccelerationData.size(), gyroscopeData.size()), Math.min(magneticFieldData.size(), rotationVectorData.size()));

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
/*
        long firstTimeStamp;
        long lastTimeStamp;




        if (packSize == accelerationDataSize) {
            firstTimeStamp = linearAccelerationData.get(0).getTimestamp();
            lastTimeStamp = Iterables.getLast(linearAccelerationData).getTimestamp();
        } else if (packSize == gyroscopeDataSize) {
            firstTimeStamp = gyroscopeData.get(0).getTimestamp();
            lastTimeStamp = Iterables.getLast(gyroscopeData).getTimestamp();
        } else if (packSize == magneticFieldDataSize) {
            firstTimeStamp = magneticFieldData.get(0).getTimestamp();
            lastTimeStamp = Iterables.getLast(magneticFieldData).getTimestamp();
        } else {
            firstTimeStamp = rotationVectorData.get(0).getTimestamp();
            lastTimeStamp = Iterables.getLast(rotationVectorData).getTimestamp();
        }


        if (accelerationDataSize - packSize > 0) {
            for (int i = 0; i < accelerationDataSize - packSize; i++) {
                if ((Math.abs(firstTimeStamp - linearAccelerationData.get(0).getTimestamp()) > (Math.abs(lastTimeStamp - linearAccelerationData.get(linearAccelerationData.size() - 1).getTimestamp())))) {
                    linearAccelerationData.remove(i);
                } else {
                    linearAccelerationData.remove(linearAccelerationData.size() - 1);
                }
            }
        }

        if (gyroscopeDataSize - packSize > 0) {
            for (int i = 0; i < gyroscopeDataSize - packSize; i++) {
                if ((Math.abs(firstTimeStamp - gyroscopeData.get(0).getTimestamp()) > (Math.abs(lastTimeStamp - gyroscopeData.get(gyroscopeData.size() - 1).getTimestamp())))) {
                    gyroscopeData.remove(i);
                } else {
                    gyroscopeData.remove(gyroscopeData.size() - 1);
                }
            }
        }

        if (magneticFieldDataSize - packSize > 0) {
            for (int i = 0; i < magneticFieldDataSize - packSize; i++) {
                if ((Math.abs(firstTimeStamp - magneticFieldData.get(0).getTimestamp()) > (Math.abs(lastTimeStamp - magneticFieldData.get(magneticFieldData.size() - 1).getTimestamp())))) {
                    magneticFieldData.remove(i);
                } else {
                    magneticFieldData.remove(magneticFieldData.size() - 1);
                }
            }
        }

        if (rotationVectorDataSize - packSize > 0) {
            for (int i = 0; i < rotationVectorDataSize - packSize; i++) {
                if ((Math.abs(firstTimeStamp - rotationVectorData.get(0).getTimestamp()) > (Math.abs(lastTimeStamp - rotationVectorData.get(rotationVectorData.size() - 1).getTimestamp())))) {
                    rotationVectorData.remove(i);
                } else {
                    rotationVectorData.remove(rotationVectorData.size() - 1);
                }
            }
        }*/

        Log.e("Sensor data normalized", "Acc:\t" + linearAccelerationData.size() + "\tGyro:\t" + gyroscopeData.size() + "\tMagn:\t" + magneticFieldData.size() + "\tRot:\t" + rotationVectorData.size());

       /* for (int i = 0; i < packSize; i++) {
            Log.e("Test", "it\t" + (i + 1) + "\tta:\t" + String.valueOf(linearAccelerationData.get(i).mTimestamp)
                    + "\ttg:\t" + String.valueOf(gyroscopeData.get(i).getTimestamp())
                    + "\ttm:\t" + String.valueOf(magneticFieldData.get(i).getTimestamp())
                    + "\ttrv:\t" + String.valueOf(rotationVectorData.get(i).getTimestamp()));
        }*/
    }

    public int getPackSize() {
        return packSize;
    }

    private boolean areListsSameSize() {
        return this.linearAccelerationData.size() == this.gyroscopeData.size() && this.linearAccelerationData.size() == this.magneticFieldData.size() && this.linearAccelerationData.size() == this.rotationVectorData.size();
    }
}
