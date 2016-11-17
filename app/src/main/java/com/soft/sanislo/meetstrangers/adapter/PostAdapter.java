package com.soft.sanislo.meetstrangers.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.view.PostViewHolder;

/**
 * Created by root on 26.09.16.
 */
public class PostAdapter extends FirebaseRecyclerAdapter<Post, PostViewHolder> {
    private static final String TAG = PostAdapter.class.getSimpleName();
    private OnClickListener mOnClickListener;
    private LoadMoreListener mLoadMoreListener;
    private Context mContext;
    private String mAuthUserUID;
    private int mCommentsVisiblePos = RecyclerView.NO_POSITION;

    public PostAdapter(Context context, Class<Post> modelClass, int modelLayout,
                       Class<PostViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mContext = context;
    }

    public PostAdapter(Context context, Class<Post> modelClass, int modelLayout,
                       Class<PostViewHolder> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
        mContext = context;
    }

    @Override
    protected void populateViewHolder(PostViewHolder viewHolder, Post model, int position) {
        viewHolder.setExpanded(position == mCommentsVisiblePos);
        viewHolder.setAuthUID(mAuthUserUID);
        viewHolder.populate(mContext, model, mAuthUserUID, mOnClickListener, position);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;

    }

    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        mLoadMoreListener = loadMoreListener;
    }

    public interface OnClickListener {
        void onClick(View view, int position, Post post);
        void onClickAddComment(Post post, String commentText);
        void onClickCancelComment();
        void onClickHighlightComment();
        void onClickLikeComment(Comment comment);
    }

    public OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    public String getAuthUserUID() {
        return mAuthUserUID;
    }

    public void setAuthUserUID(String authUserUID) {
        mAuthUserUID = authUserUID;
    }

    public int getCommentsVisiblePos() {
        return mCommentsVisiblePos;
    }

    public void setCommentsVisiblePos(int commentsVisiblePos) {
        mCommentsVisiblePos = commentsVisiblePos;
    }
}
