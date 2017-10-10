package com.golive.cinema.data.source;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.network.entity.Location;
import com.golive.network.entity.Response;

import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Created by Wangzj on 2016/12/21.
 */

public class StatisticsRepository implements StatisticsDataSource {

    private static StatisticsRepository INSTANCE = null;
    private final StatisticsDataSource mRemoteDataSource;

    public static StatisticsRepository getInstance(@NonNull StatisticsDataSource remoteDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new StatisticsRepository(remoteDataSource);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(StatisticsDataSource)} to create a new
     * instance next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    private StatisticsRepository(@NonNull StatisticsDataSource remoteDataSource) {
        mRemoteDataSource = checkNotNull(remoteDataSource);
    }

    @Override
    public Observable<Location> getLocation() {
        return mRemoteDataSource.getLocation();
    }

    @Override
    public Observable<Response> reportAppException(String exceptionType, String exceptionCode,
            String exceptionMsg, String exceptionLevel, String partnerId) {
        return mRemoteDataSource.reportAppException(exceptionType, exceptionCode, exceptionMsg,
                exceptionLevel, partnerId);
    }

    @Override
    public Observable<ResponseBody> reportAppStart(String caller, String destination,
            String userStatus, String province, String city, String net, String os,
            String versionCode, String kdmVersion, String duration) {
        return mRemoteDataSource.reportAppStart(caller, destination, userStatus, province,
                city, net, os, versionCode, kdmVersion, duration);
    }

    @Override
    public Observable<ResponseBody> reportAppExit(String duration) {
        return mRemoteDataSource.reportAppExit(duration);
    }

    @Override
    public Observable<ResponseBody> reportEnterActivity(String code, String filmName,
            String from) {
        return mRemoteDataSource.reportEnterActivity(code, filmName, from);
    }

    @Override
    public Observable<ResponseBody> reportExitActivity(String code, String filmName,
            String to, String duration) {
        return mRemoteDataSource.reportExitActivity(code, filmName, to, duration);
    }

    @Override
    public Observable<ResponseBody> reportVideoStart(String filmId, String filmName,
            String definition, String filmType, String watchType, String orderSerial,
            String valueType, String caller) {
        return mRemoteDataSource.reportVideoStart(filmId, filmName, definition, filmType, watchType,
                orderSerial, valueType, caller);
    }

    @Override
    public Observable<ResponseBody> reportVideoLoad(String filmId, String filmName,
            String filmType, String watchType, String mediaUrl, String mediaIp, String definition,
            String bufferDuration, String orderSerial, String valueType, String speed) {
        return mRemoteDataSource.reportVideoLoad(filmId, filmName, filmType, watchType, mediaUrl,
                mediaIp, definition, bufferDuration, orderSerial, valueType, speed);
    }

    @Override
    public Observable<ResponseBody> reportVideoStreamSwitch(String filmId, String filmName,
            String filmType, String watchType, String definition, String toDefinition,
            String watchDuration, String playDuration, String totalDuration, String playProgress,
            String orderSerial, String valueType) {
        return mRemoteDataSource.reportVideoStreamSwitch(filmId, filmName, filmType, watchType,
                definition, toDefinition, watchDuration, playDuration, totalDuration, playProgress,
                orderSerial, valueType);
    }

    @Override
    public Observable<ResponseBody> reportVideoPlayPause(String filmId, String filmName,
            String filmType, String watchType, String definition, String pauseDuration,
            String watchDuration, String playDuration, String totalDuration, String playProgress,
            String orderSerial, String valueType) {
        return mRemoteDataSource.reportVideoPlayPause(filmId, filmName, filmType, watchType,
                definition, pauseDuration, watchDuration, playDuration, totalDuration, playProgress,
                orderSerial, valueType);
    }

    @Override
    public Observable<ResponseBody> reportVideoSeek(String filmId, String filmName,
            String filmType, String watchType, String definition, String playProgress,
            String toPosition, String toType, String orderSerial, String valueType) {
        return mRemoteDataSource.reportVideoSeek(filmId, filmName, filmType, watchType, definition,
                playProgress, toPosition, toType, orderSerial, valueType);
    }

    @Override
    public Observable<ResponseBody> reportVideoBlocked(String filmId, String filmName,
            String filmType, String watchType, String definition, String mediaIp,
            String bufferDuration, String watchDuration, String playDuration, String totalDuration,
            String playProgress, String orderSerial, String valueType) {
        return mRemoteDataSource.reportVideoBlocked(filmId, filmName, filmType, watchType,
                definition, mediaIp, bufferDuration, watchDuration, playDuration, totalDuration,
                playProgress, orderSerial,
                valueType);
    }

    @Override
    public Observable<ResponseBody> reportVideoException(String filmId, String filmName,
            String filmType, String watchType, String definition, String errCode, String errMsg,
            String watchDuration, String playDuration, String totalDuration, String playProgress,
            String orderSerial, String valueType) {
        return mRemoteDataSource.reportVideoException(filmId, filmName, filmType, watchType,
                definition, errCode, errMsg, watchDuration, playDuration, totalDuration,
                playProgress, orderSerial,
                valueType);
    }

    @Override
    public Observable<ResponseBody> reportVideoExit(String filmId, String filmName,
            String filmType, String watchType, String definition, String watchDuration,
            String playDuration, String totalDuration, String playProgress, String orderSerial,
            String valueType) {
        return mRemoteDataSource.reportVideoExit(filmId, filmName, filmType, watchType, definition,
                watchDuration, playDuration, totalDuration, playProgress, orderSerial,
                valueType);
    }

    @Override
    public Observable<ResponseBody> reportEnterFilmDetail(String filmId, String filmName,
            String filmStatus, String caller) {
        return mRemoteDataSource.reportEnterFilmDetail(filmId, filmName, filmStatus, caller);
    }

    @Override
    public Observable<ResponseBody> reportExitFilmDetail(String filmId, String filmName,
            String filmStatus, String destination,
            String duration) {
        return mRemoteDataSource.reportExitFilmDetail(filmId, filmName, filmStatus, destination,
                duration);
    }

    @Override
    public Observable<ResponseBody> reportEnterEvent(String filmId, String filmName,
            String caller) {
        return mRemoteDataSource.reportEnterEvent(filmId, filmName, caller);
    }

    @Override
    public Observable<ResponseBody> reportExitEvent(String filmId, String filmName,
            String destination,
            String duration) {
        return mRemoteDataSource.reportExitEvent(filmId, filmName, destination, duration);
    }

    @Override
    public Observable<ResponseBody> reportEnterUserCenter() {
        return mRemoteDataSource.reportEnterUserCenter();
    }

    @Override
    public Observable<ResponseBody> reportExitUserCenter(String duration) {
        return mRemoteDataSource.reportExitUserCenter(duration);
    }

    @Override
    public Observable<ResponseBody> reportClickUserCenterTopUp(String price,
            String accountBalance, String button) {
        return mRemoteDataSource.reportClickUserCenterTopUp(price, accountBalance, button);
    }

    @Override
    public Observable<ResponseBody> reportClickUserCenterVip(String price,
            String accountBalance, String button) {
        return mRemoteDataSource.reportClickUserCenterVip(price, accountBalance, button);
    }

    @Override
    public Observable<ResponseBody> reportEnterAd(String caller) {
        return mRemoteDataSource.reportEnterAd(caller);
    }

    @Override
    public Observable<ResponseBody> reportExitAd(String caller, String duration) {
        return mRemoteDataSource.reportExitAd(caller, duration);
    }

    @Override
    public Observable<ResponseBody> reportThirdPartyAd(String reportUrl) {
        return mRemoteDataSource.reportThirdPartyAd(reportUrl);
    }

    @Override
    public Observable<ResponseBody> reportExitWatchNotice(String button, String duration,
            String type) {
        return mRemoteDataSource.reportExitWatchNotice(button, duration, type);
    }

    @Override
    public Observable<ResponseBody> reportExitPrompt(String button, String duration,
            String type) {
        return mRemoteDataSource.reportExitPrompt(button, duration, type);
    }

    @Override
    public Observable<ResponseBody> reportPlayAdStart(String adId, String adName,
            String adType, String adOwnerCode, String adOwnerName, String adDefinition,
            String adUrl, String adLocation, String filmId, String filmName,
            String filmPlayProgress) {
        return mRemoteDataSource.reportPlayAdStart(adId, adName, adType, adOwnerCode,
                adOwnerName, adDefinition, adUrl, adLocation, filmId, filmName,
                filmPlayProgress);
    }

    @Override
    public Observable<ResponseBody> reportPlayAdException(String adId, String adName,
            String adType, String errCode, String errMsg, String adOwnerCode,
            String adOwnerName, String adDefinition, String adUrl, String adLocation,
            String filmId, String filmName, String filmPlayProgress) {
        return mRemoteDataSource.reportPlayAdException(adId, adName, adType, errCode,
                errMsg, adOwnerCode, adOwnerName, adDefinition, adUrl, adLocation,
                filmId, filmName, filmPlayProgress);
    }

    @Override
    public Observable<ResponseBody> reportPlayAdExit(String adId, String adName,
            String adType, String bufferDuration, String adDuration, String adProgress,
            String adOwnerCode, String adOwnerName, String adDefinition, String adUrl,
            String adLocation, String filmId, String filmName, String filmPlayProgress) {
        return mRemoteDataSource.reportPlayAdExit(adId, adName, adType, bufferDuration,
                adDuration, adProgress, adOwnerCode, adOwnerName, adDefinition, adUrl,
                adLocation, filmId, filmName, filmPlayProgress);
    }

    @Override
    public Observable<ResponseBody> reportPlayAdLoad(String adId, String adName,
            String adType, String adDuration, String adMediaIp, String adOwnerCode,
            String adOwnerName, String adDefinition, String adUrl, String adLocation,
            String filmId, String filmName, String filmPlayProgress) {
        return mRemoteDataSource.reportPlayAdLoad(adId, adName, adType, adDuration,
                adMediaIp, adOwnerCode, adOwnerName, adDefinition, adUrl, adLocation,
                filmId, filmName, filmPlayProgress);
    }

    @Override
    public Observable<ResponseBody> reportPlayAdBlocked(String adId, String adName,
            String adType, String bufferDuration, String adDuration, String adProgress,
            String adMediaIp, String adOwnerCode, String adOwnerName, String adDefinition,
            String adUrl, String adLocation, String filmId, String filmName,
            String filmPlayProgress) {
        return mRemoteDataSource.reportPlayAdBlocked(adId, adName, adType, bufferDuration,
                adDuration, adProgress, adMediaIp, adOwnerCode, adOwnerName, adDefinition,
                adUrl, adLocation, filmId, filmName, filmPlayProgress);
    }

    @Override
    public Observable<ResponseBody> reportHardwareInfo(String wirelessMac, String wireMac,
            String bluetoothMac, String sn, String cpuId, String deviceId, String deviceName,
            String deviceType, String memory, String storage, String density, String resolution,
            String screenSize) {
        return mRemoteDataSource.reportHardwareInfo(wirelessMac, wireMac, bluetoothMac, sn, cpuId,
                deviceId, deviceName, deviceType, memory, storage, density, resolution, screenSize);
    }

    @Override
    public Observable<ResponseBody> reportAdThirdExposure(int adType, String adCode,
            String materialCode,
            String showTime, String showType, String pkgName, String activityName) {
        return mRemoteDataSource.reportAdThirdExposure(adType, adCode, materialCode, showTime,
                showType, pkgName, activityName);
    }
}
