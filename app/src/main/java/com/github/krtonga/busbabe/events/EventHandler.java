package com.github.krtonga.busbabe.events;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.common.api.Status;

/**
 * This is used as a place to put events.
 */

public class EventHandler {
    public final static String BROADCAST_PLEASE_TURN_ON_GPS = "com.github.krtonga.busbabe.PLEASE_TURN_ON_GPS";
    public final static String STATUS_EXTRA = "status";

    public static void sendGpsRequest(Context context, Status status) {
        Intent resultIntent = new Intent();
        resultIntent.setAction(BROADCAST_PLEASE_TURN_ON_GPS);
        resultIntent.putExtra(STATUS_EXTRA, status);
        LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent);
    }
}
