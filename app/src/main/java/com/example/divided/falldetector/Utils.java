package com.example.divided.falldetector;

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

}
