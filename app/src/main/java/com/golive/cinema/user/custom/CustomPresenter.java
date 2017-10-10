package com.golive.cinema.user.custom;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.init.domain.usecase.GetMainConfigUseCase;
import com.golive.cinema.player.domain.usecase.GetKdmVersionUseCase;
import com.golive.cinema.user.custom.domain.usecase.GetClientServiceUseCase;
import com.golive.player.kdm.KDMResCode;
import com.initialjie.log.Logger;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Administrator on 2016/10/31.
 */

public class CustomPresenter extends BasePresenter<CustomContract.View> implements
        CustomContract.Presenter {
    private final GetMainConfigUseCase mGetMainConfigUseCase;
    private final GetClientServiceUseCase mGetClientServiceUseCase;
    private final GetKdmVersionUseCase mGetKdmVersionUseCase;

    public CustomPresenter(@NonNull CustomContract.View view,
            @NonNull GetClientServiceUseCase getClientServicetask,
            @NonNull GetMainConfigUseCase cfgtask,
            @NonNull GetKdmVersionUseCase getKdmVersionUseCase) {
        mGetMainConfigUseCase = checkNotNull(cfgtask, "cfgtask cannot be null!");
        mGetClientServiceUseCase = checkNotNull(getClientServicetask,
                "payurltask cannot be null!");
        mGetKdmVersionUseCase = checkNotNull(getKdmVersionUseCase,
                "getKdmVersionUseCase cannot be null!");
        checkNotNull(view, "View cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void loadInfo() {
        getView().setLoadingIndicator(true);
        Subscription subscription = getClientServiceInfoObs()
                .concatMap(new Func1<GetClientServiceUseCase.ResponseValue, Observable<?>>() {
                    @Override
                    public Observable<?> call(GetClientServiceUseCase.ResponseValue responseValue) {
                        return Observable.zip(loadMainConfigInfoObs(), getKdmVersionObs(),
                                new Func2<GetMainConfigUseCase.ResponseValue, GetKdmVersionUseCase
                                        .ResponseValue, Object>() {
                                    @Override
                                    public Object call(
                                            GetMainConfigUseCase.ResponseValue responseValue,
                                            GetKdmVersionUseCase.ResponseValue responseValue2) {
                                        return null;
                                    }
                                });
                    }
                })
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        CustomContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "loadInfo, onError : ");
                        CustomContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setLoadingIndicator(false);
                        }
                    }

                    @Override
                    public void onNext(Object o) {
                    }
                });
        addSubscription(subscription);
    }

    private Observable<GetMainConfigUseCase.ResponseValue> loadMainConfigInfoObs() {
        return mGetMainConfigUseCase.run(
                new GetMainConfigUseCase.RequestValues(false))
                .onErrorReturn(new Func1<Throwable, GetMainConfigUseCase.ResponseValue>() {
                    @Override
                    public GetMainConfigUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getConfigLoad, onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetMainConfigUseCase.ResponseValue>() {
                    @Override
                    public void call(GetMainConfigUseCase.ResponseValue responseValue) {
                        CustomContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        if (responseValue.getMainConfig() != null) {
                            view.showMainInfo(responseValue.getMainConfig());
                        }
                    }
                });
    }

    private Observable<GetClientServiceUseCase.ResponseValue> getClientServiceInfoObs() {
        return mGetClientServiceUseCase.run(
                new GetClientServiceUseCase.RequestValues(false))
                .onErrorReturn(new Func1<Throwable, GetClientServiceUseCase.ResponseValue>() {
                    @Override
                    public GetClientServiceUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getClientServiceInfo, onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetClientServiceUseCase.ResponseValue>() {
                    @Override
                    public void call(GetClientServiceUseCase.ResponseValue responseValue) {
                        CustomContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.showClientService(responseValue.getClientService());
                    }
                });
    }

    private Observable<GetKdmVersionUseCase.ResponseValue> getKdmVersionObs() {
        return mGetKdmVersionUseCase.run(new GetKdmVersionUseCase.RequestValues())
                .onErrorReturn(new Func1<Throwable, GetKdmVersionUseCase.ResponseValue>() {
                    @Override
                    public GetKdmVersionUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getKdmVersion, onErrorReturn : ");
                        return null;
                    }
                })
                .doOnNext(new Action1<GetKdmVersionUseCase.ResponseValue>() {
                    @Override
                    public void call(GetKdmVersionUseCase.ResponseValue responseValue) {
                        CustomContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        if (responseValue != null && responseValue.getKDMResCode() != null) {
                            KDMResCode kdmResCode = responseValue.getKDMResCode();
                            if (kdmResCode != null
                                    && KDMResCode.RESCODE_OK == kdmResCode.getResult()) {
                                String platform = "";
                                String version = "";
                                KDMResCode.VERSION kdmVer = kdmResCode.version;
                                if (kdmVer != null) {
                                    platform = kdmVer.getPlatform();
                                    version = kdmVer.getVersion();
                                    view.setKdmVersion(version, platform);
                                }
                                Logger.d("getKdmVersion getVerCallback, platform : " + platform
                                        + ", version : " + version);
                            }
                        }
                    }
                });
    }
}