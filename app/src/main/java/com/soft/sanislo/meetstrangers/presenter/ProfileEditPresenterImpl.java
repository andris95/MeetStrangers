package com.soft.sanislo.meetstrangers.presenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.BlurBuilder;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.FirebaseUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.ProfileEditView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import static android.app.Activity.RESULT_OK;

/**
 * Created by root on 21.01.17.
 */

public class ProfileEditPresenterImpl implements ProfileEditPresenter {
    private String TAG = ProfileEditPresenter.class.getSimpleName();

    private ProfileEditView mView;
    private String mUID;
    private User mUser;

    private DatabaseReference mDatabaseReference;
    private Context mContext;
    private Uri mAvatarUri;

    private HashMap<String, Object> toUpdate = new HashMap<>();

    public ProfileEditPresenterImpl(ProfileEditView view) {
        mView = view;
        mUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabaseReference = FirebaseUtils.getDatabaseReference();
    }

    private ValueEventListener mUserValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mUser = dataSnapshot.getValue(User.class);
            mView.onUserChanged(mUser);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void confirmEdit(Context context, String firstName, String lastName) {
        mContext = context;
        boolean isValid = Utils.isValidName(firstName, lastName);
        if (isValid) {
            toUpdate = new HashMap<>();
            String newFullName = firstName + " " + lastName;
            if (!mUser.getFullName().equals(newFullName)) {
                mUser.setFirstName(firstName);
                mUser.setLastName(lastName);
                mUser.setFullName(newFullName);
            }
        }
        updateUser();
    }

    @Override
    public void onResume() {
        mDatabaseReference.child(Constants.F_USERS)
                .child(mUID)
                .addListenerForSingleValueEvent(mUserValueEventListener);
    }

    @Override
    public void onPause() {
        mDatabaseReference.child(Constants.F_USERS)
                .child(mUID)
                .removeEventListener(mUserValueEventListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == Constants.RC_PICK_IMAGE_GALLERY) {
            if (data != null) {
                mAvatarUri = data.getData();
            }
        }
    }

    private void uploadUserAvatar() {
        StorageReference storageReference = FirebaseUtils.getStorageRef();
        final StorageReference avatarRef = storageReference
                .child(Constants.F_USERS)
                .child(mUID)
                .child(Constants.STORAGE_PHOTO_ALBUMS)
                .child(Constants.STORAGE_ALBUM_PROFILE_PHOTOS)
                .child(getAvatarFileName());

        UploadTask uploadTask = avatarRef.putFile(mAvatarUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String avatarURL = taskSnapshot.getDownloadUrl().toString();
                mUser.setAvatarURL(avatarURL);
                updateUserData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: ");
                e.printStackTrace();
            }
        });
    }

    private String getAvatarFileName() {
        return mUID + "_" + new Date().getTime();
    }

    private void uploadBlurAvatar() {
        String bitmapToBlurPath = Utils.getPath(mContext, mAvatarUri);
        Bitmap bitmapToBlur = BitmapFactory.decodeFile(bitmapToBlurPath);
        Bitmap blurredBitmap = BlurBuilder.blur(mContext, bitmapToBlur);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        blurredBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

        StorageReference blurPhotoRef = FirebaseUtils.getStorageRef()
                .child(Constants.F_USERS)
                .child(mUID)
                .child(Constants.STORAGE_PHOTO_ALBUMS)
                .child(mUID + "_blur");

        UploadTask uploadTask = blurPhotoRef
                .putStream(bs);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: blur");
                String blurURL = taskSnapshot.getDownloadUrl().toString();
                mUser.setAvatarBlurURL(blurURL);
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
        toUpdate.put("/users/" + mUID + "/fullName", mUser.getFullName());
        toUpdate.put("/users/" + mUID + "/firstName", mUser.getFirstName());
        toUpdate.put("/users/" + mUID + "/lastName", mUser.getLastName());
        toUpdate.put("/users/" + mUID + "/avatarURL", mUser.getAvatarURL());
        toUpdate.put("/users/" + mUID + "/avatarBlurURL", mUser.getAvatarBlurURL());
        doPostUpdates();
    }

    private void doPostUpdates() {
        mDatabaseReference.child(Constants.F_POSTS)
                .child(mUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        toUpdate.putAll(getPostUpdateMap(dataSnapshot));
                        Set<String> keySet = toUpdate.keySet();
                        for (String k : keySet) {
                            Log.d(TAG, "onDataChange: k: " + k + " v: " + toUpdate.get(k));
                        }
                        mDatabaseReference.updateChildren(toUpdate, onUpdateCompleteListener);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /** updates posts at /posts/{uid}/ */
    private HashMap<String, Object> getPostUpdateMap(DataSnapshot dataSnapshot) {
        HashMap<String, Object> postUpdateMap = new HashMap<>();
        Iterable<DataSnapshot> data = dataSnapshot.getChildren();
        for (DataSnapshot snapShot : data) {
            /** /posts/{muid}/{postuid}/authorName */
            String key = snapShot.getKey();
            String path = "/posts/" + mUID + "/" + key;
            postUpdateMap.put(path + "/authorName", mUser.getFullName());
            postUpdateMap.put(path + "/authorAvatarURL", mUser.getAvatarURL());
        }
        return postUpdateMap;
    }

    private DatabaseReference.CompletionListener onUpdateCompleteListener = new DatabaseReference.CompletionListener() {
        @Override
        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            Log.d(TAG, "onComplete: ");
            if (databaseError != null) {
                String errorMessage = databaseError.getMessage();
                if (!TextUtils.isEmpty(errorMessage)) {
                    mView.onError(errorMessage, null);
                }
            } else {
                mView.onProfileUpdated();
            }
            mAvatarUri = null;
        }
    };

    private void updateUser() {
        if (mAvatarUri != null) {
            uploadBlurAvatar();
        } else {
            updateUserData();
        }
    }
}
