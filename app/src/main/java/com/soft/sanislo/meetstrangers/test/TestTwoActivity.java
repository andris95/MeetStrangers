package com.soft.sanislo.meetstrangers.test;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.firebase.database.Query;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.activity.BaseActivity;
import com.soft.sanislo.meetstrangers.adapter.PostAdapter;
import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.PostViewHolder;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 08.11.16.
 */

public class TestTwoActivity extends BaseActivity {

    @BindView(R.id.rv_test)
    RecyclerView rvTest;

    @BindView(R.id.nsv_test_two)
    NestedScrollView mNestedScrollView;

    private static final String TAG = TestTwoActivity.class.getSimpleName();
    private static final int HOLDER_COUNT = 10;
    private CommentTestAdapter mCommentTestAdapter;
    private int mLastY = -1;
    private int mCurrentPage = 0;
    private int mTotalItemCount;
    private int mLastVisibleItemPosition;
    private int mFirstFullVisItemPos;
    private int mFirstVisibleItemPos;
    private int mVisibleItemCount;

    private String lawrence1 = "https://wallpaperscraft.com/image/joy_jennifer_lawrence_2015_105464_1920x1080.jpg";
    private String lawrence2 = "http://wallpapersdsc.net/wp-content/uploads/2016/01/Jennifer-Lawrence-Desktop.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_two);
        ButterKnife.bind(this);
        //mNestedScrollView.setNestedScrollingEnabled(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTest.setLayoutManager(layoutManager);
        rvTest.setNestedScrollingEnabled(false);
        rvTest.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                Log.d(TAG, "onScrollStateChanged: newState: " + newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > mLastY) {
                    mTotalItemCount = layoutManager.getItemCount();
                    mVisibleItemCount = layoutManager.getChildCount();
                    mFirstVisibleItemPos = layoutManager.findFirstVisibleItemPosition();
                    mFirstFullVisItemPos = layoutManager.findFirstCompletelyVisibleItemPosition();
                    Log.d(TAG, "onScrolled: mTotalItemCount: " + mTotalItemCount);
                    Log.d(TAG, "onScrolled: mVisibleItemCount: " + mVisibleItemCount);

                    if (mVisibleItemCount + mFirstVisibleItemPos >= mTotalItemCount) {
                        loadMoreData();
                    }
                }
            }
        });

        mCommentTestAdapter = new CommentTestAdapter(this, generateData(0));
        rvTest.setAdapter(mCommentTestAdapter);
    }

    private void loadMoreData() {
        if (mCommentTestAdapter.isLoading()) return;
        mCommentTestAdapter.setLoading(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCurrentPage++;
                mCommentTestAdapter.setLoading(false);
                mCommentTestAdapter.addMoreData(generateData(mCurrentPage * HOLDER_COUNT));
                Log.d(TAG, "onScrolled: mCurrentPage: " + mCurrentPage);
            }
        }, 5000);
    }

    private ArrayList<CommentTest> generateData(int start) {
        ArrayList<CommentTest> data = new ArrayList<>();
        for (int i = start; i < start + HOLDER_COUNT; i++) {
            CommentTest commentTest = new CommentTest(
                    i % 2 == 0 ? lawrence1 : lawrence2,
                    "Jennifer Lawrence " + i,
                    getString(R.string.lorem),
                    false,
                    new Date().getTime()
            );
            data.add(commentTest);
        }
        return data;
    }
}
