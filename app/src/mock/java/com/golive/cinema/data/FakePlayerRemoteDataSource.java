package com.golive.cinema.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.data.source.PlayerDataSource;
import com.golive.network.entity.Response;
import com.golive.network.entity.Ticket;

import rx.Observable;

/**
 * Created by Wangzj on 2016/11/1.
 */

public class FakePlayerRemoteDataSource implements PlayerDataSource {

    private static FakePlayerRemoteDataSource INSTANCE;

    public static FakePlayerRemoteDataSource getInstance() {
        if (null == INSTANCE) {
            INSTANCE = new FakePlayerRemoteDataSource();
        }
        return INSTANCE;
    }

    private FakePlayerRemoteDataSource() {
    }

    @Override
    public Observable<Ticket> getPlayTicket(@NonNull String filmId, @Nullable String mediaName,
            @NonNull String orderSerial, @NonNull String licenseId) {
        return Observable.empty();
    }

    @Override
    public Observable<Ticket> getPlayToken(@NonNull String ticket, @NonNull String licenseId,
            @Nullable String checkcode, @Nullable String kdmId) {
        return Observable.empty();
    }

    @Override
    public Observable<Response> reportTicketStatus(@NonNull String ticket, @NonNull String status,
            @Nullable String progressrate) {
        return Observable.empty();
    }
}
