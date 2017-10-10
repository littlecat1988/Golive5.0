package com.golive.cinema.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.network.entity.Response;
import com.golive.network.entity.Ticket;

import rx.Observable;

/**
 * Created by Wangzj on 2016/10/27.
 */

public class PlayerRepository implements PlayerDataSource {

    private static PlayerRepository INSTANCE = null;

    private final PlayerDataSource mPlayerRemoteDataSource;

    public static PlayerRepository getInstance(@NonNull PlayerDataSource playerRemoteDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new PlayerRepository(playerRemoteDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(PlayerDataSource)} to create a new
     * instance next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    private PlayerRepository(@NonNull PlayerDataSource playerRemoteDataSource) {
        mPlayerRemoteDataSource = playerRemoteDataSource;
    }

    @Override
    public Observable<Ticket> getPlayTicket(@NonNull String filmId, @Nullable String mediaName,
            @NonNull String orderSerial, @NonNull String licenseId) {
        return mPlayerRemoteDataSource.getPlayTicket(filmId, mediaName, orderSerial, licenseId);
    }

    @Override
    public Observable<Ticket> getPlayToken(@NonNull String ticket, @NonNull String licenseId,
            @Nullable String checkcode, @Nullable String kdmId) {
        return mPlayerRemoteDataSource.getPlayToken(ticket, licenseId, checkcode, kdmId);
    }

    @Override
    public Observable<Response> reportTicketStatus(@NonNull String ticket,
            @NonNull String status, @Nullable String progressrate) {
        return mPlayerRemoteDataSource.reportTicketStatus(ticket, status, progressrate);
    }

    @Override
    public Observable<Response> reportAdvertMiaozhen(String movieid, String scaleddensity,
            String manufacturerId, String pvalue, String kvalue, String advertId, String type) {
        return mPlayerRemoteDataSource.reportAdvertMiaozhen(movieid, scaleddensity, manufacturerId,
                pvalue, kvalue, advertId, type);
    }
}
