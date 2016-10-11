package com.soft.sanislo.meetstrangers.model;

/**
 * Created by root on 04.09.16.
 */
public class User {
    private String uid;
    private String firstName;
    private String lastName;
    private String fullName;
    private String birthDate;
    private int gender;
    private String status;
    private String phoneNumber;
    private String linkInstagram;
    private String linkFacebook;
    private String linkTwitter;
    private String avatarURL;
    private String avatarBlurURL;
    private boolean isOnline;
    private long lastActiveTimestamp;

    public User() {}

    public User(String firstName, String lastName, String birthDate, int gender, String status, String phoneNumber, String linkInstagram, String linkFacebook, String linkTwitter) {
        this.firstName = firstName;
        this.lastName = lastName;
        fullName = firstName + " " + lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.linkInstagram = linkInstagram;
        this.linkFacebook = linkFacebook;
        this.linkTwitter = linkTwitter;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLinkInstagram() {
        return linkInstagram;
    }

    public void setLinkInstagram(String linkInstagram) {
        this.linkInstagram = linkInstagram;
    }

    public String getLinkFacebook() {
        return linkFacebook;
    }

    public void setLinkFacebook(String linkFacebook) {
        this.linkFacebook = linkFacebook;
    }

    public String getLinkTwitter() {
        return linkTwitter;
    }

    public void setLinkTwitter(String linkTwitter) {
        this.linkTwitter = linkTwitter;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getAvatarBlurURL() {
        return avatarBlurURL;
    }

    public void setAvatarBlurURL(String avatarBlurURL) {
        this.avatarBlurURL = avatarBlurURL;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public long getLastActiveTimestamp() {
        return lastActiveTimestamp;
    }

    public void setLastActiveTimestamp(long lastActiveTimestamp) {
        this.lastActiveTimestamp = lastActiveTimestamp;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", gender=" + gender +
                ", status='" + status + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", linkInstagram='" + linkInstagram + '\'' +
                ", linkFacebook='" + linkFacebook + '\'' +
                ", linkTwitter='" + linkTwitter + '\'' +
                ", avatarURL='" + avatarURL + '\'' +
                ", avatarBlurURL='" + avatarBlurURL + '\'' +
                '}';
    }
}
