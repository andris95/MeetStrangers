package com.soft.sanislo.meetstrangers.model;

import com.soft.sanislo.meetstrangers.utilities.Constants;

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

    /**retruns the String represantation of relationship between two users*/
    public String getRelationshipStatusString(String authenticatedUID) {
        switch (getStatus()) {
            case Constants.RS_FRIENDS:
                return "Delete from friends";
            case Constants.RS_PENDING:
                if (lastActionUserUID.equals(authenticatedUID)) {
                    return "Cancel follow request";
                } else {
                    return "Accept follow request";
                }
            case Constants.RS_DELETED:
                if (lastActionUserUID.equals(authenticatedUID)) {
                    return "Accept follow request";
                } else {
                    return "Cancel follow request";
                }
            default:
                return "UNKNOWN";
        }
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
