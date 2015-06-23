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
    private Context mContext;

    public ViewGifPresenter(Context context) {
        mContext = context;
    }

    public void loadGifIntoImageView(Gif selectedGif, ImageView gifImageView){

        if(selectedGif != null){

            if(selectedGif.getGifSource().equals(App.getStringById(R.string.title_dropbox))){
                getImageFromDropbox(selectedGif.getFullImageToLoadPath(), gifImageView);
            }
            else if(selectedGif.getGifSource().equals(App.getStringById(R.string.title_drive))){
                getImageFromGoogleDrive(selectedGif.getFullImageToLoadPath(), gifImageView);
            }
            else{
                //local file so just load the image
                Glide.with(mContext)
                        .load(selectedGif.getFullImageToLoadPath())
                        .asGif()
                        .crossFade()
                        .placeholder(R.mipmap.ic_launcher)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(gifImageView);
            }
        }
        else{
            App.showShortToast("Unable to load your image");
        }
    }

    private void getImageFromGoogleDrive(String basePath, ImageView gifImageView){
        GoogleAccountCredential credential;
        credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(DriveScopes.DRIVE));
        credential.setSelectedAccountName(PrefUtils.getPrefDriveUser());

        Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).setApplicationName(App.getStringById(R.string.app_name)).build();
        new DownloadFileFromGoogleDrive(mContext, service, gifImageView, basePath).execute();
    }

    protected void getImageFromDropbox(String basePath, ImageView gifImageView){
        new DownloadFileFromDropbox(mContext, basePath, gifImageView).execute();
    }

    private class DownloadFileFromDropbox extends AsyncTask<Void, Void, String> {
        ProgressDialog mDialog;
        private String dropboxGifPath;
        ImageView imageView = null;

        public DownloadFileFromDropbox(Context context, String basePath, ImageView gifImageView) {
            dropboxGifPath = basePath;
            imageView = gifImageView;

            mContext = context.getApplicationContext();

            mDialog = new ProgressDialog(context);
            mDialog.setMessage("Downloading Image");
            mDialog.show();
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
            if(mDialog != null)
                mDialog.dismiss();

            if(gifFilePath != null && !gifFilePath.isEmpty()){
                Glide.with(mContext)
                        .load(gifFilePath)
                        .asGif()
                        .crossFade()
                        .placeholder(R.mipmap.ic_launcher)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(imageView);
            }
        }

    }

    private class DownloadFileFromGoogleDrive extends AsyncTask <Void, Void, String>{
        ProgressDialog progressDialog;
        private Drive driveService;
        private ImageView imageView;
        private String pathToDownload;

        public DownloadFileFromGoogleDrive(Context context, Drive fileSelected, ImageView gifImageView, String basePath){
            driveService = fileSelected;
            pathToDownload = basePath;
            imageView = gifImageView;
            
            progressDialog = App.getProgressDialog(context, "Downloading");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();
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
            if(progressDialog != null)
                progressDialog.dismiss();

            if(gifFilePath != null && !gifFilePath.isEmpty()){
                Glide.with(mContext)
                        .load(gifFilePath)
                        .asGif()
                        .crossFade()
                        .placeholder(R.mipmap.ic_launcher)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(imageView);
            }
        }
    }

}