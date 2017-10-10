package com.golive.cinema.init;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.init.dialog.CommonAlertDialog;
import com.golive.cinema.init.network.NetWorkReceiver;
import com.golive.cinema.util.ActivityUtils;
import com.golive.cinema.util.FragmentUtils;
import com.golive.cinema.util.PackageUtils;
import com.initialjie.log.Logger;

/**
 * Created by chgang on 2016/10/28.
 */

public class SplashActivity extends BaseActivity implements NetWorkReceiver.NetworkCallback {

    public static long FIST_SHOW_UP_TIME;

    private NetWorkReceiver mNetWorkReceiver;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

//        if (!isTaskRoot()) {
//            finish();
//            return;
//        }

        setContentView(R.layout.activity_splash);

        InitFragment initFragment = FragmentUtils.newFragment(InitFragment.class);
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), initFragment,
                R.id.splash_fragment);
        Context context = getApplicationContext();
        InitPresenter mInitPresenter = new InitPresenter(initFragment,
                Injection.provideGetMainConfigUseCase(context)
                , Injection.provideGetShutdownMessageUseCase(context)
                , Injection.provideBootImageUseCase(context)
                , Injection.provideUpgradeUseCase(context)
                , Injection.provideRepeatMacUseCase(context)
                , Injection.provideGetKdmInitUseCase(context)
                , Injection.provideGetKdmVersionUseCase(context)
                , Injection.provideGetKdmServerVersionUseCase(context)
                , Injection.provideUpgradeKdmUseCase(context)
                , Injection.provideNotifyKdmReadyUseCase(context)
                , Injection.provideLogin(context)
                , Injection.provideGetUserHeadUseCase(context)
                , Injection.provideAdvertUseCase(context)
                , Injection.provideSchedulerProvider());

        String text = "V" + PackageUtils.getVersionName(context, context.getPackageName());
        ((TextView) findViewById(R.id.init_version_text)).setText(text);
        if (null == mNetWorkReceiver) {
            mNetWorkReceiver = new NetWorkReceiver().setNetworkCallback(this);
        }
        mNetWorkReceiver.registerReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNetWorkReceiver != null) {
            unregisterReceiver(mNetWorkReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (FIST_SHOW_UP_TIME <= 0) {
            FIST_SHOW_UP_TIME = System.currentTimeMillis();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
//        sendBroadcast(new Intent(Constants.INIT_SPLASH_EXIT_BROADCAST_ACTION));
        exit();
        super.onBackPressed();
    }

    public void exit() {
        this.setResult(RESULT_OK);
        this.finish();
        Logger.d("exit");
    }

    public void close() {
        sendBroadcast(new Intent(Constants.INIT_SPLASH_BROADCAST_ACTION));
        this.finish();
        Logger.d("close");
    }

    @Override
    public void callNetworkStatus(final Boolean status) {
        CommonAlertDialog commonAlertDialog;
        final String fragTag = CommonAlertDialog.DIALOG_FRAGMENT_TAG;
        if (status) {
            commonAlertDialog =
                    (CommonAlertDialog) getSupportFragmentManager().findFragmentByTag(fragTag);
            if (commonAlertDialog != null) {
                commonAlertDialog.dismiss();
            }
            return;
        }

        commonAlertDialog = FragmentUtils.newFragment(CommonAlertDialog.class);
        Bundle bundle = new Bundle();
        bundle.putInt(CommonAlertDialog.DIALOG_TAG, CommonAlertDialog.MESSAGE_TYPE_NETWORK);
        commonAlertDialog.setArguments(bundle);
        commonAlertDialog.setCommonCallback(new CommonAlertDialog.CommonCallback() {
            @Override
            public void resultDismiss(boolean isExit) {
                if (isExit) {
                    exit();
                }
            }
        });
        FragmentUtils.removePreviousFragment(getSupportFragmentManager(), fragTag);
        commonAlertDialog.show(getSupportFragmentManager(), fragTag);
    }
}
