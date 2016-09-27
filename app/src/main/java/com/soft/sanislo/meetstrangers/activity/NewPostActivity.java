package com.soft.sanislo.meetstrangers.activity;

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
import com.roughike.bottombar.BottomBarTab;
import com.roughike.bottombar.OnTabSelectListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;

import java.io.InputStream;
import java.util.Date;

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
    private DatabaseReference postReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private User user;
    private String uid;

    private Menu mMenu;
    private String mPhotoPath;

    private ValueEventListener userValueEventLisener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            user = dataSnapshot.getValue(User.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

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

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

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
                if (mPhotoPath == null) {
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
            mPhotoPath = data.getData().toString();
            mMenu.findItem(R.id.menu_send_post).setEnabled(true);
            Log.d(TAG, "onActivityResult: mPhotoPath: " + mPhotoPath);
        }
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode + " resultCode: " + resultCode);
    }

    private void sendPost() {
        progressBar.setVisibility(View.VISIBLE);
        String postText = edtPostText.getText().toString();
        String postKey = mDatabaseReference.child(Constants.F_POSTS).child(uid).push().getKey();

        Post post = new Post(postText, uid, postKey, new Date().getTime());
        post.setAuthFullName(user.getFullName());
        post.setAuthorAvatarURL(user.getAvatarURL());

        if (mPhotoPath != null) {
            sendPostWithPhoto(post);
        } else {
            sendPostJSONData(post);
        }
    }

    private OnCompleteListener<Void> postCompleteListener = new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
            Toast.makeText(getApplicationContext(), "Posted successfully", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            finish();
        }
    };
    private OnFailureListener postFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Posting failed, please try again later...", Toast.LENGTH_SHORT).show();
        }
    };

    private void sendPostJSONData(Post post) {
        mDatabaseReference.child(Constants.F_POSTS).child(uid).child(post.getPostID()).setValue(post)
                .addOnCompleteListener(postCompleteListener).addOnFailureListener(postFailureListener);
    }

    private void sendPostWithPhoto(final Post post) {
        StorageReference storageRef = mStorage.getReferenceFromUrl(Constants.STORAGE_BUCKET);
        StorageReference postPhotoRef = storageRef.child(Constants.F_POSTS).child(uid)
                .child(post.getPostID() + ".jpg");
        Log.d(TAG, "sendPost: postPhotoRef path: " + postPhotoRef.getPath());

        UploadTask uploadTask = postPhotoRef.putFile(Uri.parse(mPhotoPath));
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                post.setPhotoURL(taskSnapshot.getDownloadUrl().toString());
                sendPostJSONData(post);
            }
        }).addOnFailureListener(postFailureListener);
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onProgress: " + taskSnapshot.getBytesTransferred() + " / " + taskSnapshot.getTotalByteCount());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDatabaseReference.child(Constants.F_USERS).child(uid).addValueEventListener(userValueEventLisener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatabaseReference.child(Constants.F_USERS).child(uid).removeEventListener(userValueEventLisener);
    }
}
