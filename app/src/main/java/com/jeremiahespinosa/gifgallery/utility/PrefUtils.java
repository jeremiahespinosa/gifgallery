package com.jeremiahespinosa.gifgallery.utility;

/**
 * Created by jespinosa on 6/22/15.
 */
public class PrefUtils {

    public static final String PREF_DRIVE_USER = "pref_drive_user";
    public static final String PREF_DRIVE_TOKEN = "pref_drive_token";
    public static final String PREF_DROPBOX_ACCESS_TOKEN = "pref_dropbox_access_token";

    public static String getPrefDriveUser() {
        return App.getSharedPrefString(PREF_DRIVE_USER);
    }

    public static String getPrefDriveToken() {
        return App.getSharedPrefString(PREF_DRIVE_TOKEN);
    }

    public static void setPrefGoogleDriveUser(String user){
        App.putSharedPref(PREF_DRIVE_USER, user);
    }

    public static void setPrefGoogleDriveToken(String token){
        App.putSharedPref(PREF_DRIVE_TOKEN, token);
    }

    public static void setPrefDropboxAccessToken(String accessToken){
        App.putSharedPref(PREF_DROPBOX_ACCESS_TOKEN, accessToken);
    }

    public static String getPrefDropboxAccessToken(){
        return App.getSharedPrefString(PREF_DROPBOX_ACCESS_TOKEN);
    }
}
