package com.soft.sanislo.meetstrangers.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.Group;
import com.soft.sanislo.meetstrangers.presenter.GroupEditPresenter;
import com.soft.sanislo.meetstrangers.presenter.GroupEditPresenterImpl;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.GroupEditView;

import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by root on 04.12.16.
 */

public class GroupEditActivity extends BaseActivity implements GroupEditView {
    public static final String TAG = GroupEditActivity.class.getSimpleName();
    public static final String KEY_GROUP_KEY = "KEY_GROUP_KEY";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.iv_group_avatar)
    ImageView ivGroupAvatar;

    @BindView(R.id.tv_group_name)
    TextView tvGroupName;

    @BindView(R.id.tv_group_status)
    TextView tvGroupStatus;

    @BindView(R.id.btn_group_action)
    Button btnGroupAction;

    private GroupEditPresenter mGroupEditPresenter;
    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnFail(R.drawable.placeholder)
            .showImageForEmptyUri(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mGroupEditPresenter = new GroupEditPresenterImpl(GroupEditActivity.this, getIntent());
        initGroupsPosts();
    }

    private void initGroupsPosts() {

    }

    @OnClick(R.id.tv_group_status)
    public void onClickGroupStatus() {
        mGroupEditPresenter.onClickGroupStatus();
    }

    @OnClick(R.id.iv_group_avatar)
    public void onClickGroupAvatar() {
        mGroupEditPresenter.onClickGroupAvatar();
    }

    @OnClick(R.id.btn_group_action)
    public void onClockGroupAction() {
        mGroupEditPresenter.onClickGroupAction();
    }

    @OnClick(R.id.btn_new_group_post)
    public void onClickNewGroupPost() {
        mGroupEditPresenter.onCLickNewGroupPost();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGroupEditPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGroupEditPresenter.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mGroupEditPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onGroupDataChange(Group group) {
        bindGroupData(group);
    }

    @Override
    public void onSelectGallery() {
        onSelectedGallery();
    }

    private void onSelectedGallery() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, Constants.RC_PICK_IMAGE_GALLERY);
    }

    private void bindGroupData(Group group) {
        Log.d(TAG, "bindGroupData: " + group);
        tvGroupName.setText(group.getName());
        mToolbar.setTitle(group.getName());
        if (!TextUtils.isEmpty(group.getStatus())) {
            tvGroupStatus.setText(group.getStatus());
        } else {
            tvGroupStatus.setText("Change group status");
        }
        imageLoader.displayImage(group.getGroupAvatar(), ivGroupAvatar, displayImageOptions);
        if (group.isMember(getAuthenticatedUserUID())) {
            btnGroupAction.setText("joined");
        } else {
            btnGroupAction.setText("join");
        }
    }
}
