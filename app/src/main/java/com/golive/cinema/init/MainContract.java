package com.golive.cinema.init;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.CreditOperation;
import com.golive.network.entity.FinanceMessage;
import com.golive.network.entity.ServerMessage;
import com.golive.network.response.ApplicationPageResponse;

import java.util.List;

import rx.Observable;

/**
 * Created by chgang on 2016/11/19.
 */

public interface MainContract {

    interface View extends IBaseView<MainContract.Presenter> {

        void showPageView(ApplicationPageResponse pageResponse);

        void showPageError(String msg);

        Observable<?> showCreditOperationView(CreditOperation creditOperation);

        Observable<?> showFinanceMessageView(FinanceMessage financeMessage);

        void showAllMessageView(List<ServerMessage> messageList);

        void onCompleted();
    }

    interface Presenter extends IBasePresenter<MainContract.View> {
        void initAppPage();
    }

}
