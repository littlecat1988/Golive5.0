package com.golive.cinema;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.initialjie.log.Logger;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Activity that comes with a CompositeSubscription that you can add to and is later automatically
 * unsubscribed.
 *
 * @author wangzijie E-mail:initialjie90@gmail.com
 * @version V1.0
 * @Title RxCleanActivity.java
 * @Package com.example.retrofit
 * @Description TODO
 * @date 2015年12月2日 下午4:54:55
 */
public class RxCleanActivity extends AppCompatActivity implements SubscriptionCollector {

    private static final String TAG = RxCleanActivity.class.getSimpleName();
    private CompositeSubscription mCompositeSubscription;

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unsubscribe();
    }

    /**
     * Hold this subscription which will be unsubscribed at once in onDestroy().
     */
    public synchronized void addSubscription(Subscription subscription) {
        Logger.d("addSubscription");
        if (null == subscription) {
            // subscription is null, then return;
            return;
        }

        if (null == mCompositeSubscription) {
            mCompositeSubscription = new CompositeSubscription();
        }
        mCompositeSubscription.add(subscription);
    }

    /**
     * Remove the subscription.
     */
    public synchronized void removeSubscription(Subscription subscription) {
        Logger.d("removeSubscription");
        if (null == subscription) {
            // subscription is null, then return;
            return;
        }

        if (null != mCompositeSubscription) {
            mCompositeSubscription.remove(subscription);
        }
    }

    /**
     * Unsubscribe all subscriptions held by this Activity.
     */
    public synchronized void unsubscribe() {
        Logger.d("unsubscribe");
        if (mCompositeSubscription != null) {
            mCompositeSubscription.clear();
        }
    }
}
