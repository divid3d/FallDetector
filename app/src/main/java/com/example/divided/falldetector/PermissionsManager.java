package com.example.divided.falldetector;

import android.Manifest;
import android.support.v4.app.FragmentActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;

public class PermissionsManager {
    private RxPermissions rxPermissions;

    PermissionsManager(FragmentActivity activity) {
        rxPermissions = new RxPermissions(activity);
    }

    private boolean checkSmsPermission() {
        return rxPermissions.isGranted(Manifest.permission.SEND_SMS);
    }

    private boolean checkLocalizationPermission() {
        return rxPermissions.isGranted(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public void requestPermissions() {
        rxPermissions.request(Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe().dispose();

    }

    public boolean checkAllPermissions() {
        return checkSmsPermission() && checkLocalizationPermission();
    }
}
