package com.golive.cinema.filmdetail;


import static com.golive.cinema.filmdetail.FilmDetailContract.View.WATCH_TYPE_SHOUFA;
import static com.golive.cinema.filmdetail.FilmDetailContract.View.WATCH_TYPE_TONGBU;
import static com.golive.cinema.util.Preconditions.checkNotNull;
import static com.golive.cinema.util.StorageUtils.getStorageList;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.ObjectWarp;
import com.golive.cinema.UniteThrowable;
import com.golive.cinema.download.DownloadUtils;
import com.golive.cinema.download.domain.model.BackupDownloadInfo;
import com.golive.cinema.download.domain.model.DownloadFileList;
import com.golive.cinema.download.domain.model.MediaAndPath;
import com.golive.cinema.download.domain.usecase.GetDownloadTaskInfoUseCase;
import com.golive.cinema.filmdetail.domain.usecase.GetFilmDetailUseCase;
import com.golive.cinema.order.OrderManager;
import com.golive.cinema.order.domain.usecase.GetValidOrderUseCase;
import com.golive.cinema.player.domain.model.PlaybackValidity;
import com.golive.cinema.player.domain.usecase.GetPlayTicketUseCase;
import com.golive.cinema.player.domain.usecase.GetPlaybackValidityUseCase;
import com.golive.cinema.purchase.domain.usecase.PurchaseUseCase;
import com.golive.cinema.recommend.domain.usecase.GetMovieRecommendUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportEnterFilmDetailUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportExitFilmDetailUseCase;
import com.golive.cinema.user.myinfo.domain.usecase.GetUserInfoUseCase;
import com.golive.cinema.user.pay.QrcodeContract;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserCreditWalletUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserWalletUseCase;
import com.golive.cinema.util.EspressoIdlingResource;
import com.golive.cinema.util.StorageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Ad;
import com.golive.network.entity.Film;
import com.golive.network.entity.Media;
import com.golive.network.entity.MovieRecommendFilm;
import com.golive.network.entity.Order;
import com.golive.network.entity.Ticket;
import com.golive.network.entity.UserInfo;
import com.golive.network.entity.Video;
import com.golive.network.entity.Wallet;
import com.initialjie.download.aidl.DownloadTaskInfo;
import com.initialjie.log.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func4;

/**
 * Created by Wangzj on 2016/10/9.
 */

public class FilmDetailPresenter extends BasePresenter<FilmDetailContract.View> implements
        FilmDetailContract.Presenter {

    private final GetFilmDetailUseCase mGetFilmDetailUseCase;
    private final GetMovieRecommendUseCase mGetMovieRecommendUseCase;
    private final GetValidOrderUseCase mGetValidOrderUseCase;
    private final GetUserWalletUseCase mGetUserWalletUseCase;
    private final GetUserCreditWalletUseCase mGetUserCreditWalletUseCase;
    private final PurchaseUseCase mPurchaseFilmUseCase;
    private final GetUserInfoUseCase mGetUserInfoUseCase;
    private final GetPlaybackValidityUseCase mGetPlaybackValidityUseCase;
    private final GetDownloadTaskInfoUseCase mGetDownloadTaskInfoUseCase;
    private final GetPlayTicketUseCase mGetPlayTicketUseCase;
    private final ReportEnterFilmDetailUseCase mReportEnterFilmDetailUseCase;
    private final ReportExitFilmDetailUseCase mReportExitFilmDetailUseCase;

    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    @NonNull
    private final String mFilmId;
    private final String mDataDir;

    private boolean mFirstLoad = true;
    private Film mFilm;
    private Subscription mQrPaySubscription;
    private Observable<Film> mGetFilmObs;

    public FilmDetailPresenter(
            @Nullable String filmId, String dataDir, @NonNull FilmDetailContract.View view,
            @NonNull GetFilmDetailUseCase getFilmDetailUseCase,
            @NonNull GetMovieRecommendUseCase getMovieRecommendUseCase,
            @NonNull GetValidOrderUseCase getValidOrderUseCase,
            @NonNull GetUserWalletUseCase getUserWalletUseCase,
            @NonNull GetUserCreditWalletUseCase getUserCreditWalletUseCase,
            @NonNull PurchaseUseCase purchaseFilmUseCase,
            @NonNull GetUserInfoUseCase getUserInfoUseCase,
            @NonNull GetPlaybackValidityUseCase getPlaybackValidityUseCase,
            @NonNull GetDownloadTaskInfoUseCase getDownloadTaskInfoUseCase,
            @NonNull GetPlayTicketUseCase getPlayTicketUseCase,
            @NonNull ReportEnterFilmDetailUseCase reportEnterFilmDetailUseCase,
            @NonNull ReportExitFilmDetailUseCase reportExitFilmDetailUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        mFilmId = filmId;
        mDataDir = dataDir;
        mGetFilmDetailUseCase = checkNotNull(getFilmDetailUseCase,
                "GetFilmDetailUseCase cannot be null!");
        mGetMovieRecommendUseCase = checkNotNull(getMovieRecommendUseCase,
                "GetMovieRecommendUseCase cannot be null!");
        mGetValidOrderUseCase = checkNotNull(getValidOrderUseCase,
                "GetValidOrderUseCase cannot be null!");
        mPurchaseFilmUseCase = checkNotNull(purchaseFilmUseCase,
                "PurchaseUseCase cannot be null!");
        mGetUserWalletUseCase = checkNotNull(getUserWalletUseCase,
                "GetUserWalletUseCase cannot be null!");
        mGetUserCreditWalletUseCase = checkNotNull(getUserCreditWalletUseCase,
                "GetUserCreditWalletUseCase cannot be null!");
        mGetUserInfoUseCase = checkNotNull(getUserInfoUseCase,
                "GetUserInfoUseCase cannot be null!");
        mGetPlayTicketUseCase = checkNotNull(getPlayTicketUseCase,
                "GetPlayTicketUseCase cannot be null!");
        mSchedulerProvider = checkNotNull(schedulerProvider,
                "BaseSchedulerProvider cannot be null!");
        mGetPlaybackValidityUseCase = checkNotNull(getPlaybackValidityUseCase,
                "GetPlaybackValidityUseCase cannot be null!");
        mGetDownloadTaskInfoUseCase = checkNotNull(getDownloadTaskInfoUseCase,
                "GetDownloadTaskInfoUseCase cannot be null!");
        mReportEnterFilmDetailUseCase = checkNotNull(reportEnterFilmDetailUseCase,
                "reportEnterFilmDetailUseCase cannot be null!");
        mReportExitFilmDetailUseCase = checkNotNull(reportExitFilmDetailUseCase,
                "reportExitFilmDetailUseCase cannot be null!");
        attachView(checkNotNull(view, "filmDetailView cannot be null!"));
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        loadFilmDetail(false);
    }

    @Override
    public void loadFilmDetail(boolean forceUpdate) {
        // Simplification for sample: a network reload will be forced on first load.
        loadFilmDetail(forceUpdate || mFirstLoad, true);
        mFirstLoad = false;
    }


    @Override
    public void purchaseFilm(final String mediaId) {
        _purchaseFilm(mediaId, false);
    }

    @Override
    public void creditPay() {
        _purchaseFilm("", true);
    }

    @Override
    public void registerVip() {
        getView().setLoadingIndicator(true);
        EspressoIdlingResource.increment();

        // #1. check credit expire
        // #2. show register VIP UI

        // #1. check credit expire
        Subscription subscription = getCreditWalletObservable(false)
                .flatMap(new Func1<Wallet, Observable<Wallet>>() {
                    @Override
                    public Observable<Wallet> call(Wallet wallet) {
                        boolean creditExpire = checkAndShowCreditExpire(wallet, true);
                        if (creditExpire) {
                            // end
                            return Observable.empty();
                        }
                        return Observable.just(wallet);
                    }
                })
                .subscribe(new Subscriber<Wallet>() {
                    @Override
                    public void onCompleted() {
                        EspressoIdlingResource.decrement();

                        // The view may not be able to handle UI updates anymore
                        FilmDetailContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "registerVip, onError : ");
                        EspressoIdlingResource.decrement();

                        // The view may not be able to handle UI updates anymore
                        FilmDetailContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                            view.showCommonException(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(Wallet wallet) {
                        FilmDetailContract.View view = getView();
                        if (view != null && view.isActive()) {
                            // #2. show UI
                            view.showRegisterVipUI();
                        }
                    }
                });

        addSubscription(subscription);
    }

    @Override
    public void playFilm(String mediaId) {
        _playFilm(mediaId);
    }

    @Override
    public void playTrailer(String name, String url, String posterUrl) {
        if (StringUtils.isNullOrEmpty(url)) {
            getView().showNoTrailer();
            return;
        }
        doPlay(mFilmId, null, name, true, Media.TYPE_NO_ENCRYPT, url, null, null, null, posterUrl,
                true);
    }

    @Override
    public void download(@NonNull String mediaId, @NonNull String mediaUrl, boolean reDownload) {
        Logger.d("download, mediaId : " + mediaId);
        FilmDetailContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

        view.showDownloadUI(mFilmId, mediaId, mediaUrl, reDownload);

        // 1. get all storage devices, if not device, show no storage device UI and return.
        // 2. get download info
        // 2.1 if not exist, try get history backup download info.
        // 2.1.2 show confirm restore history download? if restore, show select restore download UI.
        //       if not, go create new download task(re-download).
        // 2.2. if download info exist, auto resume/pause the download task. if resume, check
        //      whether storage exist, if not exist, show confirm re-download UI.
    }

    @Override
    public void updateDownloadInfo(final DownloadTaskInfo downloadTaskInfo) {
        FilmDetailContract.View view = getView();
        if (null == downloadTaskInfo || null == view || !view.isActive()) {
            return;
        }
        _updateDownloadInfo(downloadTaskInfo);
    }

    @Override
    public void reportEnterFilmDetail(@Nullable final String source,
            @Nullable final String status) {
//        getFilmDetail()
//                .onErrorReturn(new Func1<Throwable, Film>() {
//                    @Override
//                    public Film call(Throwable throwable) {
//                        return null;
//                    }
//                })
//                .concatMap(new Func1<Film, Observable<?>>() {
//                    @Override
//                    public Observable<?> call(@Nullable Film film) {
//                        String filmName = film != null ? film.getName() : "";
//                        return mReportEnterFilmDetailUseCase.run(new ReportEnterFilmDetailUseCase
//                                .RequestValues(source, filmName, mFilmId, status));
//                    }
//                })
//                .subscribe();
    }

    @Override
    public void reportExitFilmDetail(@Nullable final String to, @Nullable final String duration,
            @Nullable final String status) {
//        getFilmDetail()
//                .onErrorReturn(new Func1<Throwable, Film>() {
//                    @Override
//                    public Film call(Throwable throwable) {
//                        return null;
//                    }
//                })
//                .concatMap(new Func1<Film, Observable<?>>() {
//                    @Override
//                    public Observable<?> call(@Nullable Film film) {
//                        String filmName = film != null ? film.getName() : "";
//                        return mReportExitFilmDetailUseCase.run(new ReportExitFilmDetailUseCase
//                                .RequestValues(mFilmId, filmName, to, duration, status));
//                    }
//                })
//                .subscribe();
    }

    private void loadFilmDetail(boolean forceUpdate, final boolean showLoadingUI) {
        Logger.d("loadFilmDetail, forceUpdate : " + forceUpdate);

        if (StringUtils.isNullOrEmpty(mFilmId)) {
            getView().showMissingFilm();
            return;
        }

        if (showLoadingUI) {
            getView().setLoadingIndicator(true);
        }

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        // #1. Get film detail
        // #2. Show film base info
        // #3. Base on film detail and orders, show views

        Observable<List<MovieRecommendFilm>> getRecommendFilmsObs = mGetMovieRecommendUseCase.run(
                new GetMovieRecommendUseCase.RequestValues(mFilmId))
                .onErrorReturn(new Func1<Throwable, GetMovieRecommendUseCase.ResponseValue>() {
                    @Override
                    public GetMovieRecommendUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "loadFilmDetail, get movie recommend onErrorReturn : ");
                        return null;
                    }
                })
                .map(new Func1<GetMovieRecommendUseCase.ResponseValue, List<MovieRecommendFilm>>() {
                    @Override
                    public List<MovieRecommendFilm> call(GetMovieRecommendUseCase.ResponseValue
                            responseValue) {
                        if (responseValue != null) {
                            return responseValue.getContentList();
                        }

                        return null;
                    }
                });

        // #1. Get film detail
        Subscription subscription =
                Observable.zip(getFilmDetail(), getRecommendFilmsObs,
                        new Func2<Film, List<MovieRecommendFilm>, FilmAndRecommendFilms>() {
                            @Override
                            public FilmAndRecommendFilms call(Film film,
                                    List<MovieRecommendFilm> movieRecommentFilms) {
                                return new FilmAndRecommendFilms(film, movieRecommentFilms);
                            }
                        })
                        .subscribe(new Subscriber<FilmAndRecommendFilms>() {
                            @Override
                            public void onCompleted() {
                                Logger.d("loadFilmDetail, onCompleted");
                                EspressoIdlingResource.decrement();

                                // The view may not be able to handle UI updates anymore
                                FilmDetailContract.View view = getView();
                                if (null == view || !view.isActive()) {
                                    return;
                                }

                                if (showLoadingUI) {
                                    view.setLoadingIndicator(false);
                                }

                                view.showLoadFilmSuccess();
                            }

                            @Override
                            public void onError(Throwable e) {
                                EspressoIdlingResource.decrement();
                                Logger.e(e, "loadFilmDetail, onError : ");

                                // The view may not be able to handle UI updates anymore
                                FilmDetailContract.View view = getView();
                                if (null == view || !view.isActive()) {
                                    return;
                                }

                                if (showLoadingUI) {
                                    view.setLoadingIndicator(false);
                                }

//                                if (e instanceof RestApiException) {
//                                    RestApiException exception = (RestApiException) e;
//                                    view.showRestApiException(exception.getType(),
//                                            exception.getNote(),
//                                            exception.getNoteMsg());
//                                }

                                UniteThrowable t = UniteThrowable.handleException(e);
                                if (UniteThrowable.ErrorType.UNKNOWN == t.errorType) {
                                    view.showLoadFilmFailed(e.getMessage());
                                } else {
                                    view.showError(t.errorType, t.errorCode, t.errorMsg);
                                }
                            }

                            @Override
                            public void onNext(FilmAndRecommendFilms obj) {
                                // The view may not be able to handle UI updates anymore
                                FilmDetailContract.View view = getView();
                                if (null == view || !view.isActive()) {
                                    return;
                                }

                                Film film = obj.getFilm();
                                List<MovieRecommendFilm> recommendFilms = obj.getRecommendFilms();

                                // #2. Show obj base info
                                showFilmBaseInfo(film, recommendFilms);
                                // #3. Base on obj detail and orders, show views
                                updateView(true);
                            }
                        });


//        // #1. Get film detail & Get valid order
//        // #2. Base on film detail and orders, show views
//        Subscription subscription = Observable.zip(obsGetFilmDetail, obsGetOrders,
//                new Func2<Film, List<Order>, FilmAndOrders>() {
//                    @Override
//                    public FilmAndOrders call(Film film, List<Order> orderList) {
//                        return new FilmAndOrders(film, orderList);
//                    }
//                })
//                .subscribe(new Subscriber<FilmAndOrders>() {
//                    @Override
//                    public void onCompleted() {
//                        EspressoIdlingResource.decrement();
//
//                        // The view may not be able to handle UI updates anymore
//                        if (!getView().isActive()) {
//                            return;
//                        }
//
//                        if (showLoadingUI) {
//                            getView().setLoadingIndicator(false);
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        EspressoIdlingResource.decrement();
//
//                        Log.e(TAG, "loadFilmDetail, onError : ", e);
//
//                        // The view may not be able to handle UI updates anymore
//                        if (!getView().isActive()) {
//                            return;
//                        }
//
//                        if (showLoadingUI) {
//                            getView().setLoadingIndicator(false);
//                        }
//
//                        if (e instanceof RestApiException) {
//                            RestApiException exception = (RestApiException) e;
//                            getView().showRestApiException(exception.getType(), exception
// .getNote(),
//                                    exception.getNoteMsg());
//                        }
//
//                        getView().showLoadFilmFailed();
//                    }
//
//                    @Override
//                    public void onNext(FilmAndOrders responseValue) {
//                        // The view may not be able to handle UI updates anymore
//                        if (!getView().isActive()) {
//                            return;
//                        }
//                        mFilm = responseValue.getFilm();
//                        List<Order> orders = responseValue.getOrderList();
//                        showFilmBaseInfo(mFilm);
////                        updatePlayOrPurchaseView(mFilm, orders, null);
//                        updateView();
//                    }
//                });

        addSubscription(subscription);
    }

    /**
     * Purchase film.
     *
     * @param mediaId   The media id. If it is <code>null<code/>, then pick up the first media of
     *                  the film.
     * @param creditPay Whether use credit payment.
     */
    private void _purchaseFilm(@Nullable final String mediaId, final boolean creditPay) {
        Logger.d("_purchaseFilm");
        getView().setPurchasingIndicator(true);

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        Subscription subscription;

        // #0. check credit wallet and if credit expire, show expire view and return
        // #1. get and check storage capacity if purchase download media
        // #2. get user info
        // #3. filter media by id & get user wallet
        // #4. show purchase UI
        // #5. filter purchase result True
        // #6. get valid order
        // #7. check whether the corresponding order is exist and valid
        // #8. update view and show purchase success

        final ObjectWarp<Film> filmCache = new ObjectWarp<>();
        final ObjectWarp<Media> mediaCache = new ObjectWarp<>();
        final ObjectWarp<UserInfo> userInfoCache = new ObjectWarp<>();
        final ObjectWarp<Order> orderCache = new ObjectWarp<>();
        final ObjectWarp<List<Order>> validOrderListCache = new ObjectWarp<>();
        final ObjectWarp<String> downloadPath = new ObjectWarp<>();

        final Observable<Media> obsFindMedia = getFilmDetail()
                .observeOn(mSchedulerProvider.io())
                .concatMap(new Func1<Film, Observable<Media>>() {
                    @Override
                    public Observable<Media> call(Film film) {
                        filmCache.setObject(film);
                        Media media;
                        if (StringUtils.isNullOrEmpty(mediaId)) {
                            media = film.getMedias().get(0);
                        } else {
                            media = filterMedia(film.getMedias(), mediaId);
                        }
                        // if media not found
                        if (null == media) {
                            // pick the first one
                            media = film.getMedias().get(0);
                        }
                        mediaCache.setObject(media);
                        return Observable.just(media);
                    }
                })
                .observeOn(mSchedulerProvider.ui());

        // get credit wallet
        subscription = getCreditWalletObservable(false)
                // check credit wallet
                .concatMap(new Func1<Wallet, Observable<Media>>() {
                    @Override
                    public Observable<Media> call(Wallet wallet) {
                        boolean creditExpire = checkAndShowCreditExpire(wallet, true);
                        if (creditExpire) {
                            // end
                            return Observable.empty();
                        }

                        // credit pay
                        if (creditPay) {
                            String creditLimit = wallet.getCreditLine();
                            if (!StringUtils.isNullOrEmpty(creditLimit)) {
                                BigDecimal limit = BigDecimal.ZERO;
                                try {
                                    limit = new BigDecimal(creditLimit);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                // 0 < limit
                                if (BigDecimal.ZERO.compareTo(limit) < 0) {
                                    int days = 0;
                                    String creditDeadLineDays = wallet.getCreditDeadLineDays();
                                    if (!StringUtils.isNullOrEmpty(creditDeadLineDays)) {
                                        try {
                                            days = Integer.parseInt(creditDeadLineDays);
                                        } catch (NumberFormatException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    FilmDetailContract.View view = getView();
                                    // show credit pay notice
                                    return view.showCreditPayNotice(limit.doubleValue(), days)
                                            .concatMap(new Func1<Boolean, Observable<Media>>() {
                                                @Override
                                                public Observable<Media> call(Boolean aBoolean) {
                                                    // ignore the result
                                                    return obsFindMedia;
                                                }
                                            });
                                }
                            }
                        }

                        return obsFindMedia;
                    }
                })
                .observeOn(mSchedulerProvider.ui())
                // get and check storage capacity if purchase download media
                .concatMap(new Func1<Media, Observable<List<StorageUtils.StorageInfo>>>() {
                    @Override
                    public Observable<List<StorageUtils.StorageInfo>> call(Media media) {
                        FilmDetailContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        // online
                        if (media != null && (StringUtils.isNullOrEmpty(media.getType())
                                || Media.MEDIA_TYPE_ONLINE.equals(media.getType()))) {
                            return Observable.just(null);
                        }

                        Film film = filmCache.getObject();
                        long mediaSize = -1;
                        if (media != null && !StringUtils.isNullOrEmpty(media.getSize())) {
                            try {
                                mediaSize = Long.parseLong(media.getSize());
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                        final List<Media> medias = film.getMedias();
                        final List<Media> downloadMedias = new ArrayList<>();
                        for (Media m : medias) {
                            String type = m.getType();
                            // filter download media
                            if (!StringUtils.isNullOrEmpty(type)
                                    && Media.MEDIA_TYPE_DOWNLOAD.equals(type)) {
                                downloadMedias.add(m);
                            }
                        }
                        if (-1 == mediaSize) {
                            // min media size
                            mediaSize = getMinMediaFileSize(downloadMedias);
                        }

                        if (0 > mediaSize) {
                            mediaSize = 0;
                        }

                        final List<StorageUtils.StorageInfo> storageList =
                                getStorageList();
                        // no storage device
                        if (null == storageList || storageList.isEmpty()) {
                            // show no storage device
                            view.showDownloadNoStorage(mediaSize, true);
                            return Observable.empty();
                        }

                        boolean isCapacityEnough = isSdcardCapacityEnough(film);
                        if (!isCapacityEnough) {
                            // show space not enough
                            view.showDownloadCapacityNoEnough(mediaSize, true);
                            return Observable.empty();
                        }

                        // show select download media and path
                        return view.showSelectDownloadMediaAndPathView(mFilmId, downloadMedias,
                                storageList)
                                .concatMap(
                                        new Func1<MediaAndPath, Observable<List<StorageUtils
                                                .StorageInfo>>>() {
                                            @Override
                                            public Observable<List<StorageUtils.StorageInfo>> call(
                                                    MediaAndPath mediaAndPath) {
                                                String mediaId = mediaAndPath.getMediaId();
                                                String path = mediaAndPath.getPath();
                                                Media media = filterMedia(downloadMedias, mediaId);
                                                mediaCache.setObject(media);
                                                downloadPath.setObject(path);
                                                return Observable.just(storageList);
                                            }
                                        });
                    }
                })
                // get user info
                .concatMap(new Func1<List<StorageUtils.StorageInfo>, Observable<UserInfo>>() {
                    @Override
                    public Observable<UserInfo> call(List<StorageUtils.StorageInfo> storageInfos) {
                        return getUserInfo(false);
                    }
                })
                // get wallet
                .concatMap(new Func1<UserInfo, Observable<MediaAndWallet>>() {
                    @Override
                    public Observable<MediaAndWallet> call(UserInfo userInfo) {
                        userInfoCache.setObject(userInfo);
                        // #1. filter media by id & get user wallet
                        return Observable.zip(obsFindMedia, getUserWallet(false),
                                new Func2<Media, Wallet, MediaAndWallet>() {
                                    @Override
                                    public MediaAndWallet call(Media media, Wallet wallet) {
                                        return new MediaAndWallet(media, wallet);
                                    }
                                });
                    }
                })
                // switch to UI thread
                .observeOn(mSchedulerProvider.ui())
                // show view to user to confirm purchase
                .concatMap(new Func1<MediaAndWallet, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(MediaAndWallet mediaAndWallet) {
                        FilmDetailContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        Media media = mediaAndWallet.getMedia();
//                        Wallet wallet = mediaAndWallet.getWallet();

                        final boolean isOnline = !StringUtils.isNullOrEmpty(media.getType())
                                && Media.MEDIA_TYPE_ONLINE.equals(media.getType());
                        Film film = filmCache.getObject();
                        String price = getPrice(film, isOnline, userInfoCache.getObject().isVIP());

//                        double balance = 0;
//                        if (!StringUtils.isNullOrEmpty(wallet.getValue())) {
//                            try {
//                                balance = Double.parseDouble(wallet.getValue());
//                            } catch (NumberFormatException e) {
//                                e.printStackTrace();
//                            }
//                        }

                        if (StringUtils.isNullOrEmpty(price)) {
                            price = "0";
                        }

                        String productType = isOnline ? Order.PRODUCT_TYPE_THEATRE_ONLINE
                                : Order.PRODUCT_TYPE_THEATRE_DOWNLOAD;

                        return view.showPurchaseFilmUI(film.getName(), media.getId(), isOnline,
                                media.getEncryption(), productType, media.getRankname(), price,
                                creditPay);
                    }
                })
                // switch to IO thread
                .observeOn(mSchedulerProvider.io())
                // filter True
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return aBoolean != null && aBoolean;
                    }
                })
                // check valid order
                .concatMap(new Func1<Boolean, Observable<List<Order>>>() {
                    @Override
                    public Observable<List<Order>> call(Boolean aBoolean) {
                        return getValidOrders();
                    }
                })
                // check whether the corresponding order is exist and valid
                .filter(new Func1<List<Order>, Boolean>() {
                    @Override
                    public Boolean call(List<Order> orderList) {
                        validOrderListCache.setObject(orderList);
                        if (orderList != null && !orderList.isEmpty()) {
                            // filter the corresponding order
                            Media media = mediaCache.getObject();
                            Order order = filterOrderByMediaId(orderList, media.getId());
                            // exist and valid
                            if (order != null && order.isValid()) {
                                orderCache.setObject(order);
                                return true;
                            }
                        }
                        return false;
                    }
                })
                .concatMap(new Func1<List<Order>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(
                            List<Order> orderList) {
                        Media media = mediaCache.getObject();
                        Order order = orderCache.getObject();
                        return purchaseSuccessObs(order, media);
                    }
                })
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        Logger.d("_purchaseFilm, onCompleted");
                        EspressoIdlingResource.decrement();
                        FilmDetailContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setPurchasingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        EspressoIdlingResource.decrement();
                        Logger.e(e, "_purchaseFilm, onError : ");
                        FilmDetailContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setPurchasingIndicator(false);
                            view.showPurchaseFilmFailed(mediaId, e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(Boolean playOrDownload) {
                    }
                });

        addSubscription(subscription);
    }

    private Observable<Boolean> purchaseSuccessObs(@NonNull final Order order,
            @NonNull final Media media) {

        final String mediaId = media.getId();
        final String mediaType = media.getType();
        final String encryption = media.getEncryption();
        final String mediaUrl = media.getUrl();
        boolean isOnline = StringUtils.isNullOrEmpty(mediaType)
                || Media.MEDIA_TYPE_ONLINE.equals(mediaType);
        final String licenseId = isOnline ? Media.MEDIA_TYPE_ONLINE : Media.MEDIA_TYPE_DOWNLOAD;
        // get play ticket(server require to start the period of validity)
        return getPlayTicketObs(media, order, licenseId)
                .observeOn(mSchedulerProvider.ui())
                .doOnNext(new Action1<GetPlayTicketUseCase.ResponseValue>() {
                    @Override
                    public void call(GetPlayTicketUseCase.ResponseValue responseValue) {
                        // update view
                        updateView(true);
                    }
                })
                .concatMap(new Func1<GetPlayTicketUseCase.ResponseValue, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(GetPlayTicketUseCase.ResponseValue response) {
                        FilmDetailContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        Ticket ticket = null;
                        if (response != null && response.getTicket() != null) {
                            ticket = response.getTicket();
                        }

                        long remainTime = -1;
                        // ticket not null
                        if (ticket != null) {
                            String ticketValidation = ticket.getTicketvalidation();
                            if (!StringUtils.isNullOrEmpty(ticketValidation)) {
                                try {
                                    // hour
                                    long validation = Long.parseLong(ticketValidation);
                                    // millisecond
                                    remainTime = validation * 3600000L;
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (-1 == remainTime) {
                            // order not null
                            if (order != null && !StringUtils.isNullOrEmpty(order.getRemain())) {
                                try {
                                    // millisecond
                                    remainTime = Long.parseLong(order.getRemain());
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        // show purchase success
                        view.showPurchaseFilmSuccess(mediaId, remainTime);

                        int noticeType;
                        if (!StringUtils.isNullOrEmpty(encryption) && Media.TYPE_KDM.equals(
                                encryption)) {
                            noticeType = WATCH_TYPE_TONGBU;
                        } else {
                            noticeType = WATCH_TYPE_SHOUFA;
                        }

                        return onPurchaseSuccessObs(view, remainTime, noticeType, mediaType,
                                mediaId, mediaUrl);
                    }
                });
    }

    private Observable<Boolean> onPurchaseSuccessObs(FilmDetailContract.View view, long remainTime,
            int noticeType, final String mediaType, final String mediaId, final String mediaUrl) {
        // show film watch notice
        return view.showFilmWatchNotice(noticeType, remainTime)
                .subscribeOn(mSchedulerProvider.ui())
                .observeOn(mSchedulerProvider.io())
                .concatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean aBoolean) {
                        FilmDetailContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }
                        boolean isDownload = !StringUtils.isNullOrEmpty(mediaType)
                                && Media.MEDIA_TYPE_DOWNLOAD.equals(mediaType);
                        // download
                        if (isDownload) {
                            // get download task
                            return mGetDownloadTaskInfoUseCase.run(
                                    new GetDownloadTaskInfoUseCase.RequestValues(mFilmId, mediaId))
                                    .map(new Func1<GetDownloadTaskInfoUseCase
                                            .ResponseValue, Boolean>() {
                                        @Override
                                        public Boolean call(
                                                GetDownloadTaskInfoUseCase.ResponseValue response) {
                                            DownloadTaskInfo task = response.getDownloadTaskInfo();
                                            // download finish?
                                            return task != null && task.isFinish();
                                        }
                                    });
                        }

                        // just to play
                        return Observable.just(true);
                    }
                })
                .observeOn(mSchedulerProvider.ui())
                .doOnNext(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean playOrDownload) {
                        if (playOrDownload) { // play
                            playFilm(mediaId);
                        } else { // download
                            download(mediaId, mediaUrl, false);
                        }
                    }
                });
    }

    /**
     * Get play ticket.
     */
    private Observable<GetPlayTicketUseCase.ResponseValue> getPlayTicketObs(@NonNull Media media,
            @NonNull Order order, @NonNull String licenseId) {
        return mGetPlayTicketUseCase.run(
                new GetPlayTicketUseCase.RequestValues(mFilmId, media.getName(), order.getSerial(),
                        licenseId))
                // catch the error
                .onErrorReturn(
                        new Func1<Throwable, GetPlayTicketUseCase.ResponseValue>() {
                            @Override
                            public GetPlayTicketUseCase.ResponseValue call(
                                    Throwable throwable) {
                                Logger.e(throwable,
                                        "_purchaseFilm, get play ticket, " + "onErrorReturn : ");
                                return null;
                            }
                        });
    }

    private void _playFilm(final String mediaId) {
        Logger.d("playFilm, mediaId : " + mediaId);
        EspressoIdlingResource.increment(); // App is busy until further notice

        getView().setPreparePlayingIndicator(true);

        final ObjectWarp<Film> filmCache = new ObjectWarp<>();
        final ObjectWarp<Media> mediaCache = new ObjectWarp<>();

        GetFilmDetailUseCase.RequestValues requestValue =
                new GetFilmDetailUseCase.RequestValues(mFilmId);
        Subscription subscription = mGetFilmDetailUseCase.run(requestValue)
                .flatMap(new Func1<GetFilmDetailUseCase.ResponseValue, Observable<Media>>() {
                    @Override
                    public Observable<Media> call(
                            GetFilmDetailUseCase.ResponseValue responseValue) {
                        Film film = responseValue.getFilm();
                        Media media = filterMedia(film.getMedias(), mediaId);
                        filmCache.setObject(film);
                        mediaCache.setObject(media);
                        return Observable.just(media);
                    }
                })
                .flatMap(new Func1<Object, Observable<PlaybackValidity>>() {
                    @Override
                    public Observable<PlaybackValidity> call(Object o) {
                        GetPlaybackValidityUseCase.RequestValues requestValues =
                                new GetPlaybackValidityUseCase.RequestValues(mFilmId,
                                        mediaId, mDataDir);
                        return mGetPlaybackValidityUseCase.run(requestValues)
                                .map(new Func1<GetPlaybackValidityUseCase.ResponseValue,
                                        PlaybackValidity>() {
                                    @Override
                                    public PlaybackValidity call(
                                            GetPlaybackValidityUseCase.ResponseValue
                                                    responseValue) {
                                        return responseValue.getPlaybackValidity();
                                    }
                                });
                    }
                })
                .subscribe(new Subscriber<PlaybackValidity>() {
                    @Override
                    public void onCompleted() {
                        EspressoIdlingResource.decrement();
                        FilmDetailContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setPreparePlayingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        EspressoIdlingResource.decrement();
                        Logger.e(e, "_playFilm, onError : ");
                        FilmDetailContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setPreparePlayingIndicator(false);
//                            if (e instanceof RestApiException) {
//                                RestApiException exception = (RestApiException) e;
//                                view.showRestApiException(exception.getType(),
//                                        exception.getNote(), exception.getNoteMsg());
//                            }
                            // show play film failed
                            view.showPlayFilmException(PlaybackValidity.ERR_UNKNOWN, e.getMessage(),
                                    mediaId);
                        }
                    }

                    @Override
                    public void onNext(PlaybackValidity validPeriod) {
                        FilmDetailContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

//                        PlaybackValidity validPeriod = responseValue.getPlaybackValidity();
                        Logger.d("_playFilm, play back validity isValid : " + validPeriod.isValid()
                                + ", errCode : " + validPeriod.getErrorCode());
                        Media media = mediaCache.getObject();
                        if (validPeriod.isUnlimited() || validPeriod.isValid()) {
                            Film film = filmCache.getObject();
                            String name = film.getName();
                            String url = media.getUrl();
                            String type = media.getType();
                            String encryptionType = media.getEncryption();
                            String rank = media.getRank();
                            String posterUrl = film.getBigposter();
                            boolean isOnline = StringUtils.isNullOrEmpty(type)
                                    || Media.MEDIA_TYPE_ONLINE.equals(type);
                            // kdm
                            boolean isKDM = !StringUtils.isNullOrEmpty(encryptionType)
                                    && Media.TYPE_KDM.equals(encryptionType);
                            if (isKDM) {
                                url = validPeriod.getPlayUrl();
//                                doPlay(mFilmId, mediaId, name, isOnline, encryptionType, url,
//                                        null, film.getAdverts(), posterUrl, false);
                            }
//                            else {
                            doPlay(mFilmId, mediaId, name, isOnline, encryptionType, url,
                                    rank, film.getMedias(), film.getAdverts(), posterUrl, false);
//                            }
                        } else {
                            int errCode = validPeriod.getErrorCode();
                            // show play film failed
                            view.showPlayFilmException(errCode, null, mediaId);
                            switch (errCode) {
                                case PlaybackValidity.ERR_OVERDUE: // overdue
                                case PlaybackValidity.ERR_NO_VALID_ORDER: // no valid order
                                    // update view
                                    updateView(false);
                                    break;
                                case PlaybackValidity.ERR_DOWNLOAD_FILE_NOT_FOUND: // download
                                    // file not found


                                    // show download file missing
                                    showLocalPlayFileMissing(media);
                                    break;
                                case PlaybackValidity.ERR_DOWNLOAD_NO_TASK: // no download task
                                    // no break
                                case PlaybackValidity.ERR_DOWNLOAD_NOT_FINISH: // download not
                                    // finish

                                    // continue download
                                    download(mediaId, mediaCache.getObject().getUrl(), false);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                });
        addSubscription(subscription);
    }

    private void doPlay(String filmId, String mediaId, String name, boolean isOnline,
            String encryptionType, String url, String rank, List<Media> mediaList,
            List<Ad> advertList, String posterUrl, boolean isTrailer) {
        Logger.d("doPlay, filmId : " + filmId + ", mediaId : " + mediaId + ", name : " + name
                + ", isOnline : " + isOnline + ", encryptionType : " + encryptionType + ", url : "
                + url + ", mediaList : " + mediaList + ", advertList : " + advertList
                + ", posterUrl : " + posterUrl);
        boolean isKDM = !StringUtils.isNullOrEmpty(encryptionType)
                && Media.TYPE_KDM.equals(encryptionType);

//        String[] urls = {url};
        List<String> urls = new ArrayList<>();
        List<String> ranks = new ArrayList<>();
        if (!isKDM && mediaList != null && !mediaList.isEmpty()) {
            for (int i = 0; i < mediaList.size(); i++) {
//                if(mediaList.get(i).getUrl() != null
//                        && !url.equals(mediaList.get(i).getUrl())){
                urls.add(mediaList.get(i).getUrl());
                ranks.add(mediaList.get(i).getRank());
//                }
            }
        } else {
            urls.add(url);
            ranks.add(rank);
        }
        FilmDetailContract.View view = getView();
        if (view != null && view.isActive()) {
            view.navigateToPlayerActivity(filmId, mediaId, name, isOnline, encryptionType,
                    urls, ranks, advertList, posterUrl, isTrailer);
        }
    }

    /**
     * Show film base info
     *
     * @param film Film
     */
    private void showFilmBaseInfo(@NonNull Film film,
            @Nullable List<MovieRecommendFilm> recommendFilms) {
        FilmDetailContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

        long st = System.currentTimeMillis();

        String title = film.getName();
        String description = film.getIntroduction();
        view.showTitle(title);
        view.showDescription(description);
        view.showFilmPoster(film.getBigposter());
        view.showScore(film.getScore());
//            view.showYear(film.get);
        view.showCategory(film.getCategoryname());
        view.showDirector(film.getDirector());
        view.showActors(film.getActors());
        view.showCountry(film.getAreaname());
        view.showLanguage(film.getMovielangname());
        view.showDuration(film.getDuration());
        view.showWatchTime(film.getStarttime(), film.getEndtime());
        view.showCornerMark(film.getScriptType1(), film.getScriptType2(), film.getScriptColor2());

        // show recommend films
        if (recommendFilms != null && !recommendFilms.isEmpty()) {
            view.showRecommendPosters(recommendFilms);
        }

        // show trailers
        List<Video> videoList = film.getVideosList();
        if (videoList != null && !videoList.isEmpty()) {
            for (Video video : videoList) {
                view.showTrailer(film.getName(), video.getUrl());
            }
        } else {
            view.showTrailer(null, null);
        }

        Media onlineMedia = null;
        Media downloadMedia = null;
        List<Media> medias = film.getMedias();
        if (medias != null && !medias.isEmpty()) {
            onlineMedia = filterMediasByType(medias, Media.MEDIA_TYPE_ONLINE);
            downloadMedia = filterMediasByType(medias, Media.MEDIA_TYPE_DOWNLOAD);
        }

        boolean hasOnline = onlineMedia != null;
        boolean hasDownload = downloadMedia != null;

        String onlineprice = hasOnline ? film.getOnlineprice() : null;
        String viponlineprice = hasOnline ? film.getViponlineprice() : null;
        String downloadprice = hasDownload ? film.getDownloadprice() : null;
        String vipdownloadprice = hasDownload ? film.getVipdownloadprice() : null;
        // show price
        view.showPrice(onlineprice, viponlineprice, downloadprice, vipdownloadprice);

        // has download media
        if (downloadMedia != null) {
            // hide qr code pay
            view.setQrCodePayVisible(false);
        }

        long et = System.currentTimeMillis();
        Logger.d("showFilmBaseInfo, time : " + (et - st) + "ms");
    }

    /**
     * Update view
     *
     * @param needQueryOrders need to query orders? if set false, then show purchase view.
     */
    private void updateView(boolean needQueryOrders) {
        Logger.d("updateView, needQueryOrders : " + needQueryOrders);
        FilmDetailContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

        view.setUpdatingViewIndicator(true);
        EspressoIdlingResource.increment();

        final ObjectWarp<FilmAndOrdersAndUserInfoAndWallet> cache = new ObjectWarp<>();
        Subscription subscription = getFilmAndOrdersAndUserInfoAndWallet(needQueryOrders)
                .concatMap(new Func1<FilmAndOrdersAndUserInfoAndWallet,
                        Observable<GetDownloadTaskInfoUseCase.ResponseValue>>() {
                    @Override
                    public Observable<GetDownloadTaskInfoUseCase.ResponseValue> call(
                            FilmAndOrdersAndUserInfoAndWallet obj) {
                        cache.setObject(obj);
                        List<Order> orderList = obj.getOrderList();
                        if (orderList != null) {
                            Order order = filterOrders(orderList,
                                    Order.PRODUCT_TYPE_THEATRE_DOWNLOAD);
                            if (order != null) {
                                String mediaId = order.getMediaResourceId();
                                if (!StringUtils.isNullOrEmpty(mediaId)) {
                                    return getDownloadTaskObs(mediaId);
                                }
                            }
                        }
                        return Observable.just(null);
                    }
                })
                .subscribe(new Subscriber<GetDownloadTaskInfoUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        EspressoIdlingResource.decrement();
                        FilmDetailContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setUpdatingViewIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        EspressoIdlingResource.decrement();
                        Logger.e(e, "updateView, onError : ");
                        FilmDetailContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setUpdatingViewIndicator(false);
                            view.showUpdatingViewFailed(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(GetDownloadTaskInfoUseCase.ResponseValue responseValue) {
                        FilmDetailContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        FilmAndOrdersAndUserInfoAndWallet obj = cache.getObject();
                        Film film = obj.getFilm();
                        List<Order> orderList = obj.getOrderList();
                        UserInfo userInfo = obj.getUserInfo();
                        Wallet wallet = obj.getWallet();

                        DownloadTaskInfo taskInfo = null;
                        if (responseValue != null) {
                            taskInfo = responseValue.getDownloadTaskInfo();
                        }
                        updatePlayOrPurchaseView(film, orderList, userInfo, wallet, taskInfo);
                    }
                });

        addSubscription(subscription);
    }

    private void updatePlayOrPurchaseView(@NonNull Film film, @Nullable List<Order> orders,
            @NonNull UserInfo userInfo, Wallet wallet, @Nullable DownloadTaskInfo taskInfo) {
        long st = System.currentTimeMillis();

        FilmDetailContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

        List<Media> medias = film.getMedias();
        if (null == medias || medias.isEmpty()) {
            return;
        }

//        double onlinePrice = getPrice(film, true);
//        double downloadPrice = getPrice(film, false);
//        boolean isOnlinePriceFree = 0 == Double.compare(onlinePrice, 0);
//        boolean isDownloadPriceFree = 0 == Double.compare(downloadPrice, 0);
//
//        Media firstOnlineMedia = null;
//        Media firstDownloadMedia = null;
//        for (Media media : medias) {
//            String mediaType = media.getType();
//            if (StringUtils.isNullOrEmpty(mediaType)) {
//                continue;
//            }
//            boolean isOnline = Media.MEDIA_TYPE_ONLINE.equals(mediaType);
//            if (isOnline) {
//                firstOnlineMedia = media;
//            } else {
//                firstDownloadMedia = media;
//            }
//            if (firstOnlineMedia != null && firstDownloadMedia != null) {
//                break;
//            }
//        }
//
//        Order onlineOrder = null;
//        Order downloadOrder = null;
//        if (orders != null && !orders.isEmpty()) {
//            for (Order order : orders) {
//
//                if (!order.isValid()) {
//                    continue;
//                }
//
//                String productType = order.getProductType();
//                if (StringUtils.isNullOrEmpty(productType)) {
//                    continue;
//                }
//
//                if (Order.PRODUCT_TYPE_THEATRE_ONLINE.equals(productType)) {
//                    onlineOrder = order;
//                } else if (Order.PRODUCT_TYPE_THEATRE_DOWNLOAD.equals(productType)) {
//                    downloadOrder = order;
//                }
//
//                if (onlineOrder != null && downloadOrder != null) {
//                    break;
//                }
//            }
//        }
//
//        Media onlineMedia = null;
//        Media downloadMedia = null;
//        // has valid online order
//        if (onlineOrder != null) {
//            onlineMedia = filterMedia(medias, onlineOrder.getMediaResourceId());
//        }
//        // has valid download order
//        if (downloadOrder != null) {
//            downloadMedia = filterMedia(medias, downloadOrder.getMediaResourceId());
//        }

        boolean hasShowPlayView = false;

        // try show online play view
        hasShowPlayView |= tryShowPlayFilmView(film, orders, true, taskInfo);
        // try show download or local play view
        hasShowPlayView |= tryShowPlayFilmView(film, orders, false, taskInfo);

        // if not show play view
        if (!hasShowPlayView) {
            //try show purchase online view
            boolean hasOnline = tryShowPurchaseFilmView(film, userInfo, true, wallet);
            //try show purchase download view
            boolean hasDownload = tryShowPurchaseFilmView(film, userInfo, false, wallet);
        } else {
            // hide qr code pay
            hideQrCodePay();
        }

//        for (Media media : medias) {
//            String mediaId = media.getId();
//            if (StringUtils.isNullOrEmpty(mediaId)) {
//                continue;
//            }
//
//            Order order = null;
//            if (orders != null) {
//                order = filterOrderByMediaId(orders, mediaId);
//            }
//
//            showPlayView(film, media, order);
//        }

        long et = System.currentTimeMillis();
        Logger.d("updatePlayOrPurchaseView, time : " + (et - st) + "ms");
    }

    private boolean tryShowPurchaseFilmView(@NonNull Film film, @NonNull UserInfo userInfo,
            boolean isOnline, Wallet wallet) {
        List<Media> medias = film.getMedias();
        if (null == medias || medias.isEmpty()) {
            return false;
        }
        Media firstMedia;
        String mediaType = isOnline ? Media.MEDIA_TYPE_ONLINE : Media.MEDIA_TYPE_DOWNLOAD;
        // pick up the first media
        firstMedia = filterMediasByType(medias, mediaType);
        if (firstMedia != null) {
            showPurchaseView(film, firstMedia, isOnline, userInfo, wallet);
            return true;
        }
        return false;
    }

    /**
     * Try to show play film view
     *
     * @param isOnline If show, return true; otherwise, return false
     */
    private boolean tryShowPlayFilmView(@NonNull Film film, @Nullable List<Order> orders,
            boolean isOnline, @Nullable DownloadTaskInfo taskInfo) {

        List<Media> mediaList = film.getMedias();
        if (null == mediaList || mediaList.isEmpty()) {
            return false;
        }

        Order order = null;
        Media media = null;
        if (orders != null && !orders.isEmpty()) {
            String productType = isOnline ? Order.PRODUCT_TYPE_THEATRE_ONLINE
                    : Order.PRODUCT_TYPE_THEATRE_DOWNLOAD;
            // get the valid order
            order = filterOrders(orders, productType);
        }

//        // has valid order
//        if (order != null && order.isValid()) {
//            // get the corresponding media
//            media = filterMedia(mediaList, order.getMediaResourceId());
//        }
//
//        // has order && has media
//        if (order != null && media != null) {
//            // show play
//            showPlayView(media.getId(), media.getRankname(), isOnline);
//            return true;
//        } else {
//            boolean isVip = userInfo.isVIP();
//            String price = getPrice(film, isOnline, isVip);
//            BigDecimal val = BigDecimal.ZERO;
//            try {
//                val = new BigDecimal(price);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            boolean isPriceFree = 0 == BigDecimal.ZERO.compareTo(val);
//            // price is free
//            if (isPriceFree) {
//                String mediaType = isOnline ? Media.MEDIA_TYPE_ONLINE : Media.MEDIA_TYPE_DOWNLOAD;
//                // get the corresponding media
//                media = filterMediasByType(mediaList, mediaType);
//                // has media
//                if (media != null) {
//                    // show play
//                    showPlayView(media.getId(), media.getRankname(), isOnline);
//                    return true;
//                }
//            }
//        }

        // has valid order
        if (order != null && order.isValid()) {

            // order has media resource id
            String mediaResourceId = order.getMediaResourceId();
            if (!StringUtils.isNullOrEmpty(mediaResourceId)) {
                // get the corresponding media
//                media = filterMedia(mediaList, mediaResourceId);
                String mediaType = isOnline ? Media.MEDIA_TYPE_ONLINE : Media.MEDIA_TYPE_DOWNLOAD;
                media = filterMedia(mediaList, mediaResourceId, mediaType);
            }

            // media not found
            if (null == media) {
                String mediaType = isOnline ? Media.MEDIA_TYPE_ONLINE : Media.MEDIA_TYPE_DOWNLOAD;
                // pick up the first media
                media = filterMediasByType(mediaList, mediaType);
            }

            // has media
            if (media != null) {
                final String mediaId = media.getId();
                if (isOnline) {
                    // show play
                    showPlayView(media.getId(), media.getRankname(), isOnline);
                } else {
                    boolean downloadFinish = false;
                    if (taskInfo != null) {
                        String downloadMediaId = DownloadUtils.getMediaIdFromDownloadId(
                                taskInfo.getId());
                        if (!StringUtils.isNullOrEmpty(downloadMediaId) && mediaId.equals(
                                downloadMediaId)) {
                            downloadFinish = taskInfo.isFinish();
                        }
                    }
                    if (downloadFinish) {
                        // show play
                        showPlayView(media.getId(), media.getRankname(), isOnline);
                    } else {
                        // show download
                        showDownloadView(mediaId, media.getUrl(), taskInfo, false);
                    }
                }
                return true;
            }
        }

        return false;
    }

//    private void updatePlayOrPurchaseView(@NonNull Film film, @NonNull Media media,
//            @Nullable Order order) {
//        FilmDetailContract.View view = getView();
//        String mediaId = media.getId();
//        String rankname = media.getRankname();
//        boolean isOnline = Media.MEDIA_TYPE_ONLINE.equals(media.getType());
//        double price = getPrice(film, isOnline, false);
//        boolean isPriceFree = 0 == Double.compare(price, 0);
//
//        // order is valid
//        if (isPriceFree || order != null && order.isValid()) {
//            showPlayView(mediaId, rankname, isOnline);
//        } else {
//            showPurchaseView(film, mediaId, rankname, isOnline);
//        }
//    }

    private void showPurchaseView(@NonNull Film film, Media media, final boolean isOnline,
            @NonNull UserInfo userInfo, final Wallet wallet) {
        FilmDetailContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

        String mediaId = media.getId();
        String rankName = media.getRank();
//        String mediaType = media.getType();
        String encryptionType = media.getEncryption();
//        String mediaUrl = media.getUrl();

//        String priceStr = isOnline ? film.getOnlineprice() : film.getDownloadprice();
        String priceStr = String.valueOf(getPrice(film, isOnline, userInfo.isVIP()));
        if (StringUtils.isNullOrEmpty(priceStr)) {
            priceStr = "";
        }

        // show purchase film
        view.showPurchaseFilm(mediaId, isOnline, rankName, priceStr);
        // hide all play film
        view.hideAllPlayFilms();

        BigDecimal walletBalance = BigDecimal.ZERO;
        if (wallet != null && !StringUtils.isNullOrEmpty(wallet.getValue())) {
            try {
                walletBalance = new BigDecimal(wallet.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        BigDecimal priceDecimal = BigDecimal.ZERO;
        if (!StringUtils.isNullOrEmpty(priceStr)) {
            try {
                priceDecimal = new BigDecimal(priceStr);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        BigDecimal creditLimit = BigDecimal.ZERO;
        if (wallet != null && !StringUtils.isNullOrEmpty(wallet.getCreditLine())) {
            try {
                creditLimit = new BigDecimal(wallet.getCreditLine());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // not VIP && creditLimit > 0 && walletBalance < price && not KDM media
        boolean canShowCreditPay = !userInfo.isVIP() && creditLimit.compareTo(BigDecimal.ZERO) > 0
                && priceDecimal.compareTo(walletBalance) > 0
                && (StringUtils.isNullOrEmpty(encryptionType)
                || !Media.TYPE_KDM.equals(encryptionType));

        // show or hide credit pay
        view.setCreditPayViewVisible(canShowCreditPay);

        //not vip
        if (!userInfo.isVIP()) {
            view.setRegisterVipVisible(true);
            view.setRegisterVip(film.getOnlineprice(), film.getViponlineprice(),
                    film.getDownloadprice(), film.getVipdownloadprice());
        }

        tryShowQrCodePay(film, media, isOnline, wallet, priceStr, walletBalance, priceDecimal);
    }

    /**
     * Try to show qr code pay view.
     */
    private void tryShowQrCodePay(@NonNull final Film film, final Media media,
            final boolean isOnline, final Wallet wallet, final String priceStr,
            final BigDecimal walletBalance, final BigDecimal priceDecimal) {
        FilmDetailContract.View view = getView();

        // first, hide qr code pay
//        hideQrCodePay();

        if (!isOnline || BigDecimal.ZERO.compareTo(priceDecimal) >= 0) {
            return;
        }

        // is online && price is not free
        List<Media> mediaList = film.getMedias();
        if (null == mediaList || mediaList.isEmpty()) {
            return;
        }

        // get download media
        Media dlMedia = filterMediasByType(mediaList, Media.MEDIA_TYPE_DOWNLOAD);
        // has download media
        if (dlMedia != null) {
            return;
        }

        // check credit wallet
        boolean creditExpire = checkAndShowCreditExpire(wallet, false);
        // credit expire
        if (creditExpire) {
            return;
        }

//        if (mQrPaySubscription != null) {
//            mQrPaySubscription.unsubscribe();
//        }

        final String mediaId = media.getId();
        final String encryptionType = media.getEncryption();

        // show qr code pay
        Logger.d("ShowQrCodePay");
        view.setQrCodePayVisible(true);
        if (mQrPaySubscription != null) {
            mQrPaySubscription.unsubscribe();
        }
        mQrPaySubscription = view.showQrCodePay(film.getName(), priceStr,
                QrcodeContract.ALI_WECHAT_MODE_BOTH)
                // filter pay success
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return aBoolean != null && aBoolean;
                    }
                })
                // show purchase indicator
                .doOnNext(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        FilmDetailContract.View view = getView();
                        if (view != null && view.isActive()) {
                            // hide qr code pay
                            hideQrCodePay();
                            Logger.d("qr code, show purchase indicator");
                            // show purchase indicator
                            view.setPurchasingIndicator(true);
                        }
                    }
                })
                // purchase
                .concatMap(new Func1<Boolean, Observable<PurchaseUseCase.ResponseValue>>() {
                    @Override
                    public Observable<PurchaseUseCase.ResponseValue>
                    call(Boolean aBoolean) {
                        // wallet balance < 0, then use credit pay
                        boolean creditPay = BigDecimal.ZERO.compareTo(walletBalance) > 0;
                        String productType = isOnline ? Order.PRODUCT_TYPE_THEATRE_ONLINE
                                : Order.PRODUCT_TYPE_THEATRE_DOWNLOAD;
                        return mPurchaseFilmUseCase.run(
                                new PurchaseUseCase.RequestValues(mFilmId, productType, mediaId,
                                        encryptionType, isOnline, 1, wallet.getCurrency(),
                                        creditPay));
                    }
                })
                .concatMap(new Func1<PurchaseUseCase.ResponseValue, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(PurchaseUseCase.ResponseValue response) {
                        Order order = null;
                        if (response != null && response.getPayOrderResult() != null) {
                            order = response.getPayOrderResult().getOrder();
                        }
                        // has order
                        if (order != null) {
                            return purchaseSuccessObs(order, media);
                        }

                        String productType = isOnline ? Order.PRODUCT_TYPE_THEATRE_ONLINE
                                : Order.PRODUCT_TYPE_THEATRE_DOWNLOAD;
                        // get valid orders
                        return mGetValidOrderUseCase.run(
                                new GetValidOrderUseCase.RequestValues(mFilmId, productType))
                                .map(new Func1<GetValidOrderUseCase.ResponseValue, Order>() {
                                    @Override
                                    public Order call(GetValidOrderUseCase.ResponseValue response) {
                                        List<Order> orders = response.getOrders();
                                        if (orders != null) {
                                            for (Order order : orders) {
                                                String id = order.getMediaResourceId();
                                                // find the order
                                                if (!StringUtils.isNullOrEmpty(id) && id.equals(
                                                        mediaId)) {
                                                    return order;
                                                }
                                            }
                                        }
                                        return null;
                                    }
                                })
                                .concatMap(new Func1<Order, Observable<? extends Boolean>>() {
                                    @Override
                                    public Observable<? extends Boolean> call(
                                            @Nullable Order order) {
                                        return purchaseSuccessObs(order, media);
                                    }
                                });
                    }
                })
                .subscribeOn(mSchedulerProvider.ui())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        Logger.d("showQrCodePay, onCompleted");
                        FilmDetailContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setPurchasingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "showQrCodePay, onError : ");
                        FilmDetailContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setPurchasingIndicator(false);
                            view.showPurchaseFilmFailed(mediaId, e.getMessage());
                            // show qr code pay again
                            tryShowQrCodePay(film, media, isOnline, wallet, priceStr, walletBalance,
                                    priceDecimal);
                        }
                    }

                    @Override
                    public void onNext(Boolean value) {
//                        FilmDetailContract.View view = getView();
//                        if (null == view || !view.isActive()) {
//                            return;
//                        }
//
//                        if (value != null && value.getPayOrderResult() != null) {
//                            PayOrderResult payOrderResult = value.getPayOrderResult();
//                            String need = payOrderResult.getNeeded();
//                            // pay success
//                            if (StringUtils.isNullOrEmpty(need)) {
//                                Order order = payOrderResult.getOrder();
//                                long remainTime = -1;
//                                if (order != null && !StringUtils.isNullOrEmpty(
//                                        order.getRemain())) {
//                                    try {
//                                        remainTime = Long.parseLong(order.getRemain());
//                                    } catch (NumberFormatException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//
//                                // show purchase success
//                                view.showPurchaseFilmSuccess(mediaId, remainTime);
//
//                                int noticeType;
//                                String encryption = encryptionType;
//                                if (!StringUtils.isNullOrEmpty(encryption) && Media.TYPE_KDM
// .equals(
//                                        encryption)) {
//                                    noticeType = WATCH_TYPE_TONGBU;
//                                } else {
//                                    noticeType = WATCH_TYPE_SHOUFA;
//                                }
//
//                                // show film watch notice
//                                view.showFilmWatchNotice(noticeType, remainTime);
//                            }
//                        }
//
//                        // update view
//                        updateView();
                    }
                });
        addSubscription(mQrPaySubscription);
    }

    /**
     * Hide qr code pay
     */
    private void hideQrCodePay() {
//        if (mQrPaySubscription != null) {
//            mQrPaySubscription.unsubscribe();
//        }
        FilmDetailContract.View view = getView();
        if (view != null && view.isActive()) {
            // hide qr code pay
            view.setQrCodePayVisible(false);
        }
    }

    private void showPlayView(String mediaId, String rankname, boolean isOnline) {
        FilmDetailContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }
        // show play film
        view.showPlayFilm(mediaId, isOnline, rankname);
        // hide all purchase film
        view.hideAllPurchaseFilms();
        // local play
        if (!isOnline) {
            // hide download
            view.hideDownload();
        }
        // hide credit pay view
        view.setCreditPayViewVisible(false);
        // hide vip view
        view.setRegisterVipVisible(false);
    }

    private String getPrice(@NonNull Film film, boolean isOnline, boolean isVip) {
        String onlineprice = isVip ? film.getViponlineprice() : film.getOnlineprice();
        String downloadprice = isVip ? film.getVipdownloadprice() : film.getDownloadprice();
        String priceStr = isOnline ? onlineprice : downloadprice;
//        double price = 0;
//        if (!StringUtils.isNullOrEmpty(priceStr)) {
//            try {
//                price = Double.parseDouble(priceStr);
//            } catch (NumberFormatException e) {
//                e.printStackTrace();
//            }
//        }
        return priceStr;
    }

    /**
     * Check whether credit has expired and if it expired then show expire view.
     *
     * @param wallet         credit wallet
     * @param showExpireView allow show expire view when credit has expired.
     * @return whether credit has expired.
     */
    private boolean checkAndShowCreditExpire(@NonNull Wallet wallet, boolean showExpireView) {
        boolean expired = wallet.isCreditExpired();
        if (expired && showExpireView) {
            showCreditPayExpiredView(wallet);
        }
        return expired;
    }

    private void showCreditPayExpiredView(@NonNull Wallet wallet) {
        int deadLineDays = 0;
        BigDecimal creditBill = BigDecimal.ZERO;
        BigDecimal creditLimit = BigDecimal.ZERO;

        if (!StringUtils.isNullOrEmpty(wallet.getCreditDeadLineDays())) {
            try {
                deadLineDays = Integer.parseInt(wallet.getCreditDeadLineDays());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (!StringUtils.isNullOrEmpty(wallet.getValue())) {
            try {
                creditBill = new BigDecimal(wallet.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!StringUtils.isNullOrEmpty(wallet.getCreditLine())) {
            try {
                creditLimit = new BigDecimal(wallet.getCreditLine());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FilmDetailContract.View view = getView();
        if (view != null && view.isActive()) {
            // show credit pay expire
            Subscription subscription = view.showCreditPayExpire(deadLineDays,
                    creditBill.abs().doubleValue(), creditLimit.doubleValue())
                    .subscribeOn(mSchedulerProvider.ui())
                    .subscribe(new Subscriber<Boolean>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            Logger.e(e, "showCreditPayExpire, onError : ");
                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                            // refund success
                            if (aBoolean != null && aBoolean) {
                                // update view
                                updateView(true);
                            }
                        }
                    });
            addSubscription(subscription);
        }
    }

    private void _updateDownloadInfo(final DownloadTaskInfo downloadTaskInfo) {
        final String downloadId = downloadTaskInfo.getId();
        Subscription subscription = Observable.just(downloadId)
                // get film id
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String downloadId) {
                        return DownloadUtils.getFilmIdFromDownloadId(downloadId);
                    }
                })
                // filter current film
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(final String filmId) {
                        return !StringUtils.isNullOrEmpty(filmId) && filmId.equals(mFilmId);
                    }
                })
                // get media
                .flatMap(new Func1<String, Observable<Media>>() {
                    @Override
                    public Observable<Media> call(String filmId) {
                        // get media id
                        final String mediaId = DownloadUtils.getMediaIdFromDownloadId(downloadId);
                        if (StringUtils.isNullOrEmpty(mediaId)) {
                            return Observable.empty();
                        }
                        // get media
                        return getFilmDetail()
                                .observeOn(mSchedulerProvider.io())
                                .flatMap(new Func1<Film, Observable<Media>>() {
                                    @Override
                                    public Observable<Media> call(Film film) {
                                        return Observable.just(
                                                filterMedia(film.getMedias(), mediaId));
                                    }
                                });
                    }
                })
                .subscribeOn(mSchedulerProvider.io())
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<Media>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "_updateDownloadInfo, onError : ");
                    }

                    @Override
                    public void onNext(Media media) {
                        FilmDetailContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        if (null == media) {
                            return;
                        }

                        String mediaId = media.getId();
                        boolean downloadFinish = downloadTaskInfo.isFinish();
                        if (downloadFinish) {
                            String type = media.getType();
                            boolean isOnline = StringUtils.isNullOrEmpty(type)
                                    || Media.MEDIA_TYPE_ONLINE.equals(type);
                            // show play view
                            showPlayView(mediaId, media.getRankname(), isOnline);
                        } else {
                            showDownloadView(mediaId, media.getUrl(), downloadTaskInfo, true);
                        }
                    }
                });

        addSubscription(subscription);
    }

    private void showDownloadView(String mediaId, String mediaUrl,
            DownloadTaskInfo downloadTaskInfo, boolean canShowErrView) {
        FilmDetailContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

        // show download
        view.showDownload(mediaId, mediaUrl, downloadTaskInfo, canShowErrView);
        // hide all play film
        view.hideAllPlayFilms();
        // hide all purchase film
        view.hideAllPurchaseFilms();
        // hide credit pay view
        view.setCreditPayViewVisible(false);
        // hide vip view
        view.setRegisterVipVisible(false);
    }

    private void showLocalPlayFileMissing(final Media media) {
        final FilmDetailContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }
        Subscription subscription = view.showLocalPlayFileMissing()
                .subscribeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Logger.e(e, "showLocalPlayFileMissing, onError : ");
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean != null && aBoolean) {
                            view.showDownloadUI(mFilmId, media.getId(), media.getUrl(), true);
                        }
                    }
                });
        addSubscription(subscription);
    }

    private Observable<Film> getFilmDetail() {
        if (mFilm != null) {
            return Observable.just(mFilm);
        } else {
            if (null == mGetFilmObs) {
                synchronized (this) {
                    if (null == mGetFilmObs) {
                        GetFilmDetailUseCase.RequestValues requestValue =
                                new GetFilmDetailUseCase.RequestValues(mFilmId);
                        mGetFilmObs = mGetFilmDetailUseCase.run(requestValue)
                                .map(new Func1<GetFilmDetailUseCase.ResponseValue, Film>() {
                                    @Override
                                    public Film call(
                                            GetFilmDetailUseCase.ResponseValue responseValue) {
                                        Film film = responseValue.getFilm();
                                        mFilm = film;
                                        return film;
                                    }
                                })
                                // cache 1
                                .replay(1)
                                //
                                .refCount();
                    }
                }
            }
            return mGetFilmObs;
        }
    }

    private Observable<Media> getMediaObservable(final String mediaId) {
        return getFilmDetail()
                .flatMap(new Func1<Film, Observable<Media>>() {
                    @Override
                    public Observable<Media> call(Film film) {
                        return Observable.just(filterMedia(film.getMedias(), mediaId));
                    }
                });
    }

    private Observable<UserInfo> getUserInfo(boolean forceUpdate) {
        return mGetUserInfoUseCase.run(new GetUserInfoUseCase.RequestValues(forceUpdate))
                .map(new Func1<GetUserInfoUseCase.ResponseValue, UserInfo>() {
                    @Override
                    public UserInfo call(GetUserInfoUseCase.ResponseValue responseValue) {
                        return responseValue.getUserInfo();
                    }
                });
    }

    private Observable<List<Order>> getValidOrders() {
        GetValidOrderUseCase.RequestValues requestValues = new GetValidOrderUseCase.RequestValues(
                mFilmId, Order.PRODUCT_TYPE_THEATRE);
        return mGetValidOrderUseCase.run(requestValues)
                .map(new Func1<GetValidOrderUseCase.ResponseValue, List<Order>>() {
                    @Override
                    public List<Order> call(GetValidOrderUseCase.ResponseValue responseValue) {
                        List<Order> orders = responseValue.getOrders();
                        if (orders != null && !orders.isEmpty() && orders.get(0) != null) {
                            OrderManager.getInstance().addOrder(mFilmId, orders.get(0));
                        }
                        return orders;
                    }
                });
    }

    private Observable<Wallet> getUserWallet(boolean forceUpdate) {
        final GetUserWalletUseCase.RequestValues requestValues =
                new GetUserWalletUseCase.RequestValues(forceUpdate);
        return mGetUserWalletUseCase.run(requestValues)
                .map(new Func1<GetUserWalletUseCase.ResponseValue, Wallet>() {
                    @Override
                    public Wallet call(GetUserWalletUseCase.ResponseValue responseValue) {
                        return responseValue.getWallet();
                    }
                });
    }

    private Observable<Wallet> getCreditWalletObservable(boolean forceUpdate) {
        return mGetUserCreditWalletUseCase.run(
                new GetUserCreditWalletUseCase.RequestValues(forceUpdate))
                .map(new Func1<GetUserCreditWalletUseCase.ResponseValue, Wallet>() {
                    @Override
                    public Wallet call(GetUserCreditWalletUseCase.ResponseValue responseValue) {
                        return responseValue.getWallet();
                    }
                });
    }

    private Observable<FilmAndOrdersAndUserInfoAndWallet> getFilmAndOrdersAndUserInfoAndWallet(
            boolean needQueryOrders) {
        Observable<List<Order>> validOrders = needQueryOrders ? getValidOrders() : Observable.just(
                (List<Order>) null);
        return Observable.zip(getFilmDetail(), validOrders, getUserInfo(false),
                getCreditWalletObservable(false),
                new Func4<Film, List<Order>, UserInfo, Wallet, FilmAndOrdersAndUserInfoAndWallet>
                        () {
                    @Override
                    public FilmAndOrdersAndUserInfoAndWallet call(Film film, List<Order> orderList,
                            UserInfo userInfo, Wallet wallet) {
                        return new FilmAndOrdersAndUserInfoAndWallet(film, orderList, userInfo,
                                wallet);
                    }
                });
    }

    private Observable<GetDownloadTaskInfoUseCase.ResponseValue> getDownloadTaskObs(
            String mediaId) {
        return mGetDownloadTaskInfoUseCase.run(new GetDownloadTaskInfoUseCase
                .RequestValues(mFilmId, mediaId));
    }

    /**
     * Filter the order from The list of orders
     *
     * @param orders  The list of orders
     * @param mediaId The media id
     * @return The first order that match the <code>orderType<code/>
     */
    private Order filterOrderByMediaId(@NonNull final List<Order> orders,
            @NonNull final String mediaId) {
        checkNotNull(orders);
        checkNotNull(mediaId);

        for (Order order : orders) {
            String mediaResourceId = order.getMediaResourceId();
            // check media id
            if (!StringUtils.isNullOrEmpty(mediaResourceId) && mediaId.equals(mediaResourceId)) {
                // return the order
                return order;
            }
        }
        return null;
    }

    /**
     * Filter the order from The list of orders
     *
     * @param orders    The list of orders
     * @param orderType The order product type
     * @return The first order that match the <code>orderType<code/>
     */
    private Order filterOrders(@NonNull final List<Order> orders, @NonNull final String orderType) {
        checkNotNull(orders);
        checkNotNull(orderType);

        for (Order order : orders) {
            String productType = order.getProductType();
            // check order product type
            if (!StringUtils.isNullOrEmpty(productType) && orderType.equals(productType)) {
                // return the order
                return order;
            }
        }
        return null;
    }

    private Media filterMediasByType(@NonNull final List<Media> mediaList,
            @NonNull final String mediaType) {
        checkNotNull(mediaList);
        checkNotNull(mediaType);

        for (Media media : mediaList) {
            String type = media.getType();
            // check media product type
            if (!StringUtils.isNullOrEmpty(type) && mediaType.equals(type)) {
                // return the media
                return media;
            }
        }
        return null;
    }

    private Media filterMedia(@NonNull final List<Media> mediaList, @NonNull final String mediaId) {
        checkNotNull(mediaList);
        checkNotNull(mediaId);
        for (Media media : mediaList) {
            String id = media.getId();
            if (!StringUtils.isNullOrEmpty(id) && mediaId.equals(id)) {
                return media;
            }
        }
        return null;
    }

    private Media filterMedia(@NonNull final List<Media> mediaList, @NonNull final String mediaId,
            final @NonNull String mediaType) {
        checkNotNull(mediaList);
        checkNotNull(mediaId);
        for (Media media : mediaList) {
            String id = media.getId();
            if (!StringUtils.isNullOrEmpty(id) && mediaId.equals(id)) {
                final String type = media.getType();
                if (StringUtils.isNullOrEmpty(type) || !type.equals(mediaType)) {
                    Logger.w("filter a media by id but the type is not the same! Current "
                            + "media type : " + type + ", require : " + mediaType);
                    continue;
                }
                return media;
            }
        }
        return null;
    }

    /**
     * Is storage device available capacity enough for download
     */
    private boolean isSdcardCapacityEnough(Film film) {
        List<StorageUtils.StorageInfo> storageInfos = StorageUtils.getStorageList();
        if (null == storageInfos || storageInfos.isEmpty()) {
            return false;
        }
        boolean isEnough = false;
        List<Media> mediaList = film.getMedias();
//        long minFileSize = getMinMediaFileSize(mediaList);
        String filmId = film.getReleaseid();
        boolean hasMediaSize = false;
        for (Media media : mediaList) {
            if (StringUtils.isNullOrEmpty(media.getType()) || !Media.MEDIA_TYPE_DOWNLOAD.equals(
                    media.getType())) {
                continue;
            }
            String mediaId = media.getId();
            long mediaSize = 0;
            String size = media.getSize();
            if (!StringUtils.isNullOrEmpty(size)) {
                try {
                    mediaSize = Long.parseLong(size);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            if (0 == mediaSize) {
                continue;
            } else {
                hasMediaSize = true;
            }

            // first, get history backup download
            BackupDownloadInfo downloadInfo = DownloadUtils.getBackupDownloads(filmId, mediaId);
            if (downloadInfo != null && downloadInfo.getStorageInfoList() != null) {
                List<StorageUtils.StorageInfo> infoList = downloadInfo.getStorageInfoList();
                List<List<DownloadFileList>> downloadLists = downloadInfo.getDownloadFileList();
                for (int i = 0; i < infoList.size(); i++) {
                    List<DownloadFileList> dList = downloadLists.get(i);
                    long completeSize = 0;
                    long totalSize = 0;
                    long downloadLeftSize = 0;
                    for (DownloadFileList downloadFileList : dList) {
                        completeSize += downloadFileList.mCompleteSize;
                        completeSize += downloadFileList.mMd5CompleteSize;
                        totalSize += downloadFileList.mFileSize;
                        totalSize += downloadFileList.mMd5FileSize;
                    }
                    // left download zie
                    downloadLeftSize = totalSize - completeSize;
                    String path = infoList.get(i).path;
                    // get storage capacity
                    long availableSize = StorageUtils.getAvailableCapacity(path);
                    // capacity is enough
                    if (availableSize >= downloadLeftSize) {
                        isEnough = true;
                        break;
                    }
                }
            }

            // no enough space
            if (!isEnough) {
                // for each storage device
                for (StorageUtils.StorageInfo storage : storageInfos) {
                    String path = storage.path;
                    // get storage capacity
                    long availableSize = StorageUtils.getAvailableCapacity(path);
                    // capacity is enough
                    if (availableSize >= mediaSize) {
                        isEnough = true;
                        break;
                    }
                }
            }
        }

        // no media size found
        if (!hasMediaSize) {
            isEnough = true;
        }
        return isEnough;
    }

    private long getMinMediaFileSize(List<Media> mediaList) {
        long minFileSize = 0;
        for (Media media : mediaList) {
            String type = media.getType();
            if (!StringUtils.isNullOrEmpty(type) && Media.MEDIA_TYPE_DOWNLOAD.equals(type)) {
                try {
                    long fileSize = Long.parseLong(media.getSize());
                    if (fileSize < minFileSize) {
                        minFileSize = fileSize;
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return minFileSize;
    }

    private class FilmAndRecommendFilms {
        private final Film mFilm;
        private final List<MovieRecommendFilm> mRecommendFilms;

        private FilmAndRecommendFilms(Film film, List<MovieRecommendFilm> recommendFilms) {
            mFilm = film;
            mRecommendFilms = recommendFilms;
        }

        public Film getFilm() {
            return mFilm;
        }

        public List<MovieRecommendFilm> getRecommendFilms() {
            return mRecommendFilms;
        }
    }

    private class FilmAndOrders {
        private final Film mFilm;
        private final List<Order> mOrderList;

        FilmAndOrders(Film film, List<Order> orderList) {
            mFilm = film;
            mOrderList = orderList;
        }

        com.golive.network.entity.Film getFilm() {
            return mFilm;
        }

        List<Order> getOrderList() {
            return mOrderList;
        }
    }

    private class MediaAndWallet {
        private final Media mMedia;
        private final Wallet mWallet;

        MediaAndWallet(Media media, Wallet wallet) {
            mMedia = media;
            mWallet = wallet;
        }

        Media getMedia() {
            return mMedia;
        }

        Wallet getWallet() {
            return mWallet;
        }
    }

    private class UserInfoAndWallet {
        private final UserInfo mUserInfo;
        private final Wallet mWallet;

        public UserInfoAndWallet(UserInfo userInfo, Wallet wallet) {
            mUserInfo = userInfo;
            mWallet = wallet;
        }
    }

    private class FilmAndOrdersAndUserInfoAndWallet {
        private final Film mFilm;
        private final List<Order> mOrderList;
        private final UserInfo mUserInfo;
        private final Wallet mWallet;

        FilmAndOrdersAndUserInfoAndWallet(Film film,
                List<Order> orderList, UserInfo userInfo, Wallet wallet) {
            mFilm = film;
            mOrderList = orderList;
            mUserInfo = userInfo;
            mWallet = wallet;
        }

        Film getFilm() {
            return mFilm;
        }

        List<Order> getOrderList() {
            return mOrderList;
        }

        UserInfo getUserInfo() {
            return mUserInfo;
        }

        public Wallet getWallet() {
            return mWallet;
        }
    }
}
