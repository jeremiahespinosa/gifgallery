package com.jeremiahespinosa.gifgallery.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.presenter.ViewGifPresenter;
import com.jeremiahespinosa.gifgallery.utility.App;

/**
 * Created by jespinosa on 6/22/15.
 */
public class ViewGifActivity extends Activity {

    private static String TAG = "ViewGifActivity";
    public static String GIF_URL_KEY = "gif_url_key";
    public static String GIF_TITLE_KEY = "gif_title_key";
    public static String GIF_BASE_PATH = "gif_base_path";
    public static String GIF_SOURCE = "gif_source";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_gif);
        Context mContext = ViewGifActivity.this;

        final ImageView gifImageView = (ImageView) findViewById(R.id.gifImageView);

        getExtras(gifImageView, mContext);
    }

    private void getExtras(final ImageView gifImageView, Context context){
        //could simplify by converting Gifs into parcelable
        String gifUrl = "";
        String gifTitle = "";
        String basePath = "";
        String gifSource = "";

        Bundle extrasFromIntent = getIntent().getExtras();
        if(extrasFromIntent != null){
            basePath = extrasFromIntent.getString(GIF_BASE_PATH);
            gifUrl = extrasFromIntent.getString(GIF_URL_KEY);
            gifTitle = extrasFromIntent.getString(GIF_TITLE_KEY);
            gifSource = extrasFromIntent.getString(GIF_SOURCE);
        }


        //start up the presenter. set it to load the image based on service
        ViewGifPresenter viewGifPresenter = new ViewGifPresenter(context);
        viewGifPresenter.loadGifIntoImageView(gifSource, gifUrl, basePath, gifImageView);

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
