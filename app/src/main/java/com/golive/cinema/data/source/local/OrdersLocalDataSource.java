package com.golive.cinema.data.source.local;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.Order;
import com.golive.network.entity.PayOrderResult;
import com.golive.network.entity.Response;
import com.golive.network.entity.Ticket;

import java.util.List;

import rx.Observable;

/**
 * Created by Wangzj on 2016/9/27.
 */

public class OrdersLocalDataSource implements OrdersDataSource {

    private static OrdersLocalDataSource INSTANCE;

    private OrdersLocalDataSource(@NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(context, "context cannot be null");
        checkNotNull(schedulerProvider, "scheduleProvider cannot be null");
    }

    public static OrdersLocalDataSource getInstance(@NonNull Context context,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        if (INSTANCE == null) {
            INSTANCE = new OrdersLocalDataSource(context, schedulerProvider);
        }
        return INSTANCE;
    }

    @Override
    public Observable<List<Order>> queryOrders(@NonNull String productId,
            @NonNull String productType, @NonNull String beginTime, @NonNull String endTime) {
        return Observable.empty();
    }

    @Override
    public Observable<List<Order>> getValidOrders(@NonNull String productId,
            @NonNull String productType) {
        return Observable.empty();
    }

    @Override
    public Observable<Order> getOrder(@NonNull String orderSerial) {
        return Observable.empty();
    }

    @Override
    public Observable<Order> createOrder(@NonNull String productId, @Nullable String mediaId,
            @Nullable String encryptionType, @NonNull String productType,
            @NonNull String quantity) {
        return Observable.empty();
    }

    @Override
    public Observable<PayOrderResult> payOrder(@NonNull String orderSerial) {
        return Observable.empty();
    }

    @Override
    public Observable<PayOrderResult> payCreditOrder(@NonNull String orderSerial) {
        return Observable.empty();
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
    public Observable<Response> reportTicketStatus(@NonNull String orderSerial,
            @NonNull String ticket, @NonNull String status, @Nullable String progressrate) {
        return Observable.empty();
    }

    @Override
    public Observable<Boolean> refreshOrder(@NonNull String orderSerial) {
        return Observable.empty();
    }
}
