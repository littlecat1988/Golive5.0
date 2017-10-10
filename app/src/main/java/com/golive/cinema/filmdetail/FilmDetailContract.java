package com.golive.cinema.filmdetail;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IAppBaseView;
import com.golive.cinema.download.domain.model.MediaAndPath;
import com.golive.cinema.util.StorageUtils;
import com.golive.network.entity.Ad;
import com.golive.network.entity.Media;
import com.golive.network.entity.MovieRecommendFilm;
import com.initialjie.download.aidl.DownloadTaskInfo;

import java.util.List;

import rx.Observable;

/**
 * This specifies the contract between the view and the presenter.
 * Created by Wangzj on 2016/7/8.
 */
public interface FilmDetailContract {

    interface View extends IAppBaseView<Presenter> {
        int WATCH_TYPE_SHOUFA = 0;
        int WATCH_TYPE_TONGBU = 1;

        void setLoadingIndicator(boolean active);

        void setUpdatingViewIndicator(boolean active);

        void showMissingFilm();

        void showLoadFilmSuccess();

        void showLoadFilmFailed(String errMsg);

        void showUpdatingViewFailed(String errMsg);

        void showTitle(String title);

        void showDescription(String description);

        void showScore(String score);

        void showYear(@Nullable String year);

        void showFilmPoster(@Nullable String url);

        void showCategory(@Nullable String category);

        void showDirector(@Nullable String directorName);

        void showActors(@Nullable String actorsName);

        void showCountry(@Nullable String country);

        void showLanguage(@Nullable String language);

        void showDuration(@Nullable String duration);

        void showWatchTime(@Nullable String startTime, @Nullable String endTime);

        void showCornerMark(@Nullable String cornerLeft, @Nullable String cornerRightContent,
                @Nullable String cornerRightColor);

        void showPrice(@Nullable String olNormalPrice, @Nullable String olVipPrice,
                @Nullable String dlNormalPrice, @Nullable String dlVipPrice);

        void showPurchaseFilm(String mediaId, boolean isOnline, String sharpness, String price);

        void hideAllPurchaseFilms();

        void setPurchasingIndicator(boolean active);

        Observable<Boolean> showPurchaseFilmUI(String filmName, String mediaId,
                boolean isOnline, String encryptionType, String productType, String sharpness,
                String price,
                boolean creditPay);

        Observable<Boolean> showCreditPayExpire(int expireDate, double creditBill,
                double creditMaxLimit);

        void showPurchaseFilmSuccess(String mediaId, long orderRemainTimeMillis);

        void showPurchaseFilmFailed(String mediaId, String errMsg);

        void showPlayFilm(String mediaId, boolean isOnline, String sharpness);

        void hideAllPlayFilms();

        void setRegisterVipVisible(boolean visible);

        void setRegisterVip(String onlinePrice, String onlineVipPrice, String downloadPrice,
                String downloadVipPrice);

        void showRegisterVipUI();

        void setCreditPayViewVisible(boolean visible);

        void setQrCodePayVisible(boolean visible);

        Observable<Boolean> showQrCodePay(String productName, String price, int qrPayMode);

        void showTrailer(String name, String url);

        void showNoTrailer();

        void showRecommendPosters(List<MovieRecommendFilm> recommendPosterList);

        void showBalanceNotEnough(String mediaId, double price, double balance);

        void showRestApiException(String mType, String mNote, String mNoteMsg);

        void setPreparePlayingIndicator(boolean active);

        void showPlayFilmException(int errCode, String errMsg, String mediaId);

        void navigateToPlayerActivity(@NonNull String filmId, @Nullable String mediaID,
                @Nullable String mediaName, boolean isOnline, @NonNull String encryptionType,
                @NonNull List<String> urls, @NonNull List<String> ranks, List<Ad> advertList,
                @NonNull String posterUrl, boolean isTrailer);

        void showDownload(@NonNull String mediaId, @NonNull String mediaUrl,
                @Nullable DownloadTaskInfo taskInfo, boolean canShowErrView);

        void showDownloadUI(@NonNull String filmId, @NonNull String mediaId,
                @NonNull String mediaUrl, boolean reDownload);

        void hideDownload();

        void showDownloadNoStorage(long mediaSize, boolean isToPurchase);

        void showDownloadCapacityNoEnough(long mediaSize, boolean isToPurchase);

        Observable<MediaAndPath> showSelectDownloadMediaAndPathView(@NonNull String filmId,
                @NonNull List<Media> mediaList,
                @NonNull List<StorageUtils.StorageInfo> storageList);

        Observable<Boolean> showLocalPlayFileMissing();

        /**
         * Show film watch notice
         *
         * @param watchNoteType {@linkplain #WATCH_TYPE_SHOUFA} , shoufa; {@linkplain
         *                      #WATCH_TYPE_TONGBU} , tongbu
         * @param limitTime     limit watch time
         */
        Observable<Boolean> showFilmWatchNotice(int watchNoteType, long limitTime);

        /**
         * Show credit pay notice
         *
         * @param limit        limit
         * @param deadlineDays deadline days
         */
        Observable<Boolean> showCreditPayNotice(double limit, int deadlineDays);

        void showCommonException(String errMsg);
    }

    interface Presenter extends IBasePresenter<View> {

        void loadFilmDetail(boolean forceUpdate);

        void purchaseFilm(String mediaId);

        void creditPay();

        void registerVip();

        void playFilm(String mediaId);

        void playTrailer(String name, String url, String posterUrl);

        void download(@NonNull String mediaId, @NonNull String mediaUrl, boolean reDownload);

        void updateDownloadInfo(DownloadTaskInfo downloadTaskInfo);

        void reportEnterFilmDetail(@Nullable String source, @Nullable String status);

        void reportExitFilmDetail(@Nullable String to, @Nullable String duration,
                @Nullable String status);
    }
}