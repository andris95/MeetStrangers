package com.soft.sanislo.meetstrangers.activity;

import android.annotation.TargetApi;
import android.app.Activity;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.soft.sanislo.meetstrangers.adapter.PostAdapter;
import com.soft.sanislo.meetstrangers.model.CommentModel;
import com.soft.sanislo.meetstrangers.model.LocationSnapshot;
import com.soft.sanislo.meetstrangers.service.FetchAddressIntentService;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.LocationUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.PostViewHolder;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by root on 08.09.16.
 */
public class ProfileYourselfActivity extends BaseActivity {
    public static final String KEY_UID = "KEY_UID";
    private static final String TAG = ProfileActivity.class.getSimpleName();

    @BindView(R.id.collapsingToolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.iv_avatar) ImageView ivAvatar;
    @BindView(R.id.tvProfileLastActive) TextView tvLastActive;
    @BindView(R.id.tvProfileLocation) TextView tvAddress;
    @BindView(R.id.pbProfileAvatar) ProgressBar pbAvatar;
    @BindView(R.id.btn_new_post)
    Button btnNewPost;
    @BindView(R.id.rv_posts)
    RecyclerView rvPosts;
    @BindView(R.id.fab_profile)
    FloatingActionButton fabProfile;

    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference database = Utils.getDatabase().getReference();
    private DatabaseReference mPostRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User user;
    private String uid;
    private String avatarURL;
    private PostAdapter mPostAdapter;

    private ResultReceiver mResultReceiver;
    private boolean isAddressRequested;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingProgressListener progressListener;

    private ValueEventListener userValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            user = dataSnapshot.getValue(User.class);
            if (user != null) {
                avatarURL = user.getAvatarURL();
                collapsingToolbar.setTitle(user.getFullName());
                Log.d(TAG, "onDataChange: avatarURL " + user.getAvatarURL() + " " + user.getFullName());
                mGoogleApiClient.connect();
                imageLoader.displayImage(avatarURL, ivAvatar, displayImageOptions, new ImageLoadingListener() {
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

    private PostAdapter.OnClickListener mPostClickListener = new PostAdapter.OnClickListener() {
        @Override
        public void onClick(View view, int position, Post post) {
            if (view.getTag() != null) {
                int tag = (int) view.getTag();
                Log.d(TAG, "onClick: tag: " + tag);
                return;
            }
            switch (view.getId()) {
                case R.id.iv_post_author_avatar:
                    Toast.makeText(getApplicationContext(), "pos: " + position + ", iv_post_author_avatar", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.tv_post_text:
                    Toast.makeText(getApplicationContext(), "pos: " + position + ", tv_post_text", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.iv_post_options:
                    onClickPostOptions(post);
                case R.id.iv_like_post:
                    makeToast("like");
                    //onClickLikePost(post.getKey());
                    break;
                case R.id.iv_comment_post:
                    onClickCommentPost(position);
                    break;
                default:
                    break;
            }
            try {
                Log.d(TAG, "onClick: " + view.getTag());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClickAddComment(Post post, String commentText) {
            DatabaseReference newCommentRef = database.child(Constants.F_POSTS_COMMENTS)
                    .child(post.getAuthorUID())
                    .child(post.getKey());
            String newCommentKey = newCommentRef.push().getKey();
            CommentModel commentModel = new CommentModel(newCommentKey,
                    post.getKey(),
                    user.getUid(),
                    user.getFullName(),
                    user.getAvatarURL(),
                    commentText,
                    new Date().getTime());
            newCommentRef.child(newCommentKey).setValue(commentModel)
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
            TransitionManager.beginDelayedTransition(rvPosts);
            mPostAdapter.notifyDataSetChanged();
        }

        @Override
        public void onClickLikeComment(CommentModel commentModel) {

        }

        @Override
        public void onClickHighlightComment() {
            TransitionManager.beginDelayedTransition(rvPosts);
        }
    };

    private void onClickCommentPost(int position) {
        if (mPostAdapter.getCommentsVisiblePos() == position) {
            mPostAdapter.setCommentsVisiblePos(-1);
        } else {
            mPostAdapter.setCommentsVisiblePos(position);
        }
        TransitionManager.beginDelayedTransition(rvPosts);
        mPostAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        setContentView(R.layout.activity_profile_yourself);
        themeNavAndStatusBar(this);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        initPosts();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(mConnectionCallback)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .build();

        mResultReceiver = new AddressResultReceiver(new Handler());
    }

    private void initPosts() {
        mPostRef = database.child(Constants.F_POSTS).child(uid);
        mPostAdapter = new PostAdapter(getApplicationContext(),
                Post.class,
                R.layout.item_post,
                PostViewHolder.class,
                mPostRef);
        mPostAdapter.setOnClickListener(mPostClickListener);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));
        rvPosts.setNestedScrollingEnabled(false);
        ((SimpleItemAnimator) rvPosts.getItemAnimator()).setSupportsChangeAnimations(false);
        rvPosts.setAdapter(mPostAdapter);
    }

    private void onClickPostOptions(final Post post) {
        new MaterialDialog.Builder(this)
                .items(R.array.post_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Log.d(TAG, "onSelection: which: " + which + " " + text.toString());
                        switch (which) {
                            case 0:
                                removeUserPost(post.getKey());
                                break;
                            default:
                                break;
                        }
                    }
                }).show();
    }

    private void removeUserPost(String postKey) {
        mPostRef.child(postKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: removeUserPost");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (uid != null) {
            database.child(Constants.F_USERS).child(uid)
                    .addValueEventListener(userValueEventListener);
            database.child(Constants.F_LOCATIONS).child(uid)
                    .addValueEventListener(locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
        database.child(Constants.F_USERS).child(uid)
                .removeEventListener(userValueEventListener);
        database.child(Constants.F_LOCATIONS).child(uid)
                .removeEventListener(locationListener);
    }

    @OnClick(R.id.btn_new_post)
    public void onClickNewPost() {
        Intent intent = new Intent(getApplicationContext(), NewPostActivity.class);
        startActivity(intent);
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
    public void onDestroy() {
        super.onDestroy();
        mPostAdapter.cleanup();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void themeNavAndStatusBar(Activity activity)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;

        Window w = activity.getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        w.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setNavigationBarColor(activity.getResources().getColor(android.R.color.transparent));

        w.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
    }

    @OnClick(R.id.fab_profile)
    public void onClickFabProfile() {
        new MaterialDialog.Builder(this)
                .items(R.array.profile_yourself_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Log.d(TAG, "onSelection: which: " + which);
                        switch (which) {
                            case 2:
                                launchProfileEditActivity();
                                break;
                            default:
                                break;
                        }
                    }
                }).show();
    }

    private void launchProfileEditActivity() {
        Intent intent = new Intent(getApplicationContext(), ProfileEditActivity.class);
        startActivity(intent);
    }
}
