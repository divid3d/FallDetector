package com.example.divided.falldetector;

import android.content.Context;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.widget.Toast;

import com.example.divided.falldetector.model.SensorDataPack;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static String getTime(long time) {
        long timeToEdit = time;
        String textToDraw = "";
        if (TimeUnit.MILLISECONDS.toMinutes(time) >= 10) {
            textToDraw = textToDraw + TimeUnit.MILLISECONDS.toMinutes(time);
        } else {
            textToDraw = textToDraw + "0" + TimeUnit.MILLISECONDS.toMinutes(time);
        }
        timeToEdit -= TimeUnit.MINUTES.toMillis(TimeUnit.MILLISECONDS.toMinutes(timeToEdit));
        if (TimeUnit.MILLISECONDS.toSeconds(timeToEdit) >= 10) {
            textToDraw = textToDraw + ":" + TimeUnit.MILLISECONDS.toSeconds(timeToEdit);
        } else {
            textToDraw = textToDraw + ":0" + TimeUnit.MILLISECONDS.toSeconds(timeToEdit);
        }
        timeToEdit -= TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(timeToEdit));
        if (TimeUnit.MILLISECONDS.toMillis(timeToEdit) >= 100) {
            textToDraw = textToDraw + ":" + TimeUnit.MILLISECONDS.toMillis(timeToEdit);
        } else if (TimeUnit.MILLISECONDS.toMillis(timeToEdit) >= 10) {
            textToDraw = textToDraw + ":0" + TimeUnit.MILLISECONDS.toMillis(timeToEdit);
        } else {
            textToDraw = textToDraw + ":00" + TimeUnit.MILLISECONDS.toMillis(timeToEdit);
        }
        return textToDraw;
    }

    public static boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public static boolean isGPSEnabled(Context mContext) {
        LocationManager lm = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
        return !(lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    public static String getCurrentDate() {
        return new SimpleDateFormat("dd MMMM HH:mm:ss").format(new Date());
    }

    public static boolean saveAsCsv(Context context, SensorDataPack dataPack, String filename) {

        List<Double> verticalAccelerationData = calculateVerticalAccelerationData(dataPack);

        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "Acceleration signals");

        if (!directory.exists()) {
            directory.mkdir();
        }
        File newFile = new File(directory, filename + ".csv");

        try (CSVWriter writer = new CSVWriter(new FileWriter(newFile), ';', CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER)) {
            final long linearAccelTimeStampOffset = dataPack.getLinearAccelerationData().get(0).getTimestamp();
            final long gyroTimeStampOffset = dataPack.getGyroscopeData().get(0).getTimestamp();
            final long magneticTimeStampOffset = dataPack.getMagneticFieldData().get(0).getTimestamp();
            final long rotationVectorTimeStampOffset = dataPack.getRotationVectorData().get(0).getTimestamp();

            List<String[]> data = new ArrayList<>();
            for (int i = 0; i < dataPack.getPackSize(); i++) {
                data.add(new String[]{String.format("%d", dataPack.getLinearAccelerationData().get(i).getTimestamp() - linearAccelTimeStampOffset) // czas w ns
                        , String.format("%f", dataPack.getLinearAccelerationData().get(i).getX())   //przyspieszenie liniowe X
                        , String.format("%f", dataPack.getLinearAccelerationData().get(i).getY())   //przyspieszenie liniowe Y
                        , String.format("%f", dataPack.getLinearAccelerationData().get(i).getZ())   //przuspieszenie liniowe Z
                        , String.format("%f", dataPack.getLinearAccelerationData().get(i).getModule())  //przyspieszenie liniowe Mod
                        , String.format("%d", dataPack.getGyroscopeData().get(i).getTimestamp() - gyroTimeStampOffset)
                        , String.format("%f", dataPack.getGyroscopeData().get(i).getX()) //zyroskop x
                        , String.format("%f", dataPack.getGyroscopeData().get(i).getY()) //zyroskop y
                        , String.format("%f", dataPack.getGyroscopeData().get(i).getZ()) //zyroskop Z
                        , String.format("%f", dataPack.getGyroscopeData().get(i).getModule())    //zyroskop Mod
                        , String.format("%d", dataPack.getMagneticFieldData().get(i).getTimestamp() - magneticTimeStampOffset)
                        , String.format("%f", dataPack.getMagneticFieldData().get(i).getX()) //pole magnetyczne x
                        , String.format("%f", dataPack.getMagneticFieldData().get(i).getY()) //pole magnetyczne y
                        , String.format("%f", dataPack.getMagneticFieldData().get(i).getZ()) //polemagnetyczne Z
                        , String.format("%f", dataPack.getMagneticFieldData().get(i).getModule())    //pole magnetyczne Mod
                        , String.format("%d", dataPack.getRotationVectorData().get(i).getTimestamp() - rotationVectorTimeStampOffset)
                        , String.format("%f", dataPack.getRotationVectorData().get(i).getX()) //wektor rotacji x
                        , String.format("%f", dataPack.getRotationVectorData().get(i).getY()) //wektor rotacji y
                        , String.format("%f", dataPack.getRotationVectorData().get(i).getZ()) //wektor rotacji z
                        , String.format("%f", verticalAccelerationData.get(i)) // składowa werykalna przyspieszenia
                });
            }
            writer.writeAll(data);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "File save failed", Toast.LENGTH_SHORT).show();
            return false;
        }

        Toast.makeText(context, "File save successfull", Toast.LENGTH_SHORT).show();
        return true;
    }

    private static List<Double> calculateVerticalAccelerationData(SensorDataPack dataPack) {
        List<Double> verticalAccelerationData = new ArrayList<>();

        float[] degrees = new float[3];
        float[] rotationMatrix = new float[9];
        double thetaY;
        double thetaZ;

        for (int i = 0; i < dataPack.getPackSize(); i++) {
            android.hardware.SensorManager.getRotationMatrix(rotationMatrix, null, dataPack.getRotationVectorData().get(i).getValues(), dataPack.getMagneticFieldData().get(i).getValues());
            SensorManager.getOrientation(rotationMatrix, degrees);
            thetaY = degrees[2];
            thetaZ = degrees[0];


            final double verticalAcceleration = calculateAccelerationNormal(dataPack.getLinearAccelerationData().get(i).getX(), dataPack.getLinearAccelerationData().get(i).getY(), dataPack.getLinearAccelerationData().get(i).getZ(), thetaY, thetaZ);
            verticalAccelerationData.add(verticalAcceleration);
        }

        return verticalAccelerationData;
    }

    private static double calculateAccelerationNormal(double aX, double aY, double aZ, double thetaY, double thetaZ) {

        return Math.abs(aX * Math.sin(thetaZ) + aY * Math.sin(thetaY) - aZ * Math.cos(thetaY) * Math.cos(thetaZ));
    }

    public static int findMinValue(int... args) {
        int minimum = args[0];
        for (int i = 0; i < args.length - 2; i++) {
            if (Math.min(args[i], args[i + 1]) < minimum) {
                minimum = Math.min(args[i], args[i + 1]);
            }
        }
        return minimum;
    }
}
