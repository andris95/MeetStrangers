package com.soft.sanislo.meetstrangers.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.CommentAdapter;
import com.soft.sanislo.meetstrangers.model.CommentModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by root on 09.10.16.
 */
public class CommentViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = CommentViewHolder.class.getSimpleName();
    private View mRootView;
    private CommentModel mCommentModel;
    private int mPosition;
    private Context mContext;
    private String mAuthUID;
    private CommentAdapter.OnClickListener mOnClickListener;
    private boolean isExpanded;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

    @BindView(R.id.iv_comment_author_avatar)
    ImageView ivAuthorAvatar;

    @BindView(R.id.tv_comment_author_name)
    TextView tvAuthorName;

    @BindView(R.id.tv_comment_text)
    TextView tvText;

    @BindView(R.id.iv_like_comment)
    ImageView ivLikeComment;

    @BindView(R.id.rl_comment)
    RelativeLayout rlComment;

    @BindView(R.id.rl_comment_footer)
    RelativeLayout rlCommentFooter;

    public CommentViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
        ButterKnife.bind(this, mRootView);
    }

    public void populate(Context context, final CommentModel commentModel, final int position,
                         CommentAdapter.OnClickListener onClickListener) {
        mContext = context;
        mCommentModel = commentModel;
        mPosition = position;
        mOnClickListener = onClickListener;

        setAuthorAvatar();
        setAuthorName();
        setText();
        setCommentLikeIcon();
        setFooterVisibility();
    }

    private void setFooterVisibility() {
        rlCommentFooter.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        rlComment.setSelected(isExpanded);
    }

    private void setCommentLikeIcon() {
        Log.d(TAG, "setCommentLikeIcon: liked by " + mAuthUID + ": " + mCommentModel.isLikedByUser(mAuthUID));
        ivLikeComment.setImageResource(mCommentModel.isLikedByUser(mAuthUID) ? R.drawable.heart_red
                : R.drawable.heart_outline);
    }

    @OnClick(R.id.rl_comment)
    public void onClickCommentRoot() {
        mOnClickListener.onClick(rlComment, mPosition, mCommentModel);
    }

    @OnClick(R.id.iv_like_comment)
    public void onClickLikeComment() {
        mOnClickListener.onClickLikeComment(mCommentModel);
        Log.d(TAG, "onClickLikeComment: ");
    }

    private void setAuthorAvatar() {
        imageLoader.displayImage(mCommentModel.getAuthorAvatarURL(), ivAuthorAvatar, displayImageOptions);
    }

    private void setAuthorName() {
        tvAuthorName.setText(mCommentModel.getAuthorFullName());
    }

    private void setText() {
        tvText.setText(mCommentModel.getText());
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public void setAuthUID(String authUID) {
        mAuthUID = authUID;
    }
}
