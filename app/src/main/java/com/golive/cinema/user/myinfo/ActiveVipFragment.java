package com.golive.cinema.user.myinfo;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_ACTIVE_VIP;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MY_ACCOUNT;
import static com.golive.cinema.user.usercenter.UserPublic.TipsDialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.golive.cinema.Injection;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.UIHelper;
import com.golive.network.helper.UserInfoHelper;
import com.golive.pay.PayManager;
import com.golive.pay.aidl.CallBack;
import com.golive.pay.config.SysConstant;
import com.initialjie.log.Logger;

import java.util.Map;


/**
 * Created by Mowl on 2016/11/9.
 */

public class ActiveVipFragment extends MvpFragment implements MyInfoContract.ActiveVipView {
    //    private ProgressDialog mProgressDialog;
    private MyInfoContract.ActiveVipPresenter mPresenter;
    private Button mStartBtn;
    private EditText mEditTv;
    private TextView mTipsTv, mBottomTv;
    private ProgressDialog mProgressDialog;
    private long mEnterTime;
    private String mUserId;
    private String mExchangeCode;
    private PayManager mPayManager;

    public static ActiveVipFragment newInstance() {
        return new ActiveVipFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.user_myinfo_vip_activate, container, false);
        mEditTv = (EditText) root.findViewById(R.id.user_info_vip_start_input_ed);
        mStartBtn = (Button) root.findViewById(R.id.user_info_vip_start_btn);
        mTipsTv = (TextView) root.findViewById(R.id.user_info_vip_start_tips);
        mBottomTv = (TextView) root.findViewById(R.id.user_active_bottom_phonetv);
        mTipsTv.setVisibility(View.INVISIBLE);
        mEditTv.addTextChangedListener(mInputWatcher);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExchangeCode = mEditTv.getText().toString();
                if (getPresenter() != null) {
                    getPresenter().getloadPayUrl();
                }
            }
        });
        setButtonState(false);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("onActivityCreated");
        FragmentActivity activity = getActivity();
        mPayManager = new PayManager(activity);
        mUserId = UserInfoHelper.getUserId(activity);
        Context context = getContext().getApplicationContext();
        mPresenter = new ActiveVipPresenter(this,
                Injection.provideGetPayUrlListUseCase(context),
                Injection.provideGetClientServiceUseCase(context));

        if (getPresenter() != null) {
            getPresenter().start();
        }
        mEnterTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(getContext()).reportEnterActivity(VIEW_CODE_ACTIVE_VIP, "会员激活",
                VIEW_CODE_MY_ACCOUNT);
    }

    private void setButtonState(boolean isCanClick) {
        if (!isCanClick) {
            mStartBtn.setEnabled(false);
            mStartBtn.setFocusable(false);
            mStartBtn.setClickable(false);
            mStartBtn.setTextColor(
                    ContextCompat.getColor(getContext(), R.color.info_active_btn_hui));
        } else {
            mStartBtn.setEnabled(true);
            mStartBtn.setFocusable(true);
            mStartBtn.setClickable(true);
            mStartBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.item_white_color));
        }
    }

    @Override
    public void setPresenter(MyInfoContract.ActiveVipPresenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    @Override
    protected MyInfoContract.ActiveVipPresenter getPresenter() {
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
    public void checkUrlMap(Map<String, String> urlMap) {
        if (null == urlMap) {
            return;
        }
        Logger.d("checkUrlMap");
        String payParameter = "accountID=" + mUserId + "&productName=null" + "&productPrice=null";
        PayManager payManager = mPayManager;
//        mPayManager.setServicephone(PubData.getInstance().getMainConfig()
//                .getServicephone());
        payManager.setAppType("3");
        payManager.initPayUrl(urlMap);
        payManager.payKaPay(payParameter,
                new CallBack() {
                    @Override
                    public void callback(int state, String log) {
                        finishCallBack(state, log);
                    }
                }, mExchangeCode);
    }

    private final TextWatcher mInputWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count > 0) {
                if (!mStartBtn.isEnabled()) {
                    setButtonState(true);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable arg0) {
        }
    };

    @Override
    public void setServicePhoneInfo(String phone) {
        if (phone != null) {
            if (phone.contains("(")) {
                phone = phone.replace("(", " ( ");
            }
            if (phone.contains(")")) {
                phone = phone.replace(")", " ) ");
            }
            mBottomTv.setText(String.format(getString(R.string.custom_phone), phone));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPayManager != null) {
            mPayManager.destory();
        }
        String time = String.valueOf((System.currentTimeMillis() - mEnterTime) / 1000);
        StatisticsHelper.getInstance(getActivity()).reportExitActivity(VIEW_CODE_ACTIVE_VIP, "会员激活",
                "", time);
    }

    private String getLogDetail(String resultlog, int index) {
//        resultlog =";;name:"+"data.vipProduct.name"
//                +";;remain:"+"data.attach.vipRemain"
//                +";;starttime:"+"data.vipOperation.startTime"
//                +";;endtime:"+"data.vipOperation.endTime";
        String v1 = ";;name:";
        String v2 = ";;remain:";
        String v3 = ";;starttime:";
        String v4 = ";;endtime:";

        switch (index) {
            case 1:
                if (resultlog.contains(v1) && resultlog.contains(v2)) {
                    int start = v1.length();
                    int end = resultlog.indexOf(v2);
                    String name = resultlog.substring(start, end);
                    if (name != null) {
                        return name;
                    }
                }
                return getString(R.string.active_vip_one_month);
            case 2:
                if (resultlog.contains(v2) && resultlog.contains(v3)) {
                    int start = resultlog.indexOf(v2) + v2.length();
                    int end = resultlog.indexOf(v3);
                    String name = resultlog.substring(start, end);
                    if (name != null) {
                        return name;
                    }
                }
            case 3:
                if (resultlog.contains(v3) && resultlog.contains(v4)) {
                    int start = resultlog.indexOf(v3) + v3.length();
                    int end = resultlog.indexOf(v4);
                    String name = resultlog.substring(start, end);
                    if (name != null) {
                        return name;
                    }
                }
            case 4:
                if (resultlog.contains(v4)) {
                    int start = resultlog.indexOf(v4) + v4.length();
                    String name = resultlog.substring(start);
                    if (name != null) {
                        return name;
                    }
                }
            default:
                break;
        }
        return "";
    }

    private void setDetailDialog(final Boolean isSuccess, final String log) {
        final Dialog aDialog = TipsDialog(getContext(), R.layout.user_topup_successfull_tips);
        TextView title = (TextView) aDialog
                .findViewById(R.id.tv_user_pay_dlg_title);
        TextView detail = (TextView) aDialog
                .findViewById(R.id.tv_user_pay_dlg_detail);
        String titleText, detailText;
        String name = getLogDetail(log, 1);
        String orderDate = getLogDetail(log, 4);
        if (isSuccess) {
            titleText = getString(R.string.active_vip_success);
            detailText = String.format(getString(R.string.active_vip_success_content), name,
                    orderDate);
        } else {//fail
            titleText = getString(R.string.active_vip_failed);
            detailText = String.format(getString(R.string.active_vip_failed_reason), log);
        }
        title.setText(titleText);
        detail.setText(detailText);

        Button okButton = (Button) aDialog.findViewById(R.id.user_dialog_pay_bt_yes);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.dismiss();
                Logger.d("onClick isSuccess=" + isSuccess);
            }
        });
        aDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Logger.d("onCancel isSuccess=" + isSuccess);
            }
        });
        aDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Logger.d("onDismiss isSuccess=" + isSuccess);
            }
        });
        aDialog.show();
    }

    private void finishCallBack(int state, String log) {
        Logger.d("showQrcode callback state=" + state + ",log" + log);
//        if (payCallBack !=null){
//            payCallBack.PayFinishExecute(state,log);
//        }
        if (state == SysConstant.KA_PAY_SUCCESS) {
            setDetailDialog(true, log);
            mTipsTv.setVisibility(View.GONE);
        } else {
//          setDetailDialog(false,log);//debug
            int inputTimes = Integer.parseInt(log);
            if (inputTimes < 0) {
                inputTimes = 0;
            }
            String tips = String.format(getString(R.string.active_vip_failed_tips), inputTimes);
            mTipsTv.setText(Html.fromHtml(tips));
            mTipsTv.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), R.string.active_vip_failed, Toast.LENGTH_LONG).show();
        }
    }
}