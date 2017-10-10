package com.golive.cinema.user.myinfo;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;

/**
 * Created by Wangzj on 2017/3/6.
 */

public interface PayServiceAgreementContract {

    String AGREEMENT_TYPE_SIGN = "1";
    String AGREEMENT_TYPE_MONTH = "2";

    interface View extends IBaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void showPayServiceAgreement(String title, String content);
    }

    interface Presenter extends IBasePresenter<View> {
        /**
         * @param type 1 付费服务协议; 2 连续包月声明
         */
        void getPayServiceAgreement(String type);
    }
}
