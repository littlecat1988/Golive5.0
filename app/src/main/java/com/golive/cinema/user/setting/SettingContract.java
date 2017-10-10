package com.golive.cinema.user.setting;

import android.content.Context;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.Upgrade;

/**
 * Created by Administrator on 2016/10/31.
 */

public class SettingContract {


    interface View extends IBaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void setCheckingUpgradeIndicator(boolean active);

        void setMainConfig(MainConfig cfg);

        void setKdmVersion(String version, String platform);

        void showUpgradeView(Upgrade upgrade, int upgradeType);

        void setChangeServerKey(String key);
    }

    interface Presenter extends IBasePresenter<View> {
        void getKdmVersion(Context context);

        void checkUpgrade(Context context, int versionCode, String versionName);

        void getConfigLoad();
    }


    interface CheckUpgradeView extends IBaseView<CheckUpgradePresenter> {
//        Observable<Boolean> showUpgradeView(Upgrade upgrade, int upgradeType);
    }

    interface CheckUpgradePresenter extends IBasePresenter<CheckUpgradeView> {
//        void checkUpgrade(Context context);
    }


}
