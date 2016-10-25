package com.soft.sanislo.meetstrangers.test;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.CommentModel;
import com.soft.sanislo.meetstrangers.view.CommentViewHolder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by root on 25.10.16.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private ArrayList<Comment> mComments = new ArrayList<>();
    private Context mContext;
    private int mExpandedPos = -1;
    private OnClickListener mOnClickListener;

    public CommentAdapter(Context context, ArrayList<Comment> commentModels) {
        mComments = commentModels;
        mContext = context;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_comment, parent, false);

        // Return a new holder instance
        CommentViewHolder viewHolder = new CommentViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        holder.setExpanded(mExpandedPos == position);
        holder.populate(mContext, mComments.get(position), position, mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public int getExpandedPos() {
        return mExpandedPos;
    }

    public void setExpandedPos(int expandedPos) {
        mExpandedPos = expandedPos;
    }

    public interface OnClickListener {
        void onClick(View view, int position, Comment comment);
        void onClickLikeComment(Comment comment);
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private View mRootView;
        private OnClickListener mOnClickListener;
        private boolean isExpanded;
        private Comment mComment;
        private int mPosition;

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

        public void populate(Context context, final Comment comment, final int position,
                             OnClickListener onClickListener) {
            mContext = context;
            mComment = comment;
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
            rlComment.setActivated(isExpanded);
        }

        private void setCommentLikeIcon() {
            ivLikeComment.setImageResource(mComment.isLiked() ? R.drawable.heart_red
                    : R.drawable.heart_outline);
        }

        @OnClick(R.id.rl_comment)
        public void onClickCommentRoot() {
            mOnClickListener.onClick(rlComment, mPosition, mComment);
        }

        @OnClick(R.id.iv_like_comment)
        public void onClickLikeComment() {
            mOnClickListener.onClickLikeComment(mComment);

        }

        private void setAuthorAvatar() {
            imageLoader.displayImage(mComment.getAuthorAvatarURL(), ivAuthorAvatar, displayImageOptions);
        }

        private void setAuthorName() {
            tvAuthorName.setText(mComment.getAuthorName());
        }

        private void setText() {
            tvText.setText(mComment.getText());
        }

        public void setExpanded(boolean expanded) {
            isExpanded = expanded;
        }
    }
}
