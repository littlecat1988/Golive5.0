package com.golive.cinema.user.usercenter;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.UserHead;
import com.golive.network.entity.UserInfo;
import com.golive.network.entity.Wallet;
import com.golive.network.response.RecommendResponse;

/**
 * Created by Mowl on 2016/11/15.
 */

public class UserCenterContract {

    interface View extends IBaseView<UserCenterContract.Presenter> {
        void setLoadingIndicator(boolean active);

        void setUserInfo(UserInfo userInfo);

        void setWalletInfo(Wallet wallet);

        void showUserHead(UserHead head);

        void showTemplateView(RecommendResponse response);

        void showGetTemplateFailed(String errMsg);
    }

    interface Presenter extends IBasePresenter<UserCenterContract.View> {
        void getTemplateData(String pageId);

        void getUserInfo(boolean forceUpdate);

        void getUserHead(boolean forceUpdate);

        void getTheUserWallet(boolean forceUpdate);
//            Map<String,String> map loadPayUrl();
    }
}
