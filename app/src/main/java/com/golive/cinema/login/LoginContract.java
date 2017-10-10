package com.golive.cinema.login;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.Login;

/**
 * Created by Wangzj on 2016/7/25.
 */

public class LoginContract {

    interface View extends IBaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showLoginFailed();

        void showLogin(Login login);
    }

    interface Presenter extends IBasePresenter<View> {
        void login(String userId, String password, String status);
    }
}
