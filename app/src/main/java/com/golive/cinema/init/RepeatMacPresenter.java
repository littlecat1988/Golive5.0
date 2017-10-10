package com.golive.cinema.init;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.init.domain.usecase.LoginAgainUseCase;
import com.golive.cinema.init.domain.usecase.RepeatMacUseCase;
import com.golive.cinema.init.domain.usecase.VerifyCodeUseCase;
import com.golive.cinema.user.custom.domain.usecase.GetClientServiceUseCase;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.ClientService;
import com.golive.network.entity.Error;
import com.golive.network.entity.Login;
import com.golive.network.entity.RepeatMac;
import com.initialjie.log.Logger;

import rx.Observer;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by chgang on 2016/11/8.
 */

public class RepeatMacPresenter extends BasePresenter<RepeatMacContract.View> implements
        RepeatMacContract.Presenter {

    @NonNull
    private final RepeatMacUseCase mRepeatMacUseCase;
    @NonNull
    private final VerifyCodeUseCase mVerifyCodeUseCase;
    @NonNull
    private final LoginAgainUseCase mLoginAgainUseCase;
    @NonNull
    private final GetClientServiceUseCase mGetClientServiceUseCase;
    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    public RepeatMacPresenter(@NonNull RepeatMacContract.View initView,
            @NonNull RepeatMacUseCase repeatMacUseCase,
            @NonNull VerifyCodeUseCase verifyCodeUseCase,
            @NonNull LoginAgainUseCase loginAgainUseCase,
            @NonNull GetClientServiceUseCase getClientServiceUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(initView, "initView cannot be null!");
        this.mRepeatMacUseCase = checkNotNull(repeatMacUseCase, "repeatMacUseCase cannot be null!");
        this.mVerifyCodeUseCase = checkNotNull(verifyCodeUseCase,
                "verifyCodeUseCase cannot be null!");
        this.mLoginAgainUseCase = checkNotNull(loginAgainUseCase,
                "loginAgainUseCase cannot be null!");
        this.mGetClientServiceUseCase = checkNotNull(getClientServiceUseCase,
                "getClientServiceUseCase cannot be null!");
        this.mSchedulerProvider = checkNotNull(schedulerProvider,
                "BaseSchedulerProvider cannot be null!");
        attachView(initView);
        initView.setPresenter(this);
    }

    @Override
    public void loginByPhone(String phone) {
        Logger.d("loginByPhone, phone : " + phone);
        getView().setLoadingIndicator(true);
        Subscription subscription = mRepeatMacUseCase.run(new RepeatMacUseCase.RequestValues(phone))
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Observer<RepeatMacUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        RepeatMacContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "loginByPhone, onError : ");
                        RepeatMacContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                        view.showLoginByPhoneFailedView(e.getMessage());
                    }

                    @Override
                    public void onNext(RepeatMacUseCase.ResponseValue responseValue) {
//                        Logger.d("onNext:" + responseValue.getRepeatMac().toString());
                        RepeatMacContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        if (responseValue != null && responseValue.getRepeatMac() != null) {
                            RepeatMac repeatMac = responseValue.getRepeatMac();
                            if (repeatMac.isOk()) {
                                String status = repeatMac.getStatus();
                                view.showLoginByPhoneSuccessView(true, status);
                            } else {
                                view.showLoginByPhoneSuccessView(false, "");
                            }
                        }
                    }
                });
        addSubscription(subscription);
    }

    @Override
    public void getVerifyCode(String phone) {
        Subscription subscription = mVerifyCodeUseCase.run(
                new VerifyCodeUseCase.RequestValues(phone))
                .subscribe(new Observer<VerifyCodeUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getVerifyCode, onError : ");
                        RepeatMacContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        view.showGetVerifyCodeFailedView(e.getMessage());
                    }

                    @Override
                    public void onNext(VerifyCodeUseCase.ResponseValue responseValue) {
                        RepeatMacContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        if (responseValue != null) {
                            if (responseValue.getSuccess()) {
                                view.showGetVerifyCodeSuccessView();
                            } else {
                                view.showGetVerifyCodeFailedView(null);
                            }
                        }
                    }
                });
        addSubscription(subscription);
    }

    @Override
    public void loginByVerifyCode(String phone, String verifyCode, String status,
            String accountType) {
        Logger.d("loginByVerifyCode");
        String userId = "";
        String password = "";
        String branchtype = "";
        String kdmVersion = "";
        String kdmPlatform = "";
        LoginAgainUseCase.RequestValues values = new LoginAgainUseCase.RequestValues(userId,
                password, phone, verifyCode, status, accountType, branchtype, kdmVersion,
                kdmPlatform);
        Subscription subscription = mLoginAgainUseCase.run(values)
                .subscribe(new Observer<LoginAgainUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "loginAgain, onError : ");
                        RepeatMacContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.showVerifyFailedView(e.getMessage());
                    }

                    @Override
                    public void onNext(LoginAgainUseCase.ResponseValue responseValue) {
                        RepeatMacContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        Login login = responseValue.getLogin();
                        if (login != null) {
                            if (login.isOk()) {
                                view.showVerifySuccessView();
                            } else {
                                Error error = login.getError();
                                String errMsg = error != null ? error.getNote() : null;
                                view.showVerifyFailedView(errMsg);
                            }
                        }
                    }
                });
        addSubscription(subscription);
    }

    @Override
    public void getContactInformation() {
        Subscription subscription = mGetClientServiceUseCase.run(
                new GetClientServiceUseCase.RequestValues(false))
                .subscribe(new Subscriber<GetClientServiceUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "getTheClientService onError : ");
                    }

                    @Override
                    public void onNext(GetClientServiceUseCase.ResponseValue responseValue) {
                        Logger.d("getTheClientService onNext");
                        RepeatMacContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        String phone = null;
                        String qq = null;
                        ClientService clientService = responseValue.getClientService();
                        if (clientService != null) {
                            phone = clientService.getServicePhone5();
                            qq = clientService.getQQ();
                        }
                        view.showContactInfoView(phone, qq);
                    }
                });

        addSubscription(subscription);
    }
}
