package com.golive.cinema.user.usercenter;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_CHARGE_NOTICE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MY_ACCOUNT;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_WATCH_NOTICE;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.golive.cinema.R;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.StringUtils;

import java.text.DecimalFormat;

/**
 * Created by Mowl on 2016/11/9.
 */

public class UserPublic {

    public final static String KEY_USER_FRAGMENT = "user_flag_key";
    public final static int MYINFO_FRAGMENT_MAIN = 0;
    public final static int MYINFO_FRAGMENT_INFO = 1;
    public final static int MYINFO_FRAGMENT_CREDIT = 3;
    public final static int MYINFO_FRAGMENT_VIPACTIVE = 5;

    public final static int TOPUP_FRAGMENT_MAIN = 0;
    public final static int TOPUP_FRAGMENT_QRCAODE_PAY = 1;

    public final static String KEY_PAY_PRICE = "pay_price_key";
    public final static String KEY_PAY_NAME = "pay_name_key";
    public final static String KEY_PAY_PAGE = "pay_page_key";

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

    public static String vipMonthId;
    public static final int KEY_LAUNCHER_USER_PAGE = 9901;

    public static Dialog TipsDialog(Context context, final int layoutId) {
        Dialog aDialog = new Dialog(context, R.style.dialog_fullscreen_bg);
        aDialog.setContentView(layoutId);

        if (aDialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams params = aDialog.getWindow().getAttributes();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            params.width = wm.getDefaultDisplay().getWidth();
            params.height = wm.getDefaultDisplay().getHeight() + 60;
            params.x = 0;
            params.y = 0;
            // params.alpha=0.7f;
            aDialog.getWindow().setAttributes(params);
            aDialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        }

        return aDialog;
    }

    public static String getFormatStrFromDouble(double value) {
        DecimalFormat df = new DecimalFormat("#0.00");
        String p = df.format(value);
        // String newStr = p.replaceAll("^(0+)", "");
        return p;
    }

    public static boolean isTwoPriceSame(String price1, String price2) {
        if (StringUtils.isNullOrEmpty(price1) || StringUtils.isNullOrEmpty(price2)) {
            return false;
        }
        if (price1.equals(price2)) {
            return true;
        }
        try {
            double value1 = Double.valueOf(price1);
            double value2 = Double.valueOf(price2);
            double vv = value1 - value2;
            if (Math.abs(vv) < 0.010d) {
                return true;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setNeedKnowDialog(final Context context) {
        final Dialog aDialog = TipsDialog(context, R.layout.user_setting_about_needkown);
        TextView title = (TextView) aDialog.findViewById(R.id.user_setting_need_kown_title);
        title.setText(context.getResources().getString(R.string.setting_movie_watch_need_kown));
        TextView detail = (TextView) aDialog.findViewById(R.id.user_setting_need_kown_detail);
        String detailText = context.getString(R.string.film_watch_notice_content_tongbu);
        detail.setText(Html.fromHtml(detailText));
        Button button = (Button) aDialog.findViewById(R.id.user_setting_needkown_kownbtn);
        button.setText(R.string.i_know);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.dismiss();
            }
        });

        final long intoTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(context).reportEnterActivity(VIEW_CODE_WATCH_NOTICE, "观影须知",
                VIEW_CODE_MY_ACCOUNT);

        aDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                String time = String.valueOf((System.currentTimeMillis() - intoTime) / 1000);
                StatisticsHelper.getInstance(context).reportExitActivity(VIEW_CODE_WATCH_NOTICE,
                        "观影须知", "", time);
            }
        });

        aDialog.show();
    }

    public static void setServiceKnowDialog(final Context context) {
        final Dialog aDialog = TipsDialog(context, R.layout.user_setting_about_service);

        TextView title = (TextView) aDialog.findViewById(R.id.user_setting_service_title);
        title.setText(context.getResources().getString(R.string.setting_pay_service_protocol));

        //<font color='#FF0000'>"+inputTimes+"</font>
        TextView detail = (TextView) aDialog.findViewById(R.id.user_setting_service_detail);
        String detailText = context.getString(R.string.pay_service_agreement);
        detail.setText(Html.fromHtml(detailText));


        Button button = (Button) aDialog.findViewById(R.id.user_setting_service_btn);
        button.setText(R.string.user_empty_ok);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.dismiss();
            }
        });

        final long intoTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(context).reportEnterActivity(VIEW_CODE_CHARGE_NOTICE, "收费服务协议",
                VIEW_CODE_MY_ACCOUNT);

        aDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                String time = String.valueOf((System.currentTimeMillis() - intoTime) / 1000);
                StatisticsHelper.getInstance(context).reportExitActivity(VIEW_CODE_CHARGE_NOTICE,
                        "收费服务协议", "", time);
            }
        });

        aDialog.show();
    }
}
