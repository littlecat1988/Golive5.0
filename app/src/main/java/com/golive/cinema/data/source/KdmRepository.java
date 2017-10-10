package com.golive.cinema.data.source;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.player.kdm.KdmException;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.KDMServerVersion;
import com.golive.player.kdm.KDMResCode;
import com.initialjie.log.Logger;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/12/16.
 */

public class KdmRepository implements KdmDataSource {

    @Nullable
    private static KdmRepository INSTANCE = null;

    @NonNull
    private final KdmDataSource mLocalDataSource;
    private final KdmDataSource mRemoteDataSource;
    private KDMResCode mCacheInitKdm;
    private boolean mCacheInitKdmIsDirty;
    private Observable<KDMResCode> mInitKdmObs;
    private boolean mForceRegister;
    private String mRegUrl;
    private KDMResCode mCacheKdmVersion;
    private boolean mCacheKdmVersionIsDirty;
    private Observable<KDMResCode> mKdmVersionObs;

    private KdmRepository(@NonNull KdmDataSource localDataSource,
            @NonNull KdmDataSource remoteDataSource) {
        checkNotNull(localDataSource);
        checkNotNull(remoteDataSource);
        mLocalDataSource = localDataSource;
        mRemoteDataSource = remoteDataSource;
    }

    public static KdmRepository getInstance(KdmDataSource localDataSource,
            KdmDataSource remoteDataSource) {
        if (null == INSTANCE) {
            synchronized (KdmRepository.class) {
                if (null == INSTANCE) {
                    INSTANCE = new KdmRepository(localDataSource, remoteDataSource);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(KdmDataSource, KdmDataSource)} to create a new
     * instance next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public synchronized Observable<KDMResCode> initKdm(final String regUrl,
            final boolean forceRegister) {
        boolean cacheDirty = isCacheInitKdmIsDirty() || isCacheDirty(regUrl, forceRegister);
        Logger.d("initKdm, cacheDirty : " + cacheDirty + ", regUrl : " + regUrl + ", mRegUrl : "
                + mRegUrl + ", forceRegister : " + forceRegister + ", mForceRegister : "
                + mForceRegister);
//        if (!cacheDirty && mCacheInitKdm != null) {
//            return Observable.just(mCacheInitKdm);
//        }

        mRegUrl = regUrl;
        mForceRegister = forceRegister;

        // cache is dirty
        if (cacheDirty) {
            setInitKdmObs(null);
            if (isCacheInitKdmIsDirty()) {
                setCacheInitKdmIsDirty(false);
            }
        } else if (mCacheInitKdm != null) {
            return Observable.just(mCacheInitKdm);
        }

        if (null == getInitKdmObs()) {
            Observable<KDMResCode> observable = mLocalDataSource.initKdm(regUrl,
                    forceRegister)
                    .onErrorReturn(new KdmExceptionHandler("initKdm"))
                    .doOnNext(new Action1<KDMResCode>() {
                        @Override
                        public void call(@Nullable KDMResCode kdmResCode) {
                            if (kdmResCode != null) {
                                Logger.d("doOnNext, kdmResCode getResult : "
                                        + kdmResCode.getResult());
                            }
                            // only cache ok
//                            if (kdmResCode != null
//                                    && KDMResCode.RESCODE_OK == kdmResCode.getResult())
                            {
                                mCacheInitKdm = kdmResCode;
                                setCacheInitKdmIsDirty(false);
                            }
                        }
                    })
                    // cache 1
                    .replay(1)
                    //
                    .refCount();
            setInitKdmObs(observable);
        }

        return getInitKdmObs();
    }

    @Override
    public synchronized Observable<KDMResCode> getKdmVersion() {
        boolean cacheDirty = isCacheKdmVersionIsDirty();
        if (cacheDirty) {
            setKdmVersionObs(null);
        } else if (mCacheKdmVersion != null) {
            return Observable.just(mCacheKdmVersion);
        }

        if (null == getKdmVersionObs()) {
            Observable<KDMResCode> observable = mLocalDataSource.getKdmVersion()
                    .onErrorReturn(new KdmExceptionHandler("getKdmVersion"))
                    .doOnNext(new Action1<KDMResCode>() {
                        @Override
                        public void call(KDMResCode kdmResCode) {
                            if (kdmResCode != null) {
                                Logger.d("doOnNext, kdmResCode getResult : "
                                        + kdmResCode.getResult());
                            }

                            mCacheKdmVersion = kdmResCode;
                            setCacheKdmVersionIsDirty(false);
                        }
                    })
                    // cache 1
                    .replay(1)
                    //
                    .refCount();
            setKdmVersionObs(observable);
        }

        return getKdmVersionObs();
    }

    @Override
    public Observable<KDMServerVersion> getKdmServerVersion(String version, String platform) {
        return mRemoteDataSource.getKdmServerVersion(version, platform);
    }

    @Override
    public Observable<KDMResCode> upgradeKdm(@NonNull String upgradeUrl) {
        // upgrade kdm
        return mLocalDataSource.upgradeKdm(upgradeUrl)
                .doOnNext(new Action1<KDMResCode>() {
                    @Override
                    public void call(KDMResCode kdmResCode) {
                        // upgrade kdm success
                        if (kdmResCode != null && KDMResCode.RESCODE_OK == kdmResCode.getResult()) {
                            // refresh init kdm
                            refreshInitKdm();
                        }
                    }
                });
    }

    @Override
    public void refreshInitKdm() {
        setCacheInitKdmIsDirty(true);
    }

    @Override
    public void refreshKdmVersion() {
        setCacheKdmVersionIsDirty(true);
    }

    @Override
    public void notifyKdmReady() {
        mLocalDataSource.notifyKdmReady();
    }

    private synchronized boolean isCacheDirty(final String regUrl, final boolean forceRegister) {
        return forceRegister != mForceRegister || !StringUtils.isNullOrEmpty(regUrl)
                && !StringUtils.isNullOrEmpty(mRegUrl) && !regUrl.equalsIgnoreCase(mRegUrl);
    }

    private synchronized boolean isCacheInitKdmIsDirty() {
        return mCacheInitKdmIsDirty;
    }

    private synchronized void setCacheInitKdmIsDirty(boolean cacheInitKdmIsDirty) {
        mCacheInitKdmIsDirty = cacheInitKdmIsDirty;
    }

    private synchronized Observable<KDMResCode> getInitKdmObs() {
        return mInitKdmObs;
    }

    private synchronized void setInitKdmObs(Observable<KDMResCode> initKdmObs) {
        mInitKdmObs = initKdmObs;
    }

    private synchronized KDMResCode getCacheKdmVersion() {
        return mCacheKdmVersion;
    }

    private synchronized void setCacheKdmVersion(KDMResCode cacheKdmVersion) {
        mCacheKdmVersion = cacheKdmVersion;
    }

    private synchronized boolean isCacheKdmVersionIsDirty() {
        return mCacheKdmVersionIsDirty;
    }

    private synchronized void setCacheKdmVersionIsDirty(boolean cacheKdmVersionIsDirty) {
        mCacheKdmVersionIsDirty = cacheKdmVersionIsDirty;
    }

    private synchronized Observable<KDMResCode> getKdmVersionObs() {
        return mKdmVersionObs;
    }

    private synchronized void setKdmVersionObs(Observable<KDMResCode> versionKdmObs) {
        mKdmVersionObs = versionKdmObs;
    }

    private class KdmExceptionHandler implements Func1<Throwable, KDMResCode> {
        private final String mName;

        private KdmExceptionHandler(String name) {
            mName = name;
        }

        @Override
        public KDMResCode call(Throwable throwable) {
            Logger.w(throwable, mName + ", onErrorReturn : ");
            if (throwable instanceof KdmException) {
                return ((KdmException) throwable).getKdmResCode();
            }
            return null;
        }
    }
}
