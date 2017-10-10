package com.golive.cinema.init;

import static com.golive.cinema.Constants.PAGE_INDEX_TOPIC;
import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.Constants;
import com.golive.cinema.init.domain.usecase.AppPageUseCase;
import com.golive.cinema.init.domain.usecase.ServerStatusUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.CreditOperationUseCase;
import com.golive.cinema.user.usercenter.domain.usecase.FinanceMessageUseCase;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.network.entity.CreditOperation;
import com.golive.network.entity.FinanceMessage;
import com.golive.network.entity.ServerMessage;
import com.golive.network.response.ApplicationPageResponse;
import com.google.gson.Gson;
import com.initialjie.log.Logger;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/11/19.
 */

public class MainPresenter extends BasePresenter<MainContract.View> implements
        MainContract.Presenter {

    private boolean mIsInit = false;

    @NonNull
    private final AppPageUseCase mAppPageUseCase;
    @NonNull
    private final CreditOperationUseCase mCreditOperationUseCase;
    @NonNull
    private final FinanceMessageUseCase mFinanceMessageUseCase;
    @NonNull
    private final ServerStatusUseCase mServerStatusUseCase;
    @NonNull
    private final BaseSchedulerProvider mSchedulerProvider;

    public MainPresenter(@NonNull MainContract.View initView,
            @NonNull AppPageUseCase appPageUseCase,
            @NonNull CreditOperationUseCase creditOperationUseCase,
            @NonNull FinanceMessageUseCase financeMessageUseCase,
            @NonNull ServerStatusUseCase serverStatusUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(initView, "initView cannot be null!");
        this.mAppPageUseCase = checkNotNull(appPageUseCase, "appPageUseCase cannot be null!");
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
        if (!mIsInit) {
            initAppPage();
        }
    }

    @Override
    public void initAppPage() {
        Subscription subscription = mAppPageUseCase.run(new AppPageUseCase.RequestValues())
                .subscribe(new Observer<AppPageUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "initAppPage, onError : ");
                        MainContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }
                        //view.showPageError(e.getMessage());
                        view.showPageView(new Gson().fromJson(Constants.getAppPage(),
                                ApplicationPageResponse.class));
                    }

                    @Override
                    public void onNext(AppPageUseCase.ResponseValue responseValue) {
                        MainContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return;
                        }

                        ApplicationPageResponse response = responseValue.getResponse();
//                        // check topic
//                        boolean topicEnable = checkTopicEnable(response);
//                        // save
//                        Constants.INCLUDE_TOPICS_DEFAULT = topicEnable;
                        if (response != null && response.isOk()) {
                            view.showPageView(response);
                            initUserMessage();
                        } else {
//                            String appPage = Constants.getAppPage(topicEnable);
                            String appPage = Constants.getAppPage();
                            view.showPageView(
                                    new Gson().fromJson(appPage, ApplicationPageResponse.class));
                        }
                        mIsInit = true;
                    }
                });
        addSubscription(subscription);
    }

    /**
     * check whether film topic is enable by server
     */
    private boolean checkTopicEnable(ApplicationPageResponse response) {
        if (response != null && response.isOk() && response.getApplicationPage() != null
                && response.getApplicationPage().getBasePage() != null
                && response.getApplicationPage().getBasePage().getNavigation() != null) {
            List<ApplicationPageResponse.Data> datas =
                    response.getApplicationPage().getBasePage().getNavigation().getDatas();
            final String topicStr = String.valueOf(PAGE_INDEX_TOPIC);
            for (ApplicationPageResponse.Data data : datas) {
                String action = data.getActionContent();
                if (!StringUtils.isNullOrEmpty(action) && topicStr.equals(action)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void initUserMessage() {
        Subscription subscription = mCreditOperationUseCase.run(
                new CreditOperationUseCase.RequestValues(true))
                .onErrorReturn(new Func1<Throwable, CreditOperationUseCase.ResponseValue>() {
                    @Override
                    public CreditOperationUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "CreditOperationUseCase, onErrorReturn : ");
                        return null;
                    }
                })
                .flatMap(new Func1<CreditOperationUseCase.ResponseValue, Observable<?>>() {
                    @Override
                    public Observable<?> call(CreditOperationUseCase.ResponseValue responseValue) {
                        MainContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }
                        CreditOperation creditOperation = null;
                        if (responseValue != null) {
                            creditOperation = responseValue.getCreditOperation();
                        }
                        return view.showCreditOperationView(creditOperation);
                    }
                })
                .flatMap(new Func1<Object, Observable<FinanceMessageUseCase.ResponseValue>>() {
                    @Override
                    public Observable<FinanceMessageUseCase.ResponseValue> call(Object o) {
                        return mFinanceMessageUseCase.run(
                                new FinanceMessageUseCase.RequestValues(true));
                    }
                })
                .onErrorReturn(new Func1<Throwable, FinanceMessageUseCase.ResponseValue>() {
                    @Override
                    public FinanceMessageUseCase.ResponseValue call(Throwable throwable) {
                        Logger.w(throwable, "FinanceMessageUseCase, onErrorReturn : ");
                        return null;
                    }
                })
                .flatMap(new Func1<FinanceMessageUseCase.ResponseValue, Observable<?>>() {
                    @Override
                    public Observable<?> call(FinanceMessageUseCase.ResponseValue responseValue) {
                        MainContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }
                        FinanceMessage financeMessage = null;
                        if (responseValue != null) {
                            financeMessage = responseValue.getFinanceMessage();
                        }
                        return view.showFinanceMessageView(financeMessage);
                    }
                })
                .flatMap(new Func1<Object, Observable<ServerStatusUseCase.ResponseValue>>() {
                    @Override
                    public Observable<ServerStatusUseCase.ResponseValue> call(Object o) {
                        return mServerStatusUseCase.run(
                                new ServerStatusUseCase.RequestValues());
                    }
                })
                .subscribe(new Observer<ServerStatusUseCase.ResponseValue>() {
                    @Override
                    public void onCompleted() {
                        MainContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.onCompleted();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e, "initUserMessage, onError : ");
                    }

                    @Override
                    public void onNext(ServerStatusUseCase.ResponseValue response) {
                        List<ServerMessage> serverMessageList = null;
                        if (response != null) {
                            serverMessageList = response.getServerMessageList();
                        }
                        MainContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.showAllMessageView(serverMessageList);
                        }
                    }
                });
        addSubscription(subscription);
    }
}
