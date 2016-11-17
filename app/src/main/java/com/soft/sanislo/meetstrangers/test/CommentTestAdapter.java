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
import com.soft.sanislo.meetstrangers.adapter.CommentAdapter;
import com.soft.sanislo.meetstrangers.view.ChatMessageViewHolder;
import com.soft.sanislo.meetstrangers.view.LoadingViewHolder;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 08.11.16.
 */

public class CommentTestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<CommentTest> mData;
    private LayoutInflater mLayoutInflater;
    private int mExpandedPos = RecyclerView.NO_POSITION;
    private boolean isLoading;

    private static final int TYPE_LOADING = 228;
    private static final int TYPE_COMMENT = 229;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

    public CommentTestAdapter(Context context, ArrayList<CommentTest> data) {
        mData = data;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_LOADING:
                view = mLayoutInflater.inflate(R.layout.item_loading, parent, false);
                return new LoadingViewHolder(view);
            case TYPE_COMMENT:
                view = mLayoutInflater.inflate(R.layout.item_comment, parent, false);
                return new ViewHolder(view);
            default:
                view = mLayoutInflater.inflate(R.layout.item_comment, parent, false);
                return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentTestAdapter.ViewHolder) {
            ((ViewHolder) holder).setExpanded(position == mExpandedPos);
            ((ViewHolder) holder).populate(null, mData.get(position), position, null);
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position) == null ? TYPE_LOADING : TYPE_COMMENT;
    }

    public void addMoreData(ArrayList<CommentTest> moreData) {
        mData.addAll(moreData);
        notifyDataSetChanged();
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
        if (isLoading) {
            mData.add(mData.size(), null);
            notifyItemInserted(mData.size() - 1);
        } else {
            mData.remove(mData.size() - 1);
            notifyDataSetChanged();
        }
    }

    public boolean isLoading() {
        return isLoading;
    }

    public interface OnLoadMore {
        void onLoadMore();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View mRootView;

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

        private CommentTest mComment;
        private boolean isExpanded;

        public ViewHolder(View itemView) {
            super(itemView);
            mRootView = itemView;
            ButterKnife.bind(this, mRootView);
        }

        private void setAuthorName() {
            tvAuthorName.setText(mComment.getAuthorName());
        }

        public void populate(Context context, final CommentTest comment, final int position,
                             CommentAdapter.OnClickListener onClickListener) {
            mComment = comment;
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

        private void setAuthorAvatar() {
            imageLoader.displayImage(mComment.getAuthorAvatarURL(), ivAuthorAvatar, displayImageOptions);
        }
        private void setCommentLikeIcon() {
            ivLikeComment.setImageResource(mComment.isLiked ? R.drawable.heart_red
                    : R.drawable.heart_outline);
        }

        private void setText() {
            tvText.setText(mComment.getText());
        }

        public void setExpanded(boolean expanded) {
            isExpanded = expanded;
        }
    }
}
