package com.soft.sanislo.meetstrangers.model;

import java.util.HashMap;

/**
 * Created by root on 09.10.16.
 */
public class Comment {
    private String commentKey;
    private String postKey;
    private String authorUID;
    private String authorFullName;
    private String authorAvatarURL;
    private String text;
    private long likesCount;
    private HashMap<String, Boolean> likedUsersUIDs;
    private long timestamp;

    public Comment () {}

    public Comment(String commentKey, String postKey, String authorUID, String authorFullName, String authorAvatarURL, String text, long timestamp) {
        this.commentKey = commentKey;
        this.postKey = postKey;
        this.authorUID = authorUID;
        this.authorFullName = authorFullName;
        this.authorAvatarURL = authorAvatarURL;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getCommentKey() {
        return commentKey;
    }

    public void setCommentKey(String commentKey) {
        this.commentKey = commentKey;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getAuthorUID() {
        return authorUID;
    }

    public void setAuthorUID(String authorUID) {
        this.authorUID = authorUID;
    }

    public String getAuthorFullName() {
        return authorFullName;
    }

    public void setAuthorFullName(String authorFullName) {
        this.authorFullName = authorFullName;
    }

    public String getAuthorAvatarURL() {
        return authorAvatarURL;
    }

    public void setAuthorAvatarURL(String authorAvatarURL) {
        this.authorAvatarURL = authorAvatarURL;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentKey='" + commentKey + '\'' +
                ", postKey='" + postKey + '\'' +
                ", authorUID='" + authorUID + '\'' +
                ", authorFullName='" + authorFullName + '\'' +
                ", authorAvatarURL='" + authorAvatarURL + '\'' +
                ", text='" + text + '\'' +
                ", likesCount=" + likesCount +
                ", likedUsersUIDs=" + likedUsersUIDs +
                ", timestamp=" + timestamp +
                '}';
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public HashMap<String, Boolean> getLikedUsersUIDs() {
        return likedUsersUIDs;
    }

    public void setLikedUsersUIDs(HashMap<String, Boolean> likedUsersUIDs) {
        this.likedUsersUIDs = likedUsersUIDs;
    }

    public boolean isLikedByUser(String uid) {
        HashMap<String, Boolean> likers = likedUsersUIDs;
        return likers != null && likers.containsKey(uid);
    }
}
