package com.doglandia.hometheater.videolib;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.doglandia.hometheater.R;

/**
 * Created by tdk10 on 4/21/2016.
 */
public class NoContentFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_no_content, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button refreshButton = (Button) view.findViewById(R.id.no_content_refresh);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiateRefresh();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initiateRefresh(){
        VideoLibraryActivity videoLibraryActivity = (VideoLibraryActivity) getActivity();
        videoLibraryActivity.refreshResourceData();
    }
}
