package com.soft.sanislo.meetstrangers.presenter;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.soft.sanislo.meetstrangers.activity.ChatActivity;
import com.soft.sanislo.meetstrangers.activity.ChatHeaderActivity;
import com.soft.sanislo.meetstrangers.model.ChatHeader;
import com.soft.sanislo.meetstrangers.view.ChatHeaderView;

/**
 * Created by root on 04.12.16.
 */

public class ChatHeaderPresenterImpl implements ChatHeaderPresenter {
    private Context mContext;
    private ChatHeaderView mChatHeaderView;

    public ChatHeaderPresenterImpl(ChatHeaderActivity context) {
        mContext = context;
        mChatHeaderView = context;
    }

    @Override
    public void onClickChatHeader(View view, ChatHeader chatHeader, String chatPartnerKey) {
        Intent intent = new Intent(mContext, ChatActivity.class);
        intent.putExtra(ChatActivity.KEY_CHAT_PARTNER_UID, chatPartnerKey);
        mContext.startActivity(intent);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }
}
