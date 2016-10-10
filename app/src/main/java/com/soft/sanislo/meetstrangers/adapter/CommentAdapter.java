package com.soft.sanislo.meetstrangers.adapter;

import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.view.CommentViewHolder;

/**
 * Created by root on 09.10.16.
 */
public class CommentAdapter extends FirebaseRecyclerAdapter<Comment, CommentViewHolder> {

    public CommentAdapter(Class<Comment> modelClass, int modelLayout, Class<CommentViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    public CommentAdapter(Class<Comment> modelClass, int modelLayout, Class<CommentViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(CommentViewHolder viewHolder, Comment model, int position) {
        viewHolder.populate(null, model, position, null);
    }

    public interface OnClickListener {
        void onClick(View view, int position, Comment comment);
    }
}
