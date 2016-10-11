package com.soft.sanislo.meetstrangers.utilities;

/**
 * Created by root on 04.09.16.
 */
public final class Constants {
    public static final int RC_PICK_IMAGE_GALLERY = 22228;
    public static final int RC_PICK_IMAGE_CAMERA = 22229;

    public static final int RS_FRIENDS = 11228;
    public static final int RS_PENDING = 11229;
    public static final int RS_STRANGERS = 11230;
    public static final int RS_UNKNOWN = 11231;

    /**
     * Constants related to locations in Firebase
     */
    public static final String F_USERS = "users";
    public static final String F_RELATIONSHIPS = "relationships";
    public static final String F_USERS_FRIENDS = "users_friends";
    public static final String F_USERS_ALL = "users_all";
    public static final String F_USERS_FOLLOWERS = "users_followers";
    public static final String F_USERS_FOLLOWING = "users_following";
    public static final String F_LOCATIONS = "locations";
    public static final String F_POSTS = "posts";
    public static final String F_POSTS_COMMENTS = "posts_comments";
    public static final String F_CHATS = "chats";
    public static final String F_CHATS_HEADERS = "chats_headers";
    public static final String STORAGE_PHOTO_ALBUMS = "photo_albums";
    public static final String STORAGE_ALBUM_PROFILE_PHOTOS = "profile_photos";

    /**
     * Constants for Firebase object properties
     */
    public static final String POST_AUTH_AVATAR_URL = "authorAvatarURL";
    public static final String POST_AUTH_FULL_NAME = "authFullName";

    /**
     * Constants for Firebase URL
     */
    public static final String STORAGE_BUCKET = "gs://meetstranger-142314.appspot.com";

    /**
     * Constants for bundles, extras and shared preferences keys
     */
    public static final String KEY_PROVIDER = "PROVIDER";
    public static final String KEY_ENCODED_EMAIL = "ENCODED_EMAIL";

    /**
     * Constants for Firebase login
     */
    public static final String PASSWORD_PROVIDER = "password";
    public static final String GOOGLE_PROVIDER = "google";
    public static final String PROVIDER_DATA_DISPLAY_NAME = "displayName";
}