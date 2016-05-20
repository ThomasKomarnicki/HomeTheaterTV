package com.doglandia.hometheater.videolib;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

import com.doglandia.hometheater.MediaLoaderApplication;
import com.doglandia.hometheater.R;
import com.doglandia.hometheater.event.ResourceServerConnectFailed;
import com.doglandia.hometheater.model.Resource;
import com.doglandia.hometheater.model.ResourceGroup;
import com.doglandia.hometheater.playmedia.PlayMediaActivity;
import com.doglandia.hometheater.resourceserver.ResourceServer;

import java.util.List;

/**
 * Created by tdk10 on 2/21/2016.
 */
public class VideoLibraryFragment extends BrowseFragment {

    public static final int PLAY_MEDIA_REQUEST = 8;

    public static final int MEDIA_COULDNT_CONNECT = 9;

//    private ArrayObjectAdapter mRowsAdapter;

    private List<ResourceGroup> resourceGroups;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

//        setTitle("Home Theater");
        setBadgeDrawable(getResources().getDrawable(R.drawable.badge));
        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.primary));
        // set search icon color
//        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));

        setOnItemViewClickedListener(new ItemViewClickedListener());

//        MediaLoaderApplication.getBus().register(this);

//        if(((MediaLoaderApplication) getActivity().getApplication()).getResourceServer().isConnected()){
//            getResourceData();
//        }
    }

    public void initViews(List<ResourceGroup> resourceGroups){
        this.resourceGroups = resourceGroups;

        ArrayObjectAdapter mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        VideoPresenter presenter = new VideoPresenter();

        int i;
        for (i = 0; i < resourceGroups.size(); i++) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(presenter);
            ResourceGroup resourceGroup = resourceGroups.get(i);
            for (int j = 0; j < resourceGroup.getResourceList().size(); j++) {
                listRowAdapter.add(resourceGroup.getResourceList().get(j));
            }
            HeaderItem header = new HeaderItem(i, resourceGroup.getName());
            mRowsAdapter.add(new ListRow(header, listRowAdapter));
        }

        HeaderItem gridHeader = new HeaderItem(i, "More");

        IconPresenter iconPresenter = new IconPresenter();
        ArrayObjectAdapter gridRowAdapter = new ArrayObjectAdapter(iconPresenter);
        gridRowAdapter.add(new ActionIcon(R.drawable.ic_refresh_black_24dp, "Refresh", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((VideoLibraryActivity) getActivity()).refreshResourceData();
            }
        }));
//        gridRowAdapter.add(new ActionIcon(R.drawable.ic_refresh_black_24dp, "Help", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        }));
        mRowsAdapter.add(new ListRow(gridHeader, gridRowAdapter));

        setAdapter(mRowsAdapter);

    }

    private ResourceGroup getResourceGroupForResource(Resource resource){
        for(ResourceGroup resourceGroup : resourceGroups){
            for(Resource resource1 : resourceGroup.getResourceList()){
                if(resource.getLocation().equals(resource1.getLocation())){
                    return resourceGroup;
                }
            }
        }

        return null;
    }


    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Resource) {
                Resource resource = (Resource) item;
                if (!resource.isNativeFormat()) {
                    ResourceServer server = ((MediaLoaderApplication) getActivity().getApplication()).getResourceServer();
                    String videoPath = server.getMediaUrl((Resource) item);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoPath));

                    PackageManager manager = getActivity().getPackageManager();
                    List<ResolveInfo> infos = manager.queryIntentActivities(intent, 0);
                    if (infos.size() > 1 || (infos.size() == 1 && !infos.get(0).activityInfo.packageName.contains("frameworkpackagestubs"))) {
                        //Then there is application can handle your intent
//                        intent.setData(Uri.parse(videoPath));
                        intent.setDataAndType(Uri.parse(videoPath), "video/avi");
                        startActivity(intent);
                    }else{
                        //No Application can handle your intent
                        createMXPlayerDialog();
                    }
                }else {

                    Intent intent = new Intent(getActivity(), PlayMediaActivity.class);
                    intent.putExtra("resource", resource);
                    intent.putExtra("resource_group", getResourceGroupForResource(resource));

                    Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            getActivity(),
                            ((ImageCardView) itemViewHolder.view).getMainImageView(),
                            "dog").toBundle();
                    startActivityForResult(intent, PLAY_MEDIA_REQUEST, bundle);
                }
            }
            if(item instanceof ActionIcon){
                ((ActionIcon) item).getOnClickListener().onClick(itemViewHolder.view);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PLAY_MEDIA_REQUEST && resultCode == MEDIA_COULDNT_CONNECT){
            VideoLibraryActivity activity = (VideoLibraryActivity) getActivity();
            activity.onResourceServerReconnectFailed(new ResourceServerConnectFailed());
        }
    }

    private void createMXPlayerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Install", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.mxtech.videoplayer.ad")));


            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setMessage("To play this video you'll need to download a player that supports it. MX Player is a good choice");
        builder.create().show();
    }
}
