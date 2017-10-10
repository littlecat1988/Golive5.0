package com.golive.cinema.init;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;

/**
 * Created by chgang on 2016/11/8.
 */

public interface RepeatMacContract {

    interface View extends IBaseView<RepeatMacContract.Presenter> {
        void setLoadingIndicator(boolean active);

        void showContactInfoView(String phone, String qq);

        void showLoginByPhoneSuccessView(boolean loginSuccess, String status);

        void showLoginByPhoneFailedView(String msg);

        void showGetVerifyCodeSuccessView();

        void showGetVerifyCodeFailedView(String msg);

        void showVerifySuccessView();

        void showVerifyFailedView(String msg);
    }

    interface Presenter extends IBasePresenter<RepeatMacContract.View> {
        void loginByPhone(String phone);

        void getVerifyCode(String phone);

        void loginByVerifyCode(String phone, String verifyCode, String status, String accountType);

        void getContactInformation();
    }

}
