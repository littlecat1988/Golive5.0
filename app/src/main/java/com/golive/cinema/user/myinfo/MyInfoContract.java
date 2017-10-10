package com.golive.cinema.user.myinfo;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.UserHead;
import com.golive.network.entity.UserInfo;
import com.golive.network.entity.VipMonthlyResult;
import com.golive.network.entity.Wallet;

import java.util.Map;

/**
 * Created by Administrator on 2016/10/31.
 */

public class MyInfoContract {


    interface View extends IBaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void setUserInfo(UserInfo userInfo);

        void setWalletInfo(Wallet wallet);

        void showUserHead(UserHead head);

        void setCreditInfo(Wallet wallet);

        void setVipMonthlyInfo(VipMonthlyResult vipMonthly);
    }

    interface Presenter extends IBasePresenter<View> {
    }

    interface ActiveVipView extends IBaseView<ActiveVipPresenter> {
        void setLoadingIndicator(boolean active);

        void checkUrlMap(Map<String, String> urlMap);

        void setServicePhoneInfo(String phone);
    }

    interface ActiveVipPresenter extends IBasePresenter<ActiveVipView> {
        void getloadPayUrl();
    }


    interface CreditRepayView extends IBaseView<CreditRepayPresenter> {
        void setLoadingIndicator(boolean active);

        void showError(String errMsg);

        void setCreditInfo(Wallet wallet);
    }

    interface CreditRepayPresenter extends IBasePresenter<CreditRepayView> {

    }
}
