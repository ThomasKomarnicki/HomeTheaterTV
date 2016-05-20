package com.doglandia.hometheater.playmedia;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.doglandia.hometheater.R;
import com.doglandia.hometheater.playmedia.demoplayer.DemoPlayer;
import com.doglandia.hometheater.playmedia.demoplayer.EventLogger;
import com.doglandia.hometheater.playmedia.demoplayer.ExtractorRendererBuilder;
import com.doglandia.hometheater.videolib.VideoLibraryFragment;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.upstream.HttpDataSource;
import com.google.android.exoplayer.util.Util;

public class ExoPlayerFragment extends Fragment implements MediaPlayerFragment, DemoPlayer.Listener {

    private static final String TAG = "ExoPlayerFragment";

    private SurfaceView surfaceView;
    private Uri videoUri;
    private DemoPlayer player;

    private EventLogger eventLogger;

    private long playerPosition;
    private boolean playerNeedsPrepare;

    private boolean durationSet = false;

    private PlayMediaActivity playMediaActivity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.exo_player, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        surfaceView = (SurfaceView) view.findViewById(R.id.surface);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initPlayer();
    }

    @Override
    public void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Override
    public void play() {
        player.getPlayerControl().start();
    }

    @Override
    public void pause() {
        if(player != null) {
            player.getPlayerControl().pause();
        }
    }

    @Override
    public void setVideo(Uri uri) {
        this.videoUri = uri;
        Log.d(TAG, "playing: "+uri);

        if(player != null){
            preparePlayer(true);
        }
    }

    @Override
    public void seekForward() {

        player.getPlayerControl().seekTo((int) (getCurrentPlayPosition() + getSeekChange()));
    }

    @Override
    public void seekBackward() {
        player.getPlayerControl().seekTo((int) (getCurrentPlayPosition() - getSeekChange()));
    }

    @Override
    public int getCurrentPlayPosition() {
        return player.getPlayerControl().getCurrentPosition();
    }

    @Override
    public void setDuration(PlayMediaActivity playMediaActivity) {
        this.playMediaActivity = playMediaActivity;

    }

    @Override
    public boolean isPlaying() {
        return player.getPlaybackState() == 0; // todo
    }

    private long getSeekChange(){
        return 1000 * 10;
    }

    private void initPlayer(){
        preparePlayer(true);
    }

    private void preparePlayer(boolean playWhenReady) {
        Context context = getActivity();
        String userAgent = Util.getUserAgent(context, "MediaLoader");
        DemoPlayer.RendererBuilder rendererBuilder = new ExtractorRendererBuilder(context, userAgent, videoUri);
        if(player != null) {
            player.release();
        }

        durationSet = false;

        player = new DemoPlayer(rendererBuilder);
        player.addListener(new DemoPlayer.Listener() {
            @Override
            public void onStateChanged(boolean playWhenReady, int playbackState) {
                if(playbackState == ExoPlayer.STATE_READY && !durationSet) {
                    playMediaActivity.setVideoDuration(player.getPlayerControl().getDuration());
                    durationSet = true;
                }
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, "exo player listener error");
                e.printStackTrace();
                if(e.getCause() instanceof HttpDataSource.HttpDataSourceException){
                    getActivity().setResult(VideoLibraryFragment.MEDIA_COULDNT_CONNECT);
                    getActivity().finish();
                }
            }

            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

            }
        });
        player.seekTo(0);
        playerNeedsPrepare = true;

        eventLogger = new EventLogger();
        eventLogger.startSession();
        player.addListener(eventLogger);
        player.setInfoListener(eventLogger);
        player.setInternalErrorListener(eventLogger);

        player.prepare();
        playerNeedsPrepare = false;

        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {

        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch(playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
        Log.i(TAG, text);
    }

    @Override
    public void onError(Exception e) {
        String errorString = null;
        playerNeedsPrepare = true;
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
//            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
            e.printStackTrace();
        } else if (e instanceof ExoPlaybackException
                && e.getCause() instanceof MediaCodecTrackRenderer.DecoderInitializationException) {
            // Special case for decoder initialization failures.
            MediaCodecTrackRenderer.DecoderInitializationException decoderInitializationException =
                    (MediaCodecTrackRenderer.DecoderInitializationException) e.getCause();
            if (decoderInitializationException.decoderName == null) {
                if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                    errorString = "error querying decoders" ;
                } else if (decoderInitializationException.secureDecoderRequired) {
                    errorString = "error no secure decoder "+
                            decoderInitializationException.mimeType;
                } else {
                    errorString = "error no decoder "+ decoderInitializationException.mimeType;
                }
            } else {
                errorString = "error instantiating decoder "+ decoderInitializationException.decoderName;
            }
        }
        if (errorString != null) {
            Toast.makeText(getActivity(), errorString, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

    }

    private void releasePlayer() {

        if (player != null) {
//            debugViewHelper.stop();
//            debugViewHelper = null;
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
        }
    }
}
