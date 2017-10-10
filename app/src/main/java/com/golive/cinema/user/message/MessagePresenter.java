package com.golive.cinema.user.message;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.init.domain.usecase.ServerStatusUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.CreditOperationUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.FinanceMessageUseCase;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.initialjie.log.Logger;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;

public class MessagePresenter extends BasePresenter<MessageContract.View> implements
        MessageContract.Presenter {
    @NonNull
    private final CreditOperationUseCase mCreditOperationUseCase;
    @NonNull
    private final FinanceMessageUseCase mFinanceMessageUseCase;
    @NonNull
    private final ServerStatusUseCase mServerStatusUseCase;
    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    public MessagePresenter(@NonNull MessageContract.View initView,
            @NonNull CreditOperationUseCase creditOperationUseCase,
            @NonNull FinanceMessageUseCase financeMessageUseCase,
            @NonNull ServerStatusUseCase serverStatusUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(initView, "initView cannot be null!");
        this.mCreditOperationUseCase = checkNotNull(creditOperationUseCase,
                "creditOperationUseCase cannot be null!");
        this.mFinanceMessageUseCase = checkNotNull(financeMessageUseCase,
                "financeMessageUseCase cannot be null!");
        this.mServerStatusUseCase = checkNotNull(serverStatusUseCase,
                "serverStatusUseCase cannot be null!");
        this.mSchedulerProvider = checkNotNull(schedulerProvider,
                "schedulerProvider cannot be null!");
        attachView(initView);
        initView.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        getView().setLoadingIndicator(true);
        Subscription subscription = Observable.zip(getCreditOperationObs(), getFinanceMsgObs(),
                getServerStatusObs(),
                new Func3<CreditOperationUseCase.ResponseValue, FinanceMessageUseCase
                        .ResponseValue, ServerStatusUseCase.ResponseValue, Object>() {
                    @Override
                    public Object call(CreditOperationUseCase.ResponseValue responseValue,
                            FinanceMessageUseCase.ResponseValue responseValue2,
                            ServerStatusUseCase.ResponseValue responseValue3) {
                        return null;
                    }
                })
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        MessageContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                        view.showAllMessageView();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, " onError : ");
                        MessageContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        view.setLoadingIndicator(false);
                    }

                    @Override
                    public void onNext(Object o) {
                    }
                });
        addSubscription(subscription);
    }

    private Observable<CreditOperationUseCase.ResponseValue> getCreditOperationObs() {
        return mCreditOperationUseCase.run(
                new CreditOperationUseCase.RequestValues(false))
                .onErrorReturn(new Func1<Throwable, CreditOperationUseCase.ResponseValue>() {
                    @Override
                    public CreditOperationUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getCreditOperationObs, onErrorReturn");
                        return null;
                    }
                })
                .doOnNext(new Action1<CreditOperationUseCase.ResponseValue>() {
                    @Override
                    public void call(CreditOperationUseCase.ResponseValue responseValue) {
                        MessageContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.copyCreditToMessage(responseValue.getCreditOperation());
                    }
                });
    }

    private Observable<FinanceMessageUseCase.ResponseValue> getFinanceMsgObs() {
        return mFinanceMessageUseCase.run(
                new FinanceMessageUseCase.RequestValues(false))
                .onErrorReturn(new Func1<Throwable, FinanceMessageUseCase.ResponseValue>() {
                    @Override
                    public FinanceMessageUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getFinanceMsgObs, onErrorReturn");
                        return null;
                    }
                })
                .doOnNext(new Action1<FinanceMessageUseCase.ResponseValue>() {
                    @Override
                    public void call(FinanceMessageUseCase.ResponseValue responseValue) {
                        MessageContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.copyFinanceToMessage(responseValue.getFinanceMessage());
                    }
                });
    }

    private Observable<ServerStatusUseCase.ResponseValue> getServerStatusObs() {
        return mServerStatusUseCase.run(new ServerStatusUseCase.RequestValues())
                .onErrorReturn(new Func1<Throwable, ServerStatusUseCase.ResponseValue>() {
                    @Override
                    public ServerStatusUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "getServerStatusObs, onErrorReturn");
                        return null;
                    }
                })
                .doOnNext(new Action1<ServerStatusUseCase.ResponseValue>() {
                    @Override
                    public void call(ServerStatusUseCase.ResponseValue responseValue) {
                        MessageContract.View view = getView();
                        if (null == view || !view.isActive() || null == responseValue) {
                            return;
                        }
                        view.copyServerToMessage(responseValue.getServerMessageList());
                    }
                });
    }

}
