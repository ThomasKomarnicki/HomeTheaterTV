package com.doglandia.hometheater.playmedia;

import com.doglandia.hometheater.model.Resource;

/**
 * Created by tdk10 on 2/26/2016.
 */
public interface MediaPlaybackListener {
    void onPlay();
    void onPause();
    void onFastForward();
    void onRewind();
    void onVideoChange(Resource resource);
}
