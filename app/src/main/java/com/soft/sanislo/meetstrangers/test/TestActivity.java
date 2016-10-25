package com.soft.sanislo.meetstrangers.test;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.View;

import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.activity.BaseActivity;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 25.10.16.
 */

public class TestActivity extends BaseActivity {
    @BindView(R.id.rv_comments)
    RecyclerView rvComments;

    private CommentAdapter mCommentAdapter;
    private ArrayList<Comment> mComments = new ArrayList<>();

    private String lawrence1 = "https://wallpaperscraft.com/image/joy_jennifer_lawrence_2015_105464_1920x1080.jpg";
    private String lawrence2 = "http://wallpapersdsc.net/wp-content/uploads/2016/01/Jennifer-Lawrence-Desktop.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

        for (int i = 1; i < 12; i++) {
            Comment comment = new Comment(
                    i % 2 == 0 ? lawrence1 : lawrence2,
                    "Jennifer Lawrence " + i,
                    getString(R.string.lorem),
                    false,
                    new Date().getTime()
            );
            mComments.add(comment);
        }
        mCommentAdapter = new CommentAdapter(this, mComments);
        mCommentAdapter.setOnClickListener(new CommentAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int position, Comment comment) {
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    int expanedPos = mCommentAdapter.getExpandedPos() == position ? -1 : position;
                    mCommentAdapter.setExpandedPos(expanedPos);
                    TransitionManager.beginDelayedTransition(rvComments);
                    mCommentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onClickLikeComment(Comment comment) {

            }
        });
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(mCommentAdapter);
    }
}
