package com.golive.cinema.user.buyvip;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_REGISTER_VIP;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_USER_CENTER;
import static com.golive.cinema.user.usercenter.UserPublic.TipsDialog;
import static com.golive.cinema.util.FragmentUtils.removePreviousFragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.creditpay.CreditExpireDialogFragment;
import com.golive.cinema.purchase.PurchaseDialogFragment;
import com.golive.cinema.purchase.QrCodePayFragment;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.user.usercenter.UserPublic;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.ToastUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.cinema.views.metroviews.widget.TvRecyclerView;
import com.golive.network.entity.FinanceOrder;
import com.golive.network.entity.Order;
import com.golive.network.entity.UserInfo;
import com.golive.network.entity.VipCombo;
import com.golive.network.entity.Wallet;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.log.Logger;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class BuyVipFragment extends MvpFragment implements BuyVipContract.View {
    private static final String FRAG_TAG_CREDIT_EXPIRED = "frag_tag_credit_expired";
    private static final String PURCHASE_FRAG_TAG = "PURCHASE_FRAG_TAG";
    private static final int MSG_VIEW_FOCUSED = 0;
    private BuyVipContract.Presenter mPresenter;
    private TextView mTitleTv;
    private TvRecyclerView mRecyclerView;
    private ProgressDialog mProgressDialog;
    private long mEnterTime;
    private boolean mIsUserVip;
    private String mHaveMoney = "";
    private final List<VipCombo> mVipCombos = new ArrayList<>();
    private CountDownTimer mCountTimer;

    public static BuyVipFragment newInstance() {
        return new BuyVipFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.user_buyvip, container, false);
        Logger.d("onCreateView");
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (TvRecyclerView) view.findViewById(R.id.buy_vip_list);
        initListView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("onActivityCreated");
        mEnterTime = System.currentTimeMillis();
        FragmentActivity activity = getActivity();
        StatisticsHelper.getInstance(activity).reportEnterActivity(VIEW_CODE_REGISTER_VIP,
                "开通会员", VIEW_CODE_USER_CENTER);
//        String viplevel = UserInfoHelper.getUserVip(activity);
//        mIsUserVip = !StringUtils.isNullOrEmpty(viplevel) && "2".equals(viplevel);
        mTitleTv = (TextView) activity.findViewById(R.id.user_buyvip_title_tv);

        if (getPresenter() != null) {
            getPresenter().loadVipPackages(false);
        }
    }

    @Override
    public void setPresenter(BuyVipContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    @Override
    protected BuyVipContract.Presenter getPresenter() {
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
    public void showVipListView(List<VipCombo> lists, boolean isVip) {
        Logger.d("showVipListView, isVip : " + isVip);
        mVipCombos.clear();
        if (lists != null && lists.size() > 0) {
            mVipCombos.addAll(lists);
        } else {
            setFailedDialog();
            return;
        }

        int listsize = mVipCombos.size();
        if (listsize <= 5) {
            float itemW = getResources().getDimension(R.dimen.vip_item_width);
            float paddingL = (itemW * 0.510f) * ((5 - listsize)) + itemW * 0.054f;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mRecyclerView.animate().translationX(paddingL).setDuration(0).start();
            }
        }
        checkListPriceFomat();

        BuyVipAdapter buyVipAdapter = new BuyVipAdapter(getContext(), mVipCombos, isVip);
        mRecyclerView.setAdapter(buyVipAdapter);
        mHandler.sendEmptyMessageDelayed(MSG_VIEW_FOCUSED, 200);

        for (VipCombo combo : lists) {
            if (VipCombo.PAY_MODE_CONTINUOUS_MONTHLY.equals(combo.getPayMode())) {
                UserPublic.vipMonthId = combo.getId();
                break;
            }
        }
    }

    @Override
    public void showUserInfo(@Nullable UserInfo userInfo) {
        mIsUserVip = userInfo != null && userInfo.isVIP();
        mTitleTv.setText(mIsUserVip ?
                R.string.vip_title_t_continue : R.string.vip_title_t_open);
    }

    private void checkListPriceFomat() {
        for (int i = 0; i < mVipCombos.size(); i++) {
            String price = mVipCombos.get(i).getPrice();
            String vipPrice = mVipCombos.get(i).getVipPrice();
            String curPrice = mVipCombos.get(i).getCurPrice();

            mVipCombos.get(i).setPrice(getPriceFomatEnding(price));
            mVipCombos.get(i).setVipPrice(getPriceFomatEnding(vipPrice));
            mVipCombos.get(i).setCurPrice(getPriceFomatEnding(curPrice));
        }
    }

    private String getPriceFomatEnding(String price) {
        if (StringUtils.isNullOrEmpty(price)) {
            return "0";
        }
        try {
            double value = Double.valueOf(price);
            DecimalFormat df = new DecimalFormat("#0.00");
            String strprice = df.format(value);
            if (strprice.endsWith(".00")) {
                strprice = strprice.replace(".00", "");
            }
            return strprice;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return "0";
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_VIEW_FOCUSED:
                    mRecyclerView.requestFocus();
                    break;
                default:
                    break;
            }
        }
    };

    private void initListView() {
        mRecyclerView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                //itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
//                if (mListAdapter != null) {
//                    mListAdapter.setItemBackground(false, position);
//                }
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
//                if (mListAdapter != null) {
//                    mListAdapter.setItemBackground(true, position);
//                }
            }

            @Override
            public void onReviseFocusFollow(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                gotoPurchase(mVipCombos.get(position));
            }
        });

        float spacing = getResources().getDimension(R.dimen.vip_item_margin_left);
        mRecyclerView.setSpacingWithMargins((int) spacing, 0);
    }

    private void gotoPurchase(VipCombo vipCombo) {
        if (null == vipCombo) {
            return;
        }

        String payPrice = BuyVipContract.getVipPayPrice(vipCombo, mIsUserVip);
        BigDecimal priceDecimal = BigDecimal.ZERO;
        try {
            priceDecimal = new BigDecimal(payPrice);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // price not free && credit pay expired
        if (priceDecimal.compareTo(BigDecimal.ZERO) > 0) {
            getPresenter().purchaseVip(vipCombo);
        } else {
            showPurchaseVipDialog(vipCombo);
        }

        StatisticsHelper.getInstance(getContext())
                .reportClickUserCenterVip(payPrice, mHaveMoney, "3");
    }

    @Override
    public void setWalletInfo(Wallet wallet) {
        if (wallet != null) {
            mHaveMoney = wallet.getValue();
        }
    }

    @Override
    public void setCreditInfo(Wallet credit) {
        if (null == credit) {
            return;
        }
        String maxCreditMoney = credit.getCreditLine();
        String deadLineDays = credit.getCreditDeadLineDays();
        String remain = credit.getCreditRemain();
        if (!StringUtils.isNullOrEmpty(maxCreditMoney)) {
            UserInfoHelper.setMaxCredit(getContext(), maxCreditMoney);
        }

        if (deadLineDays != null && remain != null) {
            //保存信用天数
            int days = 0;
            try {
                days = Integer.parseInt(deadLineDays);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (days > 0) {
                UserInfoHelper.setLineDayCredit(getContext(), deadLineDays);
            } else {
                UserInfoHelper.setLineDayCredit(getContext(), "0");
            }
        }
    }

    @Override
    public void showCreditPayExpired(int expireDate, double creditBill, double creditMaxLimit) {
        String fragTag = FRAG_TAG_CREDIT_EXPIRED;
        removePreviousFragment(getFragmentManager(), fragTag);
        CreditExpireDialogFragment fragment = CreditExpireDialogFragment.newInstance(
                expireDate, creditBill, creditMaxLimit);
        fragment.show(getFragmentManager(), fragTag);
    }

    @Override
    public void showPurchaseVip(VipCombo vipCombo) {
        showPurchaseVipDialog(vipCombo);
    }

    @Override
    public void showPurchaseVipError(String errMsg) {
        String text = getString(R.string.vip_pay_package_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            text += errMsg;
        }
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showPurchaseVipMonthlyRepeat() {
        ToastUtils.showToast(getContext(), getString(R.string.vip_monthly_repeat));
    }

    private void showPurchaseVipDialog(final VipCombo vipCombo) {
        String name = vipCombo.getName();
        String payPrice = BuyVipContract.getVipPayPrice(vipCombo, mIsUserVip);
        DialogFragment fragment;
        String payMode = vipCombo.getPayMode();
        // 连续包月
        if (!StringUtils.isNullOrEmpty(payMode) && VipCombo.PAY_MODE_CONTINUOUS_MONTHLY.equals(
                payMode)) {
            fragment = QrCodePayFragment.newInstance(vipCombo.getId(), name,
                    Order.PRODUCT_TYPE_VIP_MONTHLY, Double.parseDouble(payPrice), 0, false, 0,
                    false, 0, new QrCodePayFragment.OnQrCodePayResultListener() {
                        @Override
                        public void onCancel() {
                        }

                        @Override
                        public void onPurchaseResult(boolean success, String log) {
                            Logger.d("onPurchaseResult, success : " + success + ", log : "
                                    + log);
                            handlePurchaseResult(vipCombo, success, null, null, log);
                        }
                    });
        } else {
            PurchaseDialogFragment purchaseFragment = PurchaseDialogFragment.newInstance(
                    vipCombo.getId(), Order.PRODUCT_TYPE_VIP, name, "", payPrice, false, "", false,
                    1);
            purchaseFragment.setListener(new PurchaseDialogFragment.OnPurchaseResultListener() {

                @Override
                public void onCancel() {
                }

                @Override
                public void onPurchaseResult(boolean success, Order order,
                        FinanceOrder financeOrder, String errMsg) {
                    Logger.d("onPurchaseResult, success : " + success + ", order : " + order
                            + ", finance order : " + financeOrder);
                    handlePurchaseResult(vipCombo, success, order, financeOrder, errMsg);
                }
            });
            fragment = purchaseFragment;
        }

        String fragTag = PURCHASE_FRAG_TAG;
        removePreviousFragment(getFragmentManager(), fragTag);
        fragment.show(getFragmentManager(), fragTag);
    }

    private void handlePurchaseResult(VipCombo vipCombo, boolean success, Order order,
            FinanceOrder financeOrder, String errMsg) {
        if (success) {
            // re-load vip packages
            getPresenter().loadVipPackages(true);
            // show success dialog
            setSuccessDialog(vipCombo, order, financeOrder);
        } else {
            setFailedDialog(errMsg);
        }
    }

    private void setFailedDialog() {
        final Dialog aDialog = TipsDialog(getContext(), R.layout.user_topup_successfull_tips);

        TextView title = (TextView) aDialog
                .findViewById(R.id.tv_user_pay_dlg_detail);
        title.setText(R.string.buy_vip_failed);
        Button okButton = (Button) aDialog.findViewById(R.id.user_dialog_pay_bt_yes);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.dismiss();
            }
        });
        aDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                getActivity().finish();
            }
        });
        aDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getPresenter() != null) {
            getPresenter().getTheUserWallet();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String time = String.valueOf((System.currentTimeMillis() - mEnterTime) / 1000);
        StatisticsHelper.getInstance(getActivity()).reportExitActivity(VIEW_CODE_REGISTER_VIP,
                "开通会员", "", time);
        mHandler.removeCallbacksAndMessages(null);
    }

    private void setPriceTv(TextView Tv, String balance) {
        Tv.setText(balance + getString(R.string.RMB));
    }

    private void setSuccessDialog(final VipCombo vipCombo, @Nullable final Order order,
            @Nullable final FinanceOrder financeOrder) {
        String payMode = vipCombo.getPayMode();
        Logger.d("setSuccessDialog, payMode : " + payMode);
        // 连续包月
        boolean isVipMonthly = !StringUtils.isNullOrEmpty(payMode)
                && VipCombo.PAY_MODE_CONTINUOUS_MONTHLY.equals(payMode);

        final Dialog aDialog = TipsDialog(getActivity(), R.layout.user_topup_successfull_tips);
        TextView title = (TextView) aDialog
                .findViewById(R.id.tv_user_pay_dlg_title);
        TextView detail = (TextView) aDialog
                .findViewById(R.id.tv_user_pay_dlg_detail);
        String titleText, detailText, orderDate = "";
        titleText = getActivity().getString(
                isVipMonthly ? R.string.vip_pay_package_vip_monthly_sucess
                        : R.string.vip_pay_package_sucess);
        if (order != null && order.getExpirationTime() != null) {
            orderDate = order.getExpirationTime().substring(0, 10);
        }
        detailText = String.format(getActivity().getString(
                isVipMonthly ? R.string.vip_pay_package_vip_monthly_detail_sucess
                        : R.string.vip_pay_vip_validay), orderDate);
        title.setText(titleText);
        detail.setText(detailText);
        Button okButton = (Button) aDialog.findViewById(R.id.user_dialog_pay_bt_yes);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.dismiss();
            }
        });
        aDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (financeOrder != null && financeOrder.getPretreatmentDays() != null) {
                    setFinanceDialog(vipCombo, financeOrder);
                } else {
                    setResultCode(true, "");
                }
            }
        });
        aDialog.show();
    }

    private void setFailedDialog(final String errorCode) {
        final Dialog aDialog = TipsDialog(getActivity(), R.layout.user_topup_successfull_tips);
        TextView title = (TextView) aDialog
                .findViewById(R.id.tv_user_pay_dlg_title);
        TextView detail = (TextView) aDialog
                .findViewById(R.id.tv_user_pay_dlg_detail);
        String titleText, detailText = "";
        titleText = getActivity().getString(R.string.vip_pay_package_failed);
        detailText = String.format(getString(R.string.active_vip_failed_reason), errorCode);
        title.setText(titleText);
        detail.setText(detailText);
        Button okButton = (Button) aDialog.findViewById(R.id.user_dialog_pay_bt_yes);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.dismiss();
            }
        });
        aDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                setResultCode(false, errorCode);
            }
        });
        aDialog.show();
    }

    private void setResultCode(final Boolean isSuccess, final String errorCode) {
        int resultInt = isSuccess ? 1 : 0;
        String resultStr = isSuccess ? getString(R.string.purchase_success) : errorCode;
        Logger.d("setResultCode isSuccess=" + isSuccess + ",resultInt=" + resultInt);
        Intent intent = new Intent();
        intent.putExtra("result_log", resultStr);//将计算的值回传回去
        getActivity().setResult(resultInt, intent);
        getActivity().finish();
    }

    private void setFinanceDialog(final VipCombo vipCombo, final FinanceOrder financeOrder) {
        final Dialog aDialog = TipsDialog(getActivity(), R.layout.user_sucess_finance_dialog);
        TextView title = (TextView) aDialog
                .findViewById(R.id.tv_user_finance_dlg_title);
        TextView detail = (TextView) aDialog
                .findViewById(R.id.tv_user_finance_dlg_detail);
        String titleText, detailText, orderDate = "";

        titleText = getActivity().getString(R.string.vip_buy_sent_finance);
        orderDate = financeOrder.getPretreatmentDays();

        detailText = String.format(getActivity().getString(R.string.vip_buy_sent_finance_detail),
                vipCombo.getName(), orderDate);
                /*"你购买了全球播 \""+payName+"\" 商品获得收益赠送。\n" +
                "全球播赠送给您定期理财产品，到期后您将获得可观收益。\n" +
                "理财产品在"+orderDate+"个工作日完成购买，请耐心等待。";*/

        title.setText(titleText);
        detail.setText(detailText);
        final Button okButton = (Button) aDialog.findViewById(R.id.user_finance_dialog_bt_yes);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.dismiss();
            }
        });
        aDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mCountTimer != null) {
                    mCountTimer.cancel();
                }
                setResultCode(true, "");
            }
        });

        /*
         * 最简单的倒计时类，实现了官方的CountDownTimer类（没有特殊要求的话可以使用）
         * 即使退出activity，倒计时还能进行，因为是创建了后台的线程。
         * 有onTick，onFinsh、cancel和start方法
         */
        int time = 20;
        if (mCountTimer != null) {
            mCountTimer.cancel();
        }
        mCountTimer = new CountDownTimer(time * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //每隔countDownInterval秒会回调一次onTick()方法
                okButton.setText(getString(R.string.ok) + " " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                UIHelper.dismissDialog(aDialog);
            }
        };
        mCountTimer.start();// 开始计时
        aDialog.show();
    }
}