package com.jeremiahespinosa.gifgallery.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ProgressBar;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.ui.adapter.ImagePreviewAdapter;
import com.jeremiahespinosa.gifgallery.ui.fragments.SettingsPreferenceFragment;
import com.jeremiahespinosa.gifgallery.utility.App;
import com.jeremiahespinosa.gifgallery.utility.PrefUtils;
import com.jeremiahespinosa.gifgallery.models.Gifs;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class will handle all the heavy lifting for the ImagesFragment.
 * The user can load gifs from storage, load gifs from dropbox, and
 * load gifs from google drive
 *
 * Created by jespinosa on 6/23/15.
 */
public class ImagesFragmentPresenter {

    private static String TAG = "ImagesFragmentPresenter";
    private Cursor mCursor;
    private Context mContext;
    private ProgressBar loadingIndicator;
    private ImagePreviewAdapter imagePreviewAdapter;
    private CardView emptyGifsCard;

    public ImagesFragmentPresenter(Context context, ProgressBar progressBar, ImagePreviewAdapter adapter, CardView cardView) {
        mContext = context;
        loadingIndicator = progressBar;
        imagePreviewAdapter = adapter;
        emptyGifsCard = cardView;
    }

    public void loadGifsFromStorage(){

        showLoadingIndicator();

        if(mCursor == null){
            mCursor = mContext.getContentResolver().query(MediaStore.Files.getContentUri("external"),
                    null, null, null, null);
        }

        while(mCursor.moveToNext()){

            // get the media type.
            int mediaType = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE));

            if(mediaType != MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE){
                continue;
            }

            String filePath  = mCursor.getString(mCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));

            if(filePath.contains("gif")){
                //we only want gifs to be loaded

                File gifFile = new File(filePath);
                if(gifFile.exists()){

                    Gifs gif = new Gifs(filePath, filePath, null, App.getStringById(R.string.title_local));

                    imagePreviewAdapter.addAnotherItem(gif);
                }
            }
        }

        if(imagePreviewAdapter.getItemCount() < 1){
            App.showShortToast("No gifs found");
            showEmptyGifsCard();
        }

        hideLoadingIndicator();
    }

    public void loadGifsFromDropbox(){

        if(App.hasInternetConnection()){

            new LoadDropboxGifsTask().execute();
        }
        else{
            App.showShortToast(App.getStringById(R.string.no_network));
        }
    }

    public void loadGifsFromGoogleDrive(){

        GoogleAccountCredential credential;
        credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(DriveScopes.DRIVE));
        credential.setSelectedAccountName(PrefUtils.getPrefDriveUser());

        Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).setApplicationName(App.getStringById(R.string.app_name)).build();

        new LoadFilesFromGoogleDriveTask(service).execute();
    }

    private class LoadDropboxGifsTask extends AsyncTask<Void, Long, ArrayList<Gifs>> {

        public LoadDropboxGifsTask() {
            showLoadingIndicator();
        }

        @Override
        protected ArrayList<Gifs> doInBackground(Void... params) {
            ArrayList<Gifs> listOfGifsAvailable = new ArrayList<>();

            try {

                AppKeyPair appKeys = new AppKeyPair(SettingsPreferenceFragment.dropboxAppKey, SettingsPreferenceFragment.dropboxAppSecret);
                AndroidAuthSession session = new AndroidAuthSession(appKeys);
                session.setOAuth2AccessToken(PrefUtils.getPrefDropboxAccessToken());
                DropboxAPI<AndroidAuthSession> mDBApi = new DropboxAPI<AndroidAuthSession>(session);

                DropboxAPI.DeltaPage<DropboxAPI.Entry> deltaPage;
                String cursor ="";

                do {
                    //calling delta gives us all files
                    deltaPage = mDBApi.delta(cursor);
                    cursor = deltaPage.cursor;

                    for(DropboxAPI.DeltaEntry existingEntry : deltaPage.entries){

                        //looking for only gif file types
                        if(((DropboxAPI.Entry)existingEntry.metadata).mimeType != null &&
                                ((DropboxAPI.Entry)existingEntry.metadata).mimeType.contains("image/gif")){

                            String thumbnailUrl = "https://api-content.dropbox.com/1/thumbnails/auto"
                                    +((DropboxAPI.Entry)existingEntry.metadata).path
                                    +"?"
                                    +"format=jpeg&"+"size=m&"+"access_token="+PrefUtils.getPrefDropboxAccessToken();

                            Gifs gif = new Gifs(
                                        ((DropboxAPI.Entry)existingEntry.metadata).path,
                                        thumbnailUrl,
                                        ((DropboxAPI.Entry) existingEntry.metadata).fileName(),
                                        App.getStringById(R.string.title_dropbox));

                            listOfGifsAvailable.add(gif);
                        }
                    }
                } while (deltaPage.hasMore);

                return listOfGifsAvailable;

            } catch (DropboxException e) {
                e.printStackTrace();
            }

            return listOfGifsAvailable;
        }

        @Override
        protected void onPostExecute( ArrayList<Gifs> result) {

            for(Gifs gif : result){
                imagePreviewAdapter.addAnotherItem(gif);
            }

            if(imagePreviewAdapter.getItemCount() < 1){
                showEmptyGifsCard();
            }

            hideLoadingIndicator();
        }

    }

    //Loading the thumbnails from drive using the drive sdk
    private class LoadFilesFromGoogleDriveTask extends AsyncTask<Void, Void, ArrayList<Gifs>>{

        private Drive mService;

        public LoadFilesFromGoogleDriveTask(Drive service) {
            showLoadingIndicator();
            mService = service;
        }

        @Override
        protected ArrayList<Gifs> doInBackground(Void... params) {
            ArrayList<Gifs> result = null;

            try{
                result = retrieveAllFiles(mService);
            }
            catch(IOException e){
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Gifs> gifs) {

            for(Gifs gif : gifs){
                imagePreviewAdapter.addAnotherItem(gif);
            }

            if(imagePreviewAdapter.getItemCount() < 1){
                showEmptyGifsCard();
            }

            hideLoadingIndicator();
        }
    }

    /**
     * https://developers.google.com/drive/v2/reference/files/list#try-it
     *
     * Retrieve a list of File resources.
     *
     * @param service Drive API service instance.
     * @return List of File resources.
     */
    private static ArrayList<Gifs> retrieveAllFiles(Drive service) throws IOException {
        ArrayList<Gifs> result = new ArrayList<>();
        Drive.Files.List request = service.files().list();

        do {
            try {
                //requesting list of files in google drive
                FileList files = request.execute();

                for(int i = 0; i < files.getItems().size(); i++){
                    if( (files.getItems().get(i).getMimeType() != null) && files.getItems().get(i).getMimeType().contains("gif") ){
                        if(files.getItems().get(i).getThumbnailLink() != null && !files.getItems().get(i).getThumbnailLink().isEmpty()){

                            Gifs gif = new Gifs(
                                    files.getItems().get(i).getDownloadUrl(),
                                    files.getItems().get(i).getThumbnailLink(),
                                    files.getItems().get(i).getOriginalFilename(),
                                    App.getStringById(R.string.title_drive)
                            );

                            result.add(gif);
                        }
                    }
                }

                //this will be used in pagination. here is only used to keep running through all the files searching
                request.setPageToken(files.getNextPageToken());

            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        return result;
    }

    private void hideLoadingIndicator(){
        loadingIndicator.setVisibility(View.GONE);
    }

    private void showLoadingIndicator(){
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    private void hideEmptyGifsCard(){
        emptyGifsCard.setVisibility(View.GONE);
    }

    private void showEmptyGifsCard(){
        emptyGifsCard.setVisibility(View.VISIBLE);
    }
}
