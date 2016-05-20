package com.doglandia.hometheater.resourceserver;

import com.doglandia.hometheater.model.ResourcesResponse;

import retrofit2.http.GET;
import rx.Observable;


/**
 * Created by tdk10 on 2/21/2016.
 */
public interface ServerInterface {

    @GET("/data")
    Observable<ResourcesResponse> getResourceGroups();


}
