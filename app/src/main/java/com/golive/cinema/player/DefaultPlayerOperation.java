package com.golive.cinema.player;

import com.golive.cinema.MyApplication;
import com.golive.cinema.util.StringUtils;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.log.Logger;

import java.util.List;

/**
 * Default Player Operation which maintain the player's state and provide common business functions
 * about player.
 * <p/>
 * Created by Wangzj on 2016/9/12.
 */

public abstract class DefaultPlayerOperation implements PlayerOperation {

    /**
     * 播放器回调
     */
    private PlayerCallback mCallback;

    /**
     * 播放鉴权回调
     */
    private PlayerValidateCallback mPlayerValidateCallback;

    /**
     * 播放地址
     */
    private List<String> mMediaUrls;

    /**
     * 播放地址分辨率
     */
    private List<String> mRanks;

    /**
     * 字幕ID
     */
    private int mSubtitle;

    /**
     * 音轨ID
     */
    private int mSoundTrack;

    /**
     * 清晰度ID
     */
    private int mSharpness = 0;

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private int mPlayerState = PlayerState.STATE_IDLE;
    private int mTargetState = PlayerState.STATE_IDLE;

    /**
     * 播放资源名称
     */
    private String mMediaName;

    /**
     * 当前播放的清晰度标识
     */
    private int mRank = 0;

    /**
     * 资源ID
     */
    private String mFilmId;

    /**
     * 资源回调IP
     *
     * @return
     */
    private String mMediaIp;

    /**
     * toType
     *
     * @return
     */
    private String mToType;

    /**
     * mSeekDuration用户快进时的进度点
     *
     * @return
     */
    private long mSeekDuration;

    /**
     * isPauseClick
     *
     * @return
     */
    private boolean isPauseClick = false;

    @Override
    public synchronized boolean isPlaying() {
        return PlayerState.STATE_PLAYING == getPlayerState();
    }

    @Override
    public synchronized boolean isInPlaybackState() {
        return PlayerState.STATE_IDLE != getPlayerState()
                && PlayerState.STATE_PREPARING != getPlayerState()
                && PlayerState.STATE_STOPPING != getPlayerState()
                && PlayerState.STATE_STOPPED != getPlayerState()
                && PlayerState.STATE_ERROR != getPlayerState()
                && PlayerState.STATE_PLAYBACK_COMPLETED != getPlayerState();
    }

    public synchronized boolean canStop() {
        return PlayerState.STATE_STOPPING != getPlayerState()
                && PlayerState.STATE_STOPPED != getPlayerState();
    }

    @Override
    public List<String> getMediaUrls() {
        return mMediaUrls;
    }

    @Override
    public void setMediaUrls(List<String> mediaUrls) {
        mMediaUrls = mediaUrls;
    }

    @Override
    public List<String> getRanks() {
        return mRanks;
    }

    @Override
    public void setRanks(List<String> ranks) {
        mRanks = ranks;
    }

    @Override
    public String getPlayUrl() {
        int rank = getRank();
        if (rank > 0 && mRanks != null && !mRanks.isEmpty()) {
            for (int i = 0; i < mRanks.size(); i++) {
                if (!StringUtils.isNullOrEmpty(mRanks.get(i))
                        && rank == Integer.parseInt(mRanks.get(i))) {
                    if (mMediaUrls != null && !mMediaUrls.isEmpty()) {
                        return mMediaUrls.get(i);
                    }
                }
            }
            //默认取排序后第一个
            if (StringUtils.isNullOrEmpty(mRanks.get(0))) {
                setRank(0);
            } else {
                setRank(Integer.parseInt(mRanks.get(0)));
            }
            return mMediaUrls.get(0);
        }

        return null;
    }

    @Override
    public String getMediaName() {
        return mMediaName;
    }

    @Override
    public void setMediaName(String mediaName) {
        this.mMediaName = mediaName;
    }

    @Override
    public int getRank() {
        if (mRank <= 0) {
            mRank = UserInfoHelper.getDefaultDefinition(MyApplication.getContext());
            return mRank;
        }
        return mRank;
    }

    @Override
    public void setRank(int rank) {
        mRank = rank;
    }

    @Override
    public int getSubtitle() {
        return mSubtitle;
    }

    @Override
    public int getSoundTrack() {
        return mSoundTrack;
    }

    @Override
    public int getSharpness() {
        return mSharpness;
    }

    @Override
    public void setSubtitle(int index, boolean effect) {
        if (mSubtitle == index) {
            return;
        }
        mSubtitle = index;
    }

    @Override
    public void setSoundTrack(int index, boolean effect) {
        if (mSoundTrack == index) {
            return;
        }
        mSoundTrack = index;
    }

    @Override
    public void setSharpness(int index, boolean effect) {
        if (mSharpness == index) {
            return;
        }
        mSharpness = index;
    }

    @Override
    public int getSharpnessCount() {

        int count = 0;

        //if (!StringUtils.isNullOrEmpty(getUrl())) {
        //    count = 1;
        //}

        if (getMediaUrls() != null && getMediaUrls().size() > 0) {
            count = getMediaUrls().size();
        }

        return count;
    }

    @Override
    public int getSubtitleCount() {
        return 0;
    }

    @Override
    public int getSoundTrackCount() {
        return 0;
    }

    @Override
    public synchronized int getPlayerState() {
        return mPlayerState;
    }

    @Override
    public synchronized void setPlayerState(int playerState) {
        mPlayerState = playerState;
        // sync the target state with current state!
        setTargetState(playerState);
    }

    public synchronized int getTargetState() {
        return mTargetState;
    }

    public synchronized void setTargetState(int targetState) {
        mTargetState = targetState;
    }

    @Override
    public String getFilmId() {
        return mFilmId;
    }

    @Override
    public void setFilmId(String filmId) {
        mFilmId = filmId;
    }

    @Override
    public String getMediaIp() {
        return mMediaIp;
    }

    @Override
    public void setMediaIp(String mediaIp) {
        mMediaIp = mediaIp;
    }

    @Override
    public String getToType() {
        return mToType;
    }

    @Override
    public void setToType(String toType) {
        mToType = toType;
    }

    @Override
    public long getSeekDuration() {
        return mSeekDuration;
    }

    @Override
    public void setSeekDuration(long seekDuration) {
        mSeekDuration = seekDuration;
    }

    @Override
    public boolean isPauseClick() {
        return isPauseClick;
    }

    @Override
    public void setPauseClick(boolean pauseClick) {
        isPauseClick = pauseClick;
    }

    @Override
    public boolean changeRankRestart(String rank) {
        String rankSave = String.valueOf(getRank());
        if (rank != null && rank.equals(rankSave)) {
            return false;
        }

        List<String> rankList = getRanks();
        if (rankList != null && rankList.size() > 0) {
            for (int i = 0; i < rankList.size(); i++) {
                String r = rankList.get(i);
                if (rank != null && r != null && rank.equals(r)) {
                    setRank(Integer.parseInt(rank));
                    setSharpness(i, false);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public PlayerCallback getCallback() {
        return mCallback;
    }

    @Override
    public void setPlayerCallback(PlayerCallback callback) {
        mCallback = callback;
    }

    protected void fireOnPlayerPreparing() {
        Logger.d("fireOnPlayerPreparing");
        setPlayerState(PlayerState.STATE_PREPARING);
        if (mCallback != null) {
            mCallback.onPlayerPreparing();
        }
    }

    protected void fireOnPlayerPrepared() {
        Logger.d("fireOnPlayerPrepared");
        setPlayerState(PlayerState.STATE_PREPARED);
        if (mCallback != null) {
            mCallback.onPlayerPrepared();
        }
    }

    protected void fireOnPlayerStart() {
        Logger.d("fireOnPlayerStart");
        setPlayerState(PlayerState.STATE_PLAYING);
        if (mCallback != null) {
            mCallback.onPlayerStart();
        }
    }

    protected void fireOnPlayerPaused() {
        Logger.d("fireOnPlayerPaused");
        setPlayerState(PlayerState.STATE_PAUSED);
        if (mCallback != null) {
            mCallback.onPlayerPaused();
        }
    }

    protected void fireOnPlayerResumed() {
        Logger.d("fireOnPlayerResumed");
        setPlayerState(PlayerState.STATE_PLAYING);
        if (mCallback != null) {
            mCallback.onPlayerResumed();
        }
    }

    protected void fireOnPlayerStopping() {
        Logger.d("fireOnPlayerStopping");
        setPlayerState(PlayerState.STATE_STOPPING);
        if (mCallback != null) {
            mCallback.onPlayerStopping();
        }
    }

    protected void fireOnPlayerStopped() {
        Logger.d("fireOnPlayerStopped");
        setPlayerState(PlayerState.STATE_STOPPED);
        if (mCallback != null) {
            mCallback.onPlayerStopped();
        }
    }

    protected void fireOnPlayerCompleted() {
        Logger.d("fireOnPlayerCompleted");
        setPlayerState(PlayerState.STATE_PLAYBACK_COMPLETED);
        if (mCallback != null) {
            mCallback.onPlayerCompleted();
        }
    }

    protected void fireOnPlayerBuffering(boolean hasProgress, int progress, String speed) {
        Logger.d("fireOnPlayerBuffering, hasProgress : " + hasProgress + ", progress : " + progress
                + ", speed : " + speed);
//        setPlayerState(progress < 100 ? PlayerState.STATE_BUFFERING : PlayerState
// .STATE_BUFFEREND);
        if (mCallback != null) {
            mCallback.onPlayerBuffering(hasProgress, progress, speed);
        }
    }

    protected void fireOnBufferingUpdate(int percent) {
        if (mCallback != null) {
            mCallback.onBufferingUpdate(percent);
        }
    }

    protected void fireOnPlayerError(int err, int extra, String errMsg) {
        Logger.d("fireOnPlayerError, err : " + err + ", extra : " + extra
                + ", errMsg : " + errMsg);
        setPlayerState(PlayerState.STATE_ERROR);
        if (mCallback != null) {
            mCallback.onPlayerError(err, extra, errMsg);
        }
    }

    protected void fireOnPlayerBusyChange(boolean busy) {
//        Logger.d("fireOnPlayerBusyChange, busy : " + busy);
        if (mCallback != null) {
            mCallback.onPlayerBusyChange(busy);
        }
    }

    protected void fireOnPlayerRankChanged(boolean isRankChange, int rank, int toRank) {
        Logger.d("fireOnPlayerRankChanged, isRankChange : " + isRankChange + ", rank : " + rank
                + ", toRank : " + toRank);
        if (mCallback != null) {
            mCallback.onPlayerRankChanged(isRankChange, rank, toRank);
        }
    }

    protected void fireOnPlayerSeek(int msec) {
        Logger.d("fireOnPlayerSeek, msec : " + msec);
        if (mCallback != null) {
            mCallback.onPlayerSeekTo(msec);
        }
    }

    public PlayerValidateCallback getPlayerValidateCallback() {
        return mPlayerValidateCallback;
    }

    public void setPlayerValidateCallback(
            PlayerValidateCallback playerValidateCallback) {
        mPlayerValidateCallback = playerValidateCallback;
    }
}
