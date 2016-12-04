package com.soft.sanislo.meetstrangers.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.soft.sanislo.meetstrangers.utilities.BlurBuilder;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;

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

    @BindView(R.id.pb_user_avatar)
    MaterialProgressBar pbUserAvatar;

    private static final String TAG = ProfileEditActivity.class.getSimpleName();

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mUserStorageRef;
    private DatabaseReference mUserDatabaseRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User mUser;
    private String uid;
    private String mAvatarPhotoPath;
    private boolean mNeedToUploadAvatar;

    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            /*.showImageOnLoading(R.drawable.ic_stub) // resource or drawable
            .showImageForEmptyUri(R.drawable.ic_empty) // resource or drawable
            .showImageOnFail(R.drawable.ic_error) // resource or drawable
            */.build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

    private ValueEventListener mUserValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mUser = dataSnapshot.getValue(User.class);
            displayUserAvatar(mUser.getAvatarURL());
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

        mUserDatabaseRef = Utils.getDatabase().getReference()
                .child(Constants.F_USERS).child(uid);
        mFirebaseStorage = FirebaseStorage.getInstance();
        mUserStorageRef = mFirebaseStorage.getReferenceFromUrl(Constants.STORAGE_BUCKET);
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

        UploadTask uploadTask = mUserStorageRef
                .child(uid)
                .child(Constants.STORAGE_PHOTO_ALBUMS)
                .child(Constants.STORAGE_ALBUM_PROFILE_PHOTOS)
                .child(uid + "_profile.jpg")
                .putFile(Uri.parse(mAvatarPhotoPath));
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

    private void uploadBlurAvatar() {
        String bitmapToBlurPath = Utils.getPath(getApplicationContext(), Uri.parse(mAvatarPhotoPath));
        Bitmap bitmapToBlur = BitmapFactory.decodeFile(bitmapToBlurPath);
        Bitmap blurredBitmap = BlurBuilder.blur(getApplicationContext(), bitmapToBlur);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        blurredBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

        UploadTask uploadTask = mUserStorageRef
                .child(uid)
                .child(Constants.STORAGE_PHOTO_ALBUMS)
                .child(Constants.STORAGE_ALBUM_PROFILE_PHOTOS)
                .child(uid + "_blur.jpg")
                .putStream(bs);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: blur");
                mUser.setAvatarBlurURL(taskSnapshot.getDownloadUrl().toString());
                uploadUserAvatar();
            }
        });
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: blur");
                e.printStackTrace();
            }
        });
    }


    /** Updates the information about user under users node*/
    private void updateUserData() {
        mUserDatabaseRef.setValue(mUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "onComplete: updateUser");
                mNeedToUploadAvatar = false;
                updatePostsDataAboutUser();
            }
        });
    }

    /** Updates the information about user under posts node*/
    private void updatePostsDataAboutUser() {
        final HashMap<String, Object> toUpdate = new HashMap<String, Object>();
        toUpdate.put("authFullName", mUser.getFullName());
        toUpdate.put("authorAvatarURL", mUser.getAvatarURL());

        Utils.getDatabase().getReference().child(Constants.F_POSTS).child(uid)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        final String key = dataSnapshot.getKey();
                        Log.d(TAG, "onChildAdded: post key: " + key);
                        Utils.getDatabase().getReference().child(Constants.F_POSTS)
                                .child(uid).child(key).updateChildren(toUpdate);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void updateUser() {
        if (!TextUtils.isEmpty(mAvatarPhotoPath) && mNeedToUploadAvatar) {
            uploadBlurAvatar();
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
                makeToast("Error choosing photo");
                return;
            }
            mAvatarPhotoPath = data.getData().toString();
            mNeedToUploadAvatar = true;
            displayUserAvatar(mAvatarPhotoPath);
        }
    }

    private void displayUserAvatar(String url) {
        imageLoader.displayImage(url, ivAvatar, displayImageOptions, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                pbUserAvatar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                Log.d(TAG, "onLoadingFailed: " + failReason.toString());
                pbUserAvatar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                pbUserAvatar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
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
