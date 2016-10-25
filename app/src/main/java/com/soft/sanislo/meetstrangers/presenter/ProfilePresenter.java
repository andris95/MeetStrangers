package com.soft.sanislo.meetstrangers.presenter;

import com.soft.sanislo.meetstrangers.model.CommentModel;
import com.soft.sanislo.meetstrangers.model.Post;
import com.soft.sanislo.meetstrangers.model.User;

/**
 * Created by root on 17.10.16.
 */

public interface ProfilePresenter {
    void onDisplayedUserChanged(User user);
    void likeComment(CommentModel commentModel);
    void likePost(String postKey);
    void addComment(Post post, String commentText);
    void onClickFAB();

    void onResume();
    void onPause();
    void onBackPressed();
}
