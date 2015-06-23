package com.jeremiahespinosa.gifgallery.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jespinosa on 6/22/15.
 */
public class Gifs implements Parcelable{

    //fullImageToLoadPath (dropbox): used for dropbox path, since the url must be constructed for each call
    //we will save the path to the image

    //fullImageToLoadPath (gDrive): used as the link that will be called to download the selected image
    private String fullImageToLoadPath;

    //thumbnail path or url
    private String thumbnailUrlToLoad;

    //any info about the file
    private String imageName;

    //the source we are loading from-> Local, Dropbox, or Google Drive
    private String gifSource;

    /**
     *
     * @param fullImageToLoadPath
     * @param thumbnailUrlToLoad
     * @param imageName
     * @param gifSource
     */
    public Gifs(String fullImageToLoadPath, String thumbnailUrlToLoad, String imageName, String gifSource) {
        this.fullImageToLoadPath = fullImageToLoadPath;
        this.thumbnailUrlToLoad = thumbnailUrlToLoad;
        this.imageName = imageName;
        this.gifSource = gifSource;
    }

    public Gifs(Parcel in) {
        fullImageToLoadPath = in.readString();
        thumbnailUrlToLoad = in.readString();
        imageName = in.readString();
        gifSource = in.readString();
    }

    public String getFullImageToLoadPath() {
        return fullImageToLoadPath;
    }

    public void setFullImageToLoadPath(String fullImageToLoadPath) {
        this.fullImageToLoadPath = fullImageToLoadPath;
    }

    public String getThumbnailUrlToLoad() {
        return thumbnailUrlToLoad;
    }

    public void setThumbnailUrlToLoad(String thumbnailUrlToLoad) {
        this.thumbnailUrlToLoad = thumbnailUrlToLoad;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getGifSource() {
        return gifSource;
    }

    public void setGifSource(String gifSource) {
        this.gifSource = gifSource;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fullImageToLoadPath);
        dest.writeString(this.thumbnailUrlToLoad);
        dest.writeString(this.imageName);
        dest.writeString(this.gifSource);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Gifs createFromParcel(Parcel source) {
            return new Gifs(source);
        }

        @Override
        public Gifs[] newArray(int size) {
            return new Gifs[size];
        }
    };
}
