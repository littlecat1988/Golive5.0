package com.golive.cinema.init;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.Ad;
import com.golive.network.entity.Login;
import com.golive.network.entity.ServerMessage;
import com.golive.network.entity.Upgrade;

import rx.Observable;

/**
 * Created by chgang on 2016/10/27.
 */

public interface InitContract {

    interface View extends IBaseView<Presenter> {

        void showServerTimeout();

        Observable<Boolean> showServerStop(ServerMessage msg);

        Observable<Boolean> showConfirmAdvertView(Ad ad);

        Observable<Boolean> showUpgradeView(Upgrade upgrade, int upgradeType);

        String getMacAddress();

        Observable<Boolean> showMacInvalidView(String status);

        void showLoginView(Login login);

        void showLoginFailedView(String note);

        void showCompleted();

        void showInitFailed(String msg);

        void showClearUserCache();

        int getVersionCode();

        String getVersionName();

        void setPlayerUpgrading(boolean active);

        void showPlayerUpgradeSuccess();

        void showPlayerUpgradeFailed(int errCode);

        void showKdmType(String kdmType);
    }

    interface Presenter extends IBasePresenter<View> {
        void init();
    }
}
