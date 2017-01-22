package com.soft.sanislo.meetstrangers.presenter;

import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.User;

/**
 * Created by root on 17.10.16.
 */

public interface ProfilePresenter {
    void setAuthenticatedUserUID(String uid);
    void setDisplayedUserUID(String uid);

    void likeComment(Comment comment);
    void likePost(String postKey);
    void addComment(Post post, String commentText);
    void onClickFAB();
    void onDialogItemSelected(int position);
    void onResume();
    void onPause();
    void onBackPressed();
}
