package com.soft.sanislo.meetstrangers.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.soft.sanislo.meetstrangers.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 04.12.16.
 */

public class GroupProfileActivity extends BaseActivity {
    public static final String TAG = GroupProfileActivity.class.getSimpleName();

    @BindView(R.id.iv_group_avatar)
    ImageView ivGroupAvatar;

    @BindView(R.id.rv_groups_posts)
    RecyclerView rvGroupsPosts;

    @BindView(R.id.tv_group_name)
    TextView tvGroupName;

    @BindView(R.id.tv_group_status)
    TextView tvGroupStatus;

    public static final String KEY_GROUP_KEY = "KEY_GROUP_KEY";
    private String mGroupKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);
        ButterKnife.bind(this);

        mGroupKey = getIntent().getStringExtra(KEY_GROUP_KEY);
        initGroupsPosts();
    }

    private void initGroupsPosts() {

    }
}
