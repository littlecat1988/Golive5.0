package com.golive.cinema.user.setting;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.Context;
import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.Constants;
import com.golive.cinema.init.domain.usecase.GetMainConfigUseCase;
import com.golive.cinema.init.domain.usecase.UpgradeUseCase;
import com.golive.cinema.player.domain.usecase.GetKdmVersionUseCase;
import com.golive.cinema.util.PackageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.Upgrade;
import com.golive.player.kdm.KDMResCode;
import com.initialjie.log.Logger;

import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;

/**
 * Created by Administrator on 2016/10/31.
 */

public class SettingPresenter extends BasePresenter<SettingContract.View> implements
        SettingContract.Presenter {
    private final GetMainConfigUseCase mGetMainConfigUseCase;
    private final UpgradeUseCase mUpgradeUseCase;
    private final GetKdmVersionUseCase mGetKdmVersionUseCase;
    private final BaseSchedulerProvider mSchedulerProvider;

    public SettingPresenter(@NonNull SettingContract.View view,
            @NonNull GetMainConfigUseCase cfgtask,
            @NonNull UpgradeUseCase upgradetask,
            @NonNull GetKdmVersionUseCase getKdmVersionUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        mGetMainConfigUseCase = checkNotNull(cfgtask, "payurltask cannot be null!");
        mUpgradeUseCase = checkNotNull(upgradetask, "upgradetask cannot be null!");
        mGetKdmVersionUseCase = checkNotNull(getKdmVersionUseCase,
                "getKdmVersionUseCase cannot be null!");
        mSchedulerProvider = checkNotNull(schedulerProvider, "schedulerProvider cannot be null!");
        checkNotNull(view, "BuyVipView cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        getConfigLoad();
    }

    @Override
    public void getConfigLoad() {
        Logger.d("getConfigLoad");
        Subscription subscription = mGetMainConfigUseCase.run(
                new GetMainConfigUseCase.RequestValues(false))
                .subscribe(new Subscriber<GetMainConfigUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getConfigLoad onError : ");
                    }

                    @Override
                    public void onNext(GetMainConfigUseCase.ResponseValue responseValue) {
                        SettingContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        MainConfig mainConfig = responseValue.getMainConfig();
                        if (mainConfig != null) {
                            view.setMainConfig(mainConfig);
                            view.setChangeServerKey(mainConfig.getChangeServerKey());
                        }
                    }
                });
        addSubscription(subscription);
    }

    @Override
    public void getKdmVersion(Context context) {
        Logger.d("getKdmVersion");
        getView().setLoadingIndicator(true);
        Subscription subscription = mGetKdmVersionUseCase.run(
                new GetKdmVersionUseCase.RequestValues())
                .map(new Func1<GetKdmVersionUseCase.ResponseValue, KDMResCode>() {
                    @Override
                    public KDMResCode call(GetKdmVersionUseCase.ResponseValue responseValue) {
                        if (responseValue != null) {
                            return responseValue.getKDMResCode();
                        }
                        return null;
                    }
                })
                .subscribe(new Observer<KDMResCode>() {
                    @Override
                    public void onCompleted() {
                        SettingContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getKdmVersion, onError : ");
                        SettingContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onNext(KDMResCode kdmResCode) {
                        SettingContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        if (kdmResCode != null && KDMResCode.RESCODE_OK == kdmResCode.getResult()) {
                            String platform = "";
                            String version = "";
                            if (kdmResCode.version != null) {
                                platform = kdmResCode.version.getPlatform();
                                version = kdmResCode.version.getVersion();
                                view.setKdmVersion(version, platform);
                            }
                            Logger.d("getKdmVersion, platform : " + platform
                                    + ", version : " + version);
                        }
                    }
                });
        addSubscription(subscription);
    }

    @Override
    public void checkUpgrade(Context context, int versionCode, String versionName) {
        Logger.d("checkUpgrade versionCode=" + versionCode + ",versionName=" + versionName);
        getView().setCheckingUpgradeIndicator(true);
        UpgradeUseCase.RequestValues req = new UpgradeUseCase.RequestValues(
                String.valueOf(versionCode), versionName);
        Subscription subscription = mUpgradeUseCase.run(req)
                .subscribe(new Subscriber<UpgradeUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        SettingContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setCheckingUpgradeIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "checkUpgrade onError : ");
                        SettingContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setCheckingUpgradeIndicator(false);
                    }

                    @Override
                    public void onNext(UpgradeUseCase.ResponseValue responseValue) {
                        SettingContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        if (responseValue != null && responseValue.getUpgrade() != null) {
                            Upgrade upgrade = responseValue.getUpgrade();
                            int remoteUpgradeType = Integer.valueOf(upgrade.getUpgradetype());
                            int remoteVersion = -1;
                            if (!StringUtils.isNullOrEmpty(upgrade.getVersioncode())) {
                                remoteVersion = Integer.valueOf(upgrade.getVersioncode());
                            }
                            int upgradeType = PackageUtils.checkLocalUpgrade(remoteUpgradeType,
                                    remoteVersion);
                            view.showUpgradeView(upgrade, upgradeType);
                            return;
                        }

                        view.showUpgradeView(null, Constants.UPGRADE_TYPE_NO_UPGRADE);
                    }
                });
        addSubscription(subscription);
    }
}

