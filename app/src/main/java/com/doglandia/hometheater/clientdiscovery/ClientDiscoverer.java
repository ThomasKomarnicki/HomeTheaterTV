package com.doglandia.hometheater.clientdiscovery;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;

import com.doglandia.hometheater.MediaLoaderApplication;
import com.doglandia.hometheater.event.ResourceServerConnectFailed;
import com.doglandia.hometheater.resourceserver.ResourceServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    private boolean found = false;

    private boolean cancel = false;

    private int discoveryTasksRunning = 0;

    public ClientDiscoverer(Context context, OnHostFoundListener onHostFoundListener){
        this.context = context;
        this.listener = onHostFoundListener;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(200, TimeUnit.MILLISECONDS);
        client = builder.build();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.contains(LAST_DISCOVERED_HOST)){
            HostCheckTask hostCheckTask = new HostCheckTask();
            hostCheckTask.execute(prefs.getString(LAST_DISCOVERED_HOST,""));
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
        found = false;

        String subnet = getSubNet(context);

        discoveryTasksRunning = 0;
        DiscoveryTask discoveryTask = new DiscoveryTask();
        // scan subnet
        discoveryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, subnet,"1-127");
        discoveryTask = new DiscoveryTask();
        discoveryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, subnet,"128-255");
        discoveryTasksRunning +=2;

        // also scan other subnet? one for hard wire one for wifi?
        discoveryTask = new DiscoveryTask();
        if(subnet.equals("192.168.1.")){
            discoveryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "192.168.0.","1-127");
            discoveryTask = new DiscoveryTask();
            discoveryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "192.168.0.","128-255");
        }else if(subnet.equals("192.168.0.")){
            discoveryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "192.168.1.","1-127");
            discoveryTask = new DiscoveryTask();
            discoveryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "192.168.1.","128-255");
        }
        discoveryTasksRunning +=2;
    }

    // scans subnet for appropriate client

    class DiscoveryTask extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {

            String[] range = params[1].split("-");
            return scanSubNet(params[0],Integer.valueOf(range[0]),Integer.valueOf(range[1])); // todo
        }

        @Override
        protected void onPostExecute(String host) {
            super.onPostExecute(host);
            if(host != null) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putString(LAST_DISCOVERED_HOST, host).commit();
                found = true;
                listener.onHostFound(host);
            }else if(!found){
                listener.onNoHostFound();
            }
            discoveryTasksRunning--;

            if(discoveryTasksRunning == 0 && !cancel){
                // todo not connected
                MediaLoaderApplication.getBus().post(new ResourceServerConnectFailed());
            }
        }

        private String scanSubNet(String subnet, int startAddress, int endAddress){
            InetAddress inetAddress = null;
            for(int i=startAddress; i <= endAddress; i++){
                if(found || cancel){
                    cancel(true);
                    return null;
                }
//                Log.d(TAG, "Trying: " + subnet + String.valueOf(i));
                try {
                    inetAddress = InetAddress.getByName(subnet + String.valueOf(i));
//                    if(inetAddress.isReachable(200)){
//                        Log.d(TAG, inetAddress.getHostName() + "is reachable");
                        if(hostAcceptsRequest(inetAddress.getHostName())){
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

    }

    // checks to make sure client still exists, if not start scanning the subnet
    class HostCheckTask extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {
            try {
                if(hostAcceptsRequest(params[0])){
                    return params[0];
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String host) {
            super.onPostExecute(host);
            if(host != null){
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit().putString(LAST_DISCOVERED_HOST,host).commit();
                listener.onHostFound(host);
            }else{
                // notify that we failed to reconnect on last discovered host, try to rediscover host
//                MediaLoaderApplication.getBus().post(new ResourceServerConnectFailed());
                startSubNetScan();
            }
        }
    }

}