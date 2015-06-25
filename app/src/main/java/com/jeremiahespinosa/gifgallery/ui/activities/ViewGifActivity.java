package com.jeremiahespinosa.gifgallery.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.models.Gif;
import com.jeremiahespinosa.gifgallery.presenter.GifView;
import com.jeremiahespinosa.gifgallery.presenter.ViewGifPresenter;
import com.jeremiahespinosa.gifgallery.utility.App;
import com.jeremiahespinosa.gifgallery.utility.PrefUtils;

import java.util.Collections;

/**
 * Created by jespinosa on 6/22/15.
 */
public class ViewGifActivity extends Activity implements GifView {

    private static String TAG = "ViewGifActivity";
    public static String GIF_PARCEL_OBJECT = "gif_parcel_object";
    private ImageView gifImageView;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_gif);

        gifImageView = (ImageView) findViewById(R.id.gifImageView);

        getExtras();
    }

    private void getExtras(){

        Gif selectedGif = null;

        Bundle extrasFromIntent = getIntent().getExtras();
        if(extrasFromIntent != null){
            selectedGif = (Gif) extrasFromIntent.getParcelable(GIF_PARCEL_OBJECT);
        }


        //start up the presenter. set it to load the gif based on service
        ViewGifPresenter viewGifPresenter = new ViewGifPresenter(this);

        if(!PrefUtils.getPrefDriveToken().isEmpty()){
            GoogleAccountCredential credential;
            credential = GoogleAccountCredential.usingOAuth2(ViewGifActivity.this, Collections.singleton(DriveScopes.DRIVE));
            credential.setSelectedAccountName(PrefUtils.getPrefDriveUser());
            viewGifPresenter.setGoogleAccountCredential(credential);
        }

        viewGifPresenter.loadGifIntoImageView(selectedGif);

        setupToolbar(selectedGif.getImageName());
    }

    private void setupToolbar(@Nullable String title){
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

    @Override
    public void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = App.getProgressDialog(ViewGifActivity.this, App.getStringById(R.string.downloading));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        if(progressDialog != null)
            progressDialog.dismiss();
    }

    @Override
    public void loadGifToView(String filePathToGif) {
        if(filePathToGif != null && !filePathToGif.isEmpty()){
            Glide.with(ViewGifActivity.this)
                    .load(filePathToGif)
                    .asGif()
                    .crossFade()
                    .placeholder(R.mipmap.ic_launcher)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(gifImageView);
        }
    }
}
