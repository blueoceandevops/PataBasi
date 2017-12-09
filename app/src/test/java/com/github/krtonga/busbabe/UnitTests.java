package com.github.krtonga.busbabe;

import android.location.Location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(RobolectricTestRunner.class)
public class UnitTests {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void sms_canFormat() throws ParseException {
        Location mockLocation = new Location("mock");
        mockLocation.setLatitude(-12.43245);
        mockLocation.setLongitude(123.82732);
        mockLocation.setAltitude(112);
        mockLocation.setAccuracy(11);
        mockLocation.setBearing(23);
        Calendar c = Calendar.getInstance();
        c.set(2017,12-1,9,16,2,23);
        long mockTime = c.getTimeInMillis();
        mockLocation.setTime(mockTime);

        String smsMessage = Utils.createSms(mockLocation);
        Location readFromSms = Utils.readSms(smsMessage);

        assertEquals(-12.43245, readFromSms.getLatitude(),0);
        assertEquals(123.82732, readFromSms.getLongitude(), 0);
        assertEquals(112, readFromSms.getAltitude(), 0);
        assertEquals(11, readFromSms.getAccuracy(), 0);
        assertEquals(23, readFromSms.getBearing(), 0);
        assertEquals("Sat Dec 09 16:02:23 EAT 2017", new Date(readFromSms.getTime()).toString());
    }

    @Test
    public void sms_isFromABitch() {
        assertTrue(Utils.isFromABitch("-12.43245;123.82732;112;11;23;359;010704120856"));
        assertTrue(Utils.isFromABitch("-6.7712067;39.2399417;0.0;36.101;0.0;171209172214"));
        assertFalse(Utils.isFromABitch("Hey babe - My wife is on a business trip! Come on over!"));
    }
}
