package com.golive.cinema.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.network.entity.Error;
import com.golive.network.entity.Order;
import com.golive.network.entity.PayOrderResult;
import com.golive.network.entity.Response;
import com.golive.network.entity.Ticket;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;

/**
 * Created by Wangzj on 2016/10/18.
 */
public class FakeOrdersDataSource implements OrdersDataSource {

    private static FakeOrdersDataSource INSTANCE;

    private final Map<String, Order> mOrderMap;

    public static FakeOrdersDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeOrdersDataSource();
        }
        return INSTANCE;
    }

    public FakeOrdersDataSource() {
        Order order = new Order();
        order.setProductId("0");
        order.setMediaResourceId("media_id_2");
        order.setSerial("xxxxxxx");
        order.setStatus(Order.STATUS_PAY_SUCCESS);
        order.setProductType(Order.PRODUCT_TYPE_THEATRE_DOWNLOAD);
        order.setRemain("18000000");

        mOrderMap = new LinkedHashMap<>();
        mOrderMap.put(order.getSerial(), order);
    }

    @Override
    public Observable<List<Order>> queryOrders(@NonNull String productId,
            @NonNull String productType, @NonNull String beginTime, @NonNull String endTime) {
        List<Order> orderList = new ArrayList<>();
        for (Order order : mOrderMap.values()) {
            if (null == order) {
                continue;
            }

            // compare productId && productType
            if (order.getProductId().equals(productId) && order.getProductType().startsWith(
                    productType)) {
                orderList.add(order);
            }
        }
        return Observable.just(orderList);
    }

    @Override
    public Observable<List<Order>> getValidOrders(@NonNull String productId,
            @NonNull String productType) {
        List<Order> validOrders = new ArrayList<>();
        for (Order order : mOrderMap.values()) {
            if (null == order || !order.isValid()) {
                continue;
            }

            // compare productId && productType
            if (order.getProductId().equals(productId) && order.getProductType().startsWith(
                    productType)) {
                validOrders.add(order);
            }
        }
        return Observable.just(validOrders);
    }

    @Override
    public Observable<Order> getOrder(@NonNull String orderSerial) {
        return Observable.just(mOrderMap.get(orderSerial));
    }

    @Override
    public Observable<Order> createOrder(@NonNull String productId, String mediaId,
            String encryptionType, @NonNull String productType,
            @NonNull String quantity) {
        Order order = new Order();
        order.setProductId(productId);
        order.setMediaResourceId(mediaId);
        order.setSerial(String.valueOf(System.currentTimeMillis()));
        order.setPrice("5");
        order.setStatus(Order.STATUS_PAY_NOT_CONFIRM);
        order.setProductType(productType);

        mOrderMap.put(order.getSerial(), order);
        return Observable.just(order);
    }

    @Override
    public Observable<PayOrderResult> payOrder(@NonNull String orderSerial) {
        Order order = mOrderMap.get(orderSerial);
        if (order != null) {
            order.setStatus(Order.STATUS_PAY_SUCCESS);
            order.setRemain("1800000000");
        }
        PayOrderResult result = new PayOrderResult();
        result.setOrder(order);
        Error error = new Error();
        error.setType("false");
        result.setError(error);
        return Observable.just(result);
    }

    @Override
    public Observable<PayOrderResult> payCreditOrder(@NonNull String orderSerial) {
        return payOrder(orderSerial);
    }

    @Override
    public Observable<Ticket> getPlayTicket(@NonNull String filmId, @Nullable String mediaName,
            @NonNull String orderSerial, @NonNull String licenseId) {
        Ticket ticket = new Ticket();
        ticket.setTicketstring(orderSerial);
        int hour = 8;
        ticket.setTicketvalidation(String.valueOf(hour));
        Order order = mOrderMap.get(orderSerial);
        if (order != null) {
            order.setStatus(Order.STATUS_PAY_FINISH);
            order.setRemain(String.valueOf(hour * 3600000L));
        }
        return Observable.just(ticket);
    }

    @Override
    public Observable<Ticket> getPlayToken(@NonNull String ticketStr, @NonNull String licenseId,
            @Nullable String checkcode, @Nullable String kdmId) {
        Ticket ticket = new Ticket();
        ticket.setTicketstring(ticketStr);
        ticket.setTickettoken(ticketStr);
        ticket.setTicketvalidation("8");
        return Observable.just(ticket);
    }

    @Override
    public Observable<Response> reportTicketStatus(@NonNull String orderSerial,
            @NonNull String ticket, @NonNull String status, @Nullable String progressrate) {
        if (Ticket.TICKET_STATUS_PLAY_FINISH.equals(status)) {
            Order order = mOrderMap.get(orderSerial);
            if (order != null) {
                order.setStatus(Order.STATUS_ORDER_FINISH);
            }
        }
        return Observable.just(new Response());
    }
}
