package com.soft.sanislo.meetstrangers.model;

/**
 * Created by root on 04.10.16.
 */
public class ChatHeader {
    private String message;
    private String senderUID;
    private String senderName;
    private String senderAvatarURL;
    private long timestamp;

    public ChatHeader() {}

    public ChatHeader(String message, String senderUID, String senderName, String senderAvatarURL, long timestamp) {
        this.message = message;
        this.senderUID = senderUID;
        this.senderName = senderName;
        this.senderAvatarURL = senderAvatarURL;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderUID() {
        return senderUID;
    }

    public void setSenderUID(String senderUID) {
        this.senderUID = senderUID;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderAvatarURL() {
        return senderAvatarURL;
    }

    public void setSenderAvatarURL(String senderAvatarURL) {
        this.senderAvatarURL = senderAvatarURL;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ChatHeader{" +
                "message='" + message + '\'' +
                ", senderUID='" + senderUID + '\'' +
                ", senderName='" + senderName + '\'' +
                ", senderAvatarURL='" + senderAvatarURL + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
