package com.golive.cinema.init.dialog;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.init.RepeatMacContract;
import com.golive.cinema.init.RepeatMacPresenter;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.ToastUtils;
import com.golive.cinema.util.UIHelper;
import com.initialjie.log.Logger;

/**
 * Created by chgang on 2016/11/5.
 */

public class RepeatMacDialog extends BaseDialog implements View.OnClickListener,
        RepeatMacContract.View {

    public static final String FRAGMENT_TAG = "REPEAT_MAC_FRAGMENT";
    public static final String PHONE_MAC_STATUS = "phone_mac_status";

    private static final int MAX_PHONE_LENGTH = 11;

    private static final String REPEAT_MAC_STATUS_0 = "0";//无需弹窗手机注册界面
    private static final String REPEAT_MAC_STATUS_1 = "1";//需要弹窗手机注册界面
    private static final String REPEAT_MAC_STATUS_2 = "2";//无需弹窗手机登录界面（手机版）
    private static final String REPEAT_MAC_STATUS_3 = "3";//无需弹窗手机登录界面（MAC重复）
    private static final String REPEAT_MAC_STATUS_4 = "4";//无需弹窗手机登录界面（MAC重复）

    private RepeatMacContract.Presenter mPresenter;
    private RelativeLayout mContainerPhoneLoginStep1;
    private RelativeLayout mContainerPhoneLoginStep2;
    private RelativeLayout mContainerRepeatMacStep1;
    private RelativeLayout mContainerRepeatMacStep2;
    private RelativeLayout mContainerRepeatMacStep3;

    private TextView mRepeatCommonTextTips;
    private Button mRepeatCommonJumpBtn;
    private ImageView mImageMessageIv;
    private int mClickId;
    private String mStatus;

    /*********** 手机登录 ***********/
    private EditText mRepeatPhoneLoginBtEditText;
    private Button mRepeatPhoneLoginSureBtn;
    private ProgressBar mRepeatPhoneProgressbar;
    private TextView mRepeatPhoneProgressbarTv;
    private TextView mRepeatPhoneLoginErrorDetailTipsTv;
    private InputMethodManager mInputMethodManager;
    private Button mRepeatPhoneCommitBtn;
    private Button mRepeatPhoneCancelBtn;

    /************ MAC重复 ***************/
    //step1
    private EditText mPhoneMacLoginBtEditText;
    private Button mPhoneMacLoginSureBtn;
    private TextView mPhoneMacLoginErrorDetailTv;

    //step2
    private TextView mPhoneMacLoginStep2Tv;
    private EditText mPhoneMacLoginBtEditStep2Tv;
    private Button mPhoneMacLoginCheckCodeBtn;
    private TextView mPhoneMacLoginCheckCodeErrorTv;
    private Button mPhoneMacLoginCheckCommitBtn;
    private Button mPhoneMacLoginLastStepBtn;

    //step3
    private TextView mPhoneMacLoginStep3Tv;
    private Button mPhoneMacLoginStep3Btn;

    private HiddenListener mOnHiddenListener;
    private ProgressDialog mLoadingDialog;

    @Override
    public void setPresenter(RepeatMacContract.Presenter presenter) {
        this.mPresenter = presenter;
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
            if (null == mLoadingDialog) {
                mLoadingDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.please_wait));
            }

            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.show();
            }
        } else {
            if (mLoadingDialog != null) {
                UIHelper.dismissDialog(mLoadingDialog);
            }
        }
    }

    @Override
    public void showContactInfoView(String phone, String qq) {
        if (phone != null && qq != null) {
            mRepeatCommonTextTips.setText(
                    String.format(getString(R.string.phone_number_string_detail16),
                            phone, qq));
//                            clientService.getServicePhone5(), clientService.getQQ()));
        } else {
            mRepeatCommonTextTips.setText(R.string.purchase_help_info);
        }
    }

    @Override
    public void showLoginByPhoneSuccessView(boolean loginSuccess, String status) {
//        Logger.d(repeatMac.toString());
        if (mRepeatPhoneLoginSureBtn != null) {
            mRepeatPhoneLoginSureBtn.setClickable(true);
        }
        if (mPhoneMacLoginSureBtn != null) {
            mPhoneMacLoginSureBtn.setClickable(true);
        }

//        if (repeatMac != null && !repeatMac.isOk()) {
        if (!loginSuccess) {
            mRepeatPhoneLoginErrorDetailTipsTv.setText(
                    getString(R.string.phone_number_string_detail5));
        }

//        if (repeatMac != null && repeatMac.getStatus() != null)
        {
//            String status = repeatMac.getStatus();
            Logger.d("repeatMac, status : " + status);
            switch (mClickId) {
                case R.id.repeat_phone_login_bt_sure:
                    if (REPEAT_MAC_STATUS_0.equals(status)) {
                        mImageMessageIv.setImageResource(R.drawable.icon_true_bg);
                        mContainerPhoneLoginStep1.setVisibility(View.GONE);
                        mContainerPhoneLoginStep2.setVisibility(View.GONE);
                        mContainerRepeatMacStep3.setVisibility(View.VISIBLE);
                        mPhoneMacLoginStep3Tv.setText(Html.fromHtml(
                                String.format(getString(R.string.phone_number_string_detail13),
                                        mRepeatPhoneLoginBtEditText.getText().toString())));
                        mPhoneMacLoginStep3Btn.requestFocus();
                        showBring2LoginView();
                    }
                    break;
                case R.id.phone_mac_login_bt_sure:
                    if (REPEAT_MAC_STATUS_1.equals(status)) {
                        showVerifyPhoneView();
                    } else {
                        if (mContainerRepeatMacStep3.getVisibility() == View.GONE) {
                            mImageMessageIv.setImageResource(R.drawable.icon_true_bg);
                            mContainerRepeatMacStep1.setVisibility(View.GONE);
                            mContainerRepeatMacStep2.setVisibility(View.GONE);
                            mContainerRepeatMacStep3.setVisibility(View.VISIBLE);
                            mPhoneMacLoginStep3Tv.setText(
                                    getString(R.string.phone_number_string_detail22));
                            mPhoneMacLoginStep3Btn.requestFocus();
                            showBring2LoginView();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void showBring2LoginView() {
        Logger.d("bring to login...");
        mHandler.post(mShowBring2LoginRunnable);
    }

    private void showVerifyPhoneView() {
        Logger.d("showVerifyPhoneView");
        if (mContainerRepeatMacStep2.getVisibility() == View.GONE) {
            mContainerRepeatMacStep1.setVisibility(View.GONE);
            mContainerRepeatMacStep2.setVisibility(View.VISIBLE);
            mContainerRepeatMacStep3.setVisibility(View.GONE);
            mPhoneMacLoginCheckCodeBtn.requestFocus();
            String text = String.format(getString(R.string.phone_number_string_detail7),
                    mPhoneMacLoginBtEditText.getText());
            mPhoneMacLoginStep2Tv.setText(text);
        }
    }

    @Override
    public void showLoginByPhoneFailedView(String msg) {
        Logger.e("showLoginByPhoneFailedView, msg : " + msg);
        String text = getString(R.string.login_failed);
        if (!StringUtils.isNullOrEmpty(msg)) {
            text += msg;
        }
        ToastUtils.showToast(getContext(), text);

        if (mContainerRepeatMacStep1 != null
                && mContainerRepeatMacStep1.getVisibility() == View.VISIBLE) {
            mPhoneMacLoginErrorDetailTv.setText(
                    getString(R.string.phone_number_string_detail21));
            mPhoneMacLoginErrorDetailTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showGetVerifyCodeSuccessView() {
        resetGetVerifyCodeView();
        if (mPhoneMacLoginCheckCodeErrorTv != null) {
            mPhoneMacLoginCheckCodeErrorTv.setText(
                    getString(R.string.phone_number_string_detail12));
            mPhoneMacLoginCheckCodeErrorTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showGetVerifyCodeFailedView(String msg) {
        Logger.e("showGetVerifyCodeFailedView, msg : " + msg);
        String text = getString(R.string.get_verify_code_failed);
        if (!StringUtils.isNullOrEmpty(msg)) {
            text += msg;
        }
        ToastUtils.showToast(getContext(), text);

        resetGetVerifyCodeView();
        if (mPhoneMacLoginCheckCodeErrorTv != null) {
            mPhoneMacLoginCheckCodeErrorTv.setText(getString(R.string.e_400026));
            mPhoneMacLoginCheckCodeErrorTv.setVisibility(View.VISIBLE);
        }
    }

    private void resetGetVerifyCodeView() {
        // reset get verify code
        mHandler.removeCallbacks(mRepeatRunnable);
        if (mPhoneMacLoginCheckCodeBtn != null) {
            mPhoneMacLoginCheckCodeBtn.setClickable(true);
            mPhoneMacLoginCheckCodeBtn.setText(R.string.phone_number_string_detail25);
        }
    }

    @Override
    public void showVerifySuccessView() {
        //注册登录成功
        if (mContainerRepeatMacStep3.getVisibility() == View.GONE) {
            mImageMessageIv.setImageResource(R.drawable.icon_true_bg);
            mContainerRepeatMacStep1.setVisibility(View.GONE);
            mContainerRepeatMacStep2.setVisibility(View.GONE);
            mContainerRepeatMacStep3.setVisibility(View.VISIBLE);
            mPhoneMacLoginStep3Btn.requestFocus();
            showBring2LoginView();
        }
    }

    @Override
    public void showVerifyFailedView(String msg) {
        Logger.e("showLoginAgainFailedView, msg : " + msg);
        String text = getString(R.string.verify_phone_failed);
        if (!StringUtils.isNullOrEmpty(msg)) {
            text += msg;
        }
        ToastUtils.showToast(getContext(), text);
    }

    public interface HiddenListener {
        void onCompleted();

        void onExit();
    }

    public void setOnHiddenListener(HiddenListener onHiddenListener) {
        this.mOnHiddenListener = onHiddenListener;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCancelable(true);

        if (getDialog() != null) {
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                        KeyEvent event) {
                    if (KeyEvent.ACTION_DOWN == event.getAction()
                            && 0 == event.getRepeatCount()
                            && (keyCode == KeyEvent.KEYCODE_BACK
                            || keyCode == KeyEvent.KEYCODE_ESCAPE)) {
                        if (mOnHiddenListener != null) {
                            mOnHiddenListener.onExit();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }

        RepeatMacPresenter repeatMacPresenter = new RepeatMacPresenter(this,
                Injection.provideRepeatMacUseCase(getActivity()),
                Injection.provideVerifyCodeUseCase(getActivity()),
                Injection.provideLoginAgainUseCase(getActivity()),
                Injection.provideGetClientServiceUseCase(getActivity()),
                Injection.provideSchedulerProvider());

        if (mPresenter != null) {
            mPresenter.getContactInformation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.unsubscribe();
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatus = getArguments().getString(PHONE_MAC_STATUS);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundResource(R.drawable.homg_bg);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        Context applicationContext = getContext().getApplicationContext();
        mInputMethodManager = (InputMethodManager) applicationContext.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.repeat_mac_operate_frag,
                container, false);
        mImageMessageIv = (ImageView) view.findViewById(R.id.image_message_iv);
        mRepeatCommonTextTips = (TextView) view.findViewById(R.id.repeat_common_text_tips);

        if (StringUtils.isNullOrEmpty(mStatus)) {
            //本地mac异常
            view.findViewById(R.id.image_message_header_logo).setVisibility(View.GONE);
            mImageMessageIv.setImageResource(R.drawable.icon_plaint_bg);
            View childView = inflater.inflate(R.layout.common_alert_mac_error, container, false);
            childView.findViewById(R.id.mac_error_alert_btn).setOnClickListener(this);
            TextView errorText = (TextView) childView.findViewById(R.id.mac_error_alert_text);
            errorText.setText(String.format(getString(R.string.e_400035), "0X0001"));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            view.addView(childView, params);
            return view;
        }

        Logger.d("status:" + mStatus);
        if (!StringUtils.isNullOrEmpty(mStatus)) {
            if (REPEAT_MAC_STATUS_2.equals(mStatus)) {//手机登录（手机版）
                mContainerPhoneLoginStep1 = (RelativeLayout) view.findViewById(
                        R.id.repeat_phone_login_step1);
                mContainerPhoneLoginStep1.setVisibility(View.VISIBLE);

                mContainerPhoneLoginStep2 = (RelativeLayout) view.findViewById(
                        R.id.repeat_phone_login_step2);
                mRepeatPhoneCommitBtn = (Button) view.findViewById(R.id.repeat_phone_commit_btn);
                mRepeatPhoneCancelBtn = (Button) view.findViewById(R.id.repeat_phone_cancel_btn);
                mRepeatPhoneCommitBtn.setOnClickListener(this);
                mRepeatPhoneCancelBtn.setOnClickListener(this);

                mRepeatCommonJumpBtn = (Button) view.findViewById(R.id.repeat_common_jump_btn);
                mRepeatCommonJumpBtn.setOnClickListener(this);
                mRepeatPhoneLoginBtEditText = (EditText) view.findViewById(
                        R.id.repeat_phone_login_bt_editText);
                mRepeatPhoneLoginSureBtn = (Button) view.findViewById(
                        R.id.repeat_phone_login_bt_sure);
                mRepeatPhoneProgressbar = (ProgressBar) view.findViewById(
                        R.id.repeat_phone_progressbar);
                mRepeatPhoneProgressbarTv = (TextView) view.findViewById(
                        R.id.repeat_phone_progressbar_text);
                mRepeatPhoneLoginErrorDetailTipsTv = (TextView) view.findViewById(
                        R.id.repeat_phone_login_error_detail_tips);
                mRepeatPhoneLoginBtEditText.addTextChangedListener(repeatPhoneLoginListener);
                mRepeatPhoneLoginSureBtn.setOnClickListener(this);
                mRepeatPhoneLoginSureBtn.setClickable(false);
                mRepeatPhoneLoginSureBtn.setFocusable(false);
                mRepeatPhoneLoginSureBtn.setFocusableInTouchMode(false);
                mRepeatPhoneLoginBtEditText.requestFocus();
            } else if (REPEAT_MAC_STATUS_1.equals(mStatus)
                    || REPEAT_MAC_STATUS_3.equals(mStatus)) { //手机登录(mac重复)
                mContainerRepeatMacStep1 =
                        (RelativeLayout) view.findViewById(R.id.repeat_mac_operate_step1);
                mContainerRepeatMacStep2 =
                        (RelativeLayout) view.findViewById(R.id.repeat_mac_operate_step2);
                mContainerRepeatMacStep1.setVisibility(View.VISIBLE);

                //step1
                mPhoneMacLoginBtEditText =
                        (EditText) view.findViewById(R.id.phone_mac_login_bt_edit_text);
                mPhoneMacLoginBtEditText.addTextChangedListener(phoneMacLoginListener);
                mPhoneMacLoginBtEditText.requestFocus();
                mPhoneMacLoginSureBtn =
                        (Button) view.findViewById(R.id.phone_mac_login_bt_sure);
                mPhoneMacLoginSureBtn.setOnClickListener(this);
                mPhoneMacLoginSureBtn.setClickable(false);
                mPhoneMacLoginSureBtn.setFocusable(false);
                mPhoneMacLoginSureBtn.setFocusableInTouchMode(false);
                mPhoneMacLoginErrorDetailTv =
                        (TextView) view.findViewById(R.id.phone_mac_login_error_detail_text_view);

                //step2
                mPhoneMacLoginStep2Tv =
                        (TextView) view.findViewById(R.id.phone_mac_login_tv_step2);
                mPhoneMacLoginBtEditStep2Tv =
                        (EditText) view.findViewById(R.id.phone_mac_login_bt_edit_text_step2);
                mPhoneMacLoginCheckCodeBtn =
                        (Button) view.findViewById(R.id.phone_mac_login_check_code_btn);
                mPhoneMacLoginCheckCodeErrorTv =
                        (TextView) view.findViewById(R.id.phone_mac_login_check_code_error_tv);
                mPhoneMacLoginCheckCommitBtn =
                        (Button) view.findViewById(R.id.phone_mac_login_check_commit_btn);
                mPhoneMacLoginLastStepBtn =
                        (Button) view.findViewById(R.id.phone_mac_login_last_step_btn);
                mPhoneMacLoginBtEditStep2Tv.addTextChangedListener(nextPhoneMacLoginListener);
                mPhoneMacLoginCheckCodeBtn.setOnClickListener(this);
                mPhoneMacLoginCheckCommitBtn.setOnClickListener(this);
                mPhoneMacLoginLastStepBtn.setOnClickListener(this);
                mPhoneMacLoginCheckCommitBtn.setClickable(false);
                mPhoneMacLoginCheckCommitBtn.setFocusable(false);
                mPhoneMacLoginCheckCommitBtn.setFocusableInTouchMode(false);
            }
        }

        //step3
        mContainerRepeatMacStep3 =
                (RelativeLayout) view.findViewById(R.id.repeat_mac_operate_step3);
        mPhoneMacLoginStep3Tv = (TextView) view.findViewById(R.id.phone_mac_login_tv_step3);
        mPhoneMacLoginStep3Btn = (Button) view.findViewById(R.id.phone_mac_login_btn_step3);
        mPhoneMacLoginStep3Btn.setOnClickListener(this);
        return view;
    }

    private final TextWatcher nextPhoneMacLoginListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mPhoneMacLoginBtEditStep2Tv.getText().length() == 4) {
                mPhoneMacLoginCheckCommitBtn.setClickable(true);
                mPhoneMacLoginCheckCommitBtn.setFocusable(true);
                mPhoneMacLoginCheckCommitBtn.setFocusableInTouchMode(true);
                mPhoneMacLoginCheckCommitBtn.requestFocus();
                if (getActivity().getWindow().getAttributes().softInputMode
                        == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
                    mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            } else {
                mPhoneMacLoginCheckCommitBtn.setClickable(false);
                mPhoneMacLoginCheckCommitBtn.setFocusable(false);
                mPhoneMacLoginCheckCommitBtn.setFocusableInTouchMode(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private final TextWatcher phoneMacLoginListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mPhoneMacLoginBtEditText.getText().length() == 11) {
                mPhoneMacLoginSureBtn.setClickable(true);
                mPhoneMacLoginSureBtn.setFocusable(true);
                mPhoneMacLoginSureBtn.setFocusableInTouchMode(true);
                mPhoneMacLoginSureBtn.requestFocus();
                if (getActivity().getWindow().getAttributes().softInputMode
                        == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
                    mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            } else {
                mPhoneMacLoginSureBtn.setClickable(false);
                mPhoneMacLoginSureBtn.setFocusable(false);
                mPhoneMacLoginSureBtn.setFocusableInTouchMode(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private final TextWatcher repeatPhoneLoginListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mRepeatPhoneLoginBtEditText.getText().length() == 11) {
                mRepeatPhoneLoginSureBtn.setClickable(true);
                mRepeatPhoneLoginSureBtn.setFocusable(true);
                mRepeatPhoneLoginSureBtn.setFocusableInTouchMode(true);
                mRepeatPhoneLoginSureBtn.requestFocus();
                if (getActivity().getWindow().getAttributes().softInputMode
                        == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
                    mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
            } else {
                mRepeatPhoneLoginSureBtn.setClickable(false);
                mRepeatPhoneLoginSureBtn.setFocusable(false);
                mRepeatPhoneLoginSureBtn.setFocusableInTouchMode(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        mClickId = viewId;
        String phone;
        if (viewId == R.id.repeat_common_jump_btn) {
            if (mContainerPhoneLoginStep2.getVisibility() == View.GONE) {
                mContainerPhoneLoginStep2.setVisibility(View.VISIBLE);
                mContainerPhoneLoginStep1.setVisibility(View.GONE);
            }
        } else {
            if (viewId == R.id.repeat_phone_login_bt_sure) {
                if (mPresenter != null) {
                    phone = mRepeatPhoneLoginBtEditText.getText().toString();
                    if (phone.length() == MAX_PHONE_LENGTH) {
                        mRepeatPhoneLoginSureBtn.setClickable(false);
                        mPresenter.loginByPhone(phone);
                    }
                }
            } else if (viewId == R.id.repeat_phone_commit_btn) {
                if (mOnHiddenListener != null) {
                    mOnHiddenListener.onCompleted();
                }
            } else if (viewId == R.id.repeat_phone_cancel_btn) {
                if (mContainerPhoneLoginStep1.getVisibility() == View.GONE) {
                    mContainerPhoneLoginStep1.setVisibility(View.VISIBLE);
                    mContainerPhoneLoginStep2.setVisibility(View.GONE);
                }
            } else {
                String typePhone = mPhoneMacLoginBtEditText.getText().toString();
                if (viewId == R.id.phone_mac_login_bt_sure) {
                    // 注册界面
                    if (!StringUtils.isNullOrEmpty(mStatus) && REPEAT_MAC_STATUS_1.equals(
                            mStatus)) {
                        Logger.d("redirect to login view");
                        showLoginByPhoneSuccessView(true, REPEAT_MAC_STATUS_1);
                    } else if (mPresenter != null) {
                        phone = typePhone;
                        if (phone.length() == MAX_PHONE_LENGTH) {
                            mPhoneMacLoginSureBtn.setClickable(false);
                            mPresenter.loginByPhone(phone);
                        }
                    }
                } else if (viewId == R.id.phone_mac_login_check_code_btn) {
                    if (StringUtils.isNullOrEmpty(typePhone)) {
                        ToastUtils.showToast(getContext(), getString(R.string.no_phone));
                    } else if (mPresenter != null) {
                        mPhoneMacLoginCheckCodeBtn.setClickable(false);
                        mPhoneMacLoginCheckCodeErrorTv.setText(null);
                        mHandler.postDelayed(mRepeatRunnable, 0);
                        mHandler.postDelayed(mTipRunnable, 0);
                        mPresenter.getVerifyCode(typePhone);
                    }
                } else if (viewId == R.id.phone_mac_login_check_commit_btn) {//验证
                    if (mPresenter != null) {
                        phone = typePhone;
                        String code = mPhoneMacLoginBtEditStep2Tv.getText().toString();
                        if (phone.length() == MAX_PHONE_LENGTH && code.length() == 4) {
                            mPhoneMacLoginCheckCommitBtn.setClickable(false);
                            mPhoneMacLoginCheckCodeErrorTv.setText(
                                    R.string.phone_number_string_detail18);
                            mPresenter.loginByVerifyCode(phone, code, "1", "1");//1是院线.mStatus=1是注册
                        }
                    }
                } else if (viewId == R.id.phone_mac_login_last_step_btn) {
                    if (mContainerRepeatMacStep1.getVisibility() == View.GONE) {
                        mContainerRepeatMacStep1.setVisibility(View.VISIBLE);
                        mContainerRepeatMacStep2.setVisibility(View.GONE);
                        mContainerRepeatMacStep3.setVisibility(View.GONE);
                        mPhoneMacLoginBtEditStep2Tv.setText("");
                        mPhoneMacLoginCheckCodeErrorTv.setVisibility(View.GONE);
                        mPhoneMacLoginBtEditText.requestFocus();
                    }
                } else if (viewId == R.id.phone_mac_login_btn_step3) {
                    if (mOnHiddenListener != null) {
                        mOnHiddenListener.onCompleted();
                    }
                } else if (viewId == R.id.mac_error_alert_btn) {
                    if (mOnHiddenListener != null) {
                        mOnHiddenListener.onExit();
                    }
                }
            }
        }
    }

    private int mTime = 60;
    private final Handler mHandler = new Handler();
    //倒计时60秒
    private final Runnable mRepeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (--mTime >= 0) {
                if (isAdded()) {
                    mPhoneMacLoginCheckCodeBtn.setText(
                            String.format(getString(R.string.phone_number_string_detail11),
                                    String.valueOf(mTime)));
                    mHandler.postDelayed(this, 1000);
                }
            } else {
                mPhoneMacLoginCheckCodeBtn.setText(R.string.phone_number_string_detail25);
                mPhoneMacLoginCheckCodeBtn.setClickable(true);
                mTime = 120;
            }
        }
    };

    private int mLoginTime = 10;
    //登录倒计时10秒
    private final Runnable mShowBring2LoginRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) {
                if (--mLoginTime >= 0) {
                    mPhoneMacLoginStep3Btn.setText(
                            String.format(getString(R.string.phone_number_string_detail14),
                                    String.valueOf(mLoginTime)));
                    mHandler.postDelayed(this, 1000);
                } else {
                    mLoginTime = 10;
                    if (mOnHiddenListener != null) {
                        mOnHiddenListener.onCompleted();
                    }
                }
            }
        }
    };

    private int mTipTime = 120;
    //倒计时60秒
    private final Runnable mTipRunnable = new Runnable() {
        @Override
        public void run() {
            if (mPhoneMacLoginCheckCodeErrorTv.getVisibility() == View.GONE) {
                mPhoneMacLoginCheckCodeErrorTv.setVisibility(View.VISIBLE);
                mPhoneMacLoginCheckCodeErrorTv.setText(
                        getString(R.string.phone_number_string_detail12));
            }
            if (--mTipTime >= 0) {
                if (isAdded()) {
                    mHandler.postDelayed(this, 1000);
                }
            } else {
                mPhoneMacLoginCheckCodeErrorTv.setText(
                        getString(R.string.phone_number_string_detail17));
                mTipTime = 120;
            }
        }
    };
}
