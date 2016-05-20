package com.doglandia.hometheater.videolib;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doglandia.hometheater.MediaLoaderApplication;
import com.doglandia.hometheater.R;
import com.doglandia.hometheater.model.ResourcesResponse;
import com.doglandia.hometheater.resourceserver.ResourceServer;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdk10 on 4/16/2016.
 */
public class RefreshingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_refreshing, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        ResourceServer server = ((MediaLoaderApplication) getActivity().getApplication()).getResourceServer();

        server.getServerInstance().getResourceGroups()
        .delay(1500, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<ResourcesResponse>() {
                       @Override
                       public void call(ResourcesResponse resourcesResponse) {
                           ((VideoLibraryActivity) getActivity()).onRefreshed(resourcesResponse);
                       }
                   },
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });

    }
}
