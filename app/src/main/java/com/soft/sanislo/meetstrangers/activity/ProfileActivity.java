package com.soft.sanislo.meetstrangers.activity;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.soft.sanislo.meetstrangers.adapter.PostAdapter;
import com.soft.sanislo.meetstrangers.adapter.TransitionListenerAdapter;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.presenter.ProfilePresenter;
import com.soft.sanislo.meetstrangers.presenter.ProfilePresenterImpl;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.viewholders.UserPostViewHolder;
import com.soft.sanislo.meetstrangers.view.ProfileView;

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

    @BindView(R.id.tvProfileLastActive)
    TextView tvLastActive;

    @BindView(R.id.tvProfileLocation)
    TextView tvAddress;

    @BindView(R.id.fab_profile)
    FloatingActionButton fabProfile;

    @BindView(R.id.rv_posts)
    RecyclerView rvPosts;

/*    @BindView(R.id.scv_profile_content)
    NestedScrollView nscProfileContent;*/

    private ProfilePresenter mProfilePresenter;
    private DatabaseReference database = Utils.getDatabase().getReference();

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mAuthenticatedUserUID;
    private String mDisplayedUserUID;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private PostAdapter mPostAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private static final int VISIBLE_HOLDERS = 10;
    private Query mPostQuery;
    private Transition expandCollapse;

    @TargetApi(21)
    private void setStatusBarColor(Bitmap loadedImage) {
        Palette.from(loadedImage)
                .setRegion(0,
                        0,
                        loadedImage.getWidth(),
                        64)
                .generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        int color;
                        if (palette.getDarkVibrantSwatch() != null) {
                            color = palette.getDarkVibrantSwatch().getRgb();
                            Log.d(TAG, "onGenerated: darkmutedswatch: " + color);
                        } else if (palette.getDarkMutedSwatch() != null) {
                            color = palette.getDarkMutedSwatch().getRgb();
                            Log.d(TAG, "onGenerated: darkvibrantswatch: " + color);
                        } else if (palette.getLightVibrantSwatch() != null) {
                            color = palette.getLightVibrantSwatch().getRgb();
                            Log.d(TAG, "onGenerated: lightvibrantswatch: " + color);
                        } else {
                            color = palette.getDominantColor(getResources().getColor(R.color.primary_dark));
                            Log.d(TAG, "onGenerated: default");
                        }
                        color = palette.getDominantColor(getResources().getColor(R.color.primary_dark));

                        Log.d(TAG, "onGenerated: " +
                                palette.getDominantColor(getResources().getColor(R.color.primary_dark)));
                        getWindow().setStatusBarColor(color);
                    }
                });
    }

    private PostAdapter.OnClickListener mPostClickListener = new PostAdapter.OnClickListener() {
        @Override
        public void onClick(View view, int position, Post post) {
            if (view.getTag() != null) {
                int tag = (int) view.getTag();
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
            mPostAdapter.setCommentsVisiblePos(RecyclerView.NO_POSITION);
            /*if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                TransitionManager.beginDelayedTransition(rvPosts, expandCollapse);
                mPostAdapter.notifyDataSetChanged();
            }*/
            mPostAdapter.notifyDataSetChanged();
        }

        @Override
        public void onClickHighlightComment() {
            supportBegindDelayedTransition(rvPosts, expandCollapse);
        }

        @Override
        public void onClickLikeComment(Comment comment) {
            mProfilePresenter.likeComment(comment);
        }
    };

    private void onClickCommentPost(int position) {
        if (mPostAdapter.getCommentsVisiblePos() == position) {
            mPostAdapter.setCommentsVisiblePos(RecyclerView.NO_POSITION);
        } else {
            mPostAdapter.setCommentsVisiblePos(position);
        }
        /*if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            TransitionManager.beginDelayedTransition(rvPosts);
        }*/
        mPostAdapter.notifyItemChanged(position);
    }

    private View.OnTouchListener mTouchEater = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mAuthenticatedUserUID = mFirebaseUser.getUid();
        mDisplayedUserUID = getIntent().getStringExtra(KEY_UID);
        mProfilePresenter = new ProfilePresenterImpl(this, this, mDisplayedUserUID);

        initPosts();
        initPostTransition();
    }

    @TargetApi(21)
    private void initPostTransition() {
        expandCollapse = new AutoTransition();
        expandCollapse.setDuration(225);
        expandCollapse.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionStart(Transition transition) {
                super.onTransitionStart(transition);
                rvPosts.setOnTouchListener(mTouchEater);
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                super.onTransitionEnd(transition);
                rvPosts.setOnTouchListener(null);
            }
        });
    }

    private void supportBegindDelayedTransition(ViewGroup sceneRoot, Transition transition) {
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            TransitionManager.beginDelayedTransition(sceneRoot, transition);
        }
    }

    private void initPosts() {
        mPostQuery = database.child(Constants.F_POSTS)
                .child(mDisplayedUserUID)
                .orderByPriority()
                .limitToFirst(VISIBLE_HOLDERS);
        mPostAdapter = new PostAdapter(getApplicationContext(),
                Post.class,
                R.layout.item_post,
                UserPostViewHolder.class,
                mPostQuery);
        mPostAdapter.setAuthUserUID(mAuthenticatedUserUID);
        mPostAdapter.setOnClickListener(mPostClickListener);

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvPosts.setLayoutManager(mLinearLayoutManager);
        rvPosts.setNestedScrollingEnabled(false);
        rvPosts.setItemAnimator(new DefaultItemAnimator());
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
    public void onDestroy() {
        super.onDestroy();
        mPostAdapter.cleanup();
    }

    @Override
    public void onDisplayedUserChanged(User user) {
        collapsingToolbar.setTitle(user.getFullName());
        imageLoader.loadImage(user.getAvatarURL(), displayImageOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                imageLoader.displayImage(imageUri, ivAvatar, displayImageOptions, new SimpleImageLoadingListener() {
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
        mProfilePresenter.onBackPressed();
    }
}
