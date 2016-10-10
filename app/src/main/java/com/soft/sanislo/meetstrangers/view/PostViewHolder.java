package com.soft.sanislo.meetstrangers.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.soft.sanislo.meetstrangers.adapter.CommentAdapter;
import com.soft.sanislo.meetstrangers.adapter.PostAdapter;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 24.09.16.
 */
public class PostViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = PostViewHolder.class.getSimpleName();

    private View mRootView;
    private Post mPost;
    private Context mContext;
    private int mPostition;
    private PostAdapter.OnClickListener mOnClickListener;
    private boolean shouldShowComments;

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
    ImageView ivLikePost;

    @BindView(R.id.tv_like_counter)
    TextView tvLikeCounter;

    @BindView(R.id.iv_comment_post)
    ImageView ivCommentPost;

    @BindView(R.id.rv_comments)
    RecyclerView rvComments;

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
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private String mAuthUserUID;
    private CommentAdapter mCommentAdapter;

    public PostViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
        //ButterKnife.bind(mContext, mRootView);

        ivPostAuthorAvatar = (ImageView) mRootView.findViewById(R.id.iv_post_author_avatar);
        tvPostText = (TextView) mRootView.findViewById(R.id.tv_post_text);
        tvPostAuthor = (TextView) mRootView.findViewById(R.id.tv_post_author);
        tvPostDate = (TextView) mRootView.findViewById(R.id.tv_post_date);
        ivPostOptions = (ImageView) mRootView.findViewById(R.id.iv_post_options);
        llPostPhotos = (LinearLayout) mRootView.findViewById(R.id.ll_post_photos);
        ivLikePost = (ImageView) mRootView.findViewById(R.id.iv_like_post);
        tvLikeCounter = (TextView) mRootView.findViewById(R.id.tv_like_counter);
        ivCommentPost = (ImageView) mRootView.findViewById(R.id.iv_comment_post);

        rlComments = (RelativeLayout) mRootView.findViewById(R.id.rl_post_comments);
        rvComments = (RecyclerView) mRootView.findViewById(R.id.rv_comments);
        rvComments.setLayoutManager(new LinearLayoutManager(mContext));

        btnAddComment = (Button) mRootView.findViewById(R.id.btn_add_comment);
        btnCancelComment = (Button) mRootView.findViewById(R.id.btn_cancel_comment);
        edtNewComment = (EditText) mRootView.findViewById(R.id.edt_new_comment);
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

        setLikeIcon();
        setLikeCounter();
        setPostText();
        setAuthorName();
        setPostAuthorAvatar();
        setPostDate();
        setPostPhotosList();
        showCommentsList();
        setClickListeners();
    }

    private void setAuthorName() {
        tvPostAuthor.setText(mPost.getAuthFullName());
    }

    public void setPostText() {
        if (!TextUtils.isEmpty(mPost.getText())) {
            tvPostText.setText(mPost.getText());
        } else {
            tvPostText.setVisibility(View.GONE);
        }
    }

    private void setPostDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
        Date postDate = new Date(mPost.getTimestamp());
        String postDateDisplay = dateFormat.format(postDate);
        tvPostDate.setText(postDateDisplay);
    }

    public void setPostAuthorAvatar() {
        imageLoader.displayImage(mPost.getAuthorAvatarURL(), ivPostAuthorAvatar,
                displayImageOptions);
    }


    public void setPostPhotosList() {
        int counter = 0;
        llPostPhotos.removeAllViews();
        if (mPost.getPhotoURLList() == null) return;
        for (String url : mPost.getPhotoURLList()) {
            Log.d(TAG, "setPostPhotosList: url: " + url);
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

            imageLoader.displayImage(url, ivPhoto, displayImageOptions, getPostPhotoLoadingListener(ivPhoto));
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

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
                Log.d(TAG, "onLoadingFailed: " + failReason.getType().name());
                failReason.getCause().printStackTrace();
            }
        };
        return postPhotoLoadingListener;
    }

    private void showCommentsList() {
        if (shouldShowComments) {
            rlComments.setVisibility(View.VISIBLE);
            DatabaseReference commentRef = Utils.getDatabase().getReference()
                    .child(Constants.F_POSTS_COMMENTS)
                    .child(mPost.getAuthorUID())
                    .child(mPost.getKey());
            mCommentAdapter = new CommentAdapter(Comment.class,
                    R.layout.item_comment,
                    CommentViewHolder.class,
                    commentRef);
            rvComments.setAdapter(mCommentAdapter);
        } else {
            rlComments.setVisibility(View.GONE);
            if (mCommentAdapter != null) {
                mCommentAdapter.cleanup();
                mCommentAdapter = null;
            }
            btnAddComment.setOnClickListener(null);
            btnCancelComment.setOnClickListener(null);
        }
    }

    private void setLikeIcon() {
        HashMap<String, Boolean> likers = mPost.getLikedUsersUIDs();
        if (likers != null) {
            if (likers.containsKey(mAuthUserUID)) {
                ivLikePost.setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.heart));
                Log.d(TAG, "setLikeIcon: contains");
            } else {
                ivLikePost.setImageDrawable(mContext.getResources()
                        .getDrawable(R.drawable.heart_outline));
                Log.d(TAG, "setLikeIcon: !contains");
            }
        } else {
            ivLikePost.setImageDrawable(mContext.getResources()
                    .getDrawable(R.drawable.heart_outline));
            Log.d(TAG, "setLikeIcon: likers null");
        }
    }

    private void setLikeCounter() {
        if (mPost.getLikesCount() != 0) {
            tvLikeCounter.setText(mPost.getLikesCount() + "");
        } else {
            tvLikeCounter.setText("");
        }
    }

    public boolean isShouldShowComments() {
        return shouldShowComments;
    }

    public void setShouldShowComments(boolean shouldShowComments) {
        this.shouldShowComments = shouldShowComments;
    }

    private void onClickAddComment() {
        String newCommentText = edtNewComment.getText().toString();
        if (!TextUtils.isEmpty(newCommentText)) {
            mOnClickListener.onClickAddComment(mPost, newCommentText);
            edtNewComment.setText("");
        }
    }

    private void onClickCancelComment() {
        edtNewComment.setText("");
        mOnClickListener.onClickCancelComment();
    }

    private void setClickListeners() {
        ivPostAuthorAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(view, mPostition, mPost);
                    Log.d(TAG, "onClick: position: " + mPostition + " view: " + view.getId());
                }
            }
        });
        ivPostOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(view, mPostition, mPost);
                    Log.d(TAG, "onClick: position: " + mPostition + " view: " + view.getId());
                }
            }
        });
        llPostPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(view, mPostition, mPost);
                }
            }
        });
        ivLikePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) mOnClickListener.onClick(view, mPostition, mPost);
            }
        });
        ivCommentPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(view, mPostition, mPost);
                }
            }
        });
        btnCancelComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickCancelComment();
            }
        });
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAddComment();
            }
        });
    }
}
