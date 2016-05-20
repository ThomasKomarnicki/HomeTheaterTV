package com.doglandia.hometheater.playmedia;

import android.app.Activity;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;

import com.doglandia.hometheater.MediaLoaderApplication;
import com.doglandia.hometheater.R;
import com.doglandia.hometheater.model.Resource;
import com.doglandia.hometheater.resourceserver.ResourceServer;

/**
 * Created by tdk10 on 2/21/2016.
 */
public class PlayMediaActivity extends Activity{

//    private VideoView mVideoView;

    private MediaSession mSession;

    private PlaybackControlsFragment playbackControlsFragment;
    private MediaPlayerFragment mediaPlayerFragment;

    private ResourceServer server;

    public MediaPlayerFragment getMediaPlayerFragment() {
        return mediaPlayerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playback_controls);

        Resource resource = getIntent().getParcelableExtra("resource");

        mediaPlayerFragment = (MediaPlayerFragment) getFragmentManager().findFragmentById(R.id.media_player_fragment);

        server = ((MediaLoaderApplication) getApplication()).getResourceServer();
        String videoPath = server.getMediaUrl(resource);
        mediaPlayerFragment.setVideo(Uri.parse(videoPath));

        mSession = new MediaSession(this, "MediaLoader");
        mSession.setCallback(new MediaSession.Callback() {

        });
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mSession.setActive(true);


        playbackControlsFragment = (PlaybackControlsFragment) getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        playbackControlsFragment.setMediaPlaybackListener(new MediaPlaybackListener() {
            @Override
            public void onPlay() {
                mediaPlayerFragment.play();
            }

            @Override
            public void onPause() {
                mediaPlayerFragment.pause();
            }

            @Override
            public void onFastForward() {
                seekForward();
            }

            @Override
            public void onRewind() {
                seekBackward();
            }

            @Override
            public void onVideoChange(Resource resource) {
                changeVideo(resource);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mVideoView.suspend();
        mediaPlayerFragment.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSession.setActive(true);
        mediaPlayerFragment.setDuration(PlayMediaActivity.this);
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (mediaPlayerFragment.isPlaying()) {
//            if (!requestVisibleBehind(true)) {
//                // Try to play behind launcher, but if it fails, stop playback.
//                stopPlayback();
//            }
//        } else {
//            requestVisibleBehind(false);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSession.release();
    }

    private void stopPlayback() {
        mediaPlayerFragment.pause();
    }

    public void seekForward(){
        mediaPlayerFragment.seekForward();
    }

    public void seekBackward(){
        mediaPlayerFragment.seekBackward();
    }

    public void changeVideo(Resource resource){
        mediaPlayerFragment.setVideo(Uri.parse(server.getMediaUrl(resource)));
    }

    private void updatePlaybackPosition(){
        playbackControlsFragment.setPlayDuration(mediaPlayerFragment.getCurrentPlayPosition());
    }

    public void setVideoDuration(int duration) {
        playbackControlsFragment.setTotalPlayDuration(duration);
    }
}