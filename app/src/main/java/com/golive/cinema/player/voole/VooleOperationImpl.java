package com.golive.cinema.player.voole;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.media.MediaPlayer;
import android.support.annotation.NonNull;

import com.golive.cinema.player.DefaultPlayerOperation;
import com.golive.cinema.player.PlayerState;
import com.golive.cinema.player.PlayerValidateCallback;
import com.initialjie.log.Logger;
import com.vad.sdk.core.base.AdEvent;
import com.voole.epg.corelib.model.play.PlayInfo;
import com.voole.player.lib.core.VooleMediaPlayer;
import com.voole.player.lib.core.VooleMediaPlayerListener;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Wangzj on 2016/12/27.
 */
public class VooleOperationImpl extends DefaultPlayerOperation implements VooleMediaPlayerListener {

    @NonNull
    private final VooleMediaPlayer mVooleMediaPlayer;

    @NonNull
    private final Voole mVoole;

    private int mSeekWhenPrepared;
    private boolean mCanSeekBackward;
    private boolean mCanSeekForward;
    private int mCurPos;
    private int mDuration;
    private int mBufferedPercent;
    private boolean mIsQuit;
    private final CompositeSubscription mCompositeSubscription;

    public VooleOperationImpl(@NonNull VooleMediaPlayer vooleMediaPlayer, @NonNull Voole voole) {
        mVooleMediaPlayer = checkNotNull(vooleMediaPlayer);
        mVoole = checkNotNull(voole);
        mVooleMediaPlayer.setMediaPlayerListener(this);
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public synchronized boolean startPlayer() {
        Logger.d("startPlayer, player state : " + getPlayerState());
        setQuit(false);
        boolean newStart = false;
        if (isInPlaybackState()) { // 已经准备
            // prepared
            if (PlayerState.STATE_PREPARED == getPlayerState()) {
                int seekToPosition = mSeekWhenPrepared;
                if (seekToPosition != 0) {
                    Logger.d("startPlayer, seek to : " + mSeekWhenPrepared);
                    // seek
                    seekTo(seekToPosition);
                }
                start();
            }
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
            String url = getPlayUrl();
            Logger.d("startPlayer, url : " + url);

            String[] strings = url.split(";");
            final String downUrl = strings[0];
            final String mid = strings[1];
            final String sid = strings[2];
            final String fid = strings[3];
            final String mType = strings[4];

            // get play info
            Subscription subscription = mVoole.getPlayInfo(mid, fid, sid, mType)
                    // subscribe on IO thread
                    .subscribeOn(Schedulers.io())
                    // observer on main thread
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<PlayInfo>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.e(e, "getPlayInfo, onError : ");
                        }

                        @Override
                        public void onNext(PlayInfo playInfo) {
                            preparePlayer(playInfo, mid, sid, fid, downUrl, mType);
                        }
                    });
            addSubscription(subscription);
        }

        return newStart;
    }

    private synchronized void preparePlayer(PlayInfo playInfo, String mid, String sid, String fid,
            String downUrl, String mType) {
        Logger.d("preparePlayer");
        // if is quit
        if (isQuit()) {
            return;
        }

        int seekToPosition = mSeekWhenPrepared;
        if (seekToPosition != 0) {
            seekTo(seekToPosition);
        }

        String pid = playInfo.getCurrentProduct().getPid();
        String playType = playInfo.getCurrentProduct().getPtype();
        // prepare playing
        mVooleMediaPlayer.prepare(mid, sid, fid, pid, playType, downUrl, mType);
    }

    private void start() {
        Logger.d("start");
        if (mVooleMediaPlayer != null) {
            mVooleMediaPlayer.start();
            fireOnPlayerStart();
        }
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

    @Override
    public boolean pausePlayer() {
        Logger.d("pausePlayer");
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

    private void pause() {
        Logger.d("pause");
        mVooleMediaPlayer.pause();
        fireOnPlayerPaused();
    }

    @Override
    public synchronized boolean stopPlayer() {
        Logger.d("stopPlayer, player state : " + getPlayerState());
        setQuit(true);
        clearSubscription();
        setTargetState(PlayerState.STATE_STOPPED);
        mVoole.release();
//        if (canStop() && isInPlaybackState())
        {
            fireOnPlayerStopping();
            // reset buffered percent;
            setBufferedPercent(0);
            try {
                mVooleMediaPlayer.stop();
                mVooleMediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            fireOnPlayerStopped();
            return true;
        }
//        return false;
    }

    @Override
    public int getDuration() {
        if (mVooleMediaPlayer != null && isInPlaybackState()) {
            int duration = mVooleMediaPlayer.getDuration();
            mDuration = duration;
            return duration;
        } else if (mDuration > 0) {
            return mDuration;
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mVooleMediaPlayer != null && isInPlaybackState()) {
            int currentPosition = mVooleMediaPlayer.getCurrentPosition();
            mCurPos = currentPosition;
            return currentPosition;
        } else if (mCurPos > 0) {
            return mCurPos;
        }
        return 0;
    }

    @Override
    public int getBufferPercentage() {
        return getBufferedPercent();
    }

    @Override
    public int getFakeDuration() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return mVooleMediaPlayer != null;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            setSeekDuration(getCurrentPosition());
            Logger.d("seekTo, msec : " + msec);
            mVooleMediaPlayer.seekTo(msec);
            fireOnPlayerSeek(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean canSeekBackward() {
        return mCanSeekBackward;
    }

    @Override
    public boolean canSeekForward() {
        return mCanSeekForward;
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
    public boolean changeRankRestart(String rank) {
        int oldRank = getRank();
        boolean restart = super.changeRankRestart(rank);
        if (restart) {
//            mSeekWhenPrepared = 0;
            fireOnPlayerRankChanged(restart, oldRank, getRank());
            stopPlayer();
            startPlayer();
            fireOnPlayerStart();
        }
        return restart;
    }

    @Override
    public boolean isBufferedSupport() {
        // support
        return true;
    }

    @Override
    public boolean isMediaIpSupport() {
        return true;
    }

    private synchronized int getBufferedPercent() {
        return mBufferedPercent;
    }

    private synchronized void setBufferedPercent(int bufferedPercent) {
        mBufferedPercent = bufferedPercent;
    }

    /*======================== call back from Voole ========================*/
    @Override
    public void onPrepared(boolean isPreview, int previewTime, String s, String s1) {
        Logger.d("onPrepared, isPreview : " + isPreview + ", previewTime : " + previewTime);
        // if is quit
        if (isQuit()) {
            // stop player
            stopPlayer();
            return;
        }

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
    public boolean onInfo(int what, int extra) {
        Logger.d("onInfo, what : " + what + ", extra : " + extra);
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
    public boolean onError(int what, int extra, String errCode, String errCodeExtra,
            String errMsgExtra) {
        Logger.e("onError, what : " + what + ", extra : " + extra + ", errCode : " + errCode
                + ", errCodeExtra : " + errCodeExtra + ", errMsgExtra : " + errMsgExtra);
        fireOnPlayerError(what, extra, errMsgExtra);
        return true;
    }

    @Override
    public void onCompletion() {
        Logger.d("onCompletion");
        fireOnPlayerCompleted();
    }

    @Override
    public void onSeekComplete() {
        Logger.d("onSeekComplete");
    }

    @Override
    public void onBufferingUpdate(int percent) {
//        Logger.d("onBufferingUpdate, percent : " + percent);
        setBufferedPercent(percent);
    }

    @Override
    public void canSeek(boolean canSeek) {
        Logger.d("canSeek, canSeek : " + canSeek);
        setCanSeek(canSeek);
    }

    @Override
    public void canExit(boolean canExit) {
        Logger.d("canExit, canExit : " + canExit);

    }

    @Override
    public void onSeek(int pos) {
        Logger.d("onSeek, pos : " + pos);
        seekTo(pos);
        fireOnPlayerSeek(pos);
    }

    @Override
    public void onExit() {
        Logger.d("onExit");
        setCanSeek(true);
    }

    @Override
    public void onAdEvent(AdEvent adEvent) {
        AdEvent.AdStatus adStatus = adEvent.getAdStatus();
        Logger.d("onAdEvent, ad type : " + adEvent.getAdType() + ", ad status : "
                + adStatus);
        if (AdEvent.AdStatus.AD_END == adStatus) {
            setCanSeek(true);
        }
    }

    @Override
    public void onMovieStart() {
        Logger.d("onMovieStart");
    }
    /*======================== call back from Voole ========================*/

    private void setCanSeek(boolean canSeek) {
        mCanSeekBackward = canSeek;
        mCanSeekForward = canSeek;
    }

    private synchronized void addSubscription(Subscription subscription) {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.add(subscription);
        }
    }

    private synchronized void clearSubscription() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.clear();
        }
    }

    private synchronized boolean isQuit() {
        return mIsQuit;
    }

    private synchronized void setQuit(boolean quit) {
        mIsQuit = quit;
    }

}
