package com.soft.sanislo.meetstrangers.model;

/**
 * Created by root on 02.10.16.
 */
public class ChatMessage {
    private String key;
    private String message;
    private String imageURL;
    private String authorUID;
    private String authorAvatarURL;
    //private String recepientUID;
    private long timestamp;

    public ChatMessage() {}

    public ChatMessage(String key, String message, String imageURL, String authorUID, String authorAvatarURL, long timestamp) {
        this.key = key;
        this.message = message;
        this.imageURL = imageURL;
        this.authorUID = authorUID;
        this.authorAvatarURL = authorAvatarURL;
        this.timestamp = timestamp;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorUID() {
        return authorUID;
    }

    public void setAuthorUID(String authorUID) {
        this.authorUID = authorUID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getAuthorAvatarURL() {
        return authorAvatarURL;
    }

    public void setAuthorAvatarURL(String authorAvatarURL) {
        this.authorAvatarURL = authorAvatarURL;
    }
}
