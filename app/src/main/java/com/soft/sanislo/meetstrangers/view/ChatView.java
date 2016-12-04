package com.soft.sanislo.meetstrangers.view;

import com.soft.sanislo.meetstrangers.model.User;

/**
 * Created by root on 28.10.16.
 */

public interface ChatView {
    void sendChatMessage();
    void onAuthenticatedUserChange(User user);
    void onChatPartnerChange(User user);
}
