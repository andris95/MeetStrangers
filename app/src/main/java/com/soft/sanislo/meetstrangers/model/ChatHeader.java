package com.soft.sanislo.meetstrangers.model;

/**
 * Created by root on 04.10.16.
 */
public class ChatHeader {
    private String lastMessage;
    private String authorUID;
    private String chatPartnerUID;
    private long timestamp;

    public ChatHeader() {}

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getAuthorUID() {
        return authorUID;
    }

    public void setAuthorUID(String authorUID) {
        this.authorUID = authorUID;
    }

    public String getChatPartnerUID() {
        return chatPartnerUID;
    }

    public void setChatPartnerUID(String chatPartnerUID) {
        this.chatPartnerUID = chatPartnerUID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ChatHeader(String lastMessage, String authorUID, String chatPartnerUID, long timestamp) {
        this.lastMessage = lastMessage;
        this.authorUID = authorUID;
        this.chatPartnerUID = chatPartnerUID;
        this.timestamp = timestamp;
    }
}
