package com.golive.cinema.user.custom;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_CUSTOMER_SERVICE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_USER_CENTER;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.PackageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.ClientService;
import com.golive.network.entity.DeviceInfo;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.RequestParameter;
import com.golive.network.helper.DeviceHelper;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.log.Logger;

public class CustomFragment extends MvpFragment implements CustomContract.View {
    private ProgressDialog mProgressDialog;
    private CustomContract.Presenter mPresenter;
    private TextView mVersionTv, mKdmTv, mDeviceNumTv, mNetipTv, mMacTv, mPhoneTv, mQqTv, mWxTv;
    private ImageView image;
    private long mEnterTime;

    public static CustomFragment newInstance() {
        return new CustomFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_custom, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVersionTv = (TextView) view.findViewById(R.id.tv_setting_about_version);
        mKdmTv = (TextView) view.findViewById(R.id.tv_setting_about_kdm_version);
        mDeviceNumTv = (TextView) view.findViewById(R.id.tv_setting_about_device_num);
        mNetipTv = (TextView) view.findViewById(R.id.tv_setting_about_netip);
        mMacTv = (TextView) view.findViewById(R.id.tv_setting_about_mac);
        mPhoneTv = (TextView) view.findViewById(R.id.tv_custom_item_phone);
        mQqTv = (TextView) view.findViewById(R.id.tv_custom_item_qq);
        image = (ImageView) view.findViewById(R.id.imag_head_icon);
        mWxTv = (TextView) view.findViewById(R.id.tv_custom_item_detail);
        mKdmTv.setText(String.format(getString(R.string.setting_about_kdm), ""));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CustomContract.Presenter presenter = getPresenter();
        if (presenter != null) {
            presenter.loadInfo();
        }
        mEnterTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(getContext()).reportEnterActivity(VIEW_CODE_CUSTOMER_SERVICE,
                "客服", VIEW_CODE_USER_CENTER);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (!isAdded()) {
            return;
        }
        if (active) {
            if (null == mProgressDialog) {
                mProgressDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.loading));
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
    public void showClientService(ClientService info) {
        if (null == info) {
            return;
        }

        String phone5 = info.getServicePhone5();
        if (!StringUtils.isNullOrEmpty(phone5)) {
            mPhoneTv.setText(String.format(getString(R.string.custom_phone), phone5));
            UserInfoHelper.setServicePhone(getContext(), phone5);
        }
        String qq = info.getQQ();
        if (!StringUtils.isNullOrEmpty(qq)) {
            mQqTv.setText(String.format(getString(R.string.custom_qq), qq));
            UserInfoHelper.setServiceQQ(getContext(), qq);
        }

        String url = info.getWechatPublicTwoDimension();
        Glide.with(this)
                .load(url)
                .error(R.drawable.wechat_icon)
                .into(image);
        String name = info.getWechatPublicName();
        if (!StringUtils.isNullOrEmpty(name)) {
            mWxTv.setText(name);
        }
    }

    @Override
    public void setKdmVersion(String version, String platform) {
        if (!StringUtils.isNullOrEmpty(version)) {
            String text = version;
            if (!StringUtils.isNullOrEmpty(platform)) {
                text = version + " " + platform;
            }

            mKdmTv.setText(String.format(getString(R.string.setting_about_kdm), text));
            mKdmTv.setVisibility(View.VISIBLE);
            DeviceHelper.setKdmVersion(getContext(), text);
        }
    }

    @Override
    public void showMainInfo(MainConfig cfg) {
        if (null == cfg) {
            return;
        }
        RequestParameter parameter = RequestParameter.getInstance(getContext());
        DeviceInfo deviceInfo = parameter.getDeviceInfo();

        String clienttype = deviceInfo.getClientType();
        String mac = deviceInfo.getMac();
        String clientip = cfg.getClientip();
        String version = PackageUtils.getVersionName(getContext(), getActivity().getPackageName());

        mVersionTv.setText(String.format(getString(R.string.setting_about_version), version));
        mDeviceNumTv.setText(String.format(getString(R.string.setting_about_terminal), clienttype));
        mMacTv.setText(String.format(getString(R.string.setting_about_mac), mac));
        mNetipTv.setText(String.format(getString(R.string.setting_about_ip), clientip));
    }

    @Override
    public void setPresenter(CustomContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    @Override
    protected CustomContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy ");
        String time = String.valueOf((System.currentTimeMillis() - mEnterTime) / 1000);
        StatisticsHelper.getInstance(getActivity()).reportExitActivity(VIEW_CODE_CUSTOMER_SERVICE,
                "客服", "", time);
    }
}
