package com.golive.cinema.player.voole;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.initialjie.log.Logger;
import com.vo.sdk.VPlay;
import com.voole.epg.corelib.model.play.PlayInfo;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Wangzj on 2016/12/27.
 */

public class Voole {

    private static boolean INIT;
    private static boolean INIT_VPLAY;
    private static final MySDKListener VPlayListener = new MySDKListener();

    private final Context mContext;

    /**
     * Constructor for Voole.
     *
     * @param context Android Context. Suggested to be assigned with Android's {@link Application}
     *                .
     */
    public Voole(@NonNull Context context) {
        // use application mContext in case of memory leak.
        this.mContext = context.getApplicationContext();
        if (!isInitVplay()) {
            initVPlay(mContext);
            setInitVplay(true);
        }
    }

    public void release() {
        Logger.d("release");
        VPlayListener.setSubscriber(null);
    }

    private void initVPlay(@NonNull Context context) {
        long sTime = System.currentTimeMillis();
        VPlay vPlay = VPlay.GetInstance();
        vPlay.initApp(context);
        long eTime = System.currentTimeMillis();
        Logger.d("initVPlay, time : " + (eTime - sTime) + "ms");
    }

    public Observable<Boolean> initSdk() {
        // is init already
        if (isInit()) {
            return Observable.just(true);
        }

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }

                VPlayListener.setSubscriber(subscriber);
                VPlay vPlay = VPlay.GetInstance();
                vPlay.setSdkListener(VPlayListener);
//                vPlay.setSdkListener(new VPlay.SDKListener() {
//                    @Override
//                    public void onInitCompleted(boolean b) {
//                        Logger.d("initSdk, onInitCompleted : " + b);
//
//                        setInit(b);
//
//                        if (!subscriber.isUnsubscribed()) {
//                            subscriber.onNext(b);
//                            subscriber.onCompleted();
//                        }
//                    }
//
//                    @Override
//                    public void onReleaseCompleted() {
//                        Logger.d("initSdk, onReleaseCompleted");
//                        if (!subscriber.isUnsubscribed()) {
//                            subscriber.onNext(false);
//                            subscriber.onCompleted();
//                        }
//                    }
//                });

//                PackageManager pm = mContext.getPackageManager();
//                PackageInfo pi;
//                int versionCode = 0;
//                String versionName = "";
//                try {
//                    pi = pm.getPackageInfo(mContext.getPackageName(), 0);
//                    versionCode = pi.versionCode;
//                    versionName = pi.versionName;
//                } catch (PackageManager.NameNotFoundException e) {
//                    e.printStackTrace();
//                }

                try {
                    vPlay.initSDKInfo(mContext, "1.0.0");
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(e);
                    }
                }
            }
        };
        return Observable.create(onSubscribe);
    }

    public Observable<PlayInfo> getPlayInfo(final String mid, final String fid, final String sid,
            final String mtype) {
        // init
        return initSdk()
                // switch to IO thread
                .observeOn(Schedulers.io())
                // get play info
                .concatMap(new Func1<Boolean, Observable<PlayInfo>>() {
                    @Override
                    public Observable<PlayInfo> call(Boolean aBoolean) {
                        Observable.OnSubscribe<PlayInfo> onSubscribe =
                                new Observable.OnSubscribe<PlayInfo>() {
                                    @Override
                                    public void call(Subscriber<? super PlayInfo> subscriber) {
                                        try {
                                            PlayInfo playInfo = VPlay.GetInstance().getPlayInfo(mid,
                                                    fid, sid, mtype);
                                            if (!subscriber.isUnsubscribed()) {
                                                subscriber.onNext(playInfo);
                                                subscriber.onCompleted();
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            if (!subscriber.isUnsubscribed()) {
                                                subscriber.onError(e);
                                            }
                                        }
                                    }
                                };
                        return Observable.create(onSubscribe);
                    }
                });
    }

    private synchronized static boolean isInit() {
        return INIT;
    }

    private synchronized static void setInit(boolean init) {
        Voole.INIT = init;
    }

    private synchronized static boolean isInitVplay() {
        return INIT_VPLAY;
    }

    private synchronized static void setInitVplay(boolean initVplay) {
        INIT_VPLAY = initVplay;
    }

    private static class MySDKListener implements VPlay.SDKListener {
        private WeakReference<Subscriber<? super Boolean>> mSubscriberRef;

        @Override
        public void onInitCompleted(boolean b) {
            Logger.d("initSdk, onInitCompleted : " + b);
            setInit(b);
            synchronized (MySDKListener.this) {
                Subscriber<? super Boolean> subscriber = getSubscriber();
                if (subscriber != null && !subscriber.isUnsubscribed()) {
                    subscriber.onNext(b);
                    subscriber.onCompleted();
                }
            }
        }

        @Override
        public void onReleaseCompleted() {
            Logger.d("initSdk, onReleaseCompleted");
            synchronized (MySDKListener.this) {
                Subscriber<? super Boolean> subscriber = getSubscriber();
                if (subscriber != null && !subscriber.isUnsubscribed()) {
                    subscriber.onNext(false);
                    subscriber.onCompleted();
                }
            }
        }

        synchronized Subscriber<? super Boolean> getSubscriber() {
            if (mSubscriberRef != null) {
                return mSubscriberRef.get();
            }
            return null;
        }

        synchronized void setSubscriber(Subscriber<? super Boolean> subscriber) {
            if (null == subscriber) {
                if (mSubscriberRef != null) {
                    mSubscriberRef.clear();
                    mSubscriberRef = null;
                }
            } else {
                mSubscriberRef = new WeakReference<Subscriber<? super Boolean>>(subscriber);
            }
        }
    }
}
