package com.soft.sanislo.meetstrangers.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.soft.sanislo.meetstrangers.model.LocationSnapshot;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.LocationUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import java.util.Calendar;

/**
 * Created by root on 05.09.16.
 */
public class LocationService extends Service {
    public static final String REQUEST_CHECK_SETTINGS = "REQUEST_CHECK_SETTINGS";
    private static final long LOCATION_RC_FASTEST_INTERVAL = 1000 * 10;
    private static final long LOCATION_RC_INTERVAL = 1000 * 15;
    private static final String TAG = LocationService.class.getSimpleName();

    private DatabaseReference mDatabaseRef = Utils.getDatabase().getReference();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User mUser;
    private String mUid;

    private GoogleApiClient googleApiClient;
    private Location mCurrentLocation;
    private GoogleApiClient.ConnectionCallbacks connectionCallback;
    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener;
    private LocationListener locationListener;
    private LocationRequest locationRequest;
    private boolean isRequestingUpdates;

    private ValueEventListener mUserValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mUser = dataSnapshot.getValue(User.class);
            connectClient();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mUid = firebaseUser.getUid();

        initListeners();
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(connectionCallback)
                    .addOnConnectionFailedListener(connectionFailedListener)
                    .build();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        mDatabaseRef.child(Constants.F_USERS)
                .child(mUid)
                .addValueEventListener(mUserValueEventListener);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        disconnectClient();
        mDatabaseRef.child(Constants.F_USERS)
                .child(mUid)
                .removeEventListener(mUserValueEventListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * INIT LISTENERS
     */
    private void initListeners() {
        connectionCallback = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                Log.d(TAG, "onConnected: ");
                if (hasPermission()) {
                    initLocationRequest();
                } else {
                    Log.d(TAG, "onConnected: no permission");
                }
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        };

        connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Log.d(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
            }
        };

        locationListener = new LocationListener() {
            public void onLocationChanged(final Location newLocation) {
                // Called when a new location is found by the network location provider.
                boolean isBetterLocation = LocationUtils.isBetterLocation(newLocation, mCurrentLocation);
                Log.d(TAG, "onLocationChanged: isBetterLocation: " + isBetterLocation);
                if (isBetterLocation) {
                    Log.d(TAG, "onLocationChanged: " + newLocation);
                    pushNewLocation(newLocation);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "onStatusChanged: ");
            }

            public void onProviderEnabled(String provider) {
                Log.d(TAG, "onProviderEnabled: ");
            }

            public void onProviderDisabled(String provider) {
                Log.d(TAG, "onProviderDisabled: ");
            }
        };
    }

    private void pushNewLocation(Location newLocation) {
        mCurrentLocation = newLocation;
        LocationSnapshot locationSnapshot = new LocationSnapshot(firebaseUser.getUid(),
                newLocation.getLatitude(),
                newLocation.getLongitude(),
                Calendar.getInstance().getTimeInMillis(),
                mUser.getAvatarURL());
        mDatabaseRef.child(Constants.F_LOCATIONS).child(mUid).setValue(locationSnapshot);
    }

    private void initLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(LOCATION_RC_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_RC_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, locationListener);
        isRequestingUpdates = true;
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void connectClient() {
        Log.d(TAG, "connectClient: isConnected: " + googleApiClient.isConnected() + " isConnecting: " + googleApiClient.isConnecting());
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    private void disconnectClient() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            stopLocationUpdates();
            googleApiClient.disconnect();
        }
    }

    private void stopLocationUpdates() {
        if (isRequestingUpdates) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient,
                    locationListener);
        }
    }
}
