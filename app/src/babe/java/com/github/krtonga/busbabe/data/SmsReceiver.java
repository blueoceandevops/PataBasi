package com.github.krtonga.busbabe.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.github.krtonga.busbabe.Utils;

import java.text.ParseException;

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
        if (data == null) {
            return;
        }

        Object[] msgs = (Object[]) data.get("pdus");
        if (msgs == null) {
            return;
        }
        for (Object msg : msgs) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) msg);
            String sender = sms.getDisplayOriginatingAddress();
            Log.d(TAG, "onReceive: " + sender + "," + sms.getDisplayMessageBody());
            String message = sms.getDisplayMessageBody();
            if (Utils.isFromABitch(message) && mListener != null) {
                try {
                    mListener.smsParsed(Utils.readSms(message), sender);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void bindListener(SmsListener listener) {
        mListener = listener;
    }

    public interface SmsListener {
        void smsParsed(Location location, String phoneNumber);
    }
}
