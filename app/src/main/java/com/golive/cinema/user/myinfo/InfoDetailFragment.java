package com.golive.cinema.user.myinfo;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_ACCOUNT_INFO;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MY_ACCOUNT;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.golive.cinema.Injection;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.user.usercenter.UserPublic;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.UserHead;
import com.golive.network.entity.UserInfo;
import com.golive.network.entity.VipMonthlyResult;
import com.golive.network.entity.Wallet;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.log.Logger;

/**
 * Created by Mowl on 2016/11/9.
 */


public class InfoDetailFragment extends MvpFragment implements
        MyInfoContract.View {//, View.OnClickListener
    //    private ProgressDialog mProgressDialog;
    private MyInfoContract.Presenter mPresenter;
    private TextView mUserIdTv, mMoneyTv, mCreditTv, mVipDateTv, mMonthVipTv, mVipNameTv;
    private ImageView mUserHeader;
    private ImageView mVipHead;
    private long mEnterTime;
    private String mMaxCredit = "20.00";
    private ProgressDialog mProgressDialog;

    public static InfoDetailFragment newInstance() {
        return new InfoDetailFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.user_myinfo_info, container, false);
        mUserIdTv = (TextView) root.findViewById(R.id.my_info_head_tv);
        mVipDateTv = (TextView) root.findViewById(R.id.my_info_detail_11);
        mMoneyTv = (TextView) root.findViewById(R.id.my_info_detail_21);
        mCreditTv = (TextView) root.findViewById(R.id.my_info_detail_31);
        mMonthVipTv = (TextView) root.findViewById(R.id.my_info_detail_41);
        mVipNameTv = (TextView) root.findViewById(R.id.my_info_detail_10);
        mVipHead = (ImageView) root.findViewById(R.id.my_info_detail_vip_image);
        mUserHeader = (ImageView) root.findViewById(R.id.user_head_info_image);
        mVipHead.setImageResource(R.drawable.user_info_head_normol);
//        mVipNameTv.setText(getText(R.string.user_info_ipname) + " :");
        mUserIdTv.setText(UserInfoHelper.getUserId(getContext()));
//        mVipDateTv.setText(R.string.user_info_ipname_normol);
//        mMoneyTv.setText(R.string.user_zero_money);
//        mCreditTv.setText(R.string.user_zero_money);
//        mMonthVipTv.setText(R.string.myinfo_credit_monthvip_off);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("onActivityCreated");
        Context context = getContext().getApplicationContext();
        String headurl = UserInfoHelper.getUserHeadUrl(context);
        if (!StringUtils.isNullOrEmpty(headurl)) {
            Glide.with(this).load(headurl).into(mUserHeader);
        }
        mPresenter = new MyInfoPresenter(this,
                Injection.provideGetUserInfoUseCase(context),
                Injection.provideGetUserWalletUseCase(context),
                Injection.provideGetUserCreditWalletUseCase(context),
                Injection.provideGetUserHeadUseCase(context),
                Injection.provideGetVipMonthlyStatusUseCase(context),
                Injection.provideGetVipListUseCase(context));
        mPresenter.start();

//        if (StringUtils.isNullOrEmpty(UserPublic.vipMonthId)) {
//            mPresenter.getVipListAndMonthlyStatus();
//        } else {
//            mPresenter.getVipMonthlyStatus(UserPublic.vipMonthId);
//        }

        mEnterTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(getContext()).reportEnterActivity(VIEW_CODE_ACCOUNT_INFO,
                "账户信息", VIEW_CODE_MY_ACCOUNT);
    }

    @Override
    public void setPresenter(MyInfoContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    @Override
    public MyInfoContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
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
    public void setUserInfo(UserInfo userInfo) {
        if (null == userInfo) {
            return;
        }

        if (userInfo.isVIP()) {
            int dateTime = getValitDate(userInfo.getEffectivetime());
            if (dateTime < 0) {
                dateTime = 0;
            }
            mVipHead.setImageResource(R.drawable.user_info_head_vip);
            String vipRemain = String.format(
                    getActivity().getString(R.string.myinfo_vip_remain_days), "" + dateTime);
            mVipDateTv.setText(
                    getActivity().getString(R.string.user_info_ipname_golive) + "(" + vipRemain
                            + ")");
        } else {
            mVipHead.setImageResource(R.drawable.user_info_head_normol);
            mVipDateTv.setText(getActivity().getString(R.string.user_info_ipname_normol)
                    + "(" + getActivity().getString(R.string.user_info_ipname_normol_ts) + ")");
        }
    }

    @Override
    public void setWalletInfo(Wallet wallet) {
        if (wallet != null) {
            String money = wallet.getValue();
            if (money != null) {
                mMoneyTv.setText(money + getString(R.string.RMB));
            }
        }
    }

    @Override
    public void showUserHead(UserHead head) {
        if (null == head) {
            return;
        }
        Logger.d("showUserHead getIconName=" + head.getIconName() + ",geticonUrl="
                + head.geticonUrl());

        String url = head.geticonUrl();
        if (!StringUtils.isNullOrEmpty(url)) {
            UserInfoHelper.setUserHeadUrl(getContext(), url);
            Glide.with(this).load(url).into(mUserHeader);
        }
    }

    @Override
    public void setCreditInfo(Wallet wallet) {
        if (null == wallet) {
            return;
        }
        String creditMoney = wallet.getValue();
        if (wallet.getCreditLine() != null) {
            mMaxCredit = wallet.getCreditLine();
        }
        double moneyDb = Double.valueOf(creditMoney);
        double maxCreditDb = Math.abs(Double.valueOf(mMaxCredit));
        String rmbStr = getString(R.string.RMB);
        if (moneyDb < 0) {
            double creditDb = Math.abs(moneyDb);
            if (creditDb <= maxCreditDb) {
                String str = UserPublic.getFormatStrFromDouble(Math.abs(maxCreditDb - creditDb));
                mCreditTv.setText(str + rmbStr);
            } else {
                mCreditTv.setText("0.00" + rmbStr);
            }
        } else {
            mCreditTv.setText(mMaxCredit + rmbStr);
        }
    }

    @Override
    public void setVipMonthlyInfo(VipMonthlyResult vipMonthly) {
        boolean isMonthlyVip = vipMonthly != null && vipMonthly.isOk()
                && !StringUtils.isNullOrEmpty(vipMonthly.getStatus());
        Logger.d("setVipMonthlyInfo, isMonthlyVip : " + isMonthlyVip);
        mMonthVipTv.setText(isMonthlyVip ? R.string.myinfo_credit_monthvip_on
                : R.string.myinfo_credit_monthvip_off);
    }

    private int getValitDate(String effectivetime) {
        int dateTime = 0;
        try {
            double diff = Double.parseDouble(effectivetime);
            double days = diff / (60 * 60 * 24);
            dateTime = (int) days + 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String time = String.valueOf((System.currentTimeMillis() - mEnterTime) / 1000);
        StatisticsHelper.getInstance(getActivity()).reportExitActivity(VIEW_CODE_ACCOUNT_INFO,
                "账户信息", "", time);
    }
}
