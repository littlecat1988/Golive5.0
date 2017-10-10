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

import com.golive.cinema.data.FakeFilmsRemoteDataSource;
import com.golive.cinema.data.FakeMainConfigRemoteDataSource;
import com.golive.cinema.data.FakeOrdersDataSource;
import com.golive.cinema.data.FakePlayerRemoteDataSource;
import com.golive.cinema.data.FakeRecommendDataSource;
import com.golive.cinema.data.FakeServerInitDataSource;
import com.golive.cinema.data.FakeUserRemoteDataSource;
import com.golive.cinema.data.FakeVerifyCodeDataSource;
import com.golive.cinema.data.source.DownloadDataSource;
import com.golive.cinema.data.source.DownloadRepository;
import com.golive.cinema.data.source.FilmLibraryDataSource;
import com.golive.cinema.data.source.FilmLibraryRepository;
import com.golive.cinema.data.source.FilmsDataSource;
import com.golive.cinema.data.source.FilmsRepository;
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
import com.golive.cinema.data.source.UserDataSource;
import com.golive.cinema.data.source.UserRepository;
import com.golive.cinema.data.source.VerifyCodeDataSource;
import com.golive.cinema.data.source.local.FilmLibraryLocalDataSource;
import com.golive.cinema.data.source.local.FilmsLocalDataSource;
import com.golive.cinema.data.source.local.KdmLocalDataSource;
import com.golive.cinema.data.source.local.MainConfigLocalDataSource;
import com.golive.cinema.data.source.local.OrdersLocalDataSource;
import com.golive.cinema.data.source.local.RecommendLocalDataSource;
import com.golive.cinema.data.source.local.ServerInitLocalDataSource;
import com.golive.cinema.data.source.local.UserLocalDataSource;
import com.golive.cinema.data.source.remote.FilmLibraryRemoteDataSource;
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
import com.golive.cinema.init.domain.usecase.ActivityImageUseCase;
import com.golive.cinema.init.domain.usecase.AppPageUseCase;
import com.golive.cinema.init.domain.usecase.BootImageUseCase;
import com.golive.cinema.init.domain.usecase.ExitComboUseCase;
import com.golive.cinema.init.domain.usecase.ExitDrainageUseCase;
import com.golive.cinema.init.domain.usecase.GuideTypeUseCase;
import com.golive.cinema.init.domain.usecase.LoginAgainUseCase;
import com.golive.cinema.init.domain.usecase.MainUseCase;
import com.golive.cinema.init.domain.usecase.RepeatMacUseCase;
import com.golive.cinema.init.domain.usecase.ServerStatusUseCase;
import com.golive.cinema.init.domain.usecase.UpgradeUseCase;
import com.golive.cinema.init.domain.usecase.VerifyCodeUseCase;
import com.golive.cinema.login.domain.usecase.LoginUseCase;
import com.golive.cinema.order.domain.usecase.CreateOrderUseCase;
import com.golive.cinema.order.domain.usecase.GetValidOrderUseCase;
import com.golive.cinema.order.domain.usecase.PayCreditOrderUseCase;
import com.golive.cinema.order.domain.usecase.PayOrderUseCase;
import com.golive.cinema.order.domain.usecase.ReportTicketStatusUseCase;
import com.golive.cinema.player.domain.usecase.GetKdmInitUseCase;
import com.golive.cinema.player.domain.usecase.GetPlayTicketUseCase;
import com.golive.cinema.player.domain.usecase.GetPlayTokenUseCase;
import com.golive.cinema.player.domain.usecase.GetPlaybackValidityUseCase;
import com.golive.cinema.player.kdm.KDM;
import com.golive.cinema.purchase.domain.usecase.PurchaseFilmUseCase;
import com.golive.cinema.recommend.domain.usecase.GetMovieRecommendUseCase;
import com.golive.cinema.recommend.domain.usecase.GetRecommendUseCase;
import com.golive.cinema.user.buyvip.domain.usecase.GetVipListUseCase;
import com.golive.cinema.user.buyvip.domain.usecase.PurchaseVipUseCase;
import com.golive.cinema.user.consumption.domain.usecase.GetWalletOperationListUseCase;
import com.golive.cinema.user.custom.domain.usecase.GetClientServiceUseCase;
import com.golive.cinema.user.custom.domain.usecase.GetWechatInfoUseCase;
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
 * Enables injection of mock implementations for {@link FilmsDataSource} at compile time. This is
 * useful for testing, since it allows us to use a fake instance of the class to isolate the
 * dependencies and run a test hermetically.
 */
public class Injection {

    private static GoLiveRestApi gGoLiveRestApi;

    public static GoLiveRestApi getGoLiveRestApi(@NonNull Context context) {
        checkNotNull(context);
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

                    GoLiveRestApiFactory goLiveRestApiFactory =
                            new GoLiveRestApiFactory(context, versionCode, versionName);
                    gGoLiveRestApi = goLiveRestApiFactory.createGoLiveRestApi();
                }
            }
        }

        return gGoLiveRestApi;
    }

    public static BaseSchedulerProvider provideSchedulerProvider() {
        return SchedulerProvider.getInstance();
    }

    public static MainConfigDataSource provideMainConfigDataSource(@NonNull Context context) {
        checkNotNull(context);
        return MainConfigRepository.
                getInstance(MainConfigLocalDataSource.getInstance(),
                        FakeMainConfigRemoteDataSource.getInstance());
    }

    public static FilmsDataSource provideFilmsDataSource(@NonNull Context context) {
        checkNotNull(context);
        return FilmsRepository.getInstance(
                FakeFilmsRemoteDataSource.getInstance(),
                FilmsLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static UserDataSource provideUserDataSource(@NonNull Context context) {
        checkNotNull(context);
        return UserRepository.getInstance(
                FakeUserRemoteDataSource.getInstance(),
                UserLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static OrdersDataSource provideOrdersDataSource(@NonNull Context context) {
        checkNotNull(context);
        return OrdersRepository.getInstance(
                FakeOrdersDataSource.getInstance(),
                OrdersLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static ServerInitDataSource provideServerInitDataSource(@NonNull Context context) {
        checkNotNull(context);
        return ServerInitRepository.getInstance(FakeServerInitDataSource.getInstance(),
                ServerInitLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static RecommendDataSource provideRecommendDataSource(@NonNull Context context) {
        checkNotNull(context);
        return RecommendRepository.getInstance(FakeRecommendDataSource.getInstance(),
                RecommendLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static DownloadDataSource provideDownloadDataSource(@NonNull Context context) {
        checkNotNull(context);
        return DownloadRepository.getInstance(getGoLiveRestApi(context),
                DownloadManager.getInstance(context));
    }

    public static PlayerDataSource providePlayerDataSource(@NonNull Context context) {
        checkNotNull(context);
        return PlayerRepository.getInstance(FakePlayerRemoteDataSource.getInstance());
    }

    public static KdmDataSource provideKdmDataSource(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        KdmLocalDataSource dataSource = KdmLocalDataSource.getInstance(
                new KDM(context.getApplicationContext()));
        return KdmRepository.getInstance(dataSource);
    }

    public static FilmLibraryDataSource provideFilmLibraryDataSource(@NonNull Context context) {
        checkNotNull(context);
        GoLiveRestApi goLiveRestApi = getGoLiveRestApi(context);
        MainConfigDataSource mainConfigRepository = provideMainConfigDataSource(context);
        return FilmLibraryRepository.getInstance(
                FilmLibraryRemoteDataSource.getInstance(goLiveRestApi, mainConfigRepository),
                FilmLibraryLocalDataSource.getInstance(context, provideSchedulerProvider()));
    }

    public static GetFilmsUseCase provideGetFilms(@NonNull Context context) {
        checkNotNull(context);
        return new GetFilmsUseCase(provideFilmsDataSource(context), provideSchedulerProvider());
    }

    public static GetFilmDetailUseCase provideGetFilmDetail(@NonNull Context context) {
        checkNotNull(context);
        return new GetFilmDetailUseCase(provideFilmsDataSource(context),
                provideSchedulerProvider());
    }

    public static LoginUseCase provideLogin(@NonNull Context context) {
        checkNotNull(context);
        return new LoginUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static GetUserWalletUseCase provideGetUserWalletUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new GetUserWalletUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static GetUserCreditWalletUseCase provideGetUserCreditWalletUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        return new GetUserCreditWalletUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static GetValidOrderUseCase provideGetValidOrder(@NonNull Context context) {
        checkNotNull(context);
        return new GetValidOrderUseCase(provideOrdersDataSource(context),
                provideSchedulerProvider());
    }

    public static CreateOrderUseCase provideCreateOrderUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new CreateOrderUseCase(provideOrdersDataSource(context),
                provideSchedulerProvider());
    }

    public static PayOrderUseCase providePayOrderUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new PayOrderUseCase(provideOrdersDataSource(context), provideSchedulerProvider());
    }

    public static PayCreditOrderUseCase providePayCreditOrderUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new PayCreditOrderUseCase(provideOrdersDataSource(context),
                provideSchedulerProvider());
    }

    public static PurchaseFilmUseCase providePurchaseFilmUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new PurchaseFilmUseCase(Injection.provideCreateOrderUseCase(context),
                Injection.providePayOrderUseCase(context),
                Injection.providePayCreditOrderUseCase(context), provideSchedulerProvider());
    }

    public static GetPlaybackValidityUseCase provideGetPlaybackValidityUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        KDM kdm = new KDM(context);
        return new GetPlaybackValidityUseCase(
                provideGetFilmDetail(context), provideGetValidOrder(context),
                provideGetPlayTicketUseCase(context), provideGetPlayTokenUseCase(context),
                provideReportTicketStatusUseCase(context),
                provideGetDownloadTaskInfoUseCase(context),
                provideCheckDownloadFinishUseCase(context), provideDownloadFileUseCase(context),
                kdm, provideSchedulerProvider());
    }

    public static GetVipListUseCase provideGetVipListUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new GetVipListUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static GetHistoryListUseCase provideGetHistoryListUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new GetHistoryListUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static DeleteHistoryUseCase provideDeleteHistoryUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new DeleteHistoryUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static AddHistoryUseCase provideAddHistoryUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new AddHistoryUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static PurchaseVipUseCase providePurchaseVipUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new PurchaseVipUseCase(
                Injection.provideCreateOrderUseCase(context),
                Injection.providePayOrderUseCase(context),
                Injection.providePayCreditOrderUseCase(context),
                provideSchedulerProvider());
    }

    public static GetPayUrlUseCase provideGetPayUrlListUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new GetPayUrlUseCase(provideMainConfigDataSource(context),
                provideSchedulerProvider());
    }

    public static GetWalletOperationListUseCase provideGetWalletOperationListUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        return new GetWalletOperationListUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static TopupPriceListUseCase provideGetTopupPriceListUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new TopupPriceListUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static GetUserInfoUseCase provideGetUserInfoUseCase(@NonNull Context context) {
        checkNotNull(context);
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

    public static MainUseCase provideMaintainInformation(@NonNull Context context) {
        checkNotNull(context);
        return new MainUseCase(provideMainConfigDataSource(context), provideSchedulerProvider());
    }

    public static ServerStatusUseCase provideServerStatusUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new ServerStatusUseCase(provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static BootImageUseCase provideBootImageUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new BootImageUseCase(provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static UpgradeUseCase provideUpgradeUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new UpgradeUseCase(provideServerInitDataSource(context), provideSchedulerProvider());
    }

    public static RepeatMacUseCase provideRepeatMacUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new RepeatMacUseCase(provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static VerifyCodeUseCase provideVerifyCodeUseCase(@NonNull Context context) {
        checkNotNull(context);
        VerifyCodeDataSource verifyCodeDataSource = FakeVerifyCodeDataSource.getInstance();
        return new VerifyCodeUseCase(verifyCodeDataSource, provideSchedulerProvider());
    }

    public static LoginAgainUseCase provideLoginAgainUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new LoginAgainUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static ActivityImageUseCase provideActivityImageUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new ActivityImageUseCase(provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static CreditOperationUseCase provideCreditOperationUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new CreditOperationUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static FinanceMessageUseCase provideFinanceMessageUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new FinanceMessageUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static GetMessageUseCase provideGetMessageUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new GetMessageUseCase(provideServerInitDataSource(context),
                provideSchedulerProvider());
    }

    public static AppPageUseCase provideAppPageUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new AppPageUseCase(provideServerInitDataSource(context), provideSchedulerProvider());
    }

    public static GetFilmLibTabUseCase provideGetFilmTabUseCase(@NonNull Context context) {
        checkNotNull(context);
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

    public static GetFilmListUseCase provideGetFilmList(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetFilmListUseCase(provideFilmsDataSource(context),
                provideGetKdmInitUseCase(context), provideSchedulerProvider()
        );
    }

    public static GetWechatInfoUseCase provideGetWechatInfoUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new GetWechatInfoUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static GetClientServiceUseCase provideGetClientServiceUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new GetClientServiceUseCase(provideUserDataSource(context),
                provideSchedulerProvider());
    }

    public static GetUserHeadUseCase provideGetUserHeadUseCase(@NonNull Context context) {
        checkNotNull(context);
        return new GetUserHeadUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }

    public static GetDownloadTaskInfoUseCase provideGetDownloadTaskInfoUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        return new GetDownloadTaskInfoUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static AddDownloadTaskUseCase provideAddDownloadTaskUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        return new AddDownloadTaskUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static ResumeDownloadTaskUseCase provideResumeDownloadTaskUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        return new ResumeDownloadTaskUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static PauseDownloadTaskUseCase providePauseDownloadTaskUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        return new PauseDownloadTaskUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static PauseAllDownloadTaskUseCase providePauseAllDownloadTaskUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        return new PauseAllDownloadTaskUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static DeleteDownloadTaskUseCase provideDeleteDownloadTaskUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        return new DeleteDownloadTaskUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static CheckDownloadFinishUseCase provideCheckDownloadFinishUseCase(
            @NonNull Context context) {
        checkNotNull(context);
        return new CheckDownloadFinishUseCase(provideDownloadDataSource(context),
                provideSchedulerProvider());
    }

    public static DownloadFileUseCase provideDownloadFileUseCase(@NonNull Context context) {
        checkNotNull(context);
        DownloadFileUseCase downloadFileUseCase = new DownloadFileUseCase(
                provideDownloadDataSource(context), provideSchedulerProvider());
        return downloadFileUseCase;
    }

    public static GetKdmInitUseCase provideGetKdmInitUseCase(@NonNull Context context) {
        checkNotNull(context);
        context = context.getApplicationContext();
        return new GetKdmInitUseCase(provideSchedulerProvider(), provideKdmDataSource(context),
                provideMainConfigDataSource(context));
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
        return new ExitComboUseCase(provideUserDataSource(context), provideSchedulerProvider());
    }
}
