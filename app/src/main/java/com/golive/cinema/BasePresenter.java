package com.golive.cinema;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Wangzj on 2016/9/23.
 */

public class BasePresenter<V extends IBaseView> implements IBasePresenter<V> {

    private Reference<V> mViewRef;
    private CompositeSubscription mCompositeSubscription;

    @Override
    public void attachView(V mvpView) {
        setMvpView(mvpView);
    }

    @Override
    public void detachView() {
        setMvpView(null);
//        onUnsubscribe();
    }

    @Override
    public void start() {
    }

    @Override
    public void unsubscribe() {
        detachView();
        onUnsubscribe();
    }

    protected synchronized void addSubscription(Subscription subscription) {
        if (null == mCompositeSubscription || mCompositeSubscription.isUnsubscribed()) {
            mCompositeSubscription = new CompositeSubscription();
        }
        mCompositeSubscription.add(subscription);
    }

    private synchronized void onUnsubscribe() {
        if (mCompositeSubscription != null) {
            mCompositeSubscription.clear();
        }
    }

    private void setMvpView(V mvpView) {
        if (null == mvpView) {
            if (mViewRef != null) {
                mViewRef.clear();
                mViewRef = null;
            }
        } else {
            mViewRef = new WeakReference<>(mvpView);
        }
    }

    protected V getView() {
        if (mViewRef != null) {
            return mViewRef.get();
        }
        return null;
    }
}
