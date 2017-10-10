package com.golive.cinema.statistics;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.data.source.MainConfigRepository;
import com.golive.cinema.data.source.StatisticsRepository;
import com.golive.cinema.data.source.remote.StatisticsRemoteDataSource;
import com.golive.cinema.init.domain.usecase.GetMainConfigUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportAppExceptionUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportAppExitUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportAppStartUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportClickUserCenterTopUpUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportClickUserCenterVipUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportEnterActivityUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportEnterAdUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportEnterEventUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportEnterFilmDetailUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportEnterUserCenterUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportExitActivityUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportExitAdUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportExitEventUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportExitFilmDetailUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportExitPromptUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportExitUserCenterUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportExitWatchNoticeUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportHardwareInfoUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportPlayAdBlockedUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportPlayAdExceptionUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportPlayAdExitUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportPlayAdLoadUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportPlayAdStartUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportThirdAdvertExposureUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportThirdPartyAdUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportVideoBlockUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportVideoExceptionUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportVideoExitUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportVideoLoadUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportVideoPlayPauseUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportVideoSeekUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportVideoStartUseCase;
import com.golive.cinema.statistics.domain.usecase.ReportVideoStreamSwitchUseCase;
import com.initialjie.log.Logger;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Wangzj on 2016/12/24.
 */

public class StatisticsPresenter extends BasePresenter<StatisticsContract.View> implements
        StatisticsContract.Presenter {

    private final GetMainConfigUseCase mGetMainConfigUseCase;
    private final ReportAppExceptionUseCase mReportAppExceptionUseCase;
    private final ReportAppStartUseCase mReportAppStartUseCase;
    private final ReportAppExitUseCase mReportAppExitUseCase;
    private final ReportEnterActivityUseCase mReportEnterActivityUseCase;
    private final ReportExitActivityUseCase mReportExitActivityUseCase;
    private final ReportHardwareInfoUseCase mReportHardwareInfoUseCase;
    private final ReportEnterFilmDetailUseCase mEnterFilmDetailUseCase;
    private final ReportExitFilmDetailUseCase mExitFilmDetailUseCase;
    private final ReportExitPromptUseCase mReportExitPromptUseCase;
    private final ReportExitWatchNoticeUseCase mReportExitWatchNoticeUseCase;
    private final ReportClickUserCenterTopUpUseCase mReportClickUserCenterTopUpUseCase;
    private final ReportClickUserCenterVipUseCase mReportClickUserCenterVipUseCase;
    private final ReportEnterUserCenterUseCase mReportEnterUserCenterUseCase;
    private final ReportExitUserCenterUseCase mReportExitUserCenterUseCase;
    private final ReportEnterEventUseCase mReportEnterEventUseCase;
    private final ReportExitEventUseCase mReportExitEventUseCase;
    private final ReportEnterAdUseCase mReportEnterAdUseCase;
    private final ReportExitAdUseCase mReportExitAdUseCase;
    private final ReportThirdPartyAdUseCase mReportThirdPartyAdUseCase;
    private final ReportPlayAdBlockedUseCase mReportPlayAdBlockedUseCase;
    private final ReportPlayAdExceptionUseCase mReportPlayAdExceptionUseCase;
    private final ReportPlayAdExitUseCase mReportPlayAdExitUseCase;
    private final ReportPlayAdLoadUseCase mReportPlayAdLoadUseCase;
    private final ReportPlayAdStartUseCase mReportPlayAdStartUseCase;
    private final ReportVideoLoadUseCase mReportVideoLoadUseCase;
    private final ReportVideoStartUseCase mReportVideoStartUseCase;
    private final ReportVideoPlayPauseUseCase mReportVideoPlayPauseUseCase;
    private final ReportVideoSeekUseCase mReportVideoSeekUseCase;
    private final ReportVideoStreamSwitchUseCase mReportVideoStreamSwitchUseCase;
    private final ReportVideoBlockUseCase mReportVideoBlockUseCase;
    private final ReportVideoExceptionUseCase mReportVideoExceptionUseCase;
    private final ReportVideoExitUseCase mReportVideoExitUseCase;
    private final ReportThirdAdvertExposureUseCase mReportThirdAdvertExposureUseCase;

    public StatisticsPresenter(@NonNull StatisticsContract.View view,
            @NonNull GetMainConfigUseCase getMainConfigUseCase,
            @NonNull ReportAppExceptionUseCase reportAppExceptionUseCase,
            @NonNull ReportAppStartUseCase reportAppStartUseCase,
            @NonNull ReportAppExitUseCase reportAppExitUseCase,
            @NonNull ReportEnterActivityUseCase reportEnterActivityUseCase,
            @NonNull ReportExitActivityUseCase reportExitActivityUseCase,
            @NonNull ReportHardwareInfoUseCase reportHardwareInfoUseCase,
            @NonNull ReportEnterFilmDetailUseCase enterFilmDetailUseCase,
            @NonNull ReportExitFilmDetailUseCase reportExitFilmDetailUseCase,
            @NonNull ReportExitPromptUseCase reportExitPromptUseCase,
            @NonNull ReportExitWatchNoticeUseCase reportExitWatchNoticeUseCase,
            @NonNull ReportClickUserCenterTopUpUseCase reportClickUserCenterTopUpUseCase,
            @NonNull ReportClickUserCenterVipUseCase reportClickUserCenterVipUseCase,
            @NonNull ReportEnterUserCenterUseCase reportEnterUserCenterUseCase,
            @NonNull ReportExitUserCenterUseCase reportExitUserCenterUseCase,
            @NonNull ReportEnterEventUseCase reportEnterEventUseCase,
            @NonNull ReportExitEventUseCase reportExitEventUseCase,
            @NonNull ReportEnterAdUseCase reportEnterAdUseCase,
            @NonNull ReportExitAdUseCase reportExitAdUseCase,
            @NonNull ReportThirdPartyAdUseCase reportThirdPartyAdUseCase,
            @NonNull ReportPlayAdStartUseCase reportPlayAdStartUseCase,
            @NonNull ReportPlayAdLoadUseCase reportPlayAdLoadUseCase,
            @NonNull ReportPlayAdBlockedUseCase reportPlayAdBlockedUseCase,
            @NonNull ReportPlayAdExceptionUseCase reportPlayAdExceptionUseCase,
            @NonNull ReportPlayAdExitUseCase reportPlayAdExitUseCase,
            @NonNull ReportVideoStartUseCase reportVideoStartUseCase,
            @NonNull ReportVideoLoadUseCase reportVideoLoadUseCase,
            @NonNull ReportVideoPlayPauseUseCase reportVideoPlayPauseUseCase,
            @NonNull ReportVideoSeekUseCase reportVideoSeekUseCase,
            @NonNull ReportVideoStreamSwitchUseCase reportVideoStreamSwitchUseCase,
            @NonNull ReportVideoBlockUseCase reportVideoBlockUseCase,
            @NonNull ReportVideoExceptionUseCase reportVideoExceptionUseCase,
            @NonNull ReportVideoExitUseCase reportVideoExitUseCase,
            @NonNull ReportThirdAdvertExposureUseCase reportThirdAdvertExposureUseCase) {
        attachView(view);
        view.setPresenter(this);
        mGetMainConfigUseCase = checkNotNull(getMainConfigUseCase);
        mReportAppExceptionUseCase = checkNotNull(reportAppExceptionUseCase);
        mReportAppStartUseCase = checkNotNull(reportAppStartUseCase);
        mReportAppExitUseCase = checkNotNull(reportAppExitUseCase);
        mReportEnterActivityUseCase = checkNotNull(reportEnterActivityUseCase);
        mReportExitActivityUseCase = checkNotNull(reportExitActivityUseCase);
        mEnterFilmDetailUseCase = checkNotNull(enterFilmDetailUseCase);
        mExitFilmDetailUseCase = checkNotNull(reportExitFilmDetailUseCase);
        mReportHardwareInfoUseCase = checkNotNull(reportHardwareInfoUseCase);
        mReportExitPromptUseCase = checkNotNull(reportExitPromptUseCase);
        mReportExitWatchNoticeUseCase = checkNotNull(reportExitWatchNoticeUseCase);
        mReportClickUserCenterTopUpUseCase = checkNotNull(reportClickUserCenterTopUpUseCase);
        mReportClickUserCenterVipUseCase = checkNotNull(reportClickUserCenterVipUseCase);
        mReportEnterUserCenterUseCase = checkNotNull(reportEnterUserCenterUseCase);
        mReportExitUserCenterUseCase = checkNotNull(reportExitUserCenterUseCase);
        mReportEnterEventUseCase = checkNotNull(reportEnterEventUseCase);
        mReportExitEventUseCase = checkNotNull(reportExitEventUseCase);
        mReportEnterAdUseCase = checkNotNull(reportEnterAdUseCase);
        mReportExitAdUseCase = checkNotNull(reportExitAdUseCase);
        mReportThirdPartyAdUseCase = checkNotNull(reportThirdPartyAdUseCase);
        mReportPlayAdBlockedUseCase = checkNotNull(reportPlayAdBlockedUseCase);
        mReportPlayAdExceptionUseCase = checkNotNull(reportPlayAdExceptionUseCase);
        mReportPlayAdExitUseCase = checkNotNull(reportPlayAdExitUseCase);
        mReportPlayAdLoadUseCase = checkNotNull(reportPlayAdLoadUseCase);
        mReportPlayAdStartUseCase = checkNotNull(reportPlayAdStartUseCase);
        mReportVideoExceptionUseCase = checkNotNull(reportVideoExceptionUseCase);
        mReportVideoExitUseCase = checkNotNull(reportVideoExitUseCase);
        mReportVideoPlayPauseUseCase = checkNotNull(reportVideoPlayPauseUseCase);
        mReportVideoSeekUseCase = checkNotNull(reportVideoSeekUseCase);
        mReportVideoStartUseCase = checkNotNull(reportVideoStartUseCase);
        mReportVideoLoadUseCase = checkNotNull(reportVideoLoadUseCase);
        mReportVideoBlockUseCase = checkNotNull(reportVideoBlockUseCase);
        mReportVideoStreamSwitchUseCase = checkNotNull(reportVideoStreamSwitchUseCase);
        mReportThirdAdvertExposureUseCase = checkNotNull(reportThirdAdvertExposureUseCase);
    }

    @Override
    public void refreshReport() {
        // clear data source cache
        MainConfigRepository.destroyInstance();
//        MainConfigRemoteDataSource.destroyInstance();
        StatisticsRepository.destroyInstance();
        StatisticsRemoteDataSource.destroyInstance();
//        Subscription subscription = mGetMainConfigUseCase.run(
//                new GetMainConfigUseCase.RequestValues(true))
//                .subscribe(new MySubscriber("refreshReport"));
//        addSubscription(subscription);
    }

    @Override
    public void reportAppException(String exceptionType, String exceptionCode,
            String exceptionMsg, String exceptionLevel, String partnerId) {
        Subscription subscription = mReportAppExceptionUseCase.run(
                new ReportAppExceptionUseCase.RequestValues(exceptionCode, exceptionCode,
                        exceptionMsg, exceptionLevel, partnerId))
                .subscribe(new MySubscriber("reportAppException"));
        addSubscription(subscription);
    }

    @Override
    public void reportAppStart(String caller, String destination, String netType, String osVersion,
            String versionCode, String filmId, String userStatus, String kdmVersion,
            String duration) {
        ReportAppStartUseCase.RequestValues requestValues = new ReportAppStartUseCase.RequestValues(
                caller, destination, netType, osVersion, versionCode, filmId, userStatus,
                kdmVersion, duration);
        Subscription subscription = mReportAppStartUseCase.run(requestValues)
                .subscribe(new MySubscriber("reportAppStart"));
        addSubscription(subscription);
    }

    @Override
    public void reportAppExit(String duration) {
        Subscription subscription = mReportAppExitUseCase.run(
                new ReportAppExitUseCase.RequestValues(duration))
                .subscribe(new MySubscriber("reportAppExit"));
        addSubscription(subscription);
    }

    @Override
    public void reportHardwareInfo(String wirelessMac, String wireMac, String bluetoothMac,
            String sn, String cpuId, String deviceId, String deviceName, String deviceType,
            String memory, String storage, String density, String resolution, String screenSize) {
        Subscription subscription = mReportHardwareInfoUseCase.run(
                new ReportHardwareInfoUseCase.RequestValues(wirelessMac, wireMac, bluetoothMac, sn,
                        cpuId, deviceId, deviceName, deviceType, memory, storage, density,
                        resolution, screenSize))
                .subscribe(new MySubscriber("reportHardwareInfo"));
        addSubscription(subscription);
    }

    @Override
    public void reportEnterActivity(String code, String filmName, String from) {
        Subscription subscription = mReportEnterActivityUseCase.run(
                new ReportEnterActivityUseCase.RequestValues(code, filmName, from))
                .subscribe(new MySubscriber("reportEnterActivity"));
        addSubscription(subscription);
    }

    @Override
    public void reportExitActivity(String code, String filmName, String to,
            String duration) {
        Subscription subscription = mReportExitActivityUseCase.run(new ReportExitActivityUseCase
                .RequestValues(code, filmName, to, duration))
                .subscribe(new MySubscriber("reportExitActivity"));
        addSubscription(subscription);
    }

    @Override
    public void reportEnterFilmDetail(String source, String title, String id, String status) {
        ReportEnterFilmDetailUseCase.RequestValues requestValues =
                new ReportEnterFilmDetailUseCase.RequestValues(id, title, status, source);
        Subscription subscription = mEnterFilmDetailUseCase.run(requestValues)
                .subscribe(new MySubscriber("reportEnterFilmDetail"));
        addSubscription(subscription);
    }

    @Override
    public void reportExitFilmDetail(String id, String title, String to, String duration,
            String status) {
        ReportExitFilmDetailUseCase.RequestValues requestValues =
                new ReportExitFilmDetailUseCase.RequestValues(id, title, status, to, duration);
        Subscription subscription = mExitFilmDetailUseCase.run(requestValues)
                .subscribe(new MySubscriber("reportExitFilmDetail"));
        addSubscription(subscription);
    }

    @Override
    public void reportExitPrompt(String button, String type, String duration) {
        Subscription subscription = mReportExitPromptUseCase.run(new ReportExitPromptUseCase
                .RequestValues(button, duration, type))
                .subscribe(new MySubscriber("reportExitPrompt"));
        addSubscription(subscription);
    }

    @Override
    public void reportExitWatchNotice(String button, String type, String duration) {
        Subscription subscription = mReportExitWatchNoticeUseCase.run(
                new ReportExitWatchNoticeUseCase.RequestValues(button, duration, type))
                .subscribe(new MySubscriber("reportExitWatchNotice"));
        addSubscription(subscription);
    }

    @Override
    public void reportVideoStart(String filmId, String filmName, String filmType, String watchType,
            String definition, String orderSerial, String valueType, String caller) {
        Subscription subscription = mReportVideoStartUseCase.run(new ReportVideoStartUseCase
                .RequestValues(filmId, filmName, definition, filmType, watchType, orderSerial,
                valueType, caller))
                .subscribe(new MySubscriber("reportVideoStart"));
        addSubscription(subscription);
    }

    @Override
    public void reportVideoLoad(String filmId, String filmName, String filmType, String watchType,
            String videoUrl, String bufferDuration, String definition, String mediaIp,
            String orderSerial, String valueType, String speed) {
        Subscription subscription = mReportVideoLoadUseCase.run(new ReportVideoLoadUseCase
                .RequestValues(filmId, filmName, filmType, watchType, videoUrl, mediaIp, definition,
                bufferDuration, orderSerial, valueType, speed))
                .subscribe(new MySubscriber("reportVideoLoad"));
        addSubscription(subscription);
    }

    @Override
    public void reportVideoExit(String filmId, String filmName, String filmType, String watchType,
            String definition, String watchDuration, String playDuration, String totalDuration,
            String playProgress, String orderSerial, String valueType) {
        Subscription subscription = mReportVideoExitUseCase.run(new ReportVideoExitUseCase
                .RequestValues(filmId, filmName, filmType, watchType, definition, watchDuration,
                playDuration, totalDuration, playProgress, orderSerial, valueType))
                .subscribe(new MySubscriber("reportVideoExit"));
        addSubscription(subscription);
    }

    @Override
    public void reportVideoPlayPause(String filmId, String filmName, String filmType,
            String watchType, String definition, String pauseDuration, String watchDuration,
            String playDuration, String totalDuration, String playProgress, String orderSerial,
            String valueType) {
        Subscription subscription = mReportVideoPlayPauseUseCase.run(
                new ReportVideoPlayPauseUseCase.RequestValues(filmId, filmName, filmType, watchType,
                        definition, pauseDuration, watchDuration, playDuration, totalDuration,
                        playProgress, orderSerial, valueType))
                .subscribe(new MySubscriber("reportVideoPlayPause"));
        addSubscription(subscription);
    }

    @Override
    public void reportVideoSeek(String filmId, String filmName, String filmType, String watchType,
            String definition, String playProgress, String seekToPosition, String toType,
            String orderSerial, String valueType) {
        Subscription subscription = mReportVideoSeekUseCase.run(new ReportVideoSeekUseCase
                .RequestValues(filmId, filmName, definition, filmType, watchType, playProgress,
                seekToPosition, toType, orderSerial, valueType))
                .subscribe(new MySubscriber("reportVideoSeek"));
        addSubscription(subscription);
    }

    @Override
    public void reportVideoBlock(String filmId, String filmName, String filmType, String watchType,
            String definition, String mediaIp, String bufferDuration, String watchDuration,
            String playDuration, String totalDuration, String playProgress, String orderSerial,
            String valueType) {
        Subscription subscription = mReportVideoBlockUseCase.run(new ReportVideoBlockUseCase
                .RequestValues(filmId, filmName, filmType, watchType, definition, mediaIp,
                bufferDuration, watchDuration, playDuration, totalDuration, playProgress,
                orderSerial, valueType))
                .subscribe(new MySubscriber("reportVideoBlock"));
        addSubscription(subscription);
    }

    @Override
    public void reportVideoStreamSwitch(String filmId, String filmName, String filmType,
            String watchType, String definition, String toDefinition, String watchDuration,
            String playDuration, String totalDuration, String playProgress, String orderSerial,
            String valueType) {
        Subscription subscription = mReportVideoStreamSwitchUseCase.run(
                new ReportVideoStreamSwitchUseCase.RequestValues(filmId, filmName, filmType,
                        watchType, definition, toDefinition, watchDuration, playDuration,
                        totalDuration, playProgress, orderSerial, valueType))
                .subscribe(new MySubscriber("reportVideoStreamSwitch"));
        addSubscription(subscription);
    }

    @Override
    public void reportVideoException(String filmId, String filmName, String filmType,
            String watchType, String definition, String errCode, String errMsg,
            String watchDuration, String playDuration, String totalDuration, String playProgress,
            String orderSerial, String valueType) {
        Subscription subscription = mReportVideoExceptionUseCase.run(
                new ReportVideoExceptionUseCase.RequestValues(filmId, filmName, filmType, watchType,
                        definition, errMsg, errCode, watchDuration, playDuration, totalDuration,
                        playProgress, orderSerial, valueType))
                .subscribe(new MySubscriber("reportVideoException"));
        addSubscription(subscription);
    }

    @Override
    public void reportEnterAd(String caller) {
        Subscription subscription = mReportEnterAdUseCase.run(
                new ReportEnterAdUseCase.RequestValues(caller))
                .subscribe(new MySubscriber("reportEnterAd"));
        addSubscription(subscription);
    }

    @Override
    public void reportExitAd(String caller, String duration) {
        Subscription subscription = mReportExitAdUseCase.run(
                new ReportExitAdUseCase.RequestValues(caller, duration))
                .subscribe(new MySubscriber("reportExitAd"));
        addSubscription(subscription);
    }

    @Override
    public void reportThirdPartyAd(String reportUrl) {
        Subscription subscription = mReportThirdPartyAdUseCase.run(new ReportThirdPartyAdUseCase
                .RequestValues(reportUrl))
                .subscribe(new MySubscriber("reportThirdPartyAd"));
        addSubscription(subscription);
    }

    @Override
    public void reportPlayAdStart(String adId, String adName, String adType, String adUrl,
            String adDefinition, String adLocation, String adOwnerCode, String adOwnerName,
            String filmId, String filmName, String filmPlayProgress) {
        Subscription subscription = mReportPlayAdStartUseCase.run(new ReportPlayAdStartUseCase
                .RequestValues(adId, adName, adType, adOwnerCode, adOwnerName,
                adDefinition, adUrl, adLocation, filmId, filmName, filmPlayProgress))
                .subscribe(new MySubscriber("reportPlayAdStart"));
        addSubscription(subscription);
    }

    @Override
    public void reportPlayAdLoad(String adId, String adName, String adType, String adUrl,
            String adMediaIp, String adDefinition, String adLocation,
            String adProgress, String bufferDuration, String adOwnerCode, String adOwnerName,
            String filmId, String filmName, String filmPlayProgress) {
        Subscription subscription = mReportPlayAdLoadUseCase.run(new ReportPlayAdLoadUseCase
                .RequestValues(adId, adName, adType, bufferDuration, adMediaIp, adOwnerCode,
                adOwnerName,
                adDefinition, adUrl, adLocation, filmId, filmName, filmPlayProgress))
                .subscribe(new MySubscriber("reportPlayAdLoad"));
        addSubscription(subscription);
    }

    @Override
    public void reportPlayAdBlocked(String adId, String adName, String adType, String adUrl,
            String adMediaIp, String adDefinition, String adLocation, String adDuration,
            String adProgress, String bufferDuration, String adOwnerCode, String adOwnerName,
            String filmId, String filmName, String filmPlayProgress) {
        Subscription subscription = mReportPlayAdBlockedUseCase.run(
                new ReportPlayAdBlockedUseCase.RequestValues(adId, adName, adType, bufferDuration,
                        adDuration, adProgress, adMediaIp, adOwnerCode, adOwnerName,
                        adDefinition, adUrl, adLocation, filmName, filmId, filmPlayProgress))
                .subscribe(new MySubscriber("reportPlayAdBlocked"));
        addSubscription(subscription);
    }

    @Override
    public void reportPlayAdException(String adId, String adName, String adType, String adUrl,
            String adDefinition, String adLocation, String errCode, String errMsg,
            String adOwnerCode, String adOwnerName, String filmId, String filmName,
            String filmPlayProgress) {
        Subscription subscription = mReportPlayAdExceptionUseCase.run(
                new ReportPlayAdExceptionUseCase.RequestValues(adId, adName, adType,
                        errCode, errMsg, adOwnerCode, adOwnerName, adDefinition,
                        adUrl, adLocation, filmId, filmName, filmPlayProgress))
                .subscribe(new MySubscriber("reportPlayAdException"));
        addSubscription(subscription);
    }

    @Override
    public void reportPlayAdExit(String adId, String adName, String adType, String adUrl,
            String adDefinition, String adLocation, String adDuration, String adProgress,
            String bufferDuration, String adOwnerCode, String adOwnerName, String filmId,
            String filmName, String filmPlayProgress) {
        Subscription subscription = mReportPlayAdExitUseCase.run(new ReportPlayAdExitUseCase
                .RequestValues(adId, adName, adType, bufferDuration, adDuration,
                adProgress, adOwnerCode, adOwnerName, adDefinition, adUrl,
                adLocation, filmId, filmName, filmPlayProgress))
                .subscribe(new MySubscriber("reportPlayAdExit"));
        addSubscription(subscription);
    }

    @Override
    public void reportAdThirdExposure(int adType, String adCode, String materialCode,
            String showTime, String showType, String pkgName, String activityName) {
        Subscription subscription = mReportThirdAdvertExposureUseCase.run(
                new ReportThirdAdvertExposureUseCase
                        .RequestValues(adType, adCode, materialCode, showTime,
                        showType, pkgName, activityName))
                .subscribe(new MySubscriber("reportAdThirdExposure"));
        addSubscription(subscription);
    }

    @Override
    public void reportEnterUserCenter() {
        Subscription subscription = mReportEnterUserCenterUseCase.run(
                new ReportEnterUserCenterUseCase.RequestValues())
                .subscribe(new MySubscriber("reportEnterUserCenter"));
        addSubscription(subscription);
    }

    @Override
    public void reportExitUserCenter(String duration) {
        Subscription subscription = mReportExitUserCenterUseCase.run(
                new ReportExitUserCenterUseCase.RequestValues(duration))
                .subscribe(new MySubscriber("reportExitUserCenter"));
        addSubscription(subscription);
    }

    @Override
    public void reportClickUserCenterTopUp(String price, String accountBalance, String button) {
        Subscription subscription = mReportClickUserCenterTopUpUseCase.run(
                new ReportClickUserCenterTopUpUseCase.RequestValues(price, accountBalance, button))
                .subscribe(new MySubscriber("reportClickUserCenterTopUp"));
        addSubscription(subscription);
    }

    @Override
    public void reportClickUserCenterVip(String price, String accountBalance, String button) {
        Subscription subscription = mReportClickUserCenterVipUseCase.run(
                new ReportClickUserCenterVipUseCase.RequestValues(price, accountBalance, button))
                .subscribe(new MySubscriber("reportClickUserCenterVip"));
        addSubscription(subscription);
    }

    @Override
    public void reportEnterEvent(String filmId, String filmName, String caller) {
        Subscription subscription = mReportEnterEventUseCase.run(new ReportEnterEventUseCase
                .RequestValues(caller, filmName, filmId))
                .subscribe(new MySubscriber("reportEnterEvent"));
        addSubscription(subscription);
    }

    @Override
    public void reportExitEvent(String filmId, String filmName, String destination,
            String duration) {
        Subscription subscription = mReportExitEventUseCase.run(new ReportExitEventUseCase
                .RequestValues(filmName, filmId, duration, destination))
                .subscribe(new MySubscriber("reportExitEvent"));
        addSubscription(subscription);
    }

    private class MySubscriber extends Subscriber<Object> {

        private final String mReportType;

        public MySubscriber(String reportType) {
            mReportType = reportType;
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Logger.w(e, mReportType + ", onError : ");
        }

        @Override
        public void onNext(Object o) {
        }
    }
}
