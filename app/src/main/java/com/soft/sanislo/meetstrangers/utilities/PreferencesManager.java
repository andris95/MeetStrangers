package com.soft.sanislo.meetstrangers.utilities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by root on 21.01.17.
 */

public class PreferencesManager {
    private static final String sSharedPrefName = "SETTINGS";
    private static SharedPreferences sSharedPreferences;

    private static final String KEY_LOCATION_SHARED = "KEY_LOCATION_SHARED";

    public static final SharedPreferences getSharedPreferences(Context context) {
        if (sSharedPreferences == null) {
            sSharedPreferences = context.getSharedPreferences(sSharedPrefName, Context.MODE_PRIVATE);
        }
        return sSharedPreferences;
    }

    public static void setLocationShared(Context context, boolean isLocationShared) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putBoolean(KEY_LOCATION_SHARED, isLocationShared);
        editor.apply();
    }

    public static boolean isLocationShared(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_LOCATION_SHARED, false);
    }
}
