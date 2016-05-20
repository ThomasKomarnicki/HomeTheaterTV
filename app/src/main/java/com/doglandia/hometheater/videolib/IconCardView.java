package com.doglandia.hometheater.videolib;

import android.content.Context;
import android.support.v17.leanback.widget.BaseCardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.doglandia.hometheater.R;

/**
 * Created by tdk10 on 3/30/2016.
 */
public class IconCardView extends BaseCardView {

    private ImageView icon;
    private TextView title;

    public IconCardView(Context context) {
        super(context);
        init();
    }

    public IconCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IconCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.icon_card_view, this);
        icon = (ImageView) findViewById(R.id.icon);
        title = (TextView) findViewById(R.id.title);
    }

    public ImageView getIcon() {
        return icon;
    }

    public TextView getTitle() {
        return title;
    }
}
