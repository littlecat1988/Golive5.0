package com.golive.cinema.player;

/**
 * Created by Wangzj on 2016/9/12.
 */

public class PlayerState {

    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_STOPPING = 5;
    public static final int STATE_STOPPED = 6;
    public static final int STATE_BUFFERING = 7;
    public static final int STATE_BUFFEREND = 8;
    public static final int STATE_PLAYBACK_COMPLETED = 100;
}
