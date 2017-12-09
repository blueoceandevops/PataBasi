package com.github.krtonga.busbabe;

import android.location.Location;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * For your static method needs...
 */

public class Utils {
    private static final SimpleDateFormat SMS_FORMAT = new SimpleDateFormat("yyMMddHHmmss"	, Locale.getDefault()); // EXAMPLE: 010704120856

    // Example SMS: -12.43245;123.82732;112;11;23;359;010704120856
    public static  String createSms(Location location) {
        if (location == null) {
            return null;
        }
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double elevation = location.getAltitude();
        float accuracy = location.getAccuracy();
        float bearing = location.getBearing();
        String date = SMS_FORMAT.format(location.getTime());

        return latitude+";"+longitude+";"+elevation+";"+accuracy+";"+bearing+";"+date;
    }

    public static boolean isFromABitch(String message) {
        return message.matches(".+;.+;.+;.+;\\d+");
    }

    public static Location readSms(String message) throws ParseException {
        if (message == null) {
            return null;
        }
        Location location = new Location("sms");

        String[] parts = message.split(";");
        location.setLatitude(Double.valueOf(parts[0]));
        location.setLongitude(Double.valueOf(parts[1]));
        location.setAltitude(Float.valueOf(parts[2]));
        location.setAccuracy(Float.valueOf(parts[3]));
        location.setBearing(Float.valueOf(parts[4]));
        location.setTime(SMS_FORMAT.parse(parts[5]).getTime());

        return location;
    }

}
