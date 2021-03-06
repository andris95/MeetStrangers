package com.soft.sanislo.meetstrangers.adapter;

import android.content.Context;
import android.util.Log;
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
    private static final String TAG = CommentAdapter.class.getSimpleName();
    private OnClickListener mOnClickListener;
    private Context mContext;
    private int mExpandedPos = -1;
    private String mAuthUID;

    public CommentAdapter(Class<Comment> modelClass, int modelLayout, Class<CommentViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    public CommentAdapter(Class<Comment> modelClass, int modelLayout, Class<CommentViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(CommentViewHolder viewHolder, Comment model, int position) {
        viewHolder.setExpanded(getExpandedPos() == position);
        viewHolder.setAuthUID(mAuthUID);
        Log.d(TAG, "populateViewHolder: " + mAuthUID);
        viewHolder.populate(mContext, model, position, mOnClickListener);
    }

    public int getExpandedPos() {
        return mExpandedPos;
    }

    public void setExpandedPos(int expandedPos) {
        mExpandedPos = expandedPos;
    }

    public void setAuthUID(String authUID) {
        mAuthUID = authUID;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public interface OnClickListener {
        void onClick(View view, int position, Comment comment);
        void onClickLikeComment(Comment comment);
    }
}
