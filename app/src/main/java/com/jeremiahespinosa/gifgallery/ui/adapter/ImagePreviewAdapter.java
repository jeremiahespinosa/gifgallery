package com.jeremiahespinosa.gifgallery.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.ui.activities.ViewGifActivity;
import com.jeremiahespinosa.gifgallery.utility.models.Gifs;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jespinosa on 6/22/15.
 */
public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder>  {

    private ArrayList<Gifs> mListOfGifs = new ArrayList<>();

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_grid_photo, viewGroup, false);

        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder viewHolder, int position) {
        Gifs gif = mListOfGifs.get(position);

        Glide.with(viewHolder.mImageView.getContext())
                .load(gif.getUrlToLoad())
                .asBitmap()
                .centerCrop()
                .into(viewHolder.mImageView);

        if(gif.getImageName() != null && !gif.getImageName().isEmpty()) {
            viewHolder.mImageTitle.setVisibility(View.VISIBLE);
            viewHolder.mImageTitle.setText(gif.getImageName());
        }

        viewHolder.bindGif(gif);
    }

    @Override
    public int getItemCount() {
        return mListOfGifs.size();
    }

    public void animateTo(List<Gifs> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<Gifs> newModels) {
        for (int i = mListOfGifs.size() - 1; i >= 0; i--) {
            final Gifs model = mListOfGifs.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Gifs> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final Gifs model = newModels.get(i);
            if (!mListOfGifs.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Gifs> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final Gifs model = newModels.get(toPosition);
            final int fromPosition = mListOfGifs.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public void addAnotherItem(Gifs model){
        mListOfGifs.add(model);
        notifyDataSetChanged();
    }

    public Gifs removeItem(int position) {
        final Gifs model = mListOfGifs.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, Gifs model) {
        mListOfGifs.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Gifs model = mListOfGifs.remove(fromPosition);
        mListOfGifs.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    

    public static class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mImageView;
        public final TextView mImageTitle;
        private Gifs selectedGif;

        public ImageViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            mImageView = (ImageView) itemView.findViewById(R.id.imagePreview);
            mImageTitle = (TextView) itemView.findViewById(R.id.imageTitle);
        }

        public void bindGif(Gifs gif){
            selectedGif = gif;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mImageView.getContext(), ViewGifActivity.class);
            intent.putExtra(ViewGifActivity.GIF_URL_KEY, selectedGif.getUrlToLoad());
            intent.putExtra(ViewGifActivity.GIF_TITLE_KEY, selectedGif.getImageName());
            intent.putExtra(ViewGifActivity.GIF_BASE_PATH, selectedGif.getBasePath());
            mImageView.getContext().startActivity(intent);
        }
    }
}
