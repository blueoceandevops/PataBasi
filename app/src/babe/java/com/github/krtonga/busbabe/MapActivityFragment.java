package com.github.krtonga.busbabe;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.github.krtonga.busbabe.data.SmsReceiver;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.sql.Date;


/**
 * A placeholder fragment containing a simple view.
 */
public class MapActivityFragment extends MapboxBaseFragment implements SmsReceiver.SmsListener {
    public static final String TAG = "MapActivityFragment";
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;

    private int mCount;

    public MapActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        SmsReceiver.bindListener(this);
    }

    @Override
    protected int getFragLayoutId() {
        return R.layout.fragment_map;
    }

    @Override
    protected int getMapViewId() {
        return R.id.mapView;
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        super.onMapReady(mapboxMap);

        getSmsFromInbox();

        Snackbar.make(getView().getRootView(), "Found "+mCount+" SMS in your inbox", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void getSmsFromInbox() {
        Log.d(TAG, "getSmsFromInbox: ");
        if(isSmsReadEnabled()) {
            Log.d(TAG, "getSmsFromInbox: SMS Read enabled");
            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor cursor = getActivity().getContentResolver().query(
                    uriSms, new String[]{"_id", "address", "date", "body"},null, null, null);
            if (cursor == null) {
                return;
            }
            try {
                cursor.moveToFirst();
                mCount = 0;
                while (cursor.moveToNext()) {
                    String address = cursor.getString(1);
                    String body = cursor.getString(3);
                    Log.d(TAG, "getSms: " + address + ":" + body);
                    if (Utils.isFromABitch(body)) {
                        Log.d(TAG, "getSms:TRUE! " + address + ":" + body);
                        smsParsed(Utils.readSms(body), address);
                        mCount++;
                    }
                }
            } catch (java.text.ParseException e) {
                // Not a bitch after all...
            } finally {
                cursor.close();
            }
        } else {
            Log.d(TAG, "getSmsFromInbox: Asking for SMS Read permission");
            ActivityCompat.requestPermissions(getActivity(), new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
            // TODO Send activity intent back
        }
    }

    private boolean isSmsReadEnabled() {
        return ContextCompat.checkSelfPermission(getActivity().getBaseContext(),
                "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void smsParsed(Location location, String phoneNumber) {
        addMarker(new LatLng(location.getLatitude(), location.getLongitude()),
                phoneNumber, new Date(location.getTime()).toString());
    }
}
