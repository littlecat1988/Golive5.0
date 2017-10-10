package com.golive.cinema.data.source;

import android.support.annotation.NonNull;

import com.golive.network.entity.ActivityImage;
import com.golive.network.entity.BootImage;
import com.golive.network.entity.DrainageInfo;
import com.golive.network.entity.GuideTypeInfo;
import com.golive.network.entity.RepeatMac;
import com.golive.network.entity.ServerMessage;
import com.golive.network.entity.Upgrade;
import com.golive.network.response.AdvertResponse;
import com.golive.network.response.ApplicationPageResponse;
import com.initialjie.log.Logger;

import java.util.List;

import rx.Observable;

/**
 * Created by chgang on 2016/11/1.
 */

public class ServerInitRepository implements ServerInitDataSource {

    private static ServerInitRepository INSTANCE = null;

    private final ServerInitDataSource mServerInitRemoteDataSource;
    private final ServerInitDataSource serverInitLocalDataSource;

    private ServerInitRepository(@NonNull ServerInitDataSource serverStatusRemoteDataSource,
            @NonNull ServerInitDataSource serverStatusLocalDataSource) {
        Logger.d("ServerInitRepository");
        this.mServerInitRemoteDataSource = serverStatusRemoteDataSource;
        this.serverInitLocalDataSource = serverStatusLocalDataSource;
    }

    public static ServerInitRepository getInstance(
            ServerInitDataSource serverStatusRemoteDataSource,
            ServerInitDataSource serverStatusLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new ServerInitRepository(serverStatusRemoteDataSource,
                    serverStatusLocalDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(ServerInitDataSource, ServerInitDataSource)} to create a
     * new instance next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<List<ServerMessage>> queryServerMessages() {
        return mServerInitRemoteDataSource.queryServerMessages();
    }

    @Override
    public Observable<BootImage> queryBootImage() {
        return mServerInitRemoteDataSource.queryBootImage();
    }

    @Override
    public Observable<Upgrade> upgrade(String code, String name) {
        return mServerInitRemoteDataSource.upgrade(code, name);
    }

    @Override
    public Observable<RepeatMac> queryRepeatMacStatus(String phone) {
        return mServerInitRemoteDataSource.queryRepeatMacStatus(phone);
    }

    @Override
    public Observable<ActivityImage> queryActivityPoster() {
        return mServerInitRemoteDataSource.queryActivityPoster();
    }

    @Override
    public Observable<ApplicationPageResponse> queryApplicationPageAction() {
        return mServerInitRemoteDataSource.queryApplicationPageAction();
    }

    @Override
    public Observable<GuideTypeInfo> queryGuideTypeInfo() {
        return mServerInitRemoteDataSource.queryGuideTypeInfo();
    }

    @Override
    public Observable<DrainageInfo> queryDrainageInfo() {
        return mServerInitRemoteDataSource.queryDrainageInfo();
    }

    @Override
    public Observable<AdvertResponse> queryOtherAdvert(int adType, String filmId,
            String filmName, String isFree, String hostIp) {
        return mServerInitRemoteDataSource.queryOtherAdvert(adType, filmId,
                filmName, isFree, hostIp);
    }

}
