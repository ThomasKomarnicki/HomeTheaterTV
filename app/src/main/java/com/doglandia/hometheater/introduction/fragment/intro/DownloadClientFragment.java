package com.doglandia.hometheater.introduction.fragment.intro;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doglandia.hometheater.R;

/**
 * Created by tdk10 on 3/10/2016.
 */
public class DownloadClientFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download_client, container, false);
    }
}
