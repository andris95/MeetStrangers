package com.soft.sanislo.meetstrangers.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.Post;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 24.09.16.
 */
public class PostViewHolder extends RecyclerView.ViewHolder {
    View mRootView;

    @BindView(R.id.iv_post_photo)
    ImageView ivPost;

    @BindView(R.id.tv_post_text)
    TextView tvPostText;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder().build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingProgressListener progressListener;

    public PostViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
        //ButterKnife.bind(mRootView);
        ivPost = (ImageView) mRootView.findViewById(R.id.iv_post_photo);
        tvPostText = (TextView) mRootView.findViewById(R.id.tv_post_text);
    }

    public void setPostText(Post post) {
        if (post.getText() != null) {
            tvPostText.setText(post.getText());
        }
    }

    public void setPostPhoto(Post post) {
        imageLoader.displayImage(post.getPhotoURL(), ivPost, displayImageOptions, null, progressListener);
    }
}
