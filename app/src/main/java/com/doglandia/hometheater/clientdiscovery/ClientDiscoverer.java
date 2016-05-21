package com.doglandia.hometheater.clientdiscovery;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;

import com.doglandia.hometheater.HomeTheaterApplication;
import com.doglandia.hometheater.event.ResourceServerConnectFailed;
import com.doglandia.hometheater.resourceserver.ResourceServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tdk10 on 2/22/2016.
 */
public class ClientDiscoverer {

    private static final String TAG = "ClientDiscoverer";

    private static final String LAST_DISCOVERED_HOST = "last_discovered_host";

    public interface OnHostFoundListener{
        void onHostFound(String host);
        void onNoHostFound();
        void onProgressUpdate(int progress);
    }

    private OnHostFoundListener listener;

    private Context context;

    private OkHttpClient client;

    private boolean cancel = false;

    private boolean found = false;

    public ClientDiscoverer(final Context context, OnHostFoundListener onHostFoundListener){
        this.context = context;
        this.listener = onHostFoundListener;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(200, TimeUnit.MILLISECONDS);
        client = builder.build();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.contains(LAST_DISCOVERED_HOST)){
            hostAcceptsObservable(prefs.getString(LAST_DISCOVERED_HOST,""))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String host) {
                            if (host != null) {
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                                prefs.edit().putString(LAST_DISCOVERED_HOST, host).commit();
                                listener.onHostFound(host);
                            }else{
                                Log.d(TAG, "scanning subnet because host is null");
                                startSubNetScan();
                            }
                        }

                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            throwable.printStackTrace();
                            Log.d(TAG, "scanning subnet because last discovered host failed");
                            startSubNetScan();
                            // notify that we failed to reconnect on last discovered host, try to rediscover host
//                HomeTheaterApplication.getBus().post(new ResourceServerConnectFailed());
                        }
                    });
        }else {
            startSubNetScan();
        }
    }

    public void cancelTasks(){
        cancel = true;
    }

    boolean hostAcceptsRequest(String hostName) throws IOException {

        Request request = new Request.Builder()
                .url("http://"+hostName+":"+ResourceServer.PORT+"/ping")
                .build();
//        Log.d(TAG, "calling "+request.url().toString());
        Response response = client.newCall(request).execute();
        String body = response.body().string();
//        Log.d(TAG, "body of "+hostName + " = "+body);
        return body.contains("\"status\":200");
    }

    private String getSubNet(Context context){
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        Log.d(TAG, "ip = "+ip);

        String[] splits = ip.split("\\.");
        StringBuilder subnet = new StringBuilder();
        for(int i = 0; i < splits.length-1; i++){
            subnet.append(splits[i]+".");
        }

        Log.d(TAG, "discovered subnet = "+subnet.toString());
        return subnet.toString();
    }

    private void startSubNetScan(){
        final String subnet = getSubNet(context);
        found = false;

        Observable<String> observable = scanSubnetObservable(subnet, 1, 127)
                .mergeWith(scanSubnetObservable(subnet, 128, 255));

        if(subnet.equals("192.168.1.")){
            observable = observable.mergeWith(scanSubnetObservable("192.168.0.", 1, 127)
                    .mergeWith(scanSubnetObservable("192.168.0.", 128, 255)));
        }else if(subnet.equals("192.168.0.")){
            observable = observable.mergeWith(scanSubnetObservable("192.168.1.", 1, 127)
                    .mergeWith(scanSubnetObservable("192.168.1.", 128, 255)));
        }

        observable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String host) {

                        found = true;
//                        cancel = true;
                        Log.d(TAG, "On Next " + host);
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        prefs.edit().putString(LAST_DISCOVERED_HOST, host).commit();
                        listener.onHostFound(host);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        if(!found) {
                            listener.onNoHostFound();
                            HomeTheaterApplication.getBus().post(new ResourceServerConnectFailed());
                        }
                    }
                });

    }

    private Observable<String> scanSubnetObservable(final String subnet, final int startAddress, final int endAddress){
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                scanSubNet(subnet,startAddress,endAddress, subscriber);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread());
    }


    private String scanSubNet(String subnet, int startAddress, int endAddress, Subscriber subscriber){
        InetAddress inetAddress = null;
        for(int i=startAddress; i <= endAddress; i++){
            if(found || cancel){
                subscriber.onCompleted();
                return null;
            }
//                Log.d(TAG, "Trying: " + subnet + String.valueOf(i));
            try {
                inetAddress = InetAddress.getByName(subnet + String.valueOf(i));
//                    if(inetAddress.isReachable(200)){
//                        Log.d(TAG, inetAddress.getHostName() + "is reachable");
                if(hostAcceptsRequest(inetAddress.getHostName())){
                    subscriber.onNext(inetAddress.getHostName());
                    return inetAddress.getHostName();
                }
//                    }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.d(TAG, "IO exception, failed on address "+inetAddress.getHostName());
//                    e.printStackTrace();
            }
        }
        return null;
    }

    private Observable<String> hostAcceptsObservable(final String host){
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    if(hostAcceptsRequest(host)){
                        subscriber.onNext(host);
                    }else{
                        subscriber.onError(new Throwable("could not connect to host"));
                    }
                } catch (IOException e) {
                    subscriber.onError(new Throwable("could not connect to host"));
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io());
    }
}