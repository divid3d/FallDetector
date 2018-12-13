package com.example.divided.falldetector;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import java.util.concurrent.TimeUnit;

public class TimeCounter extends LinearLayout {

    private TickerView mHour;
    private TickerView mMinute;
    private TickerView mSecond;

    public TimeCounter(Context context) {
        super(context);
        init(context);
    }

    public TimeCounter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        View mRootView = inflate(context, R.layout.main_activity_counter_layout, this);
        mHour = mRootView.findViewById(R.id.ticker_view_hour);
        mMinute = mRootView.findViewById(R.id.ticker_view_minute);
        mSecond = mRootView.findViewById(R.id.ticker_view_second);

        mHour.setCharacterLists(TickerUtils.provideNumberList());
        mMinute.setCharacterLists(TickerUtils.provideNumberList());
        mSecond.setCharacterLists(TickerUtils.provideNumberList());


    }

    public void setTypeFace(Typeface typeFace) {
        mHour.setTypeface(typeFace);
        mMinute.setTypeface(typeFace);
        mSecond.setTypeface(typeFace);
    }

    public void reset() {
        mHour.setText("00");
        mMinute.setText("00");
        mSecond.setText("00");
    }

    public void setTime(long mills) {
        setHour(mills);
        setMinutes(mills);
        setSecond(mills);
    }

    private void setHour(long time) {
        if (TimeUnit.MILLISECONDS.toHours(time) >= 10) {
            mHour.setText(String.valueOf(TimeUnit.MILLISECONDS.toHours(time)));
        } else {
            mHour.setText("0" + String.valueOf(TimeUnit.MILLISECONDS.toHours(time)));
        }
    }

    private void setMinutes(long time) {
        time -= TimeUnit.HOURS.toMillis(TimeUnit.MILLISECONDS.toHours(time));
        if (TimeUnit.MILLISECONDS.toMinutes(time) >= 10) {
            mMinute.setText(String.valueOf(TimeUnit.MILLISECONDS.toMinutes(time)));
        } else {
            mMinute.setText("0" + String.valueOf(TimeUnit.MILLISECONDS.toMinutes(time)));
        }
    }

    private void setSecond(long time) {
        time -= TimeUnit.MINUTES.toMillis(TimeUnit.MILLISECONDS.toMinutes(time));
        if (TimeUnit.MILLISECONDS.toSeconds(time) >= 10) {
            mSecond.setText(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(time)));
        } else {
            mSecond.setText("0" + String.valueOf(TimeUnit.MILLISECONDS.toSeconds(time)));
        }
    }

    public void show() {
        this.setVisibility(VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.counter_in_anim);
        animation.setFillAfter(true);
        this.startAnimation(animation);
    }

    public void hide() {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.counter_out_anim);
        animation.setFillAfter(true);
        this.startAnimation(animation);
    }
}
