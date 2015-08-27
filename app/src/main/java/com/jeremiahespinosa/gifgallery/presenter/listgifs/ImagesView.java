package com.jeremiahespinosa.gifgallery.presenter.listgifs;

import android.view.View;

import com.jeremiahespinosa.gifgallery.models.Gif;

import java.util.ArrayList;

/**
 * Created by jespinosa on 6/24/15.
 */
public interface ImagesView {
    void setListOfGifs(ArrayList<Gif> listOfGifs);
    void showLoadingIndicator();
    void hideLoadingIndicator();
    void onGifSelected(Gif gif, View v);
}
