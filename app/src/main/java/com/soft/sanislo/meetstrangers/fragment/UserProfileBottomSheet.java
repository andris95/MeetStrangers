package com.soft.sanislo.meetstrangers.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.FirebaseUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 25.01.17.
 */

public class UserProfileBottomSheet extends BottomSheetDialogFragment {
    private String TAG = UserProfileBottomSheet.class.getSimpleName();
    public static final String EXTRA_UID = "EXTRA_UID";

    @BindView(R.id.iv_user_avatar)
    ImageView ivUserAvatar;

    @BindView(R.id.tv_user_name)
    TextView tvUserName;

    private DatabaseReference mDatabaseReference;
    private String mUID;
    private User mUser;

    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_user_profile_bottom_sheet, null);
        ButterKnife.bind(this, contentView);
        dialog.setContentView(contentView);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseReference = FirebaseUtils.getDatabaseReference();
        mUID = getArguments().getString(EXTRA_UID);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDatabaseReference.child(Constants.F_USERS)
                .child(mUID)
                .addListenerForSingleValueEvent(mUserListener);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public static UserProfileBottomSheet newInstance(String uid) {
        Bundle args = new Bundle();
        args.putString(EXTRA_UID, uid);
        UserProfileBottomSheet fragment = new UserProfileBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    private ValueEventListener mUserListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            mUser = dataSnapshot.getValue(User.class);
            tvUserName.setText(mUser.getFullName());
            Glide.with(UserProfileBottomSheet.this)
                    .load(mUser.getAvatarURL())
                    .into(ivUserAvatar);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
}
