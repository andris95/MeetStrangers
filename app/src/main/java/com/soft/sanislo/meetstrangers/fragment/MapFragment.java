package com.soft.sanislo.meetstrangers.fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.soft.sanislo.meetstrangers.activity.MainActivity;
import com.soft.sanislo.meetstrangers.activity.ProfileActivity;
import com.soft.sanislo.meetstrangers.activity.ProfileYourselfActivity;
import com.soft.sanislo.meetstrangers.model.LocationSnapshot;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import java.util.HashMap;

public class MapFragment extends com.google.android.gms.maps.MapFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int IMAGE_SIZE = 96;

    private GoogleMap mMap;
    private boolean isCameraMoved;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference locationReference = databaseReference.child("locations");
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String uid;
    private HashMap<String, LocationSnapshot> locationModelMap = new HashMap<>();
    private HashMap<String, Marker> mMarkers = new HashMap<>();

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            /*.showImageOnLoading(R.drawable.ic_stub) // resource or drawable
            .showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
            .showImageOnFail(R.drawable.ic_error) // resource or drawable
            */.build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingProgressListener progressListener;

    private ChildEventListener locationEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            LocationSnapshot locationSnapshot = dataSnapshot.getValue(LocationSnapshot.class);
            locationModelMap.put(locationSnapshot.getId(), locationSnapshot);
            addMarker(locationSnapshot);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildChanged: key: " + dataSnapshot.getKey());
            LocationSnapshot locationSnapshot = dataSnapshot.getValue(LocationSnapshot.class);
            locationModelMap.put(locationSnapshot.getId(), locationSnapshot);
            addMarker(locationSnapshot);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            LocationSnapshot locationSnapshot = dataSnapshot.getValue(LocationSnapshot.class);
            removeMarker(locationSnapshot);
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
        progressListener = new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                Log.d(TAG, "onProgressUpdate: current: " + current + " total: " + total);
            }
        };
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

    private void moveCameraToMe(LocationSnapshot locationSnapshot) {
        if (uid.equals(locationSnapshot.getId())) {
            Location location = new Location("");//provider name is unecessary
            location.setLatitude(locationSnapshot.getLat());//your coords of course
            location.setLongitude(locationSnapshot.getLng());
            moveCamera(location);
        }
    }

    private void addMarker(final LocationSnapshot locationSnapshot) {
        String id = locationSnapshot.getId();
        String title = locationSnapshot.getId();
        LatLng latLng = new LatLng(locationSnapshot.getLat(), locationSnapshot.getLng());
        MarkerOptions options = new MarkerOptions()
                .position(latLng).title(title);
        final Marker marker;
        if (mMarkers.containsKey(id)) {
            marker = mMarkers.get(id);
            marker.setPosition(latLng);
        } else {
            marker = mMap.addMarker(options);
        }
        mMarkers.put(id, marker);

        loadImageOnMarker(marker, locationSnapshot);
        moveCameraToMe(locationSnapshot);
    }

    private void loadImageOnMarker(final Marker marker, final LocationSnapshot locationSnapshot) {
        SimpleImageLoadingListener imageLoadingListener = new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
            {
                // Do whatever you want with Bitmap
                if (loadedImage != null) {
                    loadedImage = Utils.getCroppedBitmap(loadedImage);
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(loadedImage));
                    mMarkers.put(locationSnapshot.getId(), marker);
                }
            }
        };
        ImageSize imageSize = new ImageSize(IMAGE_SIZE, IMAGE_SIZE);
        imageLoader.loadImage(locationSnapshot.getIcon(), imageSize, displayImageOptions, imageLoadingListener, progressListener);

    }

    private void removeMarker(LocationSnapshot locationSnapshot) {
        if (locationModelMap.containsKey(locationSnapshot.getId())) {
            locationModelMap.remove(locationSnapshot.getId());
        }
        if (mMarkers.containsKey(locationSnapshot.getId())) {
            Marker markerToRemove = mMarkers.get(locationSnapshot.getId());
            markerToRemove.remove();
            mMarkers.remove(locationSnapshot.getId());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        locationReference.addChildEventListener(locationEventListener);
        if (mMarkers.containsKey(uid)) {
            Marker marker = mMarkers.get(uid);
            LatLng latLng = marker.getPosition();
            Location location = new Location("");
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);
            isCameraMoved = false;
            moveCamera(location);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationReference.removeEventListener(locationEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: ");
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        locationReference.addChildEventListener(locationEventListener);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClick: marker id " + marker.getTitle());
        Intent intent;
        if (marker.getTitle().equals(uid)) {
            intent = new Intent(getActivity(), ProfileYourselfActivity.class);
        } else {
            intent = new Intent(getActivity(), ProfileActivity.class);
            intent.putExtra(ProfileActivity.KEY_UID, marker.getTitle());
        }
        startActivity(intent);
        return true;
    }
}
