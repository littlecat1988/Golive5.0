package com.golive.cinema.player.controller;

import android.view.View;

/**
 * Created by Wangzj on 2016/9/30.
 */
public interface IMediaController {
    void hide();

    boolean isShowing();

    void setAnchorView(View view);

    void setEnabled(boolean enabled);

    void setMediaPlayer(CustomMediaController.MediaPlayerControl player);

    void show(int timeout);

    void show();

    //----------
    // Extends
    //----------
    void showOnce(View view);
}
