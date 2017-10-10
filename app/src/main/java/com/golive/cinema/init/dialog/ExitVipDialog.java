package com.golive.cinema.init.dialog;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.user.pay.QrcodeContract;
import com.golive.cinema.user.pay.QrcodeFragment;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.Combo;
import com.golive.network.response.RecommendComboResponse;
import com.hwangjr.rxbus.Bus;
import com.hwangjr.rxbus.RxBus;

/**
 * Created by chgang on 2016/12/14.
 */

public class ExitVipDialog extends BaseDialog implements View.OnClickListener, ExitVipContract.View,
        QrcodeContract.PayResultCallBack {

    public static final String FRAGMENT_TAG = "ExitVipDialog_Tag";
    public static final String VIP_LIST_KEY = "VipComboList_Tag";
    public static final String IS_VIP_MONTHLY_KEY = "is_vip_monthly_key";

    public static final int PAY_SUCCESS = 1;
    public static final int PAY_NO_MOVE = 2;
    public static final int PAY_NO_EXIT = 3;

    private Combo mCombo;
    private ExitVipContract.Presenter mPresenter;
    private ExitAppCallback mExitAppCallback;
    private ProgressDialog mProgressDialog;
    private boolean mIsPurchaseVipMonthly;

    public interface ExitAppCallback {
        void resultDismiss(Integer isExit);
    }

    public void setExitAppCallback(ExitAppCallback callback) {
        mExitAppCallback = callback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mCombo = (Combo) arguments.getSerializable(VIP_LIST_KEY);
            mIsPurchaseVipMonthly = arguments.getBoolean(IS_VIP_MONTHLY_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.exit_dialog_vip, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (mCombo != null) {
            //title
            TextView init_exit_dialog_title_tv =
                    (TextView) view.findViewById(R.id.init_exit_dialog_title_tv);
            if (!StringUtils.isNullOrEmpty(mCombo.getPrice())) {
                init_exit_dialog_title_tv.setText(
                        String.format(getString(R.string.init_exit_dialog_title_text),
                                mCombo.getName(), mCombo.getPrice()));
            }

            //btn
            view.findViewById(R.id.init_exit_dialog_qrcode_move_btn).setOnClickListener(this);
            view.findViewById(R.id.init_exit_dialog_qrcode_exit).setOnClickListener(this);

            //qr
//            mQrcodeLayout = (ViewGroup) view.findViewById(R.id.init_exit_dialog_qrcode_tv);
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCombo != null) {
            initQrCodeFragment();
            new ExitVipPresenter(mCombo.getVipProductId(), mCombo.getName(),
                    this, Injection.providePurchaseFilmUseCase(getContext()),
                    Injection.provideGetUserCreditWalletUseCase(getContext()));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.unsubscribe();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.init_exit_dialog_qrcode_move_btn) {
            if (mExitAppCallback != null) {
                mExitAppCallback.resultDismiss(PAY_NO_MOVE);
            }
        } else if (v.getId() == R.id.init_exit_dialog_qrcode_exit) {
            if (mExitAppCallback != null) {
                mExitAppCallback.resultDismiss(PAY_NO_EXIT);
            }
            dismiss();
        }
    }

    @Override
    public void setPurchasingIndicator(boolean active) {
        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mProgressDialog) {
                mProgressDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.purchase_purchasing_please_wait));
            }

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } else {
            if (mProgressDialog != null) {
                UIHelper.dismissDialog(mProgressDialog);
            }
        }
    }

    @Override
    public void showPurchaseSuccess() {
        Toast.makeText(getContext(), R.string.purchase_success, Toast.LENGTH_SHORT).show();
        // send notification
        Bus bus = RxBus.get();
        bus.post(Constants.EventType.TAG_UPDATE_USER_INFO, true);

        if (mExitAppCallback != null) {
            mExitAppCallback.resultDismiss(PAY_SUCCESS);
        }
        dismiss();
    }

    @Override
    public void showPurchaseFailure(String errMsg) {
        String failedMsg = getString(R.string.purchase_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            failedMsg += ", " + errMsg;
        }
        Toast.makeText(getContext(), failedMsg, Toast.LENGTH_LONG).show();
        initQrCodeFragment();
//        dismiss();
    }

    @Override
    public void setPresenter(ExitVipContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void PayFinishExecute(int state, String log) {
        String text;
        switch (state) {
            case 1:
                text = getString(R.string.qrcode_pay_success);
                break;
            default:
                text = getString(R.string.qrcode_pay_failed);
                break;
        }

        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();

        // pay success
        if (1 == state) {

            Bus bus = RxBus.get();
            bus.post(Constants.EventType.TAG_UPDATE_WALLET, true);

            // is vip monthly
            if (mIsPurchaseVipMonthly) {
                // send notification
                bus.post(Constants.EventType.TAG_UPDATE_USER_INFO, true);

                // show purchase success
                showPurchaseSuccess();
            } else {
                // not vip monthly
                if (!mIsPurchaseVipMonthly && mPresenter != null) {
                    // purchase
                    mPresenter.purchase();
                }
            }
        }
    }

    private void initQrCodeFragment() {
        int height = (int) getResources().getDimension(
                R.dimen.init_exit_dialog_qrcode_width_height);
        String price = mCombo.getPrice();
        String vipProductId = mCombo.getVipProductId();
        String name = mCombo.getName();
        QrcodeFragment fragment = QrcodeFragment.newInstance(price,
                vipProductId, name, this, QrcodeContract.ALI_WECHAT_MODE_BOTH,
                mIsPurchaseVipMonthly, height, height, false);
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.init_exit_dialog_qrcode_tv, fragment)
                .commit();
    }
}
