package com.jeremiahespinosa.gifgallery.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.presenter.ImagesFragmentPresenter;
import com.jeremiahespinosa.gifgallery.ui.activities.MainActivity;
import com.jeremiahespinosa.gifgallery.ui.adapter.ImagePreviewAdapter;
import com.jeremiahespinosa.gifgallery.utility.App;
import com.jeremiahespinosa.gifgallery.ui.widgets.GridSpacingDecoration;
import com.jeremiahespinosa.gifgallery.models.Gifs;

import java.util.ArrayList;

/**
 * Created by jespinosa on 6/22/15.
 */
public class ImagesFragment extends Fragment {

    private static String TAG = "ImagesFragment";
    private String typeOfFragment = "";

    public ImagesFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gif_previews, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        ProgressBar loadingIndicator = (ProgressBar) view.findViewById(R.id.imagesProgressBar);

        ImagePreviewAdapter imagePreviewAdapter = new ImagePreviewAdapter();

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.setAdapter(imagePreviewAdapter);
        recyclerView.addItemDecoration(new GridSpacingDecoration());

        CardView emptyGifsCard = (CardView) view.findViewById(R.id.emptyGifsCard);

        ImagesFragmentPresenter fragmentPresenter = new ImagesFragmentPresenter(getActivity(), loadingIndicator, imagePreviewAdapter, emptyGifsCard);

        Bundle args = getArguments();

        if(args != null) {
            typeOfFragment = args.getString(MainActivity.BUNDLE_KEY, "");
        }

        //setting the presenter to load the correct thumbnails based on fragment type
        if(typeOfFragment.equals(App.getStringById(R.string.title_drive))){
            fragmentPresenter.loadGifsFromGoogleDrive();
        }
        else if(typeOfFragment.equals(App.getStringById(R.string.title_dropbox))){
            fragmentPresenter.loadGifsFromDropbox();
        }
        else{
            fragmentPresenter.loadGifsFromStorage();
        }

        return view;
    }
}
