package com.golive.cinema.purchase;

import static com.golive.cinema.user.myinfo.PayServiceAgreementContract.AGREEMENT_TYPE_MONTH;
import static com.golive.cinema.user.myinfo.PayServiceAgreementContract.AGREEMENT_TYPE_SIGN;
import static com.golive.cinema.util.FragmentUtils.removePreviousFragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.user.myinfo.PayServiceAgreementFragment;
import com.golive.cinema.user.pay.QrcodeContract;
import com.golive.cinema.user.pay.QrcodeFragment;
import com.golive.cinema.util.MathExtend;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.Order;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.initialjie.log.Logger;

/**
 * Created by Wangzj on 2016/11/16.
 */

public class QrCodePayFragment extends BaseDialog implements QrcodeContract.PayResultCallBack,
        View.OnClickListener {

    private static final String PAY_SERVICE_KNOWN_FRAG_TAG = "pay_service_known_frag_tag";

    public interface OnQrCodePayResultListener {

        /**
         * Called when user cancel.
         */
        void onCancel();

        /**
         * Called when purchase finish.
         *
         * @param success Purchase success or not.
         * @param log     log.
         */
        void onPurchaseResult(boolean success, String log);
    }

    private String mProductId;
    private String mProductName;
    private String mProductType;
    private double mNeedPay;
    private double mPrice;
    private double mWalletPay;
    private boolean mIsCreditPay;
    private double mCreditBalance;
    private boolean mRefundCredit;
    private int mCreditPayDeadLineDays;
    private OnQrCodePayResultListener mListener;

    public static QrCodePayFragment newInstance(String productId, String productName,
            String productType, double price, double walletPay, boolean isCreditPay,
            double creditBalance, boolean refundCredit, int creditPayDeadLineDays,
            OnQrCodePayResultListener listener) {
        QrCodePayFragment fragment = new QrCodePayFragment();
        fragment.mListener = listener;
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_PRODUCT_ID, productId);
        arguments.putString(Constants.EXTRA_PRODUCT_NAME, productName);
        arguments.putString(Constants.EXTRA_PRODUCT_TYPE, productType);
        arguments.putDouble(Constants.EXTRA_PRICE, price);
        arguments.putDouble(Constants.EXTRA_NORMAL_PAY_AMOUNT, walletPay);
        arguments.putBoolean(Constants.EXTRA_CREDIT_PAY, isCreditPay);
        arguments.putDouble(Constants.EXTRA_CREDIT_PAY_AMOUNT, creditBalance);
        arguments.putBoolean(Constants.EXTRA_REFUND_REFUND_CREDIT, refundCredit);
        arguments.putInt(Constants.EXTRA_CREDIT_PAY_DEADLINE, creditPayDeadLineDays);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        mProductId = arguments.getString(Constants.EXTRA_PRODUCT_ID);
        mProductName = arguments.getString(Constants.EXTRA_PRODUCT_NAME);
        mProductType = arguments.getString(Constants.EXTRA_PRODUCT_TYPE);
        mPrice = arguments.getDouble(Constants.EXTRA_PRICE);
        mWalletPay = arguments.getDouble(Constants.EXTRA_NORMAL_PAY_AMOUNT);
        mIsCreditPay = arguments.getBoolean(Constants.EXTRA_CREDIT_PAY);
        mCreditBalance = arguments.getDouble(Constants.EXTRA_CREDIT_PAY_AMOUNT);
        mRefundCredit = arguments.getBoolean(Constants.EXTRA_REFUND_REFUND_CREDIT);
        mCreditPayDeadLineDays = arguments.getInt(Constants.EXTRA_CREDIT_PAY_DEADLINE);

        // need pay
        double totalBalance = mWalletPay;
        if (mIsCreditPay) {
            totalBalance += Math.abs(mCreditBalance);
        }
        mNeedPay = mRefundCredit ? mPrice : MathExtend.subtract(mPrice, totalBalance);
        Logger.d("onCreate, mProductName : " + mProductName + ", mPrice : " + mPrice
                + ", mWalletPay : " + mWalletPay + ", is credit pay : " + mIsCreditPay
                + ", credit balance : " + mCreditBalance + ", mRefundCredit : " + mRefundCredit);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        int layoutId =
                isPurchaseVipMonthly() ? R.layout.qrcode_pay_vip_monthly_frag
                        : R.layout.qrcode_pay_frag;
        final View view = inflater.inflate(layoutId, container, false);

        TextView titleTv = (TextView) view.findViewById(R.id.qrcode_pay_title_tv);
        TextView priceTv = (TextView) view.findViewById(R.id.qrcode_pay_price_tv);
        TextView priceTagTv = (TextView) view.findViewById(R.id.qrcode_pay_price_tag_tv);
        TextView walletPayTv = (TextView) view.findViewById(R.id.qrcode_pay_wallet_pay_tv);
        TextView walletPayTagTv = (TextView) view.findViewById(R.id.qrcode_pay_wallet_pay_tag_tv);
        TextView creditPayTv = (TextView) view.findViewById(R.id.qrcode_pay_credit_balance_tv);
        TextView creditPayTagTv = (TextView) view.findViewById(
                R.id.qrcode_pay_credit_balance_tag_tv);
        TextView needPayTv = (TextView) view.findViewById(R.id.qrcode_pay_need_pay_tv);
        TextView noticeTv = (TextView) view.findViewById(R.id.qrcode_pay_notice_tv);
        TextView helpTv = (TextView) view.findViewById(R.id.qrcode_pay_help_tv);
        View vipMonthlyKnownView = view.findViewById(R.id.qrcode_pay_vip_monthly_known_btn);
        View payServiceKnownView = view.findViewById(R.id.qrcode_pay_pay_service_known_btn);

        // show price
        if (walletPayTv != null) {
            UIHelper.setViewVisibleOrGone(walletPayTv, !mRefundCredit);
        }
        if (walletPayTagTv != null) {
            UIHelper.setViewVisibleOrGone(walletPayTagTv, !mRefundCredit);
        }
        if (!mRefundCredit && walletPayTv != null) {
            String walletPayText = String.format(getString(R.string.price_RMB), mWalletPay);
            walletPayTv.setText(walletPayText);
        }
        String needPayText = String.format(getString(R.string.price_RMB), mNeedPay);
        needPayTv.setText(needPayText);
        if (creditPayTv != null) {
            UIHelper.setViewVisibleOrGone(creditPayTv, mIsCreditPay);
        }
        if (creditPayTagTv != null) {
            UIHelper.setViewVisibleOrGone(creditPayTagTv, mIsCreditPay);
        }
        if (mIsCreditPay) {
            String text = String.format(getString(R.string.price_RMB), Math.abs(mCreditBalance));
            creditPayTv.setText(text);
        }

        String priceText = String.format(getString(R.string.price_RMB), mPrice);
        priceTv.setText(priceText);
        priceTagTv.setText(
                mRefundCredit ? R.string.qrcode_pay_price_refund : R.string.qrcode_pay_price);

        // show title, notice
        String titleStr;
        String noticeStr = noticeTv.getText().toString();
        switch (mProductType) {
            case Order.PRODUCT_TYPE_VIP:// vip
                titleStr = String.format(getString(R.string.qrcode_pay_title_vip), mProductName,
                        mNeedPay);
                noticeStr = getString(R.string.qrcode_pay_to_register_vip);
                break;
            case Order.PRODUCT_TYPE_VIP_MONTHLY: // vip monthly
                titleStr = String.format(getString(R.string.qrcode_pay_normal_vip_monthly),
                        mNeedPay);
                break;
            default: // others
                titleStr = getString(
                        mRefundCredit ? R.string.qrcode_pay_refund : R.string.qrcode_pay_normal);
                titleStr = String.format(titleStr, mNeedPay);
                noticeStr = getString(mRefundCredit ? R.string.qrcode_pay_to_refund_credit
                        : R.string.qrcode_pay_to_play_film);
                break;
        }
        titleTv.setText(Html.fromHtml(titleStr));
        noticeTv.setText(noticeStr);

        String helpText = null;
        if (mRefundCredit) {
            if (mCreditPayDeadLineDays > 0) {
                helpText = String.format(getString(R.string.purchase_help_credit_pay),
                        mCreditPayDeadLineDays);
            } else {
                helpText = getString(R.string.purchase_help_credit_pay_not_deadline);
            }
        } else {
            helpText = getString(R.string.purchase_help_info);
        }
        helpTv.setText(helpText);

        if (vipMonthlyKnownView != null) {
            vipMonthlyKnownView.setOnClickListener(this);
        }

        if (payServiceKnownView != null) {
            payServiceKnownView.setOnClickListener(this);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        String type;
        switch (v.getId()) {
            case R.id.qrcode_pay_vip_monthly_known_btn:
                type = AGREEMENT_TYPE_MONTH;
                break;
            case R.id.qrcode_pay_pay_service_known_btn:
            default:
                type = AGREEMENT_TYPE_SIGN;
                break;
        }
        String fragTag = PAY_SERVICE_KNOWN_FRAG_TAG;
        removePreviousFragment(getFragmentManager(), fragTag);
        // show service agreement view
        PayServiceAgreementFragment frag = PayServiceAgreementFragment.newInstance(type);
        frag.show(getFragmentManager(), fragTag);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initQrCodeFragment();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Logger.d("onCancel");
        if (mListener != null) {
            mListener.onCancel();
        }
    }

    private void initQrCodeFragment() {
        int height = (int) getActivity().getResources().getDimension(
                R.dimen.qrcode_pay_view_size_w);
        QrcodeFragment fragment = QrcodeFragment.newInstance(String.valueOf(mNeedPay),
                mProductId, mProductName, this, QrcodeContract.ALI_WECHAT_MODE_BOTH,
                isPurchaseVipMonthly(), height, height, false);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.qrcode_pay_fl, fragment)
                .commit();
    }

    @Override
    public void PayFinishExecute(int state, String log) {
        Logger.d("PayFinishExecute, state : " + state + ", log : " + log);
        String text;
        switch (state) {
            case 1:
                text = getString(R.string.qrcode_pay_success);
                break;
            default:
                text = getString(R.string.qrcode_pay_failed);
                break;
        }

        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();

        // pay success
        boolean paySuccess = 1 == state;
        if (paySuccess) {
            // dismiss the dialog
            dismiss();
        }

        if (mListener != null) {
            mListener.onPurchaseResult(paySuccess, log);
        }

        if (isPurchaseVipMonthly()) {
            Bus bus = RxBus.get();
            bus.post(Constants.EventType.TAG_UPDATE_USER_INFO, true);
            bus.post(Constants.EventType.TAG_UPDATE_WALLET, true);
        }
    }

    private boolean isPurchaseVipMonthly() {
        return !StringUtils.isNullOrEmpty(mProductType) && Order.PRODUCT_TYPE_VIP_MONTHLY.equals(
                mProductType);
    }
}
