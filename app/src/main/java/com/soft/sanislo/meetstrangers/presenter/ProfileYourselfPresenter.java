package com.soft.sanislo.meetstrangers.presenter;

import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.Post;

/**
 * Created by andras on 19.12.16.
 */

public interface ProfileYourselfPresenter {
    void likeComment(Comment comment);
    void likePost(String postKey);
    void addComment(Post post, String commentText);
    void onClickFAB();

    void onResume();
    void onPause();
    void onBackPressed();
}
