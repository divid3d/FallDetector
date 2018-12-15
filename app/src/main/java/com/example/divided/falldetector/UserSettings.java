package com.example.divided.falldetector;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class UserSettings implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Context mContext;
    private String username;
    private boolean isSmsEnabled;
    private boolean isEmailEnabled;
    private String phoneNumber;
    private String emailAddress;
    private String emailLogin;
    private String emailPassword;
    private boolean isVibrationEnabled;
    private int alarmDuration;
    private boolean isAlarmSoundEnabled;

    UserSettings(Context context) {
        mContext = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.registerOnSharedPreferenceChangeListener(this);
        this.username = prefs.getString("key_username", "");
        this.isSmsEnabled = prefs.getBoolean("sms_request_enable", false);
        this.isEmailEnabled = prefs.getBoolean("email_request_enable", false);
        this.phoneNumber = prefs.getString("key_phone_number", "");
        this.emailAddress = prefs.getString("key_email_address", "");
        this.emailLogin = prefs.getString("key_email_login", "");
        this.emailPassword = prefs.getString("key_email_password", "");
        this.isVibrationEnabled = prefs.getBoolean("alarm_vibration_enable", false);
        this.alarmDuration = prefs.getInt("key_alarm_time", 0);
        this.isAlarmSoundEnabled = prefs.getBoolean("key_alarm_sound", false);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("key_username")) {
            this.username = sharedPreferences.getString("key_username", "");
        } else if (key.equals("sms_request_enable")) {
            this.isSmsEnabled = sharedPreferences.getBoolean("sms_request_enable", false);
        } else if (key.equals("email_request_enable")) {
            this.isEmailEnabled = sharedPreferences.getBoolean("email_request_enable", false);
        } else if (key.equals("key_phone_number")) {
            this.phoneNumber = sharedPreferences.getString("key_phone_number", "");
        } else if (key.equals("key_email_address")) {
            this.emailAddress = sharedPreferences.getString("key_email_address", "");
        } else if (key.equals("key_email_login")) {
            this.emailLogin = sharedPreferences.getString("key_email_login", "");
        } else if (key.equals("key_email_password")) {
            this.emailPassword = sharedPreferences.getString("key_email_password", "");
        } else if (key.equals("alarm_vibration_enable")) {
            this.isVibrationEnabled = sharedPreferences.getBoolean("alarm_vibration_enable", false);
        } else if (key.equals("key_alarm_time")) {
            this.alarmDuration = sharedPreferences.getInt("key_alarm_time", 0);
        } else if (key.equals("key_alarm_sound")) {
            this.isAlarmSoundEnabled = sharedPreferences.getBoolean("key_alarm_sound", false);
        }
    }

    public boolean verify() {
        if (username.trim().length() == 0) {
            Toast.makeText(mContext, "Please enter username", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!(isSmsEnabled || isEmailEnabled)) {
            Toast.makeText(mContext, "Please enable sms or mail request", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            if (isEmailEnabled) {
                if (emailAddress.trim().length() == 0 || emailLogin.trim().length() == 0 || emailPassword.trim().length() == 0) {
                    Toast.makeText(mContext, "Please fill all mail related fields", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            if (isSmsEnabled) {
                if (phoneNumber.trim().length() == 0) {
                    Toast.makeText(mContext, "Please fill phone number field", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        return true;
    }

    public int getAlarmDuration() {
        return alarmDuration;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getEmailLogin() {
        return emailLogin;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public String getUsername() {
        return username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isAlarmSoundEnabled() {
        return isAlarmSoundEnabled;
    }

    public boolean isEmailEnabled() {
        return isEmailEnabled;
    }

    public boolean isSmsEnabled() {
        return isSmsEnabled;
    }

    public boolean isVibrationEnabled() {
        return isVibrationEnabled;
    }
}
