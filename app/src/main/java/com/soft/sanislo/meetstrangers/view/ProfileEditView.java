package com.soft.sanislo.meetstrangers.view;

import com.soft.sanislo.meetstrangers.model.User;

/**
 * Created by root on 21.01.17.
 */

public interface ProfileEditView {
    void onUserChanged(User user);
    void onProfileUpdated();
    void onError(String errorMessage, Exception e);
}
