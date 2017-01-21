package com.soft.sanislo.meetstrangers.activity;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

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
import com.soft.sanislo.meetstrangers.service.LocationService;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, LocationService.class));
        initFirebase();
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mUID = firebaseUser.getUid();
        mDatabaseReference = FirebaseUtils.getDatabaseReference();
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
        mDrawer.openDrawer();
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
            case 6:
                mDrawer.closeDrawer();
                logout();
                return true;
            default:
                return false;
        }
    }
}
