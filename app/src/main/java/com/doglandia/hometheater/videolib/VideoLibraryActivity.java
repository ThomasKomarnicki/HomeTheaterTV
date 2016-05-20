package com.doglandia.hometheater.videolib;

import android.app.Activity;
import android.os.Bundle;

import com.doglandia.hometheater.MediaLoaderApplication;
import com.doglandia.hometheater.R;
import com.doglandia.hometheater.event.ResourceServerConnectFailed;
import com.doglandia.hometheater.event.ResourceServerConnected;
import com.doglandia.hometheater.model.ResourceGroup;
import com.doglandia.hometheater.model.ResourcesResponse;
import com.doglandia.hometheater.resourceserver.ResourceServer;
import com.squareup.otto.Subscribe;

import java.util.List;

import rx.functions.Action1;

/**
 * Created by tdk10 on 2/21/2016.
 */
public class VideoLibraryActivity extends Activity {

    private VideoLibraryFragment videoLibraryFragment;

    private ReconnectingFragment reconnectingFragment;
    private RefreshingFragment refreshingFragment;

    private List<ResourceGroup> resourceGroups;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_library);

        reconnectingFragment = new ReconnectingFragment();
        videoLibraryFragment = new VideoLibraryFragment();
        refreshingFragment = new RefreshingFragment();

//        getFragmentManager().beginTransaction().add(R.id.video_lib_content,reconnectingFragment).commit();


        MediaLoaderApplication.getBus().register(this);

        ResourceServer server = ((MediaLoaderApplication) getApplication()).getResourceServer();
        if(!server.isConnected()) {
            onResourceServerReconnectFailed(new ResourceServerConnectFailed());
        }else{
            getResourceData();
        }
    }

    public void getResourceData() {
        ResourceServer server = ((MediaLoaderApplication) getApplication()).getResourceServer();
        server.getResourceGroups().subscribe(new Action1<ResourcesResponse>() {
            @Override
            public void call(ResourcesResponse resourcesResponse) {
                showResourceGroups(resourcesResponse);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    public void refreshResourceData(){
        getFragmentManager().beginTransaction().add(android.R.id.content, refreshingFragment).commitAllowingStateLoss();

    }

    private void showResourceGroups(ResourcesResponse resourcesResponse){
        resourceGroups = resourcesResponse.getResourceGroups();
        if(resourceGroups.size() == 0){
            getFragmentManager().beginTransaction().replace(R.id.video_lib_content, new NoContentFragment()).commitAllowingStateLoss();
        }else {
            getFragmentManager().beginTransaction().replace(R.id.video_lib_content, videoLibraryFragment).commitAllowingStateLoss();
            videoLibraryFragment.initViews(resourceGroups);
        }
    }

    public void onRefreshed(ResourcesResponse resourcesResponse){
        getFragmentManager().beginTransaction().remove(refreshingFragment).commitAllowingStateLoss();
        resourceGroups = resourcesResponse.getResourceGroups();
        videoLibraryFragment.initViews(resourceGroups);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        MediaLoaderApplication.getBus().unregister(this);
    }

    @Subscribe
    public void onResourceServerConnected(ResourceServerConnected resourceServerConnected){
        getResourceData();
    }

    @Subscribe
    public void onResourceServerReconnectFailed(ResourceServerConnectFailed event){
        getFragmentManager().beginTransaction().replace(R.id.video_lib_content, reconnectingFragment).commitAllowingStateLoss();

    }
}
