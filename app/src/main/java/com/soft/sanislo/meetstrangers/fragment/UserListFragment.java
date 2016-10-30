package com.soft.sanislo.meetstrangers.fragment;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.activity.ProfileActivity;
import com.soft.sanislo.meetstrangers.activity.ProfileYourselfActivity;
import com.soft.sanislo.meetstrangers.adapter.UserAdapter;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.UserViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 10.10.16.
 */
public class UserListFragment extends android.support.v4.app.Fragment {
    private static final String TAG = UserListFragment.class.getSimpleName();
    public static final String KEY_RELATIONSHIP_STATUS = "KEY_RELATIONSHIP_STATUS";
    public static final int USERS_ALL = 22800;
    public static final int USERS_FRIENDS = 22801;
    public static final int USERS_INCOMING_REQUESTS = 22802;
    public static final int USERS_OUTCOMING_REQUESTS = 22803;
    public static final int USERS_BLACK_LIST = 22804;

    @BindView(R.id.rv_user_list)
    RecyclerView rvUserList;

    private View mRootView;

    private DatabaseReference database = Utils.getDatabase().getReference();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User mAuthenticatedUser;
    private String mAuthenticatedUserUID;
    private DatabaseReference mUserListRef;

    private int mRelationshipStatus = USERS_ALL;
    private UserAdapter mUserAdapter;

    /** ValueEventListener for current logged in mAuthenticatedUser*/
    private ValueEventListener mAuthenticatedUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mAuthenticatedUser = dataSnapshot.getValue(User.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_user_list, container, false);
        ButterKnife.bind(this, mRootView);
        rvUserList.setLayoutManager(new LinearLayoutManager(getActivity()));
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = firebaseUser.getUid();
        mRelationshipStatus = getArguments().getInt(KEY_RELATIONSHIP_STATUS, USERS_ALL);

        setUserListRef();
        mUserAdapter = new UserAdapter(Boolean.class,
                R.layout.item_user,
                UserViewHolder.class,
                mUserListRef);
        mUserAdapter.setContext(getActivity());
        mUserAdapter.setOnClickListener(new UserAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int position, User user) {
                launchProfileActivity(view, user.getUid());
            }
        });
    }

    private void launchProfileActivity(View view, String uid) {
        if (uid.equals(mAuthenticatedUserUID)) {
            startActivity(new Intent(getActivity(), ProfileYourselfActivity.class));
        } else {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            intent.putExtra(ProfileActivity.KEY_UID, uid);
            View sharedView = view.findViewById(R.id.iv_user_avatar);
            Pair<View, String> sharedViews = new Pair<>(sharedView, getString(R.string.transition_user_avatar));
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                /**Bundle sharedBundle = ActivityOptions.makeSceneTransitionAnimation(getActivity(), sharedViews)
                        .toBundle();
                startActivity(intent, sharedBundle);*/
                startActivity(intent);
            } else {
                startActivity(intent);
            }
        }
    }

    public static UserListFragment newInstance(int relationshipStatus) {
        Bundle args = new Bundle();
        args.putInt(KEY_RELATIONSHIP_STATUS, relationshipStatus);
        UserListFragment fragment = new UserListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        database.child(Constants.F_USERS).child(mAuthenticatedUserUID)
                .addValueEventListener(mAuthenticatedUserListener);
        rvUserList.setAdapter(mUserAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        database.child(Constants.F_USERS).child(mAuthenticatedUserUID)
                .removeEventListener(mAuthenticatedUserListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUserAdapter.cleanup();
        mUserAdapter = null;
    }

    private void setUserListRef() {
        switch (mRelationshipStatus) {
            case USERS_ALL:
                mUserListRef = database.child(Constants.F_USERS_ALL);
                break;
            case USERS_FRIENDS:
                mUserListRef = database.child(Constants.F_USERS_FRIENDS)
                    .child(mAuthenticatedUserUID);
                break;
            case USERS_INCOMING_REQUESTS:
                mUserListRef = database.child(Constants.F_USERS_FOLLOWERS)
                        .child(mAuthenticatedUserUID);
                break;
            case USERS_OUTCOMING_REQUESTS:
                mUserListRef = database.child(Constants.F_USERS_FOLLOWING)
                        .child(mAuthenticatedUserUID);
                break;
            default:
                mUserListRef = database.child(Constants.F_USERS_ALL);
                break;
        }
    }
}
