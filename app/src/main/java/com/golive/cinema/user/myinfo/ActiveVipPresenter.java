package com.golive.cinema.user.myinfo;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.user.custom.domain.usecase.GetClientServiceUseCase;
import com.golive.cinema.user.pay.domain.usecase.GetPayUrlUseCase;
import com.initialjie.log.Logger;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Mowl on 2016/11/22.
 */

public class ActiveVipPresenter extends BasePresenter<MyInfoContract.ActiveVipView> implements
        MyInfoContract.ActiveVipPresenter {
    private final GetPayUrlUseCase getPayUrlUseCase;
    private final GetClientServiceUseCase mGetClientServiceUseCase;

    public ActiveVipPresenter(@NonNull MyInfoContract.ActiveVipView view,
            @NonNull GetPayUrlUseCase payurltask,
            @NonNull GetClientServiceUseCase getClientServicetask) {
        checkNotNull(view, "ActiveVipView cannot be null!");
        mGetClientServiceUseCase = checkNotNull(getClientServicetask,
                "payurltask cannot be null!");
        getPayUrlUseCase = checkNotNull(payurltask, "payurltask cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        getClientServiceInfo();
    }

    public void getloadPayUrl() {
        Logger.d("loadPayUrl");
        getView().setLoadingIndicator(true);
        Subscription subscription = getPayUrlUseCase.run(new GetPayUrlUseCase.RequestValues())
                .subscribe(new Subscriber<GetPayUrlUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        MyInfoContract.ActiveVipView view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getPayUrlUseCase onError : ");
                        MyInfoContract.ActiveVipView view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onNext(GetPayUrlUseCase.ResponseValue responseValue) {
                        MyInfoContract.ActiveVipView view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.checkUrlMap(responseValue.getPayConfig());
                    }
                });
        addSubscription(subscription);
    }

    private void getClientServiceInfo() {
        getView().setLoadingIndicator(true);
        Subscription subscription = mGetClientServiceUseCase.run(
                new GetClientServiceUseCase.RequestValues(false))
                .subscribe(new Subscriber<GetClientServiceUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        MyInfoContract.ActiveVipView view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getTheClientService onError : ");
                        MyInfoContract.ActiveVipView view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onNext(GetClientServiceUseCase.ResponseValue responseValue) {
                        MyInfoContract.ActiveVipView view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        if (responseValue.getClientService() != null) {
                            String phone = responseValue.getClientService().getServicePhone5();
                            if (phone != null) {
                                view.setServicePhoneInfo(phone);
                            }
                        }
                    }
                });
        addSubscription(subscription);
    }
}
