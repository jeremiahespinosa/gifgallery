package com.jeremiahespinosa.gifgallery.ui.fragments;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.ui.activities.SettingsActivity;
import com.jeremiahespinosa.gifgallery.utility.App;
import com.jeremiahespinosa.gifgallery.utility.PrefUtils;

import java.io.IOException;
import java.util.Collections;

/**
 * Created by jespinosa on 6/22/15.
 */
public class SettingsPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private static String TAG = "SettingsPreferenceFragment";
    /**
     * Request code for auto Google Play Services error resolution.
     */
    public static final int REQUEST_CODE_RESOLUTION = 1;
    public static final int COMPLETE_AUTHORIZATION_REQUEST_CODE = 35;

    public static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    private static final int REQUEST_AUTHORIZATION_CODE = 1993;

    Preference dropboxPreference;
    Preference googleDrivePreference;

    public static String dropboxAppKey = "wyrfili22wvit59";
    public static String dropboxAppSecret = "gqh8gdzgp8sctak";

    GoogleAccountCredential credential;

    // In the class declaration section:
    private DropboxAPI<AndroidAuthSession> mDBApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        // And later in some initialization function:
        AppKeyPair appKeys = new AppKeyPair(dropboxAppKey, dropboxAppSecret);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        dropboxPreference = findPreference(SettingsActivity.KEY_PREF_DROPBOX);
        googleDrivePreference = findPreference(SettingsActivity.KEY_PREF_GOOGLE_DRIVE);

        dropboxPreference.setOnPreferenceClickListener(this);
        googleDrivePreference.setOnPreferenceClickListener(this);

        if (!PrefUtils.getPrefDropboxAccessToken().isEmpty()) {
            dropboxPreference.setTitle(App.getStringById(R.string.currently_in_dropbox));
        }
        if ( !PrefUtils.getPrefDriveToken().isEmpty()) {
            googleDrivePreference.setTitle(App.getStringById(R.string.currently_in_drive));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                String accessToken = mDBApi.getSession().getOAuth2AccessToken();

                PrefUtils.setPrefDropboxAccessToken(accessToken);

                dropboxPreference.setTitle(App.getStringById(R.string.currently_in_dropbox));

            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String preferenceClicked = preference.getKey();
        if(App.hasInternetConnection()){
            if (preferenceClicked.equals(SettingsActivity.KEY_PREF_DROPBOX)) {

                if (PrefUtils.getPrefDropboxAccessToken().isEmpty()) {
                    mDBApi.getSession().startOAuth2Authentication(getActivity());
                }
                else {
                    mDBApi.getSession().unlink();
                    PrefUtils.setPrefDropboxAccessToken("");
                    dropboxPreference.setTitle(App.getStringById(R.string.sign_in_dropbox));
                }
            }

            else if (preferenceClicked.equals(SettingsActivity.KEY_PREF_GOOGLE_DRIVE)) {

                if(PrefUtils.getPrefDriveToken().isEmpty()) {   //user needs to pick an email to sign in and authorize
                    pickUserAccount();
                }
                else {   //user is already signed in so sign them out

                    credential.getGoogleAccountManager().invalidateAuthToken(PrefUtils.getPrefDriveToken());

                    PrefUtils.setPrefGoogleDriveToken("");
                    googleDrivePreference.setTitle(App.getStringById(R.string.sign_in_drive));
                }
            }
        }
        else{
            App.showShortToast(App.getStringById(R.string.no_network));
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CODE_PICK_ACCOUNT:
                if (resultCode == Activity.RESULT_OK) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

                    //save account name
                    PrefUtils.setPrefGoogleDriveUser(accountName);

                    credential = GoogleAccountCredential.usingOAuth2(getActivity(), Collections.singleton(DriveScopes.DRIVE));

                    credential.setSelectedAccountName(accountName);

                    new GetTokenAsyncTask(getActivity()).execute();
                }
                else {
                    Log.i(TAG, "user cancelled request code pick");
                }
                break;
            case COMPLETE_AUTHORIZATION_REQUEST_CODE:

                if (resultCode == Activity.RESULT_OK) {
                    googleDrivePreference.setTitle(App.getStringById(R.string.currently_in_drive));
                    // App is authorized, you can go back to sending the API request
                }
                else {
                    googleDrivePreference.setTitle(App.getStringById(R.string.sign_in_drive));
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_CODE_PICK_ACCOUNT);
                    // User denied access, show him the account chooser again
                }
                break;
            case REQUEST_CODE_RESOLUTION:
                googleDrivePreference.setTitle(App.getStringById(R.string.currently_in_drive));
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, true, null, null, null, null);
        getActivity().startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private class GetTokenAsyncTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog mDialog;

        public GetTokenAsyncTask(Context context) {
            mDialog = new ProgressDialog(context);
            mDialog.setMessage("Authorizing");
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try{
                String access_token = credential.getToken();

                PrefUtils.setPrefGoogleDriveToken(access_token);

                Log.d(TAG, "saving access_token: " + access_token);

            } catch (UserRecoverableAuthIOException e) {
                e.printStackTrace();
                startActivityForResult(e.getIntent(), COMPLETE_AUTHORIZATION_REQUEST_CODE);
                return false;
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return !PrefUtils.getPrefDriveToken().isEmpty();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(mDialog != null)
                mDialog.dismiss();

            if(result) {
                googleDrivePreference.setTitle(App.getStringById(R.string.currently_in_drive));
            }

        }
    }
}