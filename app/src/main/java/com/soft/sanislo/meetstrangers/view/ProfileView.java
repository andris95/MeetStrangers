package com.soft.sanislo.meetstrangers.view;

import com.google.firebase.database.DatabaseError;
import com.soft.sanislo.meetstrangers.model.User;

/**
 * Created by root on 17.10.16.
 */

public interface ProfileView {
    void onDisplayedUserChanged(User user);
    void onLastActiveChanged(String lastActive);

    void onAddressFetchSuccess(String address);
    void onAddressFetchFailure(String errorMessage);
    void showDialog(String userStatus);
    void onComplete(String message);
    void onError(String errorMessage);
    void onError(Exception e);
    void onDatabaseError(DatabaseError databaseError);
}
