package com.soft.sanislo.meetstrangers.presenter;

import android.content.Context;
import android.content.Intent;

/**
 * Created by root on 01.11.16.
 */

public interface NewPostPresenter {
    void canPushPost(String postText);
    void pushPost(Context context, String postText);
    void onActivityResult(int requestCode, int resultCode, Intent data);
    void onResume();
    void onPause();
}
