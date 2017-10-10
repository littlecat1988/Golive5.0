package com.golive.cinema.order;

import com.golive.network.entity.Order;
import com.initialjie.log.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Mowl on 2016/11/28.
 */

public class OrderManager {
    private static final String TAG = OrderManager.class.getSimpleName();

    private static final long UPDATE_TIME = 1000; // 订单更新时间，单位：毫秒

    public static final String INTENT_FILM_ID = "intent_film_id";
    public static final String INTENT_ORDER_TYPE = "intent_order_type";

    /* 过期时发出的广播 */
    public static final String INTENT_OVERDUE_ACTION = "intent_overdue_action";

    /* 购买成功时发出的广播 */
    public static final String INTENT_PURCHASE_ACTION = "intent_purchase_action";
    public static final String INTENT_ORDER = "intent_purchase_order";
    public static final String INTENT_TICKET = "intent_purchase_ticket";

    private static OrderManager mOrderManager;

    /**
     * 影片订单列表Map <影片ID, <订单类型，订单>>
     */
    private ConcurrentHashMap<String, Map<String, Order> /*
                                                         * ConcurrentHashMap<String
														 * , Order>
														 */> mFilmOrders;

    private ScheduledExecutorService mUpdateService;

    private ConcurrentHashMap<String, Map<String, Order>> mOverdueFilms;

    // 过期订单，临时
    private ConcurrentHashMap<String, List<String>> tmpOverdueFilms;

    public static OrderManager getInstance() {
        if (null == mOrderManager) {
            synchronized (OrderManager.class) {
                if (null == mOrderManager) {
                    mOrderManager = new OrderManager();
                }
            }
        }
        return mOrderManager;
    }

    public OrderManager() {

        init();
    }

    private synchronized void init() {
        mOverdueFilms = new ConcurrentHashMap<>();
        mFilmOrders = new ConcurrentHashMap<>();

//        startUpdateOrderStatus();
    }

    private synchronized void unInit() {
        clear();
        stopUpdateOrderStatus();
    }

    /**
     * 新增订单
     *
     * @param filmID 影片ID
     * @param order  订单
     */
    public synchronized void addOrder(String filmID, Order order) {
        Logger.d(
                "addOrder, filmID : " + filmID + ", order type : "
                        + order.getProductType());
        Map<String, Order> orderMap = mFilmOrders.get(filmID);
        if (null == orderMap) {
            orderMap = new ConcurrentHashMap<>();
            mFilmOrders.put(filmID, orderMap);
        }
        orderMap.put(order.getProductType(), order);
    }

    /**
     * 新增订单
     *
     * @param filmID
     *            影片ID
     * @param order
     *            订单
     *
     * @param mediaId 资源id
     */
//	public synchronized void addOrder(String mediaId, Order order) {
//		Logger.d(
//				"addOrder, mediaId : " + mediaId + ", order type : "
//						+ order.getProductType() + ", remain : "
//						+ order.getRemainL());
//		Map<String, Order> orderMap = mFilmOrders.get(mediaId);
//		if (null == orderMap) {
//			orderMap = new ConcurrentHashMap<String, Order>();
//			mFilmOrders.put(mediaId, orderMap);
//		}
//		orderMap.put(order.getProductType(), order);
//	}

    /**
     * 更改kdm在线播放的订单状态
     *
     * @param film
     * @param order
     */
//	public synchronized void changeKdmOnlineOrderType(Film film, Order order) {
//		for (Media media : film.getMediaList()) {
//			if (Media.TYPE_KDM.equals(media.getEncryptiontype()) && Media.MEDIA_TYPE_ONLINE.equals
// (media.getType())) {
//				if (media.getMediaid().equals(order.getMediaResId())) {
//					order.setProductType(Order.PRODUCT_TYPE_THEATRE_ONLINE);
//				}
//			}
//		}
//	}

    /**
     * 替换订单
     */
    public synchronized void changeOrder(String filmID, Order order) {
        addOrder(filmID, order);
    }

    /**
     * 删除订单
     */
    public synchronized void deleteOrder(String filmID, String orderType) {
        Logger.d("deleteOrder, filmID : " + filmID + ", order type : "
                + orderType);
        Map<String, Order> orderMap = mFilmOrders.get(filmID);
        if (null == orderMap) {
            return;
        }

        orderMap.remove(orderType);
        // 如果是本地播放过期，则把下载订单也过期
        if (Order.PRODUCT_TYPE_THEATRE_LOCAL_PLAY.equals(orderType)) {
            orderMap.remove(Order.PRODUCT_TYPE_THEATRE_DOWNLOAD);
        }
    }

    /**
     * 获取订单
     *
     * @param filmID    影片ID
     * @param orderType 订单类型
     * @param mediaId   购买的定单类型
     */
    public synchronized Order getOrder(String filmID, String orderType, String mediaId) {

        // 如果是本地播放
        if (Order.PRODUCT_TYPE_THEATRE_LOCAL_PLAY.equals(orderType)) {
            orderType = Order.PRODUCT_TYPE_THEATRE_DOWNLOAD;
        }

        Order order = null;
        Map<String, Order> orderMap = mFilmOrders.get(filmID);
        if (null != orderMap) {
            order = orderMap.get(orderType);
        }

//        if (order != null) {
//            Logger.d("getOrder, filmID : " + filmID + ", order type : "
//                    + orderType);
//        } else {
//            Logger.d("getOrder, filmID : " + filmID + ", order type : "
//                    + orderType);
//        }

        return order;
    }

    /**
     * 获取订单
     *
     * @param filmID    影片ID
     * @param orderType 订单类型
     */
    public synchronized Order getOrder(String filmID, String orderType) {

        // 如果是本地播放
        if (Order.PRODUCT_TYPE_THEATRE_LOCAL_PLAY.equals(orderType)) {
            orderType = Order.PRODUCT_TYPE_THEATRE_DOWNLOAD;
        }

        Order order = null;
        Map<String, Order> orderMap = mFilmOrders.get(filmID);
        if (null != orderMap) {
            order = orderMap.get(orderType);
        }

//        if (order != null) {
//            Logger.d("getOrder, filmID : " + filmID + ", order type : "
//                    + orderType);
//        } else {
//            Logger.d("getOrder, filmID : " + filmID + ", order type : "
//                    + orderType);
//        }

        return order;
    }

//    /**
//     * 使订单过期
//     *
//     * @param context
//     *            TODO
//     * @param filmID
//     * @param orderType
//     *
//     * @return true表示成功使订单过期并发送过期广播
//     */
//    public synchronized boolean makeOverdue(Context context, String filmID,
//                                            String orderType) {
//        Logger.d("makeOverdue, filmID : " + filmID + ", order type : "
//                + orderType);
//
//        boolean delSuccess = false;
//        Order order = null;
//        // 如果是本地播放过期，则把下载订单也过期
//        if (Order.PRODUCT_TYPE_THEATRE_LOCAL_PLAY.equals(orderType)) {
//            order = _deleteOverdueOrder(filmID,
//                    Order.PRODUCT_TYPE_THEATRE_DOWNLOAD);
//            if (order != null) {
//                delSuccess = true;
//            }
//        }
//
//        order = _deleteOverdueOrder(filmID, orderType);
//        if (order != null) {
//            delSuccess = true;
//        }
//
//        // if (delSuccess)
//        {
//            // 发送过期广播
//            sendOverdueBroadCast(context, filmID, orderType);
//        }
//        return true;
//
//    }
//
//    /**
//     * @param filmID
//     * @param orderType
//     */
//    private Order _deleteOverdueOrder(String filmID, String orderType) {
//        Order order = getOrder(filmID, orderType);
//        if (null == order) {
//            return null;
//        }
//
//        order.makeOverDue();
//
//        // 保存过期订单
//        Map<String, Order> orderMap = mOverdueFilms.get(filmID);
//        if (null == orderMap) {
//            orderMap = new ConcurrentHashMap<String, Order>();
//            mOverdueFilms.put(filmID, orderMap);
//        }
//        orderMap.put(orderType, order);
//
////		deleteOrder(filmID, orderType);
//        return order;
//    }

//    /**
//     * 定期更新订单有效期
//     */
//    public synchronized void startUpdateOrderStatus() {
//        Logger.d("startUpdateOrderStatus");
//        if (null == mUpdateService || mUpdateService.isShutdown()) {
//            mUpdateService = Executors.newSingleThreadScheduledExecutor();
//            mUpdateService.scheduleAtFixedRate(new Runnable() {
//
//                @Override
//                public void run() {
//                    updateOrders();
//                }
//            }, 0, UPDATE_TIME, TimeUnit.MILLISECONDS);
//        }
//    }

//    /**
//     * 更新订单有效期
//     */
//    private synchronized void updateOrders() {
//        // Logger.d("updateOrderStatus");
//
//        // 每一部影片
//        for (Entry<String, Map<String, Order>> ordersSet : mFilmOrders
//                .entrySet()) {
//
//            String filmID = ordersSet.getKey();
//            Map<String, Order> orderMap = ordersSet.getValue();
//
//            // 某个影片的各种订单
//            for (Entry<String, Order> ordetSet : orderMap.entrySet()) {
//                String orderType = ordetSet.getKey();
//                Order order = ordetSet.getValue();
//                boolean fireExpire = order.reduceRemainL(UPDATE_TIME);
//
//                // 致使订单过期
//                if (fireExpire) {
//                    // 保存过期订单，临时
//                    if (null == tmpOverdueFilms) {
//                        tmpOverdueFilms = new ConcurrentHashMap<String, List<String>>();
//                    }
//                    List<String> tmpOrderTypes = tmpOverdueFilms.get(filmID);
//                    if (null == tmpOrderTypes) {
//                        tmpOrderTypes = new ArrayList<String>();
//                        tmpOverdueFilms.put(filmID, tmpOrderTypes);
//                    }
//                    tmpOrderTypes.add(orderType);
//
//                    // // 发送过期广播
//                    // sendOverdueBroadCast(mContext, filmID, type);
//                }
//            }
//        }
//
//        // 处理过期订单
//        if (tmpOverdueFilms != null) {
//            for (Entry<String, List<String>> ordersSet : tmpOverdueFilms
//                    .entrySet()) {
//                String filmID = ordersSet.getKey();
//                List<String> orderTypes = ordersSet.getValue();
//                for (String orderType : orderTypes) {
//                    // // 删除订单
//                    // deleteOrder(filmID, orderType);
//                    makeOverdue(GoliveApp.getAppContext(), filmID, orderType);
//                }
//            }
//
//            tmpOverdueFilms.clear();
//            // tmpOverdueFilms = null;
//        }
//
//    }

    /**
     * 停止更新订单有效期
     */
    public synchronized void stopUpdateOrderStatus() {
        Logger.d("stopUpdateOrderStatus");
        if (mUpdateService != null) {
            mUpdateService.shutdown();
        }
    }

    /**
     * 清空管理的订单
     */
    public synchronized void clear() {
        Logger.d("clear");
        if (mFilmOrders != null) {
            mFilmOrders.clear();
        }
        if (tmpOverdueFilms != null) {
            tmpOverdueFilms.clear();
        }
    }

//    /**
//     * 发送订单过期广播
//     *
//     * @param context
//     * @param filmID
//     * @param type
//     */
//    public static void sendOverdueBroadCast(Context context, String filmID,
//                                            String type) {
//        Intent intent = new Intent(INTENT_OVERDUE_ACTION);
//        intent.putExtra(INTENT_FILM_ID, filmID);
//        intent.putExtra(INTENT_ORDER_TYPE, type);
//        context.sendBroadcast(intent);
//    }

//    /**
//     * 发送购买成功广播
//     *
//     * @param context
//     * @param filmID
//     * @param type
//     */
//    public static void sendPurchaseSuccessBroadCast(Context context,
//                                                    String filmID, String type, Order order,
// Ticket ticket) {
//        Intent intent = new Intent(INTENT_PURCHASE_ACTION);
//        intent.putExtra(INTENT_FILM_ID, filmID);
//        intent.putExtra(INTENT_ORDER_TYPE, type);
//        intent.putExtra(INTENT_ORDER, order);
//        intent.putExtra(INTENT_TICKET, ticket);
//        context.sendBroadcast(intent);
//    }
}

