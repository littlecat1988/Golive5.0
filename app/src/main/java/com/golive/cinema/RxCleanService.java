package com.golive.cinema;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Service that comes with a CompositeSubscription that you can add to and is later automatically
 * unsubscribed.
 *
 * @author wangzijie E-mail:initialjie90@gmail.com
 * @version V1.0
 * @Title RxCleanService.java
 * @Package com.example.retrofit
 * @Description TODO
 * @date 2015年12月2日 下午4:54:55
 */
public class RxCleanService extends Service implements SubscriptionCollector {

    private CompositeSubscription mCompositeSubscription;

    @Override
    public void onDestroy() {
        super.onDestroy();

        unsubscribe();
    }

    @Override
    public IBinder onBind(Intent pIntent) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Unsubscribe all subscriptions held by this Service.
     */
    public synchronized void unsubscribe() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.clear();
        }
    }

    /**
     * Hold this subscription which will be unsubscribed at once in onDestroy().
     */
    public synchronized void addSubscription(Subscription subscription) {
        if (null == subscription) {
            // subscription is null, then return;
            return;
        }

        if (null == mCompositeSubscription || mCompositeSubscription.isUnsubscribed()) {
            mCompositeSubscription = new CompositeSubscription();
        }
        mCompositeSubscription.add(subscription);
    }

    /**
     * Remove the subscription.
     */
    public synchronized void removeSubscription(Subscription subscription) {
        if (null == subscription) {
            // subscription is null, then return;
            return;
        }

        if (null != mCompositeSubscription) {
            mCompositeSubscription.remove(subscription);
        }
    }
}
