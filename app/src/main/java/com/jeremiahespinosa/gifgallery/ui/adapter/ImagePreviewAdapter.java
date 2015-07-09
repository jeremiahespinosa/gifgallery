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

/**
 * Created by jespinosa on 6/22/15.
 */
public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder>  {

    private ArrayList<Gif> mListOfGifs = new ArrayList<>();

    public ImagePreviewAdapter(ArrayList<Gif> mListOfGifs) {
        this.mListOfGifs = mListOfGifs;
    }

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
