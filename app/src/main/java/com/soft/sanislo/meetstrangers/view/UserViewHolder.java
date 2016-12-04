package com.soft.sanislo.meetstrangers.view;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.UserAdapter;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by root on 10.10.16.
 */
public class UserViewHolder extends RecyclerView.ViewHolder {
    private View mRootView;
    private UserAdapter.OnClickListener mOnClickListener;
    private String mUserUID;
    private User mUser;
    private Context mContext;
    private DisplayImageOptions displayImageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .showImageOnLoading(R.drawable.placeholder)
            .build();
    private ImageLoader imageLoader = ImageLoader.getInstance();

    @BindView(R.id.iv_user_avatar)
    ImageView ivUserAvatar;

    @BindView(R.id.tv_user_name)
    TextView tvUserName;
    private int mPosition;

    public UserViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
        ButterKnife.bind(this, mRootView);
    }

    public void populate(Context context,
                         String userUID,
                         final UserAdapter.OnClickListener onClickListener,
                         final int position) {
        mContext = context;
        mUserUID = userUID;
        mPosition = position;
        mOnClickListener = onClickListener;
        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            ivUserAvatar.setTransitionName(mContext.getString(R.string.transition_user_avatar));
        }
        Utils.getDatabase().getReference()
                .child(Constants.F_USERS)
                .child(mUserUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mUser = dataSnapshot.getValue(User.class);
                        setUserName();
                        setUserAvatar();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(view, position, mUser);
                }
            }
        });
    }

    private void setUserName() {
        tvUserName.setText(mUser.getFullName());
    }

    public void setUserAvatar() {
        imageLoader.displayImage(mUser.getAvatarURL(), ivUserAvatar,
                displayImageOptions);
    }
}
