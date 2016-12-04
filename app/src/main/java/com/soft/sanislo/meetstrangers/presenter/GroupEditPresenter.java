package com.soft.sanislo.meetstrangers.presenter;

import android.content.Intent;

/**
 * Created by root on 04.12.16.
 */

public interface GroupEditPresenter {
    void onClickGroupAvatar();
    void onClickGroupStatus();
    void onClickGroupAction();
    void onCLickNewGroupPost();
    void onPause();
    void onResume();
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
