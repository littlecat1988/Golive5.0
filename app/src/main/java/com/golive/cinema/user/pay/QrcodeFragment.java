package com.golive.cinema.user.pay;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.golive.cinema.Injection;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.network.entity.Order;
import com.golive.network.helper.StringUtils;
import com.golive.network.helper.UserInfoHelper;
import com.golive.pay.PayManager;
import com.golive.pay.aidl.CallBack;
import com.golive.pay.util.AliQrcodePay;
import com.golive.pay.util.QRCodePay;
import com.golive.pay.util.WeChatCodePay;
import com.initialjie.log.Logger;

import java.util.Map;

/**
 * Created by Mowl on 2016/11/10.
 */

public class QrcodeFragment extends MvpFragment implements QrcodeContract.View, CallBack {
    private static final int QRCODE_LOAD_FAILED_RES = R.drawable.qrcode_load_failed;
    private QrcodeContract.Presenter mPresenter;
    private QrcodeContract.PayResultCallBack mPayCallBack;
    private TextView mAndTv;
    private ImageView mDefaultQrcodeImg;
    private ImageView mAlipayImage, mWechatImage;
    private ViewGroup mQrcodeView;
    private QRCodePay mQrodePayTask;
    private WeChatCodePay mWeChatCodePay;
    private AliQrcodePay mAliQrcodePay;
    private QRCodePay mQrCodeVipMonthlyPay;
    private PayManager mPayManager;
    private int mViewWidth;
    private int mViewHight;
    /** 0 all 1 ali 2 wechat */
    private int qrCodeMode = QrcodeContract.ALI_WECHAT_MODE_BOTH;
    private boolean mVipMonthly;
    private boolean mIsShowText = true;
    private String mPayPrice;
    private String mProductId;
    private String mPayName;
    private String mUserId;

    public static QrcodeFragment newInstance(String price, String productId, String name,
            QrcodeContract.PayResultCallBack callback, int aliOrWechat, boolean vipMonthly) {
        QrcodeFragment fragment = new QrcodeFragment();
        fragment.mPayCallBack = callback;
        fragment.mPayPrice = price;
        fragment.mProductId = productId;
        fragment.mPayName = name;
        fragment.qrCodeMode = aliOrWechat;
        fragment.mVipMonthly = vipMonthly;
        return fragment;
    }

    public static QrcodeFragment newInstance(String price, String productId, String name,
            QrcodeContract.PayResultCallBack callback, int aliOrWechat, boolean vipMonthly,
            int width, int height, boolean showText) {
        QrcodeFragment instance = newInstance(price, productId, name, callback, aliOrWechat,
                vipMonthly);
        instance.mViewWidth = width;
        instance.mViewHight = height;
        instance.mIsShowText = showText;
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_pay_qrcode_base, container, false);
        mDefaultQrcodeImg = (ImageView) rootView.findViewById(R.id.user_pay_qrcode_default_img);
        mQrcodeView = (ViewGroup) rootView.findViewById(R.id.user_pay_qrcode_view);
        mAlipayImage = (ImageView) rootView.findViewById(R.id.user_pay_qrcode_image_alipay);
        mWechatImage = (ImageView) rootView.findViewById(R.id.user_pay_qrcode_image_wechat);
        mAndTv = (TextView) rootView.findViewById(R.id.user_pay_qrcode_tv_and);
        if (!mIsShowText) {
            (rootView.findViewById(R.id.user_pay_qrcode_bottom_text)).setVisibility(View.GONE);
        }
        setDefaultQrcodeImgShow(false);
        setIconTextVisible(qrCodeMode);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mQrodePayTask != null) {
            mQrodePayTask.resumeTask(this);
        }
        if (mAliQrcodePay != null) {
            mAliQrcodePay.resumeTask(this);
        }
        if (mWeChatCodePay != null) {
            mWeChatCodePay.resumeTask(this);
        }
        if (mQrCodeVipMonthlyPay != null) {
            mQrCodeVipMonthlyPay.resumeTask(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mQrodePayTask != null) {
            mQrodePayTask.removeTask(false);
        }
        if (mAliQrcodePay != null) {
            mAliQrcodePay.removeTask(false);
        }
        if (mWeChatCodePay != null) {
            mWeChatCodePay.removeTask(false);
        }
        if (mQrCodeVipMonthlyPay != null) {
            mQrCodeVipMonthlyPay.removeTask(false);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("onActivityCreated ,price=" + mPayPrice + ",name=" + mPayName);
        Activity activity = getActivity();
        mPayManager = new PayManager(activity);
        mUserId = UserInfoHelper.getUserId(activity);
        mPresenter = new QrcodePresenter(this, Injection.provideGetPayUrlListUseCase(activity));
        if (getPresenter() != null) {
            getPresenter().start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPayManager != null) {
            mPayManager.destory();
        }
    }

    @Override
    public void setPresenter(QrcodeContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    @Override
    protected QrcodeContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private String getPayParameter() {
        String accountID = mUserId;
        String productName = mPayName;
        String productPrice = mPayPrice;
        String orderType = mVipMonthly ? Order.PRODUCT_TYPE_VIP : "RMB";
        String productId = mVipMonthly && !StringUtils.isNullOrEmpty(mProductId) ? mProductId : "";

        return "accountID="
                + accountID
                + "&productName="
                + productName
                + "&productPrice="
                + productPrice
                + "&orderType="
                + orderType
                + "&productId="
                + productId
                + "&backgroupFile="
                + "";
    }

    @Override
    public void setLoadingIndicator(boolean active) {

    }

    @Override
    public void showQrcode(Map<String, String> urlMap) {
        Logger.d("showQrcode");
        final String payParameter = getPayParameter();
        mPayManager.initPayUrl(urlMap);

        // 连续包月会员
        if (mVipMonthly) {
            if (mQrCodeVipMonthlyPay != null) {
                mQrCodeVipMonthlyPay.removeTask(false);
            }
            mQrCodeVipMonthlyPay = mPayManager.payBlendMonthQRcode(mQrcodeView, payParameter, this);
            return;
        }

        if (QrcodeContract.ALI_WECHAT_MODE_ALI == qrCodeMode) { //阿里支付
            if (mAliQrcodePay != null) {
                mAliQrcodePay.removeTask(false);
            }
            mAliQrcodePay = mPayManager.payByAliQRcode(mQrcodeView, payParameter, this);
        } else if (QrcodeContract.ALI_WECHAT_MODE_WECHAT == qrCodeMode) { //微信支付
            if (mWeChatCodePay != null) {
                mWeChatCodePay.removeTask(false);
            }
            mWeChatCodePay = mPayManager.payByWeChatQRcode(mQrcodeView, payParameter, this);
        } else { //混合二维码（微信/支付宝）
//            float width = mViewHight * 0.4f;
//            float width = mViewHight;
            if (mQrodePayTask != null) {
                mQrodePayTask.removeTask(false);
            }
            mQrodePayTask = mPayManager.payBlendQRcode(mQrcodeView, payParameter, this, mViewHight,
                    QRCODE_LOAD_FAILED_RES);
        }
    }

    private void payFinishBackBack(int state, String log) {
        Logger.d("showQrcode callback state=" + state + ", log" + log);
        if (state != 1) {
            Toast.makeText(getContext(), log, Toast.LENGTH_SHORT).show();
        }
//        mQrcodeView.setDefaultQrcodeImgShow(false);
        setDefaultQrcodeImgShow(false);
        if (mPayCallBack != null) {
            mPayCallBack.PayFinishExecute(state, log);
        }
    }

    private void setDefaultQrcodeImgShow(boolean isShow) {
        if (mDefaultQrcodeImg != null) {
            if (isShow) {
                mDefaultQrcodeImg.setVisibility(View.VISIBLE);
            } else {
                mDefaultQrcodeImg.setVisibility(View.GONE);
            }
        }
        if (mQrcodeView != null) {
            if (isShow) {
                mQrcodeView.setVisibility(View.GONE);
            } else {
                mQrcodeView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setIconTextVisible(int aliOrWechat) {
        if (aliOrWechat == QrcodeContract.ALI_WECHAT_MODE_ALI) {
            mAlipayImage.setVisibility(View.VISIBLE);
            mWechatImage.setVisibility(View.GONE);
            mAndTv.setVisibility(View.GONE);
        } else if (aliOrWechat == QrcodeContract.ALI_WECHAT_MODE_WECHAT) {
            mAlipayImage.setVisibility(View.GONE);
            mWechatImage.setVisibility(View.VISIBLE);
            mAndTv.setVisibility(View.GONE);
        } else { //混合二维码（微信/支付宝）
            if (mAlipayImage != null) {
                mAlipayImage.setVisibility(View.VISIBLE);
            }
            if (mWechatImage != null) {
                mWechatImage.setVisibility(View.VISIBLE);
            }
            if (mAndTv != null) {
                mAndTv.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void callback(int state, String log) {
        payFinishBackBack(state, log);
    }
}
