package com.golive.cinema.player.noencrypt;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.widget.VideoView;

import com.golive.cinema.player.DefaultPlayerOperation;
import com.golive.cinema.player.PlayerState;
import com.golive.cinema.player.PlayerValidateCallback;
import com.initialjie.log.Logger;

/**
 * Created by Wangzj on 2016/9/12.
 */

public class PlayerOperationImpl extends DefaultPlayerOperation
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener {

    private final VideoView mVideoView;
    private int mSeekWhenPrepared;
    private int mDuration;
    private int mCurPos;

    public PlayerOperationImpl(@NonNull VideoView videoView) {
        mVideoView = checkNotNull(videoView, "videoView cannot be null!");
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnPreparedListener(this);
    }

    @Override
    public synchronized boolean startPlayer() {
        Logger.d("startPlayer, player state : " + getPlayerState());
        if (null == mVideoView) {
            return false;
        }

        if (isPlaying()) {
            fireOnPlayerStart();
            return false;
        }

        boolean newStart = false;

        if (isInPlaybackState()) { // 已经准备
            Logger.d("startPlayer, isInPlaybackState");
            // 刚刚准备好
            if (PlayerState.STATE_PREPARED == getPlayerState()
                    || PlayerState.STATE_PAUSED == getPlayerState()) {
                int seekToPosition = mSeekWhenPrepared;
                if (seekToPosition != 0) {
                    Logger.d("startPlayer, seek to : " + mSeekWhenPrepared);
                    // seek
                    seekTo(seekToPosition);
                }
            }

            start();
        } else if (PlayerState.STATE_PREPARING != getPlayerState()) { // 不是正在准备
            PlayerValidateCallback validateCallback = getPlayerValidateCallback();
            // 已经过期
            if (validateCallback != null && !validateCallback.onValidate()) {
                // 停止播放器
                stopPlayer();
                validateCallback.onOverdue();
                return false;
            }

            newStart = true;
            fireOnPlayerPreparing();
            int seekToPosition = mSeekWhenPrepared;
            if (seekToPosition != 0) {
                // 跳转到上一次位置
                seekTo(seekToPosition);
//                fireOnPlayerStart();
            }

            String url = getPlayUrl();
            // String url = "http://www.baidu.com/1.mp4";

            Logger.d("startPlayer, seek position : " + seekToPosition + ", url : " + url);
            mVideoView.setVideoPath(url);
            //http://letv-cdn.golivetv
            // .tv/NoCrypt/2016/11/16/dieyingchongchong5/dieyingchongchong5_2400.mp4
        }

        return newStart;
    }

    @Override
    public boolean resumePlayer() {
        Logger.d("resumePlayer");
        setTargetState(PlayerState.STATE_PLAYING);
        if (isInPlaybackState()) {
            start();
            fireOnPlayerResumed();
        }
        return true;
    }

    /**
     * do start
     */
    private void start() {
        if (mVideoView != null) {
            mVideoView.start();
            fireOnPlayerStart();
        }
    }

    @Override
    public boolean pausePlayer() {
        Logger.d("pausePlayer");
//        if (!isPlaying() && PlayerState.STATE_PAUSED != getPlayerState()) {
//            return false;
//        }

        setTargetState(PlayerState.STATE_PAUSED);
        if (isInPlaybackState()) {
            // is playing
            if (isPlaying()) {
                // pause
                pause();
                fireOnPlayerPaused();
            }
        }
        return true;
    }

    /**
     * do pause
     */
    private void pause() {
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }

    @Override
    public boolean stopPlayer() {
        Logger.d("stopPlayer");
//        if (!canStop()) {
//            return false;
//        }
        setTargetState(PlayerState.STATE_STOPPED);
        if (mVideoView != null) {
            fireOnPlayerStopping();
            try {
                Logger.d("stopPlayback");
                mVideoView.stopPlayback();
            } catch (Exception e) {
                e.printStackTrace();
            }
            fireOnPlayerStopped();
        }
        return true;
    }

    @Override
    public synchronized int getDuration() {
        if (mVideoView != null && isInPlaybackState()) {
            int duration = mVideoView.getDuration();
            mDuration = duration;
            return duration;
        } else if (mDuration > 0) {
            return mDuration;
        }
        return 0;
    }

    @Override
    public synchronized int getCurrentPosition() {
        if (mVideoView != null && isInPlaybackState()) {
            int currentPosition = mVideoView.getCurrentPosition();
            mCurPos = currentPosition;
            return currentPosition;
        } else if (mCurPos > 0) {
            return mCurPos;
        }
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getFakeDuration() {
        return 0;
    }

    @Override
    public boolean canPause() {
        if (mVideoView != null) {
            return mVideoView.canPause();
        }
        return false;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            setSeekDuration(getCurrentPosition());
            _seekTo(msec);
            fireOnPlayerSeek(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    private void _seekTo(int msec) {
        mVideoView.seekTo(msec);
    }

    @Override
    public boolean canSeekBackward() {
        if (mVideoView != null) {
            return mVideoView.canSeekBackward();
        }
        return false;
    }

    @Override
    public boolean canSeekForward() {
        if (mVideoView != null) {
            return mVideoView.canSeekForward();
        }
        return false;
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    @Override
    public boolean isLocalPlay() {
        return false;
    }

    @Override
    public void setSharpness(int index, boolean effect) {
        if (getSharpness() == index) {
            return;
        }
        super.setSharpness(index, effect);

        if (effect) {
            stopPlayer();
            startPlayer();
        }
    }

    @Override
    public boolean changeRankRestart(String rank) {
        Logger.d("changeRankRestart:");
        int oldRank = getRank();
        boolean restart = super.changeRankRestart(rank);
        Logger.d("restart:" + restart);
        if (restart) {
//            mSeekWhenPrepared = 0;
            fireOnPlayerRankChanged(restart, oldRank, getRank());
            stopPlayer();
            startPlayer();
//            fireOnPlayerStart();
        }
        return restart;
    }

    @Override
    public boolean isBufferedSupport() {
        return true;
    }

    @Override
    public boolean isMediaIpSupport() {
        return false;
    }

    @Override
    public boolean isPlaying() {
        try {
            return super.isPlaying() || (mVideoView != null && mVideoView.isPlaying());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Logger.d("onCompletion");
        fireOnPlayerCompleted();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Logger.e("onError! what : " + what + ", extra : " + extra);
        fireOnPlayerError(what, extra, null);
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Logger.d("onPrepared");
        mp.setOnInfoListener(this);
        mp.setOnBufferingUpdateListener(this);

        int targetState = getTargetState();
        setPlayerState(PlayerState.STATE_PREPARED);
        // mSeekWhenPrepared may be changed after seekTo() call
        int seekToPosition = mSeekWhenPrepared;
        if (seekToPosition != 0) {
            seekTo(seekToPosition);
        }

        fireOnPlayerPrepared();

        switch (targetState) {
            case PlayerState.STATE_PLAYING:  // target to play
                startPlayer();
                break;
            case PlayerState.STATE_PAUSED:  // target to pause
                pausePlayer();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Logger.d("onInfo, what : " + what);
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:// buffer start
                fireOnPlayerBuffering(false, 0, "");
                return true;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:// buffer end
                fireOnPlayerBuffering(true, 100, "");
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
//        Logger.d("onBufferingUpdate, percent : " + percent);
        fireOnBufferingUpdate(percent);
    }
}
