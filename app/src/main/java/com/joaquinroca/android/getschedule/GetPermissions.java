package com.joaquinroca.android.getschedule;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/*
 * Created by joaquinroca on 8/20/17.
 */

class GetPermissions {
    private static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 1;


    // https://developer.android.com/training/permissions/requesting.html
    static void checkCalendarPermission(Activity thisActivity) {

        // https://developer.android.com/reference/android/support/v4/content/ContextCompat.html
        int permissionCheck = ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.READ_CALENDAR);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            // Gets whether you should show UI with rationale for requesting a permission.
            // You should do this only if you do not have the permission and the context in which the permission
            // is requested does not clearly communicate to the user what would be the benefit from granting this permission.
            // https://developer.android.com/reference/android/support/v4/app/ActivityCompat.html
            boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(thisActivity, Manifest.permission.READ_CALENDAR);
            if (showRationale) {
                // TODO: should probably show an explanation.
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // MY_PERMISSIONS_REQUEST_READ_CALENDAR is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                // https://developer.android.com/reference/android/support/v4/app/ActivityCompat.html#requestPermissions(android.app.Activity, java.lang.String[], int)
                ActivityCompat.requestPermissions(
                        thisActivity,
                        new String[]{Manifest.permission.READ_CALENDAR},
                        MY_PERMISSIONS_REQUEST_READ_CALENDAR
                );

            }
        }

    }
}
