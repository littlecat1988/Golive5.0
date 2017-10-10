package com.golive.cinema.data.source.remote;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.cinema.restapi.RestApiErrorCheckFlatMap;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.Order;
import com.golive.network.entity.OrderList;
import com.golive.network.entity.PayOrderResult;
import com.golive.network.entity.Response;
import com.golive.network.entity.Ticket;
import com.golive.network.net.GoLiveRestApi;

import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/9/27.
 */

public class OrdersRemoteDataSource implements OrdersDataSource {
    private static OrdersRemoteDataSource INSTANCE;

    private final GoLiveRestApi mGoLiveRestApi;
    private final MainConfigDataSource mMainConfigDataSource;

    private OrdersRemoteDataSource(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        this.mGoLiveRestApi = goLiveRestApi;
        this.mMainConfigDataSource = mainConfigDataSource;
    }

    public static OrdersRemoteDataSource getInstance(GoLiveRestApi goLiveRestApi,
            MainConfigDataSource mainConfigDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new OrdersRemoteDataSource(goLiveRestApi, mainConfigDataSource);
        }
        return INSTANCE;
    }

    @Override
    public Observable<List<Order>> queryOrders(@NonNull final String productId,
            @NonNull final String productType, @NonNull final String beginTime,
            @NonNull final String endTime) {

        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<OrderList>>() {
                    @Override
                    public Observable<OrderList> call(MainConfig mainConfig) {
                        String url = mainConfig.getQueryorders();
                        return mGoLiveRestApi.queryOrders(url, productId, productType, beginTime,
                                endTime);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<OrderList>())
                .map(new Func1<OrderList, List<Order>>() {
                    @Override
                    public List<Order> call(OrderList orderList) {
                        return orderList.getOrders();
                    }
                });
    }

    @Override
    public Observable<List<Order>> getValidOrders(@NonNull final String productId,
            @NonNull String productType) {
//        checkNotNull(productId);
//        checkNotNull(productType);
//
//        String beginTime = "";
//        String endTime = "";
//        Observable<List<Order>> observable = queryOrders(productId, productType, beginTime,
// endTime)
//                .map(
//                        new Func1<List<Order>, List<Order>>() {
//                            @Override
//                            public List<Order> call(List<Order> orders) {
//                                Logger.d("getValidOrders, orders : " + orders);
//                                if (null == orders || orders.isEmpty()) {
//                                    return null;
//                                }
//
//                                List<Order> validOrders = null;
//                                for (Order order : orders) {
//                                    Logger.d("order.isValid() : " + order.isValid());
//                                    // if order is valid
//                                    if (order != null && order.isValid()) {
//                                        if (null == validOrders) {
//                                            validOrders = new ArrayList<Order>();
//                                        }
//                                        // add this order
//                                        validOrders.add(order);
//                                    }
//                                }
//                                return validOrders;
//                            }
//                        });
//        return observable;
        return Observable.empty();
    }

    @Override
    public Observable<Order> getOrder(@NonNull final String orderSerial) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(
                        new Func1<MainConfig, Observable<OrderList>>() {
                            @Override
                            public Observable<OrderList> call(MainConfig mainConfig) {
                                String url = mainConfig.getQueryorder();
                                return mGoLiveRestApi.getOrder(url, orderSerial);
                            }
                        })
                .flatMap(new RestApiErrorCheckFlatMap<OrderList>())
                .map(new Func1<OrderList, Order>() {
                    @Override
                    public Order call(OrderList orderList) {
                        List<Order> orders = orderList.getOrders();
                        if (orders != null && !orders.isEmpty()) {
                            return orders.get(0);
                        }
                        return null;
                    }
                });
    }

    @Override
    public Observable<Order> createOrder(@NonNull final String productId,
            @Nullable final String mediaId, @Nullable final String encryptionType,
            @NonNull final String productType, @NonNull final String quantity) {

        return mMainConfigDataSource.getMainConfig()
                .flatMap(
                        new Func1<MainConfig, Observable<OrderList>>() {
                            @Override
                            public Observable<OrderList> call(MainConfig mainConfig) {
                                String url = mainConfig.getCreateorder();
                                return mGoLiveRestApi.createOrder(url, productId, mediaId,
                                        encryptionType, productType, quantity);
                            }
                        })
                .flatMap(new RestApiErrorCheckFlatMap<OrderList>())
                .map(new Func1<OrderList, Order>() {
                    @Override
                    public Order call(OrderList orderList) {
                        List<Order> orders = orderList.getOrders();
                        if (orders != null && !orders.isEmpty()) {
                            return orders.get(0);
                        }
                        return null;
                    }
                });
    }

    @Override
    public Observable<PayOrderResult> payOrder(@NonNull final String orderSerial) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<PayOrderResult>>() {
                    @Override
                    public Observable<PayOrderResult> call(MainConfig mainConfig) {
                        String url = mainConfig.getPayorder();
                        return mGoLiveRestApi.payOrder(url, orderSerial);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<PayOrderResult>());
    }

    @Override
    public Observable<PayOrderResult> payCreditOrder(@NonNull final String orderSerial) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<PayOrderResult>>() {
                    @Override
                    public Observable<PayOrderResult> call(MainConfig mainConfig) {
                        String url = mainConfig.getPayOrderByCredit();
                        return mGoLiveRestApi.payOrder(url, orderSerial);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<PayOrderResult>());
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
            @NonNull final String licenseId, @Nullable final String checkcode,
            @Nullable final String kdmId) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Ticket>>() {
                    @Override
                    public Observable<Ticket> call(MainConfig mainConfig) {
                        String url = mainConfig.getGettickettoken();
                        String fCheckCode = StringUtils.isNullOrEmpty(checkcode) ? "" : checkcode;
                        String fKdmId = StringUtils.isNullOrEmpty(kdmId) ? "" : kdmId;
                        return mGoLiveRestApi.getTicketToken(url, ticket, licenseId, fKdmId,
                                fCheckCode);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<Ticket>());
    }

    @Override
    public Observable<Response> reportTicketStatus(@NonNull String orderSerial,
            @NonNull final String ticket, @NonNull final String status,
            @Nullable final String progressrate) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportticketstatus();
                        String fProgressrate = StringUtils.isNullOrEmpty(progressrate) ? ""
                                : progressrate;
                        return mGoLiveRestApi.reportTicketStatus(url, ticket, status,
                                fProgressrate);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<>());
    }

    @Override
    public Observable<Boolean> refreshOrder(@NonNull String orderSerial) {
        return Observable.empty();
    }
}
