package com.golive.cinema.data.source.local;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.Context;
import android.support.annotation.NonNull;

import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.ActivityImage;
import com.golive.network.entity.BootImage;
import com.golive.network.entity.DrainageInfo;
import com.golive.network.entity.GuideTypeInfo;
import com.golive.network.entity.RepeatMac;
import com.golive.network.entity.ServerMessage;
import com.golive.network.entity.Upgrade;
import com.golive.network.response.AdvertResponse;
import com.golive.network.response.ApplicationPageResponse;

import java.util.List;

import rx.Observable;

/**
 * Created by chgang on 2016/11/1.
 */

public class ServerInitLocalDataSource implements ServerInitDataSource {

    private static ServerInitLocalDataSource INSTANCE = null;

    private ServerInitLocalDataSource(@NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(context, "context cannot be null");
        checkNotNull(schedulerProvider, "scheduleProvider cannot be null");
    }


    public static ServerInitLocalDataSource getInstance(@NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        if (INSTANCE == null) {
            INSTANCE = new ServerInitLocalDataSource(context, schedulerProvider);
        }
        return INSTANCE;
    }


    @Override
    public Observable<List<ServerMessage>> queryServerMessages() {
        return Observable.empty();
    }

    @Override
    public Observable<BootImage> queryBootImage() {
        return Observable.empty();
    }

    @Override
    public Observable<Upgrade> upgrade(String code, String name) {
        return Observable.empty();
    }

    @Override
    public Observable<RepeatMac> queryRepeatMacStatus(String phone) {
        return Observable.empty();
    }

    @Override
    public Observable<ActivityImage> queryActivityPoster() {
        return Observable.empty();
    }

    @Override
    public Observable<ApplicationPageResponse> queryApplicationPageAction() {
        return Observable.empty();
    }

    @Override
    public Observable<GuideTypeInfo> queryGuideTypeInfo() {
        return Observable.empty();
    }

    @Override
    public Observable<DrainageInfo> queryDrainageInfo() {
        return Observable.empty();
    }

    @Override
    public Observable<AdvertResponse> queryOtherAdvert(int adType, String filmId,
            String filmName, String isFree, String hostIp) {
        return Observable.empty();
    }

}
