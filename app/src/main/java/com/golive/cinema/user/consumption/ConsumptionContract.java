package com.golive.cinema.user.consumption;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.WalletOperationItem;

import java.util.List;

/**
 * Created by Administrator on 2016/10/31.
 */

public class ConsumptionContract {


    interface View extends IBaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void showGetListView(List<WalletOperationItem> lists);

        void showGetError(String errMsg);
    }

    interface Presenter extends IBasePresenter<View> {

    }
}
