package com.soft.sanislo.meetstrangers.model;

/**
 * Created by root on 10.10.16.
 */
public class Relationship {
    private int status;
    private long timestamp;
    private String lastActionUserUID;
    private String firstActionUserUID;

    public Relationship() {}

    public Relationship(int status, long timestamp, String lastActionUserUID, String firstActionUserUID) {
        this.status = status;
        this.timestamp = timestamp;
        this.lastActionUserUID = lastActionUserUID;
        this.firstActionUserUID = firstActionUserUID;
    }

    public String getFirstActionUserUID() {
        return firstActionUserUID;
    }

    public void setFirstActionUserUID(String firstActionUserUID) {
        this.firstActionUserUID = firstActionUserUID;
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

    public String getLastActionUserUID() {
        return lastActionUserUID;
    }

    public void setLastActionUserUID(String lastActionUserUID) {
        this.lastActionUserUID = lastActionUserUID;
    }

    @Override
    public String toString() {
        return "Relationship{" +
                "status=" + status +
                ", timestamp=" + timestamp +
                ", lastActionUserUID='" + lastActionUserUID + '\'' +
                '}';
    }
}
