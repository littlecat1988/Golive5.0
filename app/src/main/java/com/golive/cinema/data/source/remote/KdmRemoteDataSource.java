package com.golive.cinema.data.source.remote;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.KdmDataSource;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.network.entity.KDMServerVersion;
import com.golive.network.entity.MainConfig;
import com.golive.network.net.GoLiveRestApi;
import com.golive.player.kdm.KDMResCode;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2017/1/11.
 */

public class KdmRemoteDataSource implements KdmDataSource {

    private static KdmRemoteDataSource INSTANCE;

    private final GoLiveRestApi mGoLiveRestApi;
    private final MainConfigDataSource mMainConfigDataSource;

    public static KdmRemoteDataSource getInstance(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new KdmRemoteDataSource(goLiveRestApi, mainConfigDataSource);
        }
        return INSTANCE;
    }

    private KdmRemoteDataSource(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        checkNotNull(goLiveRestApi);
        checkNotNull(mainConfigDataSource);
        mGoLiveRestApi = goLiveRestApi;
        mMainConfigDataSource = mainConfigDataSource;
    }

    @Override
    public Observable<KDMResCode> initKdm(String regUrl, boolean forceRegister) {
        return Observable.empty();
    }

    @Override
    public Observable<KDMResCode> getKdmVersion() {
        return Observable.empty();
    }

    @Override
    public Observable<KDMServerVersion> getKdmServerVersion(final String version,
            final String platform) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends KDMServerVersion>>() {
                    @Override
                    public Observable<? extends KDMServerVersion> call(MainConfig mainConfig) {
                        String url = mainConfig.getGetkdmserverversion();
                        return mGoLiveRestApi.getKDMServerVersion(url, platform, version);
                    }
                });
    }

    @Override
    public Observable<KDMResCode> upgradeKdm(@NonNull String upgradeUrl) {
        return Observable.empty();
    }

    @Override
    public void refreshInitKdm() {

    }

    @Override
    public void refreshKdmVersion() {

    }

    @Override
    public void notifyKdmReady() {

    }
}
