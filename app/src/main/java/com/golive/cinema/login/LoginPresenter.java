package com.golive.cinema.login;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.login.domain.usecase.LoginUseCase;
import com.golive.cinema.util.EspressoIdlingResource;
import com.golive.network.entity.Login;
import com.initialjie.log.Logger;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Wangzj on 2016/8/22.
 */

public class LoginPresenter extends BasePresenter<LoginContract.View> implements
        LoginContract.Presenter {

    private final LoginUseCase mUseCase;

    public LoginPresenter(@NonNull LoginContract.View view, @NonNull LoginUseCase useCase) {
        checkNotNull(view, "loginView cannot be null!");
        mUseCase = checkNotNull(useCase, "loginUseCase cannot be null!");
        attachView(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        login("", "", "");
    }

    @Override
    public void login(String userId, String password, String status) {

        getView().setLoadingIndicator(true);

        // The network request might be handled in a different thread so make sure Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        String branchtype = "";
        String kdmVersion = "";
        String kdmPlatform = "";
        LoginUseCase.RequestValues requestValues =
                new LoginUseCase.RequestValues(userId, password, status, branchtype, kdmVersion,
                        kdmPlatform);

        Subscription subscription =
                mUseCase.run(requestValues)
                        .subscribe(new Subscriber<LoginUseCase.ResponseValue>() {
                            @Override
                            public void onCompleted() {
                                EspressoIdlingResource.decrement();

                                if (!getView().isActive()) {
                                    return;
                                }

                                getView().setLoadingIndicator(false);
                            }

                            @Override
                            public void onError(Throwable e) {
                                EspressoIdlingResource.decrement();
                                Logger.e(e, "login, onError : ");

                                // The view may not be able to handle UI updates anymore
                                if (!getView().isActive()) {
                                    return;
                                }

                                getView().setLoadingIndicator(false);
                                getView().showLoginFailed();
                            }

                            @Override
                            public void onNext(LoginUseCase.ResponseValue responseValue) {

                                // The view may not be able to handle UI updates anymore
                                if (!getView().isActive()) {
                                    return;
                                }

                                Login login = responseValue.getLogin();
                                processLogin(login);
                            }
                        });
        addSubscription(subscription);
    }

    private void processLogin(Login login) {
        if (null == login) {
            getView().showLoginFailed();
        } else {
            getView().showLogin(login);
        }
    }
}
