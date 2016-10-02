package com.soft.sanislo.meetstrangers.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.soft.sanislo.meetstrangers.model.LocationSnapshot;
import com.soft.sanislo.meetstrangers.service.FetchAddressIntentService;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.LocationUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by root on 08.09.16.
 */
public class ProfileActivity extends BaseActivity {
    public static final String KEY_UID = "KEY_UID";
    private static final String TAG = ProfileActivity.class.getSimpleName();

    private CollapsingToolbarLayout collapsingoToolbar;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.ivAvatar) ImageView ivAvatar;
    @BindView(R.id.tvProfileLastActive) TextView tvLastActive;
    @BindView(R.id.tvProfileLocation) TextView tvAddress;
    @BindView(R.id.pbProfileAvatar) ProgressBar pbAvatar;
    @BindView(R.id.fab_profile)
    FloatingActionButton fabProfile;

    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User mAuthenticatedUser;
    private String mAuthenticatedUserUID;

    private User mDisplayedUser;
    private String mDisplayedUserUID;
    private String mAvatarURL;

    private ResultReceiver mResultReceiver;
    private boolean isAddressRequested;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            /*.showImageOnLoading(R.drawable.ic_stub) // resource or drawable
            .showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
            .showImageOnFail(R.drawable.ic_error) // resource or drawable
            */.build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingProgressListener progressListener;

    /** ValueEventListener for current logged in mDisplayedUser*/
    private ValueEventListener mAuthenticatedUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mAuthenticatedUser = dataSnapshot.getValue(User.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    /** ValueEventListener for displayed mDisplayedUser*/
    private ValueEventListener mDisplayedUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mDisplayedUser = dataSnapshot.getValue(User.class);
            if (mDisplayedUser != null) {
                mAvatarURL = mDisplayedUser.getAvatarURL();
                collapsingoToolbar.setTitle(mDisplayedUser.getFullName());
                Log.d(TAG, "onDataChange: mAvatarURL " + mDisplayedUser.getAvatarURL() + " " + mDisplayedUser.getFullName());
                mGoogleApiClient.connect();
                imageLoader.displayImage(mAvatarURL, ivAvatar, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        pbAvatar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        pbAvatar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        pbAvatar.setVisibility(View.GONE);
                    }
                });

            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    private ValueEventListener locationListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            LocationSnapshot locationSnapshot = dataSnapshot.getValue(LocationSnapshot.class);
            if (locationSnapshot != null && !isAddressRequested) {
                Intent intent = new Intent(getApplicationContext(), FetchAddressIntentService.class);
                intent.putExtra(FetchAddressIntentService.RECEIVER, mResultReceiver);
                intent.putExtra(FetchAddressIntentService.LOCATION_DATA_EXTRA,
                        LocationUtils.getLocation(locationSnapshot));
                startService(intent);
                isAddressRequested = true;
                tvLastActive.setText(Utils.getLastOnline(locationSnapshot));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private GoogleApiClient.ConnectionCallbacks mConnectionCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.d(TAG, "onConnected: ");
            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                    .getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                    Log.d(TAG, "onResult: likelyPlace size: " + likelyPlaces.toString());
                    Status status = likelyPlaces.getStatus();
                    int statusCode = status.getStatusCode();
                    Log.d(TAG, "onResult: statusCOde " + statusCode);
                    switch (statusCode) {

                    }
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                        tvAddress.setText(placeLikelihood.getPlace().getName());
                    }
                    likelyPlaces.release();
                }
            });
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    private GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = firebaseUser.getUid();
        mDisplayedUserUID = getIntent().getStringExtra(KEY_UID);

        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        collapsingoToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        setSupportActionBar(toolbar);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(mConnectionCallback)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .build();

        mResultReceiver = new AddressResultReceiver(new Handler());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @OnClick(R.id.fab_profile)
    public void onClickFabProfile() {
        String[] profileOptions = getResources().getStringArray(R.array.profile_stranger_options);
        ArrayList<String> options = new ArrayList<>(Arrays.asList(profileOptions));

        new MaterialDialog.Builder(this)
                .items(options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Log.d(TAG, "onSelection: which: " + which);
                        switch (which) {
                            case 0:
                                launchChatActivity();
                                break;
                            default:
                                break;
                        }
                    }
                }).show();
    }

    private void launchChatActivity() {
        Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.KEY_CHAT_PARTER_UID, mDisplayedUserUID);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDisplayedUserUID != null) {
            database.child(Constants.F_USERS).child(mAuthenticatedUserUID)
                    .addValueEventListener(mAuthenticatedUserListener);
            database.child(Constants.F_USERS).child(mDisplayedUserUID)
                    .addValueEventListener(mDisplayedUserListener);
            database.child(Constants.F_LOCATIONS).child(mDisplayedUserUID)
                    .addValueEventListener(locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
        database.child(Constants.F_USERS).child(mAuthenticatedUserUID)
                .removeEventListener(mAuthenticatedUserListener);
        database.child(Constants.F_USERS).child(mDisplayedUserUID)
                .removeEventListener(mDisplayedUserListener);
        database.child(Constants.F_LOCATIONS).child(mDisplayedUserUID)
                .removeEventListener(locationListener);
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(final int resultCode, final Bundle resultData) {
            if (resultCode == FetchAddressIntentService.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(FetchAddressIntentService.RESULT_ADDRESS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "run: address: " + address.toString());
                        String featureName = address.getFeatureName();
                        tvAddress.setText(featureName);
                    }
                });
            } else if (resultCode == FetchAddressIntentService.FAILURE_RESULT) {
                String errorMessage = resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY);
                tvAddress.setText(errorMessage);
            }
        }
    }
}
