package com.soft.sanislo.meetstrangers.viewholders;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.CommentAdapter;
import com.soft.sanislo.meetstrangers.interfaces.PostClickListener;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.Group;
import com.soft.sanislo.meetstrangers.model.MediaFile;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.DateUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by root on 04.12.16.
 */

public class GroupPostViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = UserPostViewHolder.class.getSimpleName();

    private View mRootView;
    private Post mPost;
    private Group mGroup;
    private Context mContext;
    private int mPostition;
    private String mAuthUserUID;
    private PostClickListener mOnClickListener;
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

    @BindView(R.id.iv_like_post)
    ImageButton ivLikePost;

    @BindView(R.id.tv_like_counter)
    TextView tvLikeCounter;

    @BindView(R.id.iv_comment_post)
    ImageView ivCommentPost;

    @BindView(R.id.rv_test)
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
            .showImageOnFail(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private CommentAdapter mCommentAdapter;

    public GroupPostViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
        ButterKnife.bind(this, mRootView);
        rvComments.setLayoutManager(new LinearLayoutManager(mContext));
        rvComments.setItemAnimator(new DefaultItemAnimator());
    }

    public void populate(Context context,
                         Post post,
                         final PostClickListener onClickListener,
                         final int position) {
        mPost = post;
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
        setPostDate();
        setCommentsListVisibility();
        getAuthorData();
    }

    private void getAuthorData() {
        Utils.getDatabase().getReference()
                .child(Constants.F_GROUPS)
                .child(mPost.getAuthorUID())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mGroup = dataSnapshot.getValue(Group.class);
                        setAuthorName();
                        setPostAuthorAvatar();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void setAuthorName() {
        tvPostAuthor.setText(mGroup.getName());
    }

    private void setPostText() {
        if (!TextUtils.isEmpty(mPost.getContent())) {
            tvPostText.setText(mPost.getContent());
        } else {
            tvPostText.setVisibility(View.GONE);
        }
    }

    private void setPostDate() {
        String postDateDisplay = DateUtils.getDateDisplay(mPost.getTimestamp());
        tvPostDate.setText(postDateDisplay);
    }

    private void setPostAuthorAvatar() {
        imageLoader.displayImage(mGroup.getAvatarURL(), ivPostAuthorAvatar,
                displayImageOptions);
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
