package com.golive.cinema.data.source;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.network.entity.Order;
import com.golive.network.entity.PayOrderResult;
import com.golive.network.entity.Response;
import com.golive.network.entity.Ticket;

import java.util.List;

import rx.Observable;

/**
 * Main entry point for accessing order data.
 * <p>
 * Created by Wangzj on 2016/9/27.
 */

public interface OrdersDataSource {

    /**
     * Query the orders of the product range from beginTime to endTime.
     *
     * @param productId   product id
     * @param productType product type
     * @param beginTime   begin time
     * @param endTime     end time
     * @return The the orders of the product range from beginTime to endTime if exist.
     */
    Observable<List<Order>> queryOrders(@NonNull String productId, @NonNull String productType,
            @NonNull String beginTime, @NonNull String endTime);

    /**
     * Get valid orders of the product.
     *
     * @param productId   product id
     * @param productType product type
     * @return The valid order of the product or <code>null<code/>.
     */
    Observable<List<Order>> getValidOrders(@NonNull String productId, @NonNull String productType);

    /**
     * Get order by order serial.
     *
     * @param orderSerial order serial.
     * @return The order detail or <code>null<code/>.
     */
    Observable<Order> getOrder(@NonNull String orderSerial);

    /**
     * Create the order of the product.
     *
     * @return The order(not paid yet) created by Server.
     */
    Observable<Order> createOrder(@NonNull String productId, @Nullable String mediaId,
            @Nullable String encryptionType, @NonNull String productType, @NonNull String quantity);

    /**
     * Pay the order.
     *
     * @param orderSerial order number
     * @return Pay status.
     */
    Observable<PayOrderResult> payOrder(@NonNull String orderSerial);

    /**
     * Pay the credit order.
     *
     * @param orderSerial order number
     * @return Pay status.
     */
    Observable<PayOrderResult> payCreditOrder(@NonNull String orderSerial);

    Observable<Ticket> getPlayTicket(@NonNull String filmId, @Nullable String mediaName,
            @NonNull String orderSerial, @NonNull String licenseId);

    Observable<Ticket> getPlayToken(@NonNull String ticket, @NonNull String licenseId,
            @Nullable String checkcode, @Nullable String kdmId);

    /**
     * Report the ticket status
     */
    Observable<Response> reportTicketStatus(@NonNull String orderSerial, @NonNull String ticket,
            @NonNull String status, @Nullable String progressrate);

    /**
     * refresh order by order serial
     *
     * @param orderSerial order serial
     */
    Observable<Boolean> refreshOrder(@NonNull String orderSerial);
}
