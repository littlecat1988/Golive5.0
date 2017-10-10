package com.golive.cinema.data.source.local;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.KdmDataSource;
import com.golive.cinema.player.kdm.KDM;
import com.golive.network.entity.KDMServerVersion;
import com.golive.player.kdm.KDMResCode;

import rx.Observable;

/**
 * Created by Wangzj on 2016/12/16.
 */

public class KdmLocalDataSource implements KdmDataSource {

    private static KdmLocalDataSource INSTANCE;

    private final KDM mKDM;

    private KdmLocalDataSource(@NonNull KDM kdm) {
        checkNotNull(kdm);
        mKDM = kdm;
    }

    public static KdmLocalDataSource getInstance(KDM kdm) {
        if (INSTANCE == null) {
            INSTANCE = new KdmLocalDataSource(kdm);
        }
        return INSTANCE;
    }

    @Override
    public Observable<KDMResCode> initKdm(String regUrl, boolean forceRegister) {
        return mKDM.initKdm(regUrl, forceRegister);
    }

    @Override
    public Observable<KDMResCode> getKdmVersion() {
        return mKDM.getKdmVersion();
    }

    @Override
    public Observable<KDMServerVersion> getKdmServerVersion(String version, String platform) {
        return Observable.empty();
    }

    @Override
    public Observable<KDMResCode> upgradeKdm(@NonNull String upgradeUrl) {
        return mKDM.upgradeKdm(upgradeUrl);
    }

    @Override
    public void refreshInitKdm() {

    }

    @Override
    public void refreshKdmVersion() {

    }

    @Override
    public void notifyKdmReady() {
        mKDM.notifyReady();
    }
}
