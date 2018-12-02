package com.example.divided.falldetector;

public class ChartPoint {
    private float Value;
    private float timeStamp;

    public ChartPoint(float Value, float timeStamp){
        this.Value = Value;
        this.timeStamp = timeStamp;
    }

    public float getValue() {
        return Value;
    }

    public float getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(float timeStamp){
        this.timeStamp = timeStamp;
    }
}
