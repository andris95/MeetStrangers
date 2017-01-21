package com.soft.sanislo.meetstrangers.model;

import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 24.09.16.
 */
public class Post {
    public static final int BY_USER = 111;
    public static final int BY_GROUP = 222;
    private String key;
    private String authorUID;
    private String authorName;
    private String authorAvatarURL;
    private int createdBy;
    private String content;
    private long timestamp;
    private long likesCount;
    private long dislikesCount;
    private long commentsCount;
    private String photoURL;
    private List<MediaFile> mediaFiles;
    private HashMap<String, Boolean> likedUsersUIDs;
    private HashMap<String, Boolean> dislikedUsersUIDs;

    public Post() {}

    public Post(String key, String authorUID, String authorName, String authorAvatarURL, int createdBy, String content, long timestamp, long likesCount, long dislikesCount, long commentsCount, String photoURL, List<MediaFile> mediaFiles, HashMap<String, Boolean> likedUsersUIDs, HashMap<String, Boolean> dislikedUsersUIDs) {
        this.key = key;
        this.authorUID = authorUID;
        this.authorName = authorName;
        this.authorAvatarURL = authorAvatarURL;
        this.createdBy = createdBy;
        this.content = content;
        this.timestamp = timestamp;
        this.likesCount = likesCount;
        this.dislikesCount = dislikesCount;
        this.commentsCount = commentsCount;
        this.photoURL = photoURL;
        this.mediaFiles = mediaFiles;
        this.likedUsersUIDs = likedUsersUIDs;
        this.dislikedUsersUIDs = dislikedUsersUIDs;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public String getAuthorUID() {
        return authorUID;
    }

    public void setAuthorUID(String authorUID) {
        this.authorUID = authorUID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public int getCreatedBy() {
        return createdBy;
    }

    public List<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(List<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorAvatarURL() {
        return authorAvatarURL;
    }

    public void setAuthorAvatarURL(String authorAvatarURL) {
        this.authorAvatarURL = authorAvatarURL;
    }

    public long getDislikesCount() {
        return dislikesCount;
    }

    public void setDislikesCount(long dislikesCount) {
        this.dislikesCount = dislikesCount;
    }

    public HashMap<String, Boolean> getDislikedUsersUIDs() {
        return dislikedUsersUIDs;
    }

    public void setDislikedUsersUIDs(HashMap<String, Boolean> dislikedUsersUIDs) {
        this.dislikedUsersUIDs = dislikedUsersUIDs;
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

    public static class Builder {
        private String postUID;
        private String authorUID;
        private String authorName;
        private String authorAvatarURL;
        private String content;
        private int createdBy;
        private long timestamp;
        private long likesCount;
        private long dislikesCount;
        private long commentsCount;
        private String photoURL;
        private List<MediaFile> mediaFiles;
        private HashMap<String, Boolean> likedUsersUIDs;
        private HashMap<String, Boolean> dislikedUsersUIDs;

        public Builder setPostUID(String postUID) {
            this.postUID = postUID;
            return this;
        }

        public Builder setCreatedBy(int createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setAuthorUID(String authorUID) {
            this.authorUID = authorUID;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setLikesCount(long likesCount) {
            this.likesCount = likesCount;
            return this;
        }

        public Builder setDislikesCount(long dislikesCount) {
            this.dislikesCount = dislikesCount;
            return this;
        }

        public Builder setCommentsCount(long commentsCount) {
            this.commentsCount = commentsCount;
            return this;
        }

        public Builder setMediaFiles(List<MediaFile> mediaFiles) {
            this.mediaFiles = mediaFiles;
            return this;
        }

        public Builder setLikedUsersUIDs(HashMap<String, Boolean> likedUsersUIDs) {
            this.likedUsersUIDs = likedUsersUIDs;
            return this;
        }

        public Builder setDislikedUsersUIDs(HashMap<String, Boolean> dislikedUsersUIDs) {
            this.dislikedUsersUIDs = dislikedUsersUIDs;
            return this;
        }

        public Builder setPhotoURL(String photoURL) {
            this.photoURL = photoURL;
            return this;
        }

        public Builder setAuthorName(String authorName) {
            this.authorName = authorName;
            return this;
        }

        public Builder setAuthorAvatarURL(String authorAvatarURL) {
            this.authorAvatarURL = authorAvatarURL;
            return this;
        }

        //Return the finally consrcuted User object
        public Post build() {
            Post post = new Post(postUID,
                    authorUID,
                    authorName,
                    authorAvatarURL,
                    createdBy,
                    content,
                    timestamp,
                    likesCount,
                    dislikesCount,
                    commentsCount,
                    photoURL,
                    mediaFiles,
                    likedUsersUIDs,
                    dislikedUsersUIDs);
            return post;
        }
    }
}
