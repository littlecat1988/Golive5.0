package com.golive.cinema.data.source;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.Order;
import com.golive.network.entity.PayOrderResult;
import com.golive.network.entity.Response;
import com.golive.network.entity.Ticket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/9/27.
 */

public class OrdersRepository implements OrdersDataSource {

    @Nullable
    private static OrdersRepository INSTANCE = null;

    @NonNull
    private final OrdersDataSource mRemoteDataSource;

    @NonNull
    private final OrdersDataSource mLocalDataSource;

    /** Map<productId, Map<productType, List<Order>>> */
    private final Map<String, Map<String, List<Order>>> mCacheOrders;

    /** Map<orderSerial, Order> */
    private final Map<String, Order> mCacheHistoryValidOrders;

    private OrdersRepository(@NonNull OrdersDataSource remoteDataSource,
            @NonNull OrdersDataSource localDataSource) {
        this.mRemoteDataSource = checkNotNull(remoteDataSource);
        this.mLocalDataSource = checkNotNull(localDataSource);
        mCacheOrders = new LinkedHashMap<>();
        mCacheHistoryValidOrders = new LinkedHashMap<>();
    }

    public static OrdersRepository getInstance(OrdersDataSource remoteDataSource,
            OrdersDataSource localDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new OrdersRepository(remoteDataSource, localDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(OrdersDataSource, OrdersDataSource)} to create a new
     * instance next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<List<Order>> queryOrders(@NonNull String productId,
            @NonNull String productType, @NonNull String beginTime, @NonNull String endTime) {
        checkNotNull(productId);
        checkNotNull(productType);
        checkNotNull(beginTime);
        checkNotNull(endTime);
        return mRemoteDataSource.queryOrders(productId, productType,
                beginTime, endTime);
    }

    @Override
    public Observable<List<Order>> getValidOrders(@NonNull final String productId,
            @NonNull final String productType) {
        checkNotNull(productId);
        checkNotNull(productType);
        List<Order> orders = getCacheValidOrders(productId, productType);
        if (orders != null && !orders.isEmpty()) {
            return Observable.just(orders);
//            for (Order order : orders) {
//                // if orders is valid
//                if (order != null && order.isValid()) {
//                    return Observable.just(orders);
//                }
//            }
        }
//        // if orders is valid
//        if (orders != null && orders.isValid()) {
//            return Observable.just(orders);
//        }

        String beginTime = "";
        String endTime = "";
        // query orders
        return queryOrders(productId, productType, beginTime, endTime)
                // filter valid orders
                .map(new Func1<List<Order>, List<Order>>() {
                    @Override
                    public List<Order> call(List<Order> orders) {
                        if (null == orders || orders.isEmpty()) {
                            return null;
                        }

                        List<Order> validOrders = null;
                        for (Order order : orders) {
                            if (null == order) {
                                continue;
                            }

                            String status = order.getStatus();
                            if (order.isValid()) { // order is valid
                                if (null == validOrders) {
                                    validOrders = new ArrayList<>();
                                }
                                // add this order
                                validOrders.add(order);
                                // order is valid, cache it
                                cacheHistoryValidOrder(order);
                            } else {
                                if (!StringUtils.isNullOrEmpty(status)
                                        && Order.STATUS_ORDER_FINISH.equals(status)) {
                                    // order is finish, cache it
                                    cacheHistoryValidOrder(order);
                                }
                            }
                        }

                        if (validOrders != null && !validOrders.isEmpty()) {
                            if (mCacheOrders != null) {
                                Map<String, List<Order>> orderMap = mCacheOrders.get(productId);
                                if (null == orderMap) {
                                    orderMap = new LinkedHashMap<>();
                                    mCacheOrders.put(productId, orderMap);
                                }

                                // if query theatre order
                                final String tmpType = getOrderType(productType);

                                // get history orders
                                List<Order> historyOrders = orderMap.get(tmpType);
                                if (null == historyOrders) {
                                    orderMap.put(tmpType, validOrders);
                                } else {
                                    historyOrders.addAll(validOrders);
                                }
                            }
                        }
                        return validOrders;
                    }
                });

//        Observable<List<Order>> observable = mRemoteDataSource.getValidOrders(productId,
//                productType)
//                .doOnNext(new Action1<List<Order>>() {
//                    @Override
//                    public void call(List<Order> orderList) {
//                        if (null == orderList || orderList.isEmpty()) {
//                            return;
//                        }
//                        if (mCacheOrders != null) {
//                            Map<String, List<Order>> orderMap = mCacheOrders.get(productId);
//                            if (null == orderMap) {
//                                orderMap = new LinkedHashMap<>();
//                                mCacheOrders.put(productId, orderMap);
//                            }
//                            orderMap.put(productType, orderList);
//                        }
//                    }
//                });
//        return observable;
    }

    @Override
    public Observable<Order> getOrder(@NonNull String orderSerial) {
        checkNotNull(orderSerial);
        return mRemoteDataSource.getOrder(orderSerial);
    }

    @Override
    public Observable<Order> createOrder(@NonNull String productId, @Nullable String mediaId,
            @Nullable String encryptionType, @NonNull String productType,
            @NonNull String quantity) {
        checkNotNull(productId);
        checkNotNull(productType);
        checkNotNull(quantity);
        return mRemoteDataSource.createOrder(productId, mediaId, encryptionType, productType,
                quantity);
    }

    private final Action1<PayOrderResult> mPayOrderResultAction = new Action1<PayOrderResult>() {
        @Override
        public void call(PayOrderResult payOrderResult) {
            if (payOrderResult != null && payOrderResult.isOk()) {
                String needed = payOrderResult.getNeeded();
                // pay success
                if (StringUtils.isNullOrEmpty(needed)) {
                    Order order = payOrderResult.getOrder();
                    // order valid
                    if (order != null && order.isValid()) {
                        if (mCacheOrders != null) {
                            String productId = order.getProductId();
                            String productType = order.getProductType();
                            Map<String, List<Order>> orderMap = mCacheOrders.get(productId);
                            if (null == orderMap) {
                                orderMap = new LinkedHashMap<>();
                                mCacheOrders.put(productId, orderMap);
                            }

                            // if query theatre order
                            final String tmpType = getOrderType(productType);

                            List<Order> orderList = orderMap.get(tmpType);
                            if (null == orderList) {
                                orderList = new ArrayList<>();
                                orderMap.put(tmpType, orderList);
                            }
                            orderList.add(order);
                        }
                        cacheHistoryValidOrder(order);
                    }
                }
            }
        }
    };

    @Override
    public Observable<PayOrderResult> payOrder(@NonNull String orderSerial) {
        checkNotNull(orderSerial);
        return mRemoteDataSource.payOrder(orderSerial)
                .doOnNext(mPayOrderResultAction);
    }

    @Override
    public Observable<PayOrderResult> payCreditOrder(@NonNull String orderSerial) {
        checkNotNull(orderSerial);
        return mRemoteDataSource.payCreditOrder(orderSerial)
                .doOnNext(mPayOrderResultAction);
    }

    @Override
    public Observable<Ticket> getPlayTicket(@NonNull String filmId, @Nullable String mediaName,
            @NonNull final String orderSerial, @NonNull String licenseId) {
        return mRemoteDataSource.getPlayTicket(filmId, mediaName, orderSerial, licenseId)
                .doOnNext(new Action1<Ticket>() {
                    @Override
                    public void call(Ticket ticket) {
                        if (ticket != null && ticket.isOk()) {
                            // get cache valid order
                            Order order = mCacheHistoryValidOrders.get(orderSerial);
                            if (order != null) {
                                String validation = ticket.getTicketvalidation();
                                if (!StringUtils.isNullOrEmpty(validation)) {
                                    try {
                                        // 小时
                                        long ticketTime = Long.parseLong(validation);

                                        // 毫秒
                                        ticketTime *= 3600000L;

                                        // 设置订单有效期
                                        order.setRemain(Long.toString(ticketTime));

                                        // 设置订单状态“已出票”
                                        order.setStatus(Order.STATUS_PAY_FINISH);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public Observable<Ticket> getPlayToken(@NonNull String ticket, @NonNull String licenseId,
            @Nullable String checkcode, @Nullable String kdmId) {
        return mRemoteDataSource.getPlayToken(ticket, licenseId, checkcode, kdmId);
    }

    @Override
    public Observable<Response> reportTicketStatus(@NonNull final String orderSerial,
            @NonNull String ticket, @NonNull final String status, @Nullable String progressrate) {
        return mRemoteDataSource.reportTicketStatus(orderSerial, ticket, status, progressrate)
                .doOnNext(new Action1<Response>() {
                    @Override
                    public void call(Response response) {
                        // play finish
                        if (response.isOk() && !StringUtils.isNullOrEmpty(status)
                                && Ticket.TICKET_STATUS_PLAY_FINISH.equals(status)) {
                            // get cache valid order
                            Order order = mCacheHistoryValidOrders.get(orderSerial);
                            if (order != null) {
                                // make order overdue
                                order.setStatus(Order.STATUS_ORDER_FINISH);
                            }
                        }
                    }
                });
    }

    @Override
    public Observable<Boolean> refreshOrder(@NonNull String orderSerial) {
        checkNotNull(orderSerial);
        if (mCacheHistoryValidOrders != null && !mCacheHistoryValidOrders.isEmpty()) {
            Order order = mCacheHistoryValidOrders.remove(orderSerial);
            if (order != null) {
                // mark order dirty!
                order.setDirty(true);
            }
        }
        return Observable.just(true);
    }

    @Nullable
    private List<Order> getCacheValidOrders(@NonNull String productId,
            @NonNull String productType) {
        checkNotNull(productId);
        checkNotNull(productType);
        if (mCacheOrders != null && !mCacheOrders.isEmpty()) {
            Map<String, List<Order>> orderMap = mCacheOrders.get(productId);
            if (orderMap != null && !orderMap.isEmpty()) {
                // if query theatre order
                final String tmpType = getOrderType(productType);
                List<Order> orderList = orderMap.get(tmpType);
                if (orderList != null && !orderList.isEmpty()) {
                    List<Order> validOrders = null;
                    Iterator<Order> iterable = orderList.iterator();
                    while (iterable.hasNext()) {
                        Order order = iterable.next();
                        // check order product type && order is valid
                        if (order != null && order.isValid()) {
                            if (null == validOrders) {
                                validOrders = new ArrayList<>();
                            }
                            // add this order
                            validOrders.add(order);
                        } else {
                            // remove not valid order
                            iterable.remove();
                        }
                    }
                    return validOrders;
                }
            }
        }
        return null;
    }

    private void cacheHistoryValidOrder(@NonNull Order order) {
        if (mCacheHistoryValidOrders != null) {
            mCacheHistoryValidOrders.put(order.getSerial(), order);
        }
    }

    @NonNull
    private String getOrderType(@NonNull String productType) {
        return productType.startsWith(Order.PRODUCT_TYPE_THEATRE) ? Order.PRODUCT_TYPE_THEATRE
                : productType;
    }
}
