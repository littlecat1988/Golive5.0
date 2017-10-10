package com.golive.cinema.user.custom;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.ClientService;
import com.golive.network.entity.MainConfig;

/**
 * Created by Administrator on 2016/10/31.
 */

public class CustomContract {

    interface View extends IBaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        //            void showWechatInfo(WechatInfo info);
        void showMainInfo(MainConfig info);

        void showClientService(ClientService info);

        void setKdmVersion(String version, String platform);
    }

    interface Presenter extends IBasePresenter<View> {
        void loadInfo();
    }
}
