package com.soft.sanislo.meetstrangers.utilities;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.soft.sanislo.meetstrangers.model.LocationSnapshot;

/**
 * Created by root on 02.09.16.
 */
public class LocationUtils {
    private static final String TAG = LocationUtils.class.getSimpleName();
    private static final int TIME_DELTA = 1000 * 5;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    public static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIME_DELTA;
        boolean isSignificantlyOlder = timeDelta < -TIME_DELTA;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    public static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public static Location getLocation(LocationSnapshot locationSnapshot) {
        Location location = new Location("");
        location.setLongitude(locationSnapshot.getLng());
        location.setLatitude(locationSnapshot.getLat());
        return location;
    }

    public static LatLng getLatLng(LocationSnapshot locationSnapshot) {
        LatLng latLng = new LatLng(locationSnapshot.getLat(), locationSnapshot.getLng());
        return latLng;
    }
}
