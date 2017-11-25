package com.github.krtonga.busbabe.location;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;

import com.github.krtonga.busbabe.events.EventHandler;
import com.google.android.gms.common.api.Status;

/**
 * This is used to contain GLS methods which are used to find current location.
 *
 * *** IMPORTANT ***
 *
 * To avoid memory leaks, do not forget to call myLocationTracker.onPause()
 * in the onPause() Activity lifecycle method. EX:
 *
 *
 *
 * To ensure that permissions is updated correctly, override onRequestPermissionsResult()
 * in Activity. For example:
 *
 *   @Override
 *   public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
 *      mLocationTracker.respondToPermissions(requestCode, grantResults, this);
 *   }
 *
 * To ensure that GPS is turned on correctly, override onActivityResult() in Activity. For example:
 *
 *  @Override
 *  public void onActivityResult(int requestCode, int resultCode, Intent data) {
 *      mLocationTracker.respondToActivityResult(requestCode, resultCode);
 *  }
 */

public class LocationTracker {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private Activity mActivity;
    private LocationListener mChangeListener;

    private int mUpdateInterval = 60000; // time in millseconds
    private String mNumber;

    public LocationTracker(Activity activity) {
        mActivity = activity;
    }

    public LocationTracker(Activity activity, int interval) {
        mActivity = activity;
        mUpdateInterval = interval;
    }

    public void start(LocationListener listener, String phoneNumber, int interval) {
        mChangeListener = listener;
        mUpdateInterval = interval;
        mNumber = phoneNumber;

        // register for events from the location service
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(mGpsPermissionRequestEvent,
                new IntentFilter(EventHandler.BROADCAST_PLEASE_TURN_ON_GPS));

        // In newest APIs user might manually turn location permission off for this app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (appHasLocationPermission()) {
                LocationService.startLocationService(mActivity, mNumber, mUpdateInterval);
            } else {
                askForLocationPermission();
            }
        }
        // In previous APIs permissions are asked for on install
        else {
            LocationService.startLocationService(mActivity, mNumber, mUpdateInterval);
        }
    }

    public void stopLocationUpdates() {
        LocationService.stopLocationService(mActivity);
    }

    // This must be called in Activity onPause() to avoid memory leaks.
    public void onPause() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(mGpsPermissionRequestEvent);
    }

    // This is triggered when user attempted to start a LocationTracker when GPS was off
    public void respondToActivityResult(int requestCode, int resultCode) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            onPermissionsChange(resultCode == Activity.RESULT_OK);
        }
    }

    // This is triggered when user attempted to start a LocationTracker when location permission
    // has not been granted to the application
    public void respondToPermissions(int requestCode, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            onPermissionsChange(grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    private boolean appHasLocationPermission() {
        return ContextCompat.checkSelfPermission(mActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void askForLocationPermission() {
        if (!appHasLocationPermission()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    && mChangeListener.getPermissionAlertTitle() != null) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(mActivity)
                        .setTitle(mChangeListener.getPermissionAlertTitle())
                        .setMessage(mChangeListener.getPermissionAlertDescription())
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(mActivity,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    private void onPermissionsChange(boolean granted) {
        if (granted) {
            if (appHasLocationPermission()) {
                LocationService.startLocationService(mActivity.getBaseContext(),mNumber, mUpdateInterval);
            }
        } else {
            if (mChangeListener != null) {
                mChangeListener.onPermissionDenied();
            }
        }
    }

    private BroadcastReceiver mGpsPermissionRequestEvent = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getExtras() != null) {
                Status status = intent.getExtras().getParcelable(EventHandler.STATUS_EXTRA);
                if (status == null) {
                    return;
                }
                // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                try {
                    // NOTE: Parent Activity & Fragment must override onActivityResult().
                    status.startResolutionForResult(mActivity, MY_PERMISSIONS_REQUEST_LOCATION);
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                }
            }
        }
    };

    public interface LocationListener {
        String getPermissionAlertTitle();
        String getPermissionAlertDescription();
        void onLocationChanged(Location location);
        void onPermissionDenied();
    }
}
