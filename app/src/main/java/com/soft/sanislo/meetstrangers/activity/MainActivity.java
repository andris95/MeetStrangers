package com.soft.sanislo.meetstrangers.activity;


import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.MapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.soft.sanislo.meetstrangers.service.LocationService;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;

public class MainActivity extends BaseActivity {
    public static final String REQUEST_CHECK_SETTINGS = "REQUEST_CHECK_SETTINGS";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final long LOCATION_REQUEST_INTERVAL = 1000 * 5;

    private MapFragment mapFragment;

    private AccountHeaderBuilder headerBuilder;
    private AccountHeader accountHeader;
    private DrawerBuilder drawerBuilder;
    private Drawer drawer;

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User user;
    private String uid;

    private ValueEventListener userValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            user = dataSnapshot.getValue(User.class);
            if (user != null) {
                initDrawer(user.getAvatarURL());
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMapFragment();
        startService(new Intent(this, LocationService.class));

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();
        database.child(Constants.F_USERS).child(uid).addValueEventListener(userValueEventListener);
    }

    private void initMapFragment() {
        mapFragment = new com.soft.sanislo.meetstrangers.fragment.MapFragment();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fl_map_container, mapFragment);
        fragmentTransaction.commit();
    }

    private void initDrawer(String avatarURL) {
        headerBuilder = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.drawer_header)
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                        intent.putExtra(ProfileActivity.KEY_UID, uid);
                        startActivity(intent);
                        return true;
                    }

                    @Override
                    public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                        return false;
                    }
                });
        if (initProfileDrawerItem(user) != null) {
            headerBuilder.addProfiles(initProfileDrawerItem(user));
        }
        accountHeader = headerBuilder.build();

        PrimaryDrawerItem primaryItemMap = new PrimaryDrawerItem()
                .withName(getString(R.string.map));
        PrimaryDrawerItem primaryItemMessages = new PrimaryDrawerItem()
                .withName(getString(R.string.messages));
        PrimaryDrawerItem primaryItemFriends = new PrimaryDrawerItem()
                .withName(getString(R.string.friends));
        PrimaryDrawerItem primaryItemStrangers = new PrimaryDrawerItem()
                .withName(getString(R.string.strangers));
        SecondaryDrawerItem itemSignOut = new SecondaryDrawerItem()
                .withName(getString(R.string.btn_sign_out));
        drawerBuilder = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        primaryItemMap,
                        primaryItemMessages,
                        primaryItemFriends,
                        primaryItemStrangers,
                        new DividerDrawerItem(),
                        itemSignOut
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        return onDrawerItemClick(view, position, drawerItem);
                    }
                });
        drawer = drawerBuilder.build();
    }

    private boolean onDrawerItemClick(View view, int position, IDrawerItem drawerItem) {
        Log.d(TAG, "onItemClick: position clicked " + position);
        switch (position) {
            case 6:
                signOut();
                break;
        }
        return true;
    }

    private ProfileDrawerItem initProfileDrawerItem(User user) {
        if (user == null) {
            return null;
        }
        ProfileDrawerItem profileDrawerItem = new ProfileDrawerItem()
                .withName(user.getFullName());
        if (!TextUtils.isEmpty(user.getAvatarURL())) {
            profileDrawerItem.withIcon(user.getAvatarURL());
        }
        return profileDrawerItem;
    }

    private void signOut() {
        firebaseAuth.signOut();
    }
}
