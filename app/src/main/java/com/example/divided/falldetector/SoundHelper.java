package com.example.divided.falldetector;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.RawRes;

public class SoundHelper {

    private Context mContext;
    private MediaPlayer mMediaPlayer;

    SoundHelper(Context context, @RawRes int sound,boolean looping) {
        if (mContext == null) {
            mContext = context;
            mMediaPlayer = MediaPlayer.create(mContext, sound);
            mMediaPlayer.setLooping(looping);
        }
    }

    public void startSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    public void stopSound() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
            }
        }
    }

    public void release(){
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }
}
