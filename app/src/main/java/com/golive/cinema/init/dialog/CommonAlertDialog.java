package com.golive.cinema.init.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.log.Logger;

/**
 * Created by chgang on 2016/11/8.
 */

public class CommonAlertDialog extends BaseDialog implements View.OnClickListener {

    public static final String DIALOG_FRAGMENT_TAG = "CommonAlertDialog_Tag";
    public static final String DIALOG_TAG = "Dialog_Tag";
    public static final String DIALOG_MSG_TAG = "CommonAlertDialog_Message_Tag";
    public static final String DIALOG_TITLE_TAG = "CommonAlertDialog_Title_Tag";

    public static final int MESSAGE_TYPE_NETWORK = 0;//网络异常
    //    public static final int MESSAGE_TYPE_REPEAT_MAC_LOCAL = 1;//MAC本地校验失败
    public static final int MESSAGE_TYPE_SERVER_STOP = 2;//停机维护
    public static final int MESSAGE_TYPE_LOGIN_ERROR = 3;//登录失败
    public static final int MESSAGE_TYPE_NETWORK_TIMEOUT = 4;//连接超时
    public static final int MESSAGE_TYPE_FACTORY_MODE = 5;//工厂模式
    public static final int MESSAGE_TYPE_UPGRADE_SHOP = 6;//商店升级提示
    public static final int MESSAGE_TYPE_INIT_FAILED = 7; // 初始化失败
//    public static final int MESSAGE_TYPE = 4;
//    public static final int MESSAGE_TYPE = 5;

    private CommonCallback mCommonCallback = null;
    private TextView mServiceTv;

    public interface CommonCallback {
        void resultDismiss(boolean isExit);
    }

    public void setCommonCallback(CommonCallback commonCallback) {
        mCommonCallback = commonCallback;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        setCancelable(false);

        View view = null;

        int tag = getArguments().getInt(DIALOG_TAG);
        String note = getArguments().getString(DIALOG_MSG_TAG);
        // 判断包含Failed to connect to或者HTTP 404 Not Found这样的字符被认为是连接超时
        if (!StringUtils.isNullOrEmpty(note)) {
            String str = note.toLowerCase();
            if (str.contains("failed") || str.contains("http")) {
                tag = MESSAGE_TYPE_NETWORK_TIMEOUT;
            }
        }
        Logger.d("tag:" + tag);
        switch (tag) {
//            case MESSAGE_TYPE_REPEAT_MAC_LOCAL:
//                view = inflater.inflate(R.layout.common_alert_mac_error, container, false);
//                ((TextView)view.findViewById(R.id.mac_error_alert_text)).setText(getString(R
// .string.e_400035));
//                ((TextView)view.findViewById(R.id.mac_error_alert_title)).setText(getString(R
// .string.e_400029));
//                view.findViewById(R.id.mac_error_alert_btn).setOnClickListener(this);
//                TextView tips = (TextView) view.findViewById(R.id.mac_error_bottom_tips);
//                tips.setText(String.format(getResources().getString(R.string
// .phone_number_string_detail16), "4001876867(9-21点)", "3115968260", "http://golive-tv.com",
//                        DeviceUtil.getMacAddress(getActivity())));
//                break;
            case MESSAGE_TYPE_SERVER_STOP:
                view = inflater.inflate(R.layout.common_alert_server_stop, container, false);
                TextView titleView = ((TextView) view.findViewById(R.id.server_stop_alert_title));
                String title = getArguments().getString(DIALOG_TITLE_TAG);
                if (!StringUtils.isNullOrEmpty(title)) {
                    titleView.setText(title);
                } else {
                    titleView.setText(R.string.e_400032);
                }
                if (!StringUtils.isNullOrEmpty(note)) {
                    ((TextView) view.findViewById(R.id.server_stop_alert_text)).setText(
                            Html.fromHtml(note));
                } else {
                    ((TextView) view.findViewById(R.id.server_stop_alert_text)).setText(
                            R.string.e_400030);
                }
                view.findViewById(R.id.server_stop_alert_btn).setOnClickListener(this);
                mServiceTv = ((TextView) view.findViewById(R.id.service_phone));
                break;
            case MESSAGE_TYPE_NETWORK_TIMEOUT:
                view = inflater.inflate(R.layout.common_alert_network_error, container, false);
                TextView timeOutTitle = ((TextView) view.findViewById(
                        R.id.network_error_alert_title));
                timeOutTitle.setText(R.string.fail_connect_timeout);
                ((TextView) view.findViewById(R.id.network_error_alert_text)).setText(
                        R.string.e_400034);
                view.findViewById(R.id.network_error_alert_btn).setOnClickListener(this);

                mServiceTv = ((TextView) view.findViewById(R.id.service_phone));
                final Button retryBtn = (Button) view.findViewById(R.id.network_error_try_btn);
                retryBtn.setVisibility(View.VISIBLE);
                retryBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getContext().sendBroadcast(
                                new Intent(Constants.INIT_NETWORK_RESTART_BROADCAST_ACTION));
                    }
                });
                retryBtn.requestFocus();
                break;
            case MESSAGE_TYPE_LOGIN_ERROR: // 登录失败
            case MESSAGE_TYPE_INIT_FAILED: // 初始化失败
                view = inflater.inflate(R.layout.common_alert_mac_error, container, false);
                TextView titleTv = (TextView) view.findViewById(R.id.mac_error_alert_title);
                if (titleTv != null) {
                    titleTv.setText(MESSAGE_TYPE_LOGIN_ERROR == tag ? R.string.e_400021
                            : R.string.init_failed);
                }
                view.findViewById(R.id.mac_error_alert_btn).setOnClickListener(this);
                mServiceTv = ((TextView) view.findViewById(R.id.service_phone));
                if (!StringUtils.isNullOrEmpty(note)) {
                    ((TextView) view.findViewById(R.id.mac_error_alert_text))
                            .setText(String.format(getString(R.string.e_400036),
                                    getNoteMessage(note)));
                }
                break;
            case MESSAGE_TYPE_FACTORY_MODE:
                break;
            case MESSAGE_TYPE_UPGRADE_SHOP:
                view = inflater.inflate(R.layout.common_alert_shopping_upgrade, container, false);
                TextView gradeTitle = (TextView) view.findViewById(R.id.title_tv);
                gradeTitle.setText(getString(R.string.dialog_update_info_title));
                TextView gradeContent = (TextView) view.findViewById(R.id.content_tv);
                Button okButton = (Button) view.findViewById(R.id.ok_btn);
                if (!StringUtils.isNullOrEmpty(note)) {
                    if (Integer.parseInt(note) == Constants.UPGRADE_TYPE_OPTIONAL_FORCE) {
                        gradeContent.setText(getString(R.string.dialog_update_info_force1));
                        okButton.setOnClickListener(this);
                    } else {
                        gradeContent.setText(getString(R.string.dialog_update_info_normal1));
                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mCommonCallback != null) {
                                    mCommonCallback.resultDismiss(false);
                                }
                            }
                        });
                    }
                }
                break;
            case MESSAGE_TYPE_NETWORK://网络异常
            default:
                view = inflater.inflate(R.layout.common_alert_network_error, container, false);
                TextView titleNetView = ((TextView) view.findViewById(
                        R.id.network_error_alert_title));
                titleNetView.setText(R.string.e_400031);
                ((TextView) view.findViewById(R.id.network_error_alert_text)).setText(
                        R.string.e_400033);
                view.findViewById(R.id.network_error_alert_btn).setOnClickListener(this);
                mServiceTv = ((TextView) view.findViewById(R.id.service_phone));
                break;
        }

        showServicePhone();
        return view;
    }

    @Override
    public void onClick(View v) {
        if (mCommonCallback != null) {
            mCommonCallback.resultDismiss(true);
        }
    }

    private String getNoteMessage(String note) {
        String message = "";
        if (note.contains("1007")) {
            message = getString(R.string.e_1007);
        } else if (note.contains("2003")) {
            message = getString(R.string.e_2003);
        } else if (note.contains("2004")) {
            message = getString(R.string.e_2004);
        } else if (note.contains("2005")) {
            message = getString(R.string.e_2005);
        } else if (note.contains("2006")) {
            message = getString(R.string.e_2006);
        } else if (note.contains("2007")) {
            message = getString(R.string.e_2007);
        } else if (note.contains("2011")) {
            message = getString(R.string.e_2011);
        } else if (note.contains("2012")) {
            message = getString(R.string.e_2012);
        } else if (note.contains("2013")) {
            message = getString(R.string.e_2013);
        } else if (note.contains("2014")) {
            message = getString(R.string.e_2014);
        } else if (note.contains("2015")) {
            message = getString(R.string.e_2015);
        }

        if (!StringUtils.isNullOrEmpty(message)) {
            return message;
        } else {
            return note;
        }
    }

    private void showServicePhone() {
        String phone = UserInfoHelper.getServicePhone(getContext());
        String qq = UserInfoHelper.getServiceQQ(getContext());
        if (!StringUtils.isNullOrEmpty(phone) && !StringUtils.isNullOrEmpty(qq)
                && mServiceTv != null) {
            mServiceTv.setText(String.format(getResources()
                    .getString(R.string.init_service_phone_qq), phone, qq));
        }
    }
}
