package com.soft.sanislo.meetstrangers;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.soft.sanislo.meetstrangers.model.LocationModel;

import java.util.ArrayList;
import java.util.HashMap;

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
    private boolean isLocationListenerAttached;
    private boolean isCameraMoved;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference locationReference = databaseReference.child("locations");
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String uid;
    private ArrayList<LocationModel> locationModels = new ArrayList<>();
    private HashMap<String, LocationModel> locationModelHashMap = new HashMap<>();
    private HashMap<String, Marker> mMarkers = new HashMap<>();

    private DisplayImageOptions options = new DisplayImageOptions.Builder()
            /*.showImageOnLoading(R.drawable.ic_stub) // resource or drawable
            .showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
            .showImageOnFail(R.drawable.ic_error) // resource or drawable
            */.build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

    private ChildEventListener locationEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d(TAG, "onChildAdded: key" + dataSnapshot.getKey());
            LocationModel locationModel = dataSnapshot.getValue(LocationModel.class);
            Log.d(TAG, "onChildAdded: model: " + locationModel);
            locationModelHashMap.put(locationModel.getId(), locationModel);
            addMarker(locationModel);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildChanged: key: " + dataSnapshot.getKey());
            LocationModel locationModel = dataSnapshot.getValue(LocationModel.class);
            Log.d(TAG, "onChildChanged: model: " + locationModel);
            addMarker(locationModel);
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

    }

    private void moveCamera(Location location) {
        if (!isCameraMoved) {
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
            isCameraMoved = true;
        }
    }

    private void addMarker(LocationModel locationModel) {
        String id = locationModel.getId();
        Log.d(TAG, "addMarker: " + locationModel);
        LatLng latLng = new LatLng(locationModel.getLat(), locationModel.getLng());
        String title = id;
        MarkerOptions options = new MarkerOptions()
                .position(latLng).title(title);
        final Marker newMarker;


        if (mMarkers.containsKey(id)) {
            Log.d(TAG, "addMarker: contains: " + id);
            Marker oldMarker = mMarkers.get(id);
            oldMarker.remove();
            newMarker = mMap.addMarker(options);
            mMarkers.put(id, newMarker);
        } else {
            Log.d(TAG, "addMarker: else");
            newMarker = mMap.addMarker(options);
            mMarkers.put(id, newMarker);
        }

        imageLoader.loadImage(locationModel.getIcon(), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
            {
                // Do whatever you want with Bitmap
                Log.d(TAG, "onLoadingComplete: imageUri: " + imageUri);
                BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(loadedImage);
                //newMarker.setIcon(descriptor);
            }
        });

        Log.d(TAG, "addMarker: uid == id" + uid + "_" + id);
        if (uid.equals(id)) {
            Location location = new Location("");//provider name is unecessary
            location.setLatitude(locationModel.getLat());//your coords of course
            location.setLongitude(locationModel.getLng());
            moveCamera(location);
        } else {
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLocationListenerAttached) {
            Log.d(TAG, "onResume: ");
            locationReference.addChildEventListener(locationEventListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isLocationListenerAttached) {
            locationReference.removeEventListener(locationEventListener);
        }
        isLocationListenerAttached = false;
        Log.d(TAG, "onPause: isLocatioonListenerAttached" + isLocationListenerAttached);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        locationReference.addChildEventListener(locationEventListener);
        isLocationListenerAttached = true;
    }
}
