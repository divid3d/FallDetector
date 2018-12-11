package com.example.divided.falldetector.model;

public class RotationVectorData extends Data {
    public RotationVectorData(float[] values, long timestamp) {
        super(values, timestamp);
    }

    RotationVectorData(float x, float y, float z, float cos, float accuracy, long timestamp) {
        super(new float[]{x, y, z, cos, accuracy}, timestamp);
    }

    /**
     * @return x*sin(θ/2)
     */
    public float getX() {
        return getValues()[0];
    }

    /**
     * @return y*sin(θ/2)
     */
    public float getY() {
        return getValues()[1];
    }

    /**
     * @return z*sin(θ/2)
     */
    public float getZ() {
        return getValues()[2];
    }

    /**
     * @return 1*cos(θ/2)
     */
    public float getCos() {
        return getValues()[3];
    }

    /**
     * @return estimated heading Accuracy (in radians) (-1 if unavailable)
     */
    public float getEstimatedHeadingAccuracy() {
        return getValues()[4];
    }
}
