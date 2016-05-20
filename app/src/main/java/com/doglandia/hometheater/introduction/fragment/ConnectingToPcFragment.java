package com.doglandia.hometheater.introduction.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v17.leanback.widget.ItemBridgeAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.doglandia.hometheater.MediaLoaderApplication;
import com.doglandia.hometheater.R;
import com.doglandia.hometheater.event.ResourceServerConnectFailed;
import com.doglandia.hometheater.event.ResourceServerConnected;
import com.doglandia.hometheater.introduction.IntroductionActivity;
import com.doglandia.hometheater.model.ResourceGroup;
import com.doglandia.hometheater.model.ResourcesResponse;
import com.doglandia.hometheater.resourceserver.ResourceServer;
import com.doglandia.hometheater.videolib.VideoLibraryActivity;
import com.squareup.otto.Subscribe;

import java.util.List;

import rx.functions.Action1;


/**
 * Created by tdk10 on 2/27/2016.
 */
public class ConnectingToPcFragment extends Fragment {

//    private CountDownTimer countDownTimer;

    private boolean doneCountingDown = false;
    private List<ResourceGroup> resourceGroups;

//    private HorizontalGridView horizontalGridView;
    private ItemBridgeAdapter bridgeAdapter;

    private View errorView;
    private View connectingView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connecting, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        connectingView = view.findViewById(R.id.connecting_view);
        errorView = view.findViewById(R.id.connection_error_view);
        view.findViewById(R.id.try_again).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryConnection();
            }
        });
        view.findViewById(R.id.go_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackToWalkThrough();
            }
        });

        showConnectingView();

//        horizontalGridView = (HorizontalGridView) view.findViewById(R.id.gridView);

//        ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(new IconPresenter());
//        arrayObjectAdapter.add(new ActionIcon(R.drawable.ic_refresh_black_24dp, "Go Back",
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                }));
//        arrayObjectAdapter.add(new ActionIcon(R.drawable.ic_refresh_black_24dp, "Retry",
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                    }
//                }));
//        bridgeAdapter = new ItemBridgeAdapter(arrayObjectAdapter);
//        horizontalGridView.setAdapter(bridgeAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MediaLoaderApplication.getBus().register(this);

        startClientDiscovery();

    }

    private void startClientDiscovery(){
        // wait atlesat 3 seconds before continuing to the next screen
        CountDownTimer countDownTimer = new CountDownTimer(3000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                doneCountingDown = true;
                onResourceGroupsRetrieved();
            }
        };
        countDownTimer.start();

        ((MediaLoaderApplication) getActivity().getApplication()).getResourceServer().startClientDiscovery();
    }

    @Subscribe
    public void onResourceServerConnected(ResourceServerConnected resourceServerConnected){

        if(getActivity() != null) { //
            ResourceServer server = ((MediaLoaderApplication) getActivity().getApplication()).getResourceServer();
            server.getResourceGroups().subscribe(new Action1<ResourcesResponse>() {
                @Override
                public void call(ResourcesResponse resourcesResponse) {
                    resourceGroups = resourcesResponse.getResourceGroups();
                    onResourceGroupsRetrieved();
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        }
    }

    @Subscribe
    public void onResourceServerConnectionFailed(ResourceServerConnectFailed event){
        showConnectingErrorView();
    }

    private void onResourceGroupsRetrieved(){
        if(resourceGroups == null || !doneCountingDown){
            return;
        }
        // if resource groups size == 0, set message for adding folders, maybe should do this at video lib screen

        // else go to video library

        if(resourceGroups.size() != 0){
            setFirstRunPreference();
            Intent intent = new Intent(getActivity(), VideoLibraryActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    private void setFirstRunPreference(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(IntroductionActivity.FIRST_RUN_CONNECTED,true);
        editor.commit();
    }

    private void showConnectingView(){
        connectingView.setVisibility(View.VISIBLE);
        errorView.setVisibility(View.GONE);
    }

    private void showConnectingErrorView(){
        errorView.setVisibility(View.VISIBLE);
        connectingView.setVisibility(View.GONE);
//        horizontalGridView.setSelected(true);
//        horizontalGridView.setSelectedPosition(0);
    }

    private void goBackToWalkThrough(){
        Intent intent = new Intent(getActivity(), IntroductionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void retryConnection(){
        startClientDiscovery();
        showConnectingView();
    }
}
