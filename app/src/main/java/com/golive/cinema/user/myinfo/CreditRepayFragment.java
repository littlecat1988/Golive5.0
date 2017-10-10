package com.golive.cinema.user.myinfo;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_CREDIT_PAY;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MY_ACCOUNT;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.golive.cinema.Injection;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.user.topup.TopupActivity;
import com.golive.cinema.user.usercenter.UserPublic;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.Wallet;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.log.Logger;

/**
 * Created by Mowl on 2016/11/11.
 */

public class CreditRepayFragment extends MvpFragment implements MyInfoContract.CreditRepayView {
    private MyInfoContract.CreditRepayPresenter mPresenter;
    private Button mPayButton;
    private TextView mCanUsedMoneyTv, mMaxCreditTv, mBottomTv, mCreditPriceTv;
    //mCreditPriceTv,mBottomTv
    private ProgressDialog mProgressDialog;
    private String mCreditPrice, mCanUsedMoney;
    private String mMaxCredit = "20.00";
    private long mEnterTime;

    public static CreditRepayFragment newInstance() {
        return new CreditRepayFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.user_myinfo_credit_pay, container, false);
        mCreditPriceTv = (TextView) root.findViewById(R.id.user_credit_need_pay);
        mCanUsedMoneyTv = (TextView) root.findViewById(R.id.user_credit_can_used_num);
        mMaxCreditTv = (TextView) root.findViewById(R.id.user_credit_max);
        mBottomTv = (TextView) root.findViewById(R.id.user_credit_buttom_text);
        mPayButton = (Button) root.findViewById(R.id.user_credit_btn_pay);
        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TopupActivity.class);
                intent.putExtra(UserPublic.KEY_USER_FRAGMENT,
                        UserPublic.TOPUP_FRAGMENT_QRCAODE_PAY);
                intent.putExtra(UserPublic.KEY_PAY_PRICE, mCreditPrice);
                String titleNameStr = String.format(
                        getActivity().getString(R.string.credit_please_pay_yuan), mCreditPrice);
                intent.putExtra(UserPublic.KEY_PAY_NAME, titleNameStr);
                intent.putExtra(UserPublic.KEY_PAY_PAGE, "credit");
                getActivity().startActivityForResult(intent, 1);
            }
        });
        mCreditPrice = "0.00";
        mCanUsedMoney = "0.00";//mMaxCredit - mCreditPrice
        String rmbStr = getString(R.string.RMB);
        mMaxCreditTv.setText(mMaxCredit + rmbStr);
        mCanUsedMoneyTv.setText(mCanUsedMoney + rmbStr);
        mCreditPriceTv.setText(mCreditPrice + rmbStr);
        mBottomTv.setText(
                String.format(getActivity().getString(R.string.user_credit_bottom_tips), ""));
        initButtonState();
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("onActivityCreated");
        mEnterTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(getContext()).reportEnterActivity(VIEW_CODE_CREDIT_PAY, "先看后付",
                VIEW_CODE_MY_ACCOUNT);
        Context applicationContext = getContext().getApplicationContext();
        mPresenter = new CreditRepayPresenter(this,
                Injection.provideGetUserCreditWalletUseCase(applicationContext));
        if (getPresenter() != null) {
            getPresenter().start();
        }
    }

    private void initButtonState() {
        mPayButton.setText(
                String.format(getActivity().getString(R.string.credit_btn_pay_yuan), mCreditPrice));
        double creditPriceDb = Double.valueOf(mCreditPrice);
        Context context = getContext();
        if (creditPriceDb <= 0.001) {
//            mPayButton.setEnabled(false);
//            mPayButton.setFocusable(false);
            mPayButton.setClickable(false);
            mPayButton.setTextColor(ContextCompat.getColor(context, R.color.info_credit_text_hui));
        } else {
//            mPayButton.setEnabled(true);
//            mPayButton.setFocusable(true);
            mPayButton.setClickable(true);
            mPayButton.setTextColor(ContextCompat.getColor(context, R.color.info_credit_text_bai));
        }
    }

    @NonNull
    @Override
    protected MyInfoContract.CreditRepayPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mProgressDialog) {
                mProgressDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.please_wait));
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
    public void showError(String errMsg) {
        Toast.makeText(getContext(), errMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setCreditInfo(Wallet wallet) {
        if (null == wallet) {
            return;
        }

        String price = wallet.getValue();
        double priceDb = Double.valueOf(price);
        String totalCreditMoney = wallet.getCreditLine();
        if (!StringUtils.isNullOrEmpty(totalCreditMoney)) {
            UserInfoHelper.setMaxCredit(getContext(), totalCreditMoney);
            mMaxCredit = totalCreditMoney;
        }
        if (priceDb < 0) {
            mCreditPrice = UserPublic.getFormatStrFromDouble(Math.abs(priceDb));
            mCanUsedMoney = getCanUsedMoney(price, mMaxCredit);
        } else {
            mCreditPrice = "0.00";
            mCanUsedMoney = mMaxCredit;
        }
        String rmbStr = getString(R.string.RMB);
        mMaxCreditTv.setText(mMaxCredit + rmbStr);
        mCreditPriceTv.setText(mCreditPrice + rmbStr);
        mCanUsedMoneyTv.setText(mCanUsedMoney + rmbStr);
        initButtonState();

        String date = "";
        String lineDays = wallet.getCreditDeadLineDays();
        if (lineDays != null && Integer.parseInt(lineDays) > 0) {
            date = String.format(getString(R.string.you_pay_intime_ok), lineDays);
            UserInfoHelper.setLineDayCredit(getContext(), lineDays);
        } else {
            UserInfoHelper.setLineDayCredit(getContext(), "0");
        }
        mBottomTv.setText(String.format(getString(R.string.user_credit_bottom_tips), date));
    }

    @Override
    public void setPresenter(MyInfoContract.CreditRepayPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private String getCanUsedMoney(String credit, String maxC) {
        try {
            double creditD = Double.valueOf(credit);
            creditD = Math.abs(creditD);
            double maxD = Double.valueOf(maxC);
            maxD = Math.abs(maxD);
            double canUsedD = maxD - creditD;
            if (canUsedD < 0) {
                canUsedD = 0;
            }
            Logger.d("setCreditInfo,creditD=" + creditD + ",maxD" + maxD + ",canUsedD=" + canUsedD);
            return UserPublic.getFormatStrFromDouble(canUsedD);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return maxC;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String time = String.valueOf((System.currentTimeMillis() - mEnterTime) / 1000);
        StatisticsHelper.getInstance(getActivity()).reportExitActivity(VIEW_CODE_CREDIT_PAY, "先看后付",
                "", time);
    }
}