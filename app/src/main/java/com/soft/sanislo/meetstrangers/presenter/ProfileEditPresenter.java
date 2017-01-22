package com.soft.sanislo.meetstrangers.presenter;

import android.content.Context;
import android.content.Intent;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.soft.sanislo.meetstrangers.R;
import com.soft.sanislo.meetstrangers.model.User;

/**
 * Created by root on 21.01.17.
 */

public interface ProfileEditPresenter {
    void confirmEdit(Context context, String firstName, String lastName);
    void onResume();
    void onPause();
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
