package com.golive.cinema.filmdetail;

import static android.text.format.Formatter.formatFileSize;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_FILM_DETAIL;
import static com.golive.cinema.util.FragmentUtils.removePreviousFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.golive.cinema.CommonAlertDialogFragment;
import com.golive.cinema.Constants;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.UniteThrowable;
import com.golive.cinema.creditpay.CreditExpireDialogFragment;
import com.golive.cinema.creditpay.CreditPayNoticeDialogFragment;
import com.golive.cinema.download.DownloadDialogFragment;
import com.golive.cinema.download.DownloadErrorFragment;
import com.golive.cinema.download.DownloadUtils;
import com.golive.cinema.download.SelectDownloadMediaAndPathFragment;
import com.golive.cinema.download.domain.model.MediaAndPath;
import com.golive.cinema.player.PlayerActivity;
import com.golive.cinema.player.domain.model.PlaybackValidity;
import com.golive.cinema.purchase.PurchaseDialogFragment;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.user.buyvip.BuyVipActivity;
import com.golive.cinema.user.pay.QrcodeContract;
import com.golive.cinema.user.pay.QrcodeFragment;
import com.golive.cinema.util.DateHelper;
import com.golive.cinema.util.ItemClickSupport;
import com.golive.cinema.util.MathExtend;
import com.golive.cinema.util.NetworkUtils;
import com.golive.cinema.util.ResourcesUtils;
import com.golive.cinema.util.StorageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.Ad;
import com.golive.network.entity.FinanceOrder;
import com.golive.network.entity.Media;
import com.golive.network.entity.MovieRecommendFilm;
import com.golive.network.entity.Order;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.download.DownloadConstants;
import com.initialjie.download.aidl.DownloadTaskInfo;
import com.initialjie.log.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Wangzj on 2016/10/9.
 */
public class FilmDetailFragment extends MvpFragment implements FilmDetailContract.View {
    private static final String SHOW_INTRODUCE_FRAG_TAG = "show_introduce_frag_tag";
    private static final String PURCHASE_FRAG_TAG = "confirm_purchase_frag_tag";
    private static final String SHOW_WATCH_NOTICE_FRAG_TAG = "show_watch_notice_frag_tag";
    private static final String SHOW_CREDIT_PAY_NOTICE_FRAG_TAG = "show_credit_pay_notice_frag_tag";
    private static final String CREDIT_EXPIRE_FRAG_TAG = "credit_expire_frag_tag";
    private static final String PLAY_ERROR_FRAG_TAG = "play_error_frag_tag";
    private static final String DOWNLOAD_FRAG_TAG = "download_frag_tag";
    private static final String PLAY_OVERDUE_FRAG_TAG = "play_overdue_frag_tag";
    private static final String DOWNLOAD_ERROR_FRAG_TAG = "download_error_frag_tag";
    private static final String SEL_DOWNLOAD_MEDIA_PATH_FRAG_TAG =
            "sel_download_media_path_frag_tag";
    private static final String DOWNLOAD_ERROR_NO_STORAGE_FRAG_TAG =
            "download_error_no_storage_frag_tag";
    private static final String DOWNLOAD_ERROR_SPACE_NOT_ENOUGH_FRAG_TAG =
            "download_error_space_not_enough_frag_tag";
    private static final int REQUEST_CODE_CONFIRM_PURCHASE = 1;
    private static final int REQUEST_CODE_PLAY = 2;

    private FilmDetailContract.Presenter mPresenter;
    private ProgressDialog mProgressDialog;
    private ProgressDialog mUpdatingViewDialog;
    private ProgressDialog mPurchasingFilmDialog;
    private ProgressDialog mPreparePlayingDialog;
    private ImageView mPosterIgv;
    private ImageView mCornerRightIgv;
    private TextView mTitleTv;
    private TextView mScoreTv;
    private TextView mCategoryTv;
    private TextView mDirectorTv;
    private TextView mActorsTv;
    private TextView mCountryTv;
    private TextView mLanguageTv;
    private TextView mDurationTv;
    private TextView mWatchTimeTv;
    //    private TextView mNormalPriceTv;
//    private TextView mVipPriceTv;
    private TextView mYearTv;
    private TextView mOnlinePriceTv;
    private TextView mDownloadPriceTv;
    private TextView mDownloadProgressTv;
    private TextView mDownloadDetailTv;
    private TextView mCornerRightTv;
    private TextView mCornerLeftTv;
    //    private ViewGroup mPlayPurchaseVg;
//    private ViewGroup mTrailerVg;
    private ViewGroup mBtnsVg;
    private ViewGroup mRecommendPosterVg;
    private ViewGroup mQrCodePayVg;
    private ViewGroup mQrCodeContainer;
    private View mBgView;
    private View mCrediaPayView;
    private View mBuyVipVg;
    private View mPlayFilmView;
    private View mPurchaseOnlineFilmView;
    private View mPurchaseDownloadFilmView;
    private View mPlayTrailerView;
    private View mDownloadProgressVg;
    private View mDownloadBtnVg;
    private View mLastFirstFocusBtn;
    private View mPriceVg;
    private View mRightView;
    private ProgressBar mDownloadProgressBar;
    private Button mDetailBtn;
    private View mDownloadBtn;
    private TextView mDownloadTv;
    private String mFilmId;
    private String mFilmName;
    private String mFilmPoster;
    private AsyncTask<Bitmap, Void, Palette> mPaletteTask;
    private RecyclerView mRecommendPosterRv;
    private RecommendPostersAdapter mRecommendPostersAdapter;
    private final StringBuilder mStringBuilder = new StringBuilder();
    private DownloadReceiver mDownloadReceiver;
    private boolean mFirstUpdateFocus = true;
    private int mLastVisibleBtnCounts;
    private int[] mColorBg;
    private int mFrom;
    private Date mEnterTime;
    private Subscription mUpdateProgressSubscription;

//    private QrcodeFragment smallQrcodeFragment;

    public static FilmDetailFragment newInstance(String filmId, int from) {
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_FILM_ID, filmId);
        arguments.putInt(Constants.EXTRA_FROM, from);
        FilmDetailFragment fragment = new FilmDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        mFilmId = arguments.getString(Constants.EXTRA_FILM_ID);
        mFrom = arguments.getInt(Constants.EXTRA_FROM);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.filmdetail_frag, container, false);
        mBgView = view.findViewById(R.id.filmdetail_bg_v);
        mPosterIgv = (ImageView) view.findViewById(R.id.filmdetail_poster_igv);
        mTitleTv = (TextView) view.findViewById(R.id.filmdetail_title_tv);
        mScoreTv = (TextView) view.findViewById(R.id.filmdetail_score_tv);
        mYearTv = (TextView) view.findViewById(R.id.filmdetail_year_tv);
        mCategoryTv = (TextView) view.findViewById(R.id.filmdetail_category_tv);
        mCornerRightIgv = (ImageView) view.findViewById(R.id.filmdetail_poster_corner_igv);
        mCornerRightTv = (TextView) view.findViewById(R.id.filmdetail_corner_right_tv);
        mCornerLeftTv = (TextView) view.findViewById(R.id.filmdetail_corner_left_tv);
//        mPlayOrPurchaseVg = (ViewGroup) view.findViewById(R.id.filmdetail_playOrPurchase_film_ll);
//        mTrailerVg = (ViewGroup) view.findViewById(R.id.filmdetail_trailer_ll);
        mBtnsVg = (ViewGroup) view.findViewById(R.id.filmdetail_btns_vg);
        mPlayFilmView = view.findViewById(R.id.filmdetail_play_film_btn);
        mPurchaseOnlineFilmView = view.findViewById(R.id.filmdetail_purchase_online_film_btn);
        mPurchaseDownloadFilmView = view.findViewById(R.id.filmdetail_purchase_download_film_btn);
        mBuyVipVg = view.findViewById(R.id.filmdetail_buy_vip_btn);
        mCrediaPayView = view.findViewById(R.id.filmdetail_credit_pay_btn);
        mPlayTrailerView = view.findViewById(R.id.filmdetail_trailer_btn);
        mRightView = view.findViewById(R.id.filmdetail_qr_code_vg);
        mQrCodePayVg = (ViewGroup) view.findViewById(R.id.filmdetail_right_vg);
        mQrCodeContainer = (ViewGroup) view.findViewById(R.id.filmdetail_qr_code);
        mRecommendPosterVg = (ViewGroup) view.findViewById(R.id.film_detail_recommend_poster_vg);
        mRecommendPosterRv = (RecyclerView) view.findViewById(
                R.id.film_detail_recommend_poster_list);
        mDirectorTv = (TextView) view.findViewById(R.id.filmdetail_director_tv);
        mActorsTv = (TextView) view.findViewById(R.id.filmdetail_actors_tv);
        mCountryTv = (TextView) view.findViewById(R.id.filmdetail_country_tv);
        mLanguageTv = (TextView) view.findViewById(R.id.filmdetail_language_tv);
        mDurationTv = (TextView) view.findViewById(R.id.filmdetail_duration_tv);
        mWatchTimeTv = (TextView) view.findViewById(R.id.filmdetail_watch_time_tv);
//        mNormalPriceTv = (TextView) view.findViewById(R.id.filmdetail_normal_price_tv);
//        mVipPriceTv = (TextView) view.findViewById(R.id.filmdetail_vip_price_tv);
        mPriceVg = view.findViewById(R.id.filmdetail_price_vg);
        mOnlinePriceTv = (TextView) view.findViewById(R.id.filmdetail_online_price_tv);
        mDownloadPriceTv = (TextView) view.findViewById(R.id.filmdetail_download_price_tv);
        mDownloadProgressVg = view.findViewById(R.id.filmdetail_download_vg);
        mDownloadProgressBar = (ProgressBar) view.findViewById(R.id.filmdetail_download_pb);
        mDownloadProgressBar.setMax(100);
        mDownloadProgressTv = (TextView) view.findViewById(R.id.filmdetail_download_progress_tv);
        mDownloadDetailTv = (TextView) view.findViewById(R.id.filmdetail_download_detail_tv);
        mDownloadBtnVg = view.findViewById(R.id.filmdetail_download_btn);
//        mDownloadBtn = (Button) mDownloadBtnVg.findViewById(R.id.btn);
//        View detailView = view.findViewById(R.id.filmdetail_more_btn);
        mDetailBtn = (Button) view.findViewById(R.id.filmdetail_more_btn);
        if (mDetailBtn != null) {
//            // change background
//            mDetailBtn.setBackgroundResource(R.drawable.selector_bg_film_detail_more_button);
//            mDetailBtn.setText(R.string.film_detail_more);
            mDetailBtn.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (KeyEvent.ACTION_DOWN == event.getAction()) {
                        // ignore left right keycode
                        if (KeyEvent.KEYCODE_DPAD_LEFT == keyCode
                                || KeyEvent.KEYCODE_DPAD_RIGHT == keyCode) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        if (mCrediaPayView != null) {
            View creditPayBtn = mCrediaPayView.findViewById(R.id.btn);
            TextView tv = (TextView) mCrediaPayView.findViewById(R.id.tv);
            if (creditPayBtn != null) {
//                creditPayBtn.setText(R.string.film_detail_credia_pay);
                creditPayBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Logger.d("Credit Pay");
                        getPresenter().creditPay();
                    }
                });
            }
            if (tv != null) {
                tv.setText(R.string.film_detail_credit_pay);
            }
        }

//        mQrCodeContainer.post(new Runnable() {
//            @Override
//            public void run() {
//                ViewGroup.LayoutParams layoutParams = mQrCodeContainer.getLayoutParams();
//                // make height equals with width
//                layoutParams.height = mQrCodeContainer.getMeasuredWidth();
//                Logger.d("mQrCodeContainer, width : " + mQrCodeContainer.getWidth()
//                        + ", getMeasuredWidth : " + mQrCodeContainer.getMeasuredWidth());
//                mQrCodeContainer.setLayoutParams(layoutParams);
//            }
//        });

        // init recommend poster view
        initRecommendPosterView();

        // set next focus down
        int id = mRecommendPosterRv.getId();
        mPlayFilmView.findViewById(R.id.btn).setNextFocusDownId(id);
        mDownloadBtnVg.findViewById(R.id.btn).setNextFocusDownId(id);
        mPurchaseOnlineFilmView.findViewById(R.id.btn).setNextFocusDownId(id);
        mPurchaseDownloadFilmView.findViewById(R.id.btn).setNextFocusDownId(id);
        mBuyVipVg.findViewById(R.id.btn).setNextFocusDownId(id);
        mCrediaPayView.findViewById(R.id.btn).setNextFocusDownId(id);
        mPlayTrailerView.findViewById(R.id.btn).setNextFocusDownId(id);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FilmDetailContract.Presenter presenter = getPresenter();
        if (presenter != null) {
            presenter.start();
//            presenter.reportEnterFilmDetail("1", "1");
        }
//        dynamicUpdateBackground(getView());
        mEnterTime = new Date();
//        String filmName = StringUtils.isNullOrEmpty(mFilmName) ? "" : mFilmName;
//        StatisticsHelper.getInstance(getContext()).reportEnterFilmDetail(mFilmId, filmName, "1",
//                "1");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
        if (mPaletteTask != null) {
            mPaletteTask.cancel(true);
        }
        if (mUpdateProgressSubscription != null) {
            mUpdateProgressSubscription.unsubscribe();
        }
        UIHelper.dismissDialog(mProgressDialog);
        UIHelper.dismissDialog(mUpdatingViewDialog);
        UIHelper.dismissDialog(mPurchasingFilmDialog);
        UIHelper.dismissDialog(mPreparePlayingDialog);

        long duration = (new Date().getTime() - mEnterTime.getTime()) / 1000;
        StatisticsHelper instance = StatisticsHelper.getInstance(
                getContext().getApplicationContext());
        instance.reportExitActivity(VIEW_CODE_FILM_DETAIL, mFilmName, "", String.valueOf(duration));
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d("onResume");
//        if (getPresenter() != null) {
//            getPresenter().start();
//        }
        registerReceiver();
    }


    @Override
    public void onPause() {
        super.onPause();
        Logger.d("onPause");
        unRegisterReceiver();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d("onActivityResult, requestCode : " + requestCode + ", resultCode : " + resultCode);
        // player return
        if (REQUEST_CODE_PLAY == requestCode && Activity.RESULT_OK == resultCode) {
            updatePlayProgress();
        }
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mProgressDialog) {
                mProgressDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.film_detail_loading_please_wait));
            }

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } else {
            if (mProgressDialog != null) {
                UIHelper.dismissDialog(mProgressDialog);
            }
        }
    }

    @Override
    public void setUpdatingViewIndicator(boolean active) {
        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mUpdatingViewDialog) {
                mUpdatingViewDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.film_detail_updating_view_please_wait));
            }

            if (!mUpdatingViewDialog.isShowing()) {
                mUpdatingViewDialog.show();
            }
        } else {
            if (mUpdatingViewDialog != null) {
                UIHelper.dismissDialog(mUpdatingViewDialog);
            }
//            updateButtons();
        }
    }

    @Override
    public void showMissingFilm() {
        if (!isAdded()) {
            return;
        }
        Toast.makeText(getContext(), getString(R.string.film_detail_missing_film),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoadFilmSuccess() {
        if (!isAdded()) {
            return;
        }
        reportEnterActivity();
    }

    @Override
    public void showLoadFilmFailed(String errMsg) {
        Logger.w("showLoadFilmFailed, errMsg : " + errMsg);
        if (!isAdded()) {
            return;
        }
        String text = getString(R.string.film_detail_load_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            text += ", " + errMsg;
        }
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
        reportEnterActivity();
    }

    @Override
    public void showUpdatingViewFailed(String errMsg) {
        Logger.w("showLoadFilmFailed, errMsg : " + errMsg);
        if (!isAdded()) {
            return;
        }
        String text = getString(R.string.film_detail_update_view_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            text += ", " + errMsg;
        }
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showError(int errorType, int errorCode, @Nullable String errorMsg) {
        Logger.d("showError, errorType : " + errorType + ", errorCode : " + errorCode
                + ", errorMsg : " + errorMsg);
        StringBuilder sb = new StringBuilder();
        String errTypeStr;
        switch (errorType) {
            case UniteThrowable.ErrorType.NETWORK_ERROR:
                errTypeStr = getString(R.string.err_no_network);
                sb.append(errTypeStr);
                break;
            case UniteThrowable.ErrorType.PARSE_ERROR:
                errTypeStr = getString(R.string.err_parse);
                sb.append(errTypeStr);
                break;
            case UniteThrowable.ErrorType.HTTP_ERROR:
                errTypeStr = getString(R.string.err_http);
                sb.append(errTypeStr);
                break;
            case UniteThrowable.ErrorType.SSL_ERROR:
                errTypeStr = getString(R.string.err_ssl);
                sb.append(errTypeStr);
                break;
            case UniteThrowable.ErrorType.KDM_ERROR: // kdm error
                errorCode += 100000;
            case UniteThrowable.ErrorType.UNKNOWN:
            default:
                errTypeStr = getString(R.string.err_unknown);
                sb.append(errTypeStr);
                String errTxt = ResourcesUtils.getErrorDescription(getContext(),
                        String.valueOf(errorCode));
                if (!StringUtils.isNullOrEmpty(errTxt)) {
                    sb.append(", ");
                    sb.append(errTxt);
                }
                if (!StringUtils.isNullOrEmpty(errorMsg)) {
                    sb.append(", ");
                    sb.append(errorMsg);
                }
                break;
        }

        Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void showTitle(String title) {
        mFilmName = title;
        setText(mTitleTv, title);
    }

    @Override
    public void showDescription(final String description) {
        UIHelper.setViewVisible(mDetailBtn, true);
        mDetailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isNullOrEmpty(description)) {
                    Toast.makeText(getContext(), R.string.film_detail_no_description,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String fragTag = SHOW_INTRODUCE_FRAG_TAG;
                removePreviousFragment(getFragmentManager(), fragTag);

                FilmIntroduceDialogFragment fragment = FilmIntroduceDialogFragment.newInstance(
                        description);
                fragment.show(getFragmentManager(), fragTag);
            }
        });
    }

    @Override
    public void showScore(String score) {
        mScoreTv.setText(score);
    }

    @Override
    public void showYear(@Nullable String year) {
        setText(mYearTv, year);
    }

    @Override
    public void showFilmPoster(@Nullable String url) {
        if (StringUtils.isNullOrEmpty(url)) {
            return;
        }

        mFilmPoster = url;
        Glide.with(this)
                .load(url)
                .asBitmap()
                .error(R.drawable.film_detail_poster_default)
                .priority(Priority.HIGH)
                .into(new BitmapImageViewTarget(mPosterIgv) {
                    @Override
                    public void onResourceReady(Bitmap resource,
                            GlideAnimation<? super Bitmap> glideAnimation) {
                        super.onResourceReady(resource, glideAnimation);
                        if (resource != null) {
                            doDynamicUpdateBackground(mBgView, resource);
                        }
                    }
                });
    }

    @Override
    public void showCategory(@Nullable String category) {
        setText(mCategoryTv, category);
    }

    @Override
    public void showDirector(@Nullable String directorName) {
        setText(mDirectorTv, directorName);
    }

    @Override
    public void showActors(@Nullable String actorsName) {
        setText(mActorsTv, actorsName);
    }

    @Override
    public void showCountry(@Nullable String country) {
        setText(mCountryTv, country);
    }

    @Override
    public void showLanguage(@Nullable String language) {
        setText(mLanguageTv, language);
    }

    @Override
    public void showDuration(@Nullable String duration) {
        if (!StringUtils.isNullOrEmpty(duration)) {
            long msTime = DateHelper.toMsTime(duration);
            long hour = msTime / 60000;
            setText(mDurationTv, hour + getString(R.string.minute));
        }
    }

    @Override
    public void showWatchTime(@Nullable String startTime, @Nullable String endTime) {
//        if (StringUtils.isNullOrEmpty(startTime)) {
//            startTime = "";
//        }
//        if (StringUtils.isNullOrEmpty(endTime)) {
//            endTime = "";
//        }
//        String text = startTime + " - " + endTime;

        String month2Day = getString(R.string.film_detail_watch_time_month2day);
        String year2month2Day = getString(R.string.film_detail_watch_time_year2month2day);

        int startY = 0;
        int startM = 0;
        int startD = 0;
        int endY = 0;
        int endM = 0;
        int endD = 0;
        String start = "";
        String end = "";
        String text = "";
        if (!StringUtils.isNullOrEmpty(startTime)) {
            startY = DateHelper.getYear(startTime);
            startM = DateHelper.getMonth(startTime);
            startD = DateHelper.getDay(startTime);
        }
        if (!StringUtils.isNullOrEmpty(endTime)) {
            endY = DateHelper.getYear(endTime);
            endM = DateHelper.getMonth(endTime);
            endD = DateHelper.getDay(endTime);
        }

        if (startM > 0 && startD > 0) {
            // year is different
            if (startY != endY) {
                start = String.format(year2month2Day, startY, startM, startD);
            } else {
                start = String.format(month2Day, startM, startD);
            }
        }
        if (endM > 0 && endD > 0) {
            // year is different
            if (startY != endY) {
                end = String.format(year2month2Day, endY, endM, endD);
            } else {
                end = String.format(month2Day, endM, endD);
            }
        }

        if (!StringUtils.isNullOrEmpty(start) || !StringUtils.isNullOrEmpty(end)) {
            text = start + " - " + end;
        }

        setText(mWatchTimeTv, text);
    }

    @Override
    public void showCornerMark(@Nullable String cornerLeftContent,
            @Nullable String cornerRightContent, @Nullable String cornerRightColor) {
        if (!StringUtils.isNullOrEmpty(cornerLeftContent)) {
            UIHelper.setViewVisibleOrGone(mCornerLeftTv, true);
            mCornerLeftTv.setText(cornerLeftContent);
        }

        if (!StringUtils.isNullOrEmpty(cornerRightContent)) {
            UIHelper.setViewVisibleOrGone(mCornerRightTv, true);
            mCornerRightTv.setText(cornerRightContent);
            // for marquee
            mCornerRightTv.setSelected(true);
            UIHelper.setViewVisibleOrGone(mCornerRightIgv, true);
            int resId = getSubscriptBgResource(cornerRightColor);
            mCornerRightIgv.setImageResource(resId);
        }
    }

    @Override
    public void showPrice(@Nullable String olNormalPriceStr, @Nullable String olVipPriceStr,
            @Nullable String dlNormalPriceStr, @Nullable String dlVipPriceStr) {

        boolean hasBothPrice = !StringUtils.isNullOrEmpty(olNormalPriceStr)
                && !StringUtils.isNullOrEmpty(dlNormalPriceStr);

        String onlinePriceTxt = "";
        String downloadPriceTxt = "";
        if (!StringUtils.isNullOrEmpty(olNormalPriceStr)) {
            onlinePriceTxt = getPriceTxt(hasBothPrice, true, olNormalPriceStr, olVipPriceStr);
        }
        if (!StringUtils.isNullOrEmpty(dlNormalPriceStr)) {
            downloadPriceTxt = getPriceTxt(hasBothPrice, false, dlNormalPriceStr, dlVipPriceStr);
        }

        String txt = "";
        if (hasBothPrice) {
            txt = onlinePriceTxt + "  " + downloadPriceTxt;
        } else if (!StringUtils.isNullOrEmpty(olNormalPriceStr)) {  // has online price
            txt = onlinePriceTxt;
        } else if (!StringUtils.isNullOrEmpty(dlNormalPriceStr)) {  // has download price
            txt = downloadPriceTxt;
        } else {
        }
        mOnlinePriceTv.setText(txt);
        UIHelper.setViewVisible(mPriceVg, true);
        UIHelper.setViewVisible(mOnlinePriceTv, true);

//        BigDecimal olVipPrice = null;
//        BigDecimal dlVipPrice = null;
//        if (!StringUtils.isNullOrEmpty(olVipPriceStr)) {
//            try {
//                olVipPrice = new BigDecimal(olVipPriceStr);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if (!StringUtils.isNullOrEmpty(dlVipPriceStr)) {
//            try {
//                dlVipPrice = new BigDecimal(dlVipPriceStr);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        // no vip price
//        boolean hasPrice = olVipPrice != null || dlVipPrice != null;
//        UIHelper.setViewVisibleOrGone(mCornerRightIgv, hasPrice);
//        UIHelper.setViewVisibleOrGone(mCornerRightTv, hasPrice);
//        if (hasPrice) {
//            boolean isVipPriceFree = false;
//            if (olVipPrice != null && 0 == BigDecimal.ZERO.compareTo(olVipPrice)
//                    || dlVipPrice != null && 0 == BigDecimal.ZERO.compareTo(dlVipPrice)) {
//                isVipPriceFree = true;
//            }
//            mCornerRightTv.setText(isVipPriceFree ? R.string.film_detail_corner_vip_free
//                    : R.string.film_detail_corner_vip_charge);
//            mCornerRightIgv.setImageResource(
//                    isVipPriceFree ? R.drawable.subscript_color5 : R.drawable.subscript_color2);
//        }
    }

    private String getPriceTxt(boolean hasBothPrice, boolean isOnline,
            @Nullable String normalPriceStr, @Nullable String vipPriceStr) {
        double price = 0;
        double vipPrice = -1;

        if (!StringUtils.isNullOrEmpty(normalPriceStr)) {
            try {
                price = Double.parseDouble(normalPriceStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!StringUtils.isNullOrEmpty(vipPriceStr)) {
            try {
                vipPrice = Double.parseDouble(vipPriceStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        boolean isNormalPriceFree = 0 == Double.compare(0, price);
        boolean isVipPriceFree = 0 == Double.compare(0, vipPrice);

        String rmbTxt = getString(R.string.RMB);
        String onlinePriceTag = getString(R.string.online_price);
        String downloadPriceTag = getString(R.string.download_price);
        String normalPriceTag = getString(R.string.price);
        String normalPriceText = isNormalPriceFree ? "0" : normalPriceStr;

        if (hasBothPrice) {
            normalPriceTag = isOnline ? onlinePriceTag : downloadPriceTag;
        }

        String text = normalPriceTag + " : " + normalPriceText + rmbTxt;

        if (isNormalPriceFree && isVipPriceFree) { // all free
            UIHelper.setViewVisibleOrGone(mDownloadPriceTv, false);
        } else {
            // has vip price
            if (Double.compare(-1, vipPrice) != 0) {
                String vipPriceTag = getString(R.string.vip);
                String vipPriceText = isVipPriceFree ? getString(R.string.free)
                        : vipPriceStr + rmbTxt;
//                text += "  " + vipPriceTag + (isVipPriceFree ? "" : " : ") + vipPriceText;
                text += "  " + vipPriceTag + vipPriceText;
            }
        }
        return text;
    }

    @Override
    public void showPurchaseFilm(final String mediaId, boolean isOnline, String sharpness,
            String price) {
        Logger.d("showPurchaseFilm, mediaId : " + mediaId + ", isOnline : " + isOnline
                + ", price : " + price + ", thread id : " + Thread.currentThread().getId());

        if (StringUtils.isNullOrEmpty(price)) {
            price = "";
        }
//        String purchase = getString(R.string.purchase);
//        String play = getString(R.string.play);
//        String download = getString(R.string.download);
//        String text =
//                purchase + " " + (isOnline ? play : download) + mLineSeparator + sharpness + " "
//                        + price;

//        View view = null;
//        Button button = null;
//        view = mPlayOrPurchaseVg.findViewWithTag(mediaId);
//        if (null == view) {
//            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
//            view = layoutInflater.inflate(R.layout.simple_button, null);
//            button = (Button) view.findViewById(R.id.btn);
//            view.setTag(mediaId);
//            mPlayOrPurchaseVg.addView(view);
//        }
//
//
//        button.setText(text);
//        view.setTag(R.id.tag_play_or_purchase, Constants.VIEW_TAG_PURCHASE);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getPresenter().purchaseFilm(mediaId);
//            }
//        });

        View view = isOnline ? mPurchaseOnlineFilmView : mPurchaseDownloadFilmView;

        int tag_child_view = R.id.tag_child_view;
        View button = (View) view.getTag(tag_child_view);
        if (null == button) {
            button = view.findViewById(R.id.btn);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                view.setTag(tag_child_view, button);
            }
        }
        if (button != null) {
            int resid = isOnline ? R.string.purchase_to_play : R.string.purchase_to_download;
            TextView tv = (TextView) view.findViewById(R.id.tv);
            tv.setText(resid);
//            button.setText(resid);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getPresenter().purchaseFilm(mediaId);
                }
            });
        }
        if (View.VISIBLE != view.getVisibility()) {
            UIHelper.setViewVisible(view, true);
            updateButtons();
        }
    }

    @Override
    public void hideAllPurchaseFilms() {
        removeChildViewsByTag(Constants.VIEW_TAG_PURCHASE);
//        updateButtons();
    }


    @Override
    public void showPlayFilm(final String mediaId, boolean isOnline, String sharpness) {
        Logger.d("showPlayFilm, mediaId : " + mediaId + ", isOnline : " + isOnline);
        String play = getString(R.string.play);
        String online = getString(R.string.online);
        String local = getString(R.string.local);
//        String text = (isOnline ? online : local) + " " + play + " " + sharpness;
        String text = (isOnline ? online : local) + play;

        //        String play = getString(R.string.play);
//        String online = getString(R.string.online);
//        String local = getString(R.string.local);
//        String text = (isOnline ? online : local) + " " + play + " " + sharpness;

//        View view = null;
//        Button button = null;
//        view = mPlayOrPurchaseVg.findViewWithTag(mediaId);
//        if (null == view) {
//            LayoutInflater layoutInflater = getActivity().getLayoutInflater();
//            view = layoutInflater.inflate(R.layout.simple_button, null);
//            button = (Button) view.findViewById(R.id.btn);
//            view.setTag(mediaId);
//            mPlayOrPurchaseVg.addView(view);
//        }
//
//
//        button.setText(text);
//        view.setTag(R.id.tag_play_or_purchase, Constants.VIEW_TAG_PLAY);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getPresenter().playFilm(mediaId);
//            }
//        });

        int tag_child_view = R.id.tag_child_view;
        View button = (View) mPlayFilmView.getTag(tag_child_view);
        if (null == button) {
            button = mPlayFilmView.findViewById(R.id.btn);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mPlayFilmView.setTag(tag_child_view, button);
            }
        }
        if (button != null) {
//            button.setText(text);
            TextView tv = (TextView) mPlayFilmView.findViewById(R.id.tv);
            tv.setText(text);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getPresenter().playFilm(mediaId);
                }
            });
        }
        if (View.VISIBLE != mPlayFilmView.getVisibility()) {
            UIHelper.setViewVisible(mPlayFilmView, true);
            updateButtons();
        }

        updatePlayProgress();
    }

    @Override
    public void hideAllPlayFilms() {
        removeChildViewsByTag(Constants.VIEW_TAG_PLAY);
//        updateButtons();
    }

    @Override
    public void setRegisterVipVisible(boolean visible) {
        if (!isAdded()) {
            return;
        }

        if (null == mBuyVipVg && getView() != null) {
            mBuyVipVg = getView().findViewById(R.id.filmdetail_buy_vip_btn);
        }
        if (visible) {
            if (View.VISIBLE != mBuyVipVg.getVisibility()) {
                UIHelper.setViewVisibleOrGone(mBuyVipVg, visible);
                updateButtons();
            }
        } else {
            if (View.VISIBLE == mBuyVipVg.getVisibility()) {
                UIHelper.setViewVisibleOrGone(mBuyVipVg, visible);
                updateButtons();
            }
        }
    }

    @Override
    public void setRegisterVip(String onlinePrice, String onlineVipPrice, String downloadPrice,
            String downloadVipPrice) {
        if (!isAdded()) {
            return;
        }

        BigDecimal olPriceVal = BigDecimal.ZERO;
        BigDecimal olVipPriceVal = BigDecimal.ZERO;
        BigDecimal dlPriceVal = BigDecimal.ZERO;
        BigDecimal dlVipPriceVal = BigDecimal.ZERO;

        if (!StringUtils.isNullOrEmpty(onlinePrice)) {
            try {
                olPriceVal = new BigDecimal(onlinePrice);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!StringUtils.isNullOrEmpty(onlineVipPrice)) {
            try {
                olVipPriceVal = new BigDecimal(onlineVipPrice);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!StringUtils.isNullOrEmpty(downloadPrice)) {
            try {
                dlPriceVal = new BigDecimal(downloadPrice);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!StringUtils.isNullOrEmpty(downloadVipPrice)) {
            try {
                dlVipPriceVal = new BigDecimal(downloadVipPrice);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // any VIP price is 0 && normal price is not 0
        boolean isVipFreeAlone = !StringUtils.isNullOrEmpty(onlineVipPrice)
                && 0 == BigDecimal.ZERO.compareTo(olVipPriceVal) && BigDecimal.ZERO.compareTo(
                olPriceVal) < 0
                || !StringUtils.isNullOrEmpty(downloadVipPrice) && 0 == BigDecimal.ZERO.compareTo(
                dlVipPriceVal) && BigDecimal.ZERO.compareTo(dlPriceVal) < 0;

        // all normal price is the same as vip price
        boolean priceMatch = (StringUtils.isNullOrEmpty(onlinePrice) || 0 == olPriceVal.compareTo(
                olVipPriceVal)) && (StringUtils.isNullOrEmpty(downloadPrice)
                || 0 == dlPriceVal.compareTo(dlVipPriceVal));

        int resId;
        if (isVipFreeAlone) {
            resId = R.string.film_detail_open_vip_for_free;
        } else if (priceMatch) {
            resId = R.string.film_detail_open_vip;
        } else {
            resId = R.string.film_detail_open_vip_for_discount;
        }

        if (null == mBuyVipVg && getView() != null) {
            mBuyVipVg = getView().findViewById(R.id.filmdetail_buy_vip_btn);
        }
        View buyVipBtn = (View) mBuyVipVg.getTag(R.id.tag_child_view);
        if (null == buyVipBtn) {
            buyVipBtn = mBuyVipVg.findViewById(R.id.btn);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mBuyVipVg.setTag(R.id.tag_child_view, buyVipBtn);
            }
            if (buyVipBtn != null) {
                buyVipBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Logger.d("Register Vip");
                        getPresenter().registerVip();
                    }
                });
            }
        }

//        if (buyVipBtn != null) {
//            buyVipBtn.setText(resId);
//        }

        TextView tv = (TextView) mBuyVipVg.findViewById(R.id.tv);
        if (tv != null) {
            tv.setText(resId);
        }
    }

    @Override
    public void showRegisterVipUI() {
        if (!isAdded()) {
            return;
        }

        Context context = getContext();
        context.startActivity(new Intent(context, BuyVipActivity.class));
    }

    @Override
    public void setCreditPayViewVisible(boolean visible) {
        if (!isAdded()) {
            return;
        }

        if (mCrediaPayView != null) {
            UIHelper.setViewVisibleOrGone(mCrediaPayView, visible);
            if (visible && View.VISIBLE != mCrediaPayView.getVisibility()
                    || !visible && View.VISIBLE == mCrediaPayView.getVisibility()) {
                updateButtons();
            }
        }
    }

    @Override
    public void setQrCodePayVisible(boolean visible) {
        Logger.d("setQrCodePayVisible, visible : " + visible);
        UIHelper.setViewVisibleOrGone(mRightView, visible);
        UIHelper.setViewVisibleOrGone(mQrCodePayVg, visible);
        UIHelper.setViewVisibleOrGone(mQrCodeContainer, visible);
        if (!visible) {
            int containerViewId = R.id.filmdetail_qr_code;
            FragmentManager childFragmentManager = getChildFragmentManager();
            Fragment fragment = childFragmentManager.findFragmentById(containerViewId);
            if (fragment != null) {
                Logger.d("setQrCodePayVisible, remove old fragment : " + fragment);
                childFragmentManager.beginTransaction().remove(fragment).commit();
            }
        }
    }

    @Override
    public Observable<Boolean> showQrCodePay(final String productName, final String price,
            final int qrPayMode) {
        Logger.d("showQrCodePay, productName : " + productName + ", price : " + price
                + ", qrPayMode : " + qrPayMode);
        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }

                int containerViewId = R.id.filmdetail_qr_code;
                FragmentManager childFragmentManager = getChildFragmentManager();
                Fragment fragment = childFragmentManager.findFragmentById(containerViewId);
                Logger.d("showQrCodePay, old fragment : " + fragment);
                if (fragment != null) {
                    Logger.d("showQrCodePay, remove old fragment : " + fragment);
                    childFragmentManager.beginTransaction().remove(fragment).commit();
                }

//                int height = (int) getResources().getDimension(R.dimen.film_detail_qr_pay_width);
                int height = mQrCodeContainer.getMeasuredWidth();
                Logger.d("showQrCodePay, height : " + height);
                QrcodeFragment smallQrcodeFragment = QrcodeFragment.newInstance(price, null,
                        productName, new QrcodeContract.PayResultCallBack() {
                            @Override
                            public void PayFinishExecute(int state, String log) {
                                if (subscriber.isUnsubscribed()) {
                                    return;
                                }
                                subscriber.onNext(1 == state);
                                subscriber.onCompleted();
                            }
                        }, qrPayMode, false, height, height, false);
                childFragmentManager
                        .beginTransaction()
                        .replace(containerViewId, smallQrcodeFragment)
                        .commit();
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public void showTrailer(final String name, final String url) {
        int tag_child_view = R.id.tag_child_view;
        View button = (View) mPlayTrailerView.getTag(tag_child_view);
        if (null == button) {
            button = mPlayTrailerView.findViewById(R.id.btn);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mPlayTrailerView.setTag(tag_child_view, button);
            }
        }
        if (button != null) {
            TextView tv = (TextView) mPlayTrailerView.findViewById(R.id.tv);
            tv.setText(R.string.film_detail_trailer);
//                button.setText(R.string.film_detail_trailer);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getPresenter().playTrailer(name, url, mFilmPoster);
                }
            });
        }

        UIHelper.setViewVisible(mPlayTrailerView, true);

        updateButtons();
        mFirstUpdateFocus = true;
    }

    @Override
    public void showNoTrailer() {
        Activity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.film_detail_toast, null);
        TextView tv = (TextView) view.findViewById(R.id.tv);
        tv.setText(R.string.film_detail_toast_no_trailer);
        Toast toast = new Toast(activity);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0);
        UIHelper.positionToast(toast, mBtnsVg, getActivity().getWindow(), 0, 0);
        toast.show();
    }

    @Override
    public void showRecommendPosters(final List<MovieRecommendFilm> recommendPosterList) {
        if (null == recommendPosterList || recommendPosterList.isEmpty()) {
            return;
        }

        if (mRecommendPosterVg != null) {
            mRecommendPosterVg.setVisibility(View.VISIBLE);
        }

        if (null == mRecommendPostersAdapter) {
            mRecommendPostersAdapter = new RecommendPostersAdapter(this, recommendPosterList);
            mRecommendPostersAdapter.setHasStableIds(true);
            mRecommendPosterRv.setAdapter(mRecommendPostersAdapter);
        } else {
            mRecommendPostersAdapter.replaceData(recommendPosterList);
        }
    }

    @Override
    public void setPurchasingIndicator(boolean active) {
        Logger.d("setPurchasingIndicator, active : " + active);
        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mPurchasingFilmDialog) {
                mPurchasingFilmDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.order_purchasing_film_please_wait));
                mPurchasingFilmDialog.setCancelable(false);
                mPurchasingFilmDialog.setCanceledOnTouchOutside(false);
            }

            if (!mPurchasingFilmDialog.isShowing()) {
                mPurchasingFilmDialog.show();
            }
        } else {
            if (mPurchasingFilmDialog != null) {
                UIHelper.dismissDialog(mPurchasingFilmDialog);
            }
        }
    }

    @Override
    public Observable<Boolean> showPurchaseFilmUI(final String filmName, final String mediaId,
            final boolean isOnline, final String encryptionType, final String productType,
            String sharpness, final String price, final boolean creditPay) {
        Logger.d("showPurchaseFilmUI");
        if (!isAdded()) {
            return Observable.just(false);
        }

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {

                String fragTag = PURCHASE_FRAG_TAG;
                removePreviousFragment(getFragmentManager(), fragTag);

                // Create and show the dialog.
                PurchaseDialogFragment fragment = PurchaseDialogFragment.newInstance(mFilmId,
                        productType, filmName, mediaId, price, creditPay, encryptionType, isOnline,
                        1);
                fragment.setListener(new PurchaseDialogFragment.OnPurchaseResultListener() {

                    @Override
                    public void onCancel() {
                        Logger.d("showPurchaseFilmUI, onCancel");
                    }

                    @Override
                    public void onPurchaseResult(boolean success, Order order,
                            FinanceOrder financeOrder, String errMsg) {
                        Logger.d(
                                "showPurchaseFilmUI, success : " + success + ", order : " + order
                                        + ", finance order : " + financeOrder + ", errMsg : "
                                        + errMsg);
                        // purchase success
                        if (success && !subscriber.isUnsubscribed()) {
                            subscriber.onNext(success);
                        }
                    }
                });
                fragment.setOnDialogDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Logger.d("showPurchaseFilmUI, onDismiss");
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
                fragment.show(getFragmentManager(), fragTag);
            }
        };

        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<Boolean> showCreditPayExpire(final int expireDate, final double creditBill,
            final double creditMaxLimit) {
        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                String fragTag = CREDIT_EXPIRE_FRAG_TAG;
                removePreviousFragment(getFragmentManager(), fragTag);
                // Create and show the dialog.
                CreditExpireDialogFragment fragment = CreditExpireDialogFragment.newInstance(
                        expireDate, creditBill, creditMaxLimit);
                fragment.setOnRefundResultListener(
                        new CreditExpireDialogFragment.OnRefundResultListener() {
                            @Override
                            public void onCancel() {
                                Logger.d("showCreditPayExpire, onCancel");
                                // user cancel
                            }

                            @Override
                            public void onRefundResult(boolean success, Order order) {
                                Logger.d("showCreditPayExpire, onRefundResult, success : " + success
                                        + ", order : " + order);
                                subscriber.onNext(success);
                            }
                        });
                fragment.setOnDialogDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
                fragment.show(getFragmentManager(), fragTag);
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public void showPurchaseFilmSuccess(String mediaId, long orderRemainTimeMillis) {
        Logger.d("showPurchaseFilmSuccess, mediaId : " + mediaId + ", orderRemainTimeMillis : "
                + orderRemainTimeMillis);
        if (!isAdded()) {
            return;
        }

        Toast.makeText(getContext(), R.string.film_detail_purchase_success,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showPurchaseFilmFailed(String mediaId, String errMsg) {
        if (!isAdded()) {
            return;
        }
        String text = getString(R.string.film_detail_purchase_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            text += ", " + errMsg;
        }
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showBalanceNotEnough(String mediaId, double price, double balance) {
        if (!isAdded()) {
            return;
        }

        String str =
                "Balance is not enough! ( mediaId : " + mediaId + ", price : " + price
                        + ", balance : " + balance + " )";
        Toast.makeText(getContext(), str, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showRestApiException(String errType, String errCode, String errMsg) {
        if (!isAdded()) {
            return;
        }

        String str =
                "RestApiException ( type : " + errType + ", note : " + errCode + ", noteMsg : "
                        + errMsg + " )";
        Toast.makeText(getContext(), str, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setPreparePlayingIndicator(boolean active) {
        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mPreparePlayingDialog) {
                mPreparePlayingDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.film_detail_checking_play_validity));
            }

            if (!mPreparePlayingDialog.isShowing()) {
                mPreparePlayingDialog.show();
            }
        } else {
            if (mPreparePlayingDialog != null) {
                UIHelper.dismissDialog(mPreparePlayingDialog);
            }
        }
    }

    @Override
    public void showPlayFilmException(int errCode, String errMsg, final String mediaId) {
        String text = getString(R.string.play_failed);
        String errDetailMsg = "";
        boolean isOverdue = false;
        switch (errCode) {
            case PlaybackValidity.ERR_TYPE_NOT_SUPPORT:
                errDetailMsg = getString(R.string.play_error_type_not_support);
                break;
            case PlaybackValidity.ERR_NO_VALID_ORDER: // no valid order
                errDetailMsg = getString(R.string.play_error_no_order);
                isOverdue = true;
                break;
            case PlaybackValidity.ERR_OVERDUE: // overdue
                errDetailMsg = getString(R.string.play_error_ticket_overdue);
                isOverdue = true;
                break;
            case PlaybackValidity.ERR_MEDIAINFO_NOT_FOUND:
                errDetailMsg = getString(R.string.play_error_mediainfo_not_found);
                break;
            case PlaybackValidity.ERR_DOWNLOAD_FILE_NOT_FOUND:
                errDetailMsg = getString(R.string.play_error_local_file_missing);
                break;
            case PlaybackValidity.ERR_DOWNLOAD_NO_TASK:
                errDetailMsg = getString(R.string.play_error_download_not_task);
                break;
            case PlaybackValidity.ERR_DOWNLOAD_NOT_FINISH:
                errDetailMsg = getString(R.string.play_error_download_not_finish);
                break;
            case PlaybackValidity.ERR_UNKNOWN:
                errDetailMsg = getString(R.string.play_error_unknown);
                break;
            default:
                break;
        }

        if (!StringUtils.isNullOrEmpty(errMsg)) {
            errDetailMsg += ", " + errMsg;
        }

        if (!StringUtils.isNullOrEmpty(errDetailMsg)) {
            text += ", " + errDetailMsg;
        }

        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();

        if (isOverdue) {
            Logger.d("show overdue view");
            final String fragment_TAG = PLAY_OVERDUE_FRAG_TAG;
            FragmentManager fragmentManager = getFragmentManager();
            removePreviousFragment(fragmentManager, fragment_TAG);
            PlayOverdueDialogFragment fragment = PlayOverdueDialogFragment.newInstance(mFilmName);
            fragment.setListener(new PlayOverdueDialogFragment.onSelectResultListener() {
                @Override
                public void onSelectResult(boolean ok) {
//                    if (ok) {
//                        getPresenter().purchaseFilm(mediaId);
//                    }
                }
            });
            fragment.show(fragmentManager, fragment_TAG);
        }
    }

    @Override
    public void navigateToPlayerActivity(@NonNull String filmId, @Nullable String mediaID,
            @Nullable String mediaName, boolean isOnline, @NonNull String encryptionType,
            @NonNull List<String> urls, @NonNull List<String> ranks, @NonNull List<Ad> advertList,
            @NonNull String posterUrl, boolean isTrailer) {
//        PlayerActivity.navigateToPlayerActivity(getContext(), filmId, mediaID, mediaName,
//                isOnline, encryptionType, urls, mediaList, advertList, posterUrl, isTrailer,
//                mColorBg);

        Intent intent = PlayerActivity.getNavigateIntent(getContext(), filmId, mediaID, mediaName,
                isOnline, encryptionType, urls, ranks, advertList, posterUrl, isTrailer, mColorBg);
        startActivityForResult(intent, REQUEST_CODE_PLAY);
    }

    @Override
    public void showDownload(@NonNull String mediaId, @NonNull String mediaUrl,
            @Nullable DownloadTaskInfo taskInfo, boolean canShowErrView) {
        _showDownload(mediaId, mediaUrl, taskInfo, canShowErrView);
    }

    @Override
    public void showDownloadUI(@NonNull String filmId, @NonNull String mediaId,
            @NonNull String mediaUrl, boolean reDownload) {
        _showDownloadUI(filmId, mediaId, mediaUrl, reDownload);
    }

    private void _showDownloadUI(@NonNull String filmId, @NonNull String mediaId,
            @NonNull String mediaUrl, boolean reDownload) {
        String fragTag = DOWNLOAD_FRAG_TAG;
        removePreviousFragment(getFragmentManager(), fragTag);
        // Create and show the dialog.
        DownloadDialogFragment fragment = DownloadDialogFragment.newInstance(filmId, mFilmName,
                mediaId, mediaUrl, null, reDownload);
        fragment.show(getFragmentManager(), fragTag);
    }

    @Override
    public void hideDownload() {
        if (mDownloadBtnVg != null) {
            if (View.VISIBLE == mDownloadBtnVg.getVisibility()) {
                UIHelper.setViewVisibleOrGone(mDownloadBtnVg, false);
                updateButtons();
            }
        }
        if (mDownloadProgressVg != null) {
            UIHelper.setViewVisibleOrGone(mDownloadProgressVg, false);
        }
        mDownloadBtn = null;
    }

    @Override
    public void showDownloadNoStorage(long mediaSize, boolean isToPurchase) {
        String title = getString(R.string.film_detail_download_error_no_storage);
        String baseTxt = getString(R.string.film_detail_download_error_no_storage_content);
        String size = Formatter.formatFileSize(getContext(), mediaSize);
        baseTxt = String.format(baseTxt, size);
        int resId = isToPurchase ? R.string.film_detail_download_error_to_purchase
                : R.string.film_detail_download_error_re_download;
        String text = baseTxt + ", " + getString(resId);

        String fragTag = DOWNLOAD_ERROR_NO_STORAGE_FRAG_TAG;
        removePreviousFragment(getFragmentManager(), fragTag);
        CommonAlertDialogFragment fragment = CommonAlertDialogFragment.newInstance(
                title, text, 10000);
        fragment.show(getFragmentManager(), fragTag);
    }

    @Override
    public void showDownloadCapacityNoEnough(long mediaSize, boolean isToPurchase) {
        String title = getString(R.string.film_detail_download_error_space_not_enough);
        String baseTxt = getString(R.string.film_detail_download_error_space_not_enough_content);
        String size = Formatter.formatFileSize(getContext(), mediaSize);
        baseTxt = String.format(baseTxt, size);
        int resId = isToPurchase ? R.string.film_detail_download_error_to_purchase
                : R.string.film_detail_download_error_re_download;
        String text = baseTxt + ", " + getString(resId);

        String fragTag = DOWNLOAD_ERROR_SPACE_NOT_ENOUGH_FRAG_TAG;
        removePreviousFragment(getFragmentManager(), fragTag);
        CommonAlertDialogFragment fragment = CommonAlertDialogFragment.newInstance(title, text,
                10000);
        fragment.show(getFragmentManager(), fragTag);
    }

    @Override
    public Observable<MediaAndPath> showSelectDownloadMediaAndPathView(@NonNull final String filmId,
            @NonNull final List<Media> mediaList,
            @NonNull List<StorageUtils.StorageInfo> storageList) {
        Logger.d("showSelectDownloadMediaAndPathView, filmId : " + filmId);

        Observable.OnSubscribe<MediaAndPath> onSubscribe =
                new Observable.OnSubscribe<MediaAndPath>() {
                    @Override
                    public void call(final Subscriber<? super MediaAndPath> subscriber) {

                        ArrayList<Media> medias = new ArrayList<>(mediaList);

                        String fragTag = SEL_DOWNLOAD_MEDIA_PATH_FRAG_TAG;
                        removePreviousFragment(getFragmentManager(), fragTag);

                        // Create and show the dialog.
                        SelectDownloadMediaAndPathFragment fragment =
                                SelectDownloadMediaAndPathFragment.newInstance(filmId, medias);
                        fragment.setListener(new SelectDownloadMediaAndPathFragment
                                .OnSelectDownloadMediaAndPathListener() {

                            @Override
                            public void onSelectedResult(String mediaId, String path) {
                                Logger.d(
                                        "showSelectDownloadMediaAndPathView, onSelectedResult, "
                                                + "mediaId : " + mediaId + ", path : " + path);
                                MediaAndPath mediaAndPath = new MediaAndPath(mediaId, path);
                                subscriber.onNext(mediaAndPath);
                            }

                            @Override
                            public void onCancel() {
                                Logger.d("showSelectDownloadMediaAndPathView, onCancel");
                                // user cancel
                            }
                        });
                        fragment.setOnDialogDismissListener(
                                new DialogInterface.OnDismissListener() {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        Logger.d("showSelectDownloadMediaAndPathView, onDismiss");
                                        if (!subscriber.isUnsubscribed()) {
                                            subscriber.onCompleted();
                                        }
                                    }
                                });
                        fragment.show(getFragmentManager(), fragTag);
                    }
                };

        return Observable.create(onSubscribe);

    }

    @Override
    public Observable<Boolean> showLocalPlayFileMissing() {
        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                String fragTag = PLAY_ERROR_FRAG_TAG;
                removePreviousFragment(getFragmentManager(), fragTag);
                // Create and show the dialog.
                String title = getString(R.string.play_error_local_file_missing);
                String content = getString(
                        R.string.play_error_local_file_missing_content);
                DownloadErrorFragment fragment = DownloadErrorFragment.newInstance(
                        title, content, true);
                fragment.setListener(
                        new DownloadErrorFragment.OnDownloadErrorListener() {
                            @Override
                            public void onReDownload() {
                                subscriber.onNext(true);
                            }

                            @Override
                            public void onCancel() {
                            }
                        });
                fragment.setOnDialogDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
                fragment.show(getFragmentManager(), fragTag);
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<Boolean> showFilmWatchNotice(final int watchNoteType, final long limitTime) {
        boolean hide = UserInfoHelper.getHideFilmWatchNotice(getContext());
        if (hide) {
            return Observable.just(true);
        }

        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                String fragTag = SHOW_WATCH_NOTICE_FRAG_TAG;
                removePreviousFragment(getFragmentManager(), fragTag);
//        final long sTime = System.currentTimeMillis();
                FilmWatchNoticeDialogFragment fragment = FilmWatchNoticeDialogFragment.newInstance(
                        watchNoteType, limitTime);
                fragment.setOnNoticeHideListener(
                        new FilmWatchNoticeDialogFragment.OnNoticeHideListener() {
                            @Override
                            public void onHide(boolean hideAlways) {
                                UserInfoHelper.setHideFilmWatchNotice(getContext(), hideAlways);
                                if (!subscriber.isUnsubscribed()) {
                                    subscriber.onNext(true);
                                }
//                final long eTime = System.currentTimeMillis();
//                StatisticsHelper instance = StatisticsHelper.getInstance(getContext());
//                instance.reportExitWatchNotice(
//                        hideAlways ? "1" : "2", "1", String.valueOf((eTime - sTime) / 1000));
                            }
                        });
                fragment.setOnDialogDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
                fragment.show(getFragmentManager(), fragTag);
            }
        });
    }

    @Override
    public Observable<Boolean> showCreditPayNotice(final double limit, final int deadlineDays) {
        boolean show = UserInfoHelper.getShowCreditPayNotice(getContext());
        if (show) {
            return Observable.just(false);
        }

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                String fragTag = SHOW_CREDIT_PAY_NOTICE_FRAG_TAG;
                removePreviousFragment(getFragmentManager(), fragTag);
                CreditPayNoticeDialogFragment fragment = CreditPayNoticeDialogFragment.newInstance(
                        limit, deadlineDays);
                fragment.setOnNoticeHideListener(
                        new CreditPayNoticeDialogFragment.OnNoticeHideListener() {
                            @Override
                            public void onHide() {
                                UserInfoHelper.setShowCreditPayNotice(getContext(), true);
                                if (subscriber.isUnsubscribed()) {
                                    return;
                                }
                                subscriber.onNext(true);
                            }
                        });
                fragment.setOnDialogDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
                fragment.show(getFragmentManager(), fragTag);
            }
        };

        return Observable.create(onSubscribe);
    }

    @Override
    public void showCommonException(String errMsg) {
        if (isAdded()) {
            if (StringUtils.isNullOrEmpty(errMsg)) {
                errMsg = "";
            }
            Toast.makeText(getContext(), "Exception : " + errMsg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setPresenter(FilmDetailContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    protected FilmDetailContract.Presenter getPresenter() {
        return mPresenter;
    }

    private void removeChildViewsByTag(@NonNull final String TYPE) {
//        int childCount = mPlayOrPurchaseVg.getChildCount();
//        int i = 0;
//        // has child views && not reach end
//        while (mPlayOrPurchaseVg.getChildCount() > 0 && i < mPlayOrPurchaseVg.getChildCount()) {
//            View childView = mPlayOrPurchaseVg.getChildAt(i);
//            Object value = childView.getTag(R.id.tag_play_or_purchase);
//
//            if (value != null && TYPE.equals(value)) { // find the view
//                // remove it
//                mPlayOrPurchaseVg.removeViewAt(i);
//            } else { // not found
//                // move to next
//                i++;
//            }
//        }

        switch (TYPE) {
            case Constants.VIEW_TAG_PURCHASE:
                if (mPurchaseOnlineFilmView != null) {
                    if (View.VISIBLE == mPurchaseOnlineFilmView.getVisibility()) {
                        UIHelper.setViewVisibleOrGone(mPurchaseOnlineFilmView, false);
                        updateButtons();
                    }
                }
                if (mPurchaseDownloadFilmView != null) {
                    if (View.VISIBLE == mPurchaseDownloadFilmView.getVisibility()) {
                        UIHelper.setViewVisibleOrGone(mPurchaseDownloadFilmView, false);
                        updateButtons();
                    }
                }
                break;
            case Constants.VIEW_TAG_PLAY:
                if (mPlayFilmView != null) {
                    if (View.VISIBLE == mPlayFilmView.getVisibility()) {
                        UIHelper.setViewVisibleOrGone(mPlayFilmView, false);
                        updateButtons();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void setText(@Nullable TextView textView, @Nullable String text) {
        if (null == textView) {
            return;
        }
        if (StringUtils.isNullOrEmpty(text)) {
            text = "";
        }
        textView.setText(text);
    }

    private void dynamicUpdateBackground(final View v) {
        long st = System.currentTimeMillis();

        Logger.d("mPosterIgv.getDrawable() : " + mPosterIgv.getDrawable());

        // first pick up poster initial drawable
        Drawable drawable = mPosterIgv.getDrawable();
        Bitmap bitmap = null;

        // if no initial drawable
//        if (null == drawable) {
//
//            int[] backgroundIds = new int[]{R.drawable.film_detail_demo_big_poster,
//                    R.drawable.film_detail_demo_big_poster2,
//                    R.drawable.film_detail_demo_big_poster3,
//                    R.drawable.film_detail_demo_big_poster4};
//            int backgroundId = 0;
//
//            String filmId = getArguments().getString(Constants.EXTRA_FILM_ID);
//            if (!StringUtils.isNullOrEmpty(filmId)) {
//                try {
//                    int i = Integer.parseInt(filmId);
//                    backgroundId = backgroundIds[i % backgroundIds.length];
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//
//            if (backgroundId != 0) {
//                Glide.with(this)
//                        .load(backgroundId)
//                        .asBitmap()
//                        .into(new BitmapImageViewTarget(mPosterIgv) {
//                            @Override
//                            public void onResourceReady(Bitmap resource,
//                                    GlideAnimation<? super Bitmap> glideAnimation) {
//                                super.onResourceReady(resource, glideAnimation);
//                                Logger.d("onResourceReady, resource : " + resource);
//                                if (resource != null) {
//                                    doDynamicUpdateBackground(v, resource);
//                                }
//                            }
//                        });
//            }
//        }

        if (drawable != null) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
            doDynamicUpdateBackground(v, bitmap);
        }

        long et = System.currentTimeMillis();
        Logger.d("dynamicUpdateBackground, time : " + (et - st) + "ms");
    }

    private void doDynamicUpdateBackground(@NonNull final View view, @NonNull Bitmap bitmap) {

        if (null == view || null == bitmap) {
            return;
        }

        if (bitmap.isRecycled()) {
            return;
        }

        // Asynchronous

        Palette.PaletteAsyncListener listener = new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {

                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
                Palette.Swatch mutedSwatch = palette.getMutedSwatch();
                Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();
                Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();
//                Logger.d("Palette.generateAsync, getMutedSwatch : " + mutedSwatch
//                        + ", getDarkMutedSwatch : " + darkMutedSwatch + ", getVibrantSwatch : "
//                        + vibrantSwatch + ", darkVibrantSwatch : " + darkVibrantSwatch);

                Palette.Swatch swatch = null;
                Palette.Swatch darkSwatch = null;
//                swatch = mutedSwatch != null ? mutedSwatch : darkMutedSwatch;
//                 darkSwatch =
//                        darkMutedSwatch != null ? darkMutedSwatch : darkVibrantSwatch;

                swatch = mutedSwatch;
                darkSwatch = darkMutedSwatch;

                if (null == swatch || null == darkSwatch) {
                    swatch = vibrantSwatch;
                    darkSwatch = darkVibrantSwatch;
                }

                // If we have a color
                if (swatch != null && darkSwatch != null) {
                    // generate gradient drawable
                    int[] colors = null;
//                    if (lightMutedSwatch != null) {
//                        colors = new int[]{darkSwatch.getRgb(), swatch.getRgb(),
//                                lightMutedSwatch.getRgb()};
//                    } else {
//                        colors = new int[]{darkSwatch.getRgb(), swatch.getRgb()};
//                    }
                    colors = new int[]{darkSwatch.getRgb(), swatch.getRgb()};
                    mColorBg = colors;
                    GradientDrawable gd = new GradientDrawable(
                            GradientDrawable.Orientation.TOP_BOTTOM, colors);
//                    gd.setCornerRadius(0f);
                    // first make view transparent
//                    view.setAlpha(0);

                    // change background
                    UIHelper.setBackground(view, gd);

//                    // change alpha
//                    ObjectAnimator anim = ObjectAnimator.ofFloat(view, View.ALPHA.getName(), 0f,
//                            1f);
//                    anim.setDuration(600);
//                    anim.setInterpolator(new DecelerateInterpolator());
//                    anim.start();

                }
            }
        };

        if (mPaletteTask != null) {
            // cancel last task
            mPaletteTask.cancel(true);
        }

        mPaletteTask = new Palette.Builder(bitmap).maximumColorCount(24).generate(listener);
    }

    private void initRecommendPosterView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        mRecommendPosterRv.setLayoutManager(linearLayoutManager);
        final int space = (int) getResources().getDimension(
                R.dimen.film_detail_recommend_poster_item_spacing);
        mRecommendPosterRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                    RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildLayoutPosition(view) != 0) {
                    outRect.left = space;
                }
            }
        });
        ItemClickSupport.addTo(mRecommendPosterRv).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        Logger.d("onItemClicked, position : " + position);
                        MovieRecommendFilm poster = mRecommendPostersAdapter.getItem(position);
                        String filmId = poster.getReleaseid();
                        if (!StringUtils.isNullOrEmpty(filmId)) {
                            FilmDetailActivity.jumpToFilmDetailActivity(getContext(), filmId,
                                    VIEW_CODE_FILM_DETAIL, false, 0);
                        } else {
                            Toast.makeText(getContext(), R.string.film_detail_missing_film,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
        UIHelper.setAllParentsClip(mRecommendPosterRv, false);
    }

    private void updateButtons() {
//        Logger.d("updateButtons");
        if (null == mBtnsVg || 0 == mBtnsVg.getChildCount()) {
            return;
        }

//        mCrediaPayView.setVisibility(View.VISIBLE);

        View firstChildView = null;
        View lastChildView = null;
        View lastVisibleChildView = null;
        int childCount = mBtnsVg.getChildCount();
        int visibleBtnCounts = 0;
        for (int i = 0; i < childCount; i++) {
            View childView = mBtnsVg.getChildAt(i);
            if (childView != null && View.VISIBLE == childView.getVisibility()) {
                // count visible buttons
                visibleBtnCounts++;
            }
        }

        Logger.d("updateButtons, visibleBtnCounts : " + visibleBtnCounts);
        int bgResId = -1;
        int pbMarginResId = 0;
        if (visibleBtnCounts >= 4) {
            bgResId = R.drawable.sel_film_detail_btn_bg_large;
            pbMarginResId = R.dimen.simple_btn_pb_margin_w_large;
        } else if (visibleBtnCounts >= 3) {
            bgResId = R.drawable.sel_film_detail_btn_bg_middle;
            pbMarginResId = R.dimen.simple_btn_pb_margin_w_middle;
        } else if (visibleBtnCounts >= 1) {
            bgResId = R.drawable.sel_film_detail_btn_bg_small;
            pbMarginResId = R.dimen.simple_btn_pb_margin_w_small;
        } else {
            bgResId = R.drawable.sel_film_detail_btn_bg_large;
            pbMarginResId = R.dimen.simple_btn_pb_margin_w_large;
        }

        for (int i = 0; i < childCount; i++) {
            View childView = mBtnsVg.getChildAt(i);
            if (childView != null && View.VISIBLE == childView.getVisibility()) {
                lastVisibleChildView = childView;
                if (null == firstChildView) {
                    firstChildView = childView;
                }

                // last time is 1 button && current count is not
                if (1 == mLastVisibleBtnCounts && visibleBtnCounts != mLastVisibleBtnCounts) {
                    LinearLayout.LayoutParams layoutParams =
                            (LinearLayout.LayoutParams) childView.getLayoutParams();
                    if (Double.valueOf(layoutParams.weight).compareTo(1d) != 0) {
                        // let the child view be auto stretched
                        layoutParams.width = 0;
                        layoutParams.weight = 1;
                        childView.setLayoutParams(layoutParams);
                    }
                }

                // visible buttons is different from last time
                if (mLastVisibleBtnCounts != visibleBtnCounts) {
                    View btn = (View) childView.getTag();
                    View pb = childView.findViewById(R.id.progressbar);
                    if (null == btn) {
                        btn = childView.findViewById(R.id.btn);
                        childView.setTag(btn);
                    }
                    if (btn != null) {
                        // change background
                        UIHelper.setBackground(btn, bgResId);
                    }
                    if (pb != null) {
                        ViewGroup.MarginLayoutParams layoutParams =
                                (ViewGroup.MarginLayoutParams) pb.getLayoutParams();
                        Resources resources = getResources();
                        int marginH = Math.round(resources.getDimension(pbMarginResId));
                        layoutParams.leftMargin = marginH;
                        layoutParams.rightMargin = marginH;
                        // if progressbar is shown
                        if (View.VISIBLE == pb.getVisibility()) {
                            pb.setLayoutParams(layoutParams);
                        }
                    }
                }
            }
        }

        lastChildView = lastVisibleChildView;

        if (firstChildView != null) {
            ViewGroup.MarginLayoutParams layoutParams = null;
            layoutParams = (ViewGroup.MarginLayoutParams) firstChildView.getLayoutParams();
            if (0 != layoutParams.leftMargin) {
                layoutParams.leftMargin = 0;
                firstChildView.setLayoutParams(layoutParams);
            }
        }

        if (lastChildView != null) {
            ViewGroup.MarginLayoutParams layoutParams = null;
            layoutParams = (ViewGroup.MarginLayoutParams) lastChildView.getLayoutParams();
            if (0 != layoutParams.rightMargin) {
                layoutParams.rightMargin = 0;
                lastChildView.setLayoutParams(layoutParams);
            }
        }

        // only one button view && last time is not
        if (1 == visibleBtnCounts && mLastVisibleBtnCounts != visibleBtnCounts
                && firstChildView != null) {
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) firstChildView.getLayoutParams();
            if (Double.valueOf(layoutParams.weight).compareTo(0d) != 0) {
                // set width to half of the parent's width
                layoutParams.width = mBtnsVg.getWidth() >> 1;
                layoutParams.weight = 0;
                firstChildView.setLayoutParams(layoutParams);
            }
        }

        // get current focus view
        View focusedView = getActivity().getCurrentFocus();

        Logger.d("updateButtons, mFirstUpdateFocus : " + mFirstUpdateFocus + "\n"
                + "mLastVisibleBtnCounts : " + mLastVisibleBtnCounts + ", visibleBtnCounts : "
                + visibleBtnCounts);

        // first update focus || visible views count has change || no focus view || focus view is
        // not visible
        if (mFirstUpdateFocus
                || (visibleBtnCounts > 0 && mLastVisibleBtnCounts != visibleBtnCounts)
                || (mLastFirstFocusBtn != null
                && View.VISIBLE != mLastFirstFocusBtn.getVisibility())
                || null == focusedView
                || View.VISIBLE != focusedView.getVisibility()) {

            boolean isChildViewFocus = false;
            if (mBtnsVg.isFocused() || mBtnsVg.getFocusedChild() != null) {
                isChildViewFocus = true;
            }

            // no child view get focused && has child views...
            if ((mFirstUpdateFocus || !isChildViewFocus) && firstChildView != null) {
                mLastFirstFocusBtn = firstChildView;
                // first child
//                View btn = firstChildView.findViewById(R.id.btn);
                View btn = (View) firstChildView.getTag();
                if (null == btn) {
                    btn = firstChildView.findViewById(R.id.btn);
                    firstChildView.setTag(btn);
                }
                if (btn != null) {
                    // request focus
                    btn.requestFocus();
                    btn.requestFocusFromTouch();
                }
            }
        }
        mFirstUpdateFocus = false;
        mLastVisibleBtnCounts = visibleBtnCounts;
    }

    private void registerReceiver() {
        // download broadcast filter
        IntentFilter filter = new IntentFilter(DownloadConstants.DOWNLOAD_ACTION);
        filter.addAction(DownloadConstants.AUTO_RESUME_ACTION);
        if (null == mDownloadReceiver) {
            mDownloadReceiver = new DownloadReceiver();
        }
        getActivity().registerReceiver(mDownloadReceiver, filter);
    }

    private void unRegisterReceiver() {
        if (mDownloadReceiver != null) {
            getActivity().unregisterReceiver(mDownloadReceiver);
            mDownloadReceiver = null;
        }
    }

    private void _showDownload(@NonNull final String mediaId, @NonNull final String mediaUrl,
            @Nullable DownloadTaskInfo downloadTaskInfo, boolean canShowErrView) {
        if (mDownloadBtnVg != null) {
            if (View.VISIBLE != mDownloadBtnVg.getVisibility()) {
                UIHelper.setViewVisible(mDownloadBtnVg, true);
                updateButtons();
            }
        }
        if (mDownloadProgressVg != null) {
            UIHelper.setViewVisible(mDownloadProgressVg, true);
        }

        String btnText = getString(R.string.film_detail_download_start);
        String downloadDetailText = "";
        double progress = 0;
        if (downloadTaskInfo != null) {
            long fileSize = downloadTaskInfo.getTotalSize();
            long completeSize = downloadTaskInfo.getCompleteSize();
            if (fileSize != 0) {
                progress = 1.0 * completeSize / fileSize * 100;
                progress = MathExtend.round(progress, 2);
            }
            // 已经下载部分大小，但可能进度太小，若进度为0.0%，则应该显示0.01%来提示用户
            if (completeSize > 0 && new BigDecimal(progress).equals(BigDecimal.ZERO)) {
                progress = 0.01;
            }

            int status = downloadTaskInfo.getStatus();
            int errCode = downloadTaskInfo.getErrCode();
            switch (status) {

                // pending
                case DownloadConstants.Status.PENDING:
                    btnText = getString(R.string.film_detail_download_pause);
                    downloadDetailText = getString(R.string.film_detail_download_pending);
                    break;

                // downloading
                case DownloadConstants.Status.START:
                case DownloadConstants.Status.RUNNING:
                    btnText = getString(R.string.film_detail_download_pause);
                    long speed = downloadTaskInfo.getCurrentSpeed();
                    String speedStr = formatFileSize(getContext(), speed);
                    mStringBuilder.setLength(0);
                    mStringBuilder.append(speedStr);
                    mStringBuilder.append("/S");
                    // download speed
                    downloadDetailText = mStringBuilder.toString();
                    break;

                case DownloadConstants.Status.FAILURE:
                    // download error description
                    downloadDetailText = DownloadUtils.getDownloadErrorDescription(getResources(),
                            errCode);
                case DownloadConstants.Status.STOPPED:
                case DownloadConstants.Status.STOPPING:
                    btnText = getString(R.string.film_detail_download_resume);
                    break;
                default:
                    downloadDetailText = "";
                    break;
            }

            // update progress bar style
            int progressBarResId = DownloadConstants.Status.FAILURE == status
                    ? R.drawable.sel_film_detail_download_progress_error
                    : R.drawable.sel_film_detail_download_progress_normal;
            Drawable drawable = ContextCompat.getDrawable(getContext(), progressBarResId);
            mDownloadProgressBar.setProgressDrawable(drawable);

            // can show error view &&  download error
            if (canShowErrView && DownloadConstants.Status.FAILURE == status) {
                // network available
                if (NetworkUtils.isNetworkAvailable(getContext())) {
                    // show download error view
                    showDownloadError(mediaId, mediaUrl, downloadTaskInfo.getFilePath(), errCode);
                }
            }
        }

        // set download progress
        mDownloadProgressBar.setProgress((int) progress);
        mStringBuilder.setLength(0);
        mStringBuilder.append(progress);
        mStringBuilder.append("%");
        mDownloadProgressTv.setText(mStringBuilder.toString());

        // set download detail text
        mDownloadDetailTv.setText(downloadDetailText);

        if (null == mDownloadBtn) {
            mDownloadBtn = mDownloadBtnVg.findViewById(R.id.btn);
            mDownloadBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Logger.d("Download btn onClick, mediaId : " + mediaId);
                    getPresenter().download(mediaId, mediaUrl, false);
                }
            });
        }

        if (null == mDownloadTv) {
            mDownloadTv = (TextView) mDownloadBtnVg.findViewById(R.id.tv);
        }
        // set download text
        mDownloadTv.setText(btnText);

        // set download button text
//        mDownloadBtn.setText(btnText);
        // save media id
        mDownloadBtn.setTag(mediaId);
    }

    private void showDownloadError(final String mediaId, final String mediaUrl, String filePath,
            int errCode) {
        String fragTag = DOWNLOAD_ERROR_FRAG_TAG;
        removePreviousFragment(getFragmentManager(), fragTag);
        // Create and show the dialog.
        DownloadErrorFragment fragment = DownloadErrorFragment.newInstance(mFilmName, filePath,
                errCode);
        fragment.setListener(new DownloadErrorFragment.OnDownloadErrorListener() {
            @Override
            public void onReDownload() {
                // re-download
                getPresenter().download(mediaId, mediaUrl, true);
            }

            @Override
            public void onCancel() {
            }
        });
        fragment.show(getFragmentManager(), fragTag);
    }

    /**
     * update playback progress
     */
    private void updatePlayProgress() {
        if (null == mPlayFilmView || View.VISIBLE != mPlayFilmView.getVisibility()) {
            return;
        }

        Observable.OnSubscribe<Pair<Integer, Integer>> onSubscribe =
                new Observable.OnSubscribe<Pair<Integer, Integer>>() {
                    @Override
                    public void call(Subscriber<? super Pair<Integer, Integer>> subscriber) {
                        long duration = UserInfoHelper.getPlayDuration(getContext(), mFilmId);
                        long position = UserInfoHelper.getUserPlayCurrentPosition(getContext(),
                                mFilmId);
                        Logger.d("updatePlayProgress, duration : " + duration + ", position : "
                                + position);
                        subscriber.onNext(new Pair<>((int) duration, (int) position));
                        subscriber.onCompleted();
                    }
                };
        if (mUpdateProgressSubscription != null) {
            mUpdateProgressSubscription.unsubscribe();
        }
        mUpdateProgressSubscription = Observable.create(onSubscribe)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<Integer, Integer>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "updatePlayProgress, onError : ");
                    }

                    @Override
                    public void onNext(Pair<Integer, Integer> integerPair) {
                        if (integerPair != null) {
                            ProgressBar progressBar = (ProgressBar) mPlayFilmView.findViewById(
                                    R.id.progressbar);
                            int duration = integerPair.first;
                            int position = integerPair.second;
                            if (duration > 0 && position > 0) {
                                progressBar.setMax((int) duration);
                                progressBar.setProgress((int) position);
                                UIHelper.setViewVisible(progressBar, true);
                            } else {
                                UIHelper.setViewVisible(progressBar, false);
                            }
                        }
                    }
                });
    }

    private int getSubscriptBgResource(String type) {
        int color = R.color.translucent;
        if (!StringUtils.isNullOrEmpty(type)) {
            switch (type) {
                case "1":
                    color = R.drawable.subscript_color1;
                    break;
                case "2":
                    color = R.drawable.subscript_color2;
                    break;
                case "3":
                    color = R.drawable.subscript_color3;
                    break;
                case "4":
                    color = R.drawable.subscript_color4;
                    break;
                case "5":
                    color = R.drawable.subscript_color5;
                    break;
                default:
                    break;
            }
        }
        return color;
    }

    private void reportEnterActivity() {
        StatisticsHelper instance = StatisticsHelper.getInstance(
                getContext().getApplicationContext());
        instance.reportEnterActivity(VIEW_CODE_FILM_DETAIL, mFilmName, mFrom);
    }

    private class DownloadReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String downloadId = intent
                    .getStringExtra(DownloadConstants.Intents.DOWNLOAD_INTENT_ID);
            if (StringUtils.isNullOrEmpty(action) || StringUtils.isNullOrEmpty(downloadId)) {
                return;
            }

            if (DownloadConstants.AUTO_RESUME_ACTION.equals(action)) {
                // show resume download view...
                // TODO: 2016/11/22
            } else if (DownloadConstants.DOWNLOAD_ACTION.equals(action)) {
                final DownloadTaskInfo downloadTaskInfo = intent
                        .getParcelableExtra(DownloadConstants.Intents.DOWNLOAD_INTENT_TASKINFO);
                if (null == downloadTaskInfo) {
                    return;
                }
                if (getPresenter() != null) {
                    getPresenter().updateDownloadInfo(downloadTaskInfo);
                }
            }

        }
    }
}