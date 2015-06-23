package com.jeremiahespinosa.gifgallery.ui.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jeremiahespinosa.gifgallery.R;
import com.jeremiahespinosa.gifgallery.ui.activities.ViewGifActivity;
import com.jeremiahespinosa.gifgallery.models.Gif;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jespinosa on 6/22/15.
 */
public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder>  {

    private ArrayList<Gif> mListOfGifs = new ArrayList<>();

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_grid_photo, viewGroup, false);

        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder viewHolder, int position) {
        Gif gif = mListOfGifs.get(position);

        Glide.with(viewHolder.mImageView.getContext())
                .load(gif.getThumbnailUrlToLoad())
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

    /**
     * Used to help show when items in the list
     * are changed either by adding or deleting
     *
     * @param models
     */
    public void animateTo(List<Gif> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<Gif> newModels) {
        for (int i = mListOfGifs.size() - 1; i >= 0; i--) {
            final Gif model = mListOfGifs.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<Gif> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final Gif model = newModels.get(i);
            if (!mListOfGifs.contains(model)) {
                addItemAtPosition(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<Gif> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final Gif model = newModels.get(toPosition);
            final int fromPosition = mListOfGifs.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public void addAnotherItem(Gif model){
        mListOfGifs.add(model);
        notifyDataSetChanged();
    }

    public Gif removeItem(int position) {
        final Gif model = mListOfGifs.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItemAtPosition(int position, Gif model) {
        mListOfGifs.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Gif model = mListOfGifs.remove(fromPosition);
        mListOfGifs.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mImageView;
        public final TextView mImageTitle;
        private Gif selectedGif;

        public ImageViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            mImageView = (ImageView) itemView.findViewById(R.id.imagePreview);
            mImageTitle = (TextView) itemView.findViewById(R.id.imageTitle);
        }

        public void bindGif(Gif gif){
            selectedGif = gif;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mImageView.getContext(), ViewGifActivity.class);
            intent.putExtra(ViewGifActivity.GIF_PARCEL_OBJECT, selectedGif);

            mImageView.getContext().startActivity(intent);
        }
    }
}
