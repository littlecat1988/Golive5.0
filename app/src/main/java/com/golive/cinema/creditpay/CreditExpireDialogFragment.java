package com.golive.cinema.creditpay;

import static com.golive.cinema.util.FragmentUtils.removePreviousFragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.purchase.QrCodePayFragment;
import com.golive.network.entity.Order;
import com.initialjie.log.Logger;

/**
 * Created by Wangzj on 2016/11/15.
 */

public class CreditExpireDialogFragment extends BaseDialog implements View.OnClickListener {

    private static final String QR_CODE_PAY_FRAG_TAG = "qr_code_pay_frag_tag";

    public interface OnRefundResultListener {

        /**
         * Called when user cancel.
         */
        void onCancel();

        /**
         * Called when refund finish.
         *
         * @param success Refund success or not.
         * @param order   If refund success, this is the corresponding order; otherwise, this is
         *                <code>null<code/>.
         */
        void onRefundResult(boolean success, Order order);
    }

    private int mExpireDate;
    private double mBill;
    private double mLimit;
    private OnRefundResultListener mResultListener;

    public static CreditExpireDialogFragment newInstance(int expireDate, double bill,
            double limit) {
        CreditExpireDialogFragment fragment = new CreditExpireDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(Constants.EXTRA_CREDIT_EXPIRE_DATE, expireDate);
        arguments.putDouble(Constants.EXTRA_CREDIT_EXPIRE_BILL, bill);
        arguments.putDouble(Constants.EXTRA_CREDIT_EXPIRE_LIMIT, limit);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        mExpireDate = arguments.getInt(Constants.EXTRA_CREDIT_EXPIRE_DATE);
        mBill = arguments.getDouble(Constants.EXTRA_CREDIT_EXPIRE_BILL);
        mLimit = arguments.getDouble(Constants.EXTRA_CREDIT_EXPIRE_LIMIT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.credit_expire_frag, container, false);

        TextView tv = (TextView) view.findViewById(R.id.credit_expire_content);
        View refundView = view.findViewById(R.id.credit_expire_refund_btn);
        View noticeLaterBtn = view.findViewById(R.id.credit_expire_notice_later_btn);
        TextView noticeTv = (TextView) view.findViewById(R.id.credit_expire_notice_tv);

        String text = getString(R.string.credit_expire_content);
        String content = String.format(text, Integer.toString(mExpireDate), mBill, mLimit);
        tv.setText(Html.fromHtml(content));

        // has dead line days
        if (mExpireDate > 0) {
            String txt = String.format(getString(R.string.purchase_help_credit_pay), mExpireDate);
            noticeTv.setText(txt);
        }

        refundView.setOnClickListener(this);
        noticeLaterBtn.setOnClickListener(this);

        return view;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mResultListener != null) {
            mResultListener.onCancel();
        }
    }

    @Override
    public void onClick(View v) {
        if (null == v) {
            return;
        }

        switch (v.getId()) {
            case R.id.credit_expire_refund_btn:
                String fragTag = QR_CODE_PAY_FRAG_TAG;
                removePreviousFragment(getFragmentManager(), fragTag);
                // Create and show the dialog.
                QrCodePayFragment fragment = QrCodePayFragment.newInstance(null,
                        getString(R.string.credit_pay_refund), Order.PRODUCT_TYPE_THEATRE, mBill, 0,
                        false, 0, true, 0, new QrCodePayFragment.OnQrCodePayResultListener() {
                            @Override
                            public void onCancel() {
                                Logger.d("showQrCodePayUI, onCancel");
                            }

                            @Override
                            public void onPurchaseResult(boolean success, String log) {
                                Logger.d("onPurchaseResult, success : " + success + ", log : "
                                        + log);
                                if (mResultListener != null) {
                                    mResultListener.onRefundResult(success, null);
                                }
                                if (success) {
                                    // dismiss the dialog
                                    dismiss();
                                }
                            }
                        });

                fragment.show(getFragmentManager(), fragTag);
                break;

            case R.id.credit_expire_notice_later_btn:
                if (mResultListener != null) {
                    mResultListener.onCancel();
                }
                // dismiss the dialog
                dismiss();
                break;

            default:
                break;
        }

    }

    public void setOnRefundResultListener(OnRefundResultListener resultListener) {
        mResultListener = resultListener;
    }
}
