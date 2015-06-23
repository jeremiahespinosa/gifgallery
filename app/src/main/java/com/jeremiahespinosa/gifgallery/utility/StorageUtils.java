package com.jeremiahespinosa.gifgallery.utility;

import android.os.Environment;
import android.util.Log;

import com.dropbox.client2.exception.DropboxException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.services.drive.Drive;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by jespinosa on 6/23/2015.
 */
public class StorageUtils {
    private static String TAG = "StorageUtils";

    public static void copyInputStreamToFile( InputStream in, java.io.File file ) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getStorageDirectory(String folderName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), folderName);
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file;
    }

    /**
     * Download a file's content.
     *
     * @param service Drive API service instance.
     * @param basePath the url to download
     * @return InputStream containing the file's content if successful,
     *         {@code null} otherwise.
     */
    public static InputStream downloadFile(Drive service, String basePath) {

        if (basePath != null && !basePath.isEmpty()) {
            try {
                HttpResponse resp =
                        service.getRequestFactory().buildGetRequest(new GenericUrl(basePath ))
                                .execute();
                return resp.getContent();
            } catch (IOException e) {
                // An error occurred.
                e.printStackTrace();
                return null;
            }
        } else {
            // The file doesn't have any content stored on Drive.
            return null;
        }
    }


    /**
     * Downloads a file from dropbox
     * @return
     * @throws DropboxException
     * @throws IOException
     */
    public static InputStream downloadFile(String dropboxGifPath) throws DropboxException, IOException {

        String dropboxUrl = "https://api-content.dropbox.com/1/files/auto/";

        String slashlessString = dropboxGifPath;
        slashlessString = slashlessString.substring(1);

        String urlToDownload = dropboxUrl +slashlessString+"?"+"access_token="+ PrefUtils.getPrefDropboxAccessToken();

        URL url = new URL(urlToDownload);
        URLConnection urlConnection = url.openConnection();

        return new BufferedInputStream(urlConnection.getInputStream());
    }
}
