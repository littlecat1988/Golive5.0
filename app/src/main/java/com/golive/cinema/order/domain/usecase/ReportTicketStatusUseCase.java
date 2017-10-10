package com.golive.cinema.order.domain.usecase;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Response;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/12/21.
 */

public class ReportTicketStatusUseCase extends
        UseCase<ReportTicketStatusUseCase.RequestValues, ReportTicketStatusUseCase.ResponseValue> {

    private final OrdersDataSource mOrdersDataSource;

    public ReportTicketStatusUseCase(@NonNull BaseSchedulerProvider schedulerProvider,
            OrdersDataSource ordersDataSource) {
        super(schedulerProvider);
        mOrdersDataSource = ordersDataSource;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(RequestValues requestValues) {
        return mOrdersDataSource.reportTicketStatus(requestValues.mOrderSerial,
                requestValues.mTicket, requestValues.mStatus, requestValues.mProgressrate)
                .map(new Func1<Response, ResponseValue>() {
                    @Override
                    public ResponseValue call(Response response) {
                        return new ResponseValue(response);
                    }
                });
    }

    public static class RequestValues implements UseCase.RequestValues {
        private final String mOrderSerial;
        private final String mTicket;
        private final String mStatus;
        private final String mProgressrate;

        public RequestValues(String orderSerial, String ticket, String status,
                String progressrate) {
            mOrderSerial = orderSerial;
            mTicket = ticket;
            mStatus = status;
            mProgressrate = progressrate;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        private final Response mResponse;

        public ResponseValue(Response response) {
            mResponse = response;
        }

        public Response getResponse() {
            return mResponse;
        }
    }

}
