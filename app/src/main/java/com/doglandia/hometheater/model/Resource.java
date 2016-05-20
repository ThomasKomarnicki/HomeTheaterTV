package com.doglandia.hometheater.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;

import com.bumptech.glide.Glide;
import com.doglandia.hometheater.MediaLoaderApplication;

/**
 * Created by tdk10 on 2/21/2016.
 */
public class Resource implements Parcelable{

    private String name;
    private String location;
    private String thumbnailPath;

    // tightly coupled to the the video lib view
    private transient Presenter.ViewHolder viewHolder;

    protected Resource(Parcel in) {
        name = in.readString();
        location = in.readString();
        thumbnailPath = in.readString();
    }

    public static final Creator<Resource> CREATOR = new Creator<Resource>() {
        @Override
        public Resource createFromParcel(Parcel in) {
            return new Resource(in);
        }

        @Override
        public Resource[] newArray(int size) {
            return new Resource[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;

        if(viewHolder != null){
            MediaLoaderApplication application = (MediaLoaderApplication) viewHolder.view.getContext().getApplicationContext();
            String thumbnailUrl = application.getResourceServer().getThumbnailUrl(this);
            setViewHolderImage(thumbnailUrl);
        }
    }

    public void setViewHolder(Presenter.ViewHolder viewHolder) {
        this.viewHolder = viewHolder;
    }

    private void setViewHolderImage(String url){
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        if(getThumbnailPath() != null) {
            Glide.with(viewHolder.view.getContext())
                    .load(url)
                    .centerCrop()
                    .into(cardView.getMainImageView());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(location);
        dest.writeString(thumbnailPath);
    }

    public boolean isNativeFormat() {
        return name.endsWith(".mp4") || name.endsWith(".3gp") || name.endsWith(".webm");
    }
}
