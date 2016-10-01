package com.soft.sanislo.meetstrangers.model;

import java.util.List;

/**
 * Created by root on 24.09.16.
 */
public class Post {
    private String authorUID;
    private String authFullName;
    private String authorAvatarURL;
    private String postID;
    private String text;
    private String photoURL;
    private long timestamp;
    private long likesCount;
    private long commentsCount;
    private List<String> photoURLList;

    private String photoURLs;

    public Post() {}

    public Post(String text, String authorUID, String postID, long timestamp) {
        this.text = text;
        this.authorUID = authorUID;
        this.postID = postID;
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthorUID() {
        return authorUID;
    }

    public void setAuthorUID(String authorUID) {
        this.authorUID = authorUID;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getAuthFullName() {
        return authFullName;
    }

    public void setAuthFullName(String authFullName) {
        this.authFullName = authFullName;
    }

    public String getAuthorAvatarURL() {
        return authorAvatarURL;
    }

    public void setAuthorAvatarURL(String authorAvatarURL) {
        this.authorAvatarURL = authorAvatarURL;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getPhotoURLs() {
        return photoURLs;
    }

    public List<String> getPhotoURLList() {
        return photoURLList;
    }

    public void setPhotoURLList(List<String> photoURLList) {
        this.photoURLList = photoURLList;
    }

    public void setPhotoURLs(String photoURLs) {
        this.photoURLs = photoURLs;
    }
}
