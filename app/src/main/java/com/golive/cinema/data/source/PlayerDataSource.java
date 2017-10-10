package com.golive.cinema.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.network.entity.Response;
import com.golive.network.entity.Ticket;

import rx.Observable;

/**
 * Created by Wangzj on 2016/10/27.
 */

public interface PlayerDataSource {

//    Observable<PlaybackValidity> getPlaybackValidity(@NonNull String filmId,
//            @NonNull String mediaId);

    Observable<Ticket> getPlayTicket(@NonNull String filmId, @Nullable String mediaName,
            @NonNull String orderSerial, @NonNull String licenseId);

    Observable<Ticket> getPlayToken(@NonNull String ticket, @NonNull String licenseId,
            @Nullable String checkcode, @Nullable String kdmId);

    /**
     * Report the ticket status
     */
    Observable<Response> reportTicketStatus(@NonNull String ticket, @NonNull String status,
            @Nullable String progressrate);

    Observable<Response> reportAdvertMiaozhen(String movieid, String scaleddensity,
            String manufacturerId, String pvalue, String kvalue, String advertId, String type);
}
