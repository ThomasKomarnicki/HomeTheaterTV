package com.doglandia.hometheater.videolib;

import android.view.View;

/**
 * Created by tdk10 on 3/29/2016.
 */
public class ActionIcon {

    private int resId;
    private String title;

    private View.OnClickListener onClickListener;

    public ActionIcon(int resId, String title, View.OnClickListener onClickListener) {
        this.resId = resId;
        this.onClickListener = onClickListener;
        this.title = title;
    }

    public int getResId() {
        return resId;
    }

    public View.OnClickListener getOnClickListener() {
        return onClickListener;
    }

    public String getTitle() {
        return title;
    }
}
