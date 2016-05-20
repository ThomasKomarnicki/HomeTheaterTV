package com.doglandia.hometheater.playmedia;

import android.net.Uri;

public interface MediaPlayerFragment {
    void play();
    void pause();
    void setVideo(Uri uri);
    void seekForward();
    void seekBackward();
    int getCurrentPlayPosition();
    void setDuration(PlayMediaActivity playMediaActivity);

    boolean isPlaying();
}
