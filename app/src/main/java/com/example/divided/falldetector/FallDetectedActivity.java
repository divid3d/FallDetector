package com.example.divided.falldetector;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class FallDetectedActivity extends AppCompatActivity {

    TextView mTextViewFallDetected;
    TextView mTextViewTimeRemaining;
    TextSwitcher mCommunicates;
    RelativeLayout mRequestCommunicate;
    Button mButtonCancel;
    CircularProgressBar mProgressBar;
    CountDownTimer countDownTimer;
    Vibrator vibrator;
    boolean isTimerRunning = false;
    Location currentLocation;
    UserSettings userSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall_detected);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        Toolbar toolbar = findViewById(R.id.fall_detection_activity_toolbar);
        setSupportActionBar(toolbar);

        TextView date = findViewById(R.id.text_view_date);
        date.setText(Utils.getCurrentDate());

        userSettings = new UserSettings(this);


        mTextViewFallDetected = findViewById(R.id.tv_alarm_text);
        mTextViewTimeRemaining = findViewById(R.id.tv_time_remaining);
        mRequestCommunicate = findViewById(R.id.communicate_layout);
        mCommunicates = findViewById(R.id.ts_communicate);
        mButtonCancel = findViewById(R.id.btn_cancel);

        MaterialRippleLayout.on(mButtonCancel)
                .rippleColor(Color.WHITE)
                .rippleAlpha(0.3f)
                .rippleHover(true)
                .rippleOverlay(true)
                .rippleRoundedCorners(2)
                .create();

        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }
        client.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation = location;
                Toast.makeText(getApplicationContext(), location.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        SoundHelper soundHelper = new SoundHelper(this);


        mButtonCancel.setOnClickListener(v -> {
            mButtonCancel.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_tap_anim));
            if (countDownTimer != null && isTimerRunning) {
                countDownTimer.cancel();
                if (userSettings.isVibrationEnabled()) {
                    cancelVibration(vibrator);
                }
                if (userSettings.isAlarmSoundEnabled()) {
                    soundHelper.stopAlarmSound();
                }
                Toast.makeText(getApplicationContext(), "Counting stopped by user", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        mProgressBar = findViewById(R.id.progress_bar);
        ProgressBar indicator = findViewById(R.id.progress_indicator);
        indicator.getIndeterminateDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (userSettings.isVibrationEnabled() && vibrator != null) {
            vibrateWithRepeat(vibrator);
        }

        if (userSettings.isAlarmSoundEnabled()) {
            soundHelper.startAlarmSound();
        }
        countDownTimer = new CountDownTimer(userSettings.getAlarmDuration() * 1000, 1) {

            @Override
            public void onTick(long millisUntilFinished) {
                mTextViewTimeRemaining.setText(Utils.getTime(millisUntilFinished));
                mProgressBar.setProgress((millisUntilFinished / (userSettings.getAlarmDuration() * 1000f)) * 100);
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                mTextViewTimeRemaining.setText(Utils.getTime(0));
                mProgressBar.setProgress(0);
                if (userSettings.isVibrationEnabled()) {
                    cancelVibration(vibrator);
                }
                if (userSettings.isAlarmSoundEnabled()) {
                    soundHelper.stopAlarmSound();
                }
                //sendSMS(new String[]{"732921078"});
                //sendEmail(new String[]{"woojciechczop@gmail.com", "n.kozlowska@vp.pl"});

                Animation cancelButtonAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                cancelButtonAnim.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mButtonCancel.setVisibility(View.GONE);
                        mRequestCommunicate.setVisibility(View.VISIBLE);
                        mRequestCommunicate.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in));
                        Animation fadeOutAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
                        fadeOutAnim.setFillAfter(true);
                        mTextViewTimeRemaining.startAnimation(fadeOutAnim);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mButtonCancel.startAnimation(cancelButtonAnim);
            }
        }.start();
        isTimerRunning = true;
        startAnimation();
    }

    private void startAnimation() {
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                mTextViewFallDetected,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f));
        scaleDown.setDuration(500);
        scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);
        scaleDown.start();
    }

    private void vibrateWithRepeat(Vibrator vibrator) {
        long[] mVibratePattern = new long[]{0, 500, 500};
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(mVibratePattern, 0);
        }
    }

    private void cancelVibration(Vibrator vibrator) {
        if (vibrator.hasVibrator()) {
            vibrator.cancel();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION,}, 1);
    }

    private void sendSMS(String[] phoneNumbers) {
        mCommunicates.setText("Sending SMS messages...");
        if (currentLocation != null) {
            for (String phoneNumber : phoneNumbers) {
                final String message = "Fall alert!\n" + "My location:\n" + "Lat: " + currentLocation.getLatitude()
                        + "\n" + "Long: " + currentLocation.getLongitude() + "\n\n"
                        + "https://maps.google.com/?q=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude();
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(phoneNumber, null, message, null, null);
            }
        }
    }

    private void sendEmail(String[] emailAdresses) {
        mCommunicates.setText("Sending e-mail messages...");

        if (currentLocation != null) {
            final String username = "robo6666666@gmail.com";
            final String password = "asiamakota333";
            final String body = "Fall alert!\n" + "My location:\n" + "Lat: " + currentLocation.getLatitude()
                    + "\n" + "Long: " + currentLocation.getLongitude() + "\n\n"
                    + "https://maps.google.com/?q=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude();

            GMailSender sender = new GMailSender(username, password);

            Thread senderThread = new Thread(() -> {
                for (String emailAddress : emailAdresses) {
                    try {
                        sender.sendMail("Fall alarm!",
                                body,
                                userSettings.getUsername(),
                                emailAddress);
                    } catch (Exception e) {
                        Log.e("E-MAIL", "Error: " + e.getMessage());
                    }
                }
            });
            senderThread.start();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mButtonCancel.callOnClick();
    }
}
