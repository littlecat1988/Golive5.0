package com.golive.cinema.user.setting;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_ABOUT;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_SETTING;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.MyApplication;
import com.golive.cinema.R;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.PackageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.DeviceInfo;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.RequestParameter;
import com.golive.network.entity.Upgrade;
import com.golive.network.helper.DeviceHelper;
import com.initialjie.log.Logger;


/**
 * Created by Mowl on 2016/11/4.
 */

public class AboutFragment extends BaseDialog implements SettingContract.View {
    private static final int MAX_CHAR_LENGTH = 1024;
    private SettingContract.Presenter mPresenter;
    private TextView mVersionTv, mKdmTv, mDeviceNumTv, mDeviceMacTv, mHttpTv, mNetIpTv, mBelongTv;
    private ProgressDialog mProgressDialog;
    private long mEnterTime;
    private String mChangeServerKey;
    private final StringBuilder mStringBuilder = new StringBuilder();

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.user_settings_about, container, false);
        mVersionTv = (TextView) root.findViewById(R.id.tv_setting_about_version);
        mKdmTv = (TextView) root.findViewById(R.id.tv_setting_about_kdm_version);
        mDeviceNumTv = (TextView) root.findViewById(R.id.tv_setting_about_device_num);
        mDeviceMacTv = (TextView) root.findViewById(R.id.tv_setting_about_mac);
        mBelongTv = (TextView) root.findViewById(R.id.tv_setting_about_belong);
        mNetIpTv = (TextView) root.findViewById(R.id.tv_setting_about_netip);
        mHttpTv = (TextView) root.findViewById(R.id.tv_setting_about_http);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (KeyEvent.ACTION_UP == event.getAction()) {
                    // not product release version
                    if (!Constants.PROD) {
                        checkHideControl(keyCode);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("onActivityCreated");
        mEnterTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(getContext()).reportEnterActivity(VIEW_CODE_ABOUT, "关于",
                VIEW_CODE_SETTING);
        Context context = getContext().getApplicationContext();
        mPresenter = new SettingPresenter(this,
                Injection.provideGetMainConfigUseCase(context),
                Injection.provideUpgradeUseCase(context),
                Injection.provideGetKdmVersionUseCase(context),
                Injection.provideSchedulerProvider());
        if (getPresenter() != null) {
            getPresenter().start();
        }
        initShowInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy ");
        if (getPresenter() != null) {
            getPresenter().unsubscribe();
        }
        String time = String.valueOf((System.currentTimeMillis() - mEnterTime) / 1000);
        StatisticsHelper.getInstance(getActivity()).reportExitActivity(VIEW_CODE_ABOUT, "关于", "",
                time);
    }

    private void initShowInfo() {
        RequestParameter parameter = RequestParameter.getInstance(getContext());
        DeviceInfo deviceInfo = parameter.getDeviceInfo();

        //String devmodel = parameter.getDeviceModel();
        //String deviceId = deviceInfo.getDeviceId();
        String clienttype = deviceInfo.getClientType();
        String mac = deviceInfo.getMac();
        //String deviceid = deviceInfo.getDeviceId();

        String golive_version = PackageUtils.getVersionName(getContext(),
                getActivity().getPackageName());
        mVersionTv.setText(String.format(getString(R.string.setting_about_version),
                golive_version));//UserPublic.getApkVersionName(getContext())
        mKdmTv.setText(String.format(getString(R.string.setting_about_kdm), ""));
        mDeviceNumTv.setText(String.format(getString(R.string.setting_about_terminal), clienttype));
        mNetIpTv.setText(String.format(getString(R.string.setting_about_ip), ""));
        mDeviceMacTv.setText(String.format(getString(R.string.setting_about_mac), mac));

        getPresenter().getKdmVersion(getActivity());
    }

    @Override
    public void setMainConfig(MainConfig cfg) {
        if (null == cfg) {
            return;
        }

        //String phone = cfg.getServicephone();
        String net = cfg.getOfficialwebsite();
        //String currenttime = cfg.getCurrenttime();
        String clientip = cfg.getClientip();
        String copyright = cfg.getCopyright();
        //String help = cfg.getHelp();

        if (clientip != null) {
            mNetIpTv.setText(String.format(getString(R.string.setting_about_ip), clientip));
        }

        if (copyright != null) {
            mBelongTv.setText(copyright);
        }

        if (net != null) {
            mHttpTv.setText(net);
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
    public void showUpgradeView(final Upgrade upgrade, final int upgradeType) {
        Logger.d("showUpgradeView:" + upgradeType);
//        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
//            @Override
//            public void call(final Subscriber<? super Boolean> subscriber) {
//                if (upgradeType == Constants.UPGRADE_TYPE_NO_UPGRADE) {
//                } else {
//                }
//            }
//        };
    }

    @Override
    public void setChangeServerKey(String key) {
        Logger.d("setChangeServerKey : " + key);
        mChangeServerKey = StringUtils.isNullOrEmpty(key) ? Constants.KEY_CHANGE_SERVER : key;
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
    public void setCheckingUpgradeIndicator(boolean active) {
    }

    @Override
    public void setPresenter(SettingContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    protected SettingContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private void checkHideControl(int keyCode) {
        if (StringUtils.isNullOrEmpty(mChangeServerKey)) {
            return;
        }

        if (KeyEvent.KEYCODE_0 <= keyCode && KeyEvent.KEYCODE_9 >= keyCode) {
            mStringBuilder.append(keyCode - KeyEvent.KEYCODE_0);
            String string = mStringBuilder.toString();
            if (string.contains(mChangeServerKey)) {
                // reset
                mStringBuilder.setLength(0);
                final Context context = getContext();
                final String[] SERVER_URLS = Constants.SERVER_URLS;
                new AlertDialog.Builder(context)
                        .setTitle(R.string.change_server)
                        .setItems(SERVER_URLS, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                // change server
                                String url = SERVER_URLS[which];
                                MyApplication application =
                                        (MyApplication) getContext().getApplicationContext();
                                application.changeServer(url);
                            }
                        })
                        .create()
                        .show();
            }

            // cache to many character
            if (mStringBuilder.length() > MAX_CHAR_LENGTH) {
                // reset
                mStringBuilder.setLength(0);
            }
        }
    }
}
