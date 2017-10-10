package com.golive.cinema.data.source;

import com.golive.network.entity.Location;
import com.golive.network.entity.Response;

import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Statistics data source
 * Created by Wangzj on 2016/12/21.
 */

public interface StatisticsDataSource {

    Observable<Location> getLocation();

    Observable<Response> reportAppException(String exceptionType, String exceptionCode,
            String exceptionMsg, String exceptionLevel, String partnerId);

    Observable<ResponseBody> reportAppStart(String caller, String destination, String userStatus,
            String province, String city, String net, String os, String versionCode,
            String kdmVersion, String duration);

    Observable<ResponseBody> reportAppExit(String duration);

    Observable<ResponseBody> reportEnterActivity(String code, String filmName, String from);

    Observable<ResponseBody> reportExitActivity(String code, String filmName, String to,
            String duration);

    Observable<ResponseBody> reportVideoStart(String filmId, String filmName, String definition,
            String filmType, String watchType, String orderSerial, String valueType, String caller);

    Observable<ResponseBody> reportVideoLoad(String filmId, String filmName, String filmType,
            String watchType, String mediaUrl, String mediaIp, String definition,
            String bufferDuration, String orderSerial, String valueType, String speed);

    Observable<ResponseBody> reportVideoStreamSwitch(String filmId, String filmName,
            String filmType, String watchType, String definition, String toDefinition,
            String watchDuration, String playDuration, String totalDuration, String playProgress,
            String orderSerial, String valueType);

    Observable<ResponseBody> reportVideoPlayPause(String filmId, String filmName,
            String filmType, String watchType, String definition, String pauseDuration,
            String watchDuration, String playDuration, String totalDuration, String playProgress,
            String orderSerial, String valueType);

    Observable<ResponseBody> reportVideoSeek(String filmId, String filmName, String filmType,
            String watchType, String definition, String playProgress, String toPosition,
            String toType,
            String orderSerial, String valueType);

    Observable<ResponseBody> reportVideoBlocked(String filmId, String filmName, String filmType,
            String watchType, String definition, String mediaIp, String bufferDuration,
            String watchDuration, String playDuration, String totalDuration, String playProgress,
            String orderSerial, String valueType);

    Observable<ResponseBody> reportVideoException(String filmId, String filmName,
            String filmType, String watchType, String definition, String errCode, String errMsg,
            String watchDuration, String playDuration, String totalDuration, String playProgress,
            String orderSerial, String valueType);

    Observable<ResponseBody> reportVideoExit(String filmId, String filmName, String filmType,
            String watchType, String definition, String watchDuration, String playDuration,
            String totalDuration, String playProgress, String orderSerial, String valueType);

    Observable<ResponseBody> reportEnterFilmDetail(String filmId, String filmName,
            String filmStatus, String caller);

    Observable<ResponseBody> reportExitFilmDetail(String filmId, String filmName, String filmStatus,
            String destination, String duration);

    Observable<ResponseBody> reportEnterEvent(String filmId, String filmName, String caller);

    Observable<ResponseBody> reportExitEvent(String filmId, String filmName, String destination,
            String duration);

    Observable<ResponseBody> reportEnterUserCenter();

    Observable<ResponseBody> reportExitUserCenter(String duration);

    Observable<ResponseBody> reportClickUserCenterTopUp(String price, String accountBalance,
            String button);

    Observable<ResponseBody> reportClickUserCenterVip(String price, String accountBalance,
            String button);

    Observable<ResponseBody> reportEnterAd(String caller);

    Observable<ResponseBody> reportExitAd(String caller, String duration);

    Observable<ResponseBody> reportThirdPartyAd(String reportUrl);

    Observable<ResponseBody> reportExitWatchNotice(String button, String duration, String type);

    Observable<ResponseBody> reportExitPrompt(String button, String duration, String type);

    Observable<ResponseBody> reportPlayAdStart(String adId, String adName, String adType,
            String adOwnerCode, String adOwnerName, String adDefinition, String adUrl,
            String adLocation, String filmId, String filmName, String filmPlayProgress);

    Observable<ResponseBody> reportPlayAdException(String adId, String adName, String adType,
            String errCode, String errMsg, String adOwnerCode, String adOwnerName,
            String adDefinition, String adUrl, String adLocation, String filmId, String filmName,
            String filmPlayProgress);

    Observable<ResponseBody> reportPlayAdExit(String adId, String adName,
            String adType, String bufferDuration, String adDuration, String adProgress,
            String adOwnerCode, String adOwnerName, String adDefinition, String adUrl,
            String adLocation, String filmId, String filmName, String filmPlayProgress);

    Observable<ResponseBody> reportPlayAdLoad(String adId, String adName, String adType,
            String adDuration, String adMediaIp, String adOwnerCode, String adOwnerName,
            String adDefinition, String adUrl, String adLocation, String filmId, String filmName,
            String filmPlayProgress);

    Observable<ResponseBody> reportPlayAdBlocked(String adId, String adName,
            String adType, String bufferDuration, String adDuration, String adProgress,
            String adMediaIp, String adOwnerCode, String adOwnerName, String adDefinition,
            String adUrl, String adLocation, String filmId, String filmName,
            String filmPlayProgress);

    Observable<ResponseBody> reportHardwareInfo(String wirelessMac, String wireMac,
            String bluetoothMac, String sn, String cpuId, String deviceId, String deviceName,
            String deviceType, String memory, String storage, String density, String resolution,
            String screenSize);

    Observable<ResponseBody> reportAdThirdExposure(int adType, String adCode, String materialCode,
            String showTime, String showType, String pkgName, String activityName);
}