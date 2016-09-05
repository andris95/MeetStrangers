package com.soft.sanislo.meetstrangers;

import android.*;
import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.style.LocaleSpan;
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
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.soft.sanislo.meetstrangers.model.LocationModel;
import com.soft.sanislo.meetstrangers.utilities.LocationUtils;

import java.util.Calendar;

/**
 * Created by root on 05.09.16.
 */
public class LocationService extends IntentService {
    public static final String REQUEST_CHECK_SETTINGS = "REQUEST_CHECK_SETTINGS";
    private static final long LOCATION_REQUEST_INTERVAL = 1000 * 5;
    private static final String TAG = LocaleSpan.class.getSimpleName();

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String uid;

    private GoogleApiClient googleApiClient;
    private Location mLocation;
    private GoogleApiClient.ConnectionCallbacks connectionCallback;
    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener;
    private LocationListener locationListener;
    private LocationRequest locationRequest;
    private PendingResult<LocationSettingsResult> result;
    private boolean isRequestUpdates;

    public LocationService() {
        super(TAG);
        
    }

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();
        Log.d(TAG, "LocationService: uid: " + uid);

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
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: ");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart: ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    /**
     * INIT LISTENERS
     */
    private void initListeners() {
        connectionCallback = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                Log.d(TAG, "onConnected: ");
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    locationRequest = new LocationRequest();
                    locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
                    locationRequest.setFastestInterval(LOCATION_REQUEST_INTERVAL * 2);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                            builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                        @Override
                        public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                            Log.d(TAG, "onResult: locationResult" + locationSettingsResult.toString());
                            final Status status = locationSettingsResult.getStatus();
                            final LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied. The client can
                                    // initialize location requests here.
                                    Log.d(TAG, "onResult: SUCCESS");
                                    LocationServices.FusedLocationApi.requestLocationUpdates(
                                            googleApiClient, locationRequest, locationListener);
                                    isRequestUpdates = true;
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied, but this can be fixed
                                    // by showing the user a dialog.
                                    /*try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        status.startResolutionForResult(
                                                this,
                                                REQUEST_CHECK_SETTINGS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                        e.printStackTrace();
                                    }
                                    break;*/
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied. However, we have no way
                                    // to fix the settings so we won't show the dialog.
                                    break;
                            }
                        }
                    });
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
            public void onLocationChanged(Location newLocation) {
                // Called when a new location is found by the network location provider.
                boolean isBetterLocation = LocationUtils.isBetterLocation(newLocation, mLocation);
                if (isBetterLocation) {
                    Log.d(TAG, "onLocationChanged: isBetterLocation" + newLocation);
                    LocationModel location = new LocationModel(mLocation.getLatitude(), mLocation.getLongitude(), Calendar.getInstance().getTimeInMillis());

                    database.child("locations").child(uid).setValue(location).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: saved location " + mLocation);
                            }
                        }
                    });
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
    }
}
