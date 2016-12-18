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
import com.soft.sanislo.meetstrangers.view.VkontakteView;
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

    VkontakteView mVkontakteView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ButterKnife.bind(this);

        mVkontakteView.test();























































































    }
}
