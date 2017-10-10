package com.golive.cinema.statistics.domain.usecase;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.UseCase;
import com.golive.cinema.data.source.StatisticsDataSource;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;

import java.net.InetAddress;
import java.net.URL;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/12/21.
 */

public class ReportPlayAdBlockedUseCase extends
        UseCase<ReportPlayAdBlockedUseCase.RequestValues,
                ReportPlayAdBlockedUseCase
                        .ResponseValue> {
    private final StatisticsDataSource mStatisticsDataSource;
    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    public ReportPlayAdBlockedUseCase(
            @NonNull BaseSchedulerProvider schedulerProvider,
            @NonNull StatisticsDataSource statisticsDataSource) {
        super(schedulerProvider);
        mStatisticsDataSource = checkNotNull(statisticsDataSource);
        mSchedulerProvider = checkNotNull(schedulerProvider);
    }

    @Override
    protected Observable<ResponseValue> executeUseCase(final RequestValues requestValues) {

        return createMediaIp(requestValues.adUrl)
                .concatMap(new Func1<String, Observable<ResponseBody>>() {
                    @Override
                    public Observable<ResponseBody> call(String mediaIp) {
                        return mStatisticsDataSource.reportPlayAdBlocked(
                                requestValues.adId, requestValues.adName,
                                requestValues.adType, requestValues.bufferDuration,
                                requestValues.adDuration, requestValues.adProgress,
                                mediaIp, requestValues.adOwnerCode,
                                requestValues.adOwnerName, requestValues.adDefinition,
                                requestValues.adUrl, requestValues.adLocation,
                                requestValues.filmName, requestValues.filmId,
                                requestValues.filmPlayProgress
                        );
                    }
                })
                .map(new Func1<ResponseBody, ResponseValue>() {
                    @Override
                    public ResponseValue call(ResponseBody responseBody) {
                        return new ResponseValue(responseBody);
                    }
                });

//        return mStatisticsDataSource.reportPlayAdBlocked(requestValues.adId, requestValues.adName,
//                requestValues.adType, requestValues.bufferDuration, requestValues.adDuration,
//                requestValues.adProgress, requestValues.adMediaIp, requestValues.adOwnerCode,
//                requestValues.adOwnerName, requestValues.adDefinition, requestValues.adUrl,
//                requestValues.adLocation, requestValues.filmId, requestValues.filmName,
//                requestValues.filmPlayProgress)
//                .subscribeOn(mSchedulerProvider.io())
//                .map(new Func1<ResponseBody, ResponseValue>() {
//                    @Override
//                    public ResponseValue call(ResponseBody responseBody) {
//                        return new ResponseValue(responseBody);
//                    }
//                });
    }

    private synchronized Observable<String> createMediaIp(final String playUrl) {
        return Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            URL url = new URL(playUrl);
                            String domainName = url.getHost();
                            InetAddress domainName_ip = InetAddress.getByName(domainName);
                            String mediaIp = domainName_ip.getHostAddress();
                            subscriber.onNext(mediaIp);
                        } catch (Exception e) {
                            e.printStackTrace();
                            subscriber.onNext(null);
                        }
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(mSchedulerProvider.io());
    }

    public static final class RequestValues implements UseCase.RequestValues {
        private final String adId;
        private final String adName;
        private final String adType;
        private final String bufferDuration;
        private final String adDuration;
        private final String adProgress;
        private final String adMediaIp;
        private final String adOwnerCode;
        private final String adOwnerName;
        private final String adDefinition;
        private final String adUrl;
        private final String adLocation;
        private final String filmName;
        private final String filmId;
        private final String filmPlayProgress;

        public RequestValues(String adId, String adName, String adType, String bufferDuration,
                String adDuration, String adProgress, String adMediaIp, String adOwnerCode,
                String adOwnerName, String adDefinition, String adUrl, String adLocation,
                String filmName, String filmId, String filmPlayProgress) {
            this.adId = adId;
            this.adName = adName;
            this.adType = adType;
            this.bufferDuration = bufferDuration;
            this.adDuration = adDuration;
            this.adProgress = adProgress;
            this.adMediaIp = adMediaIp;
            this.adOwnerCode = adOwnerCode;
            this.adOwnerName = adOwnerName;
            this.adDefinition = adDefinition;
            this.adUrl = adUrl;
            this.adLocation = adLocation;
            this.filmName = filmName;
            this.filmId = filmId;
            this.filmPlayProgress = filmPlayProgress;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {
        private final ResponseBody mResponseBody;

        public ResponseValue(ResponseBody responseBody) {
            mResponseBody = responseBody;
        }

        public ResponseBody getResponseBody() {
            return mResponseBody;
        }
    }
}
