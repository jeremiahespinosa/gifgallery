package com.jeremiahespinosa.gifgallery.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.ui.fragments.SettingsPreferenceFragment;
import com.jeremiahespinosa.gifgallery.utility.App;

/**
 * Created by jespinosa on 6/22/15.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_PREF_DROPBOX = "key_dropbox_preference";
    public static final String KEY_PREF_GOOGLE_DRIVE = "key_google_drive_preference";
    private static String SETTINGS_TAG = "SETTINGS_FRAGMENT";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        setUpToolbar();

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.SettingsMainFragmentContainer, new SettingsPreferenceFragment(), SETTINGS_TAG)
                .commit();
    }

    private void setUpToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.settingsToolbar);
        toolbar.setTitle(App.getStringById(R.string.action_settings));
        toolbar.setNavigationIcon(R.drawable.ic_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        getFragmentManager().findFragmentByTag(SETTINGS_TAG).onActivityResult(requestCode, resultCode, data);
    }
}
