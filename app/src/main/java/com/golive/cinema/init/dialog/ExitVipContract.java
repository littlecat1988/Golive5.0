package com.golive.cinema.init.dialog;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;

/**
 * This specifies the contract between the view and the presenter.
 * Created by Wangzj on 2016/7/8.
 */
public interface ExitVipContract {

    interface View extends IBaseView<Presenter> {
        void setPurchasingIndicator(boolean active);

        void showPurchaseSuccess();

        void showPurchaseFailure(String errMsg);
    }

    interface Presenter extends IBasePresenter<View> {
        void purchase();
    }
}