package com.golive.cinema.player.kdm;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.Formatter;

import com.golive.cinema.player.DefaultPlayerOperation;
import com.golive.cinema.player.PlayerState;
import com.golive.cinema.player.PlayerValidateCallback;
import com.golive.cinema.util.StringUtils;
import com.golive.player.kdm.KDMDeviceID;
import com.golive.player.kdm.KDMMetadata;
import com.golive.player.kdm.KDMPlayer;
import com.golive.player.kdm.KDMResCode;
import com.golive.player.kdm.KDMStatus;
import com.initialjie.log.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Wangzj on 2016/9/21.
 */

public class KdmOperationImpl extends DefaultPlayerOperation {
    private static final boolean HANDLE_SEEK = true;

    /**
     * KDM player
     */
    private final KDMPlayer mKdmPlayer;

    /**
     * play type
     */
    private final int mPlayType;

    /**
     * play from break point
     */
    private boolean mBreakpoint;

    private KDMDeviceID.CompanyType mCompanyType = KDMDeviceID.CompanyType.OTHER;

    private final Context mContext;
    private boolean mPlayerIdle = true;
    private int mDuration;
    private int mCurPos;
    private int mSeekWhenPrepared = -1;
    private boolean mIsQuit;
    private boolean mIsBuffering;
    /** 之前play命令在队列中， 且现在还没有发配对的stop命令的次数 */
    private final AtomicInteger mPlayRemainRef = new AtomicInteger();
    private final CompositeSubscription mCompositeSubscription;

    public KdmOperationImpl(@NonNull Context context, int mPlayType, boolean mBreakpoint,
            KDMDeviceID.CompanyType mCompanyType) {
        mContext = checkNotNull(context.getApplicationContext(), "Context cannot be null!");
        this.mPlayType = mPlayType;
        this.mBreakpoint = mBreakpoint;
        this.mCompanyType = mCompanyType;

        mKdmPlayer = createKDMPlayer(context);
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public synchronized boolean startPlayer() {
        Logger.d("startPlayer, player state : " + getPlayerState());
        setQuit(false);
        boolean newStart = false;
        if (isInPlaybackState()) { // 已经准备
            // 暂停
            if (PlayerState.STATE_PAUSED == getPlayerState()) {
                // 恢复播放
                pausePlayer();
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
            setPlayerIdle(false);

            final boolean breakPoint = mBreakpoint && 0 != mSeekWhenPrepared;

            // 下次播放时，一定是断点播放
            mBreakpoint = true;
            // reset
            mSeekWhenPrepared = -1;

            final String url = getPlayUrl();

            /*
            * 1. Get KDM medias
            * 2. Get KDM's uuid from step2 and play
            * */
//            String regUrl = "http://www.cloudmovie.net.cn:9090";
            final KDM kdm = new KDM(mContext, mCompanyType);
            // step1
            final Subscription subscription = kdm.getKdmMediaList(url)
                    .subscribe(new Subscriber<List<KDMMetadata>>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.e(e, "startPlayer, onError : ");
                            int errno = -1;
                            int extra = -1;
                            if (e instanceof KdmException) {
                                KDMResCode resCode = ((KdmException) e).getKdmResCode();
                                if (resCode != null
                                        && KDMResCode.RESCODE_ERROR == resCode.getResult()) {
                                    errno = resCode.getErrno();
                                }
                            }
                            setPlayerIdle(true);
                            onPlayerError(errno, extra, null);
                        }

                        @Override
                        public void onNext(List<KDMMetadata> metadatas) {
                            String uuid = metadatas.get(0).getTitleUuid();
                            // if is un-subscribed
                            if (isUnsubscribed()) {
                                return;
                            }
                            _startPlayer(uuid, breakPoint);
                        }
                    });

            addSubscription(subscription);
        }
        return newStart;
    }

    private synchronized void _startPlayer(String uuid, boolean breakPoint) {
        // if is quit
        if (isQuit()) {
            return;
        }

        // should stop kdm
        if (shouldStopKdm()) {
            // stop kdm first
            Logger.w("_startPlayer, stop kdm first");
            stopPlayer();
        }

        // play kdm
        int remainRef = getAndAddPlayRemainRef();
        Logger.d("_startPlayer, remainRef : " + remainRef);
        int retVal = getKdmPlayer().playKdm(mCompanyType, uuid, breakPoint, mPlayType);
        if (retVal != 0) {
            setPlayerIdle(true);
            onPlayerError(retVal, retVal, null);
        }
    }

    @Override
    public synchronized boolean resumePlayer() {
        Logger.d("resumePlayer");
        setTargetState(PlayerState.STATE_PLAYING);

        if (PlayerState.STATE_PREPARED == getPlayerState()
                || PlayerState.STATE_PAUSED == getPlayerState()) {
//            fireOnPlayerResumed();
            return pausePlayer();
        }
        return false;
    }

    @Override
    public synchronized boolean pausePlayer() {
        Logger.d("pausePlayer");
        setTargetState(PlayerState.STATE_PAUSED);

        if (PlayerState.STATE_PREPARED == getPlayerState()
                || PlayerState.STATE_PLAYING == getPlayerState()
                || PlayerState.STATE_PAUSED == getPlayerState()) {
            if (!isPlayerIdle()) {
                return false;
            }
            setPlayerIdle(false);
            int retVal = getKdmPlayer().pauseKdm();
            if (retVal != 0) {
                setPlayerIdle(true);
                onPlayerError(retVal, retVal, null);
            }
            return 0 == retVal;
        }
        return false;
    }

    @Override
    public boolean isPlaying() {
        return super.isPlaying() || PlayerState.STATE_PLAYING == getPlayerState();
    }

    @Override
    public synchronized boolean stopPlayer() {
        Logger.d("stopPlayer, player state : " + getPlayerState());
        setQuit(true);
        clearSubscription();
        setTargetState(PlayerState.STATE_STOPPED);
        if (isKdmPlaying() || shouldStopKdm() || canStop() && isInPlaybackState()) {
            setPlayerIdle(false);
            fireOnPlayerStopping();
            if (shouldStopKdm()) {
                int retVal = stopKdm();
                if (retVal != 0) {
                    setPlayerIdle(true);
                    onPlayerError(retVal, retVal, null);
                }
                return 0 == retVal;
            } else {
                setPlayerIdle(true);
                onPlayerStopped();
            }
        } else {
            fireOnPlayerStopping();
            onPlayerStopped();
        }
        return false;
    }

    private int stopKdm() {
        int remainRef = getAndDecrementPlayRemainRef();
        Logger.d("stopPlayer, remainRef : " + remainRef);
        // stop kdm
        return getKdmPlayer().stopKdm();
    }

    @Override
    public int getDuration() {
        if (mDuration > 0) {
            return mDuration;
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mCurPos > 0) {
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
        return getKdmPlayer() != null;
    }

    @Override
    public void seekTo(int msec) {
        Logger.d("seekTo, msec : " + msec);
        if (isInPlaybackState()) {
            setSeekDuration(getCurrentPosition());
            fireOnPlayerSeek(msec);
            mSeekWhenPrepared = msec;

            if (HANDLE_SEEK) {
//                int currLength = getCurrentPosition();
//                Logger.d("seekTo, current pos : " + currLength + ", seek pos : " +
// mSeekWhenPrepared);
//
//                //  current pos > 0 && 0 == seek pos
//                if (currLength > 0 && 0 == mSeekWhenPrepared) {
//                    Logger.d("seekTo, current pos > 0 && 0 == seek pos");
//                    // no break point!
//                    mBreakpoint = false;
//                    // restart player
//                    restartPlayer();
//                }
            }
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
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
    public boolean isBufferedSupport() {
        return true;
    }

    @Override
    public boolean isMediaIpSupport() {
        return true;
    }

    /**
     * Create the KDM player.
     */
    private KDMPlayer createKDMPlayer(@NonNull Context context) {

        checkNotNull(context);

        return new KDMPlayer(context) {

            @Override
            public void initKdmCallback(KDMResCode resCode) {
                super.initKdmCallback(resCode);
                setPlayerIdle(true);
                if (resCode != null) {
                    if (KDMResCode.RESCODE_ERROR == resCode.getResult()) {
                        onKdmPlayerError(resCode);
                    }
                }
            }

            @Override
            public void playCallback(KDMResCode resCode, KDMStatus mStatus) {
                super.playCallback(resCode, mStatus);
                setPlayerIdle(true);
                if (resCode != null) {
                    if (KDMResCode.RESCODE_OK == resCode.getResult()) {
                        onPlayCallback(resCode, mStatus);
                    } else if (KDMResCode.RESCODE_ERROR == resCode.getResult()) {
                        onKdmPlayerError(resCode);
                    }
                }
            }

            @Override
            public void pauseCallback(KDMResCode resCode) {
                super.pauseCallback(resCode);
                setPlayerIdle(true);
                if (resCode != null) {
                    if (KDMResCode.RESCODE_OK == resCode.getResult()) {
                        onPauseCallback(resCode);
                    } else if (KDMResCode.RESCODE_ERROR == resCode.getResult()) {
                        onKdmPlayerError(resCode);
                    }
                }
            }

            @Override
            public void stopCallback(KDMResCode resCode) {
                super.stopCallback(resCode);
                setPlayerIdle(true);
                if (resCode != null) {
                    if (KDMResCode.RESCODE_OK == resCode.getResult()) {
                        onStopCallback(resCode);
                    } else if (KDMResCode.RESCODE_ERROR == resCode.getResult()) {
                        onKdmPlayerError(resCode);
                    }
                }
            }
        };
    }

    private void onPlayCallback(@NonNull KDMResCode resCode, KDMStatus mStatus) {
        checkNotNull(resCode);
        switch (resCode.getResult()) {
            case KDMResCode.RESCODE_OK:
                if (null == mStatus) {
                    break;
                }

                KDMStatus.StatusType kdmInternalStatus = mStatus.getState();
                switch (kdmInternalStatus) {
                    case PLAYING: // internal state is "playing" in KDM
                        int currLength = Integer.parseInt(mStatus.getCurrent());
                        int totalLength = Integer.parseInt(mStatus.getDuration());
                        // ms
                        currLength *= 1000;
                        totalLength *= 1000;
                        updatePlayProgress(totalLength, currLength);

                        int targetState = getTargetState();

                        // current is preparing
                        if (PlayerState.STATE_PREPARING == getPlayerState()) {
                            fireOnPlayerPrepared();
//                            fireOnPlayerStart();
                        }

                        switch (targetState) {
                            case PlayerState.STATE_PLAYING:  // target to play
//                                startPlayer();
                                // do nothing, because current state is "playing" in KDM
                                break;
                            case PlayerState.STATE_PAUSED:  // target to pause
                                // current is not paused
                                if (PlayerState.STATE_PAUSED != getPlayerState()) {
                                    pausePlayer();
                                }
                                break;
                            default:
                                break;
                        }
                        break;
                    case PAUSING:
                        break;
                    case FREE:
                        fireOnPlayerCompleted();
                        break;
                    default:
                        break;
                }

                // 不是已经释放的
                if (KDMStatus.StatusType.FREE != kdmInternalStatus) {
                    if (mStatus.isBuffer()) { // 正在缓冲
                        String remoteServer = mStatus.getRemote();
                        String speed = mStatus.getSpeed();
                        String speedStr = "";
                        if (!StringUtils.isNullOrEmpty(speed)) {
                            // remove KB/s
                            speed = speed.replace("KB/s", "");
                            if (!StringUtils.isNullOrEmpty(speed)) {
                                try {
                                    long v = (long) (Double.parseDouble(speed) * 1000.0);
                                    // format speed
                                    speedStr = Formatter.formatFileSize(mContext, v) + "/s";
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        setMediaIp(remoteServer);
                        fireOnPlayerBuffering(false, 0, speedStr);
                        setBuffering(true);
                    } else if (KDMStatus.StatusType.PLAYING == kdmInternalStatus) { // 不是缓冲 && 正在播放
                        // 之前是缓冲状态
                        if (isBuffering()) {
                            setBuffering(false);
                            fireOnPlayerBuffering(true, 100, null);
                        }
                        fireOnPlayerStart();
                    }
                }
                break;

            case KDMResCode.RESCODE_ERROR:
                onKdmPlayerError(resCode);
                break;

            default:
                break;
        }

        // if is quit
        if (isQuit()) {
            // stop player
            stopPlayer();
        }
    }

    private void onPauseCallback(@NonNull KDMResCode resCode) {
        checkNotNull(resCode);
        int targetState = getTargetState();

        // last status is prepared or playing
        if (PlayerState.STATE_PREPARED == getPlayerState()
                || PlayerState.STATE_PLAYING == getPlayerState()) {
            fireOnPlayerPaused();
            if (PlayerState.STATE_PLAYING == targetState) { // target to playing
                resumePlayer();
            }
        } else if (PlayerState.STATE_PAUSED == getPlayerState()) { // last status is paused
            fireOnPlayerStart();
            fireOnPlayerResumed();
            if (PlayerState.STATE_PAUSED == targetState) { // target to pause
                pausePlayer();
            }
        }

    }

    private void onStopCallback(@NonNull KDMResCode resCode) {
        checkNotNull(resCode);
        onPlayerStopped();
    }

    /**
     * Called when KDM player error.
     */
    private void onKdmPlayerError(@NonNull KDMResCode resCode) {
        checkNotNull(resCode);
        int errno = resCode.getErrno();
        String errDesc = "";
        if (resCode.play != null && !StringUtils.isNullOrEmpty(resCode.play.getDesc())) {
            errDesc = resCode.play.getDesc();
        }
        Logger.e("onKdmPlayerError, errno : " + errno + ", desc : " + errDesc);
        onPlayerError(errno, -1, errDesc);
    }

    /**
     * Update the player progress.
     */
    private void updatePlayProgress(int duration, int curPos) {
        mDuration = duration;
        mCurPos = curPos;
    }

    private synchronized void setPlayerIdle(boolean playerIdle) {
        if (mPlayerIdle != playerIdle) {
            mPlayerIdle = playerIdle;
            fireOnPlayerBusyChange(!playerIdle);
        }
    }

    private synchronized boolean isPlayerIdle() {
        return mPlayerIdle;
    }

    private KDMPlayer getKdmPlayer() {
        return mKdmPlayer;
    }

    private int getPlayType() {
        return mPlayType;
    }

    private boolean isBreakpoint() {
        return mBreakpoint;
    }

    private KDMDeviceID.CompanyType getCompanyType() {
        return mCompanyType;
    }

    @Override
    public boolean changeRankRestart(String rank) {
        return false;
    }

    /**
     * KDM是否处于播放状态
     */
    private synchronized boolean isKdmPlaying() {
        boolean isPlaying = PlayerState.STATE_PREPARING == getPlayerState()
                || PlayerState.STATE_PREPARED == getPlayerState()
                || PlayerState.STATE_BUFFERING == getPlayerState()
                || PlayerState.STATE_PLAYING == getPlayerState()
                || PlayerState.STATE_PAUSED == getPlayerState();
        Logger.d("isKdmPlaying : " + isPlaying + ", getPlayerState() : " + getPlayerState());
        return isPlaying;
    }

    /**
     * Should Stop Kdm?
     */
    private synchronized boolean shouldStopKdm() {
        // 之前play命令在队列中， 且现在还没有发配对的stop命令
        int playRemainRef = getPlayRemainRef();
        boolean b = playRemainRef > 0;
        Logger.d("shouldStopKdm : " + b + ", playRemainRef : " + playRemainRef);
        return b;
    }

    private void onPlayerError(int errCode, int extra, String errMsg) {
//        String errDesc = ResourcesUtils.getErrorDescription(mContext,
//                String.valueOf(100000 + errCode));
//        if (StringUtils.isNullOrEmpty(errMsg)) {
//            errMsg = errDesc;
//        } else {
//            if (!StringUtils.isNullOrEmpty(errDesc)) {
//                errMsg = errDesc + ", " + errMsg;
//            }
//        }

        // 当前没有播放
        if (KDMResCode.KDM_STATE_NO_PLAYING == errCode) {
            // 忽略该错误
            onPlayerStopped();
            return;
        }

        fireOnPlayerError(errCode, extra, errMsg);
    }

    private void onPlayerStopped() {
        fireOnPlayerStopped();
    }

    /**
     * restart player
     */
    private void restartPlayer() {
        Logger.d("restartPlayer");
        // stop player
        stopPlayer();
        // start player
        startPlayer();
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

    private synchronized int getPlayRemainRef() {
        return mPlayRemainRef.get();
    }

    private synchronized int getAndAddPlayRemainRef() {
        return mPlayRemainRef.getAndIncrement();
    }

    private synchronized int getAndDecrementPlayRemainRef() {
        return mPlayRemainRef.getAndDecrement();
    }

    private synchronized boolean isQuit() {
        return mIsQuit;
    }

    private synchronized void setQuit(boolean quit) {
        mIsQuit = quit;
    }

    private boolean isBuffering() {
        return mIsBuffering;
    }

    private void setBuffering(boolean buffering) {
        mIsBuffering = buffering;
    }
}

