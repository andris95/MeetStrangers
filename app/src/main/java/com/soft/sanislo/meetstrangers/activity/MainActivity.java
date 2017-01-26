package com.soft.sanislo.meetstrangers.activity;


import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.interfaces.OnCheckedChangeListener;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;
import com.mikepenz.materialdrawer.model.ToggleDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.soft.sanislo.meetstrangers.fragment.MapFragment;
import com.soft.sanislo.meetstrangers.fragment.UserProfileBottomSheet;
import com.soft.sanislo.meetstrangers.service.LocationService;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.FirebaseUtils;
import com.soft.sanislo.meetstrangers.utilities.PreferencesManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements MapFragment.MarkerClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_PERM_LOCATION = 300;
    private static final int RC_LOCATION_SETTINGS = 400;
    private static final int ID_LOCATION = 111;

    private AccountHeaderBuilder mHeaderBuilder;
    private AccountHeader mAccountHeader;
    private DrawerBuilder mDrawerBuilder;
    private ProfileDrawerItem mProfileDrawerItem;
    private Drawer mDrawer;
    private List<IDrawerItem> mDrawerItems;

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User mUser;
    private String mUID;

    private GoogleApiClient mGoogleApiClient;

    private ValueEventListener mUserValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mUser = dataSnapshot.getValue(User.class);
            Log.d(TAG, "onDataChange: mUser: " + mUser);
            initDrawer();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(IDrawerItem drawerItem, CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (hasPermission()) {
                    toggleLocationSharing(true);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION},
                            RC_PERM_LOCATION);
                }
            } else {
                toggleLocationSharing(false);
            }
        }
    };

    private void toggleLocationSharing(boolean isLocationShared) {
        if (isLocationShared) {
            Log.d(TAG, "toggleLocationSharing: ");
            initGoogleApiClient();
            connectGoogleApiClient();
        } else {
            stopLocationService();
        }
        changeLocationItemIcon(isLocationShared);
        PreferencesManager.setLocationShared(MainActivity.this, isLocationShared);
    }

    private void initGoogleApiClient() {
        if (mGoogleApiClient != null) return;
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        checkLocationSettings();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
    }

    private void connectGoogleApiClient() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    private void  checkLocationSettings() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                builder.build());
        Log.d(TAG, "checkLocationSettings: ");
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                Log.d(TAG, "onResult: locationResult" + locationSettingsResult.toString());
                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        Log.d(TAG, "onResult: SUCCESS");
                        startLocationService();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        Log.d(TAG, "onResult: RESOLUTION");
                        showLocationResolution(status);
                        break;
                }
            }
        });
    }

    private void showLocationResolution(Status status) {
        try {
            // Show the dialog by calling startResolutionForResult(),
            // and check the result in onActivityResult().
            Log.d(TAG, "showLocationResolution: ");
            status.startResolutionForResult(
                    MainActivity.this,
                    RC_LOCATION_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            // Ignore the error.
        }
    }

    private void changeLocationItemIcon(boolean isChecked) {
        GoogleMaterial.Icon icon = isChecked ?
                GoogleMaterial.Icon.gmd_location_on
                : GoogleMaterial.Icon.gmd_location_off;
        SwitchDrawerItem switchDrawerItem = (SwitchDrawerItem) mDrawer.getDrawerItem(ID_LOCATION);
        switchDrawerItem.withIcon(icon)
                .withIconColorRes(R.color.md_black_1000);
        mDrawer.updateItem(switchDrawerItem);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addMapFragment();
        initFirebase();
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mUID = firebaseUser.getUid();
        mDatabaseReference = FirebaseUtils.getDatabaseReference();
    }

    private void addMapFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        MapFragment mapFragment = MapFragment.newInstance();
        ft.replace(R.id.fl_fragment_container, mapFragment);
        ft.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDatabaseReference.child(Constants.F_USERS)
                .child(mUID)
                .addValueEventListener(mUserValueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatabaseReference.child(Constants.F_USERS)
                .child(mUID)
                .removeEventListener(mUserValueEventListener);
    }

    private void initDrawer() {
        initDrawerHeader();
        initDrawerItems();
        if (mDrawer == null) {
            mDrawerBuilder = new DrawerBuilder()
                    .withActivity(this)
                    .withAccountHeader(mAccountHeader)
                    .withDrawerItems(mDrawerItems)
                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                        @Override
                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                            return onDrawerItemClick(view, position, drawerItem);
                        }
                    })
                    .withGenerateMiniDrawer(true)
                    .withShowDrawerOnFirstLaunch(true);
            mDrawer = mDrawerBuilder.build();
        } else {
            mDrawer.setHeader(mAccountHeader.getView());
            mDrawer.setItems(mDrawerItems);
        }
        //mDrawer.openDrawer();
    }

    private void initDrawerHeader() {
        mHeaderBuilder = new AccountHeaderBuilder()
                .withActivity(this)
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        Intent intent = new Intent(getApplicationContext(), ProfileYourselfActivity.class);
                        startActivity(intent);
                        return true;
                    }

                    @Override
                    public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                        return false;
                    }
                });
        if (!TextUtils.isEmpty(mUser.getAvatarBlurURL())) {
            ImageHolder imageHolder = new ImageHolder(mUser.getAvatarBlurURL());
            mHeaderBuilder.withHeaderBackground(imageHolder);
        } else {
            mHeaderBuilder.withHeaderBackground(R.drawable.drawer_header);
        }
        initProfileDrawerItem();
        mHeaderBuilder.addProfiles(mProfileDrawerItem);
        mAccountHeader = mHeaderBuilder.build();
    }

    private void initProfileDrawerItem() {
        mProfileDrawerItem = new ProfileDrawerItem()
                .withName(mUser.getFullName());
        if (!TextUtils.isEmpty(mUser.getAvatarURL())) {
            mProfileDrawerItem.withIcon(mUser.getAvatarURL());
        }
    }

    private void initDrawerItems() {
        PrimaryDrawerItem primaryItemMap = new PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_add_location)
                .withIconColorRes(R.color.md_black_1000)
                .withName(getString(R.string.map))
                .withSetSelected(true);
        PrimaryDrawerItem primaryItemMessages = new PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_message)
                .withIconColorRes(R.color.md_black_1000)
                .withName(getString(R.string.messages));
        PrimaryDrawerItem primaryItemFriends = new PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_people)
                .withIconColorRes(R.color.md_black_1000)
                .withName(getString(R.string.users));
        PrimaryDrawerItem primaryItemGroups = new PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_group)
                .withIconColorRes(R.color.md_black_1000)
                .withName(getString(R.string.groups));
        PrimaryDrawerItem itemSignOut = new PrimaryDrawerItem()
                .withIcon(FontAwesome.Icon.faw_sign_out)
                .withIconColorRes(R.color.md_black_1000)
                .withName(getString(R.string.btn_sign_out));
        mDrawerItems = new ArrayList<>();
        mDrawerItems.add(primaryItemMap);
        mDrawerItems.add(primaryItemFriends);
        mDrawerItems.add(primaryItemGroups);
        mDrawerItems.add(primaryItemMessages);
        mDrawerItems.add(new DividerDrawerItem());
        mDrawerItems.add(getLocationDrawerItem());
        mDrawerItems.add(new DividerDrawerItem());
        mDrawerItems.add(itemSignOut);
    }

    private IDrawerItem getLocationDrawerItem() {
        boolean isLocationShared = PreferencesManager.isLocationShared(MainActivity.this);
        GoogleMaterial.Icon icon = isLocationShared ? GoogleMaterial.Icon.gmd_location_on
                : GoogleMaterial.Icon.gmd_location_off;
        SwitchDrawerItem locationToggle = new SwitchDrawerItem()
                .withIdentifier(ID_LOCATION)
                .withIcon(icon)
                .withIconColorRes(R.color.md_black_1000)
                .withName(getString(R.string.share_location))
                .withChecked(isLocationShared)
                .withOnCheckedChangeListener(mOnCheckedChangeListener);
        return locationToggle;
    }

    private boolean onDrawerItemClick(View view, int position, IDrawerItem drawerItem) {
        Log.d(TAG, "onItemClick: position clicked " + position);
        /** ! indexes start from 1, not from 0!!! */
        switch (position) {
            case 1:
                mDrawer.closeDrawer();
                return true;
            case 2:
                mDrawer.closeDrawer();
                startChoosenActivity(RelationshipsActivity.class);
                return true;
            case 3:
                startChoosenActivity(GroupsActivity.class);
                return true;
            case 4:
                mDrawer.closeDrawer();
                startChoosenActivity(ChatHeaderActivity.class);
                return true;
            case 8:
                mDrawer.closeDrawer();
                logout();
                return true;
            default:
                return false;
        }
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERM_LOCATION) {
            if (grantResults.length == 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                toggleLocationSharing(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_LOCATION_SETTINGS) {
            if (resultCode == RESULT_OK) {
                startLocationService();
            } else {
                makeToast(":(");
                toggleLocationSharing(false);
            }
        }
    }

    private void startLocationService() {
        Intent intent = new Intent(MainActivity.this, LocationService.class);
        startService(intent);
    }

    private void openUserSheetPanel(String uid) {
        UserProfileBottomSheet userProfileBottomSheet = UserProfileBottomSheet.newInstance(uid);
        userProfileBottomSheet.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onMarkerClick(String uid) {
        openUserSheetPanel(uid);
    }
}
