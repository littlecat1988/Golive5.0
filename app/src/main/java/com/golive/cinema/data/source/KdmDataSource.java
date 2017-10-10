package com.golive.cinema.data.source;

import android.support.annotation.NonNull;

import com.golive.network.entity.KDMServerVersion;
import com.golive.player.kdm.KDMResCode;

import rx.Observable;

/**
 * Created by Wangzj on 2016/10/31.
 */

public interface KdmDataSource {

    Observable<KDMResCode> initKdm(final String regUrl, final boolean forceRegister);

    Observable<KDMResCode> getKdmVersion();

    Observable<KDMServerVersion> getKdmServerVersion(final String version, final String platform);

    Observable<KDMResCode> upgradeKdm(@NonNull final String upgradeUrl);

    void refreshInitKdm();

    void refreshKdmVersion();

    void notifyKdmReady();
}
