package com.golive.cinema.advert;


import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.View;

import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.player.views.PlayerBusyingView;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.ToastUtils;
import com.golive.cinema.views.AdvertVideoView;
import com.golive.cinema.views.CircleLayoutView;
import com.initialjie.log.Logger;

/**
 * Created by chgang on 2016/12/28.
 */

public class AdvertMediaDialog extends AdvertDialog implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnInfoListener,
        SurfaceHolder.Callback {

    private static final String ADVERT_QUALITY_VALUE_DEFAULT = "1";

    private int mTimeoutCount = 3;
    private long mVideoLoadDuration;
    private long mVideoLoadBlockedDuration;
    private long mFilmPlayProgress;
    private final Handler mHandler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mFilmPlayProgress = arguments.getLong(Constants.PLAYER_INTENT_PLAY_PROGRESS) / 1000;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentView(R.layout.play_advert_media_dialog);
        mPlayerBusyingView = (PlayerBusyingView) view.findViewById(R.id.advert_media_busying);
        mPlayerBusyingView.sendMessage(true, 0, null, false);
        mCircleLayoutView = (CircleLayoutView) view.findViewById(
                R.id.play_advert_media_count_down_view);
        mCircleLayoutView.setOnFinishCallback(this);
        mCircleLayoutView.setVisibility(View.GONE);
        mVideoView = (AdvertVideoView) view.findViewById(R.id.player_advert_video_view);
        mVideoView.getHolder().addCallback(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setOnErrorListener(this);
        mVideoView.setOnPreparedListener(this);
    }

    @Override
    protected void hide() {
        mTimeoutCount = 3;
        mHandler.removeCallbacksAndMessages(null);
        stopPlayer();
        super.hide();
    }

    private void startPlayer() {
        if (mAdvert != null && mAdvert.getUrl() != null && mVideoView != null) {
            Logger.d("startPlayer, " + mAdvert.getUrl());
            mVideoLoadDuration = System.currentTimeMillis();
            mVideoView.setVideoPath(mAdvert.getUrl().trim());
        } else {
            hide();
        }
    }

    private void stopPlayer() {
        Logger.d("stopPlayer");
        if (mCircleLayoutView != null) {
            mCircleLayoutView.pauseCircle();
        }
        try {
            if (mVideoView != null) {
                mVideoView.stopPlayback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Logger.d("onCompletion");
        setFinish(true);
        stopPlayer();
        hide();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Logger.e("onError, what : " + what + ", extra : " + extra);
        hide();
        reportPlayAdException(what, "");
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Logger.d("onPrepared");
        mp.setOnInfoListener(this);
        mHandler.removeCallbacksAndMessages(null);
        mVideoView.start();
        if (mPlayerBusyingView != null) {
            mPlayerBusyingView.sendMessage(false, 0, null, false);
        }
        startCircleView();
        reportPlayAdLoad();
    }

    @Override
    public void onFinish() {
        hide();
        reportPlayAdExit();
        super.onFinish();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logger.d("surfaceCreated");
        mVideoLoadBlockedDuration = System.currentTimeMillis();
        startPlayer();
        reportPlayAdStartStatistics();
        mHandler.post(mTimeoutRunnable);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.d("surfaceDestroyed");
        stopPlayer();
        if (mPlayerBusyingView != null) {
            mPlayerBusyingView.stopSpeedProgressText();
//            mPlayerBusyingView = null;
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
        Logger.d("onInfo, what:" + what);
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:// buffer start
                if (mPlayerBusyingView != null) {
                    mPlayerBusyingView.sendMessage(true, 0, null, false);
                }
                if (mCircleLayoutView != null) {
                    mCircleLayoutView.pauseCircle();
                }
                return true;
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:// buffer end
                if (mPlayerBusyingView != null) {
                    mPlayerBusyingView.sendMessage(false, 0, null, false);
                }
                resumeCircleView();
                reportPlayAdBlocked();
                return true;
            default:
                break;
        }
        return false;
    }

    private final Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (--mTimeoutCount >= 0) {
                mHandler.postDelayed(this, 1000);
            } else {
                if (mVideoView == null || !mVideoView.isPlaying()) {
                    String errorText = "";
                    if (isAdded() && getContext() != null) {
                        errorText = getString(R.string.play_advert_loading_error_text);
                        ToastUtils.showToast(getContext(), errorText);
                    }
                    reportPlayAdException(MediaPlayer.MEDIA_INFO_UNKNOWN, errorText);
                    hide();
                }
            }
        }
    };

    private void reportPlayAdStartStatistics() {
        if (mAdvert == null || mAdType == Constants.AD_REQUEST_TYPE_BOOT) {
            return;
        }

        StatisticsHelper.getInstance(getContext()).reportPlayAdStart(
                mAdvert.getId(),
                mAdvert.getAdTitle(),
                mAdvert.getAdvertType(),
                mAdvert.getUrl().trim(),
                ADVERT_QUALITY_VALUE_DEFAULT,
                mAdvert.getTimeType(),
                mAdvert.getAdvertiser(),
                mAdvert.getAdverName(),
                mFilmId,
                mFilmName,
                mFilmPlayProgress
        );
    }

    private void reportPlayAdLoad() {
        if (mAdvert == null || mAdType == Constants.AD_REQUEST_TYPE_BOOT) {
            return;
        }

        String bufferDuration = String.valueOf(
                (System.currentTimeMillis() - mVideoLoadDuration) / 1000);
        StatisticsHelper.getInstance(getContext()).reportPlayAdLoad(
                mAdvert.getId(),
                mAdvert.getAdTitle(),
                mAdvert.getAdvertType(),
                mAdvert.getUrl().trim(),
                "",
                ADVERT_QUALITY_VALUE_DEFAULT,
                mAdvert.getTimeType(),
                bufferDuration,
                mAdvert.getAdvertiser(),
                mAdvert.getAdverName(),
                mFilmId,
                mFilmName,
                mFilmPlayProgress
        );
    }

    private void reportPlayAdBlocked() {
        if (mAdvert == null || mVideoView == null
                || mAdType == Constants.AD_REQUEST_TYPE_BOOT) {
            return;

        }
        String bufferDuration = String.valueOf(
                (System.currentTimeMillis() - mVideoLoadBlockedDuration) / 1000);
        StatisticsHelper.getInstance(getContext()).reportPlayAdBlocked(
                mAdvert.getId(),
                mAdvert.getAdTitle(),
                mAdvert.getAdvertType(),
                mAdvert.getUrl().trim(),
                "",
                ADVERT_QUALITY_VALUE_DEFAULT,
                mAdvert.getTimeType(),
                String.valueOf(mVideoView.getDuration()),
                String.valueOf(mVideoView.getCurrentPosition()),
                bufferDuration,
                mAdvert.getAdvertiser(),
                mAdvert.getAdverName(),
                mFilmId,
                mFilmName,
                mFilmPlayProgress
        );
    }

    private void reportPlayAdException(int errCode, String errMsg) {
        if (mAdvert == null || mAdType == Constants.AD_REQUEST_TYPE_BOOT) {
            return;
        }

        StatisticsHelper.getInstance(getContext()).reportPlayAdException(
                mAdvert.getId(),
                mAdvert.getAdTitle(),
                mAdvert.getAdvertType(),
                mAdvert.getUrl().trim(),
                ADVERT_QUALITY_VALUE_DEFAULT,
                mAdvert.getTimeType(),
                String.valueOf(errCode),
                errMsg,
                mAdvert.getAdvertiser(),
                mAdvert.getAdverName(),
                mFilmId,
                mFilmName,
                mFilmPlayProgress
        );
    }

    private void reportPlayAdExit() {
        if (mAdvert == null || mAdType == Constants.AD_REQUEST_TYPE_BOOT) {
            return;
        }

        String bufferDuration = String.valueOf(
                (System.currentTimeMillis() - mVideoLoadDuration) / 1000);
        StatisticsHelper.getInstance(getContext()).reportPlayAdExit(
                mAdvert.getId(),
                mAdvert.getAdTitle(),
                mAdvert.getAdvertType(),
                mAdvert.getUrl().trim(),
                ADVERT_QUALITY_VALUE_DEFAULT,
                mAdvert.getTimeType(),
                String.valueOf(mVideoView.getDuration()),
                String.valueOf(mVideoView.getCurrentPosition()),
                bufferDuration,
                mAdvert.getAdvertiser(),
                mAdvert.getAdverName(),
                mFilmId,
                mFilmName,
                mFilmPlayProgress
        );
    }
}
