package com.doglandia.hometheater.playmedia;

import android.os.Bundle;
import android.support.v17.leanback.app.PlaybackOverlayFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.util.Log;

import com.doglandia.hometheater.model.Resource;
import com.doglandia.hometheater.model.ResourceGroup;
import com.doglandia.hometheater.videolib.VideoPresenter;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tdk10 on 2/26/2016.
 */
public class PlaybackControlsFragment extends PlaybackOverlayFragment {

    private static final String TAG = "PlabackControls";
    private PlaybackControlsRow.PlayPauseAction playPauseAction;
    private PlaybackControlsRow.RewindAction rewindAction;
    private PlaybackControlsRow.FastForwardAction fastForwardAction;

    private PlaybackControlsRow controlsRow;

    private MediaPlaybackListener mediaPlaybackListener;

    private MediaPlayerFragment mediaPlayerFragment;

    private Timer playbackTimer;
    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            final int playbackPosition = mediaPlayerFragment.getCurrentPlayPosition();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    controlsRow.setCurrentTime(playbackPosition);
                }
            });
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        playbackTimer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        playbackTimer = new Timer();
        playbackTimer.schedule(timerTask, 1000, 1000);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        int[] playbackSpeeds = new int[]{PlaybackControlGlue.PLAYBACK_SPEED_NORMAL, PlaybackControlGlue.PLAYBACK_SPEED_FAST_L0};
//        playbackGlue = new PlaybackGlue(getActivity(), this, playbackSpeeds, playbackSpeeds);

        Resource resource = getActivity().getIntent().getParcelableExtra("resource");
        ResourceGroup resourceGroup = getActivity().getIntent().getParcelableExtra("resource_group");

        PlayMediaActivity activity = (PlayMediaActivity) getActivity();
        mediaPlayerFragment = activity.getMediaPlayerFragment();


        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        final SparseArrayObjectAdapter controlsAdapter = new SparseArrayObjectAdapter(presenterSelector);
        playPauseAction = new PlaybackControlsRow.PlayPauseAction(activity);
        rewindAction = new PlaybackControlsRow.RewindAction(activity);
        fastForwardAction = new PlaybackControlsRow.FastForwardAction(activity);

        controlsAdapter.set(0,rewindAction);
        controlsAdapter.set(1, playPauseAction);
        controlsAdapter.set(2, fastForwardAction);

        playPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PAUSE);

        controlsRow = new PlaybackControlsRow();
        controlsRow.setPrimaryActionsAdapter(controlsAdapter);


        PlaybackControlsRowPresenter playbackControlsRowPresenter = new PlaybackControlsRowPresenter();
        playbackControlsRowPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                Log.i(TAG, "action "+action.getId() + " clicked: "+action);
                if (action.getId() == playPauseAction.getId()) {
                    boolean playing = playPauseAction.getIndex() == PlaybackControlsRow.PlayPauseAction.PAUSE;

//                    boolean playing = playbackActivity.togglePlayback();
                    Log.i(TAG, "playing = "+playing);
                    if(playing) {
//                        playbackTimer.cancel();
//                        playPauseAction.setIcon(playPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PAUSE));
                        playPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PLAY);
                        mediaPlaybackListener.onPause();
                    }else{
//                        playbackTimer.schedule(timerTask, 1000, 1000);
//                        playPauseAction.setIcon(playPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PLAY));
                        playPauseAction.setIndex(PlaybackControlsRow.PlayPauseAction.PAUSE);
                        mediaPlaybackListener.onPlay();
                    }
                    controlsAdapter.notifyArrayItemRangeChanged(1,1);


                } else if (action.getId() == fastForwardAction.getId()) {
//                    playbackActivity.seekForward();
                    mediaPlaybackListener.onFastForward();
                } else if (action.getId() == rewindAction.getId()) {
//                    playbackActivity.seekBackward();
                    mediaPlaybackListener.onRewind();
                }
                final int playbackPosition = mediaPlayerFragment.getCurrentPlayPosition();
                controlsRow.setCurrentTime(playbackPosition);

            }
        });

        ClassPresenterSelector ps = new ClassPresenterSelector();

        ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);
        ps.addClassPresenter(ListRow.class, new ListRowPresenter());

        ArrayObjectAdapter arrayObjectAdapter = new ArrayObjectAdapter(ps);
        arrayObjectAdapter.add(controlsRow);

        HeaderItem header = new HeaderItem(0, resourceGroup.getName());
        ListRow otherVideosRow = new ListRow(header,createOtherVideosRow(resourceGroup));
        arrayObjectAdapter.add(otherVideosRow);
        setAdapter(arrayObjectAdapter);

        playPauseAction.setIcon(playPauseAction.getDrawable(PlaybackControlsRow.PlayPauseAction.PAUSE));

        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                // todo change video
                if(item instanceof Resource) {
                    mediaPlaybackListener.onVideoChange((Resource) item);
                }

            }
        });
    }

    private ArrayObjectAdapter createOtherVideosRow(ResourceGroup resourceGroup){
        VideoPresenter presenter = new VideoPresenter();
        ArrayObjectAdapter adapter = new ArrayObjectAdapter(presenter);

            for (int i = 0; i < resourceGroup.getResourceList().size(); i++) {
                adapter.add(resourceGroup.getResourceList().get(i));
            }
        return adapter;
    }

    public void setMediaPlaybackListener(MediaPlaybackListener mediaPlaybackListener) {
        this.mediaPlaybackListener = mediaPlaybackListener;
    }

    public void setPlayDuration(int millis){
        controlsRow.setCurrentTime(millis);
    }

    public void setTotalPlayDuration(int millis){
        controlsRow.setTotalTime(millis);
        controlsRow.setBufferedProgress(millis);
        controlsRow.setCurrentTime(0);
        ((ArrayObjectAdapter) getAdapter()).notifyArrayItemRangeChanged(0,getAdapter().size());
    }
}
