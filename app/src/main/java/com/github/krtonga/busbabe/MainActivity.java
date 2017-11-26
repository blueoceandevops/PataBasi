package com.github.krtonga.busbabe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.krtonga.busbabe.location.LocationTracker;

public class MainActivity extends AppCompatActivity implements LocationTracker.LocationListener {
    private static final String TAG = "MainActivity";

    private LocationTracker mLocationTracker;
    private FloatingActionButton mFab;
    private EditText mMinuteInterval;
    private EditText mSecondInterval;
    private EditText mPhoneNumber;

    private boolean mLocationServiceRunning;

    private View.OnClickListener mFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mLocationServiceRunning) {
                // stop service
                mLocationTracker.stopLocationUpdates();

                // update fab
                mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                mFab.setImageResource(R.drawable.ic_map_white_24dp);

            } else {
                // update fab
                mFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                mFab.setImageResource(R.drawable.ic_cancel_white_24px);

                mLocationTracker.start(MainActivity.this, getNumber(), getIntervalInMills());
                Snackbar.make(view, "GPS Tracker started", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            // update flag
            mLocationServiceRunning = !mLocationServiceRunning;
        }
    };

    private int getIntervalInMills() {
        String secondsStr = mSecondInterval.getText().toString();
        String minutesStr = mMinuteInterval.getText().toString();
        Log.d(TAG, "getIntervalInMills: LocationService "+minutesStr+","+secondsStr);
        int totalSeconds = getIntFromTextView(mSecondInterval) +
                (getIntFromTextView(mMinuteInterval)*60);

        return totalSeconds*1000;
    }

    private int getIntFromTextView(TextView textView) {
        String string = textView.getText().toString();
        if (string.isEmpty()) {
            string = textView.getHint().toString();
        }
        try {
            return Integer.parseInt(string);
        } catch(NumberFormatException p_ex) {
            Log.e(TAG, "getIntervalInMills: seconds was not number", p_ex);
            return 0;
        }
    }

    private String getNumber() {
        String number = mPhoneNumber.getText().toString();
        // TODO is this necessary
        if (number.isEmpty()) {
            number = mPhoneNumber.getHint().toString();
        }
        return number;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMinuteInterval = findViewById(R.id.interval_minutes);
        mSecondInterval = findViewById(R.id.interval_seconds);
        mPhoneNumber = findViewById(R.id.phone_number);

        isSmsPermissionGranted();
        mLocationTracker = new LocationTracker(this);

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(mFabClickListener);
    }

    private boolean isSmsPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 0);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (BuildConfig.FLAVOR.equals("babe")) {
                Log.d(TAG, "onOptionsItemSelected: Flavor = babe.");
                Intent intent = new Intent(this, MapActivity.class);
                startActivity(intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mLocationTracker.respondToActivityResult(requestCode, resultCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationTracker.respondToPermissions(requestCode, grantResults);
    }

    @Override
    public String getPermissionAlertTitle() {
        return "Permission required";
    }

    @Override
    public String getPermissionAlertDescription() {
        return "Byotch";
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(), "This shouldn't be called...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionDenied() {
        Toast.makeText(getApplicationContext(), "Yo!", Toast.LENGTH_LONG).show();
    }
}
