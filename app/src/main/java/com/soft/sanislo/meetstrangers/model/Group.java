package com.soft.sanislo.meetstrangers.model;

import java.util.HashMap;

/**
 * Created by root on 04.12.16.
 */

public class Group {
    private String groupID;
    private String ownerUID;
    private long createdAt;
    private String name;
    private String status;
    private String groupAvatar;
    private long membersCount;
    private HashMap<String, Boolean> members;

    public Group() {}

    public Group(String groupID, String ownerUID, long createdAt, String name, String status, String groupAvatar, long membersCount, HashMap<String, Boolean> members) {
        this.groupID = groupID;
        this.ownerUID = ownerUID;
        this.createdAt = createdAt;
        this.name = name;
        this.status = status;
        this.groupAvatar = groupAvatar;
        this.membersCount = membersCount;
        this.members = members;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getOwnerUID() {
        return ownerUID;
    }

    public void setOwnerUID(String ownerUID) {
        this.ownerUID = ownerUID;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(long membersCount) {
        this.membersCount = membersCount;
    }

    public HashMap<String, Boolean> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, Boolean> members) {
        this.members = members;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGroupAvatar() {
        return groupAvatar;
    }

    public void setGroupAvatar(String groupAvatar) {
        this.groupAvatar = groupAvatar;
    }

    @Override
    public String toString() {
        return "Group{" +
                "ownerUID='" + ownerUID + '\'' +
                ", createdAt=" + createdAt +
                ", name='" + name + '\'' +
                ", membersCount=" + membersCount +
                ", members=" + members +
                '}';
    }
}
