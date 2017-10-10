package com.golive.cinema.user.buyvip;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.PayOrderResult;
import com.golive.network.entity.Wallet;

/**
 * Created by Mowl on 2016/11/17.
 */

public class VipPurchaseContract {


    interface View extends IBaseView<VipPurchaseContract.Presenter> {
        void setWalletInfo(Wallet wallet);

        void showPurchaseResult(boolean isSuccess, PayOrderResult payresult, String errorCode);
    }

    interface Presenter extends IBasePresenter<VipPurchaseContract.View> {
        void doPurchaseVip(String productId, String encryptionType, int quantity, String currency);

        void getTheUserWallet();
    }

}
