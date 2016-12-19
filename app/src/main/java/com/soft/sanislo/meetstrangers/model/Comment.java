package com.soft.sanislo.meetstrangers.model;

import com.google.firebase.database.DatabaseReference;
import com.soft.sanislo.meetstrangers.utilities.Constants;
import com.soft.sanislo.meetstrangers.utilities.Utils;

import java.util.HashMap;

/**
 * Created by root on 09.10.16.
 */
public class Comment {
    private String commentKey;
    private String postKey;
    private String authorUID;
    private String text;
    private long likesCount;
    private HashMap<String, Boolean> likedUsersUIDs;
    private long timestamp;

    public Comment() {}

    public Comment(String commentKey, String postKey, String authorUID, String text, long timestamp) {
        this.commentKey = commentKey;
        this.postKey = postKey;
        this.authorUID = authorUID;
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
        return getLikedUsersUIDs() != null && getLikedUsersUIDs().containsKey(uid);
    }

    /**
     * likes or dislikes the comment by user with uid
     * @param uid
     */
    public void setLikedByUser(String uid) {
        if (getLikedUsersUIDs() == null) {
            likedUsersUIDs = new HashMap<>();
        }
        if (isLikedByUser(uid)) {
            likedUsersUIDs.remove(uid);
            likesCount--;
        } else {
            likedUsersUIDs.put(uid, true);
            likesCount++;
        }
    }
}
