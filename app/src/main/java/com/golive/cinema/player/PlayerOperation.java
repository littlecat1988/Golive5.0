package com.golive.cinema.player;

import java.util.List;

/**
 * Created by Wangzj on 2016/9/12.
 */

public interface PlayerOperation {

    boolean startPlayer();

    /**
     * Resume play if paused or prepared.
     */
    boolean resumePlayer();

    /**
     * Trigger pause or resume play.
     */
    boolean pausePlayer();

    boolean stopPlayer();

    int getDuration();

    int getCurrentPosition();

    int getBufferPercentage();

    /**
     * 获取设定播放总时长，单位毫秒。当{@linkplain #getDuration()}方法返回小于等于0时，调用此方法获取播放总时长。
     */
    int getFakeDuration();

    boolean canPause();

    void seekTo(int msec);

    boolean canSeekBackward();

    boolean canSeekForward();

    boolean isPlaying();

    boolean isInPlaybackState();

    /**
     * 是否是全屏
     */
    boolean isFullScreen();

    /**
     * 全屏或原始影片大小
     */
    void toggleFullScreen();

    /**
     * 是否本地播放
     *
     * @return true, 本地播放; false, 在线播放
     */
    boolean isLocalPlay();

    int getSubtitleCount();

    int getSoundTrackCount();

    int getSharpnessCount();

    int getSubtitle();

    int getSoundTrack();

    int getSharpness();

    void setSubtitle(int index, boolean effect);

    void setSoundTrack(int index, boolean effect);

    void setSharpness(int index, boolean effect);

    boolean changeRankRestart(String rank);

    void setPlayerState(int playerState);

    int getPlayerState();

    List<String> getMediaUrls();

    void setMediaUrls(List<String> mediaUrls);

    String getMediaName();

    void setMediaName(String mediaName);

    int getRank();

    void setRank(int rank);

    String getFilmId();

    void setFilmId(String filmId);

//    List<Ad> getAdvertList();
//
//    void setAdvertList(List<Ad> advertList);

    /**
     * 获取播放器回调
     */
    PlayerCallback getCallback();

    /**
     * 设置播放器回调
     */
    void setPlayerCallback(PlayerCallback callback);

    boolean isBufferedSupport();

    boolean isMediaIpSupport();

    String getMediaIp();

    void setMediaIp(String mediaIp);

    String getPlayUrl();

    String getToType();

    void setToType(String toType);

    long getSeekDuration();

    void setSeekDuration(long seekDuration);

    boolean isPauseClick();

    void setPauseClick(boolean pauseClick);

    List<String> getRanks();

    void setRanks(List<String> ranks);
}
