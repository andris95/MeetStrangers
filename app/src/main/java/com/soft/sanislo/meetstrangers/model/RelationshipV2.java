package com.soft.sanislo.meetstrangers.model;

import android.util.Log;

import com.soft.sanislo.meetstrangers.utilities.Constants;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by root on 22.01.17.
 */

public class RelationshipV2 {
    private static final String TAG = RelationshipV2.class.getSimpleName();
    /**
     * status represents the relationship between 2 users
     * 0: strangers
     * 1: friends
     * 2: pending
     */
    public static final int STATUS_STRANGERS = 0;
    public static final int STATUS_FRIENDS = 1;
    public static final int STATUS_PENDING = 2;
    public static final int STATUS_BLOCKED = 3;

    private int status;
    private String actionUserUID;
    private long timestamp;

    public RelationshipV2() {}

    public RelationshipV2(int status, String actionUserUID, long timestamp) {
        this.status = status;
        this.actionUserUID = actionUserUID;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getActionUserUID() {
        return actionUserUID;
    }

    public void setActionUserUID(String actionUserUID) {
        this.actionUserUID = actionUserUID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRelationshipStatusText(String authenticatedUID) {
        String statusText;
        if (areFriends()) {
            statusText = "You are friends. Remove from friends list?";
        } else if (isMeFollowingHim(authenticatedUID)) {
            statusText = "You are following this user. Unfollow him?";
        } else if (isHeFollowingMe(authenticatedUID)) {
            statusText = "This user is following You. Accept request?";
        } else {
            statusText = "You are strangers. Follow thi user";
        }
        return statusText;
    }

    public boolean areFriends() {
        return status == 1;
    }

    public boolean isMeFollowingHim(String authenticatedUID) {
        return actionUserUID.equals(authenticatedUID) && status == 2;
    }

    public boolean isHeFollowingMe(String authenticatedUID) {
        return !actionUserUID.equals(authenticatedUID) && status == 2;
    }

    public static final HashMap<String, Object> getFollowMap(String displayedUID, String authUID) {
        HashMap<String, Object> toUpdate = new HashMap<>();
        toUpdate.put("/users_followers/" + displayedUID + "/" + authUID, true);
        toUpdate.put("/users_following/" + authUID + "/" + displayedUID, true);

        toUpdate.putAll(getTimestampMap(authUID, displayedUID));
        toUpdate.putAll(getLastActionByAuthenticated(authUID, displayedUID));
        toUpdate.putAll(getStatusMap(authUID, displayedUID, STATUS_PENDING));

        logHashMap(toUpdate);
        return toUpdate;
    }

    public static final HashMap<String, Object> getUnfollowMap(String displayedUID, String authUID) {
        HashMap<String, Object> toUpdate = new HashMap<>();
        toUpdate.put("/users_followers/" + displayedUID + "/" + authUID, null);
        toUpdate.put("/users_following/" + authUID + "/" + displayedUID, null);
        toUpdate.put("/" + Constants.F_RELATIONSHIPS + "/" + displayedUID + "/" + authUID, null);
        toUpdate.put("/" + Constants.F_RELATIONSHIPS + "/" + authUID + "/" + displayedUID, null);
        logHashMap(toUpdate);
        return toUpdate;
    }

    public static final HashMap<String, Object> getAcceptMap(String displayedUID, String authUID) {
        HashMap<String, Object> toUpdate = new HashMap<>();
        toUpdate.put("/users_followers/" + authUID + "/" + displayedUID, null);
        toUpdate.put("/users_following/" + displayedUID + "/" + authUID, null);
        toUpdate.put("/users_friends/" + authUID + "/" + displayedUID, true);
        toUpdate.put("/users_friends/" + displayedUID + "/" + authUID, true);

        toUpdate.putAll(getTimestampMap(authUID, displayedUID));
        toUpdate.putAll(getLastActionByAuthenticated(authUID, displayedUID));
        toUpdate.putAll(getStatusMap(authUID, displayedUID, STATUS_FRIENDS));
        logHashMap(toUpdate);
        return toUpdate;
    }

    public static final HashMap<String, Object> getRemoveFriendMap(String displayedUID, String authUID) {
        HashMap<String, Object> toUpdate = new HashMap<>();
        toUpdate.put("/users_followers/" + authUID + "/" + displayedUID, true);
        toUpdate.put("/users_following/" + displayedUID + "/" + authUID, true);
        toUpdate.put("/users_friends/" + authUID + "/" + displayedUID, null);
        toUpdate.put("/users_friends/" + displayedUID + "/" + authUID, null);

        toUpdate.putAll(getTimestampMap(authUID, displayedUID));
        toUpdate.putAll(getLastActionByDisplayed(authUID, displayedUID));
        toUpdate.putAll(getStatusMap(authUID, displayedUID, STATUS_PENDING));
        logHashMap(toUpdate);
        return toUpdate;
    }

    /** for timestamps */
    private static final HashMap<String, Object> getTimestampMap(String authUID, String displayedUID) {
        HashMap<String, Object> toUpdate = new HashMap<>();
        long timestamp = new Date().getTime();
        toUpdate.put("/" + Constants.F_RELATIONSHIPS +
                "/" + authUID +
                "/" + displayedUID +
                "/timestamp", timestamp);
        toUpdate.put("/" + Constants.F_RELATIONSHIPS +
                "/" + displayedUID +
                "/" + authUID +
                "/timestamp", timestamp);
        return toUpdate;
    }

    private static final HashMap<String, Object> getLastActionByAuthenticated(String authUID, String displayedUID) {
        HashMap<String, Object> toUpdate = new HashMap<>();
        /** for last action user UID */
        toUpdate.put("/" + Constants.F_RELATIONSHIPS +
                "/" + authUID +
                "/" + displayedUID +
                "/actionUserUID", authUID);
        toUpdate.put("/" + Constants.F_RELATIONSHIPS +
                "/" + displayedUID +
                "/" + authUID +
                "/actionUserUID", authUID);
        return toUpdate;
    }

    private static final HashMap<String, Object> getLastActionByDisplayed(String authUID, String displayedUID) {
        HashMap<String, Object> toUpdate = new HashMap<>();
        /** for last action user UID */
        toUpdate.put("/" + Constants.F_RELATIONSHIPS +
                "/" + authUID +
                "/" + displayedUID +
                "/actionUserUID", displayedUID);
        toUpdate.put("/" + Constants.F_RELATIONSHIPS +
                "/" + displayedUID +
                "/" + authUID +
                "/actionUserUID", displayedUID);
        return toUpdate;
    }

    private static final HashMap<String, Object> getStatusMap(String authUID, String displayedUID, int status) {
        HashMap<String, Object> toUpdate = new HashMap<>();
        /** for last action user UID */
        /** for status */
        toUpdate.put("/" + Constants.F_RELATIONSHIPS +
                "/" + authUID +
                "/" + displayedUID +
                "/status", status);
        toUpdate.put("/" + Constants.F_RELATIONSHIPS +
                "/" + displayedUID +
                "/" + authUID +
                "/status", status);
        return toUpdate;
    }

    private static void logHashMap(HashMap<String, Object> map) {
        Set<String> keySet = map.keySet();
        for (String k : keySet) {
            Log.d(TAG, "logHashMap: k: " + k + " v: " + map.get(k));
        }
    }
}
