package com.golive.cinema.player.kdm;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.golive.cinema.player.domain.model.PlaybackValidity;
import com.golive.cinema.util.StringUtils;
import com.golive.player.kdm.KDMDeviceID.CompanyType;
import com.golive.player.kdm.KDMMetadata;
import com.golive.player.kdm.KDMPlayer;
import com.golive.player.kdm.KDMRequest;
import com.golive.player.kdm.KDMResCode;
import com.initialjie.log.Logger;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * KDM common business.
 * <p/>
 * Created by Wangzj on 2016/9/21.
 */

public class KDM {

//    private final static CompanyType[] gCOMPANY_TYPES = new CompanyType[]{
//            CompanyType.OTHER, CompanyType.TCL, CompanyType.KONKA,
//            CompanyType.CHANGHONG, CompanyType.DOMY, CompanyType.TOSHIBA,
//            CompanyType.COOCAA, CompanyType.LETV};

    private static boolean sInit;
    private static boolean sIsReady;

    private final Context mContext;
    private CompanyType mCompanyType = CompanyType.OTHER;

    public static void init(Context context) {
        // has init || not ready
        if (isInit() || !isReady()) {
            return;
        }

        synchronized (KDM.class) {
            if (!isInit()) {
                Logger.d("init");
                // use application context 
                KDMRequest.init(context.getApplicationContext());
                setInit(true);
            }
        }
    }

    public static void unInit() {
        if (!isInit()) {
            return;
        }

        synchronized (KDM.class) {
            if (isInit()) {
                Logger.d("unInit");
                KDMRequest.uninit();
                setInit(false);
            }
        }
    }

    /**
     * Constructor for KDM.
     *
     * @param context Android Context. Suggested to be assigned with Android's {@link Application}
     *                .
     */
    public KDM(@NonNull Context context) {
        this(context, CompanyType.TCL);
    }

    /**
     * Constructor for KDM.
     *
     * @param context     Android Context. Suggested to be assigned with Android's {@link
     *                    Application} .
     * @param companyType CompanyType. See {@link CompanyType}.
     */
    public KDM(@NonNull Context context, CompanyType companyType) {
        context = context.getApplicationContext();
        mContext = checkNotNull(context, "Context cannot be null!");
        if (companyType != null) {
            mCompanyType = companyType;
        }

        tryInit();
    }

    /**
     * notify the KDM is ready and try to initialize the KDM if it has not been initialized.
     */
    public void notifyReady() {
        // set ready
        setReady(true);
        tryInit();
    }

    /**
     * Try to initialize the KDM if it has not been initialized.
     */
    private void tryInit() {
        if (!isInit()) {
            init(mContext);
        }
    }

    /**
     * Initialize KDM.
     *
     * @param regUrl        A Url for KDM registration.
     * @param forceRegister If set <code>true<code/>, force the KDM to register itself.
     * @return Observable of KDMResCode.
     */
    public Observable<KDMResCode> initKdm(final String regUrl, final boolean forceRegister) {
        Observable.OnSubscribe<KDMResCode> onSubscribe = new Observable.OnSubscribe<KDMResCode>() {
            @Override
            public void call(final Subscriber<? super KDMResCode> subscriber) {
                try {

                    // create kdm
                    KDMPlayer player = new KDMPlayer(getContext()) {

                        @Override
                        public void initKdmCallback(KDMResCode pResCode) {
                            super.initKdmCallback(pResCode);
                            Logger.d("initKdm, initKdmCallback");
                            if (subscriber.isUnsubscribed()) {
                                return;
                            }

                            // if success
                            if (KDMResCode.RESCODE_OK == pResCode.getResult()) {
                                // normal call back
                                subscriber.onNext(pResCode);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(new KdmException(pResCode));
                            }
                        }
                    };

                    // init kdm
                    int retVal = player.initKdm(getCompanyType(), regUrl, forceRegister);

                    // error
                    if (0 != retVal) {
                        Logger.e("initKdm, failed! Return value :  " + retVal);
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }

                        KDMResCode kdmResCode = new KDMResCode(KDMResCode.RESCODE_ERROR, retVal);
                        // error call back
                        subscriber.onError(new KdmException(kdmResCode));
                    }
                } catch (Throwable e) {
                    Logger.e(e, "initKdm, failed! Throwable ： ");
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }

                    // error call back
                    subscriber.onError(e);
                }
            }
        };

        // create Observable
        return Observable.create(onSubscribe);
    }

    /**
     * Get version of KDM.
     *
     * @return Observable of KDMResCode.
     */
    public Observable<KDMResCode> getKdmVersion() {
        Observable.OnSubscribe<KDMResCode> onSubscribe = new Observable.OnSubscribe<KDMResCode>() {
            @Override
            public void call(final Subscriber<? super KDMResCode> subscriber) {
                try {

                    // create kdm
                    KDMPlayer player = new KDMPlayer(getContext()) {

                        @Override
                        public void getVerCallback(KDMResCode pResCode) {
                            super.getVerCallback(pResCode);

                            if (subscriber.isUnsubscribed()) {
                                return;
                            }

                            // if success
                            if (KDMResCode.RESCODE_OK == pResCode.getResult()) {
                                // normal call back
                                subscriber.onNext(pResCode);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(new KdmException(pResCode));
                            }
                        }
                    };

                    // get version
                    int retVal = player.getVersion();

                    // error
                    if (0 != retVal) {
                        Logger.e("getKdmVersion, failed! Return value :  " + retVal);
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }

                        KDMResCode kdmResCode = new KDMResCode(KDMResCode.RESCODE_ERROR, retVal);
                        // error call back
                        subscriber.onError(new KdmException(kdmResCode));
                    }
                } catch (Throwable e) {
                    Logger.e(e, "getKdmVersion, failed! Throwable ： ");
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }

                    // error call back
                    subscriber.onError(e);
                }
            }
        };
        // create Observable
        return Observable.create(onSubscribe);
    }

    /**
     * Get media list of KDM.
     *
     * @param path File path that contains KDM medias.
     * @return Observable of List<KDMMetadata>.
     */
    public Observable<List<KDMMetadata>> getKdmMediaList(@NonNull final String path) {

        checkNotNull(path);

        Observable.OnSubscribe<List<KDMMetadata>> onSubscribe =
                new Observable.OnSubscribe<List<KDMMetadata>>() {
                    @Override
                    public void call(final Subscriber<? super List<KDMMetadata>> subscriber) {
                        try {

                            // create kdm
                            KDMPlayer player = new KDMPlayer(getContext()) {

                                @Override
                                public void getMovieListCallback(KDMResCode resCode,
                                        List<KDMMetadata> movieList) {
                                    super.getMovieListCallback(resCode, movieList);

                                    if (subscriber.isUnsubscribed()) {
                                        return;
                                    }

                                    // if success
                                    if (KDMResCode.RESCODE_OK == resCode.getResult()) {
                                        // normal call back
                                        subscriber.onNext(movieList);
                                        subscriber.onCompleted();
                                    } else {
                                        subscriber.onError(new KdmException(resCode));
                                    }
                                }
                            };

                            // get media list
                            int retVal = player.getMediaList(path);

                            // error
                            if (0 != retVal) {
                                Logger.e("getKdmMediaList, failed! Return value :  " + retVal);
                                if (subscriber.isUnsubscribed()) {
                                    return;
                                }

                                KDMResCode kdmResCode = new KDMResCode(KDMResCode.RESCODE_ERROR,
                                        retVal);
                                // error call back
                                subscriber.onError(new KdmException(kdmResCode));
                            }
                        } catch (Throwable e) {
                            Logger.e(e, "getKdmMediaList, failed! Throwable ： ");
                            if (subscriber.isUnsubscribed()) {
                                return;
                            }

                            // error call back
                            subscriber.onError(e);
                        }
                    }
                };
        // create Observable
        return Observable.create(onSubscribe);
    }

    public Observable<KDMResCode> setKdmTokenByPath(@NonNull final String path,
            @NonNull final String token) {
        checkNotNull(path);
        checkNotNull(token);

        return getKdmMediaList(path)
                .flatMap(new Func1<List<KDMMetadata>, Observable<KDMResCode>>() {
                    @Override
                    public Observable<KDMResCode> call(List<KDMMetadata> kdmMetadatas) {
                        if (kdmMetadatas != null && !kdmMetadatas.isEmpty()) {
                            String uuid = kdmMetadatas.get(0).getTitleUuid();
                            return setKdmTokenByUuid(uuid, token);
                        }
                        return null;
                    }
                });
    }

    /**
     * Set the ticket to KDM according to the uuid.
     *
     * @param uuid THe uuid of a KDM media. See {@link KDMMetadata}.
     * @return Observable of KDMResCode.
     */
    public Observable<KDMResCode> setKdmTokenByUuid(@NonNull final String uuid,
            @NonNull final String token) {

        checkNotNull(uuid);
        checkNotNull(token);

        /*
        * 1. Filter and transform the token if need.
        * 2. Do the actual setKDM job.
        * */
        return Observable.just(token)
                // step1
                .map(new Func1<String, String>() {

                    @Override
                    public String call(String str) {

                        String tokenStr = str;

                        if (!StringUtils.isNullOrEmpty(tokenStr)) {
                            final String prefix = "<![CDATA[";
                            if (!tokenStr.startsWith(prefix)) {
                                StringBuilder sbBuilder = new StringBuilder();
                                sbBuilder.append(prefix);
                                sbBuilder.append(tokenStr);
                                sbBuilder.append("]]>");
                                tokenStr = sbBuilder.toString();
                                sbBuilder.setLength(0);
                            }

                            tokenStr = tokenStr.replaceAll("&lt;", "<");
                            tokenStr = tokenStr.replaceAll("&gt;", ">");
                            tokenStr = tokenStr.replaceAll("&amp;", "&");
                            tokenStr = tokenStr.replaceAll("&apos;", "'");
                            tokenStr = tokenStr.replaceAll("&quot;", "\"");
                        }

                        return tokenStr;
                    }
                })
                // step2.
                .flatMap(new Func1<String, Observable<? extends KDMResCode>>() {

                    @Override
                    public Observable<? extends KDMResCode> call(String newToken) {
                        return _setKdmTokenByUuid(uuid, newToken);
                    }
                });
    }

    /**
     * Set the ticket to KDM according to the uuid.
     *
     * @return Observable of KDMResCode.
     */
    private Observable<KDMResCode> _setKdmTokenByUuid(@NonNull final String uuid,
            @NonNull final String token) {

        checkNotNull(uuid);
        checkNotNull(token);

        Observable.OnSubscribe<KDMResCode> onSubscribe = new Observable.OnSubscribe<KDMResCode>() {
            @Override
            public void call(final Subscriber<? super KDMResCode> subscriber) {
                try {

                    // create kdm
                    KDMPlayer player = new KDMPlayer(getContext()) {

                        @Override
                        public void setKDMKeyCallback(KDMResCode pResCode) {
                            super.setKDMKeyCallback(pResCode);

                            if (subscriber.isUnsubscribed()) {
                                return;
                            }

                            // if success
                            if (KDMResCode.RESCODE_OK == pResCode.getResult()) {
                                // normal call back
                                subscriber.onNext(pResCode);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(new KdmException(pResCode));
                            }
                        }
                    };

                    // set kem
                    int retVal = player.setKdmKey(uuid, token);

                    // error
                    if (0 != retVal) {
                        Logger.e("_setKdmTokenByUuid, failed! Return value :  " + retVal);
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }

                        KDMResCode kdmResCode = new KDMResCode(KDMResCode.RESCODE_ERROR, retVal);
                        // error call back
                        subscriber.onError(new KdmException(kdmResCode));
                    }
                } catch (Throwable e) {
                    Logger.e(e, "_setKdmTokenByUuid, failed! Throwable ： ");
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }

                    // error call back
                    subscriber.onError(e);
                }
            }
        };
        // create Observable
        return Observable.create(onSubscribe);
    }

    /**
     * Upgrade KDM.
     *
     * @param upgradeUrl The url for the upgrade
     * @return Observable of KDMResCode.
     */
    public Observable<KDMResCode> upgradeKdm(@NonNull final String upgradeUrl) {
        checkNotNull(upgradeUrl);

        Observable.OnSubscribe<KDMResCode> onSubscribe = new Observable.OnSubscribe<KDMResCode>() {
            @Override
            public void call(final Subscriber<? super KDMResCode> subscriber) {
                try {

                    // create kdm
                    KDMPlayer player = new KDMPlayer(getContext()) {

                        @Override
                        public void upgradeCallback(KDMResCode pResCode) {
                            super.upgradeCallback(pResCode);

                            if (subscriber.isUnsubscribed()) {
                                return;
                            }

                            // if success
                            if (KDMResCode.RESCODE_OK == pResCode.getResult()) {
                                // normal call back
                                subscriber.onNext(pResCode);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(new KdmException(pResCode));
                            }
                        }
                    };

                    // upgrade kem
                    int retVal = player.upgradeKdm(upgradeUrl);

                    // error
                    if (0 != retVal) {
                        Logger.e("upgradeKdm, failed! Return value :  " + retVal);
                        if (subscriber.isUnsubscribed()) {
                            return;
                        }

                        KDMResCode kdmResCode = new KDMResCode(KDMResCode.RESCODE_ERROR, retVal);
                        // error call back
                        subscriber.onError(new KdmException(kdmResCode));
                    }
                } catch (Throwable e) {
                    Logger.e(e, "upgradeKdm, failed! Throwable ： ");
                    if (subscriber.isUnsubscribed()) {
                        return;
                    }

                    // error call back
                    subscriber.onError(e);
                }
            }
        };
        // create Observable
        return Observable.create(onSubscribe);
    }

    /**
     * Get KDM playback status.
     *
     * @param path The file path that contains the KDM media.
     * @return Observable of PlaybackValidity.
     */
    public Observable<PlaybackValidity> getPlaybackValidity(@NonNull final String path) {

        checkNotNull(path);

        /*
        * 1. Get KDM medias.
        * 2. Map and transform KDMMetadata to PlaybackValidity.
        * */
        Observable<PlaybackValidity> observable;

        // step1
        observable = getKdmMediaList(path)
                // step2
                .map(new Func1<List<KDMMetadata>, PlaybackValidity>() {
                    @Override
                    public PlaybackValidity call(List<KDMMetadata> kdmMetadatas) {

                        if (null == kdmMetadatas || kdmMetadatas.isEmpty()) {
                            return PlaybackValidity.generatePlaybackNotValid(
                                    PlaybackValidity.ERR_MEDIAINFO_NOT_FOUND);
                        }

                        int leftCount = 0;
                        boolean isUnlimited = false;
                        boolean isValid = false;

                        final KDMMetadata titleMetadata = kdmMetadatas.get(0);
                        String authorizeCount = titleMetadata
                                .getTitleAuthorizeCount();
                        String startTime = titleMetadata.getTitleAuthorizeStart();
                        String expireTime = titleMetadata.getTitleAuthorizeExpire();
                        Logger.d("getPlaybackValidity, authorizeCount : "
                                + authorizeCount
                                + ", startTime : "
                                + startTime
                                + ", expireTime : "
                                + expireTime);
                        try {
                            // 获取剩余播放次数
                            leftCount = Integer.parseInt(authorizeCount);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // -1是KDM非加密影片，可以无限播放
                        if (-1 == leftCount) {
                            isUnlimited = true;
                            isValid = true;
                        }

                        if (!isUnlimited) {
                            // 剩余播放次数不为0 && 开始时间不为空
                            isValid = (leftCount != 0 && !StringUtils.isNullOrEmpty(startTime));
                        }

                        PlaybackValidity playbackValidity;
                        if (isUnlimited || isValid) {
                            playbackValidity = new PlaybackValidity(isUnlimited, isValid,
                                    PlaybackValidity.ERR_OK, leftCount, -1,
//                                    titleMetadata.getTitleUuid());
                                    path);
                        } else {
                            playbackValidity = PlaybackValidity.generatePlaybackNotValid(
                                    PlaybackValidity.ERR_OVERDUE);
                        }
                        return playbackValidity;
                    }
                });

        return observable;
    }

    private Context getContext() {
        return mContext;
    }

    public CompanyType getCompanyType() {
        return mCompanyType;
    }

    public synchronized static boolean isInit() {
        return sInit;
    }

    public synchronized static void setInit(boolean pGInit) {
        sInit = pGInit;
    }

    private synchronized static boolean isReady() {
        return sIsReady;
    }

    private synchronized static void setReady(boolean isReady) {
        sIsReady = isReady;
    }
}
