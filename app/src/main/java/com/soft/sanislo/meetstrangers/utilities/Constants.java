package com.soft.sanislo.meetstrangers.utilities;

/**
 * Created by root on 04.09.16.
 */
public final class Constants {
    public static final int RC_PICK_IMAGE_GALLERY = 22228;
    public static final int RC_PICK_IMAGE_CAMERA = 22229;

    /**
     * Constants related to locations in Firebase, such as the name of the node
     * where user lists are stored (ie "userLists")
     */
    public static final String F_USERS = "users";
    public static final String F_LOCATIONS = "locations";
    public static final String F_POSTS = "posts";

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


    /**
     * Constants for Firebase login
     */
    public static final String PASSWORD_PROVIDER = "password";
    public static final String GOOGLE_PROVIDER = "google";
    public static final String PROVIDER_DATA_DISPLAY_NAME = "displayName";
}