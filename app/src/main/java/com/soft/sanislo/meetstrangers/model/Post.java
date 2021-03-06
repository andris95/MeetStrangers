package com.soft.sanislo.meetstrangers.model;

import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 24.09.16.
 */
public class Post {
    private String authorUID;
    private String authFullName;
    private String authorAvatarURL;
    private String key;
    private String text;
    private long timestamp;
    private long likesCount;
    private long commentsCount;
    private List<MediaFile> mediaFiles;
    private HashMap<String, Boolean> likedUsersUIDs;

    public Post() {}

    public Post(String authorUID, String authFullName, String authorAvatarURL, String key, String text, long timestamp, long likesCount, long commentsCount, List<String> photoURLList, HashMap<String, Boolean> likedUsersUIDs) {
        this.authorUID = authorUID;
        this.authFullName = authFullName;
        this.authorAvatarURL = authorAvatarURL;
        this.key = key;
        this.text = text;
        this.timestamp = timestamp;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.likedUsersUIDs = likedUsersUIDs;
    }

    public String getAuthorUID() {
        return authorUID;
    }

    public void setAuthorUID(String authorUID) {
        this.authorUID = authorUID;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public HashMap<String, Boolean> getLikedUsersUIDs() {
        return likedUsersUIDs;
    }

    public void setLikedUsersUIDs(HashMap<String, Boolean> likedUsersUIDs) {
        this.likedUsersUIDs = likedUsersUIDs;
    }

    public List<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(List<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    public boolean isLikedByUser(String uid) {
        return likedUsersUIDs != null && likedUsersUIDs.containsKey(uid);
    }

    public void setLikedByUser(String uid) {
        if (likedUsersUIDs == null) {
            likedUsersUIDs = new HashMap<>();
            likedUsersUIDs.put(uid, true);
            likesCount++;
        } else {
            boolean isLikedByUser = isLikedByUser(uid);
            if (isLikedByUser) {
                likedUsersUIDs.remove(uid);
                likesCount--;
            } else {
                likedUsersUIDs.put(uid, true);
                likesCount++;
            }
        }
    }
}
