package com.golive.cinema.user.buyvip;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.UserInfo;
import com.golive.network.entity.VipCombo;
import com.golive.network.entity.Wallet;

import java.util.List;

/**
 * Created by Administrator on 2016/10/31.
 */

public class BuyVipContract {

    interface View extends IBaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void showVipListView(List<VipCombo> lists, boolean isVip);

        void showUserInfo(UserInfo userInfo);

        void setWalletInfo(Wallet wallet);

        void setCreditInfo(Wallet wallet);

        void showPurchaseVip(VipCombo vipCombo);

        void showPurchaseVipError(String errMsg);

        void showPurchaseVipMonthlyRepeat();

        void showCreditPayExpired(final int expireDate, final double creditBill,
                final double creditMaxLimit);
    }

    interface Presenter extends IBasePresenter<View> {
        void loadVipPackages(boolean updateUserInfo);

        void getTheUserWallet();

        void purchaseVip(VipCombo vipCombo);
    }

    public static String getVipPayPrice(VipCombo item, boolean isVip) {
        if (null == item) {
            return "";
        }
        String oldPrice = item.getPrice();
        String vipPrice = item.getVipPrice();
        String curPrice = item.getCurPrice();

        if (isVip) {
            return vipPrice;
        } else {
            if (!StringUtils.isNullOrEmpty(curPrice)) {
                return curPrice;
            } else {
                return oldPrice;
            }
        }
    }
}
