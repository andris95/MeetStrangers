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
import com.soft.sanislo.meetstrangers.presenter.ProfilePresenter;
import com.soft.sanislo.meetstrangers.presenter.ProfilePresenterImpl;
import com.soft.sanislo.meetstrangers.service.FetchAddressIntentService;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.LocationUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.PostViewHolder;
import com.soft.sanislo.meetstrangers.view.ProfileView;

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
public class ProfileActivity extends BaseActivity implements ProfileView {
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

    private ProfilePresenter mProfilePresenter;
    private DatabaseReference database = Utils.getDatabase().getReference();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mAuthenticatedUserUID;
    private String mDisplayedUserUID;

    private Relationship mRelationship;
    private String mLastActionUID;
    private String mFirstActionUID;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private PostAdapter mPostAdapter;
    private Query mPostQuery;

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
                    mProfilePresenter.likePost(post.getKey());
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
            mProfilePresenter.addComment(post, commentText);
        }

        @Override
        public void onClickCancelComment() {
            mPostAdapter.setCommentsVisiblePos(-1);
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                TransitionManager.beginDelayedTransition(rvPosts);
                mPostAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onClickHighlightComment() {
            TransitionManager.beginDelayedTransition(rvPosts);
        }

        @Override
        public void onClickLikeComment(Comment comment) {
            mProfilePresenter.likeComment(comment);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        initTransition();
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = mFirebaseUser.getUid();
        mDisplayedUserUID = getIntent().getStringExtra(KEY_UID);
        mProfilePresenter = new ProfilePresenterImpl(this, this, mDisplayedUserUID);

        initPosts();
    }

    private void initTransition() {
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
            requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);

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

    @Override
    protected void onResume() {
        super.onResume();
        mProfilePresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mProfilePresenter.onPause();
    }

    @Override
    public void onDisplayedUserChanged(User user) {
        collapsingToolbar.setTitle(user.getFullName());
        imageLoader.loadImage(user.getAvatarURL(), displayImageOptions, new SimpleImageLoadingListener() {
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
    }

    @Override
    public void onLastActiveChanged(String lastActive) {
        tvLastActive.setText(lastActive);
    }

    @Override
    public void onAddressFetchSuccess(String address) {
        tvAddress.setText(address);
    }

    @Override
    public void onAddressFetchFailure(String errorMessage) {
        tvAddress.setText(errorMessage);
    }

    @OnClick(R.id.fab_profile)
    public void onClickFAB() {
        mProfilePresenter.onClickFAB();
    }

    @Override
    public void onComplete(String message) {

    }

    @Override
    public void onError(String errorMessage) {
        makeToast(errorMessage);
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onDatabaseError(DatabaseError databaseError) {
        makeToast(databaseError.getMessage());
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        mProfilePresenter.onBackPressed();
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
