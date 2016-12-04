package com.soft.sanislo.meetstrangers.utilities;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by root on 16.10.16.
 */

public class DateUtils {
    private static final String TAG = DateUtils.class.getSimpleName();

    public static String getDateDisplay(long timestamp) {
        String dateInString = null;
        SimpleDateFormat dateFormat = getDateFormat(timestamp);
        dateInString = dateFormat.format(new Date(timestamp));
        Log.d(TAG, "getDateDisplay: dateInString: " + dateInString);
        return dateInString;
    }

    private static SimpleDateFormat getDateFormat(long timestamp) {
        SimpleDateFormat dateFormat = null;
        Calendar calendarNow = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        Calendar calendarTemp = Calendar.getInstance();
        calendarTemp.set(Calendar.DAY_OF_YEAR, calendarNow.get(Calendar.DAY_OF_YEAR) - 1);

        if (calendarNow.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
            if (calendar.get(Calendar.DAY_OF_YEAR) == calendarNow.get(Calendar.DAY_OF_YEAR)) {
                dateFormat = new SimpleDateFormat("HH:mm");
                return dateFormat;
            }
            if (calendar.get(Calendar.DAY_OF_YEAR) == calendarTemp.get(Calendar.DAY_OF_YEAR)) {
                dateFormat = new SimpleDateFormat("'yesterday at' HH:mm");
                return dateFormat;
            } else {
                return new SimpleDateFormat("d MMM");
            }
        } else {
            dateFormat = new SimpleDateFormat("d MMM yyyy");
            return dateFormat;
        }
    }
}
