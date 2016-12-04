package com.soft.sanislo.meetstrangers.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.GroupsAdapter;
import com.soft.sanislo.meetstrangers.model.Group;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 04.12.16.
 */

public class GroupViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = GroupViewHolder.class.getSimpleName();
    private View mRootView;

    @BindView(R.id.iv_group_avatar)
    ImageView ivGroupAvatar;

    @BindView(R.id.tv_group_name)
    TextView tvGroupName;

    private Group mGroup;
    private int mPosition;
    private GroupsAdapter.OnClickListener mOnClickListener;
    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

    public GroupViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
        ButterKnife.bind(this, mRootView);
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(view, mGroup, mPosition);
                }
            }
        });
    }

    public void populate(Group group, int position, GroupsAdapter.OnClickListener onClickListener) {
        mGroup = group;
        mPosition = position;
        mOnClickListener = onClickListener;

        setGroupAvatar();
        setGroupName();
    }

    private void setGroupAvatar() {
        imageLoader.displayImage(mGroup.getGroupAvatar(), ivGroupAvatar, displayImageOptions);
    }

    private void setGroupName() {
        tvGroupName.setText(mGroup.getName());
    }
}
