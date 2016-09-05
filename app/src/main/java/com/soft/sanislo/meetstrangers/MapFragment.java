package com.soft.sanislo.meetstrangers;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.util.HashMap;
import java.util.Map;

public class MapFragment extends com.google.android.gms.maps.MapFragment implements OnMapReadyCallback {
    public static final String REQUEST_CHECK_SETTINGS = "REQUEST_CHECK_SETTINGS";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final long LOCATION_REQUEST_INTERVAL = 1000 * 5;

    private LocationListener locationListener;
    private GoogleMap mMap;
    private Location mLocation;
    private Marker mMarker;
    private GoogleApiClient googleApiClient;
    private GoogleApiClient.ConnectionCallbacks connectionCallback;
    private GoogleApiClient.OnConnectionFailedListener connectionFailedListener;
    private LocationRequest locationRequest;
    private PendingResult<LocationSettingsResult> result;
    private boolean isRequestUpdates;

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String uid;
    private ChildEventListener locationEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
            LocationModel locationModel = dataSnapshot.getValue(LocationModel.class);

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        initListeners();
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(connectionCallback)
                    .addOnConnectionFailedListener(connectionFailedListener)
                    .build();
        }
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();
        getMapAsync(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * INIT LISTENERS
     */
    private void initListeners() {
        connectionCallback = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                Log.d(TAG, "onConnected: ");
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
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
                    mLocation = newLocation;
                    addMyMarker();
                    moveCamera(mLocation);

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

    private void moveCamera(Location location) {
        CameraPosition position = CameraPosition.builder()
                .target(new LatLng(location.getLatitude(),
                        location.getLongitude()))
                .zoom(16f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();

        int zoom = 15;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        Log.d(TAG, "moveCamera: ");
    }

    private void animateCamera(Location location) {
        CameraPosition position = CameraPosition.builder()
                .target(new LatLng(location.getLatitude(),
                        location.getLongitude()))
                .zoom(16f)
                .bearing(0.0f)
                .tilt(0.0f)
                .build();

        int zoom = 15;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                Toast.makeText(getActivity(), "animate finished", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void addMyMarker() {
        if (mMarker != null) {
            mMarker.remove();
        }
        String title = firebaseUser.getDisplayName();
        Bitmap bitmap = BitmapFactory.decodeFile("");
        LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        MarkerOptions options = new MarkerOptions().position(latLng)
                .title(title)/**.icon(BitmapDescriptorFactory.fromBitmap(bitmap)*/;
        mMarker = mMap.addMarker(options);
        Log.d(TAG, "onLocationChanged: mMarker is null:" + mMarker.getId());
    }

    @Override
    public void onStart() {
        super.onStart();
        connectClient();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        disconnectClient();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, locationListener);
    }

    private void connectClient() {
        Log.d(TAG, "onStart: isCOnnected" + googleApiClient.isConnected()
                + " isConnecting: " + googleApiClient.isConnecting());
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    private void disconnectClient() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                stopLocationUpdates();
                googleApiClient.disconnect();
            }
        }
    }

    private void stopLocationUpdates() {
        if (isRequestUpdates) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, locationListener);
            Log.d(TAG, "stopLocationUpdates: ");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }
}
