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

public class GetPlayTokenUseCase extends
        UseCase<GetPlayTokenUseCase.RequestValues, GetPlayTokenUseCase.ResponseValue> {

    private final OrdersDataSource mOrdersDataSource;

    public GetPlayTokenUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            OrdersDataSource ordersDataSource) {
        super(schedulerProvider);
        mOrdersDataSource = ordersDataSource;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mOrdersDataSource.getPlayToken(requestValues.mTicket, requestValues.mLicenseId,
                requestValues.mCheckcode, requestValues.mKdmId)
                .map(new Func1<Ticket, ResponseValue>() {
                    @Override
                    public ResponseValue call(Ticket ticket) {
                        return new ResponseValue(ticket);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final String mTicket;
        private final String mLicenseId;
        private final String mCheckcode;
        private final String mKdmId;

        public RequestValues(String ticket, String licenseId, String checkcode, String kdmId) {
            mTicket = ticket;
            mLicenseId = licenseId;
            mCheckcode = checkcode;
            mKdmId = kdmId;
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
