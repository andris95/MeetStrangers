package com.soft.sanislo.meetstrangers.activity;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Created by root on 24.09.16.
 */
public class NewPostActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.bottom_bar_new_post)
    BottomBar mBottomBar;
    @BindView(R.id.edt_post_text)
    EditText edtPostText;
    @BindView(R.id.pb_new_post)
    MaterialProgressBar progressBar;

    private static final String TAG = NewPostActivity.class.getSimpleName();
    private static final int PICK_IMAGE = 30000;

    private DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private StorageReference storageRef = mStorage.getReferenceFromUrl(Constants.STORAGE_BUCKET);
    private DatabaseReference postReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User user;
    private String uid;

    private Menu mMenu;
    //private String mPhotoPath;
    private ArrayList<String> mPhotoPathList = new ArrayList<>();
    private Queue<String> mTempPhotoPathQueue;
    private ArrayList<String> postPhotoURLList;
    private MaterialDialog postUploadProgressDialog;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        uid = firebaseUser.getUid();
        postReference = mDatabaseReference.child(Constants.F_POSTS).child(uid);

        addSendPostEnabledListener();
        initBottomBar();
    }

    private void initBottomBar() {
        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_camera) {
                    onSelectedCamera();
                }
            }
        });
    }

    private void onSelectedCamera() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        getIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        /*Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        pickIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);*/

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        //chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
        //chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {getIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    private void addSendPostEnabledListener() {
        edtPostText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int count, int after) {
                if (!mPhotoPathList.isEmpty()) {
                    if (!edtPostText.getText().toString().equals("")) {
                        mMenu.findItem(R.id.menu_send_post).setEnabled(true);
                    } else {
                        mMenu.findItem(R.id.menu_send_post).setEnabled(false);
                    }
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
            if (data == null) {
                Log.d(TAG, "onActivityResult: data null");
                Toast.makeText(getApplicationContext(), "Error choosing photo", Toast.LENGTH_SHORT).show();
                return;
            }
            if (data.getClipData() != null) {
                fetchClipData(data.getClipData());
            } else {
                mPhotoPathList.add(data.getData().toString());
            }
            mMenu.findItem(R.id.menu_send_post).setEnabled(true);
        }
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode + " resultCode: " + resultCode);
    }

    private void fetchClipData(ClipData clipData) {
        for (int i = 0; i < clipData.getItemCount(); i++) {
            Uri photoFileUri = clipData.getItemAt(i).getUri();
            mPhotoPathList.add(photoFileUri.toString());
            Log.d(TAG, "onActivityResult: photoFilePath: " + photoFileUri.toString());
        }
    }

    private void sendPost() {
        progressBar.setVisibility(View.VISIBLE);
        String postText = edtPostText.getText().toString();
        String postKey = mDatabaseReference.child(Constants.F_POSTS).child(uid).push().getKey();

        newPost = new Post(postText, uid, postKey, new Date().getTime());
        newPost.setAuthFullName(user.getFullName());
        newPost.setAuthorAvatarURL(user.getAvatarURL());

        if (!mPhotoPathList.isEmpty()) {
            uploadPostPhotos();
        } else {
            sendPostJSONData();
        }
    }

    private OnCompleteListener<Void> getPostCompleteListener() {
        OnCompleteListener<Void> postCompleteListener = new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getApplicationContext(), "Posted successfully", Toast.LENGTH_SHORT).show();
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
                .child(uid)
                .child(newPost.getPostID())
                .setValue(newPost)
                .addOnCompleteListener(getPostCompleteListener())
                .addOnFailureListener(postFailureListener);
    }

    private void uploadPostPhotos() {
        if (!mPhotoPathList.isEmpty()) {
            mTempPhotoPathQueue = new LinkedList<>();
            mTempPhotoPathQueue.addAll(mPhotoPathList);
            postPhotoURLList = new ArrayList<>();
            uploadNextPhotoTask();
        }
    }

    private void uploadNextPhotoTask() {
        if (mTempPhotoPathQueue.isEmpty()) {
            newPost.setPhotoURLList(postPhotoURLList);
            sendPostJSONData();
        } else {
            String photoFileName = Utils.getFileName(getApplicationContext(),
                    Uri.parse(mTempPhotoPathQueue.peek()));

            StorageReference postPhotoRef = storageRef.child(Constants.F_POSTS)
                    .child(uid)
                    .child(newPost.getPostID())
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
            postPhotoURLList.add(taskSnapshot.getDownloadUrl().toString());
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
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mDatabaseReference.child(Constants.F_USERS).child(uid)
                .addValueEventListener(userValueEventLisener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatabaseReference.child(Constants.F_USERS).child(uid).removeEventListener(userValueEventLisener);
    }
}
