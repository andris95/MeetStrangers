package com.soft.sanislo.meetstrangers;

import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.view.PostViewHolder;

/**
 * Created by root on 26.09.16.
 */
public class PostAdapter extends FirebaseRecyclerAdapter<Post, PostViewHolder> {
    private OnClickListener mOnClickListener;

    public PostAdapter(Class<Post> modelClass, int modelLayout,
                       Class<PostViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    public PostAdapter(Class<Post> modelClass, int modelLayout,
                       Class<PostViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(PostViewHolder viewHolder, Post model, int position) {
        viewHolder.populate(model, mOnClickListener, position);
    }

    public OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(View view, int position);
    }
}
