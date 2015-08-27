package com.jeremiahespinosa.gifgallery.presenter.viewgif;

import android.os.AsyncTask;

import com.dropbox.client2.exception.DropboxException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.models.Gif;
import com.jeremiahespinosa.gifgallery.presenter.viewgif.GifView;
import com.jeremiahespinosa.gifgallery.utility.App;
import com.jeremiahespinosa.gifgallery.utility.StorageUtils;
import java.io.IOException;
import java.io.InputStream;

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
                getImageFromDropbox(selectedGif.getFullImageToLoadPath(), selectedGif.getImageName());
            }
            else if(selectedGif.getGifSource().equals(App.getStringById(R.string.title_drive))){
                getImageFromGoogleDrive(selectedGif.getFullImageToLoadPath(), selectedGif.getImageName());
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

    private void getImageFromGoogleDrive(String basePath, String baseName){
        Drive service = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new GsonFactory(),
                getGoogleAccountCredential()).setApplicationName(App.getStringById(R.string.app_name)).build();

        new DownloadFileFromGoogleDrive(service, basePath, baseName).execute();
    }

    protected void getImageFromDropbox(String basePath, String baseName){
        new DownloadFileFromDropbox(basePath, baseName).execute();
    }

    private class DownloadFileFromDropbox extends AsyncTask<Void, Void, String> {

        private String dropboxGifPath;
        private String dropboxFileName;

        public DownloadFileFromDropbox(String basePath, String fileName) {
            dropboxGifPath = basePath;
            dropboxFileName = fileName;
            gifView.showProgressDialog();
        }

        @Override
        protected String doInBackground(Void... params) {
            InputStream dropboxFileContents = null;
            try{
                dropboxFileContents = StorageUtils.downloadDropboxFile(dropboxGifPath);
            }
            catch (DropboxException e){
                e.printStackTrace();
            }
            catch (IOException e){
                e.printStackTrace();
            }

            String destinationFilePath = "";

            if(App.isStringNullOrEmpty(dropboxFileName)){
                destinationFilePath =
                        App.buildGenericNameWithPath(
                            StorageUtils.getStorageDirectory(App.getStringById(R.string.app_name)).getPath()
                                + java.io.File.separator,
                                "gif");
            }
            else{
                destinationFilePath =
                        StorageUtils.getStorageDirectory(App.getStringById(R.string.app_name)).getPath()
                        + java.io.File.separator
                        + dropboxFileName;
            }

            java.io.File fileOnDisk = new java.io.File(destinationFilePath);

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
        private String driveFileName;

        public DownloadFileFromGoogleDrive(Drive fileSelected, String basePath, String fileName){
            driveService = fileSelected;
            pathToDownload = basePath;
            gifView.showProgressDialog();
            driveFileName = fileName;
        }

        @Override
        protected String doInBackground(Void... params) {

            InputStream driveFileContents = StorageUtils.downloadGoogleDriveFile(driveService, pathToDownload);

            String destinationFilePath = "";

            if(App.isStringNullOrEmpty(driveFileName)){
                destinationFilePath =
                        App.buildGenericNameWithPath(
                                StorageUtils.getStorageDirectory(App.getStringById(R.string.app_name)).getPath()
                                        + java.io.File.separator,
                                "gif");
            }
            else{
                destinationFilePath =
                        StorageUtils.getStorageDirectory(App.getStringById(R.string.app_name)).getPath()
                                + java.io.File.separator
                                + driveFileName;
            }

            //create file
            java.io.File fileOnDisk = new java.io.File(destinationFilePath);

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