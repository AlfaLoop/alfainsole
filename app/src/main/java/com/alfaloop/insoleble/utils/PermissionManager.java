package com.alfaloop.insoleble.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class PermissionManager {
    private static final int PERMISSION_REQUESTS = 1;

    public static String[] getRequiredPermissions(Context context) {
        try {
            PackageInfo info =
                    context.getPackageManager()
                            .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i("ML", "Permission granted: " + permission);
            return true;
        }
        Log.i("ML", "Permission NOT granted: " + permission);
        return false;
    }

    public static boolean allPermissionsGranted(Context context) {
        for (String permission : getRequiredPermissions(context)) {
            if (!isPermissionGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    public static void getRuntimePermissions(Context context) {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions(context)) {
            if (!isPermissionGranted(context, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    (Activity)context, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }
}
