package com.soft.sanislo.meetstrangers.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.soft.sanislo.meetstrangers.adapter.PostAdapter;
import com.soft.sanislo.meetstrangers.adapter.TransitionListenerAdapter;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.LocationSnapshot;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.Relationship;
import com.soft.sanislo.meetstrangers.service.FetchAddressIntentService;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.LocationUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.PostViewHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

    @BindView(R.id.collapsingToolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.iv_avatar)
    ImageView ivAvatar;

    @BindView(R.id.iv_avatar_shared)
    ImageView ivAvatarShared;

    @BindView(R.id.tvProfileLastActive)
    TextView tvLastActive;

    @BindView(R.id.tvProfileLocation)
    TextView tvAddress;

    @BindView(R.id.fab_profile)
    FloatingActionButton fabProfile;

    @BindView(R.id.rv_posts)
    RecyclerView rvPosts;
    @BindView(R.id.scv_profile_content)
    NestedScrollView scvProfileContent;

    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference database = Utils.getDatabase().getReference();
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    
    private User mAuthenticatedUser;
    private String mAuthenticatedUserUID;
    private User mDisplayedUser;
    private String mDisplayedUserUID;

    private Relationship mRelationship;

    private ResultReceiver mResultReceiver;
    private boolean isAddressRequested;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private PostAdapter mPostAdapter;
    private Query mPostQuery;

    private MaterialDialog mActionDialog;

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
            collapsingToolbar.setTitle(mDisplayedUser.getFullName());
            imageLoader.loadImage(mDisplayedUser.getAvatarURL(), displayImageOptions, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    imageLoader.displayImage(imageUri, ivAvatar, displayImageOptions);
                    imageLoader.displayImage(imageUri, ivAvatarShared, displayImageOptions, new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            setStatusBarColor(loadedImage);
                            supportStartPostponedEnterTransition();
                        }
                    });
                }
            });
            mGoogleApiClient.connect();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void setStatusBarColor(Bitmap loadedImage) {
        Palette.from(loadedImage)
                .setRegion(0, 0,
                        loadedImage.getWidth(),
                        64)
                .generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        int defColor = palette.getVibrantColor(getResources().getColor(R.color.primary_dark));
                        int color = defColor;
                        if (palette.getVibrantSwatch() != null) {
                            color = palette.getVibrantColor(defColor);
                        }
                        if (palette.getLightVibrantSwatch() != null) {
                            color = palette.getLightVibrantColor(defColor);
                        }
                        if (palette.getDominantSwatch() != null) {
                            color = palette.getDominantColor(defColor);
                        }
                        getWindow().setStatusBarColor(color);
                    }
                });
    }

    private ValueEventListener mRelationshipListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mRelationship = dataSnapshot.getValue(Relationship.class);
            if (mRelationship != null) {
                Log.d(TAG, "onDataChange: " + mRelationship);
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
            makeToast(databaseError.getMessage());
            databaseError.toException().printStackTrace();
        }
    };

    private PostAdapter.OnClickListener mPostClickListener = new PostAdapter.OnClickListener() {
        @Override
        public void onClick(View view, int position, Post post) {
            if (view.getTag() != null) {
                int tag = (int) view.getTag();
                makeToast("clicked photo position: " + tag + ", url: " +
                        post.getPhotoURLList().get(tag));
                return;
            }
            switch (view.getId()) {
                case R.id.iv_post_author_avatar:
                    makeToast("clicked " + post.getAuthFullName() + "'s photo");
                    break;
                case R.id.iv_post_options:
                    makeToast("options");
                    break;
                case R.id.iv_like_post:
                    onClickLikePost(post.getKey());
                    break;
                case R.id.iv_comment_post:
                    onClickCommentPost(position);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onClickAddComment(Post post, String commentText) {
            DatabaseReference newCommentRef = database.child(Constants.F_POSTS_COMMENTS)
                    .child(post.getAuthorUID())
                    .child(post.getKey());
            String newCommentKey = newCommentRef.push().getKey();
            Comment comment = new Comment(newCommentKey,
                    post.getKey(),
                    mAuthenticatedUserUID,
                    mAuthenticatedUser.getFullName(),
                    mAuthenticatedUser.getAvatarURL(),
                    commentText,
                    new Date().getTime());
            newCommentRef.child(newCommentKey).setValue(comment)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            makeToast(e.getMessage());
                            e.printStackTrace();
                        }
                    });
        }

        @Override
        public void onClickCancelComment() {
            mPostAdapter.setCommentsVisiblePos(-1);
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                TransitionManager.beginDelayedTransition(rvPosts);
            }
            mPostAdapter.notifyDataSetChanged();
        }
    };

    private void onClickCommentPost(int position) {
        if (mPostAdapter.getCommentsVisiblePos() == position) {
            mPostAdapter.setCommentsVisiblePos(-1);
        } else {
            mPostAdapter.setCommentsVisiblePos(position);
        }
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            TransitionManager.beginDelayedTransition(rvPosts);
        }
        mPostAdapter.notifyItemChanged(position);
    }

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
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        initTransition();
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = firebaseUser.getUid();
        mDisplayedUserUID = getIntent().getStringExtra(KEY_UID);

        initPosts();
        initAddressFetcher();
    }

    private void initTransition() {
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
            requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);

            //Transition contentEnterTrans = TransitionInflater.from(this).inflateTransition(R.transition.trans_slide);
            //contentEnterTrans.addTarget(scvProfileContent);
            //getWindow().setEnterTransition(contentEnterTrans);
            Transition revealAvatarTrans = TransitionInflater.from(this).inflateTransition(R.transition.transition_reveal_avatar);
            getWindow().setEnterTransition(revealAvatarTrans);
            getWindow().getEnterTransition().addListener(new TransitionListenerAdapter() {
                @Override
                public void onTransitionStart(Transition transition) {
                    super.onTransitionStart(transition);
                    Log.d(TAG, "onTransitionStart: ");
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    super.onTransitionEnd(transition);
                    Log.d(TAG, "onTransitionEnd: ");
                    ivAvatar.setVisibility(View.VISIBLE);
                    ivAvatarShared.setVisibility(View.INVISIBLE);
                }
            });
            postponeEnterTransition();
        }
    }

    private void initPosts() {
        mPostQuery = database.child(Constants.F_POSTS)
                .child(mDisplayedUserUID)
                .orderByPriority();
        mPostAdapter = new PostAdapter(getApplicationContext(),
                Post.class,
                R.layout.item_post,
                PostViewHolder.class,
                mPostQuery);
        mPostAdapter.setAuthUserUID(mAuthenticatedUserUID);
        mPostAdapter.setOnClickListener(mPostClickListener);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setNestedScrollingEnabled(false);
        ((SimpleItemAnimator) rvPosts.getItemAnimator()).setSupportsChangeAnimations(false);
        rvPosts.setAdapter(mPostAdapter);
    }

    private void initAddressFetcher() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(mConnectionCallback)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .build();
        mResultReceiver = new AddressResultReceiver(new Handler());
    }

    private void onClickLikePost(final String postKey) {
        database.child(Constants.F_POSTS)
                .child(mDisplayedUserUID)
                .child(postKey).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                mutableData = likePost(mutableData);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    databaseError.toException().printStackTrace();
                    makeToast(databaseError.getMessage());
                }
            }
        });
    }

    private MutableData likePost(MutableData mutableData) {
        Post post = mutableData.getValue(Post.class);
        long postLikesCount = post.getLikesCount();
        HashMap<String, Boolean> likedUsersUIDs = post.getLikedUsersUIDs();
        if (likedUsersUIDs != null &&
                likedUsersUIDs.containsKey(mAuthenticatedUserUID)) {
            post.setLikesCount(postLikesCount - 1);
            likedUsersUIDs.remove(mAuthenticatedUserUID);
        } else {
            post.setLikesCount(postLikesCount + 1);
            if (likedUsersUIDs == null) {
                likedUsersUIDs = new HashMap<>();
            }
            likedUsersUIDs.put(mAuthenticatedUserUID, true);
        }
        post.setLikedUsersUIDs(likedUsersUIDs);
        mutableData.setValue(post);
        return mutableData;
    }

    @OnClick(R.id.fab_profile)
    public void onClickFabProfile() {
        String[] profileOptions = getResources().getStringArray(R.array.profile_stranger_options);
        ArrayList<String> options = new ArrayList<>(Arrays.asList(profileOptions));
        options.add(getRelationshipStatusString());

        mActionDialog = new MaterialDialog.Builder(this)
                .items(options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Log.d(TAG, "onSelection: which: " + which);
                        switch (which) {
                            case 0:
                                launchChatActivity();
                                break;
                            case 1:
                                onClickRelationshipAction();
                                break;
                            default:
                                break;
                        }
                    }
                }).show();
    }

    private void launchChatActivity() {
        Intent intent = new Intent(ProfileActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.KEY_CHAT_PARTNER_UID, mDisplayedUserUID);
        startActivity(intent);
    }

    private String getRelationshipStatusString() {
        String relationshipStatus = "UNKNOWN";
        if (mRelationship == null) {
            relationshipStatus = "Follow";
        } else {
            if (mRelationship.getStatus() == Constants.RS_FRIENDS) {
                relationshipStatus = "Delete from friends";
            } else if (mRelationship.getStatus() == Constants.RS_PENDING) {
                if (mRelationship.getLastActionUserUID().equals(mAuthenticatedUserUID)) {
                    relationshipStatus = "Unfollow";
                } else {
                    relationshipStatus = "Accept follow request";
                }
            }
        }
        return relationshipStatus;
    }

    private void onClickRelationshipAction() {
        if (mRelationship == null) {
            setRelationshipStatus(Constants.RS_PENDING);
            setFollowers(mDisplayedUserUID, mAuthenticatedUserUID, true);
        } else {
            switch (mRelationship.getStatus()) {
                /** users were friends, but now they are not*/
                case Constants.RS_FRIENDS:
                    setRelationshipStatus(Constants.RS_PENDING);
                    setFriends(mAuthenticatedUserUID, mDisplayedUserUID, false);
                    setFriends(mDisplayedUserUID, mAuthenticatedUserUID, false);
                    setFollowers(mAuthenticatedUserUID, mDisplayedUserUID, true);
                    break;

                case Constants.RS_PENDING:
                    if (mRelationship.getLastActionUserUID().equals(mAuthenticatedUserUID)) {
                        setRelationshipStatus(Constants.RS_UNKNOWN);
                        setFollowers(mDisplayedUserUID, mAuthenticatedUserUID, false);
                    } else {
                        setRelationshipStatus(Constants.RS_FRIENDS);
                        setFriends(mAuthenticatedUserUID, mDisplayedUserUID, true);
                        setFriends(mDisplayedUserUID, mAuthenticatedUserUID, true);
                        setFollowers(mAuthenticatedUserUID, mDisplayedUserUID, false);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void setFriends(String firstUserUID, String secondUserUID, boolean areFriends) {
        database.child(Constants.F_USERS_FRIENDS)
                .child(firstUserUID)
                .child(secondUserUID).setValue((areFriends) ? true : null);
    }

    /** sets*/
    private void setFollowers(String userToFollowUID, String followerUID, boolean areFollowers) {
        database.child(Constants.F_USERS_FOLLOWERS)
                .child(userToFollowUID)
                .child(followerUID).setValue((areFollowers) ? true : null);
        database.child(Constants.F_USERS_FOLLOWING)
                .child(followerUID)
                .child(userToFollowUID).setValue((areFollowers) ? true : null);
    }

    private void setRelationshipStatus(int status) {
        mRelationship = new Relationship(status,
                new Date().getTime(),
                mAuthenticatedUserUID,);
        if (status == Constants.RS_UNKNOWN) {
            mRelationship = null;
        }
        database.child(Constants.F_RELATIONSHIPS)
                .child(mAuthenticatedUserUID)
                .child(mDisplayedUserUID)
                .setValue(mRelationship);
        database.child(Constants.F_RELATIONSHIPS)
                .child(mDisplayedUserUID)
                .child(mAuthenticatedUserUID)
                .setValue(mRelationship);
        mActionDialog.dismiss();
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
            database.child(Constants.F_RELATIONSHIPS)
                    .child(mAuthenticatedUserUID)
                    .child(mDisplayedUserUID)
                    .addValueEventListener(mRelationshipListener);
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
        database.child(Constants.F_RELATIONSHIPS)
                .child(mAuthenticatedUserUID)
                .child(mDisplayedUserUID)
                .removeEventListener(mRelationshipListener);
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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        supportFinishAfterTransition();
    }

    /**
     * Schedules the shared element transition to be started immediately
     * after the shared element has been measured and laid out within the
     * activity's view hierarchy. Some common places where it might make
     * sense to call this method are:
     *
     * (1) Inside a Fragment's onCreateView() method (if the shared element
     *     lives inside a Fragment hosted by the called Activity).
     *
     * (2) Inside a Picasso Callback object (if you need to wait for Picasso to
     *     asynchronously load/scale a bitmap before the transition can begin).
     *
     * (3) Inside a LoaderCallback's onLoadFinished() method (if the shared
     *     element depends on data queried by a Loader).
     */
    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        supportStartPostponedEnterTransition();
                        return true;
                    }
                });
    }
}
