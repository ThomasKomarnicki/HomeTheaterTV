package com.doglandia.hometheater;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.doglandia.hometheater.resourceserver.ResourceServer;
import com.squareup.otto.Bus;
import io.fabric.sdk.android.Fabric;

/**
 * Created by tdk10 on 2/21/2016.
 */
public class MediaLoaderApplication extends Application {

    private static final Bus bus = new Bus();

    public static Bus getBus() {
        return bus;
    }

    private ResourceServer resourceServer;

//    private ThumbnailManager thumbnailManager;


    public ResourceServer getResourceServer() {
//        if(resourceServer == null){
//            resourceServer = new ResourceServer();
//        }
        return resourceServer;
    }

//    public ThumbnailManager getThumbnailManager() {
//        return thumbnailManager;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(!BuildConfig.BUILD_TYPE.equals("debug")) {
            Fabric.with(this, new Crashlytics());
        }

        resourceServer = new ResourceServer(getApplicationContext());
//        thumbnailManager = new ThumbnailManager(resourceServer, getFilesDir());
    }
}
