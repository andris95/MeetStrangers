package com.soft.sanislo.meetstrangers.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.activity.NewPostActivity;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.FirebaseUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.NewPostView;

import org.w3c.dom.Text;

import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * Created by root on 01.11.16.
 */

public class NewPostPresenterImpl implements NewPostPresenter {
    public static final String TAG = NewPostPresenter.class.getSimpleName();

    private NewPostView mView;

    private DatabaseReference mDatabaseReference;
    private StorageReference mStorageReference;

    private String mUID;
    private User mUser;

    private Uri mPhotoUri;
    private Post.Builder mPostBuilder;
    private String mPostUID;

    private ValueEventListener mUserValueEventLisener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mUser = dataSnapshot.getValue(User.class);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            databaseError.toException().printStackTrace();
        }
    };

    public NewPostPresenterImpl(NewPostView view, String uid) {
        mView = view;
        mUID = uid;
        mDatabaseReference = FirebaseUtils.getDatabaseReference();
        mStorageReference = FirebaseUtils.getStorageRef();
    }

    @Override
    public void canPushPost(String postText) {
        boolean canPushPost = !TextUtils.isEmpty(postText) || mPhotoUri != null;
        mView.onCanPushPostChecked(canPushPost);
    }

    @Override
    public void pushPost(Context context, String postText) {
        buildNewPost(postText);
        if (mPhotoUri != null) {
            uploadPostPhoto(context);
        } else {
            sendPostJSONData();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == NewPostActivity.PICK_IMAGE) {
            if (data.getClipData() != null) {
                //fetchClipData(data.getClipData());
            } else {
                mPhotoUri = data.getData();
            }
        }
    }

    /** get paths for many selected photos */
    /*private void fetchClipData(ClipData clipData) {
        for (int i = 0; i < clipData.getItemCount(); i++) {
            Uri photoFileUri = clipData.getItemAt(i).getUri();
            mPhotoPathList.add(photoFileUri.toString());
        }
    }*/

    private void buildNewPost(String postText) {
        long timestamp = new Date().getTime();
        mPostUID = generatePostKey();

        mPostBuilder = new Post.Builder();
        mPostBuilder
                .setAuthorUID(mUID)
                .setAuthorName(mUser.getFullName())
                .setAuthorAvatarURL(mUser.getAvatarURL())
                .setPostUID(mPostUID)
                .setCommentsCount(0)
                .setLikesCount(0)
                .setDislikesCount(0)
                .setContent(postText)
                .setTimestamp(timestamp);
    }

    private String generatePostKey() {
        return mDatabaseReference.child(Constants.F_POSTS)
                .child(mUID)
                .push()
                .getKey();
    }

    private void sendPostJSONData() {
        Post post = mPostBuilder.build();
        mDatabaseReference.child(Constants.F_POSTS)
                .child(mUID)
                .child(post.getKey())
                .setValue(post, 0 - new Date().getTime())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: ");
                        mView.onPostPushed();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: ");
                e.printStackTrace();
                mView.onError(e.getMessage(), e);
            }
        });
    }

    private void uploadPostPhoto(Context context) {
        String photoFileName = Utils.getFileName(context, mPhotoUri);
        Log.d(TAG, "uploadPostPhotos: photoFileName: " + photoFileName);
        StorageReference photoReference = mStorageReference.child(Constants.F_POSTS)
                .child(mUID)
                .child(mPostUID)
                .child(photoFileName);
        UploadTask photoUploadTask = photoReference.putFile(mPhotoUri);
        photoUploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: ");
                String photoURL = taskSnapshot.getDownloadUrl().toString();
                Log.d(TAG, "onSuccess: photoURL: " + photoURL);
                mPostBuilder.setPhotoURL(photoURL);
                sendPostJSONData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: ");
                e.printStackTrace();
                mView.onError(e.getMessage(), e);
            }
        });
    }

    @Override
    public void onResume() {
        mDatabaseReference.child(Constants.F_USERS)
                .child(mUID)
                .addValueEventListener(mUserValueEventLisener);
    }

    @Override
    public void onPause() {
        mDatabaseReference.child(Constants.F_USERS)
                .child(mUID)
                .removeEventListener(mUserValueEventLisener);
    }
}
