package com.soft.sanislo.meetstrangers.utilities;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.text.TextUtils;
import android.util.Log;
import android.util.TimeUtils;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.LocationSnapshot;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by root on 05.09.16.
 */
public class Utils {
    public static final String TAG = Utils.class.getSimpleName();
    private static final long TIME_ONE_MINUTE = 1000 * 60;
    private static final long TIME_ONE_HOUR = TIME_ONE_MINUTE * 60;

    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    public static boolean validateEmailPwrd(Context context, String email, String password) {
        if (!isValidEmail(email)) {
            Toast.makeText(context, "Incorrect email address!", Toast.LENGTH_SHORT).show();
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

    public static boolean isValidName(Context context, String firstName, String lastName) {
        String regx = "^[\\p{L}\\s.’\\-,]+$";
        Pattern pattern = Pattern.compile(regx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(firstName);
        if (TextUtils.isEmpty(firstName)) {
            Toast.makeText(context, "First Name can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!matcher.find()) {
            Toast.makeText(context, "Invalid First Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(lastName)) {
            Toast.makeText(context, "Last Name can't be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        matcher = pattern.matcher(lastName);
        if (!matcher.find()) {
            Toast.makeText(context, "Invalid Last Name", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static Bitmap getCircledBitmap(Bitmap sourceBitmap) {
        Log.d(TAG, "getCircledBitmap: " + sourceBitmap.getWidth() + " x " + sourceBitmap.getHeight());
        int circleDiameter = Math.min(sourceBitmap.getWidth(), sourceBitmap.getHeight());
        float circleRadius = circleDiameter / 2;
        Log.d(TAG, "getCircledBitmap: circleDiameter: " + circleDiameter);

        int offsetX = (sourceBitmap.getWidth() - circleDiameter) / 2;
        int offsetY = (sourceBitmap.getHeight() - circleDiameter) / 2;
        Log.d(TAG, "getCircledBitmap: offsetX: " + offsetX + " offsetY: " + offsetY);

        Bitmap tmpBitmap = Bitmap.createBitmap(sourceBitmap,
                offsetX,
                offsetY,
                circleDiameter,
                circleDiameter);
        Bitmap squaredBitmap = tmpBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(squaredBitmap);
        canvas.drawARGB(0, 0, 0, 0);
        Path circlePath = new Path();
        circlePath.addCircle((circleDiameter - 1) / 2,
                (circleDiameter - 1) / 2,
                circleRadius,
                Path.Direction.CCW);
        canvas.clipPath(circlePath);

        Rect one = new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());
        RectF two = new RectF(0, 0, circleDiameter, circleDiameter);
        canvas.drawBitmap(squaredBitmap, one, two, new Paint());
        return squaredBitmap;
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

    public static String getFileName(String filePath) {
        String fileName = null;
        if (filePath == null) {
            return fileName;
        }
        File file = new File(filePath);
        if (file.exists()) {
            fileName = file.getName();
        }
        return fileName;
    }

    public static String getFileName(Context context, Uri uri) {
        String filePath = getPath(context, uri);
        String fileName = getFileName(filePath);
        return fileName;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {
        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
