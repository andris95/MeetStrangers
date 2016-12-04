package com.soft.sanislo.meetstrangers.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.view.UserViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by root on 10.10.16.
 */
public class UserAdapter extends FirebaseRecyclerAdapter<Boolean, UserViewHolder> {
    private static final String TAG = UserAdapter.class.getSimpleName();
    private OnClickListener mOnClickListener;
    private Context mContext;

    public UserAdapter(Class<Boolean> modelClass, int modelLayout, Class<UserViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    public UserAdapter(Class<Boolean> modelClass, int modelLayout, Class<UserViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(UserViewHolder viewHolder, Boolean model, int position) {
        String userKey = getRef(position).getKey();
        viewHolder.populate(mContext, userKey, mOnClickListener, position);
    }

    public interface OnClickListener {
        void onClick(View view, int position, User user);
    }

    public OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }
}
