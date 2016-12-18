package com.soft.sanislo.meetstrangers.interfaces;

import android.view.View;

import com.soft.sanislo.meetstrangers.model.Comment;
import com.soft.sanislo.meetstrangers.model.Post;

/**
 * Created by root on 11.12.16.
 */

public interface PostClickListener {
    void onClick(View view, int position, Post post);
    void onClickAddComment(Post post, String commentText);
    void onClickCancelComment();
    void onClickHighlightComment();
    void onClickLikeComment(Comment comment);
}
