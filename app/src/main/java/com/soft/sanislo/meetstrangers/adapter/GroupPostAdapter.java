package com.soft.sanislo.meetstrangers.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.soft.sanislo.meetstrangers.interfaces.PostClickListener;
import com.soft.sanislo.meetstrangers.model.Group;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.viewholders.GroupPostViewHolder;
import com.soft.sanislo.meetstrangers.viewholders.GroupViewHolder;

/**
 * Created by root on 04.12.16.
 */

public class GroupPostAdapter extends FirebaseRecyclerAdapter<Post, GroupPostViewHolder> {
    private Context mContext;
    private PostClickListener mPostClickListener;

    public GroupPostAdapter(Class<Post> modelClass, int modelLayout, Class<GroupPostViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    public GroupPostAdapter(Class<Post> modelClass, int modelLayout, Class<GroupPostViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    public void setPostClickListener(PostClickListener postClickListener) {
        mPostClickListener = postClickListener;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    @Override
    protected void populateViewHolder(GroupPostViewHolder viewHolder, Post model, int position) {
        viewHolder.populate(mContext, model, mPostClickListener, position);
    }
}
