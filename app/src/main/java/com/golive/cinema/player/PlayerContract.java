package com.golive.cinema.player;

import android.support.annotation.NonNull;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.Ad;
import com.golive.network.entity.MovieRecommendFilm;

import java.util.List;

import rx.Observable;

/**
 * Created by Wangzj on 2016/9/12.
 */

public class PlayerContract {

    public interface View extends IBaseView<Presenter> {

        void setPlayingIndicator(boolean active, int rank, boolean force);

        boolean isReadyToPlay();

        Observable<Boolean> waitForPlayer(boolean isKdm, int rank);

        /**
         * Show the player is busy.
         */
        void showPlayerBusy(boolean busy);

        /**
         * Update the pause/play button UI.
         */
        void updatePausePlayUI();

        /**
         * Update the player progress.
         */
        void updatePlayerProgress();

        /**
         * Show the player has been completed.
         */
        void showPlayerCompleted();

        /**
         * Show the player is buffering.
         *
         * @param hasProgress Whether the current buffer process has progress.
         * @param progress    Buffer percent in 0-100%. Only make sense when hasProgress is
         *                    <code>true<code/>.
         */
        void showPlayerBuffering(boolean hasProgress, int progress, String speed,
                boolean isBufferedSupport);

        /**
         * Update the buffering percent.
         *
         * @param percent the percentage (0-100) of the content
         *                that has been buffered or played thus far
         */
        void updateBufferingPercent(int percent);

        /**
         * @param err
         * @param extra
         * @param errMsg
         */
        void showPlayerError(int err, int extra, String errMsg);

        void setLoadingAdvertIndicator(boolean active);

        void setLoadingRecommendIndicator(boolean active);

        void showRecommendMovieList(List<MovieRecommendFilm> recommendFilmList);

        void showPlayerDuration(long duration);

        void showPlayingCurrentPosition(int position);

        void showPlayerSourceChanged(boolean isSourceChange);

        void showPlayingRankChanged(int rank);

        /**
         * Show advert
         *
         * @param ad       advert
         * @param position position
         */
        Observable<Boolean> showAdvert(Ad ad, long position);

        void hidePauseAdvert();

        void setWaterMarkIndicator(boolean active);

        void setWaterMark(String showText, int showTime, int intervalTime);

        long getSavePlayPosition();

        String getUserId();

        String getMacAddress();

        boolean isRetrySupport();

        void showRetryPlayView(int retryTimes);

        /************ player Statistics *****************/
        void reportVideoLoadStatistics(String trailer, String watchType, String rank,
                String videoUrl, String mediaIp, String serial, String free);

        void reportVideoStartBlockStatistics();

        void reportVideoBlockStatistics(String trailer, String watchType, String rank,
                String mediaIp, long playDuration, long totalDuration, long playProgress,
                String serial, String free);

        void reportVideoStartStatistics(String trailer, String watchType, String rank,
                String source, String serial, String free);

        void reportVideoStreamChangeStatistics(String trailer, String watchType, String rank,
                String toRank, long playDuration, long totalDuration, long playProgress,
                String serial, String free);

        void reportVideoSeekStatistics(String trailer, String watchType, String rank,
                long playProgress, long seekToPosition, String toType, String serial, String free);

        void reportVideoPlayPauseStatistics();

        void reportVideoPlayPauseResumeStatistics(String trailer, String watchType, String rank,
                long playDuration, long totalDuration, long playProgress, String serial,
                String free);

        void reportVideoExitStatistics(String trailer, String watchType, String rank,
                long playDuration, long totalDuration, long playProgress, String serial,
                String free);

        void reportVideoExceptionStatistics(String trailer, String watchType, String rank,
                String errCode, String errMsg, long playDuration, long totalDuration,
                long playProgress, String serial, String free);
    }

    public interface Presenter extends IBasePresenter<View> {

        /**
         * Get the player operator.
         *
         * @return player operator
         */
        PlayerOperation getPlayerOperation();

        /**
         * Bind a player operator to this Presenter.
         *
         * @param playerOperation A player operator.
         */
        void setPlayerOperation(@NonNull PlayerOperation playerOperation);

        /**
         * Start the player.
         */
        void startPlayer();

        /**
         * Stop the player.
         */
        void stopPlayer();

        /**
         * pause player
         */
        void pausePlayer();

        /**
         * resume player
         */
        void resumePlayer();

        /**
         * Auto start or resume player
         */
        void startOrResumePlayer();

        /**
         * seek to position
         *
         * @param msec position
         */
        void seekTo(long msec);

        /**
         * player is playing
         */
        boolean isPlaying();

        /**
         * exit and stop player
         */
        void exit();

        /**
         * recommend movie source
         */
        void loadRecommendFilmList(String filmId);

        void addToHistory(final String orderSerial);

        void setAdvertList(List<Ad> advertList);
    }
}
