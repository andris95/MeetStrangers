package com.soft.sanislo.meetstrangers.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.adapter.ChatHeaderAdapter;
import com.soft.sanislo.meetstrangers.model.ChatHeader;
import com.soft.sanislo.meetstrangers.model.User;
import com.soft.sanislo.meetstrangers.presenter.ChatHeaderPresenter;
import com.soft.sanislo.meetstrangers.presenter.ChatHeaderPresenterImpl;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;
import com.soft.sanislo.meetstrangers.view.ChatHeaderView;
import com.soft.sanislo.meetstrangers.view.ChatHeaderViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by root on 04.10.16.
 */
public class ChatHeaderActivity extends BaseActivity implements ChatHeaderView {
    private static final String TAG = ChatHeaderActivity.class.getSimpleName();

    @BindView(R.id.rv_chat_headers)
    RecyclerView rvChatHeaders;

    private ChatHeaderPresenter mChatHeaderPresenter;
    private DatabaseReference database = Utils.getDatabase().getReference();
    private DatabaseReference chatHeaderRef;
    private String mAuthUID;
    private ChatHeaderAdapter mChatHeaderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_header);
        ButterKnife.bind(this);

        mChatHeaderPresenter = new ChatHeaderPresenterImpl(this);
        mAuthUID = getAuthenticatedUserUID();

        initChatHeaders();
    }

    private void initChatHeaders() {
        chatHeaderRef = database.child(Constants.F_CHATS_HEADERS).child(mAuthUID);
        mChatHeaderAdapter = new ChatHeaderAdapter(ChatHeader.class,
                R.layout.item_chat_header,
                ChatHeaderViewHolder.class,
                chatHeaderRef,
                this);
        mChatHeaderAdapter.setOnClickListener(new ChatHeaderAdapter.OnClickListener() {
            @Override
            public void onClick(View view, int position, ChatHeader chatHeader, String chatPartnerKey) {
                mChatHeaderPresenter.onClickChatHeader(view, chatHeader, chatPartnerKey);
            }
        });
        rvChatHeaders.setLayoutManager(new LinearLayoutManager(this));
        rvChatHeaders.setAdapter(mChatHeaderAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mChatHeaderPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mChatHeaderPresenter.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mChatHeaderAdapter.cleanup();
        mChatHeaderAdapter = null;
    }
}
