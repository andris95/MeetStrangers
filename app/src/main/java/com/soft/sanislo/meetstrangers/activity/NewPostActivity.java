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
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.MediaFile;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.ImageUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;

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
public class NewPostActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.edt_post_text)
    EditText edtPostText;

    @BindView(R.id.pb_new_post)
    MaterialProgressBar progressBar;

    @BindView(R.id.btn_add_photos)
    Button btnAddPhotos;

    private static final String TAG = NewPostActivity.class.getSimpleName();
    private static final int PICK_IMAGE = 30000;

    private DatabaseReference mDatabaseReference = Utils.getDatabase().getReference();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private StorageReference storageRef = mStorage.getReferenceFromUrl(Constants.STORAGE_BUCKET);
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User user;
    private String mAuthUID;

    private Menu mMenu;
    private ArrayList<String> mPhotoPathList = new ArrayList<>();
    private Queue<String> mTempPhotoPathQueue;
    private ArrayList<MediaFile> mMediaFiles;

    private Post newPost;

    private ValueEventListener userValueEventLisener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            user = dataSnapshot.getValue(User.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            databaseError.toException().printStackTrace();
        }
    };
    private int mCurrentMediaIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        mAuthUID = firebaseUser.getUid();

        addSendPostEnabledListener();
    }

    @OnClick(R.id.btn_add_photos)
    public void onClickAddPhotos() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        Intent сhooserIntent = Intent.createChooser(getIntent, "Select Image");
        startActivityForResult(сhooserIntent, PICK_IMAGE);
    }

    private void addSendPostEnabledListener() {
        edtPostText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int count, int after) {
                String text = edtPostText.getText().toString();
                if (TextUtils.isEmpty(text) && mPhotoPathList.isEmpty()) {
                    mMenu.findItem(R.id.menu_send_post).setEnabled(false);
                } else {
                    mMenu.findItem(R.id.menu_send_post).setEnabled(true);
                }
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
                sendPost();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            if (data.getClipData() != null) {
                fetchClipData(data.getClipData());
            } else {
                mPhotoPathList.add(data.getData().toString());
            }
            mMenu.findItem(R.id.menu_send_post).setEnabled(true);
        }
    }

    private void fetchClipData(ClipData clipData) {
        for (int i = 0; i < clipData.getItemCount(); i++) {
            Uri photoFileUri = clipData.getItemAt(i).getUri();
            mPhotoPathList.add(photoFileUri.toString());
        }
    }

    private void sendPost() {
        progressBar.setVisibility(View.VISIBLE);
        buildNewPost();
        if (!mPhotoPathList.isEmpty()) {
            uploadPostPhotos();
        } else {
            sendPostJSONData();
        }
    }

    private void buildNewPost() {
        long timestamp = new Date().getTime();
        String postText = edtPostText.getText().toString();
        String postKey = mDatabaseReference.child(Constants.F_POSTS)
                .child(mAuthUID)
                .push()
                .getKey();

        newPost = new Post();
        newPost.setKey(postKey);
        newPost.setAuthorUID(user.getUid());
        newPost.setAuthFullName(user.getFullName());
        newPost.setAuthorAvatarURL(user.getAvatarURL());
        newPost.setLikesCount(0);
        newPost.setCommentsCount(0);
        newPost.setText(postText);
        newPost.setTimestamp(timestamp);
    }

    private OnCompleteListener<Void> getPostCompleteListener() {
        OnCompleteListener<Void> postCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE);
                finish();
            }
        };
        return postCompleteListener;
    }

    private OnFailureListener postFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            progressBar.setVisibility(View.GONE);
            e.printStackTrace();
            makeToast("Posting failed, please try again later...");
        }
    };

    private void sendPostJSONData() {
        mDatabaseReference.child(Constants.F_POSTS)
                .child(mAuthUID)
                .child(newPost.getKey())
                .setValue(newPost, 0 - new Date().getTime())
                .addOnCompleteListener(getPostCompleteListener())
                .addOnFailureListener(postFailureListener);
    }

    private void uploadPostPhotos() {
        mTempPhotoPathQueue = new LinkedList<>();
        mTempPhotoPathQueue.addAll(mPhotoPathList);
        mMediaFiles = new ArrayList<>();
        for (String photoFilePath : mPhotoPathList) {
            MediaFile mediaFile = ImageUtils.getPhotoSize(getApplicationContext(), photoFilePath);
            mMediaFiles.add(mediaFile);
            Log.d(TAG, "uploadPostPhotos: " + mediaFile);
        }
        mCurrentMediaIndex = 0;
        uploadNextPhotoTask();
    }

    private void uploadNextPhotoTask() {
        if (mTempPhotoPathQueue.isEmpty()) {
            newPost.setMediaFiles(mMediaFiles);
            sendPostJSONData();
        } else {
            String photoFileName = Utils.getFileName(getApplicationContext(),
                    Uri.parse(mTempPhotoPathQueue.peek()));

            StorageReference postPhotoRef = storageRef.child(Constants.F_POSTS)
                    .child(mAuthUID)
                    .child(newPost.getKey())
                    .child(photoFileName + ".jpg");
            Uri photoUri = Uri.parse(mTempPhotoPathQueue.remove());
            UploadTask uploadTask = postPhotoRef.putFile(photoUri);
            uploadTask.addOnSuccessListener(photoUploadSuccessListener)
                    .addOnProgressListener(photoUploadProgressListener)
                    .addOnFailureListener(photoUploadFailureListener);
        }
    }

    private OnSuccessListener<UploadTask.TaskSnapshot> photoUploadSuccessListener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            mMediaFiles.get(mCurrentMediaIndex).setUrl(taskSnapshot.getDownloadUrl().toString());
            mCurrentMediaIndex++;
            uploadNextPhotoTask();
        }
    };

    private OnProgressListener<UploadTask.TaskSnapshot> photoUploadProgressListener = new OnProgressListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
            Log.d(TAG, "onProgress: " + taskSnapshot.getBytesTransferred() / (float) taskSnapshot.getTotalByteCount());
        }
    };

    private OnFailureListener photoUploadFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            e.printStackTrace();
            makeToast(e.getMessage());
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mDatabaseReference.child(Constants.F_USERS).child(mAuthUID)
                .addValueEventListener(userValueEventLisener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatabaseReference.child(Constants.F_USERS).child(mAuthUID)
                .removeEventListener(userValueEventLisener);
    }
}
