package com.soft.sanislo.meetstrangers.model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by root on 04.09.16.
 */
public class LocationModel {
    private String mId;
    private double lat;
    private double lng;
    private long timestamp;
    private String icon;

    public LocationModel() {}

    public LocationModel(String mId, double lat, double lng, long timestamp, String icon) {
        this.mId = mId;
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

    /*public Location getLocation() {
        Location location = new Location("");
        location.setLongitude(lng);
        location.setLatitude(lat);
        return location;
    }

    public LatLng getLatLng() {
        LatLng latLng = new LatLng(lat, lng);
        return latLng;
    }*/

    @Override
    public String toString() {
        return "LocationModel{" +
                "mId='" + mId + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                ", timestamp=" + timestamp +
                ", icon='" + icon + '\'' +
                '}';
    }
}
