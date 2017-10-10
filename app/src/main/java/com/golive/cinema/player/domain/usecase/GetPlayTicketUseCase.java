package com.golive.cinema.player.domain.usecase;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Ticket;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/12/21.
 */

public class GetPlayTicketUseCase extends
        UseCase<GetPlayTicketUseCase.RequestValues, GetPlayTicketUseCase.ResponseValue> {

    private final OrdersDataSource mOrdersDataSource;

    public GetPlayTicketUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            OrdersDataSource ordersDataSource) {
        super(schedulerProvider);
        mOrdersDataSource = ordersDataSource;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mOrdersDataSource.getPlayTicket(requestValues.mFilmId, requestValues.mMediaName,
                requestValues.mOrderSerial, requestValues.mLicenseId)
                .map(new Func1<Ticket, ResponseValue>() {
                    @Override
                    public ResponseValue call(Ticket ticket) {
                        return new ResponseValue(ticket);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final String mFilmId;
        private final String mMediaName;
        private final String mOrderSerial;
        private final String mLicenseId;

        public RequestValues(String filmId, String mediaName, String orderSerial,
                String licenseId) {
            mFilmId = filmId;
            mMediaName = mediaName;
            mOrderSerial = orderSerial;
            mLicenseId = licenseId;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final Ticket mTicket;

        public ResponseValue(Ticket ticket) {
            mTicket = ticket;
        }

        public Ticket getTicket() {
            return mTicket;
        }
    }

}
