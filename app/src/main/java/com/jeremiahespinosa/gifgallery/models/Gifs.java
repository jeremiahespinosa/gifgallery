package com.jeremiahespinosa.gifgallery.models;

/**
 * Created by jespinosa on 6/22/15.
 */
public class Gifs {
    //basePath (dropbox):used for dropbox path, since the url must be constructed for each call
    //we will save the path to the image

    //basePath (gDrive):used as the link that will be called to download the selected image
    private String basePath;

    //thumbnail path/url
    private String urlToLoad;

    //any info about the file
    private String imageName;

    private String gifSource;

    public Gifs() {

    }

    public Gifs(String urlToLoad, String imageName) {
        this.urlToLoad = urlToLoad;
        this.imageName = imageName;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getUrlToLoad() {
        return urlToLoad;
    }

    public void setUrlToLoad(String urlToLoad) {
        this.urlToLoad = urlToLoad;
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

}
