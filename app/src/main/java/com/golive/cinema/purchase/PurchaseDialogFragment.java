package com.golive.cinema.purchase;

import static android.app.Activity.RESULT_OK;

import static com.golive.cinema.util.FragmentUtils.removePreviousFragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.user.topup.TopupActivity;
import com.golive.cinema.util.Preconditions;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.FinanceOrder;
import com.golive.network.entity.Order;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.initialjie.log.Logger;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Wangzj on 2016/11/11.
 */

public class PurchaseDialogFragment extends BaseDialog implements PurchaseContract.View,
        View.OnClickListener {

    private static final int REQ_CODE_TOPUP = 123;
    private static final String QR_CODE_PAY_FRAG_TAG = "qr_code_pay_frag_tag";

    public interface OnPurchaseResultListener {

        /**
         * Called when user cancel.
         */
        void onCancel();

        /**
         * Called when purchase finish.
         *
         * @param success      Purchase success or not.
         * @param order        If purchase success, this is the corresponding order; otherwise,
         *                     this
         *                     is
         * @param financeOrder finance order, may be null.
         * @param errMsg       error message if purchase failed.
         */
        void onPurchaseResult(boolean success, Order order, FinanceOrder financeOrder,
                String errMsg);
    }

    public static PurchaseDialogFragment newInstance(@NonNull String productId, String productType,
            @Nullable String productName, @Nullable String mediaId, String price, boolean creditPay,
            String encryptionType, boolean isOnline, int quantity) {
        Preconditions.checkNotNull(productId);
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_PRODUCT_ID, productId);
        arguments.putString(Constants.EXTRA_PRODUCT_TYPE, productType);
        arguments.putString(Constants.EXTRA_PRODUCT_NAME, productName);
        arguments.putString(Constants.EXTRA_MEDIA_ID, mediaId);
        arguments.putString(Constants.EXTRA_PRICE, price);
        arguments.putBoolean(Constants.EXTRA_CREDIT_PAY, creditPay);
        arguments.putString(Constants.EXTRA_ENCRYPTION_TYPE, encryptionType);
        arguments.putBoolean(Constants.EXTRA_IS_ONLINE, isOnline);
        arguments.putInt(Constants.EXTRA_QUANTITY, quantity);
        PurchaseDialogFragment fragment = new PurchaseDialogFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    private PurchaseContract.Presenter mPresenter;
    private ProgressDialog mLoadingDlg;
    private ProgressDialog mPurchasingDlg;
    private ProgressDialog mRefundingDlg;
    private Button mPurchaseBtn;
    private Button mTopUpBtn;
    private Button mRefundCreditBtn;
    private TextView mTitleTv;
    private TextView mPriceTv;
    private TextView mPayTv;
    private TextView mCreditPayTv;
    private TextView mNeedPayTv;
    private TextView mCreditBalanceTv;
    private TextView mPurchaseHelpTv;
    private View mPurchaseIgv;
    private OnPurchaseResultListener mListener;

    @NonNull
    private String mProductId;

    @Nullable
    private String mProductName;

    @NonNull
    private String mProductType;

    @Nullable
    private String mMediaId;

    private String mPrice;

    private boolean mCreditPay;

    @Nullable
    private String mEncryptionType;

    private boolean mIsOnline;

    private int mQuantity;

    private String mPriceRMB;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        RxBus.get().register(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        mProductId = arguments.getString(Constants.EXTRA_PRODUCT_ID);
        mProductName = arguments.getString(Constants.EXTRA_PRODUCT_NAME);
        mProductType = arguments.getString(Constants.EXTRA_PRODUCT_TYPE);
        mMediaId = arguments.getString(Constants.EXTRA_MEDIA_ID);
        mPrice = arguments.getString(Constants.EXTRA_PRICE);
        mCreditPay = arguments.getBoolean(Constants.EXTRA_CREDIT_PAY);
        mEncryptionType = arguments.getString(Constants.EXTRA_ENCRYPTION_TYPE);
        mIsOnline = arguments.getBoolean(Constants.EXTRA_IS_ONLINE);
        mQuantity = arguments.getInt(Constants.EXTRA_QUANTITY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.purchase_frag, container, false);

        mTitleTv = (TextView) view.findViewById(R.id.title_tv);
        mPriceTv = (TextView) view.findViewById(R.id.price_tv);
        mPayTv = (TextView) view.findViewById(R.id.pay_tv);
        View creditPayTagTv = view.findViewById(R.id.credit_pay_tag_tv);
        mCreditPayTv = (TextView) view.findViewById(R.id.credit_pay_tv);
        mNeedPayTv = (TextView) view.findViewById(R.id.need_pay_tv);
        mCreditBalanceTv = (TextView) view.findViewById(R.id.credit_balance_tv);
        mRefundCreditBtn = (Button) view.findViewById(R.id.refund_credit_btn);
        mPurchaseBtn = (Button) view.findViewById(R.id.purchase_btn);
        mTopUpBtn = (Button) view.findViewById(R.id.top_up_btn);
        mPurchaseHelpTv = (TextView) view.findViewById(R.id.purchase_help_tv);
        mPurchaseIgv = view.findViewById(R.id.purchase_igv);

        mPriceRMB = getString(R.string.price_RMB);
        double price = 0;
        try {
            price = Double.parseDouble(mPrice);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        String titleStr;
        int purchaseBtnResId = R.string.purchase_confirm;
        switch (mProductType) {
            case Order.PRODUCT_TYPE_VIP:// vip
                titleStr = String.format(getString(R.string.purchase_title_vip), mProductName);
                break;
            case Order.PRODUCT_TYPE_VIP_MONTHLY: // vip monthly
                titleStr = String.format(getString(R.string.qrcode_pay_normal_vip_monthly), price);
                break;
            default: // others
                titleStr = String.format(getString(R.string.purchase_title), mProductName, price);
                purchaseBtnResId = R.string.purchase_confirm_and_play;
                break;
        }
        mTitleTv.setText(Html.fromHtml(titleStr));
        mPriceTv.setText(String.format(mPriceRMB, price));
        mPurchaseBtn.setText(purchaseBtnResId);
        mPurchaseBtn.setOnClickListener(this);
        mTopUpBtn.setOnClickListener(this);
        mRefundCreditBtn.setOnClickListener(this);

        int txtResId = mCreditPay ? R.string.purchase_help_credit_pay_not_deadline
                : R.string.purchase_help_info;
        mPurchaseHelpTv.setText(txtResId);

        UIHelper.setViewVisibleOrGone(mTopUpBtn, !mCreditPay);
        UIHelper.setViewVisibleOrGone(creditPayTagTv, mCreditPay);
        UIHelper.setViewVisibleOrGone(mCreditPayTv, mCreditPay);
        UIHelper.setViewVisibleOrGone(mCreditBalanceTv, mCreditPay);
        UIHelper.setViewVisibleOrGone(mRefundCreditBtn, mCreditPay);

        // make purchase view disable
        setPurchaseViewActive(false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Context context = getContext().getApplicationContext();
        mPresenter = new PurchasePresenter(this, mProductId, mProductType, mProductName, mMediaId,
                mEncryptionType, mIsOnline, mPrice, mQuantity, mCreditPay,
                Injection.provideGetUserWalletUseCase(context),
                Injection.provideGetUserCreditWalletUseCase(context),
                Injection.providePurchaseFilmUseCase(context),
                Injection.provideGetClientServiceUseCase(context),
                Injection.provideSchedulerProvider());

        if (getPresenter() != null) {
            getPresenter().start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
        if (getPresenter() != null) {
            getPresenter().unsubscribe();
        }
        UIHelper.dismissDialog(mLoadingDlg);
        UIHelper.dismissDialog(mPurchasingDlg);
        UIHelper.dismissDialog(mRefundingDlg);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        RxBus.get().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d("startActivityForResult, requestCode : " + requestCode + ", resultCode : "
                + resultCode);
        if (REQ_CODE_TOPUP == requestCode && RESULT_OK == resultCode) {

        }
    }

    @Override
    public void onClick(View v) {
        if (null == v) {
            return;
        }

        switch (v.getId()) {
            case R.id.purchase_btn:
                getPresenter().purchase();
                break;
            case R.id.top_up_btn:
                getPresenter().topUp();
                break;
            case R.id.refund_credit_btn:
                getPresenter().refundCredit();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Logger.d("onCancel");
        if (mListener != null) {
            mListener.onCancel();
        }
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (active && isActive()) {
            if (null == mLoadingDlg) {
                mLoadingDlg = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.purchase_loading_please_wait));
            }

            if (!mLoadingDlg.isShowing()) {
                mLoadingDlg.show();
            }
        } else if (!active) {
            if (mLoadingDlg != null) {
                UIHelper.dismissDialog(mLoadingDlg);
            }
        }
    }

    @Override
    public void showGetPayInfoSuccess() {
        setPurchaseViewActive(true);
    }

    @Override
    public void showGetPayInfoFailed(String errMsg) {
        setPurchaseViewActive(false);
        String failedMsg = getString(R.string.purchase_loading_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            failedMsg += errMsg;
        }
        Toast.makeText(getContext(), failedMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setPurchasingIndicator(boolean active) {
        if (active && isActive()) {
            if (null == mPurchasingDlg) {
                mPurchasingDlg = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.purchase_purchasing_please_wait));
                // set dialog can not be canceled.
                mPurchasingDlg.setCancelable(false);
                mPurchasingDlg.setCanceledOnTouchOutside(false);
            }

            if (!mPurchasingDlg.isShowing()) {
                mPurchasingDlg.show();
            }
        } else if (!active) {
            if (mPurchasingDlg != null) {
                UIHelper.dismissDialog(mPurchasingDlg);
            }
        }
    }

    @Override
    public void setRefundingCreditIndicator(boolean active) {
        if (active && isActive()) {
            if (null == mRefundingDlg) {
                mRefundingDlg = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.purchase_refunding_please_wait));
            }

            if (!mRefundingDlg.isShowing()) {
                mRefundingDlg.show();
            }
        } else if (!active) {
            if (mRefundingDlg != null) {
                UIHelper.dismissDialog(mRefundingDlg);
            }
        }
    }

    @Override
    public void showRefundSuccess() {
        Toast.makeText(getContext(), R.string.purchase_refund_success, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showRefundFailed(String errMsg) {
        if (!isAdded()) {
            return;
        }

        String msg = getString(R.string.purchase_refund_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            msg += ", " + errMsg;
        }
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setPurchaseVisible(boolean visible) {
        mPurchaseBtn.setEnabled(visible);
        mPurchaseBtn.setFocusable(visible);
        mPurchaseBtn.setFocusableInTouchMode(visible);
        if (visible) {
            mPurchaseBtn.requestFocus();
            mPurchaseBtn.requestFocusFromTouch();
        } else {
            if (View.VISIBLE == mRefundCreditBtn.getVisibility()) {
                mRefundCreditBtn.requestFocus();
                mRefundCreditBtn.requestFocusFromTouch();
            }
        }
    }

    @Override
    public void showBalanceEnough(boolean enough) {
        int resId = -1;
        if (!StringUtils.isNullOrEmpty(mProductType) && Order.PRODUCT_TYPE_VIP.equals(
                mProductType)) {
            resId = enough ? R.drawable.purchase_vip_balance_enough
                    : R.drawable.purchase_vip_balance_not_enough;
        } else {
            resId = enough ? R.drawable.purchase_normal_balance_enough
                    : R.drawable.purchase_normal_balance_not_enough;
        }
        mPurchaseIgv.setBackgroundResource(resId);
    }

    @Override
    public void setRefundCreditVisible(boolean visible) {
        mRefundCreditBtn.setVisibility(View.VISIBLE);
        mRefundCreditBtn.setEnabled(visible);
    }

    @Override
    public void showPayAmount(double amount) {
        mPayTv.setText(String.format(mPriceRMB, amount));
    }

    @Override
    public void showBalance(double balance) {
    }

    @Override
    public void showCreditPayAmount(double amount) {
        mCreditPayTv.setText(String.format(mPriceRMB, amount));
    }

    @Override
    public void showCreditBalance(double balance) {
        String text = getString(R.string.purchase_credit_balance);
        text = String.format(text, balance);
        mCreditBalanceTv.setText(text);
    }

    @Override
    public void showCreditPayDeadline(int days) {
        if (days > 0) {
            String txt = String.format(getString(R.string.purchase_help_credit_pay), days);
            mPurchaseHelpTv.setText(txt);
        }
    }

    @Override
    public void showNeedForPay(double amount) {
        mNeedPayTv.setText(String.format(mPriceRMB, amount));
    }

    @Override
    public void showTopUpUI() {
        Logger.d("showRegisterVipUI");
        Intent intent = new Intent(getContext(), TopupActivity.class);
        startActivityForResult(intent, REQ_CODE_TOPUP);
    }

    @Override
    public void showPurchaseSuccess(Order order, FinanceOrder financeOrder) {
        Toast.makeText(getContext(), R.string.purchase_success, Toast.LENGTH_SHORT).show();
        if (mListener != null) {
            mListener.onPurchaseResult(true, order, financeOrder, null);
        }

        // dismiss the dialog
        dismiss();

        Bus bus = RxBus.get();
        // purchase vip
        if (isPurchaseVip()) {
            bus.post(Constants.EventType.TAG_UPDATE_USER_INFO, true);
        }
        bus.post(Constants.EventType.TAG_UPDATE_WALLET, true);
    }

    @Override
    public void showPurchaseFailure(String errMsg) {
        String failedMsg = getString(R.string.purchase_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            failedMsg += ", " + errMsg;
        }
        Toast.makeText(getContext(), failedMsg, Toast.LENGTH_LONG).show();
        if (mListener != null) {
            mListener.onPurchaseResult(false, null, null, failedMsg);
        }

//        // dismiss the dialog
//        dismiss();
    }

    @Override
    public Observable<Boolean> showQrCodePayUI(final int type, final double payPrice,
            final double balance, final double creditBalance, final double refundCredit) {
        Logger.d("showQrCodePayUI, is credit pay : " + mCreditPay + ", type : " + type
                + ", payPrice : " + payPrice + ", balance : " + balance + ", creditBalance : "
                + creditBalance + ", is refund : " + refundCredit);

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                String fragTag = PurchaseDialogFragment.QR_CODE_PAY_FRAG_TAG;
                removePreviousFragment(getFragmentManager(), fragTag);
                boolean isRefund = PurchaseContract.QR_CODE_PAY_TYPE_REFUND == type;
                double needPay = isRefund ? refundCredit : payPrice;
                String produceName = isRefund ? getString(R.string.credit_pay_refund)
                        : mProductName;

                // Create and show the dialog.
                QrCodePayFragment fragment = QrCodePayFragment.newInstance(mProductId, produceName,
                        mProductType, needPay, balance, mCreditPay, creditBalance, isRefund, 0,
                        new QrCodePayFragment.OnQrCodePayResultListener() {
                            @Override
                            public void onCancel() {
                                Logger.d("showQrCodePayUI, onCancel");
                                // user cancel
                            }

                            @Override
                            public void onPurchaseResult(boolean success, String log) {
                                Logger.d("onPurchaseResult, success : " + success + ", log : "
                                        + log);
                                subscriber.onNext(success);
                            }
                        });
                fragment.setOnDialogDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
                fragment.show(getFragmentManager(), fragTag);
            }
        };

        return Observable.create(onSubscribe);
    }

    @Override
    public void showCustomerService(String phoneNumber, String qq) {
        if (mCreditPay) {
            return;
        }

        String text = "";
        if (!StringUtils.isNullOrEmpty(phoneNumber)) {
            text += String.format(getString(R.string.custom_phone), phoneNumber);
        }
        if (!StringUtils.isNullOrEmpty(qq)) {
            qq = String.format(getString(R.string.custom_qq), qq);
            if (StringUtils.isNullOrEmpty(text)) {
                text += qq;
            } else {
                text += "  " + qq;
            }
        }

        if (StringUtils.isNullOrEmpty(text)) {
            text = getString(R.string.purchase_help_info);
        }

        mPurchaseHelpTv.setText(text);
    }

    private void setPurchaseViewActive(boolean active) {
        mPurchaseBtn.setEnabled(active);
        mPurchaseBtn.setFocusable(active);
        mPurchaseBtn.setClickable(active);
    }

    @Override
    public void setPresenter(PurchaseContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    private PurchaseContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    public void setListener(OnPurchaseResultListener listener) {
        mListener = listener;
    }

    private boolean isPurchaseVip() {
        return !StringUtils.isNullOrEmpty(mProductType) &&
                (Order.PRODUCT_TYPE_VIP.equals(mProductType)
                        || Order.PRODUCT_TYPE_VIP_MONTHLY.equals(mProductType));
    }

    @Subscribe(
            tags = {@Tag(Constants.EventType.TAG_UPDATE_WALLET)}
    )
    public void onUpdateWallet(Object obj) {
        Logger.d("onUpdateWallet");
        if (getPresenter() != null) {
            // wallet is updated, so load view again
            getPresenter().start();
        }
    }
}