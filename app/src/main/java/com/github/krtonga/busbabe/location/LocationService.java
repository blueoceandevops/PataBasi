package com.github.krtonga.busbabe.location;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.github.krtonga.busbabe.events.EventHandler;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.List;

/**
 * This sends location updates at the given interval.
 */

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String TAG = "LocationService";
    private static final String START_LOCATION_UPDATES = "com.github.krtonga.busbabe.sendupdate";
    private static final String INTERVAL_EXTRA = "interval";
    private static final String PHONE_EXTRA = "phone";

    private LocationServiceBinder mBinder;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private int mUpdateInterval;
    private String mPhoneNumber;

    public static void startLocationService(Context context, String phoneNumber, int interval) {
        Log.d(TAG, "startLocationService: ");
        Intent startIntent = new Intent(context, LocationService.class);
        startIntent.setAction(START_LOCATION_UPDATES);
        startIntent.putExtra(INTERVAL_EXTRA, interval);
        startIntent.putExtra(PHONE_EXTRA, phoneNumber);
        context.startService(startIntent);
    }

    public static void stopLocationService(Context context) {
        Log.d(TAG, "stopLocationService: ");
        Intent stopIntent = new Intent(context, LocationService.class);
        context.stopService(stopIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case START_LOCATION_UPDATES :
                    mUpdateInterval = intent.getIntExtra(INTERVAL_EXTRA, 60000);
                    mPhoneNumber = intent.getStringExtra(PHONE_EXTRA);
                    buildGoogleApiClient();
                    break;
            }
        }
        // If the system kills the service after onStartCommand() returns, recreate the service
        // and call onStartCommand() with the last intent that was delivered to the service.
        // Any pending intents are delivered in turn.
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private synchronized void buildGoogleApiClient() {
        Log.d(TAG, "buildGoogleApiClient: "+mUpdateInterval);
        mGoogleApiClient = new GoogleApiClient.Builder(getBaseContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(mUpdateInterval);
        mLocationRequest.setFastestInterval(mUpdateInterval); // should it steal from other requests?
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: ");
        mLocationRequest = createLocationRequest();

        // For our purposes, we require that GPS is turned on. The following checks if GPS is on,
        // and if not presently enabled prompts the user
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates state = locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.d(TAG, "onConnected:onResult: GPS SUCCESS");
                        // All location settings are satisfied. The client can initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d(TAG, "onConnected:onResult: GPS RESOLUTION REQUIRED");
                        EventHandler.sendGpsRequest(getBaseContext(), status);
                        // Location settings are not satisfied. But could be fixed by showing the user a dialog.
//                        try {
//                            // NOTE: Parent Activity & Fragment must override onActivityResult().
//                            status.startResolutionForResult(mActivity, MY_PERMISSIONS_REQUEST_LOCATION);
//                        } catch (IntentSender.SendIntentException e) {
//                            // Ignore the error.
//                        }
//                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix it.
                        Log.d(TAG, "onConnected:onResult: GPS RESOLUTION IMPOSSIBLE");
                        break;
                }
                // For now, just start them...
                requestLocationUpdates();
            }
        });
    }

    private void requestLocationUpdates() {
        Log.d(TAG, "requestLocationUpdates: ");
        if (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            System.out.println("Last location: "+LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: "+location);
        sendSms(location);
    }

    private void sendSms(Location location) {
        SmsManager smsManager = SmsManager.getDefault();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            int subscriptionId = SmsManager.getDefaultSmsSubscriptionId();

            // On dual SIM phones, default SIM is null
            if (subscriptionId == -1) {
                SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
                List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
                subscriptionId = subscriptionInfoList.get(0).getSubscriptionId();
            }
            smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
            Log.d(TAG,"DEFAULT subscriptionId:"+subscriptionId);
        }

        Log.d(TAG, "sendSms: "+mPhoneNumber);
        smsManager.sendTextMessage(mPhoneNumber,
                null,
                location.toString(),
                null,
                null);
    }


    private class LocationServiceBinder extends Binder {
        LocationService getService() {
            return LocationService.this;
        }
    }
}
