package com.jeremiahespinosa.gifgallery.presenter;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.dropbox.client2.exception.DropboxException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.models.Gif;
import com.jeremiahespinosa.gifgallery.utility.App;
import com.jeremiahespinosa.gifgallery.utility.PrefUtils;
import com.jeremiahespinosa.gifgallery.utility.StorageUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * This class will handle all the heavy lifting
 * for the ViewGifActivity.
 *
 * If the user is trying to view a gif from a
 * service then the image will be downloaded
 * first otherwise the gif will be set to load
 *
 * Created by jespinosa on 6/23/15.
 */
public class ViewGifPresenter {

    private static String TAG = "ViewGifPresenter";
    private GifView gifView;
    private GoogleAccountCredential googleAccountCredential;

    public ViewGifPresenter(GifView gifView) {
        this.gifView = gifView;
    }

    public void setGoogleAccountCredential(GoogleAccountCredential credential){
        googleAccountCredential = credential;
    }

    public GoogleAccountCredential getGoogleAccountCredential(){
        return googleAccountCredential;
    }

    public void loadGifIntoImageView(Gif selectedGif){

        if(selectedGif != null){

            if(selectedGif.getGifSource().equals(App.getStringById(R.string.title_dropbox))){
                getImageFromDropbox(selectedGif.getFullImageToLoadPath());
            }
            else if(selectedGif.getGifSource().equals(App.getStringById(R.string.title_drive))){
                getImageFromGoogleDrive(selectedGif.getFullImageToLoadPath());
            }
            else{
                //local file so just load the image
                gifView.loadGifToView(selectedGif.getFullImageToLoadPath());
            }
        }
        else{
            App.showShortToast(App.getStringById(R.string.unable_to_load));
        }
    }

    private void getImageFromGoogleDrive(String basePath){
        Drive service = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                getGoogleAccountCredential()).setApplicationName(App.getStringById(R.string.app_name)).build();

        new DownloadFileFromGoogleDrive(service, basePath).execute();
    }

    protected void getImageFromDropbox(String basePath){
        new DownloadFileFromDropbox(basePath).execute();
    }

    private class DownloadFileFromDropbox extends AsyncTask<Void, Void, String> {

        private String dropboxGifPath;

        public DownloadFileFromDropbox(String basePath) {
            dropboxGifPath = basePath;
            gifView.showProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {
            InputStream dropboxFileContents = null;
            try{
                dropboxFileContents = StorageUtils.downloadFile(dropboxGifPath);
            }
            catch (DropboxException e){
                e.printStackTrace();
            }
            catch (IOException e){
                e.printStackTrace();
            }

            String dateBasedFileName_timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US).format(new Date());

            String destinationFilename =
                    StorageUtils.getStorageDirectory(App.getStringById(R.string.app_name)).getPath()
                    + java.io.File.separator
                    + "GIF_"+dateBasedFileName_timeStamp + ".gif";

            java.io.File fileOnDisk = new java.io.File(destinationFilename);

            if(dropboxFileContents != null){
                StorageUtils.copyInputStreamToFile(dropboxFileContents, fileOnDisk);
            }

            return fileOnDisk.getPath();
        }

        @Override
        protected void onPostExecute(String gifFilePath) {
            gifView.loadGifToView(gifFilePath);
            gifView.hideProgressDialog();
        }

    }

    private class DownloadFileFromGoogleDrive extends AsyncTask <Void, Void, String>{
        private Drive driveService;
        private String pathToDownload;

        public DownloadFileFromGoogleDrive(Drive fileSelected, String basePath){
            driveService = fileSelected;
            pathToDownload = basePath;
            gifView.showProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {

            InputStream driveFileContents = StorageUtils.downloadFile(driveService, pathToDownload);

            String dateBasedFileName_timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US).format(new Date());

            String destinationFilename =
                    StorageUtils.getStorageDirectory(App.getStringById(R.string.app_name)).getPath()
                            + java.io.File.separator
                            + "GIF_"+dateBasedFileName_timeStamp + ".gif";

            //create file
            java.io.File fileOnDisk = new java.io.File(destinationFilename);

            //trying to write stream to the created file
            if(driveFileContents != null){
                StorageUtils.copyInputStreamToFile(driveFileContents, fileOnDisk);
            }

            return fileOnDisk.getPath();
        }

        @Override
        protected void onPostExecute(String gifFilePath) {
            gifView.loadGifToView(gifFilePath);
            gifView.hideProgressDialog();
        }
    }

}