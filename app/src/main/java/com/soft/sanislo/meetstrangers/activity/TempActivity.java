package com.soft.sanislo.meetstrangers.activity;


import android.app.Fragment;
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
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.ImageHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.soft.sanislo.meetstrangers.fragment.NewsFragment;
import com.soft.sanislo.meetstrangers.service.LocationService;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.test.TestActivity;
import com.soft.sanislo.meetstrangers.test.TestTwoActivity;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import java.util.ArrayList;
import java.util.List;

public class TempActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private MapFragment mapFragment;

    private AccountHeaderBuilder mHeaderBuilder;
    private AccountHeader mAccountHeader;
    private DrawerBuilder drawerBuilder;
    private Drawer mDrawer;

    private DatabaseReference database = Utils.getDatabase().getReference();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User user;
    private String uid;

    private ValueEventListener userValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            user = dataSnapshot.getValue(User.class);
            if (user != null) {
                initDrawer();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private List<IDrawerItem> mDrawerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initNewsFragment();
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
        fragmentTransaction.add(R.id.fl_fragment_container, mapFragment);
        fragmentTransaction.commit();
    }

    private void getFragment(String TAG) {
        Fragment fragment;
        switch (TAG) {
            case "NewsFragment":
                fragment = NewsFragment.newInstance();
                break;
            case "":
        }
    }

    private void initNewsFragment() {
        Fragment fragment = NewsFragment.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fl_fragment_container, fragment);
        ft.commit();
    }

    private void initDrawerItems() {
        PrimaryDrawerItem primaryItemMap = new PrimaryDrawerItem()
                .withName(getString(R.string.map));
        PrimaryDrawerItem primaryItemMessages = new PrimaryDrawerItem()
                .withName(getString(R.string.messages));
        PrimaryDrawerItem primaryItemFriends = new PrimaryDrawerItem()
                .withName("Users");
        PrimaryDrawerItem primaryItemGroups = new PrimaryDrawerItem()
                .withName("Groups");
        SecondaryDrawerItem itemSignOut = new SecondaryDrawerItem()
                .withName(getString(R.string.btn_sign_out));
        SecondaryDrawerItem testItem = new SecondaryDrawerItem()
                .withName("TestActivity");
        SecondaryDrawerItem testTwoItem = new SecondaryDrawerItem()
                .withName("TestTwoActivity");
        mDrawerItems = new ArrayList<>();
        mDrawerItems.add(primaryItemMap);
        mDrawerItems.add(primaryItemFriends);
        mDrawerItems.add(primaryItemGroups);
        mDrawerItems.add(primaryItemMessages);
        mDrawerItems.add(new DividerDrawerItem());
        mDrawerItems.add(itemSignOut);
    }

    private void initDrawer() {
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

        if (!TextUtils.isEmpty(user.getAvatarBlurURL())) {
            ImageHolder imageHolder = new ImageHolder(user.getAvatarBlurURL());
            mHeaderBuilder.withHeaderBackground(imageHolder);
        } else {
            mHeaderBuilder.withHeaderBackground(R.drawable.drawer_header);
        }
        if (initProfileDrawerItem(user) != null) {
            mHeaderBuilder.addProfiles(initProfileDrawerItem(user));
        }
        mAccountHeader = mHeaderBuilder.build();

        initDrawerItems();
        drawerBuilder = new DrawerBuilder()
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
        mDrawer = drawerBuilder.build();
        mDrawer.openDrawer();
        Log.d(TAG, "initDrawer: " + mDrawer.isDrawerOpen());
    }

    private boolean onDrawerItemClick(View view, int position, IDrawerItem drawerItem) {
        Log.d(TAG, "onItemClick: position clicked " + position);
        switch (position) {
            case 2:
                startChoosenActivity(ChatHeaderActivity.class);
                mDrawer.closeDrawer();
                return true;
            case 3:
                startChoosenActivity(RelationshipsActivity.class);
                mDrawer.closeDrawer();
                return true;
            case 4:
                startChoosenActivity(GroupsActivity.class);
                return true;
            case 5:
                signOut();
                mDrawer.closeDrawer();
                return true;
            case 6:
                startChoosenActivity(TestActivity.class);
                mDrawer.closeDrawer();
                return true;
            case 7:
                startChoosenActivity(TestTwoActivity.class);
                mDrawer.closeDrawer();
                return true;
            default:
                mDrawer.closeDrawer();
                return false;
        }
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
