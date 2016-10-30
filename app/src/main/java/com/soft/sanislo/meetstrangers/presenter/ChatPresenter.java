package com.soft.sanislo.meetstrangers.presenter;

import com.soft.sanislo.meetstrangers.model.ChatMessage;
import com.soft.sanislo.meetstrangers.model.User;

/**
 * Created by root on 28.10.16.
 */

public interface ChatPresenter {
    void pushChatMessage(ChatMessage chatMessage);

    void onPause();
    void onResume();
    void onDestroy();
}
