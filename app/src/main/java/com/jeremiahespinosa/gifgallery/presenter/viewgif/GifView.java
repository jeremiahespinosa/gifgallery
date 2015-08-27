package com.jeremiahespinosa.gifgallery.presenter.viewgif;

/**
 * Created by jespinosa on 6/24/15.
 */
public interface GifView {
    void showProgressDialog();
    void hideProgressDialog();
    void loadGifToView(String filePathToGif);
}
