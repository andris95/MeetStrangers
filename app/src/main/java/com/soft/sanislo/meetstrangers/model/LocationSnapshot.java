package com.soft.sanislo.meetstrangers.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by root on 04.09.16.
 */
public class LocationSnapshot {
    private String mId;
    private double lat;
    private double lng;
    private String address;
    private long timestamp;
    private String icon;

    public LocationSnapshot() {}

    public LocationSnapshot(String mId,
                            double lat,
                            double lng,
                            String address,
                            long timestamp,
                            String icon) {
        this.mId = mId;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.timestamp = timestamp;
        this.icon = icon;
    }

    public LocationSnapshot(String id, double lat, double lng, long timestamp, String icon) {
        mId = id;
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
        this.icon = icon;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "LocationSnapshot{" +
                "mId='" + mId + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", timestamp=" + timestamp +
                ", icon='" + icon + '\'' +
                '}';
    }
}
