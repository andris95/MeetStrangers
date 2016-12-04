package com.soft.sanislo.meetstrangers.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.soft.sanislo.meetstrangers.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 27.11.16.
 */

public class PhotViewHolder extends RecyclerView.ViewHolder {
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private View mRootView;

    @BindView(R.id.iv_photo)
    ImageView ivPhoto;

    public PhotViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
        ButterKnife.bind(this, mRootView);
    }

    public void bind(String url) {
        imageLoader.displayImage(url, ivPhoto);
    }
}
