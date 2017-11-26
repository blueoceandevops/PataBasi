package com.github.krtonga.busbabe.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * This catches SMS messages that are sent to the app.
 */

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReciever";
    private static SmsListener mListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        Bundle data = intent.getExtras();

        Object[] msgs = (Object[]) data.get("pdus");
        for (int i = 0; i < msgs.length; i++) {
            SmsMessage message = SmsMessage.createFromPdu((byte[])msgs[i]);
            String sender = message.getDisplayOriginatingAddress();
            Log.d(TAG, "onReceive: "+sender+","+message.getDisplayMessageBody());
        }
    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }

    public interface SmsListener {
        void smsParsed(Location location);
    }
}
