package com.golive.cinema.data.source.remote;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.data.source.PlayerDataSource;
import com.golive.cinema.restapi.RestApiErrorCheckFlatMap;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.Response;
import com.golive.network.entity.Ticket;
import com.golive.network.net.GoLiveRestApi;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/11/1.
 */

public class PlayerRemoteDataSource implements PlayerDataSource {

    private static PlayerRemoteDataSource INSTANCE = null;

    private final GoLiveRestApi mGoLiveRestApi;
    private final MainConfigDataSource mMainConfigDataSource;

    public static PlayerRemoteDataSource getInstance(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new PlayerRemoteDataSource(goLiveRestApi, mainConfigDataSource);
        }
        return INSTANCE;
    }

    private PlayerRemoteDataSource(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        mGoLiveRestApi = goLiveRestApi;
        mMainConfigDataSource = mainConfigDataSource;
    }

    @Override
    public Observable<Ticket> getPlayTicket(@NonNull final String filmId,
            @Nullable final String mediaName, @NonNull final String orderSerial,
            @NonNull final String licenseId) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Ticket>>() {
                    @Override
                    public Observable<Ticket> call(MainConfig mainConfig) {
                        String url = mainConfig.getUserticket();
                        return mGoLiveRestApi.getTicket(url, filmId, mediaName, orderSerial,
                                licenseId);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<Ticket>());
    }

    @Override
    public Observable<Ticket> getPlayToken(@NonNull final String ticket,
            @NonNull final String licenseId,
            @Nullable final String checkcode, @Nullable final String kdmId) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Ticket>>() {
                    @Override
                    public Observable<Ticket> call(MainConfig mainConfig) {
                        String url = mainConfig.getGettickettoken();
                        return mGoLiveRestApi.getTicketToken(url, ticket, licenseId, kdmId,
                                checkcode);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<Ticket>());
    }

    @Override
    public Observable<Response> reportTicketStatus(@NonNull final String ticket,
            @NonNull final String status, @Nullable final String progressrate) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportticketstatus();
                        return mGoLiveRestApi.reportTicketStatus(url, ticket, status,
                                progressrate);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<>());
    }

    @Override
    public Observable<Response> reportAdvertMiaozhen(final String movieid,
            final String scaleddensity, final String manufacturerId, final String pvalue,
            final String kvalue, final String advertId, final String type) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportAdvertMiaozhen();
                        return mGoLiveRestApi.reportAdvertMiaozhen(url, movieid,
                                scaleddensity, manufacturerId, pvalue, kvalue, advertId, type);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<>());
    }
}
