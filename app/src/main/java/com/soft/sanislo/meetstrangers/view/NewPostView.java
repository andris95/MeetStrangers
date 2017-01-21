package com.soft.sanislo.meetstrangers.view;

/**
 * Created by root on 21.01.17.
 */

public interface NewPostView {
    void onCanPushPostChecked(boolean canPushPost);
    void onPostPushed();
    void onError(String error, Exception e);
}
