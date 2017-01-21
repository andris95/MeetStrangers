package com.soft.sanislo.meetstrangers.activity;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.MediaFile;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.presenter.NewPostPresenter;
import com.soft.sanislo.meetstrangers.presenter.NewPostPresenterImpl;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.ImageUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.NewPostView;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Created by root on 24.09.16.
 */
public class NewPostActivity extends BaseActivity implements NewPostView {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.edt_post_text)
    EditText edtPostText;

    @BindView(R.id.pb_new_post)
    MaterialProgressBar progressBar;

    @BindView(R.id.btn_select_photo)
    Button btnAddPhotos;

    private static final String TAG = NewPostActivity.class.getSimpleName();
    public static final int PICK_IMAGE = 30000;
    public static final String KEY_GROUP_KEY = "GROUP_KEY";

    private String mPostAuthorUID;
    private String mGroupUID;

    private Menu mMenu;

    private NewPostPresenter mNewPostPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mGroupUID = getIntent().getStringExtra(KEY_GROUP_KEY);
        addSendPostEnabledListener();
        mNewPostPresenter = new NewPostPresenterImpl(this, getAuthenticatedUserUID());
    }

    @OnClick(R.id.btn_select_photo)
    public void selectPhoto() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent сhooserIntent = Intent.createChooser(getIntent, "Select Image");
        startActivityForResult(сhooserIntent, PICK_IMAGE);
    }

    /*private void selectManyPhotos() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image*//*");
        getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        Intent сhooserIntent = Intent.createChooser(getIntent, "Select Image");
        startActivityForResult(сhooserIntent, PICK_IMAGE);
    }*/

    private void addSendPostEnabledListener() {
        edtPostText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int count, int after) {
                String text = edtPostText.getText().toString();
                mNewPostPresenter.canPushPost(text);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_post, menu);
        menu.findItem(R.id.menu_send_post).setEnabled(false);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_send_post:
                mNewPostPresenter.pushPost(NewPostActivity.this, edtPostText.getText().toString());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            mMenu.findItem(R.id.menu_send_post).setEnabled(true);
            mNewPostPresenter.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNewPostPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNewPostPresenter.onPause();
    }

    @Override
    public void onCanPushPostChecked(boolean canPushPost) {
        mMenu.findItem(R.id.menu_send_post).setEnabled(canPushPost);
    }

    @Override
    public void onPostPushed() {
        Toast.makeText(this, "Post published", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String error, Exception e) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }
}
