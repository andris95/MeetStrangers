package com.soft.sanislo.meetstrangers.model;

/**
 * Created by root on 04.09.16.
 */
public class User {
    private String id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String birthDate;
    private boolean isMale;
    private String status;
    private String phoneNumber;
    private String linkInstagram;
    private String linkFacebook;
    private String linkTwitter;
    private String avatarURL;

    public User() {}

    public User(String firstName, String lastName, String birthDate, boolean isMale, String status, String phoneNumber, String linkInstagram, String linkFacebook, String linkTwitter) {
        this.firstName = firstName;
        this.lastName = lastName;
        fullName = firstName + " " + lastName;
        this.birthDate = birthDate;
        this.isMale = isMale;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.linkInstagram = linkInstagram;
        this.linkFacebook = linkFacebook;
        this.linkTwitter = linkTwitter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isMale() {
        return isMale;
    }

    public void setMale(boolean male) {
        isMale = male;
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

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", isMale=" + isMale +
                ", status='" + status + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", linkInstagram='" + linkInstagram + '\'' +
                ", linkFacebook='" + linkFacebook + '\'' +
                ", linkTwitter='" + linkTwitter + '\'' +
                ", avatarURL='" + avatarURL + '\'' +
                '}';
    }
}
