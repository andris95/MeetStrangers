package com.soft.sanislo.meetstrangers.model;

/**
 * Created by root on 02.10.16.
 */
public class ChatMessage {
    private String key;
    private String message;
    private String authorUID;
    private String recepientUID;
    private long timestamp;

    public ChatMessage() {}

    public ChatMessage(String chatMessageKey, String message, String authorUID, String recipientUID, long timestamp) {
        this.key = chatMessageKey;
        this.message = message;
        this.authorUID = authorUID;
        this.recepientUID = recipientUID;
        this.timestamp = timestamp;
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

    public String getRecepientUID() {
        return recepientUID;
    }

    public void setRecepientUID(String recepientUID) {
        this.recepientUID = recepientUID;
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
}
