package com.soft.sanislo.meetstrangers.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.activity.GroupEditActivity;
import com.soft.sanislo.meetstrangers.activity.NewPostActivity;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.Group;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.FirebaseUtils;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.GroupEditView;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by root on 04.12.16.
 */

public class GroupEditPresenterImpl implements GroupEditPresenter {
    public static final String TAG = GroupEditPresenterImpl.class.getSimpleName();
    private Context mContext;
    private GroupEditView mView;

    private GroupEditPresenter mGroupEditPresenter;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private String mAuthUID;
    private String mGroupKey;
    private Group mGroup;

    public GroupEditPresenterImpl(GroupEditActivity context, Intent intent) {
        mContext = context;
        mView = context;

        mAuthUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = FirebaseUtils.getStorageRef();
        mDatabaseReference = FirebaseUtils.getDatabaseReference();
        mGroupKey = intent.getStringExtra(GroupEditActivity.KEY_GROUP_KEY);
    }

    private ValueEventListener mGroupListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mGroup = dataSnapshot.getValue(Group.class);
            mView.onGroupDataChange(mGroup);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onClickGroupAvatar() {
        new MaterialDialog.Builder(mContext)
                .title("Choose photo from")
                .items(R.array.pick_photo)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Log.d(TAG, "onSelection: which: " + which);
                        switch (which) {
                            case 0:
                                onSelectedTakePhoto();
                                break;
                            case 1:
                                mView.onSelectGallery();
                                break;
                            default:
                                break;
                        }
                    }
                })
                .show();
    }

    private void onSelectedTakePhoto() {
        //mContext.makeToast("coming soon...");
    }

    @Override
    public void onClickGroupStatus() {
        new MaterialDialog.Builder(mContext)
                .title("Status")
                .content(mGroup.getStatus())
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("status...", mGroup.getStatus(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        updateGroupStatus(input.toString());
                    }
                }).show();
    }

    private void updateGroupStatus(String status) {
        mDatabaseReference.child(Constants.F_GROUPS)
                .child(mGroupKey)
                .child(Group.PROPERTY_GROUP_STATUS)
                .setValue(status);
    }

    @Override
    public void onClickGroupAction() {
        if (mGroup.isMember(mAuthUID)) {
            showDialogToLeaveGroup();
        } else {
            Log.d(TAG, "onClickGroupAction: joinGroup");
            joinGroup();
        }
    }

    private void showDialogToLeaveGroup() {
        new MaterialDialog.Builder(mContext)
                .content("Do You really want to leave this community?")
                .positiveText("yes")
                .negativeText("cancel")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        leaveGroup();
                    }
                })
                .show();
    }

    private void joinGroup() {
        mDatabaseReference.child(Constants.F_GROUPS)
                .child(mGroupKey)
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Group group = mutableData.getValue(Group.class);
                        if (group == null) return Transaction.success(mutableData);
                        group.addMember(mAuthUID);
                        mutableData.setValue(group);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        if (databaseError != null) {
                            databaseError.toException().printStackTrace();
                        }
                    }
                });
    }

    private void leaveGroup() {
        mDatabaseReference.child(Constants.F_GROUPS)
                .child(mGroupKey)
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Group group = mutableData.getValue(Group.class);
                        if (group == null) return Transaction.success(mutableData);
                        group.removeMember(mAuthUID);
                        mutableData.setValue(group);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        if (databaseError != null) {
                            databaseError.toException().printStackTrace();
                        }
                    }
                });
    }

    @Override
    public void onCLickNewGroupPost() {
        Intent intent = new Intent(mContext, NewPostActivity.class);
        intent.putExtra(NewPostActivity.KEY_GROUP_KEY, mGroupKey);
        mContext.startActivity(intent);
    }

    @Override
    public void onPause() {
        mDatabaseReference.child(Constants.F_GROUPS)
                .child(mGroupKey)
                .removeEventListener(mGroupListener);
    }

    @Override
    public void onResume() {
        mDatabaseReference.child(Constants.F_GROUPS)
                .child(mGroupKey)
                .addValueEventListener(mGroupListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.RC_PICK_IMAGE_GALLERY) {
            updateGroupAvatar(data);
        }
    }

    private void updateGroupAvatar(Intent data) {
        if (data == null) {
            return;
        }
        String fileName = mGroupKey + new Date().getTime() + ".jpg";
        UploadTask uploadTask = (UploadTask) mStorageReference.child(Constants.F_GROUPS)
                .child(mGroupKey)
                .child(fileName)
                .putFile(data.getData())
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String groupAvatarUrl = taskSnapshot.getDownloadUrl().toString();
                        HashMap<String, Object> updateMap = new HashMap<String, Object>();
                        updateMap.put(Group.PROPERTY_GROUP_AVATAR, groupAvatarUrl);
                        mDatabaseReference.child(Constants.F_GROUPS)
                                .child(mGroupKey)
                                .updateChildren(updateMap);
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        float progress = taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: " + taskSnapshot.getBytesTransferred() + " " + taskSnapshot.getTotalByteCount());
                        Log.d(TAG, "onProgress: progress: " + progress);
                    }
                });
    }
}
