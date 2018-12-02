package com.example.divided.falldetector;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundHelper {

    private Context mContext;
    private MediaPlayer mMediaPlayer;

    public SoundHelper(Context context) {

        if (mContext == null) {
            mContext = context;

            mMediaPlayer = MediaPlayer.create(mContext,
                    R.raw.alarm_1);
            mMediaPlayer.setLooping(true);

        }
    }

    public void startAlarmSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    public void stopAlarmSound() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
            }
        }
    }
}
