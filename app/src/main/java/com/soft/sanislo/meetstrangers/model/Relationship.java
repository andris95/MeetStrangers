package com.soft.sanislo.meetstrangers.model;

/**
 * Created by root on 10.10.16.
 */
public class Relationship {
    private int status;
    private long timestamp;
    private String lastUserActionUID;

    public Relationship() {}

    public Relationship(int status, long timestamp, String lastUserActionUID) {
        this.status = status;
        this.timestamp = timestamp;
        this.lastUserActionUID = lastUserActionUID;
    }

    public int getStatus() {

        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLastUserActionUID() {
        return lastUserActionUID;
    }

    public void setLastUserActionUID(String lastUserActionUID) {
        this.lastUserActionUID = lastUserActionUID;
    }

    @Override
    public String toString() {
        return "Relationship{" +
                "status=" + status +
                ", timestamp=" + timestamp +
                ", lastUserActionUID='" + lastUserActionUID + '\'' +
                '}';
    }
}
