package com.doglandia.hometheater.introduction.fragment.intro;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doglandia.hometheater.R;

/**
 * Created by tdk10 on 3/14/2016.
 */
public class SelectVideoLocationsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.select_video_locations_fragment, container, false);
    }


}
