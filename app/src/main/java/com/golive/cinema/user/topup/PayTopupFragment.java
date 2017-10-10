package com.golive.cinema.user.topup;

import static com.golive.cinema.user.usercenter.UserPublic.TipsDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.golive.cinema.Constants;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.user.pay.QrcodeContract;
import com.golive.cinema.user.pay.QrcodeFragment;
import com.golive.cinema.user.usercenter.UserPublic;
import com.golive.cinema.util.StringUtils;
import com.golive.network.helper.UserInfoHelper;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;
import com.initialjie.log.Logger;

import java.text.DecimalFormat;

/**
 * Created by Mowl on 2016/11/10.
 */

public class PayTopupFragment extends MvpFragment implements PayTopupContract.View,
        QrcodeContract.PayResultCallBack {
    private PayTopupContract.Presenter mPresenter;
    private TextView mTitleTv, mPriceTv, mScanTv;
    private TextView mPriceNameTv, mWorkingTv, mBottomTv;
    private String mPayPrice;
    private String mPayName;
    private String mPricePnt;
    private Dialog mDialog;
    private CountDownTimer mCountDownTimer;

    public static PayTopupFragment newInstance() {
        return new PayTopupFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.user_pay_qrcode_topup, container, false);
        mTitleTv = (TextView) root.findViewById(R.id.user_pay_qrcode_title);
        mPriceTv = (TextView) root.findViewById(R.id.user_pay_qrcode_price);
        mScanTv = (TextView) root.findViewById(R.id.user_pay_qrcode_scanprice);
        mPriceNameTv = (TextView) root.findViewById(R.id.user_pay_qrcode_price_name);
        mWorkingTv = (TextView) root.findViewById(R.id.topup_pay_qrcode_right_tips_work);
        mBottomTv = (TextView) root.findViewById(R.id.topup_credit_buttom_text);

        Intent intent = getActivity().getIntent();
        this.mPayPrice = intent.getStringExtra(UserPublic.KEY_PAY_PRICE);
        this.mPayName = intent.getStringExtra(UserPublic.KEY_PAY_NAME);
        String mPageCode = intent.getStringExtra(UserPublic.KEY_PAY_PAGE);
        Logger.d("price=" + mPayPrice + ",name=" + mPayName);
        if (mPayName != null) {
            setTopupName(mPayName);
        } else {
            setTopupName(String.format(getString(R.string.topup_please_pay_yuan), mPayPrice));
        }
        mPricePnt = new DecimalFormat("#0.00").format(Float.parseFloat(mPayPrice));
        setTopupPrice(mPricePnt);
        setTopupScan(mPricePnt);
        if (!StringUtils.isNullOrEmpty(mPageCode) && "credit".equals(mPageCode)) {
            mPriceNameTv.setText(getString(R.string.vip_pay_credit_totol));
            mWorkingTv.setText(getString(R.string.credit_qrcode_pay_quik_finish));

            String lineDays = UserInfoHelper.getLineDayCredit(getContext());
            String date = "";
            if (!StringUtils.isNullOrEmpty(lineDays)) {
                try {
                    if (Integer.parseInt(lineDays) > 0) {
                        date = String.format(getString(R.string.you_pay_intime_ok), lineDays);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            mBottomTv.setText(String.format(getString(R.string.user_credit_bottom_tips), date));
            mBottomTv.setVisibility(View.VISIBLE);
        } else {
            mPriceNameTv.setText(getString(R.string.vip_pay_dlg_goods_name));
            mWorkingTv.setText(getString(R.string.topup_qrcode_pay_quik_finish));
            mBottomTv.setVisibility(View.GONE);
        }

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initQrcodeFragment();
        mPresenter = new PayTopupPresenter(this);
        if (getPresenter() != null) {
            getPresenter().start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dismissDialog();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    private void initQrcodeFragment() {
        int height = (int) getContext().getResources().getDimension(R.dimen.user_topup_qrcode_h);
        FragmentTransaction ft = this.getChildFragmentManager().beginTransaction();
        QrcodeFragment mQrcodeFragment = QrcodeFragment.newInstance(
                mPayPrice, null,
                mPayName,
                this,
                QrcodeContract.ALI_WECHAT_MODE_BOTH,
                false, height, height, false);
        ft.replace(R.id.user_topup_qrcode_view_inclut, mQrcodeFragment);
        ft.commit();
    }

    @Override
    public void setPresenter(PayTopupContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    @Override
    protected PayTopupContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void setLoadingIndicator(boolean active) {
    }

    private void setTopupName(String val) {
        mTitleTv.setText(val);
    }

    private void setTopupPrice(String val) {
        mPriceTv.setText(val + getString(R.string.RMB));
    }

    private void setTopupScan(String val) {
        mScanTv.setText(val + getString(R.string.RMB));
    }

    @Override
    public void PayFinishExecute(int state, String log) {
        Logger.d("PayFinishExecute ,state=" + state + ",log=" + log);

        if (1 == state) {
            setDetailDialog(true);

            Bus bus = RxBus.get();
            bus.post(Constants.EventType.TAG_UPDATE_WALLET, true);
        } else {
            setDetailDialog(false);
        }
    }

    private void setDetailDialog(final Boolean success) {
        if (!isAdded() || getActivity().isFinishing()) {
            return;
        }

        mDialog = TipsDialog(getContext(), R.layout.user_topup_successfull_tips);

        TextView title = (TextView) mDialog.findViewById(R.id.tv_user_pay_dlg_title);
        String showText;
        if (success) {
            showText = String.format(getString(R.string.topup_dl_sucess_and_credit), mPricePnt);
        } else {
            showText = getString(R.string.topup_failed);
        }
        title.setText(showText);

        final Button okButton = (Button) mDialog.findViewById(R.id.user_dialog_pay_bt_yes);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
                if (success) {
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                }
            }
        });
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mCountDownTimer != null) {
                    mCountDownTimer.cancel();
                }
            }
        });
        mDialog.show();

        if (success) {
            final String text = getResources().getString(R.string.user_topup_btn_time);
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
            mCountDownTimer = new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    okButton.setText(String.format(text, millisUntilFinished / 1000));
                }

                @Override
                public void onFinish() {
                    dismissDialog();
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                }
            };
            mCountDownTimer.start();
        }
    }

    private void dismissDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}
