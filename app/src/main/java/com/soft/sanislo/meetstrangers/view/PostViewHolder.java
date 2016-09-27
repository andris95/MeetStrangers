package com.soft.sanislo.meetstrangers.view;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.soft.sanislo.meetstrangers.PostAdapter;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 24.09.16.
 */
public class PostViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = PostViewHolder.class.getSimpleName();

    private View mRootView;

    @BindView(R.id.iv_post_photo)
    ImageView ivPost;

    @BindView(R.id.iv_post_author_avatar)
    ImageView ivPostAuthorAvatar;

    @BindView(R.id.tv_post_text)
    TextView tvPostText;

    @BindView(R.id.tv_post_author)
    TextView tvPostAuthor;

    @BindView(R.id.tv_post_date)
    TextView tvPostDate;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder().build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingProgressListener progressListener;

    public PostViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
        //ButterKnife.populate(mRootView);
        ivPost = (ImageView) mRootView.findViewById(R.id.iv_post_photo);
        ivPostAuthorAvatar = (ImageView) mRootView.findViewById(R.id.iv_post_author_avatar);
        tvPostText = (TextView) mRootView.findViewById(R.id.tv_post_text);
        tvPostAuthor = (TextView) mRootView.findViewById(R.id.tv_post_author);
        tvPostDate = (TextView) mRootView.findViewById(R.id.tv_post_date);
    }

    public void setPostText(Post post) {
        if (post.getText() != null) {
            tvPostText.setText(post.getText());
        } else {
            tvPostText.setVisibility(View.GONE);
        }
    }

    public void setPostPhoto(Post post) {
        imageLoader.displayImage(post.getPhotoURL(), ivPost, displayImageOptions, null, progressListener);
    }

    public void setPostAuthorAvatar(Post post) {
        imageLoader.displayImage(post.getAuthorAvatarURL(), ivPostAuthorAvatar, displayImageOptions);
    }

    public void populate(Post post, final PostAdapter.OnClickListener onClickListener, final int position) {
        tvPostAuthor.setText(post.getAuthFullName());
        tvPostDate.setText(post.getTimestamp() + "");
        setPostText(post);
        setPostAuthorAvatar(post);
        setPostPhoto(post);

        ivPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(view, position);
                    Log.d(TAG, "onClick: position: " + position + " view: " + view.getId());
                }
            }
        });
        ivPostAuthorAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(view, position);
                    Log.d(TAG, "onClick: position: " + position + " view: " + view.getId());
                }
            }
        });
    }
}
