package com.jeremiahespinosa.gifgallery.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.query.Query;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.DriveScopes;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.models.Gif;
import com.jeremiahespinosa.gifgallery.presenter.listgifs.ImagesFragmentPresenter;
import com.jeremiahespinosa.gifgallery.presenter.listgifs.ImagesView;
import com.jeremiahespinosa.gifgallery.ui.activities.BaseActivity;
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

    private String mNextPageToken;
    private boolean mHasMore = true;


    public ImagesFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gif_previews, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        recyclerView.addItemDecoration(new GridSpacingDecoration());

        loadingIndicator = (ProgressBar) view.findViewById(R.id.imagesProgressBar);

        emptyGifsCard = (CardView) view.findViewById(R.id.emptyGifsCard);

        ImagesFragmentPresenter fragmentPresenter = new ImagesFragmentPresenter(getActivity().getContentResolver(), this);

        Bundle args = getArguments();

        if(args != null) {
            typeOfFragment = args.getString(MainActivity.BUNDLE_KEY, "");
        }

        //setting the presenter to load the correct thumbnails based on fragment type
        if(typeOfFragment.equals(App.getStringById(R.string.title_drive))){

            GoogleAccountCredential credential;
            credential = GoogleAccountCredential.usingOAuth2(getActivity(), Collections.singleton(DriveScopes.DRIVE));
            credential.setSelectedAccountName(PrefUtils.getPrefDriveUser());

//            fragmentPresenter.loadGifsFromGoogleDrive(credential);
            retrieveNextPage();
        }
        else if(typeOfFragment.equals(App.getStringById(R.string.title_dropbox))){
            fragmentPresenter.loadGifsFromDropbox();
        }
        else{
            fragmentPresenter.loadGifsFromStorage();
        }

        return view;
    }

    /**
     * Retrieves results for the next page. For the first run,
     * it retrieves results for the first page.
     */
    private void retrieveNextPage() {
        // if there are no more results to retrieve,
        // return silently.
        if (!mHasMore) {
            return;
        }
        // retrieve the results for the next page.
        Query query = new Query.Builder()
                .setPageToken(mNextPageToken)
                .build();
        Drive.DriveApi.query(((BaseActivity)getActivity()).getGoogleApiClient(), query)
                .setResultCallback(metadataBufferCallback);
    }

    /**
     * Appends the retrieved results to the result buffer.
     */
    private final ResultCallback<DriveApi.MetadataBufferResult> metadataBufferCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {

            @Override
            public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                if (!metadataBufferResult.getStatus().isSuccess()) {
                    App.showShortToast("Problem while retrieving files");
                    return;
                }
                Log.d(TAG, "metadata->"+metadataBufferResult.toString() );
//                mResultsAdapter.append(result.getMetadataBuffer());
//                mNextPageToken = result.getMetadataBuffer().getNextPageToken();
//                mHasMore = mNextPageToken != null;
            }
    };

                @Override
    public void setListOfGifs(ArrayList<Gif> listOfGifs) {

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
