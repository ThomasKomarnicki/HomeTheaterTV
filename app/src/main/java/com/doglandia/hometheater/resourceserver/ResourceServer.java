package com.doglandia.hometheater.resourceserver;

import android.content.Context;

import com.doglandia.hometheater.MediaLoaderApplication;
import com.doglandia.hometheater.clientdiscovery.ClientDiscoverer;
import com.doglandia.hometheater.event.ResourceServerConnected;
import com.doglandia.hometheater.model.Resource;
import com.doglandia.hometheater.model.ResourcesResponse;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by tdk10 on 2/21/2016.
 */
public class ResourceServer implements ServerInterface, ClientDiscoverer.OnHostFoundListener{

    public static final String PORT = "8988";

    private ServerInterface instance;

    private ClientDiscoverer clientDiscoverer;

    private String discoveredHost;

    private Context context;

    public ResourceServer(Context context){
        this.context = context;
//        clientDiscoverer = new ClientDiscoverer(context, this);
    }

    public void startClientDiscovery(){
        clientDiscoverer = new ClientDiscoverer(context,this);
    }

    private void createRestAdapter(){

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.addInterceptor(new HttpLoggingInterceptor());


        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(getResourceServerEndpoint());
        builder.addConverterFactory(GsonConverterFactory.create());
        builder.addCallAdapterFactory(RxJavaCallAdapterFactory.create());
        builder.client(clientBuilder.build());

        instance = builder.build().create(ServerInterface.class); // possible to switch to testable instance
    }

    public ServerInterface getServerInstance(){
        return this;
    }

    private String getResourceServerEndpoint(){
        return "http://"+discoveredHost+":"+PORT;
    }

    public boolean isConnected(){
        return discoveredHost != null;
    }


//    @Override
//    public void getResourceGroups(Callback<ResourcesResponse> callback) {
//        instance.getResourceGroups(callback);
//    }
//
//    @Override
//    public ResourcesResponse getResourceGroups() {
//        return instance.getResourceGroups();
//    }

    public String getMediaUrl(Resource resource) {
        // todo
        return (getResourceServerEndpoint() + "/media/" + resource.getLocation()).replace(" ","%20");
    }

    public String getThumbnailUrl(Resource resource){
        return getResourceServerEndpoint()+"/"+resource.getThumbnailPath();
    }

    @Override
    public void onHostFound(String host) {
        discoveredHost = host;
        createRestAdapter();

        MediaLoaderApplication.getBus().post(new ResourceServerConnected());
    }

    @Override
    public void onNoHostFound() {
        // host not found message
    }

    @Override
    public void onProgressUpdate(int progress) {

    }

    public void cancelConnectingTasks() {
        if(clientDiscoverer != null){
            clientDiscoverer.cancelTasks();
        }
    }

    @Override
    public Observable<ResourcesResponse> getResourceGroups() {
        return instance.getResourceGroups().flatMap(new Func1<ResourcesResponse, Observable<ResourcesResponse>>() {
            @Override
            public Observable<ResourcesResponse> call(ResourcesResponse resourcesResponse) {
                resourcesResponse.expandResourceGroups();
                return Observable.just(resourcesResponse);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
