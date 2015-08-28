package com.jeremiahespinosa.gifgallery.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.models.Gif;
import com.jeremiahespinosa.gifgallery.presenter.listgifs.ImagesFragmentPresenter;
import com.jeremiahespinosa.gifgallery.presenter.listgifs.ImagesView;
import com.jeremiahespinosa.gifgallery.ui.activities.MainActivity;
import com.jeremiahespinosa.gifgallery.ui.activities.ViewGifActivity;
import com.jeremiahespinosa.gifgallery.ui.adapter.ImagePreviewAdapter;
import com.jeremiahespinosa.gifgallery.utility.App;
import com.jeremiahespinosa.gifgallery.ui.widgets.GridSpacingDecoration;
import com.jeremiahespinosa.gifgallery.utility.PrefUtils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by jespinosa on 6/22/15.
 */
public class ImagesFragment extends Fragment implements ImagesView {

    private static String TAG = "ImagesFragment";
    private String typeOfFragment = "";
    private CardView emptyGifsCard;
    private ProgressBar loadingIndicator;
    private RecyclerView recyclerView;
    private ImagesFragmentPresenter fragmentPresenter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public ImagesFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gif_previews, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.primary_dark, R.color.accent);

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        recyclerView.addItemDecoration(new GridSpacingDecoration());

        loadingIndicator = (ProgressBar) view.findViewById(R.id.imagesProgressBar);

        emptyGifsCard = (CardView) view.findViewById(R.id.emptyGifsCard);

        fragmentPresenter = new ImagesFragmentPresenter(getActivity().getContentResolver(), this);

        Bundle args = getArguments();

        if(args != null) {
            typeOfFragment = args.getString(MainActivity.BUNDLE_KEY, "");
        }

        refreshContent();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });


        return view;
    }

    private void refreshContent(){
        //setting the presenter to load the correct thumbnails based on fragment type
        if(typeOfFragment.equals(App.getStringById(R.string.title_drive))){
            Log.d(TAG, "swipe refresh - drive");
            GoogleAccountCredential credential;
            credential = GoogleAccountCredential.usingOAuth2(getActivity(), Collections.singleton(DriveScopes.DRIVE));
            credential.setSelectedAccountName(PrefUtils.getPrefDriveUser());

            fragmentPresenter.loadGifsFromGoogleDrive(credential);
        }
        else if(typeOfFragment.equals(App.getStringById(R.string.title_dropbox))){
            Log.d(TAG, "swipe refresh - dropbox");

            fragmentPresenter.loadGifsFromDropbox();
        }
        else{
            Log.d(TAG, "swipe refresh - storage");

            fragmentPresenter.loadGifsFromStorage();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.v(TAG, "Resuming fragment");

        //TODO: check if we can update tabs from here
    }

    @Override
    public void setListOfGifs(ArrayList<Gif> listOfGifs) {
        mSwipeRefreshLayout.setRefreshing(false);

        if(listOfGifs.size() < 1){
            App.showShortToast("No gifs found");
            showEmptyGifsCard();
        }
        else{
            recyclerView.setAdapter(new ImagePreviewAdapter(listOfGifs, this));
            hideEmptyGifsCard();
        }
    }

    @Override
    public void showLoadingIndicator() {
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingIndicator() {
        loadingIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onGifSelected(Gif gif, View v) {

        Intent intent = new Intent(getActivity(), ViewGifActivity.class);
        intent.putExtra(ViewGifActivity.GIF_PARCEL_OBJECT, gif);

        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(getActivity(), (View) v, "viewSomeGif");

        getActivity().startActivity(intent, options.toBundle());
    }

    private void hideEmptyGifsCard(){
        emptyGifsCard.setVisibility(View.GONE);
    }

    private void showEmptyGifsCard(){
        emptyGifsCard.setVisibility(View.VISIBLE);
    }
}
