package com.jeremiahespinosa.gifgallery.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.models.Gifs;
import com.jeremiahespinosa.gifgallery.presenter.ViewGifPresenter;
import com.jeremiahespinosa.gifgallery.utility.App;

/**
 * Created by jespinosa on 6/22/15.
 */
public class ViewGifActivity extends Activity {

    private static String TAG = "ViewGifActivity";
    public static String GIF_PARCEL_OBJECT = "gif_parcel_object";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_gif);
        Context mContext = ViewGifActivity.this;

        final ImageView gifImageView = (ImageView) findViewById(R.id.gifImageView);

        getExtras(gifImageView, mContext);
    }

    private void getExtras(final ImageView gifImageView, Context context){

        Gifs selectedGif = null;

        Bundle extrasFromIntent = getIntent().getExtras();
        if(extrasFromIntent != null){
            selectedGif = (Gifs) extrasFromIntent.getParcelable(GIF_PARCEL_OBJECT);
        }


        //start up the presenter. set it to load the gif based on service
        ViewGifPresenter viewGifPresenter = new ViewGifPresenter(context);
        viewGifPresenter.loadGifIntoImageView(selectedGif, gifImageView);

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

}
