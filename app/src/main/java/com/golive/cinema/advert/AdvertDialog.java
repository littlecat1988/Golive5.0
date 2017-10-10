package com.golive.cinema.advert;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.player.dialog.RecommendAlertDialog;
import com.golive.cinema.player.views.PlayerBusyingView;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.DateHelper;
import com.golive.cinema.util.FragmentUtils;
import com.golive.cinema.util.NetworkUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.cinema.views.AdvertVideoView;
import com.golive.cinema.views.CircleLayoutView;
import com.golive.network.entity.Ad;
import com.golive.network.entity.MovieRecommendFilm;
import com.golive.network.helper.DeviceHelper;
import com.initialjie.hw.util.DeviceUtil;
import com.initialjie.log.Logger;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by chgang on 2016/12/30.
 */

public abstract class AdvertDialog extends BaseDialog
        implements CircleLayoutView.OnFinishCallback, AdvertRecommendContract.View {

    private static final int CIRCLE_TIME_DEFAULT = 15;

    private AdvertRecommendContract.Presenter mPresenter;
    private boolean mOnKeyBack;
    private boolean mIsExit;
    private boolean mIsFinish;

    protected Ad mAdvert;
    protected String mFilmId;
    protected String mFilmName;
    protected int mAdType;

    protected PlayerBusyingView mPlayerBusyingView;
    protected View mLoadingView;
    protected ImageView mAdvertImageView;
    protected ImageView mAdvertImagePauseView;
    protected AdvertVideoView mVideoView;
    protected CircleLayoutView mCircleLayoutView;

    private AdvertCallback mAdvertCallback;
    private List<MovieRecommendFilm> mMovieRecommendFilms;

    public interface AdvertCallback {
        void onExit(boolean isExit, boolean isFinish);
    }

    public void setOnAdvertCallback(AdvertCallback advertCallback) {
        this.mAdvertCallback = advertCallback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mAdType = arguments.getInt(Constants.PLAYER_INTENT_BOOT_ADVERT,
                    Constants.AD_REQUEST_TYPE_BOOT);
            mFilmId = arguments.getString(Constants.PLAYER_INTENT_FILM_ID);
            mFilmName = arguments.getString(Constants.PLAYER_INTENT_NAME);
            mAdvert = (Ad) arguments.getSerializable(Constants.PLAYER_INTENT_MEDIA_ADVERT);
//            Logger.d("mAdvert:" + mAdvert.toString());
        }
//        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.dialog_fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.advert_dialog, container, false);
        mLoadingView = view.findViewById(R.id.advert_loading_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundResource(android.R.color.transparent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getDialog() != null) {
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                        KeyEvent event) {
                    if (KeyEvent.ACTION_DOWN == event.getAction() && 0 == event.getRepeatCount()
                            && (KeyEvent.KEYCODE_BACK == keyCode
                            || KeyEvent.KEYCODE_ESCAPE == keyCode)) {
                        Logger.d("keyCode:" + keyCode);
                        if (mCircleLayoutView != null) {
                            mCircleLayoutView.pauseCircle();
                        }

                        try {
                            // is playing
                            if (mVideoView != null && mVideoView.isPlaying()) {
                                // pause player
                                mVideoView.pause();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (Constants.AD_REQUEST_TYPE_PLAYER == mAdType && mPresenter != null
                                && !mOnKeyBack && NetworkUtils.isNetworkAvailable(getContext())) {
                            mOnKeyBack = true;
                            if (null == mMovieRecommendFilms || mMovieRecommendFilms.isEmpty()) {
                                mPresenter.loadRecommendFilmList(mFilmId);
                            } else {
                                showRecommendMovieList(mMovieRecommendFilms);
                            }
                        } else {
                            setExit(true);
                            hide();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

        //presenter
        Context context = getContext().getApplicationContext();
        AdvertRecommendPresenter mAdvertRecommendPresenter =
                new AdvertRecommendPresenter(this,
                        Injection.provideGetUserInfoUseCase(context),
                        Injection.provideGetMovieRecommendUseCase(context),
                        Injection.provideReportAdvertMiaozhenUseCase(context));

        if (NetworkUtils.isNetworkAvailable(context) && mAdvert != null) {
            String adUrl = mAdvert.getReportUrl();
            if (!StringUtils.isNullOrEmpty(adUrl)) {
                String mac = DeviceUtil.getMacAddress(context);
                String reportUrl = StringUtils.getReportUrl(adUrl, mac);
                Logger.d("reportUrl:" + reportUrl);
                StatisticsHelper.getInstance(getContext()).reportThirdPartyAd(reportUrl);

                //report miaozhen advert
                if (mPresenter != null) {
                    mPresenter.reportAdvertMiaozhen(mFilmId, mAdvert.getId(), adUrl,
                            mAdvert.getAdvertiser(), mac,
                            DeviceHelper.getScreenWAndScreenH(context));
                }
            }

            //曝光
            if (mAdvert.isThirdAdvert()) {
                String pkgName = getActivity().getPackageName();
                String activityName = getActivity().getClass().getName();
                String showTime = DateHelper.dateFormatToString(new Date(),
                        DateHelper.DATE_FORMAT2);
                StatisticsHelper.getInstance(getContext()).reportAdExposure(mAdType,
                        mAdvert.getAdCode(), mAdvert.getMaterialCode(), showTime, pkgName,
                        activityName);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        if (mPresenter != null) {
            mPresenter.unsubscribe();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mAdvertCallback != null) {
            mAdvertCallback.onExit(isExit(), isFinish());
        }
    }

    /**
     * Set content view by layout id
     *
     * @param layoutResID layout id
     */
    public void setContentView(int layoutResID) {
        if (null == getView()) {
            return;
        }

        ViewGroup contentParent = (ViewGroup) getView().findViewById(R.id.advert_content);
        if (contentParent != null) {
            contentParent.removeAllViews();
            LayoutInflater.from(getContext()).inflate(layoutResID, contentParent);
        }
    }

    @Override
    public void onFinish() {
        setFinish(true);
        hide();
    }

    @Override
    public void setLoadingRecommendIndicator(boolean active) {
        if (null == mLoadingView) {
            return;
        }

        UIHelper.setViewVisibleOrGone(mLoadingView, active);
        Drawable background = mLoadingView.getBackground();
        if (background != null && background instanceof AnimationDrawable) {
            AnimationDrawable speedLoadingViewDrawable = (AnimationDrawable) background;
            if (active) {
                speedLoadingViewDrawable.start();
            } else {
                speedLoadingViewDrawable.stop();
            }
        }
    }

    @Override
    public void showRecommendMovieList(List<MovieRecommendFilm> recommendFilmList) {
        mMovieRecommendFilms = recommendFilmList;
        if (recommendFilmList != null && !recommendFilmList.isEmpty() && mOnKeyBack) {
            final RecommendAlertDialog mRecommendAlertDialog = FragmentUtils.newFragment(
                    RecommendAlertDialog.class);
            Bundle bundle = new Bundle();
            final String fragmentTag = RecommendAlertDialog.FRAGMENT_TAG;
            if (mAdType == Constants.AD_REQUEST_TYPE_BOOT) {
                bundle.putString(Constants.PLAYER_INTENT_NAME,
                        getString(R.string.user_exchange_advert));
            } else {
                bundle.putSerializable(fragmentTag,
                        (Serializable) recommendFilmList);
                bundle.putString(Constants.PLAYER_INTENT_NAME, mFilmName);
            }
            mRecommendAlertDialog.setArguments(bundle);
            mRecommendAlertDialog.setDismissListener(new RecommendAlertDialog.OnDismissListener() {

                @Override
                public void onDialogDismiss(boolean exit) {
                    if (exit) {
                        mCircleLayoutView = null;
                        setExit(true);
                        hide();
                    } else {
                        try {
                            // is not playing
                            if (mVideoView != null && !mVideoView.isPlaying()) {
                                // start player
                                mVideoView.start();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        resumeCircleView();
                    }
                }
            });
            FragmentUtils.removePreviousFragment(getFragmentManager(), fragmentTag);
            mRecommendAlertDialog.show(getFragmentManager(), fragmentTag);
        } else {
            mCircleLayoutView = null;
            setExit(true);
            hide();
        }
        mOnKeyBack = false;
    }

    @Override
    public void setPresenter(AdvertRecommendContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    protected void hide() {
//        Logger.d("hide");
        release();
        if (isAdded()) {
            dismiss();
        }
    }

    protected void startCircleView() {
        if (mCircleLayoutView != null) {
            int duration = -1;
            if (mVideoView != null) {
                duration = mVideoView.getDuration() / 1000;
            }

            int total = 0;
            if (mAdvert != null) {
                int promptInt = CIRCLE_TIME_DEFAULT;
                String prompt = mAdvert.getDuration();
                if (!StringUtils.isNullOrEmpty(prompt)) {
                    try {
                        promptInt = Integer.parseInt(prompt);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }

                promptInt = Math.max(CIRCLE_TIME_DEFAULT, promptInt);
                if ((promptInt > 0 && promptInt < duration) || duration < 0) {
                    total = promptInt;
                } else {
                    total = duration;
                }
            }

            mCircleLayoutView.startCircle(total);
            mCircleLayoutView.setVisibility(View.VISIBLE);
        }
    }

    protected void resumeCircleView() {
        if (null == mCircleLayoutView || !mCircleLayoutView.isPaused()) {
            return;
        }
        int duration;
        if (mVideoView != null) {
            duration = (mVideoView.getDuration() - mVideoView.getCurrentPosition()) / 1000;
        } else {
            duration = mCircleLayoutView.getTimeText();
        }

        Logger.d("startCircleView, duration:" + duration);
        mCircleLayoutView.resumeCircle(duration);
    }

    private void release() {
        if (mAdvertImageView != null) {
            mAdvertImageView.setVisibility(View.GONE);
//            mAdvertImageView = null;
        }

        if (mAdvertImagePauseView != null) {
            mAdvertImagePauseView.setVisibility(View.GONE);
//            mAdvertImagePauseView = null;
        }

        if (mCircleLayoutView != null) {
            mCircleLayoutView.circleClear();
//            mCircleLayoutView = null;
        }

        if (mPlayerBusyingView != null) {
            mPlayerBusyingView.stopSpeedProgressText();
//            mPlayerBusyingView = null;
        }
    }

    public boolean isExit() {
        return mIsExit;
    }

    public void setExit(boolean exit) {
        mIsExit = exit;
    }

    private boolean isFinish() {
        return mIsFinish;
    }

    protected void setFinish(boolean finish) {
        mIsFinish = finish;
    }
}
