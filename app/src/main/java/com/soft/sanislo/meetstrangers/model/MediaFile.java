package com.soft.sanislo.meetstrangers.model;

/**
 * Created by root on 15.10.16.
 */

public class MediaFile {
    private int width;
    private int height;
    private String mimeType;
    private String url;

    public MediaFile() {}

    public MediaFile(int width, int height, String url) {
        this.width = width;
        this.height = height;
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String toString() {
        return "MediaFile{" +
                "width=" + width +
                ", height=" + height +
                ", mimeType='" + mimeType + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
