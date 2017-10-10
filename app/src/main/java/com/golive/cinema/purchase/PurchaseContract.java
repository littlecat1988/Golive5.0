package com.golive.cinema.purchase;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.network.entity.FinanceOrder;
import com.golive.network.entity.Order;

import rx.Observable;

/**
 * Created by Wangzj on 2016/7/25.
 */

public class PurchaseContract {

    public static final int QR_CODE_PAY_TYPE_TOP_UP = 1;
    public static final int QR_CODE_PAY_TYPE_REFUND = 2;

    public interface View extends IBaseView<Presenter> {

        void setLoadingIndicator(boolean active);

        void showGetPayInfoSuccess();

        void showGetPayInfoFailed(String errMsg);

        void setPurchasingIndicator(boolean active);

        void setRefundingCreditIndicator(boolean active);

        void showRefundSuccess();

        void showRefundFailed(String errMsg);

        void setPurchaseVisible(boolean visible);

        void showBalanceEnough(boolean enough);

        void setRefundCreditVisible(boolean visible);

        void showPayAmount(double amount);

        void showBalance(double balance);

        void showCreditPayAmount(double amount);

        void showCreditBalance(double balance);

        void showCreditPayDeadline(int days);

        void showNeedForPay(double amount);

        void showTopUpUI();

        void showPurchaseSuccess(Order order, FinanceOrder financeOrder);

        void showPurchaseFailure(String errMsg);

        /**
         * Show qr code pay UI
         *
         * @param type          Qr code pay type. 1, top up; 2, refund the credit
         * @param payPrice      Price amount to pay
         * @param balance       User wallet balance
         * @param creditBalance Credit balance
         * @param refundCredit  Price amount to refund the credit
         * @return Pay successfully, return <code>true<code/>; otherwise, return <code>false<code/>.
         */
        Observable<Boolean> showQrCodePayUI(int type, double payPrice, double balance,
                double creditBalance, double refundCredit);

        void showCustomerService(String phoneNumber, String qq);
    }

    public interface Presenter extends IBasePresenter<View> {

        void loadPurchaseDetail();

        void purchase();

        void topUp();

        void refundCredit();
    }
}
