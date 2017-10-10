package com.golive.cinema.user.topup;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.R;
import com.golive.cinema.user.usercenter.UserPublic;

public class TopupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_center_base);

        Intent intent = getIntent();
        int flag = intent.getIntExtra(UserPublic.KEY_USER_FRAGMENT, UserPublic.TOPUP_FRAGMENT_MAIN);
        switchFragment(flag);
    }

    private void switchFragment(int fragmentFlag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (fragmentFlag) {
            case UserPublic.TOPUP_FRAGMENT_QRCAODE_PAY:
                PayTopupFragment mPayQrcodeFragment = PayTopupFragment.newInstance();
                ft.replace(R.id.userContentFrame, mPayQrcodeFragment);
                break;
            case UserPublic.TOPUP_FRAGMENT_MAIN:
            default:
                TopupFragment mTopupFragment = TopupFragment.newInstance();
                ft.replace(R.id.userContentFrame, mTopupFragment);
                break;
        }
        ft.commit();
    }
}
