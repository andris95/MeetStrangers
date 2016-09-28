package com.soft.sanislo.meetstrangers.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.soft.sanislo.meetstrangers.model.LocationSnapshot;

import java.util.Calendar;

/**
 * Created by root on 05.09.16.
 */
public class Utils {
    public static final String TAG = Utils.class.getSimpleName();
    private static final long TIME_ONE_MINUTE = 1000 * 60;
    private static final long TIME_ONE_HOUR = TIME_ONE_MINUTE * 60;

    public static boolean validate(Context context, String email, String password) {
        if (!isValidEmail(email)) {
            Toast.makeText(context, "Enter email address!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(context, "Enter password!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(context, "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static boolean isValidEmail(CharSequence target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static String getLastOnline(LocationSnapshot model) {
        Calendar currentCalendar = Calendar.getInstance();
        long currentMillis = currentCalendar.getTimeInMillis();
        long markerMillis = model.getTimestamp();

        long differenceInMillis = currentMillis - markerMillis;
        if (differenceInMillis < TIME_ONE_MINUTE) {
            return "Last active a few seconds ago";
        } else if (differenceInMillis > TIME_ONE_MINUTE && differenceInMillis < TIME_ONE_HOUR) {
            int minutes = (int) differenceInMillis / 1000 / 60;
            return "Last active " + minutes + " ago";
        } else {
            return "unknown";
        }
    }

    public static String getPostDate(long postTimestamp) {
        String postDate;
        Calendar currentCalendar = Calendar.getInstance();
        Calendar postCalendar = Calendar.getInstance();
        postCalendar.setTimeInMillis(postTimestamp);

        int difference = currentCalendar.compareTo(postCalendar);
        Log.d(TAG, "getPostDate: difference: " + difference);
        return null;
    }
}
