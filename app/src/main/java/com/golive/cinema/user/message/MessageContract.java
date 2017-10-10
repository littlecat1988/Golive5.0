package com.golive.cinema.user.message;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.CreditOperation;
import com.golive.network.entity.FinanceMessage;
import com.golive.network.entity.ServerMessage;

import java.util.List;

public interface MessageContract {

    interface View extends IBaseView<MessageContract.Presenter> {
        void setLoadingIndicator(boolean active);

        void copyServerToMessage(List<ServerMessage> messageList);

        void copyCreditToMessage(CreditOperation credit);

        void copyFinanceToMessage(FinanceMessage finance);

        void showAllMessageView();
    }

    interface Presenter extends IBasePresenter<MessageContract.View> {
    }
}
