package com.soft.sanislo.meetstrangers.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.Query;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.soft.sanislo.meetstrangers.adapter.CommentAdapter;
import com.soft.sanislo.meetstrangers.adapter.PostAdapter;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.MediaFile;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.DateUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by root on 24.09.16.
 */
public class PostViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = PostViewHolder.class.getSimpleName();

    private View mRootView;
    private Post mPost;
    private Context mContext;
    private int mPostition;
    private String mAuthUID;
    private PostAdapter.OnClickListener mOnClickListener;
    private Transition expandCollapse;
    private boolean isExpanded;
    private Query commentQuery;

    @BindView(R.id.iv_post_author_avatar)
    ImageView ivPostAuthorAvatar;

    @BindView(R.id.tv_post_text)
    TextView tvPostText;

    @BindView(R.id.tv_post_author)
    TextView tvPostAuthor;

    @BindView(R.id.tv_post_date)
    TextView tvPostDate;

    @BindView(R.id.iv_post_options)
    ImageView ivPostOptions;

    @BindView(R.id.ll_post_photos)
    LinearLayout llPostPhotos;

    @BindView(R.id.iv_like_post)
    ImageButton ivLikePost;

    @BindView(R.id.tv_like_counter)
    TextView tvLikeCounter;

    @BindView(R.id.iv_comment_post)
    ImageView ivCommentPost;

    @BindView(R.id.rv_test)
    RecyclerView rvComments;

    @BindView(R.id.)

    @BindView(R.id.btn_add_comment)
    Button btnAddComment;

    @BindView(R.id.btn_cancel_comment)
    Button btnCancelComment;

    @BindView(R.id.edt_new_comment)
    EditText edtNewComment;

    @BindView(R.id.rl_post_comments)
    RelativeLayout rlComments;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.placeholder)
            .showImageOnFail(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private String mAuthUserUID;
    private CommentAdapter mCommentAdapter;

    public PostViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
        ButterKnife.bind(this, mRootView);
        rvComments.setLayoutManager(new LinearLayoutManager(mContext));
        rvComments.setItemAnimator(new DefaultItemAnimator());
    }

    public void populate(Context context,
                         Post post,
                         String authUserUID,
                         final PostAdapter.OnClickListener onClickListener,
                         final int position) {
        mPost = post;
        mAuthUserUID = authUserUID;
        mContext = context;
        mPostition = position;
        mOnClickListener = onClickListener;
        commentQuery = Utils.getDatabase().getReference()
                .child(Constants.F_POSTS_COMMENTS)
                .child(mPost.getAuthorUID())
                .child(mPost.getKey())
                .orderByPriority();

        setLikeIcon();
        setLikeCounter();
        setPostText();
        setAuthorName();
        setPostAuthorAvatar();
        setPostDate();
        setPostPhotosList();
        setCommentsListVisibility();
    }

    private void setAuthorName() {
        tvPostAuthor.setText(mPost.getAuthFullName());
    }

    private void setPostText() {
        if (!TextUtils.isEmpty(mPost.getText())) {
            tvPostText.setText(mPost.getText());
        } else {
            tvPostText.setVisibility(View.GONE);
        }
    }

    private void setPostDate() {
        String postDateDisplay = DateUtils.getDateDisplay(mPost.getTimestamp());
        tvPostDate.setText(postDateDisplay);
    }

    private void setPostAuthorAvatar() {
        imageLoader.displayImage(mPost.getAuthorAvatarURL(), ivPostAuthorAvatar,
                displayImageOptions);
    }

    private void setPostPhotosList() {
        int counter = 0;
        llPostPhotos.removeAllViews();
        if (mPost.getMediaFiles() == null) return;
        for (MediaFile mediaFile : mPost.getMediaFiles()) {
            Log.d(TAG, "setPostPhotosList: " + mediaFile);
            final ImageView ivPhoto = new ImageView(mContext);
            ivPhoto.setAdjustViewBounds(true);
            ivPhoto.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            final int finalCounter = counter;
            ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(view, finalCounter, mPost);
                    }
                }
            });
            ivPhoto.setTag(counter);
            ImageSize imageSize = new ImageSize(mediaFile.getWidth(), mediaFile.getHeight());
            imageLoader.displayImage(mediaFile.getUrl(),
                    ivPhoto,
                    displayImageOptions,
                    getPostPhotoLoadingListener(ivPhoto));
            counter++;
        }
    }

    private SimpleImageLoadingListener getPostPhotoLoadingListener(final ImageView ivPhoto) {
        SimpleImageLoadingListener postPhotoLoadingListener = new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                llPostPhotos.addView(ivPhoto);
            }
        };
        return postPhotoLoadingListener;
    }

    private void setCommentsListVisibility() {
        if (isExpanded) {
            showComments();
        } else {
            hideComments();
        }
    }

    private void showComments() {
        mCommentAdapter = new CommentAdapter(Comment.class,
                R.layout.item_comment,
                CommentViewHolder.class,
                commentQuery);
        mCommentAdapter.setContext(mContext);
        mCommentAdapter.setOnClickListener(new CommentAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int position, Comment comment) {
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    int expanedPos = mCommentAdapter.getExpandedPos() == position ? -1 : position;
                    mCommentAdapter.setExpandedPos(expanedPos);
                    mCommentAdapter.notifyDataSetChanged();
                    mOnClickListener.onClickHighlightComment();
                }
            }

            @Override
            public void onClickLikeComment(Comment comment) {
                mOnClickListener.onClickLikeComment(comment);
            }
        });
        mCommentAdapter.setAuthUID(mAuthUserUID);
        rvComments.setAdapter(mCommentAdapter);
        rlComments.setVisibility(View.VISIBLE);
    }

    private void hideComments() {
        rlComments.setVisibility(View.GONE);
        if (mCommentAdapter != null) {
            mCommentAdapter.cleanup();
            mCommentAdapter = null;
        }
    }

    private void setLikeIcon() {
        ivLikePost.setSelected(mPost.isLikedByUser(mAuthUserUID));
    }

    private void setLikeCounter() {
        if (mPost.getLikesCount() != 0) {
            tvLikeCounter.setText(mPost.getLikesCount() + "");
        } else {
            tvLikeCounter.setText("");
        }
    }

    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
    }

    public void setAuthUID(String authUID) {
        mAuthUID = authUID;
    }

    private void addComment() {
        String newCommentText = edtNewComment.getText().toString();
        if (!TextUtils.isEmpty(newCommentText)) {
            mOnClickListener.onClickAddComment(mPost, newCommentText);
            edtNewComment.setText("");
        } else {
            mOnClickListener.onClickAddComment(mPost, mContext.getString(R.string.lorem));
        }
    }

    private void cancelComment() {
        edtNewComment.setText("");
        mOnClickListener.onClickCancelComment();
    }

    @OnClick(R.id.iv_post_author_avatar)
    public void onClickPostAuthorAvatar() {
        mOnClickListener.onClick(ivPostAuthorAvatar, mPostition, mPost);
    }

    @OnClick(R.id.iv_post_options)
    public void onClickPostOptions() {
        mOnClickListener.onClick(ivPostOptions, mPostition, mPost);
    }

    @OnClick(R.id.ll_post_photos)
    public void onClickPostPhotos() {
        mOnClickListener.onClick(llPostPhotos, mPostition, mPost);
    }

    @OnClick(R.id.iv_like_post)
    public void onClickLikePost() {
        mOnClickListener.onClick(ivLikePost, mPostition, mPost);
    }

    @OnClick(R.id.btn_cancel_comment)
    public void onClickCancelComment() {
        cancelComment();
    }

    @OnClick(R.id.btn_add_comment)
    public void onClickAddComment() {
        addComment();
    }

    @OnClick(R.id.iv_comment_post)
    public void onClickCommentPost() {
        mOnClickListener.onClick(ivCommentPost, mPostition, mPost);
    }
}
