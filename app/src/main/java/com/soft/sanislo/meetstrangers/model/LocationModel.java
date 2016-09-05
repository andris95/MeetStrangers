package com.soft.sanislo.meetstrangers.model;

import android.location.Location;

/**
 * Created by root on 04.09.16.
 */
public class LocationModel {
    private double lat;
    private double lng;
    private long timestamp;

    public LocationModel() {}

    public LocationModel(double lat, double lng, long timestamp) {
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
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
}
