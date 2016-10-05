package com.soft.sanislo.meetstrangers.model;

/**
 * Created by root on 02.10.16.
 */
public class ChatMessage {
    private String chatMessageKey;
    private String message;
    private String authorUID;
    private String recipientUID;
    private long timestamp;

    public ChatMessage() {}

    public ChatMessage(String chatMessageKey, String message, String authorUID, String recipientUID, long timestamp) {
        this.chatMessageKey = chatMessageKey;
        this.message = message;
        this.authorUID = authorUID;
        this.recipientUID = recipientUID;
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

    public String getRecipientUID() {
        return recipientUID;
    }

    public void setRecipientUID(String recipientUID) {
        this.recipientUID = recipientUID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getChatMessageKey() {
        return chatMessageKey;
    }

    public void setChatMessageKey(String chatMessageKey) {
        this.chatMessageKey = chatMessageKey;
    }
}
