package com.soft.sanislo.meetstrangers.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.soft.sanislo.meetstrangers.adapter.PostAdapter;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.Post;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    private PostAdapter.OnClickListener mOnClickListener;

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

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

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
        imageLoader.displayImage(mPost.getAuthorAvatarURL(), ivPostAuthorAvatar, displayImageOptions);
    }

    public void setPostPhotosList() {
        int counter = 0;
        for (String url : mPost.getPhotoURLList()) {
            Log.d(TAG, "setPostPhotosList: url: " + url);
            ImageView ivPhoto = new ImageView(mContext);
            ivPhoto.setAdjustViewBounds(true);
            ivPhoto.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnClickListener != null) {
                        mOnClickListener.onClick(view, 0, mPost);
                    }
                }
            });
            ivPhoto.setTag(counter);
            llPostPhotos.addView(ivPhoto);
            imageLoader.displayImage(url, ivPhoto);
            counter++;
        }
    }

    public void populate(Context context,
                         Post post,
                         final PostAdapter.OnClickListener onClickListener,
                         final int position) {
        mPost = post;
        mContext = context;
        mOnClickListener = onClickListener;

        tvPostAuthor.setText(post.getAuthFullName());
        setPostText();
        setPostAuthorAvatar();
        setPostDate();
        setPostPhotosList();

        ivPostAuthorAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(view, position, mPost);
                    Log.d(TAG, "onClick: position: " + position + " view: " + view.getId());
                }
            }
        });
        ivPostOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(view, position, mPost);
                    Log.d(TAG, "onClick: position: " + position + " view: " + view.getId());
                }
            }
        });
        llPostPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener != null) {
                    onClickListener.onClick(view, position, mPost);
                }
            }
        });
    }
}
