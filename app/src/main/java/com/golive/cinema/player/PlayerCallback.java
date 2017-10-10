package com.golive.cinema.player;

/**
 * Created by Wangzj on 2016/9/12.
 */

public interface PlayerCallback {

    void onPlayerPreparing();

    void onPlayerPrepared();

    void onPlayerStart();

    void onPlayerPaused();

    void onPlayerResumed();

    void onPlayerSeekTo(int msec);

    void onPlayerStopping();

    void onPlayerStopped();

    void onPlayerCompleted();

    /**
     * 当正在缓冲
     *
     * @param hasProgress 是否包含缓冲进度
     * @param progress    缓冲进度，只有当hasProgress为true时，才有效。当progress >= 100时，代表已经缓冲完毕
     */
    void onPlayerBuffering(boolean hasProgress, int progress, String speed);

    /**
     * Called to update status in buffering a media stream received through
     * progressive HTTP download. The received buffering percentage
     * indicates how much of the content has been buffered or played.
     * For example a buffering update of 80 percent when half the content
     * has already been played indicates that the next 30 percent of the
     * content to play has been buffered.
     *
     * @param percent the percentage (0-100) of the content
     *                that has been buffered or played thus far
     */
    void onBufferingUpdate(int percent);

    /**
     * 当发生错误
     *
     * @param err    错误编码
     * @param extra  附带的错误编码
     * @param errMsg 错误信息
     */
    void onPlayerError(int err, int extra, String errMsg);

    /**
     * Called when the player's busy change.
     */
    void onPlayerBusyChange(boolean busy);

    /**
     * @param isRankChange
     */
    void onPlayerRankChanged(boolean isRankChange, int rank, int toRank);
}
