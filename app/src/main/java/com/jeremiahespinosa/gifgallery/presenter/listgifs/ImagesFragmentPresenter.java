package com.jeremiahespinosa.gifgallery.presenter.listgifs;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.ui.fragments.SettingsPreferenceFragment;
import com.jeremiahespinosa.gifgallery.utility.App;
import com.jeremiahespinosa.gifgallery.utility.PrefUtils;
import com.jeremiahespinosa.gifgallery.models.Gif;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class will handle all the heavy lifting for the ImagesFragment.
 * The user can load gifs from storage, load gifs from dropbox, and
 * load gifs from google drive
 *
 * Created by jespinosa on 6/23/15.
 */
public class ImagesFragmentPresenter {

    private static String TAG = "ImagesFragmentPresenter";

    private ImagesView imagesView;
    private ContentResolver contentResolver;

    public ImagesFragmentPresenter(ContentResolver contentResolver, ImagesView imagesView) {
        this.contentResolver = contentResolver;
        this.imagesView = imagesView;
    }

    public void loadGifsFromStorage(){

        imagesView.showLoadingIndicator();

        Cursor mCursor = contentResolver.query(MediaStore.Files.getContentUri("external"),
                    null, null, null, null);

        ArrayList<Gif> gifArrayList = new ArrayList<>();

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

                    //creating gif this way for readability
                    Gif gif = new Gif(filePath, filePath, null, App.getStringById(R.string.title_local));

                    gifArrayList.add(gif);
                }
            }
        }

        mCursor.close();

        imagesView.setListOfGifs(gifArrayList);

        imagesView.hideLoadingIndicator();
    }

    public void loadGifsFromDropbox(){

        if(App.hasInternetConnection()){
            new LoadDropboxGifsTask().execute();
        }
        else{
            App.showShortToast(App.getStringById(R.string.no_network));
        }
    }

    public void loadGifsFromGoogleDrive(GoogleAccountCredential credential){

        Drive service = new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential).setApplicationName(App.getStringById(R.string.app_name)).build();

        new LoadFilesFromGoogleDriveTask(service).execute();
    }

    private class LoadDropboxGifsTask extends AsyncTask<Void, Long, ArrayList<Gif>> {

        public LoadDropboxGifsTask() {
            imagesView.showLoadingIndicator();
        }

        @Override
        protected ArrayList<Gif> doInBackground(Void... params) {
            ArrayList<Gif> listOfGifsAvailable = new ArrayList<>();

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

                            //creating gif this way for readability
                            Gif gif = new Gif(
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
        protected void onPostExecute( ArrayList<Gif> gifs) {

            imagesView.setListOfGifs(gifs);

            imagesView.hideLoadingIndicator();
        }

    }

    //Loading the thumbnails from drive using the drive sdk
    private class LoadFilesFromGoogleDriveTask extends AsyncTask<Void, Void, ArrayList<Gif>>{

        private Drive mService;

        public LoadFilesFromGoogleDriveTask(Drive service) {
            imagesView.showLoadingIndicator();

            mService = service;
        }

        @Override
        protected ArrayList<Gif> doInBackground(Void... params) {
            ArrayList<Gif> result = null;

            try{
                result = retrieveAllFiles(mService);
            }
            catch(IOException e){
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Gif> gifs) {

            imagesView.setListOfGifs(gifs);

            imagesView.hideLoadingIndicator();
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
    private static ArrayList<Gif> retrieveAllFiles(Drive service) throws IOException {
        ArrayList<Gif> result = new ArrayList<>();
        Drive.Files.List request = service.files().list();

        do {
            try {
                //requesting list of files in google drive
                FileList files = request.execute();

                for(int i = 0; i < files.getItems().size(); i++){
                    if( (files.getItems().get(i).getMimeType() != null) && files.getItems().get(i).getMimeType().contains("gif") ){
                        if(files.getItems().get(i).getThumbnailLink() != null && !files.getItems().get(i).getThumbnailLink().isEmpty()){
                            //creating gif this way for readability
                            Gif gif = new Gif(
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
}
