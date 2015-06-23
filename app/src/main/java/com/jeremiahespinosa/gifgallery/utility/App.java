package com.jeremiahespinosa.gifgallery.utility;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * Created by jespinosa on 6/22/15.
 */
public class App extends Application {

    public static final int GINGERBREAD = 9;
    public static final int HONEYCOMB = 11;
    public static final int ICE_CREAM_SANDWICH = 14;
    public static final int ICE_CREAM_SANDWICH_MR1 = 15;
    public static final int JELLY_BEAN = 16;
    public static final int KITKAT = 19;
    public static final int LOLLIPOP = 21;
    private static Context mContext;
    private SharedPreferences sharedPreferences;
    private static App applicationInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        applicationInstance = this;

        sharedPreferences = mContext.getSharedPreferences(getPackageName(), MODE_PRIVATE);
    }

    public static SharedPreferences getSharedPreferences() {
        return applicationInstance.sharedPreferences;
    }

    public static String getSharedPrefString(String key, String defValue) {
        return applicationInstance.sharedPreferences.getString(key, defValue);
    }

    public static String getSharedPrefString(String key) {
        return applicationInstance.sharedPreferences.getString(key, "");
    }

    public static int getSharedPrefInteger(String key, int defValue) {
        return applicationInstance.sharedPreferences.getInt(key, defValue);
    }

    public static long getSharedPrefLong(String key, long defValue) {
        return applicationInstance.sharedPreferences.getLong(key, defValue);
    }

    public static boolean getSharedPrefBool(String key, boolean defValue) {
        return applicationInstance.sharedPreferences.getBoolean(key, defValue);
    }

    public static void putSharedPref(String key, String value) {
        applicationInstance.sharedPreferences.edit().putString(key, value).apply();
    }

    public static void putSharedPref(String key, boolean value) {
        applicationInstance.sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static void putSharedPref(String key, int value) {
        applicationInstance.sharedPreferences.edit().putInt(key, value).apply();
    }

    public static void putSharedPref(String key, long value) {
        applicationInstance.sharedPreferences.edit().putLong(key, value).apply();
    }

    /**
     * Displays a toast to the duration LENGTH_SHORT
     *
     * @param message
     */
    public static void showShortToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Gets Color value from xml resources
     * @param colorId = id of color to get
     * @return color value
     */
    public static int getColorById(int colorId){
        return mContext.getResources().getColor(colorId);
    }

    /**
     * Gets String value from xml resources
     * @param stringId = id of string to get
     * @return string value
     */
    public static String getStringById(int stringId){
        return mContext.getResources().getString(stringId);
    }

    /**
     * Gets integer value from xml resources
     * @param integerId = id of integer to get
     * @return integer value
     */
    public static Integer getIntegerById(int integerId){
        return mContext.getResources().getInteger(integerId);
    }

    /**
     *
     * @return true if the Internet connection is available, false if not
     *         available
     */
    public static boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isAvailable()
                && networkInfo.isConnected();
    }

    /**
     * @param context
     * @param message
     * @return a built progress dialog
     */
    public static ProgressDialog getProgressDialog(Context context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;
                }
                return false;
            }
        });

        return progressDialog;
    }

    /**
     *
     * @param context
     * @param messageId
     * @return a built progress dialog
     */
    public static ProgressDialog getProgressDialog(Context context, int messageId) {
        return getProgressDialog(context, getStringById(messageId));
    }
}
