package com.krsxh.conexa.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtils {
    public static final int REQ_READ_CONTACTS = 100;
    public static final int REQ_CALL_PHONE = 101;
    public static final int REQ_WRITE_CONTACTS = 102;

    public static boolean hasContactsPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestContactsPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
            new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
            REQ_READ_CONTACTS);
    }

    public static boolean hasCallPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE)
            == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCallPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
            new String[]{Manifest.permission.CALL_PHONE}, REQ_CALL_PHONE);
    }
}