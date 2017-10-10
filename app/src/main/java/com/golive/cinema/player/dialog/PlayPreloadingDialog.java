package com.golive.cinema.player.dialog;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.graphics.Palette;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.util.NetworkUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.log.Logger;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by chgang on 2016/11/30.
 */

public class PlayPreloadingDialog extends BaseDialog implements View.OnKeyListener {

    private static final int CHECK_NET_SPEED_TIME = 500;
    private static final long LOADING_DELAY_TIME = 3000L;

    private AsyncTask<Bitmap, Void, Palette> mPaletteTask;

    private String mFilmId;
    private String mFilmName;
    private String mFilmPoster;
    private int mFilmRank;
    private boolean mWaitForRestart;
    private boolean mIsTrailer;
    private int[] mColorBg = null;
    private long mSeekPosition;

    private RelativeLayout mLoadingLayout;
    private TextView mSpeedView;
    private TextView mReplayingView;
    private ImageView mClarityView;
    private TextView mRestartTipsView;
    private ImageView mRestartLeftView;

    private long mOldBytes;
    private long mTimeStamp;
    private long mCreateTime;

    /** restart play */
    private boolean mRestartPlay;
    private final Handler mHandler = new Handler();
    private ScheduledExecutorService mUpdateNetWorkTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mFilmId = arguments.getString(Constants.PLAYER_INTENT_FILM_ID);
            mFilmName = arguments.getString(Constants.PLAYER_INTENT_NAME);
            mFilmPoster = arguments.getString(Constants.PLAYER_INTENT_FILM_ID_POSTER);
            mIsTrailer = arguments.getBoolean(Constants.PLAYER_INTENT_FILM_MEDIA_TRAILER,
                    false);
            mColorBg = arguments.getIntArray(Constants.PLAYER_INTENT_FILM_ID_POSTER_COLOR);
            mFilmRank = arguments.getInt(Constants.PLAYER_INTENT_FILM_RANK, 0);
            mWaitForRestart = arguments.getBoolean(Constants.PLAYER_INTENT_WAIT_FOR, false);
        }
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.play_preloading_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //bg
        mLoadingLayout = (RelativeLayout) view.findViewById(R.id.play_pre_loading_layout);

        //name
        TextView nameView = (TextView) view.findViewById(R.id.play_pre_loading_name);
        if (!StringUtils.isNullOrEmpty(mFilmName)) {
            nameView.setText(mFilmName);
        }

        //clarity
        mClarityView = (ImageView) view.findViewById(R.id.play_pre_loading_clarity);

        //speed
        mSpeedView = (TextView) view.findViewById(R.id.play_pre_loading_network_speed);
        mReplayingView = (TextView) view.findViewById(R.id.play_pre_loading_replaying);

        //bar
        ImageView barView = (ImageView) view.findViewById(R.id.play_pre_loading_network_speed_bar);
        AnimationDrawable speedLoadingViewDrawable = (AnimationDrawable) barView.getDrawable();
        speedLoadingViewDrawable.start();

        //tips
        mRestartTipsView = (TextView) view.findViewById(R.id.play_pre_loading_restart_tips);
        mRestartLeftView = (ImageView) view.findViewById(R.id.play_pre_loading_left_seek);
        mRestartLeftView.setClickable(true);
        mRestartLeftView.setFocusable(true);
        mRestartLeftView.setFocusableInTouchMode(true);
        mRestartLeftView.setOnKeyListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mCreateTime = System.currentTimeMillis();
        setClarity();
        setBG();
        setReplayingText();
        getSpeedProgressText();

        if (mWaitForRestart) {
            mHandler.postDelayed(mRestartRunnable,
                    mSeekPosition > 0 ? LOADING_DELAY_TIME : 0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        release();
    }

    private void setReplayingText() {
        if (!mIsTrailer) {
            mSeekPosition = UserInfoHelper.getUserPlayCurrentPosition(getContext(), mFilmId);
        }
        if (mSeekPosition > 0) {
//            StringBuilder mFormatBuilder = new StringBuilder();
//            java.util.Formatter mFormatter = new java.util.Formatter(mFormatBuilder,
//                    Locale.getDefault());
//            mFormatBuilder.setLength(0);
            int totalSeconds = (int) (mSeekPosition / 1000);
            int seconds = totalSeconds % 60;
            int minutes = (totalSeconds / 60) % 60;
            int hours = totalSeconds / 3600;
            String time = String.format(Locale.getDefault()
                    , "%02d:%02d:%02d", hours, minutes, seconds);
            mReplayingView.setText(
                    String.format(getString(R.string.play_pre_replaying_loading_tips), time));
            mRestartLeftView.setVisibility(View.VISIBLE);
            mRestartTipsView.setVisibility(View.VISIBLE);
            mRestartLeftView.requestFocus();
        } else {
            mReplayingView.setText(getString(R.string.play_pre_loading_tips));
        }
    }

    private void setClarity() {
        String clarity = String.valueOf(mFilmRank);
        switch (clarity) {
            case Constants.PLAY_MEDIA_RANK_CLARITY_SUPER:
                mClarityView.setImageResource(R.drawable.play_clarity_super_bg);
                break;
            case Constants.PLAY_MEDIA_RANK_CLARITY_STANDARD:
                mClarityView.setImageResource(R.drawable.play_clarity_standard_bg);
                break;
            case Constants.PLAY_MEDIA_RANK_CLARITY_HIGH:
                mClarityView.setImageResource(R.drawable.play_clarity_high_bg);
                break;
        }
    }

    private void setBG() {
        if (mColorBg != null) {
            GradientDrawable gd = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM, mColorBg);
            gd.setCornerRadius(0f);
            UIHelper.setBackground(mLoadingLayout, gd);
        } else {
            if (!StringUtils.isNullOrEmpty(mFilmPoster)) {
                Glide.with(this)
                        .load(mFilmPoster)
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource,
                                    GlideAnimation<? super Bitmap> glideAnimation) {
                                if (resource != null) {
                                    doDynamicUpdateBackground(resource);
                                }
                            }
                        });
            }
        }
    }

    private void getSpeedProgressText() {
        if (mUpdateNetWorkTask == null || mUpdateNetWorkTask.isShutdown()) {
            mUpdateNetWorkTask = Executors.newSingleThreadScheduledExecutor();
            mUpdateNetWorkTask.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    if (!isAdded()) {
                        return;
                    }
                    FragmentActivity activity = getActivity();
                    // 获取网速
                    long speed = checkSpeed();
                    if (speed < 0) {
                        speed = 0;
                    }
                    final StringBuilder sb = new StringBuilder();
                    sb.setLength(0);
                    sb.append(Formatter.formatFileSize(activity, speed));
                    sb.append("/S");
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSpeedView.setText(sb.toString());
                        }
                    });
                }
            }, 200, CHECK_NET_SPEED_TIME, TimeUnit.MILLISECONDS);
        }
    }

    private void doDynamicUpdateBackground(@NonNull Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        Palette.PaletteAsyncListener listener = new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {

                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
                Palette.Swatch mutedSwatch = palette.getMutedSwatch();
//                Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
                Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();
                Logger.d("Palette.generateAsync, getMutedSwatch : " + mutedSwatch
                        + ", getDarkMutedSwatch : " + darkMutedSwatch + ", getVibrantSwatch : "
                        + vibrantSwatch + ", darkVibrantSwatch : " + darkVibrantSwatch);

                Palette.Swatch swatch = mutedSwatch;
                Palette.Swatch darkSwatch = darkMutedSwatch;

                if (null == swatch || null == darkSwatch) {
                    swatch = vibrantSwatch;
                    darkSwatch = darkVibrantSwatch;
                }

                if (swatch != null && darkSwatch != null) {
                    int[] colors = new int[]{darkSwatch.getRgb(), swatch.getRgb()};
                    GradientDrawable gd = new GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM, colors);
                    gd.setCornerRadius(0f);

                    UIHelper.setBackground(mLoadingLayout, gd);
                }
            }
        };

        if (mPaletteTask != null) {
            mPaletteTask.cancel(true);
        }

        mPaletteTask = new Palette.Builder(bitmap).maximumColorCount(24).generate(listener);
    }

    public void release() {
        mHandler.removeCallbacksAndMessages(null);
        mRestartPlay = false;
        if (mUpdateNetWorkTask != null) {
            mUpdateNetWorkTask.shutdown();
            mUpdateNetWorkTask = null;
        }
        if (mPaletteTask != null) {
            mPaletteTask.cancel(true);
            mPaletteTask = null;
        }
    }

    private long checkSpeed() {
        long netSpeed = 0;
        long now = System.currentTimeMillis();
        long currBytesDown = 0;

        long newRxBytes = NetworkUtils.getTotalDataBytes(true);
        long newBytes = newRxBytes - mOldBytes;
        mOldBytes = newRxBytes;

        if (0 != mTimeStamp) {
            if (currBytesDown == TrafficStats.UNSUPPORTED) {
                Logger.d("TrafficStats is not supported!");
            } else {
                long byteSpan;
                // byteSpan = currBytesDown - bytesDown;
                byteSpan = newBytes;
                long timeSpan = now - mTimeStamp;
                if (timeSpan != 0) {
                    netSpeed = (long) Math.ceil(1000.0 * byteSpan / timeSpan);
                }
            }
        }

        mTimeStamp = now;
        return netSpeed;
    }

    public void delayCloseLoading() {
        if (StringUtils.isNullOrEmpty(mFilmId) || mIsTrailer) {
            fireOnResultDismiss(false);
            return;
        }

        mRestartLeftView.requestFocus();
        long curTime = System.currentTimeMillis() - mCreateTime;
        if (isAdded()) {
            if (curTime > LOADING_DELAY_TIME) {
                mHandler.post(mTipRunnable);
            } else {
                mHandler.postDelayed(mTipRunnable, LOADING_DELAY_TIME - curTime);
            }
        }
    }

    private final Runnable mTipRunnable = new Runnable() {
        @Override
        public void run() {
            mRestartLeftView.setVisibility(View.GONE);
            mRestartTipsView.setVisibility(View.GONE);
            if (mRestartPlay) {
                mSeekPosition = 0;
            }
            fireOnResultDismiss(false);
        }
    };

    private final Runnable mRestartRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPreloadingDismissCallback != null) {
                mPreloadingDismissCallback.onSelectRestart(mSeekPosition, mRestartPlay);
            }
        }
    };

    private void dismissAll() {
        release();
        if (isResumed()) {
            dismiss();
        }
    }

    private PreloadingDismissCallback mPreloadingDismissCallback = null;

    public interface PreloadingDismissCallback {
        void onSelectRestart(long position, boolean restart);
    }

    public void setPreloadingDismissCallback(PreloadingDismissCallback callback) {
        mPreloadingDismissCallback = callback;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    mRestartPlay = true;
                    mReplayingView.setText(getString(R.string.play_pre_loading_tips));
                    mReplayingView.setTextColor(
                            getResources().getColor(R.color.play_pre_loading_replaying_text_color));
                    mHandler.removeCallbacks(mRestartRunnable);
                    mHandler.post(mRestartRunnable);

                    if (mPreloadingDismissCallback != null) {
                        mPreloadingDismissCallback.onSelectRestart(mSeekPosition, mRestartPlay);
                    }
                    break;
                case KeyEvent.KEYCODE_BACK:
                case KeyEvent.KEYCODE_ESCAPE:
                    mHandler.removeCallbacks(mTipRunnable);
                    mRestartPlay = false;
                    release();
//                    fireOnResultDismiss(true);
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    private void fireOnResultDismiss(boolean isRestart) {
        dismissAll();
//        if (mPreloadingDismissCallback != null) {
//            mPreloadingDismissCallback.onSelectRestart(isRestart, mSeekPosition, false);
//        }
    }
}
