package com.golive.cinema.data.source.remote;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.Constants;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.data.source.StatisticsDataSource;
import com.golive.cinema.util.DateHelper;
import com.golive.cinema.util.DeviceUtils;
import com.golive.network.entity.Location;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.Response;
import com.golive.network.net.GoLiveRestApi;

import java.util.Date;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/12/21.
 */

public class StatisticsRemoteDataSource implements StatisticsDataSource {
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";
    private static StatisticsRemoteDataSource INSTANCE = null;
    private final GoLiveRestApi mGoLiveRestApi;
    private final MainConfigDataSource mMainConfigDataSource;

    public static StatisticsRemoteDataSource getInstance(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new StatisticsRemoteDataSource(goLiveRestApi, mainConfigDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(GoLiveRestApi, MainConfigDataSource)} to create a new
     * instance next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    private StatisticsRemoteDataSource(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        mGoLiveRestApi = checkNotNull(goLiveRestApi);
        mMainConfigDataSource = checkNotNull(mainConfigDataSource);
    }

    @Override
    public Observable<Location> getLocation() {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends Location>>() {
                    @Override
                    public Observable<? extends Location> call(MainConfig mainConfig) {
                        String url = mainConfig.getIplookup();
                        return mGoLiveRestApi.getLocation(url);
                    }
                });
    }

    @Override
    public Observable<Response> reportAppException(final String exceptionType,
            final String exceptionCode, final String exceptionMsg, final String exceptionLevel,
            final String partnerId) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends Response>>() {
                    @Override
                    public Observable<? extends Response> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportexceptioninfo();
                        return mGoLiveRestApi.reportAppException(url, exceptionType, exceptionCode,
                                exceptionMsg, exceptionLevel, partnerId);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportAppStart(final String caller, final String destination,
            final String userStatus, final String province, final String city, final String net,
            final String os, final String versionCode, final String kdmVersion,
            final String duration) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportAppStart(url, caller, destination,
                                province, city, net, os, versionCode, kdmVersion, userStatus,
                                duration);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportAppExit(final String duration) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportAppExit(url, duration);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportEnterActivity(final String code, final String filmName,
            final String from) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportEnterActivity(url, code, filmName, from);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportExitActivity(final String code, final String filmName,
            final String to, final String duration) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportExitActivity(url, code, filmName, to, duration);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportVideoStart(final String filmId, final String filmName,
            final String definition, final String filmType, final String watchType,
            final String orderSerial, final String valueType, final String caller) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportVideoStart(url, caller, filmId, filmName,
                                filmType, watchType, definition, orderSerial, valueType);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportVideoLoad(final String filmId, final String filmName,
            final String filmType, final String watchType, final String mediaUrl,
            final String mediaIp, final String definition, final String bufferDuration,
            final String orderSerial, final String valueType, final String speed) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportVideoLoad(url, filmId, filmName, filmType,
                                mediaUrl, mediaIp, definition, bufferDuration, watchType,
                                orderSerial, valueType, speed);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportVideoStreamSwitch(final String filmId,
            final String filmName, final String filmType, final String watchType,
            final String definition, final String toDefinition, final String watchDuration,
            final String playDuration, final String totalDuration, final String playProgress,
            final String orderSerial, final String valueType) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportVideoStreamSwitch(url, filmId, filmName,
                                filmType, watchType, definition, toDefinition, watchDuration,
                                playDuration, totalDuration, playProgress, orderSerial, valueType);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportVideoPlayPause(final String filmId,
            final String filmName, final String filmType, final String watchType,
            final String definition, final String pauseDuration, final String watchDuration,
            final String playDuration, final String totalDuration, final String playProgress,
            final String orderSerial, final String valueType) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportVideoPlayPause(url, filmId, filmName, filmType,
                                watchType, definition, pauseDuration, watchDuration, playDuration,
                                totalDuration, playProgress, orderSerial, valueType);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportVideoSeek(final String filmId, final String filmName,
            final String filmType, final String watchType, final String definition,
            final String playProgress, final String toPosition, final String toType,
            final String orderSerial, final String valueType) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportVideoSeek(url, filmId, filmName, definition,
                                filmType, watchType, playProgress, toPosition, toType, orderSerial,
                                valueType);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportVideoBlocked(final String filmId,
            final String filmName, final String filmType, final String watchType,
            final String definition, final String mediaIp, final String bufferDuration,
            final String watchDuration, final String playDuration, final String totalDuration,
            final String playProgress, final String orderSerial, final String valueType) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportVideoBlocked(url, filmId, filmName, filmType,
                                watchType, mediaIp, definition, bufferDuration, watchDuration,
                                playDuration, totalDuration, playProgress, orderSerial,
                                valueType);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportVideoException(final String filmId,
            final String filmName, final String filmType, final String watchType,
            final String definition, final String errCode, final String errMsg,
            final String watchDuration, final String playDuration, final String totalDuration,
            final String playProgress, final String orderSerial, final String valueType) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportVideoException(url, filmId, filmName, filmType,
                                watchType, definition, errMsg, errCode, watchDuration, playDuration,
                                totalDuration, playProgress, orderSerial, valueType);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportVideoExit(final String filmId, final String filmName,
            final String filmType, final String watchType, final String definition,
            final String watchDuration, final String playDuration, final String totalDuration,
            final String playProgress, final String orderSerial, final String valueType) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportVideoExit(url, filmId, filmName, filmType,
                                watchType, definition, watchDuration, playDuration, totalDuration,
                                playProgress, orderSerial, valueType);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportEnterFilmDetail(final String filmId,
            final String filmName, final String filmStatus, final String caller) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportEnterFilmDetail(url, filmName, filmId,
                                filmStatus, caller
                        );
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportExitFilmDetail(final String filmId,
            final String filmName, final String filmStatus, final String destination,
            final String duration) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportExitFilmDetail(url, filmId, filmName,
                                filmStatus, duration, destination
                        );
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportEnterEvent(final String filmId, final String filmName,
            final String caller) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportEnterEvent(url, filmId, filmName, caller);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportExitEvent(final String filmId, final String filmName,
            final String destination, final String duration) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportExitEvent(url, filmId, filmName, destination,
                                duration);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportEnterUserCenter() {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportEnterUserCenter(url);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportExitUserCenter(final String duration) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportExitUserCenter(url, duration);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportClickUserCenterTopUp(final String price,
            final String accountBalance, final String button) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportClickUserCenterTopUp(url, price,
                                accountBalance, button);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportClickUserCenterVip(final String price,
            final String accountBalance, final String button) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportClickUserCenterVip(url, price,
                                accountBalance, button);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportEnterAd(final String caller) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportEnterAd(url, caller);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportExitAd(final String caller,
            final String duration) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportExitAd(url, caller, duration);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportThirdPartyAd(String reportUrl) {
        return mGoLiveRestApi.reportThirdPartyAd(reportUrl);
    }

    @Override
    public Observable<ResponseBody> reportExitWatchNotice(final String button,
            final String duration, final String type) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportExitWatchNotice(url, button, duration,
                                type);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportExitPrompt(final String button,
            final String duration, final String type) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportExitPrompt(url, button, duration, type);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportPlayAdStart(final String adId,
            final String adName, final String adType, final String adOwnerCode,
            final String adOwnerName, final String adDefinition, final String adUrl,
            final String adLocation, final String filmId, final String filmName,
            final String filmPlayProgress) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportPlayAdStart(url, adId, adName,
                                adType, adOwnerCode, adOwnerName, adUrl, adDefinition, adLocation,
                                filmName, filmId, filmPlayProgress);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportPlayAdException(final String adId,
            final String adName, final String adType, final String errCode,
            final String errMsg, final String adOwnerCode, final String adOwnerName,
            final String adDefinition, final String adUrl, final String adLocation,
            final String filmId, final String filmName,
            final String filmPlayProgress) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportPlayAdException(url, adId, adName,
                                adType, errCode, errMsg, adOwnerCode, adOwnerName,
                                adDefinition, adUrl, adLocation, filmName, filmId
                                , filmPlayProgress);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportPlayAdExit(final String adId,
            final String adName, final String adType, final String bufferDuration,
            final String adDuration, final String adProgress, final String adOwnerCode,
            final String adOwnerName, final String adDefinition, final String adUrl,
            final String adLocation, final String filmId, final String filmName,
            final String filmPlayProgress) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportPlayAdExit(url, adId, adName, adType,
                                bufferDuration, adDuration, adProgress, adOwnerCode,
                                adOwnerName, adDefinition, adUrl, adLocation, filmId,
                                filmName, filmPlayProgress);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportPlayAdLoad(final String adId,
            final String adName, final String adType, final String adDuration,
            final String adMediaIp, final String adOwnerCode, final String adOwnerName,
            final String adDefinition, final String adUrl, final String adLocation,
            final String filmId, final String filmName,
            final String filmPlayProgress) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportPlayAdLoad(url, adId, adName, adType,
                                adDuration, adMediaIp, adOwnerCode, adOwnerName, adDefinition,
                                adUrl, adLocation, filmName, filmId, filmPlayProgress);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportPlayAdBlocked(final String adId,
            final String adName, final String adType, final String bufferDuration,
            final String adDuration, final String adProgress, final String adMediaIp,
            final String adOwnerCode, final String adOwnerName, final String adDefinition,
            final String adUrl, final String adLocation, final String filmId, final String filmName,
            final String filmPlayProgress) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportPlayAdBlocked(url, adId, adName,
                                adType, bufferDuration, adDuration, adProgress, adMediaIp,
                                adOwnerCode, adOwnerName, adDefinition, adUrl, adLocation,
                                filmId, filmName, filmPlayProgress);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportHardwareInfo(final String wirelessMac,
            final String wireMac, final String bluetoothMac, final String sn, final String cpuId,
            final String deviceId, final String deviceName, final String deviceType,
            final String memory, final String storage, final String density,
            final String resolution, final String screenSize) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {
                        String url = mainConfig.getReportStatistics();
                        return mGoLiveRestApi.reportHardwareInfo(url, wirelessMac, wireMac,
                                bluetoothMac, sn, cpuId, deviceId, deviceName, deviceType,
                                memory, storage, density, resolution, screenSize);
                    }
                });
    }

    @Override
    public Observable<ResponseBody> reportAdThirdExposure(final int adType, final String adCode,
            final String materialCode, final String showTime, final String showType,
            final String pkgName, final String activityName) {
        return mMainConfigDataSource.getMainConfig()
                .concatMap(new Func1<MainConfig, Observable<? extends ResponseBody>>() {
                    @Override
                    public Observable<? extends ResponseBody> call(MainConfig mainConfig) {

                        String reportUrl = mainConfig.getAdvertUploadUrl();
                        String adAppCode = mainConfig.getAdvertAppCode();
                        String adAppKey = mainConfig.getAdvertAppKey();
                        String appSecret = mainConfig.getAdvertAppSecret();
                        String channelCode = DeviceUtils.convertPartnerId(
                                mainConfig.getPartnerid());
                        String sendTime = DateHelper.dateFormatToString(new Date(), DATE_FORMAT);

                        String zoneCode = "";
                        if (adType == Constants.AD_REQUEST_TYPE_BOOT) {
                            zoneCode = mainConfig.getAdvertPositionBootimage();
                        } else if (adType == Constants.AD_REQUEST_TYPE_PLAYER) {
                            zoneCode = mainConfig.getAdvertPositionSection();
                        }

                        return mGoLiveRestApi.reportAdThirdExposure(reportUrl, adAppCode, adAppKey,
                                appSecret, channelCode, adCode, materialCode, showTime, showType,
                                pkgName, activityName, sendTime, zoneCode);
                    }
                });
    }
}
