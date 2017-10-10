package com.golive.cinema.init;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.Constants;
import com.golive.cinema.ObjectWarp;
import com.golive.cinema.advert.domain.usecase.AdvertUseCase;
import com.golive.cinema.init.domain.usecase.BootImageUseCase;
import com.golive.cinema.init.domain.usecase.GetMainConfigUseCase;
import com.golive.cinema.init.domain.usecase.GetShutdownMessageUseCase;
import com.golive.cinema.init.domain.usecase.RepeatMacUseCase;
import com.golive.cinema.init.domain.usecase.UpgradeUseCase;
import com.golive.cinema.login.domain.usecase.LoginUseCase;
import com.golive.cinema.player.domain.usecase.GetKdmInitUseCase;
import com.golive.cinema.player.domain.usecase.GetKdmServerVersionUseCase;
import com.golive.cinema.player.domain.usecase.GetKdmVersionUseCase;
import com.golive.cinema.player.domain.usecase.NotifyKdmReadyUseCase;
import com.golive.cinema.player.domain.usecase.UpgradeKdmUseCase;
import com.golive.cinema.restapi.exception.RestApiException;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserHeadUseCase;
import com.golive.cinema.util.PackageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Ad;
import com.golive.network.entity.BootImage;
import com.golive.network.entity.Image;
import com.golive.network.entity.KDMServerVersion;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.RepeatMac;
import com.golive.network.entity.Upgrade;
import com.golive.network.response.AdvertResponse;
import com.golive.player.kdm.KDMPlayer;
import com.golive.player.kdm.KDMResCode;
import com.initialjie.log.Logger;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by chgang on 2016/10/27.
 */

public class InitPresenter extends BasePresenter<InitContract.View> implements
        InitContract.Presenter {

    private final GetMainConfigUseCase mGetMainConfigUseCase;
    private final GetShutdownMessageUseCase mGetShutdownMessageUseCase;
    private final BootImageUseCase mBootImageUseCase;
    private final UpgradeUseCase mUpgradeUseCase;
    private final RepeatMacUseCase mRepeatMacUseCase;
    private final LoginUseCase mLoginUseCase;
    private final GetUserHeadUseCase mGetUserHeadUseCase;
    private final GetKdmInitUseCase mGetKdmInitUseCase;
    private final GetKdmVersionUseCase mGetKdmVersionUseCase;
    private final GetKdmServerVersionUseCase mGetKdmServerVersionUseCase;
    private final UpgradeKdmUseCase mUpgradeKdmUseCase;
    private final NotifyKdmReadyUseCase mNotifyKdmReadyUseCase;
    private final AdvertUseCase mAdvertUseCase;

    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    public InitPresenter(@NonNull InitContract.View initView,
            @NonNull GetMainConfigUseCase getMainConfigUseCase,
            @NonNull GetShutdownMessageUseCase getShutdownMessageUseCase,
            @NonNull BootImageUseCase bootImageUseCase,
            @NonNull UpgradeUseCase upgradeUseCase,
            @NonNull RepeatMacUseCase repeatMacUseCase,
            @NonNull GetKdmInitUseCase getKdmInitUseCase,
            @NonNull GetKdmVersionUseCase getKdmVersionUseCase,
            @NonNull GetKdmServerVersionUseCase getKdmServerVersionUseCase,
            @NonNull UpgradeKdmUseCase upgradeKdmUseCase,
            @NonNull NotifyKdmReadyUseCase notifyKdmReadyUseCase,
            @NonNull LoginUseCase loginUseCase,
            @NonNull GetUserHeadUseCase getUserHeadUseCase,
            @NonNull AdvertUseCase advertUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        mGetMainConfigUseCase = checkNotNull(getMainConfigUseCase,
                "getMainConfigUseCase cannot be null!");
        mGetShutdownMessageUseCase = checkNotNull(getShutdownMessageUseCase);
        mBootImageUseCase = checkNotNull(bootImageUseCase, "bootImageUseCase cannot be null!");
        mUpgradeUseCase = checkNotNull(upgradeUseCase, "upgradeUseCase cannot be null!");
        mRepeatMacUseCase = checkNotNull(repeatMacUseCase, "repeatMacUseCase cannot be null!");
        mGetKdmInitUseCase = checkNotNull(getKdmInitUseCase, "getKdmInitUseCase cannot be null!");
        mGetKdmVersionUseCase = checkNotNull(getKdmVersionUseCase,
                "getKdmVersionUseCase cannot be null!");
        mGetKdmServerVersionUseCase = checkNotNull(getKdmServerVersionUseCase,
                "getKdmServerVersionUseCase cannot be null!");
        mUpgradeKdmUseCase = checkNotNull(upgradeKdmUseCase, "upgradeKdmUseCase cannot be null!");
        mNotifyKdmReadyUseCase = checkNotNull(notifyKdmReadyUseCase,
                "notifyKdmReadyUseCase cannot be null!");
        mLoginUseCase = checkNotNull(loginUseCase, "loginUseCase cannot be null!");
        mGetUserHeadUseCase = checkNotNull(getUserHeadUseCase,
                "GetUserHeadUseCase cannot be null!");
        mSchedulerProvider = checkNotNull(schedulerProvider,
                "BaseSchedulerProvider cannot be null!");
        mAdvertUseCase = checkNotNull(advertUseCase,
                "AdvertUseCase cannot be null!");
        attachView(checkNotNull(initView, "initView cannot be null!"));
        initView.setPresenter(this);
    }

    @Override
    public void init() {
        Logger.d("init");

        addSubscription(Observable.zip(getAdvertObs(), getServerAndLoginInit(),
                new Func2<Object, LoginUseCase.ResponseValue, LoginUseCase.ResponseValue>() {
                    @Override
                    public LoginUseCase.ResponseValue call(Object o,
                            LoginUseCase.ResponseValue responseValue) {
                        return responseValue;
                    }
                })
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Observer<LoginUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        Logger.d("init onCompleted---");
                        InitContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.showCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "init, onError : ");
                        InitContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        // IO exception
                        if (e instanceof IOException) {
                            view.showServerTimeout();
                        } else {
                            view.showInitFailed(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(LoginUseCase.ResponseValue responseValue) {
                        Logger.d("init, onNext---");
                        InitContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        if (responseValue != null) {
                            view.showLoginView(responseValue.getLogin());
                        }
                    }
                }));
    }

    private Observable<?> getAdvertObs() {
        // get adverts
        return getAdverts()
                .concatMap(new Func1<AdvertResponse, Observable<?>>() {
                    @Override
                    public Observable<?> call(AdvertResponse advertResponse) {
                        // has advert && these are not of GoLive
                        if (advertResponse != null && !advertResponse.isGolive) {
                            return Observable.just(advertResponse);
                        }

                        // get boot image
                        return getBootImage();
                    }
                })
                .onErrorReturn(new Func1<Throwable, Object>() {
                    @Override
                    public Object call(Throwable throwable) {
                        Logger.w(throwable, "getAdvertObs, onErrorReturn : ");
                        return null;
                    }
                })
                .concatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object obj) {
                        InitContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        if (obj != null && obj instanceof AdvertResponse) {
                            Logger.d("onNext--------------AdvertResponse");
                            AdvertResponse advertResponse = (AdvertResponse) obj;
                            List<AdvertResponse.AdsBean> adLists = advertResponse.getAds();
                            if (adLists != null && !adLists.isEmpty()) {
                                AdvertResponse.AdsBean adsBean = adLists.get(0);
                                int showTime = adsBean.getShow_time();
                                List<AdvertResponse.MaterialsBean> materialsBeanList
                                        = adsBean.getMaterials();
                                if (materialsBeanList != null && !materialsBeanList.isEmpty()) {
                                    AdvertResponse.MaterialsBean mBean = materialsBeanList.get(0);

                                    //test
//                                    mBean.setRes_type(Constants.MATERIAL_TYPE_VIDEO_DIGTAL_MEDIA);
//                                    mBean.setVideo("http://launchertest.oss.aliyuncs
// .com/material/94bbf26cda3b4144bbbe71d4321979a0.mp4?md5=a888c7fecccff7e1a66f933f38d03662");
//                                    adsBean.setShow_time(15);

                                    if (mBean != null) {
                                        Ad ad = new Ad();
                                        ad.setDuration(String.valueOf(showTime));
                                        ad.setThirdAdvert(true);
                                        ad.setAdCode(adsBean.getAd_code());
                                        ad.setMaterialCode(mBean.getMaterial_code());
                                        List<String> show_url = mBean.getShow_url();
                                        if (show_url != null && !show_url.isEmpty()) {
                                            ad.setReportUrl(show_url.get(0));
                                        }
                                        if (mBean.getRes_type()
                                                == Constants.MATERIAL_TYPE_IMAGE_DIGTAL_MEDIA) {
                                            ad.setType(Constants.ADVER_TYPE_IMAGE);
                                            ad.setUrl(mBean.getImage().getUrl());
                                        } else if (mBean.getRes_type()
                                                == Constants.MATERIAL_TYPE_VIDEO_DIGTAL_MEDIA) {
                                            ad.setType(Constants.ADVER_TYPE_VIDEO);
                                            ad.setUrl(mBean.getVideo());
                                        }
                                        return view.showConfirmAdvertView(ad);
                                    }
                                }
                            }
                        } else if (obj != null && obj instanceof BootImage) {
                            Logger.d("onNext--------------BootImage");
                            BootImage objImage = (BootImage) obj;
                            List<Image> imageList = objImage.getImageList();
                            if (imageList != null && !imageList.isEmpty()) {
                                Image image = imageList.get(0);
                                if (image != null) {
                                    Ad ad = new Ad();
                                    ad.setUrl(image.getUrl());
                                    ad.setDuration(image.getDuration());
                                    ad.setThirdAdvert(false);
                                    ad.setType(Constants.ADVER_TYPE_IMAGE);
                                    return view.showConfirmAdvertView(ad);
                                }
                            }
                        }

                        return Observable.just(obj);
                    }
                });
    }

    private Observable<LoginUseCase.ResponseValue> getServerAndLoginInit() {
        final String userId = "";
        final String password = "";
        final String status = "";
        final String branchtype = "";
        final ObjectWarp<Boolean> isKdmEnable = new ObjectWarp<>();

        // get main config
        return mGetMainConfigUseCase.run(new GetMainConfigUseCase.RequestValues(false))
                .concatMap(new Func1<GetMainConfigUseCase.ResponseValue,
                        Observable<? extends Object>>() {
                    @Override
                    public Observable<? extends Object> call(
                            GetMainConfigUseCase.ResponseValue responseValue) {
                        InitContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        MainConfig mainConfig = responseValue.getMainConfig();
                        boolean kdmEnable = mainConfig.isKdmEnable();
                        Logger.d("kdmEnable : " + kdmEnable);
                        isKdmEnable.setObject(kdmEnable);
                        // kdm enable by server?
                        if (kdmEnable) {
                            // set kdm ready
                            return mNotifyKdmReadyUseCase.run(null).concatMap(
                                    new Func1<NotifyKdmReadyUseCase.ResponseValue,
                                            Observable<?>>() {
                                        @Override
                                        public Observable<?> call(NotifyKdmReadyUseCase
                                                .ResponseValue responseValue) {
                                            // init KDM
                                            return initKdmObs();
                                        }
                                    });
                        }

                        // show kdm type
                        showKdmType(-1);
                        return Observable.just(null);
                    }
                })
                .concatMap(
                        new Func1<Object, Observable<GetShutdownMessageUseCase.ResponseValue>>() {
                            @Override
                            public Observable<GetShutdownMessageUseCase.ResponseValue> call(
                                    Object obj) {
                                // get server status
                                return mGetShutdownMessageUseCase.run(
                                        new GetShutdownMessageUseCase.RequestValues());
                            }
                        })
//                //请求发生异常
//                .onErrorReturn(new Func1<Throwable, ServerStatusUseCase.ResponseValue>() {
//                    @Override
//                    public ServerStatusUseCase.ResponseValue call(Throwable throwable) {
//                        Logger.w(throwable, "init, ServerStatusUseCase, onErrorReturn : ");
//                        InitContract.View view = getView();
//                        if (view != null && view.isActive()) {
//                            view.showServerShutdown(throwable.getMessage());
//                        }
//                        return null;
//                    }
//                })
                .concatMap(
                        new Func1<GetShutdownMessageUseCase.ResponseValue, Observable<Boolean>>() {
                            @Override
                            public Observable<Boolean> call(
                                    GetShutdownMessageUseCase.ResponseValue response) {
                                InitContract.View view = getView();
                                if (null == view || !view.isActive()) {
                                    return Observable.empty();
                                }

                                //有停机维护数据
                                if (response != null && response.getServerMessage() != null) {
                                    // show server stop
                                    return view.showServerStop(response.getServerMessage());
                                }

                                return Observable.just(true);
                            }
                        })
                .subscribeOn(mSchedulerProvider.io())
                .concatMap(new Func1<Boolean, Observable<UpgradeUseCase.ResponseValue>>() {
                    @Override
                    public Observable<UpgradeUseCase.ResponseValue> call(Boolean auto) {
//                        Logger.d("UpgradeUseCase call ----------------");
                        InitContract.View view = getView();
                        if (null == view || !view.isActive() || !auto) {
                            return Observable.empty();
                        }

                        // check upgrade
                        return mUpgradeUseCase.run(new UpgradeUseCase.RequestValues(
                                String.valueOf(view.getVersionCode()),
                                view.getVersionName())
                        );
                    }
                })
//                .onErrorReturn(new Func1<Throwable, UpgradeUseCase.ResponseValue>() {
//                    @Override
//                    public UpgradeUseCase.ResponseValue call(Throwable throwable) {
//                        Logger.w(throwable, "init, UpgradeUseCase, onErrorReturn : ");
//                        InitContract.View view = getView();
//                        if (view != null && view.isActive()) {
//                            view.showServerShutdown(throwable.getMessage());
//                        }
//                        return null;
//                    }
//                })
                .concatMap(new Func1<UpgradeUseCase.ResponseValue, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(UpgradeUseCase.ResponseValue responseValue) {
                        InitContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        if (responseValue != null && responseValue.getUpgrade() != null) {
                            Upgrade upgrade = responseValue.getUpgrade();
                            int remoteUpgradeType = Integer.valueOf(upgrade.getUpgradetype());
                            int remoteVersion = -1;
                            if (!StringUtils.isNullOrEmpty(upgrade.getVersioncode())) {
                                remoteVersion = Integer.valueOf(upgrade.getVersioncode());
                            }
                            int upgradeType = PackageUtils.checkLocalUpgrade(remoteUpgradeType,
                                    remoteVersion);

                            // show upgrade view
                            return view.showUpgradeView(upgrade, upgradeType);
                        }

                        return Observable.just(true);
                    }
                })
                .subscribeOn(mSchedulerProvider.io())
                .concatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean aBoolean) {
                        InitContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        final String mac = view.getMacAddress();
                        // 检查本地MAC是否合法
                        if (StringUtils.isValidMac(mac)) {
                            return Observable.just(true);
                        }

                        // show mac invalid
                        return view.showMacInvalidView("");
                    }
                })
                .concatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean obj) {
                        // 检查mac网络风险库
                        return mRepeatMacUseCase.run(new RepeatMacUseCase.RequestValues(null))
                                .concatMap(new Func1<RepeatMacUseCase.ResponseValue,
                                        Observable<Boolean>>() {
                                    @Override
                                    public Observable<Boolean> call(
                                            RepeatMacUseCase.ResponseValue responseValue) {
                                        InitContract.View view = getView();
                                        if (null == view || !view.isActive()) {
                                            return Observable.empty();
                                        }

                                        if (responseValue != null
                                                && responseValue.getRepeatMac() != null) {
                                            RepeatMac repeatMac = responseValue.getRepeatMac();
                                            String status = repeatMac.getStatus();
                                            if (status != null && !"0".equals(status)) {
                                                // show mac invalid
                                                return view.showMacInvalidView(status);
                                            }
                                        }

                                        return Observable.just(true);
                                    }
                                });
                    }
                })
//                .onErrorReturn(new Func1<Throwable, Boolean>() {
//                    @Override
//                    public Boolean call(Throwable throwable) {
//                        Logger.w(throwable, "init, RepeatMacUseCase onErrorReturn : ");
//                        InitContract.View view = getView();
//                        if (view != null && view.isActive()) {
//                            view.showServerShutdown(throwable.getMessage());
//                        }
//                        return null;
//                    }
//                })
                .subscribeOn(mSchedulerProvider.io())
                .concatMap(new Func1<Object, Observable<KDMResCode>>() {
                    @Override
                    public Observable<KDMResCode> call(Object o) {
                        // KDM is enable
                        if (isKdmEnable.getObject()) {
                            // check KDM upgrade
                            return checkUpgradeKdmObs()
                                    .concatMap(new Func1<Object, Observable<KDMResCode>>() {
                                        @Override
                                        public Observable<KDMResCode> call(Object o) {
                                            // get KDM version
                                            return getKdmVersion();
                                        }
                                    });
                        } else {
                            return Observable.just(null);
                        }
                    }
                })
                .concatMap(new Func1<KDMResCode, Observable<LoginUseCase.ResponseValue>>() {
                    @Override
                    public Observable<LoginUseCase.ResponseValue> call(
                            @Nullable KDMResCode kdmResCode) {
                        Logger.d("Login--------------");
                        String kdmVersion = null;
                        String kdmPlatform = null;
                        if (kdmResCode != null && kdmResCode.version != null) {
                            kdmVersion = kdmResCode.version.getVersion();
                            kdmPlatform = kdmResCode.version.getPlatform();
                        }
                        if (StringUtils.isNullOrEmpty(kdmVersion)) {
                            kdmVersion = "";
                        }
                        if (StringUtils.isNullOrEmpty(kdmPlatform)) {
                            kdmPlatform = "";
                        }

                        // login
                        return getLoginObs(userId, password, status, branchtype, kdmVersion,
                                kdmPlatform)
                                .observeOn(mSchedulerProvider.ui())
                                .onErrorReturn(new Func1<Throwable, LoginUseCase.ResponseValue>() {
                                    @Override
                                    public LoginUseCase.ResponseValue call(Throwable throwable) {
                                        Logger.w(throwable, "init, login, onErrorReturn : ");
                                        if (throwable != null && throwable.getMessage() != null) {
                                            InitContract.View view = getView();
                                            if (view != null && view.isActive()) {
                                                // show login failed
                                                view.showLoginFailedView(throwable.getMessage());
                                            }
                                        }
                                        return null;
                                    }
                                });
                    }
                })
                .concatMap(new Func1<LoginUseCase.ResponseValue, Observable<? extends LoginUseCase
                        .ResponseValue>>() {
                    @Override
                    public Observable<? extends LoginUseCase.ResponseValue> call(
                            final LoginUseCase.ResponseValue loginResponse) {
                        InitContract.View view = getView();
                        if (null == view || !view.isActive() || null == loginResponse) {
                            return Observable.empty();
                        }

                        // get user head
                        return getUserHeadObs()
                                .map(new Func1<GetUserHeadUseCase.ResponseValue, LoginUseCase
                                        .ResponseValue>() {
                                    @Override
                                    public LoginUseCase.ResponseValue call(
                                            GetUserHeadUseCase.ResponseValue response) {
                                        return loginResponse;
                                    }
                                });
                    }
                })
                .observeOn(mSchedulerProvider.ui());
    }

    private Observable<KDMResCode> getKdmVersion() {
        return mGetKdmVersionUseCase.run(new GetKdmVersionUseCase.RequestValues())
                .map(new Func1<GetKdmVersionUseCase.ResponseValue, KDMResCode>() {
                    @Override
                    public KDMResCode call(GetKdmVersionUseCase.ResponseValue responseValue) {
                        if (responseValue != null) {
                            return responseValue.getKDMResCode();
                        }
                        return null;
                    }
                })
                .onErrorReturn(new Func1<Throwable, KDMResCode>() {
                    @Override
                    public KDMResCode call(Throwable throwable) {
                        Logger.w(throwable, "getKdmVersion, onErrorReturn : ");
                        return null;
                    }
                });
    }

    private Observable<LoginUseCase.ResponseValue> getLoginObs(String userId, String password,
            String status, String branchtype, String kdmVersion, String kdmPlatform) {
        final LoginUseCase.RequestValues requestValues = new LoginUseCase.RequestValues(userId,
                password, status, branchtype, kdmVersion, kdmPlatform);
        // login
        return mLoginUseCase.run(requestValues)
                // retry
                .retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
                    @Override
                    public Observable<?> call(final Observable<? extends Throwable> errors) {
                        return getLoginRetryObs(errors);

                    }
                });
    }

    private Observable<?> getLoginRetryObs(Observable<? extends Throwable> errors) {
        // max retry time
        final int MAX_ATTEMPTS = 2;
        return errors.zipWith(Observable.range(1, MAX_ATTEMPTS + 1),
                new Func2<Throwable, Integer, Pair<Throwable, Integer>>() {
                    @Override
                    public Pair<Throwable, Integer> call(Throwable t, Integer i) {
                        return new Pair<>(t, i);
                    }
                })
                .concatMap(new Func1<Pair<Throwable, Integer>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Pair<Throwable, Integer> loginRetry) {
                        Throwable t = loginRetry.first;
                        int retryTime = loginRetry.second;
                        Logger.w("getLoginObs::retryWhen, retryTime : " + retryTime);

                        // reach max retry time
                        if (MAX_ATTEMPTS == retryTime) {
                            return Observable.error(t);
                        }

                        Logger.w(t, "getLoginObs::retryWhen, Throwable : ");

                        // rest api error
                        if (t instanceof RestApiException) {
                            RestApiException exception = (RestApiException) t;
                            String note = exception.getNote();
                            if (!StringUtils.isNullOrEmpty(note)) {
                                try {
                                    int errCode = Integer.parseInt(note);
                                    Logger.w("getLoginObs::retryWhen, errCode : " + errCode);
                                    // live key error || user no exist
                                    // || device not exist || user illegal
                                    if (Constants.RestApiError.ERR_USER_LIVE_KEY
                                            == errCode
                                            || Constants.RestApiError.ERR_USER_NOT_EXIST == errCode
                                            || Constants.RestApiError.ERR_USER_DEVICE_NOT_EXIST
                                            == errCode
                                            || Constants.RestApiError.ERR_USER_ILLEGAL == errCode) {
                                        InitContract.View view = getView();
                                        if (view != null && view.isActive()) {
                                            // clear user cache
                                            view.showClearUserCache();
                                            // retry
                                            return Observable.just(retryTime);
                                        }
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        // For anything else, don't retry
                        return Observable.error(t);
                    }
                })
                // delay retry
                .flatMap(new Func1<Integer, Observable<?>>() {
                    @Override
                    public Observable<?> call(Integer retryCount) {
                        return Observable.timer((long) Math.pow(200, retryCount),
                                TimeUnit.MILLISECONDS);
                    }
                });
    }

    private Observable<GetUserHeadUseCase.ResponseValue> getUserHeadObs() {
        return mGetUserHeadUseCase.run(new GetUserHeadUseCase.RequestValues(false))
                .onErrorReturn(new Func1<Throwable, GetUserHeadUseCase.ResponseValue>() {
                    @Override
                    public GetUserHeadUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getUserHeadObs, onErrorReturn : ");
                        return null;
                    }
                });
    }

    /**
     * Check whether need to upgrade kdm
     */
    private Observable<KDMResCode> checkUpgradeKdmObs() {
        // init kdm
        return initKdmObs()
                .concatMap(new Func1<GetKdmInitUseCase.ResponseValue,
                        Observable<GetKdmServerVersionUseCase.ResponseValue>>() {
                    @Override
                    public Observable<GetKdmServerVersionUseCase.ResponseValue> call(
                            GetKdmInitUseCase.ResponseValue responseValue) {
                        // get kdm server version
                        return mGetKdmServerVersionUseCase.run(
                                new GetKdmServerVersionUseCase.RequestValues());
                    }
                })
                .onErrorReturn(new Func1<Throwable, GetKdmServerVersionUseCase.ResponseValue>() {
                    @Override
                    public GetKdmServerVersionUseCase.ResponseValue call(
                            Throwable throwable) {
                        Logger.w(throwable, "checkUpgradeKdmObs, onErrorReturn : ");
                        return null;
                    }
                })
                .observeOn(mSchedulerProvider.ui())
                .concatMap(
                        new Func1<GetKdmServerVersionUseCase.ResponseValue,
                                Observable<KDMResCode>>() {
                            @Override
                            public Observable<KDMResCode> call(
                                    @Nullable GetKdmServerVersionUseCase.ResponseValue
                                            responseValue) {
                                if (responseValue != null) {
                                    KDMServerVersion kdmServerVersion =
                                            responseValue.getKDMServerVersion();
                                    String updateType = kdmServerVersion.getType();
                                    // no need to upgrade
                                    if (StringUtils.isNullOrEmpty(updateType)
                                            || KDMServerVersion.NONEED.equals(updateType)) {
                                        return Observable.just(null);
                                    }
                                    // show upgrading player
                                    InitContract.View view = getView();
                                    if (view != null && view.isActive()) {
                                        view.setPlayerUpgrading(true);
                                    }
                                    // upgrade kdm
                                    return upgradeKdmObs(kdmServerVersion);
                                }

                                return Observable.just(null);
                            }
                        })
                .onErrorReturn(new Func1<Throwable, KDMResCode>() {
                    @Override
                    public KDMResCode call(Throwable throwable) {
                        Logger.w(throwable, "upgradeKdmObs, onErrorReturn : ");
                        return null;
                    }
                })
                .observeOn(mSchedulerProvider.ui())
                .doOnNext(new Action1<KDMResCode>() {
                    @Override
                    public void call(@Nullable KDMResCode kdmResCode) {
                        InitContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        // hide upgrading player
                        view.setPlayerUpgrading(false);

                        if (kdmResCode != null) {
                            switch (kdmResCode.getResult()) {
                                case KDMResCode.RESCODE_OK:
                                    // show upgrade player success
                                    view.showPlayerUpgradeSuccess();
                                    break;
                                case KDMResCode.RESCODE_ERROR:
                                    // show upgrade player failed
                                    view.showPlayerUpgradeFailed(kdmResCode.getErrno());
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                });
    }

    /**
     * init KDM
     */
    private Observable<GetKdmInitUseCase.ResponseValue> initKdmObs() {
        return mGetKdmInitUseCase.run(new GetKdmInitUseCase.RequestValues(false))
                .onErrorReturn(new Func1<Throwable, GetKdmInitUseCase.ResponseValue>() {
                    @Override
                    public GetKdmInitUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "initKdmObs, onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetKdmInitUseCase.ResponseValue>() {
                    @Override
                    public void call(GetKdmInitUseCase.ResponseValue responseValue) {
                        InitContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        int kdmType = -1;
                        if (responseValue != null) {
                            KDMResCode kdmResCode = responseValue.getKDMResCode();
                            if (kdmResCode != null
                                    && KDMResCode.RESCODE_OK == kdmResCode.getResult()) {
                                KDMResCode.INIT init = kdmResCode.init;
                                if (init != null) {
                                    kdmType = init.getType();
                                }
                            }
                        }

                        // show kdm type
                        showKdmType(kdmType);
                    }
                });
    }

    private Observable<KDMResCode> upgradeKdmObs(KDMServerVersion kdmServerVersion) {
        return mUpgradeKdmUseCase.run(new UpgradeKdmUseCase
                .RequestValues(kdmServerVersion.getDownloadurl()))
                .map(new Func1<UpgradeKdmUseCase.ResponseValue, KDMResCode>() {
                    @Override
                    public KDMResCode call(UpgradeKdmUseCase.ResponseValue responseValue) {
                        return responseValue.getKDMResCode();
                    }
                });
    }


    private Observable<BootImage> getBootImage() {
        return mBootImageUseCase.run(new BootImageUseCase.RequestValues())
                .filter(new Func1<BootImageUseCase.ResponseValue, Boolean>() {
                    @Override
                    public Boolean call(BootImageUseCase.ResponseValue responseValue) {
                        return responseValue != null && responseValue.getBootImage() != null;
                    }
                })
                .map(new Func1<BootImageUseCase.ResponseValue, BootImage>() {
                    @Override
                    public BootImage call(BootImageUseCase.ResponseValue responseValue) {
                        return responseValue.getBootImage();
                    }
                });
    }

    private Observable<AdvertResponse> getAdverts() {
        return mAdvertUseCase.run(
                new AdvertUseCase.RequestValues(Constants.AD_REQUEST_TYPE_BOOT, null, null, null))
                .map(new Func1<AdvertUseCase.ResponseValue, AdvertResponse>() {
                    @Override
                    public AdvertResponse call(AdvertUseCase.ResponseValue responseValue) {
                        return responseValue.getAdvertResponse();
                    }
                });
    }

    /**
     * show kdm type
     */
    private void showKdmType(int kdmType) {
        InitContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

        Logger.d("showKdmType, kdmType : " + kdmType);
        String type;
        switch (kdmType) {
            case KDMPlayer.DOWNLOAD:
                type = com.golive.network.Constants.TYPE_CINEMA_SYNCHRONIZED_DOWNLOAD;
                break;
            case KDMPlayer.ONLINE:
                type = com.golive.network.Constants.TYPE_CINEMA_SYNCHRONIZED_ONLINE;
                break;
            case KDMPlayer.BOTH:
                type = com.golive.network.Constants.TYPE_CINEMA_SYNCHRONIZED_ONLINE_DOWNLOAD;
                break;
            default:
                type = com.golive.network.Constants.TYPE_CINEMA;
                break;
        }

        // show kdm type view
        view.showKdmType(type);
    }
}
