package com.soft.sanislo.meetstrangers.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.CommentAdapter;
import com.soft.sanislo.meetstrangers.model.Comment;

import butterknife.BindView;

/**
 * Created by root on 09.10.16.
 */
public class CommentViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = CommentViewHolder.class.getSimpleName();
    private View mRootView;
    private Comment mComment;
    private Context mContext;
    private CommentAdapter.OnClickListener mOnClickListener;

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

    public CommentViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
        ivAuthorAvatar = (ImageView) mRootView.findViewById(R.id.iv_comment_author_avatar);
        tvAuthorName = (TextView) mRootView.findViewById(R.id.tv_comment_author_name);
        tvText = (TextView) mRootView.findViewById(R.id.tv_comment_text);
    }

    public void populate(Context context, Comment comment, int position,
                         CommentAdapter.OnClickListener onClickListener) {
        mContext = context;
        mComment = comment;
        mOnClickListener = onClickListener;

        setAuthorAvatar();
        setAuthorName();
        setText();
        Log.d(TAG, "populate: comment: " + comment);
    }

    private void setAuthorAvatar() {
        imageLoader.displayImage(mComment.getAuthorAvatarURL(), ivAuthorAvatar, displayImageOptions);
    }

    private void setAuthorName() {
        tvAuthorName.setText(mComment.getAuthorFullName());
    }

    private void setText() {
        tvText.setText(mComment.getText());
    }
}
