package com.golive.cinema.user.topup;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.TopupRechargeItem;
import com.golive.network.entity.Wallet;

import java.util.List;

/**
 * Created by Administrator on 2016/10/31.
 */

public class TopupContract {


    interface View extends IBaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void showLoadingFailed(String errMsg);

        void showResultListView(List<TopupRechargeItem> lists);

        void setWalletInfo(Wallet wallet);

        void setServicePhoneInfo(String phone);
    }

    interface Presenter extends IBasePresenter<View> {
        void getUserWallet();
    }

}
