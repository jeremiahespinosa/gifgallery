package com.jeremiahespinosa.gifgallery.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.presenter.ViewGifPresenter;
import com.jeremiahespinosa.gifgallery.utility.App;
import com.jeremiahespinosa.gifgallery.utility.PrefUtils;

import java.util.Collections;

/**
 * Created by jespinosa on 6/22/15.
 */
public class ViewGifActivity extends Activity {

    private static String TAG = "ViewGifActivity";
    private Context mContext;
    public static String GIF_URL_KEY = "gif_url_key";
    public static String GIF_TITLE_KEY = "gif_title_key";
    public static String GIF_BASE_PATH = "gif_base_path";
    private ViewGifPresenter viewGifPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_gif);
        mContext = ViewGifActivity.this;

        final ImageView gifImageView = (ImageView) findViewById(R.id.gifImageView);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.gifProgressBar);

        viewGifPresenter = new ViewGifPresenter(mContext);

        getExtras(gifImageView);
    }

    private void getExtras(final ImageView gifImageView){
        String gifUrl = "";
        String gifTitle = "";
        String basePath = "";

        Bundle extrasFromIntent = getIntent().getExtras();
        if(extrasFromIntent != null){
            basePath = extrasFromIntent.getString(GIF_BASE_PATH);
            gifUrl = extrasFromIntent.getString(GIF_URL_KEY);
            gifTitle = extrasFromIntent.getString(GIF_TITLE_KEY);
        }

        Log.v(TAG, "loading url-->"+gifUrl);
        Log.v(TAG, "loading gifTitle-->"+gifTitle);

        viewGifPresenter.loadGifIntoImageView(gifUrl, basePath, gifImageView);

        setupToolbar(gifTitle);
    }

    private void setupToolbar(String title){
        Toolbar toolbar = (Toolbar) findViewById(R.id.viewGifToolbar);

        if(title != null && !title.isEmpty())
            toolbar.setTitle(title);
        else
            toolbar.setTitle(App.getStringById(R.string.app_name));

        toolbar.setTitleTextColor(App.getColorById(R.color.white));
        toolbar.setNavigationIcon(R.drawable.ic_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
