package com.soft.sanislo.meetstrangers.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by root on 27.09.16.
 */
public class ProfileEditActivity extends BaseActivity {
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

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mUserStorageRef;
    private DatabaseReference mUserDatabaseRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User mUser;
    private String uid;
    private String avatarURL;
    private String mAvatarPhotoPath;
    private boolean mNeedToUploadAvatar;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            /*.showImageOnLoading(R.drawable.ic_stub) // resource or drawable
            .showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
            .showImageOnFail(R.drawable.ic_error) // resource or drawable
            */.build();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private ImageLoadingProgressListener progressListener;

    private ValueEventListener mUserValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mUser = dataSnapshot.getValue(User.class);
            imageLoader.displayImage(mUser.getAvatarURL(), ivAvatar, displayImageOptions);
            edtFirstName.setText(mUser.getFirstName());
            edtLastName.setText(mUser.getLastName());
            if (mUser.getGender() == 0) {
                rgGender.check(R.id.rb_gender_male);
            } else {
                rgGender.check(R.id.rb_gender_female);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private OnFailureListener mAvatarUploadFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            e.printStackTrace();
            makeToast("Uploading image failed, please try again later...");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();

        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.F_USERS).child(uid);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mUserStorageRef = mFirebaseStorage.getReferenceFromUrl(Constants.STORAGE_BUCKET)
                .child(Constants.F_USERS);
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
                confirmProfileEdit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void confirmProfileEdit() {
        String firstName = edtFirstName.getText().toString();
        String lastName = edtLastName.getText().toString();
        if (!TextUtils.isEmpty(firstName)) {
            mUser.setFirstName(firstName);
            mUser.setFullName(firstName + " " + mUser.getLastName());
        }
        if (!TextUtils.isEmpty(lastName)) {
            mUser.setLastName(lastName);
            mUser.setFullName(mUser.getFirstName() + " " + lastName);
        }
        updateUser();
    }

    private void uploadUserAvatar() {
        Log.d(TAG, "uploadUserAvatar: mUserStorageRef.getPath(): " + mUserStorageRef.getPath());

        UploadTask uploadTask = mUserStorageRef.putFile(Uri.parse(mAvatarPhotoPath));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mUser.setAvatarURL(taskSnapshot.getDownloadUrl().toString());
                updateUserData();
            }
        }).addOnFailureListener(mAvatarUploadFailureListener);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onProgress: " + taskSnapshot.getBytesTransferred() + " / " + taskSnapshot.getTotalByteCount());
            }
        });
    }

    private void updateUserData() {
        mUserDatabaseRef.setValue(mUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: updateUser");
                mNeedToUploadAvatar = false;
            }
        });
    }

    private void updateUser() {
        if (!TextUtils.isEmpty(mAvatarPhotoPath) && mNeedToUploadAvatar) {
            uploadUserAvatar();
            return;
        }
        updateUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUserDatabaseRef.addValueEventListener(mUserValueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mUserDatabaseRef.removeEventListener(mUserValueEventListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == Constants.RC_PICK_IMAGE_GALLERY) {
            if (data == null) {
                Log.d(TAG, "onActivityResult: data null");
                Toast.makeText(getApplicationContext(), "Error choosing photo", Toast.LENGTH_SHORT).show();
                return;
            }
            imageLoader.displayImage(data.getData().toString(), ivAvatar, displayImageOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    Log.d(TAG, "onLoadingFailed: " + failReason.toString());
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });
            mAvatarPhotoPath = data.getData().toString();
            mNeedToUploadAvatar = true;
            Log.d(TAG, "onActivityResult: mAvatarPhotoPath: " + mAvatarPhotoPath);
        }
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode + " resultCode: " + resultCode);
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
                                makeToast(text.toString());
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
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, Constants.RC_PICK_IMAGE_GALLERY);
    }
}
