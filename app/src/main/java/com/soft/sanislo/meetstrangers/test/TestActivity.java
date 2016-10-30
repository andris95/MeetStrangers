package com.soft.sanislo.meetstrangers.test;

import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Property;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;

import com.google.android.gms.vision.text.Line;
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

    @BindView(R.id.ll_test)
    LinearLayout llTest;

    private boolean animated;

    private CommentAdapter mCommentAdapter;
    private ArrayList<CommentTest> mCommentTests = new ArrayList<>();

    private String lawrence1 = "https://wallpaperscraft.com/image/joy_jennifer_lawrence_2015_105464_1920x1080.jpg";
    private String lawrence2 = "http://wallpapersdsc.net/wp-content/uploads/2016/01/Jennifer-Lawrence-Desktop.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

        llTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (animated) {
                    ObjectAnimator zAnimator = ObjectAnimator.ofFloat(llTest, "translationZ", 64, 0);
                    zAnimator.setDuration(1000);
                    zAnimator.start();
                } else {
                    ObjectAnimator zAnimator = ObjectAnimator.ofFloat(llTest, "translationZ", 0, 64);
                    zAnimator.setDuration(1000);
                    zAnimator.start();
                }
            }
        });



        for (int i = 1; i < 12; i++) {
            CommentTest commentTest = new CommentTest(
                    i % 2 == 0 ? lawrence1 : lawrence2,
                    "Jennifer Lawrence " + i,
                    getString(R.string.lorem),
                    false,
                    new Date().getTime()
            );
            mCommentTests.add(commentTest);
        }
        mCommentAdapter = new CommentAdapter(this, mCommentTests);
        mCommentAdapter.setOnClickListener(new CommentAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int position, CommentTest commentTest) {
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    int expanedPos = mCommentAdapter.getExpandedPos() == position ? -1 : position;
                    mCommentAdapter.setExpandedPos(expanedPos);
                    TransitionManager.beginDelayedTransition(rvComments);
                    mCommentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onClickLikeComment(CommentTest commentTest) {

            }
        });
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(mCommentAdapter);
    }
}
