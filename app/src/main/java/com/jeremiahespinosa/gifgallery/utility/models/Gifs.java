package com.jeremiahespinosa.gifgallery.utility.models;

/**
 * Created by jespinosa on 6/22/15.
 */
public class Gifs {
    //used for dropbox path, since the url must be constructed for each call
    //we will save the path to the image
    private String basePath;
    private String urlToLoad;
    private String imageName;

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
}
