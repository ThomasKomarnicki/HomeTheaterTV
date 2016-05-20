package com.doglandia.hometheater.videolib;

import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.doglandia.hometheater.MediaLoaderApplication;
import com.doglandia.hometheater.model.Resource;

/**
 * Created by tdk10 on 2/21/2016.
 */
public class VideoPresenter extends Presenter {

    private static final int CARD_WIDTH = (int) (313 * 1.5);
    private static final int CARD_HEIGHT = (int) (176 * 1.5);

    private static final String TAG = "VideoPresenter";

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {

//        sDefaultBackgroundColor = parent.getResources().getColor(R.color.default_background);
//        sSelectedBackgroundColor = parent.getResources().getColor(R.color.selected_background);
//        mDefaultCardImage = parent.getResources().getDrawable(R.drawable.movie);

        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
//                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
//        updateCardBackgroundColor(cardView, false);
        return new ResourceViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
//        Movie movie = (Movie) item;
        Resource resource = (Resource) item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        ResourceViewHolder resourceViewHolder = (ResourceViewHolder) viewHolder;
        resourceViewHolder.setResource(resource);

        cardView.setTitleText(resource.getName());
        cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
        if (resource.getThumbnailPath() != null) {
            MediaLoaderApplication application = (MediaLoaderApplication) viewHolder.view.getContext().getApplicationContext();
            String thumbnailUrl = application.getResourceServer().getThumbnailUrl(resource);
            Glide.with(viewHolder.view.getContext())
                    .load(thumbnailUrl)
                    .centerCrop()
                    .into(cardView.getMainImageView());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);

        ResourceViewHolder resourceViewHolder = (ResourceViewHolder) viewHolder;
        resourceViewHolder.unbind();
    }

    public static class ResourceViewHolder extends Presenter.ViewHolder{

        private Resource resource;

        public ResourceViewHolder(View view) {
            super(view);
        }

        public void setResource(Resource resource) {
            resource.setViewHolder(this);
            this.resource = resource;

        }

        public Resource getResouce() {
            return resource;
        }

        public void unbind() {
            resource.setViewHolder(null);
        }
    }
}
