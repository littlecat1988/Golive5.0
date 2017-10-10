package com.golive.cinema.advert.domain.usecase;

import android.support.annotation.NonNull;

import com.golive.cinema.Constants;
import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.MainConfig;
import com.golive.network.response.AdvertResponse;
import com.initialjie.log.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by chgang on 2017/3/2.
 */

public class AdvertUseCase extends
        UseCase<AdvertUseCase.RequestValues, AdvertUseCase.ResponseValue> {

    @NonNull
    private final MainConfigDataSource mMainConfigDataSource;
    @NonNull
    private final ServerInitDataSource mServerInitDataSource;
    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    public AdvertUseCase(MainConfigDataSource mainConfigDataSource,
            ServerInitDataSource serverInitDataSource,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        super(schedulerProvider);
        this.mMainConfigDataSource = mainConfigDataSource;
        this.mServerInitDataSource = serverInitDataSource;
        this.mSchedulerProvider = schedulerProvider;
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(
            final RequestValues requestValues) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseValue>>() {
                    @Override
                    public Observable<? extends ResponseValue> call(MainConfig mainConfig) {
                        String adResSwitch = mainConfig.getAdvertResourceSwitch();
//                        Logger.d("adResSwitch:"+adResSwitch);
                        if (!StringUtils.isNullOrEmpty(adResSwitch)
                                && Constants.AD_RESOURCE_DIGTAL_MEDIA.equals(adResSwitch)) {
                            return getAdvertDataObs(requestValues);
                        }
                        return Observable.just(new ResponseValue(new AdvertResponse(true)));
                    }
                });
    }

    private Observable<ResponseValue> getAdvertDataObs(final RequestValues requestValues) {
        return getLocalHostIpObs()
                .concatMap(new Func1<String, Observable<AdvertResponse>>() {
                    @Override
                    public Observable<AdvertResponse> call(String hostIp) {
                        return mServerInitDataSource.queryOtherAdvert(
                                requestValues.adType, requestValues.filmId,
                                requestValues.filmName, requestValues.isFree, hostIp);
                    }
                }).map(new Func1<AdvertResponse, ResponseValue>() {
                    @Override
                    public ResponseValue call(AdvertResponse advertResponse) {
                        return new ResponseValue(advertResponse);
                    }
                }).subscribeOn(mSchedulerProvider.io());
    }

    private Observable<String> getLocalHostIpObs() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String ipaddress = "";
                try {
                    Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                    while (en.hasMoreElements()) {
                        NetworkInterface nif = en.nextElement();
                        Enumeration<InetAddress> inet = nif.getInetAddresses();
                        while (inet.hasMoreElements()) {
                            InetAddress ip = inet.nextElement();
                            if (!ip.isLoopbackAddress() && !ip.isLinkLocalAddress()) {
                                ipaddress = ip.getHostAddress();
                            }
                        }

                    }
                } catch (SocketException e) {
                    Logger.e("获取本地ip地址失败" + e);
                }

                subscriber.onNext(ipaddress);
                subscriber.onCompleted();
            }
        });
    }

    public static class RequestValues implements UseCase.RequestValues {

        public final int adType;
        public final String filmId;
        public final String filmName;
        public final String isFree;

        public RequestValues(int adType, String filmId,
                String filmName, String isFree) {
            this.adType = adType;
            this.filmId = filmId;
            this.filmName = filmName;
            this.isFree = isFree;
        }
    }

    public static class ResponseValue implements UseCase.ResponseValue {
        public final AdvertResponse advertResponse;

        public ResponseValue(AdvertResponse advertResponse) {
            this.advertResponse = advertResponse;
        }

        public AdvertResponse getAdvertResponse() {
            return advertResponse;
        }
    }
}
