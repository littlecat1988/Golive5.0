/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.golive.cinema;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.golive.cinema.advert.domain.usecase.AdvertUseCase;
import com.golive.cinema.data.source.DownloadDataSource;
import com.golive.cinema.data.source.DownloadRepository;
import com.golive.cinema.data.source.FilmLibraryDataSource;
import com.golive.cinema.data.source.FilmLibraryRepository;
import com.golive.cinema.data.source.FilmTopicsDataSource;
import com.golive.cinema.data.source.FilmTopicsRepository;
import com.golive.cinema.data.source.FilmsDataSource;
import com.golive.cinema.data.source.FilmsRepository;
import com.golive.cinema.data.source.HttpDataSource;
import com.golive.cinema.data.source.KdmDataSource;
import com.golive.cinema.data.source.KdmRepository;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.data.source.MainConfigRepository;
import com.golive.cinema.data.source.OrdersDataSource;
import com.golive.cinema.data.source.OrdersRepository;
import com.golive.cinema.data.source.PlayerDataSource;
import com.golive.cinema.data.source.PlayerRepository;
import com.golive.cinema.data.source.RecommendDataSource;
import com.golive.cinema.data.source.RecommendRepository;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.data.source.ServerInitRepository;
import com.golive.cinema.data.source.StatisticsDataSource;
import com.golive.cinema.data.source.StatisticsRepository;
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.data.source.UserRepository;
import com.golive.cinema.data.source.VerifyCodeDataSource;
import com.golive.cinema.data.source.VerifyCodeRepository;
import com.golive.cinema.data.source.local.FilmLibraryLocalDataSource;
import com.golive.cinema.data.source.local.FilmTopicsLocalDataSource;
import com.golive.cinema.data.source.local.FilmsLocalDataSource;
import com.golive.cinema.data.source.local.KdmLocalDataSource;
import com.golive.cinema.data.source.local.MainConfigLocalDataSource;
import com.golive.cinema.data.source.local.OrdersLocalDataSource;
import com.golive.cinema.data.source.local.RecommendLocalDataSource;
import com.golive.cinema.data.source.local.ServerInitLocalDataSource;
import com.golive.cinema.data.source.local.UserLocalDataSource;
import com.golive.cinema.data.source.remote.FilmLibraryRemoteDataSource;
import com.golive.cinema.data.source.remote.FilmTopicsRemoteDataSource;
import com.golive.cinema.data.source.remote.FilmsRemoteDataSource;
import com.golive.cinema.data.source.remote.HttpRepository;
import com.golive.cinema.data.source.remote.KdmRemoteDataSource;
import com.golive.cinema.data.source.remote.MainConfigRemoteDataSource;
import com.golive.cinema.data.source.remote.OrdersRemoteDataSource;
import com.golive.cinema.data.source.remote.PlayerRemoteDataSource;
import com.golive.cinema.data.source.remote.RecommendRemoteDataSource;
import com.golive.cinema.data.source.remote.ServerInitRemoteDataSource;
import com.golive.cinema.data.source.remote.StatisticsRemoteDataSource;
import com.golive.cinema.data.source.remote.UserRemoteDataSource;
import com.golive.cinema.data.source.remote.VerifyCodeRemoteDataSource;
import com.golive.cinema.download.DownloadManager;
import com.golive.cinema.download.domain.usecase.AddDownloadTaskUseCase;
import com.golive.cinema.download.domain.usecase.CheckDownloadFinishUseCase;
import com.golive.cinema.download.domain.usecase.DeleteDownloadTaskUseCase;
import com.golive.cinema.download.domain.usecase.DownloadFileUseCase;
import com.golive.cinema.download.domain.usecase.GetDownloadTaskInfoUseCase;
import com.golive.cinema.download.domain.usecase.PauseAllDownloadTaskUseCase;
import com.golive.cinema.download.domain.usecase.PauseDownloadTaskUseCase;
import com.golive.cinema.download.domain.usecase.ResumeDownloadTaskUseCase;
import com.golive.cinema.filmdetail.domain.usecase.GetFilmDetailUseCase;
import com.golive.cinema.filmlibrary.dimain.usecase.GetFilmLibListUseCase;
import com.golive.cinema.filmlibrary.dimain.usecase.GetFilmLibTabUseCase;
import com.golive.cinema.films.domain.usecase.GetFilmListUseCase;
import com.golive.cinema.films.domain.usecase.GetFilmsUseCase;
import com.golive.cinema.http.domain.usecase.HttpGetUseCase;
import com.golive.cinema.init.domain.usecase.ActivityImageUseCase;
import com.golive.cinema.init.domain.usecase.AppPageUseCase;
import com.golive.cinema.init.domain.usecase.BootImageUseCase;
import com.golive.cinema.init.domain.usecase.ExitComboUseCase;
import com.golive.cinema.init.domain.usecase.ExitDrainageUseCase;
import com.golive.cinema.init.domain.usecase.GetMainConfigUseCase;
import com.golive.cinema.init.domain.usecase.GetShutdownMessageUseCase;
import com.golive.cinema.init.domain.usecase.GuideTypeUseCase;
import com.golive.cinema.init.domain.usecase.LoginAgainUseCase;
import com.golive.cinema.init.domain.usecase.RepeatMacUseCase;
import com.golive.cinema.init.domain.usecase.ServerStatusUseCase;
import com.golive.cinema.init.domain.usecase.UpgradeUseCase;
import com.golive.cinema.init.domain.usecase.VerifyCodeUseCase;
import com.golive.cinema.login.domain.usecase.LoginUseCase;
import com.golive.cinema.order.domain.usecase.CreateOrderUseCase;
import com.golive.cinema.order.domain.usecase.GetValidOrderUseCase;
import com.golive.cinema.order.domain.usecase.PayCreditOrderUseCase;
import com.golive.cinema.order.domain.usecase.PayOrderUseCase;
import com.golive.cinema.order.domain.usecase.RefreshOrderUseCase;
import com.golive.cinema.order.domain.usecase.ReportTicketStatusUseCase;
import com.golive.cinema.player.domain.usecase.GetKdmInitUseCase;
import com.golive.cinema.player.domain.usecase.GetKdmServerVersionUseCase;
import com.golive.cinema.player.domain.usecase.GetKdmVersionUseCase;
import com.golive.cinema.player.domain.usecase.GetPlayTicketUseCase;
import com.golive.cinema.player.domain.usecase.GetPlayTokenUseCase;
import com.golive.cinema.player.domain.usecase.GetPlaybackValidityUseCase;
import com.golive.cinema.player.domain.usecase.NotifyKdmReadyUseCase;
import com.golive.cinema.player.domain.usecase.ReportAdvertMiaozhenUseCase;
import com.golive.cinema.player.domain.usecase.UpgradeKdmUseCase;
import com.golive.cinema.player.kdm.KDM;
import com.golive.cinema.purchase.domain.usecase.PurchaseUseCase;
import com.golive.cinema.recommend.domain.usecase.GetMovieRecommendUseCase;
import com.golive.cinema.recommend.domain.usecase.GetRecommendUseCase;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.statistics.domain.usecase.GetLocationUseCase;
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
import com.golive.cinema.topic.domain.usecase.GetFilmTopicDetailUseCase;
import com.golive.cinema.topic.domain.usecase.GetIsTopicEnableUseCase;
import com.golive.cinema.topic.domain.usecase.GetOldFilmTopicsUseCase;
import com.golive.cinema.topic.domain.usecase.GetRecommendFilmTopicsUseCase;
import com.golive.cinema.user.buyvip.domain.usecase.GetVipListUseCase;
import com.golive.cinema.user.buyvip.domain.usecase.GetVipMonthlyStatusUseCase;
import com.golive.cinema.user.buyvip.domain.usecase.PurchaseVipUseCase;
import com.golive.cinema.user.consumption.domain.usecase.GetWalletOperationListUseCase;
import com.golive.cinema.user.custom.domain.usecase.GetClientServiceUseCase;
import com.golive.cinema.user.custom.domain.usecase.GetWechatInfoUseCase;
import com.golive.cinema.user.domain.usecase.GetAgreementUseCase;
import com.golive.cinema.user.history.domain.usecase.AddHistoryUseCase;
import com.golive.cinema.user.history.domain.usecase.DeleteHistoryUseCase;
import com.golive.cinema.user.history.domain.usecase.GetHistoryListUseCase;
import com.golive.cinema.user.myinfo.domain.usecase.GetUserInfoUseCase;
import com.golive.cinema.user.pay.domain.usecase.GetPayUrlUseCase;
import com.golive.cinema.user.topup.domain.usecase.TopupPriceListUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.CreditOperationUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.FinanceMessageUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserCreditWalletUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserHeadUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.GetUserWalletUseCase;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.cinema.util.schedulers.SchedulerProvider;
import com.golive.network.net.GoLiveRestApi;
import com.golive.network.net.GoLiveRestApiFactory;

/**
 * Enables injection of product implementations for {@link FilmsDataSource} at compile time.
 */
public class Injection {

    private static GoLiveRestApi gGoLiveRestApi;

    public static GoLiveRestApi getGoLiveRestApi(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        if (null == gGoLiveRestApi) {
            synchronized (Injection.class) {
                if (null == gGoLiveRestApi) {
                    PackageManager pm = context.getPackageManager();
                    PackageInfo pi;
                    int versionCode = 0;
                    String versionName = "";
                    try {
                        pi = pm.getPackageInfo(context.getPackageName(), 0);
                        versionCode = pi.versionCode;
                        versionName = pi.versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    String osVersion = android.os.Build.VERSION.RELEASE;
                    GoLiveRestApiFactory goLiveRestApiFactory =
                            new GoLiveRestApiFactory(context, versionCode, versionName, osVersion);
                    gGoLiveRestApi = goLiveRestApiFactory.createGoLiveRestApi();
                }
            }
        }

        return gGoLiveRestApi;
    }

    @NonNull
    private static KDM getKdm(@NonNull Context context) {
        return new KDM(context);
    }

    public static BaseSchedulerProvider provideSchedulerProvider() {
        return SchedulerProvider.getInstance();
    }

    public static MainConfigDataSource provideGetMainConfigDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        return MainConfigRepository.getInstance(MainConfigLocalDataSource.getInstance(),
                MainConfigRemoteDataSource.getInstance(goLiveRestApi));
    }

    public static FilmsDataSource provideFilmsDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        MainConfigDataSource mainConfigRepository = provideGetMainConfigDataSource(context);
        return FilmsRepository.getInstance(
                FilmsRemoteDataSource.getInstance(goLiveRestApi, mainConfigRepository),
                FilmsLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static FilmTopicsDataSource provideFilmTopicsDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        MainConfigDataSource mainConfigRepository = provideGetMainConfigDataSource(context);
        return FilmTopicsRepository.getInstance(
                FilmTopicsRemoteDataSource.getInstance(goLiveRestApi, mainConfigRepository),
                FilmTopicsLocalDataSource.getInstance());
    }

    public static RecommendDataSource provideRecommendDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        MainConfigDataSource mainConfigRepository = provideGetMainConfigDataSource(context);
        return RecommendRepository.getInstance(
                RecommendRemoteDataSource.getInstance(goLiveRestApi, mainConfigRepository),
                RecommendLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static UserDataSource provideUserDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        MainConfigDataSource mainConfigRepository = provideGetMainConfigDataSource(context);
        return UserRepository.getInstance(
                UserRemoteDataSource.getInstance(goLiveRestApi, mainConfigRepository),
                UserLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static OrdersDataSource provideOrdersDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        MainConfigDataSource mainConfigRepository = provideGetMainConfigDataSource(context);
        return OrdersRepository.getInstance(
                OrdersRemoteDataSource.getInstance(goLiveRestApi, mainConfigRepository),
                OrdersLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static FilmLibraryDataSource provideFilmLibraryDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        MainConfigDataSource mainConfigRepository = provideGetMainConfigDataSource(context);
        return FilmLibraryRepository.getInstance(
                FilmLibraryRemoteDataSource.getInstance(goLiveRestApi, mainConfigRepository),
                FilmLibraryLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static DownloadDataSource provideDownloadDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return DownloadRepository.getInstance(getGoLiveRestApi(context),
                DownloadManager.getInstance(context));
    }

    public static PlayerDataSource providePlayerDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        MainConfigDataSource mainConfigRepository = provideGetMainConfigDataSource(context);
        return PlayerRepository.getInstance(
                PlayerRemoteDataSource.getInstance(goLiveRestApi, mainConfigRepository));
    }

    public static StatisticsDataSource provideStatisticsDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        MainConfigDataSource mainConfigDataSource = provideGetMainConfigDataSource(context);
        return StatisticsRepository.getInstance(
                StatisticsRemoteDataSource.getInstance(goLiveRestApi, mainConfigDataSource));
    }

    public static KdmDataSource provideKdmDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        KdmLocalDataSource localDataSource = KdmLocalDataSource.getInstance(getKdm(context));
        KdmRemoteDataSource remoteDataSource = KdmRemoteDataSource.getInstance(
                getGoLiveRestApi(context), provideGetMainConfigDataSource(context));
        return KdmRepository.getInstance(localDataSource, remoteDataSource);
    }

    public static HttpDataSource provideHttpDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        return HttpRepository.getInstance(goLiveRestApi);
    }

    public static HttpGetUseCase provideHttpGet(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new HttpGetUseCase(provideSchedulerProvider(), provideHttpDataSource(context));
    }

    public static LoginUseCase provideLogin(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new LoginUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static GetFilmsUseCase provideGetFilms(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetFilmsUseCase(provideFilmsDataSource(context), provideSchedulerProvider());
    }

    public static GetFilmListUseCase provideGetFilmList(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetFilmListUseCase(provideFilmsDataSource(context),
                provideGetKdmInitUseCase(context), provideSchedulerProvider()
        );
    }

    public static GetFilmDetailUseCase provideGetFilmDetail(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetFilmDetailUseCase(provideFilmsDataSource(context),
                provideSchedulerProvider());
    }

    public static GetUserWalletUseCase provideGetUserWalletUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetUserWalletUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static GetUserHeadUseCase provideGetUserHeadUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetUserHeadUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static GetUserCreditWalletUseCase provideGetUserCreditWalletUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetUserCreditWalletUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static GetValidOrderUseCase provideGetValidOrder(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetValidOrderUseCase(provideOrdersDataSource(context),
                provideSchedulerProvider());
    }

    public static RefreshOrderUseCase provideRefreshOrderUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new RefreshOrderUseCase(provideOrdersDataSource(context),
                provideSchedulerProvider());
    }

    public static CreateOrderUseCase provideCreateOrderUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new CreateOrderUseCase(provideOrdersDataSource(context),
                provideSchedulerProvider());
    }

    public static PayOrderUseCase providePayOrderUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new PayOrderUseCase(provideOrdersDataSource(context), provideSchedulerProvider());
    }

    public static PayCreditOrderUseCase providePayCreditOrderUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new PayCreditOrderUseCase(provideOrdersDataSource(context),
                provideSchedulerProvider());
    }

    public static PurchaseUseCase providePurchaseFilmUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new PurchaseUseCase(Injection.provideCreateOrderUseCase(context),
                Injection.providePayOrderUseCase(context),
                Injection.providePayCreditOrderUseCase(context), provideSchedulerProvider());
    }

    public static GetPlaybackValidityUseCase provideGetPlaybackValidityUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetPlaybackValidityUseCase(
                provideGetFilmDetail(context), provideGetValidOrder(context),
                provideRefreshOrderUseCase(context), provideGetPlayTicketUseCase(context),
                provideGetPlayTokenUseCase(context),
                provideReportTicketStatusUseCase(context),
                provideGetDownloadTaskInfoUseCase(context),
                provideCheckDownloadFinishUseCase(context), provideDownloadFileUseCase(context),
                getKdm(context), provideSchedulerProvider());
    }

    public static GetVipListUseCase provideGetVipListUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetVipListUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static GetVipMonthlyStatusUseCase provideGetVipMonthlyStatusUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetVipMonthlyStatusUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static GetHistoryListUseCase provideGetHistoryListUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetHistoryListUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static DeleteHistoryUseCase provideDeleteHistoryUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new DeleteHistoryUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static AddHistoryUseCase provideAddHistoryUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new AddHistoryUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static GetWechatInfoUseCase provideGetWechatInfoUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetWechatInfoUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static GetClientServiceUseCase provideGetClientServiceUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetClientServiceUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static PurchaseVipUseCase providePurchaseVipUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new PurchaseVipUseCase(
                Injection.provideCreateOrderUseCase(context),
                Injection.providePayOrderUseCase(context),
                Injection.providePayCreditOrderUseCase(context),
                provideSchedulerProvider());
    }

    public static GetWalletOperationListUseCase provideGetWalletOperationListUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetWalletOperationListUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static GetPayUrlUseCase provideGetPayUrlListUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetPayUrlUseCase(provideGetMainConfigDataSource(context),
                provideSchedulerProvider());
    }

    public static TopupPriceListUseCase provideGetTopupPriceListUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new TopupPriceListUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static GetUserInfoUseCase provideGetUserInfoUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetUserInfoUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static GetRecommendUseCase provideGetRecommendUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetRecommendUseCase(provideRecommendDataSource(context),
                provideGetKdmInitUseCase(context), provideSchedulerProvider());
    }

    public static GetMovieRecommendUseCase provideGetMovieRecommendUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetMovieRecommendUseCase(provideRecommendDataSource(context),
                provideGetKdmInitUseCase(context), provideSchedulerProvider());
    }

    public static GetMainConfigUseCase provideGetMainConfigUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetMainConfigUseCase(provideGetMainConfigDataSource(context),
                provideSchedulerProvider());
    }

    public static ServerInitDataSource provideServerInitDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        MainConfigDataSource mainConfigRepository = provideGetMainConfigDataSource(context);

        return ServerInitRepository.getInstance(
                ServerInitRemoteDataSource.getInstance(goLiveRestApi, mainConfigRepository),
                ServerInitLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static ServerStatusUseCase provideServerStatusUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ServerStatusUseCase(provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static GetShutdownMessageUseCase provideGetShutdownMessageUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetShutdownMessageUseCase(provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static BootImageUseCase provideBootImageUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new BootImageUseCase(provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static UpgradeUseCase provideUpgradeUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new UpgradeUseCase(provideServerInitDataSource(context), provideSchedulerProvider());
    }

    public static RepeatMacUseCase provideRepeatMacUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new RepeatMacUseCase(provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static VerifyCodeUseCase provideVerifyCodeUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        MainConfigDataSource mainConfigRepository = provideGetMainConfigDataSource(context);
        VerifyCodeDataSource dataSource = VerifyCodeRepository.getInstance(
                VerifyCodeRemoteDataSource.getInstance(goLiveRestApi, mainConfigRepository));
        return new VerifyCodeUseCase(dataSource, provideSchedulerProvider());
    }

    public static LoginAgainUseCase provideLoginAgainUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new LoginAgainUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static ActivityImageUseCase provideActivityImageUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ActivityImageUseCase(provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static CreditOperationUseCase provideCreditOperationUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new CreditOperationUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static FinanceMessageUseCase provideFinanceMessageUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new FinanceMessageUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static GetAgreementUseCase provideGetAgreementUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetAgreementUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static AppPageUseCase provideAppPageUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new AppPageUseCase(provideServerInitDataSource(context), provideSchedulerProvider());
    }

    public static GetFilmLibTabUseCase provideGetFilmTabUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetFilmLibTabUseCase(provideFilmLibraryDataSource(context),
                provideSchedulerProvider());
    }

    public static GetFilmLibListUseCase provideGetFilmListUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetFilmLibListUseCase(provideFilmLibraryDataSource(context),
                provideGetKdmInitUseCase(context),
                provideSchedulerProvider());
    }

    public static GetDownloadTaskInfoUseCase provideGetDownloadTaskInfoUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetDownloadTaskInfoUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static AddDownloadTaskUseCase provideAddDownloadTaskUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new AddDownloadTaskUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static ResumeDownloadTaskUseCase provideResumeDownloadTaskUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ResumeDownloadTaskUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static PauseDownloadTaskUseCase providePauseDownloadTaskUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new PauseDownloadTaskUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static PauseAllDownloadTaskUseCase providePauseAllDownloadTaskUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new PauseAllDownloadTaskUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static DeleteDownloadTaskUseCase provideDeleteDownloadTaskUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new DeleteDownloadTaskUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static CheckDownloadFinishUseCase provideCheckDownloadFinishUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new CheckDownloadFinishUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static DownloadFileUseCase provideDownloadFileUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new DownloadFileUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static ExitDrainageUseCase provideExitDrainageUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ExitDrainageUseCase(provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static GuideTypeUseCase provideGuideTypeUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GuideTypeUseCase(provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static ExitComboUseCase provideExitComboUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ExitComboUseCase(provideUserDataSource(context),
                provideGetKdmInitUseCase(context), provideSchedulerProvider());
    }

    public static GetKdmInitUseCase provideGetKdmInitUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetKdmInitUseCase(provideSchedulerProvider(), provideKdmDataSource(context),
                provideGetMainConfigDataSource(context));
    }

    public static GetKdmVersionUseCase provideGetKdmVersionUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetKdmVersionUseCase(provideSchedulerProvider(), provideKdmDataSource(context),
                provideGetMainConfigDataSource(context));
    }

    public static GetKdmServerVersionUseCase provideGetKdmServerVersionUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetKdmServerVersionUseCase(provideSchedulerProvider(),
                provideKdmDataSource(context), provideGetKdmVersionUseCase(context),
                provideGetMainConfigDataSource(context));
    }

    public static UpgradeKdmUseCase provideUpgradeKdmUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new UpgradeKdmUseCase(provideSchedulerProvider(), provideKdmDataSource(context),
                provideGetMainConfigDataSource(context));
    }

    public static NotifyKdmReadyUseCase provideNotifyKdmReadyUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new NotifyKdmReadyUseCase(provideSchedulerProvider(), provideKdmDataSource(context));
    }

    public static GetPlayTicketUseCase provideGetPlayTicketUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetPlayTicketUseCase(provideSchedulerProvider(),
                provideOrdersDataSource(context));
    }

    public static GetPlayTokenUseCase provideGetPlayTokenUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetPlayTokenUseCase(provideSchedulerProvider(),
                provideOrdersDataSource(context));
    }

    public static ReportTicketStatusUseCase provideReportTicketStatusUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportTicketStatusUseCase(provideSchedulerProvider(),
                provideOrdersDataSource(context));
    }

    public static GetIsTopicEnableUseCase provideGetIsTopicEnableUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetIsTopicEnableUseCase(provideAppPageUseCase(context),
                provideSchedulerProvider());
    }

    public static GetOldFilmTopicsUseCase provideGetOldFilmTopicsUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetOldFilmTopicsUseCase(provideFilmTopicsDataSource(context),
                provideSchedulerProvider());
    }

    public static GetRecommendFilmTopicsUseCase provideGetRecommendFilmTopicsUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetRecommendFilmTopicsUseCase(provideFilmTopicsDataSource(context),
                provideSchedulerProvider());
    }

    public static GetFilmTopicDetailUseCase provideGetFilmTopicDetailUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetFilmTopicDetailUseCase(provideFilmTopicsDataSource(context),
                provideSchedulerProvider());
    }

    public static GetLocationUseCase provideGetLocationUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetLocationUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static StatisticsHelper provideStatisticsHelper(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return StatisticsHelper.getInstance(context);
    }

    public static ReportAppStartUseCase provideReportAppStartBehaviorUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportAppStartUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context), provideGetLocationUseCase(context));
    }

    public static ReportAppExceptionUseCase provideReportAppExceptionUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportAppExceptionUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportAppExitUseCase provideReportAppExitBehaviorUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportAppExitUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportEnterActivityUseCase provideReportEnterActivityUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportEnterActivityUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportExitActivityUseCase provideReportExitActivityUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportExitActivityUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportClickUserCenterTopUpUseCase provideReportClickUserCenterTopUpUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportClickUserCenterTopUpUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportClickUserCenterVipUseCase provideReportClickUserCenterVipUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportClickUserCenterVipUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportEnterAdUseCase provideReportEnterAdUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportEnterAdUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportEnterEventUseCase provideReportEnterEventUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportEnterEventUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportEnterFilmDetailUseCase provideReportEnterFilmDetailUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportEnterFilmDetailUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportEnterUserCenterUseCase provideReportEnterUserCenterUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportEnterUserCenterUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportExitAdUseCase provideReportExitAdUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportExitAdUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportThirdPartyAdUseCase provideReportThirdPartyAdUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportThirdPartyAdUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportExitEventUseCase provideReportExitEventUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportExitEventUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportExitFilmDetailUseCase provideReportExitFilmDetailUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportExitFilmDetailUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportExitPromptUseCase provideReportExitPromptUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportExitPromptUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportExitUserCenterUseCase provideReportExitUserCenterUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportExitUserCenterUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportExitWatchNoticeUseCase provideReportExitWatchNoticeUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportExitWatchNoticeUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportPlayAdBlockedUseCase provideReportPlayAdBlockedUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportPlayAdBlockedUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportPlayAdExceptionUseCase provideReportPlayAdExceptionUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportPlayAdExceptionUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportPlayAdExitUseCase provideReportPlayAdExitUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportPlayAdExitUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportPlayAdLoadUseCase provideReportPlayAdLoadUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportPlayAdLoadUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportPlayAdStartUseCase provideReportPlayAdStartUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportPlayAdStartUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportVideoExceptionUseCase provideReportVideoExceptionUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportVideoExceptionUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportVideoExitUseCase provideReportVideoExitUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportVideoExitUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportVideoPlayPauseUseCase provideReportVideoPlayPauseUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportVideoPlayPauseUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportVideoSeekUseCase provideReportVideoSeekUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportVideoSeekUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportVideoStartUseCase provideReportVideoStartUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportVideoStartUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportVideoLoadUseCase provideReportVideoLoadUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportVideoLoadUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportVideoBlockUseCase provideReportVideoBlockUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportVideoBlockUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportVideoStreamSwitchUseCase provideReportVideoStreamSwitchUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportVideoStreamSwitchUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportHardwareInfoUseCase provideReportHardwareInfoUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportHardwareInfoUseCase(provideSchedulerProvider(),
                provideStatisticsDataSource(context));
    }

    public static ReportAdvertMiaozhenUseCase provideReportAdvertMiaozhenUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportAdvertMiaozhenUseCase(provideSchedulerProvider(),
                providePlayerDataSource(context));
    }

    public static AdvertUseCase provideAdvertUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new AdvertUseCase(provideGetMainConfigDataSource(context),
                provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static ReportThirdAdvertExposureUseCase provideReportThirdAdvertExposureUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new ReportThirdAdvertExposureUseCase(provideStatisticsDataSource(context),
                provideSchedulerProvider());
    }
}