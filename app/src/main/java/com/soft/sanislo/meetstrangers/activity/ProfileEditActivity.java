package com.soft.sanislo.meetstrangers.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.presenter.ProfileEditPresenter;
import com.soft.sanislo.meetstrangers.presenter.ProfileEditPresenterImpl;
import com.soft.sanislo.meetstrangers.utilities.BlurBuilder;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.FirebaseUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.ProfileEditView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Created by root on 27.09.16.
 */
public class ProfileEditActivity extends BaseActivity implements ProfileEditView {
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.iv_avatar)
    ImageView ivAvatar;

    @BindView(R.id.rg_gender)
    RadioGroup rgGender;

    @BindView(R.id.edt_first_name)
    EditText edtFirstName;

    @BindView(R.id.edt_last_name)
    EditText edtLastName;

    private static final String TAG = ProfileEditActivity.class.getSimpleName();

    private ProfileEditPresenter mProfileEditPresenter;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder().build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mProfileEditPresenter = new ProfileEditPresenterImpl(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_confirm_edit:
                mProfileEditPresenter.confirmEdit(ProfileEditActivity.this,
                        edtFirstName.getText().toString(),
                        edtLastName.getText().toString());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProfileEditPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mProfileEditPresenter.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mProfileEditPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.RC_PICK_IMAGE_GALLERY) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectPhoto();
            } else {
                // showRationale = false if user clicks Never Ask Again, otherwise true
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(ProfileEditActivity.this,
                        Manifest.permission.READ_CONTACTS);
                if (showRationale) {
                    // do something here to handle degraded mode
                    Toast.makeText(ProfileEditActivity.this, "should show rationale...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Read External Storage permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void displayUserAvatar(String url) {
        imageLoader.displayImage(url, ivAvatar, displayImageOptions);
    }

    @OnClick(R.id.iv_avatar)
    public void onClickAvatar() {
        new MaterialDialog.Builder(this)
                .title("Choose photo from")
                .items(R.array.pick_photo)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Log.d(TAG, "onSelection: which: " + which);
                        switch (which) {
                            case 0:
                                makeToast(text.toString());
                                break;
                            case 1:
                                onSelectedGallery();
                                break;
                            default:
                                break;
                        }
                    }
                })
                .show();
    }

    private void onSelectedGallery() {
        if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            selectPhoto();
        } else {
            ActivityCompat.requestPermissions(ProfileEditActivity.this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                    Constants.RC_PICK_IMAGE_GALLERY);
        }
    }

    private void selectPhoto() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent сhooserIntent = Intent.createChooser(getIntent, "Select Image");
        startActivityForResult(сhooserIntent, Constants.RC_PICK_IMAGE_GALLERY);
    }

    private boolean hasPermission(String permission) { ///Manifest.permission.READ_CONTACTS
        return ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onUserChanged(User user) {
        displayUserAvatar(user.getAvatarURL());
        edtFirstName.setText(user.getFirstName());
        edtLastName.setText(user.getLastName());
        if (user.getGender() == 0) {
            rgGender.check(R.id.rb_gender_male);
        } else {
            rgGender.check(R.id.rb_gender_female);
        }
    }

    @Override
    public void onProfileUpdated() {
        makeToast("Your profile is successfully updated!");
    }

    @Override
    public void onError(String errorMessage, Exception e) {
        makeToast(errorMessage);
    }
}
