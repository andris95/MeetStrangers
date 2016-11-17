package com.soft.sanislo.meetstrangers.presenter;

import android.view.View;

import com.soft.sanislo.meetstrangers.model.ChatHeader;

/**
 * Created by root on 01.11.16.
 */

public interface ChatHeaderPresenter {
    void onClickChatHeader(View view, ChatHeader chatHeader, String chatPartnerKey);
    void onResume();
    void onPause();
}
