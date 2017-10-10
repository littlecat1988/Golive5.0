package com.golive.cinema;

import rx.Subscription;

/**
 * @author wangzijie E-mail:initialjie90@gmail.com
 * @version V1.0
 * @Title SubscriptionCollector.java
 * @Package com.golive.network
 * @Description TODO
 * @date 2015年12月4日 下午2:36:46
 */
public interface SubscriptionCollector {

    /**
     * Hold this subscription which will be unsubscribed in {@linkplain #unsubscribe()}.
     */
    void addSubscription(Subscription subscription);

    /**
     * Remove the subscription.
     */
    void removeSubscription(Subscription subscription);

    /**
     * Unsubscribe all subscriptions held by this collector.
     */
    void unsubscribe();
}
