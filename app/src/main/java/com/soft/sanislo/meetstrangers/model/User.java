package com.soft.sanislo.meetstrangers.model;

import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 04.09.16.
 */
public class User {
    public static final int RELATIONSHIP_UNKNOWN = 0;
    public static final int RELATIONSHIP_SINGLE = 11;
    public static final int RELATIONSHIP_MARRIED = 22;
    public static final int RELATIONSHIP_OPEN = 33;

    public static final int SEXUALITY_STRIGHT = 0;
    public static final int SEXUALITY_BISEXUAL = 1;
    public static final int SEXUALITY_OPEN_MINDED = 2;
    public static final int SEXUALITY_GAY = 3;

    public static final int EYE_UNKNOWN = 0;
    public static final int EYE_GREEN = 1;
    public static final int EYE_BROWN = 2;
    public static final int EYE_GREY = 3;
    public static final int EYE_BLUE = 4;
    public static final int EYE_HAZEL = 5;
    public static final int OTHER = 6;

    public static final int SMOKING_SOCIAL = 0;
    public static final int SMOKING_NON = 1;
    public static final int SMOKING_ANTI = 2;
    public static final int SMOKING_SMOKER = 3;

    public static final int DRINKING_SOCIAL = 0;
    public static final int DRINKING_NON = 1;
    public static final int DRINKING_ANTI = 2;
    public static final int DRINKING_YES_PLEASE = 3;

    private String uid;
    private String firstName;
    private String lastName;
    private String fullName;
    private long birthDate;
    private boolean showBirthDate;
    private String homeTown;
    private String liveTown;
    private int gender;
    private String status;  /**Relationship status*/
    private String phoneNumber;
    private String linkInstagram;
    private String linkFacebook;
    private String linkTwitter;
    private String avatarURL;
    private String avatarBlurURL;
    private boolean isOnline;
    private long lastActiveTimestamp;

    private int friendsCount;
    private int followersCount;

    private int height; /** cm?*/
    private int eyeColor;

    private String emailAddress;
    private String websiteAddress;
    private List<Language> languages;
    private List<Workplace> workPlaces;
    private List<SocialLink> socialLinks;

    public User() {}

    public User(String uid, String firstName, String lastName, String fullName, long birthDate, String homeTown, int gender, String status, String phoneNumber, String linkInstagram, String linkFacebook, String linkTwitter, String avatarURL, String avatarBlurURL, boolean isOnline, long lastActiveTimestamp) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.homeTown = homeTown;
        this.gender = gender;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.linkInstagram = linkInstagram;
        this.linkFacebook = linkFacebook;
        this.linkTwitter = linkTwitter;
        this.avatarURL = avatarURL;
        this.avatarBlurURL = avatarBlurURL;
        this.isOnline = isOnline;
        this.lastActiveTimestamp = lastActiveTimestamp;
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

    public long getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(long birthDate) {
        this.birthDate = birthDate;
    }

    public boolean isShowBirthDate() {
        return showBirthDate;
    }

    public void setShowBirthDate(boolean showBirthDate) {
        this.showBirthDate = showBirthDate;
    }

    public String getHomeTown() {
        return homeTown;
    }

    public void setHomeTown(String homeTown) {
        this.homeTown = homeTown;
    }

    public String getLiveTown() {
        return liveTown;
    }

    public void setLiveTown(String liveTown) {
        this.liveTown = liveTown;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
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

    public int getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(int eyeColor) {
        this.eyeColor = eyeColor;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getWebsiteAddress() {
        return websiteAddress;
    }

    public void setWebsiteAddress(String websiteAddress) {
        this.websiteAddress = websiteAddress;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public List<Workplace> getWorkPlaces() {
        return workPlaces;
    }

    public void setWorkPlaces(List<Workplace> workPlaces) {
        this.workPlaces = workPlaces;
    }

    public List<SocialLink> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<SocialLink> socialLinks) {
        this.socialLinks = socialLinks;
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

    public static class Builder {
        private String uid;
        private String firstName;
        private String lastName;
        private String fullName;
        private long birthDate;
        private boolean showBirthDate;
        private String homeTown;
        private String liveTown;
        private int gender;
        private String status;  /**Relationship status*/
        private String phoneNumber;
        private String linkInstagram;
        private String linkFacebook;
        private String linkTwitter;
        private String avatarURL;
        private String avatarBlurURL;
        private boolean isOnline;
        private long lastActiveTimestamp;

        private int friendsCount;
        private int followersCount;

        private int height; /** cm?*/
        private int eyeColor;

        private String emailAddress;
        private String websiteAddress;
        private List<Language> languages;
        private List<Workplace> workPlaces;
        private List<SocialLink> socialLinks;

        public Builder setUid(String uid) {
            this.uid = uid;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setFullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder setBirthDate(long birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Builder setShowBirthDate(boolean showBirthDate) {
            this.showBirthDate = showBirthDate;
            return this;
        }

        public Builder setHomeTown(String homeTown) {
            this.homeTown = homeTown;
            return this;
        }

        public Builder setLiveTown(String liveTown) {
            this.liveTown = liveTown;
            return this;
        }

        public Builder setGender(int gender) {
            this.gender = gender;
            return this;
        }

        public Builder setStatus(String status) {
            this.status = status;
            return this;
        }

        public Builder setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder setLinkInstagram(String linkInstagram) {
            this.linkInstagram = linkInstagram;
            return this;
        }

        public Builder setLinkFacebook(String linkFacebook) {
            this.linkFacebook = linkFacebook;
            return this;
        }

        public Builder setLinkTwitter(String linkTwitter) {
            this.linkTwitter = linkTwitter;
            return this;
        }

        public Builder setAvatarURL(String avatarURL) {
            this.avatarURL = avatarURL;
            return this;
        }

        public Builder setAvatarBlurURL(String avatarBlurURL) {
            this.avatarBlurURL = avatarBlurURL;
            return this;
        }

        public Builder setOnline(boolean online) {
            isOnline = online;
            return this;
        }

        public Builder setLastActiveTimestamp(long lastActiveTimestamp) {
            this.lastActiveTimestamp = lastActiveTimestamp;
            return this;
        }

        public Builder setFriendsCount(int friendsCount) {
            this.friendsCount = friendsCount;
            return this;
        }

        public Builder setFollowersCount(int followersCount) {
            this.followersCount = followersCount;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setEyeColor(int eyeColor) {
            this.eyeColor = eyeColor;
            return this;
        }

        public Builder setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public Builder setWebsiteAddress(String websiteAddress) {
            this.websiteAddress = websiteAddress;
            return this;
        }

        public Builder setLanguages(List<Language> languages) {
            this.languages = languages;
            return this;
        }

        public Builder setWorkPlaces(List<Workplace> workPlaces) {
            this.workPlaces = workPlaces;
            return this;
        }

        public Builder setSocialLinks(List<SocialLink> socialLinks) {
            this.socialLinks = socialLinks;
            return this;
        }

        //Return the finally consrcuted User object
        public User build() {
            User user = new User(uid,
                    firstName,
                    lastName,
                    fullName,
                    birthDate,
                    homeTown,
                    gender,
                    status,
                    phoneNumber,
                    linkInstagram,
                    linkFacebook,
                    linkTwitter,
                    avatarURL,
                    avatarBlurURL,
                    isOnline,
                    lastActiveTimestamp);
            return user;
        }
    }
}
