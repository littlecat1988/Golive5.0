package com.golive.cinema.user.topup;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;

/**
 * Created by Mowl on 2016/11/14.
 */

public class PayTopupContract {

    interface View extends IBaseView<PayTopupContract.Presenter> {
        void setLoadingIndicator(boolean active);

//        void showQrcode(Map<String, String> urlMap);


    }

    interface Presenter extends IBasePresenter<PayTopupContract.View> {
//        Map<String,String> map loadPayUrl();
//            void loadVipPackages();
//           Map<String,String> map loadPayUrl();
    }


}
