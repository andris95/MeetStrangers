package com.soft.sanislo.meetstrangers.test;

/**
 * Created by root on 25.10.16.
 */

public class CommentTest {
    private String authorAvatarURL;
    private String authorName;
    private String text;
    boolean isLiked;
    private long timestamp;

    public CommentTest(String authorAvatarURL, String authorName, String text, boolean isLiked, long timestamp) {
        this.authorAvatarURL = authorAvatarURL;
        this.authorName = authorName;
        this.text = text;
        this.isLiked = isLiked;
        this.timestamp = timestamp;
    }

    public String getAuthorAvatarURL() {
        return authorAvatarURL;
    }

    public void setAuthorAvatarURL(String authorAvatarURL) {
        this.authorAvatarURL = authorAvatarURL;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
