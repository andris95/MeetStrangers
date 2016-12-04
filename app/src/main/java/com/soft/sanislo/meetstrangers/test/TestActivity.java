package com.soft.sanislo.meetstrangers.test;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.Query;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.activity.BaseActivity;
import com.soft.sanislo.meetstrangers.adapter.PostAdapter;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.viewholders.UserPostViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 25.10.16.
 */

public class TestActivity extends BaseActivity {
    @BindView(R.id.rv_test)
    RecyclerView rvComments;

    private static final String TAG = TestActivity.class.getSimpleName();
    private boolean animated;

    private int mTotalItemCount;
    private int mLastVisibleItemPosition;
    private int mFirstFullVisItemPos;
    private int mFirstVisibleItemPos;
    private int mVisibleItemCount;
    private PostAdapter mPostAdatper;
    private static final int HOLDER_COUNT = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

        Query postQuery = Utils.getDatabase().getReference()
                .child(Constants.F_POSTS)
                .child(getAuthenticatedUserUID())
                .limitToFirst(HOLDER_COUNT);
        mPostAdatper = new PostAdapter(this, Post.class, R.layout.item_post, UserPostViewHolder.class, postQuery);
        mPostAdatper.setOnClickListener(new PostAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int position, Post post) {

            }

            @Override
            public void onClickAddComment(Post post, String commentText) {

            }

            @Override
            public void onClickCancelComment() {

            }

            @Override
            public void onClickHighlightComment() {
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    /*int expanedPos = mPostAdatper.getExpandedPos() == position ? -1 : position;
                    mCommentAdapter.setExpandedPos(expanedPos);
                    TransitionManager.beginDelayedTransition(rvComments);
                    mCommentAdapter.notifyDataSetChanged();*/
                }
            }

            @Override
            public void onClickLikeComment(Comment comment) {

            }
        });
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        rvComments.setLayoutManager(manager);
        rvComments.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mTotalItemCount = manager.getItemCount();
                mVisibleItemCount = manager.getChildCount();
                mFirstVisibleItemPos = manager.findFirstVisibleItemPosition();
                mFirstFullVisItemPos = manager.findFirstCompletelyVisibleItemPosition();

                Log.d(TAG, "onScrolled: mTotalItemCount: " + mTotalItemCount);
                Log.d(TAG, "onScrolled: mVisibleItemCount: " + mVisibleItemCount);
                Log.d(TAG, "onScrolled: mFirstVisibleItemPos: " + mFirstVisibleItemPos);
                Log.d(TAG, "onScrolled: mFirstFullVisItemPos: " + mFirstFullVisItemPos);
                //mLastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
                //Log.d(TAG, "onScrolled: mLastVisibleItem: " + mLastVisibleItemPosition);

                if (mVisibleItemCount + mFirstVisibleItemPos >= mTotalItemCount) {
                    Log.d(TAG, "onScrolled: ");
                }
            }
        });
        rvComments.setAdapter(mPostAdatper);
    }
}
