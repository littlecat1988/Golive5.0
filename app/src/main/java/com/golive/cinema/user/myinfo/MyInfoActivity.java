package com.golive.cinema.user.myinfo;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.R;
import com.golive.cinema.user.usercenter.UserPublic;

public class MyInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_center_base);

        int flag = getIntent().getIntExtra(UserPublic.KEY_USER_FRAGMENT,
                UserPublic.MYINFO_FRAGMENT_MAIN);
        switchFragment(flag);
    }

    private void switchFragment(int fragmentFlag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (fragmentFlag) {
            case UserPublic.MYINFO_FRAGMENT_INFO:
                ft.replace(R.id.userContentFrame, InfoDetailFragment.newInstance());
                break;
            case UserPublic.MYINFO_FRAGMENT_VIPACTIVE:
                ft.replace(R.id.userContentFrame, ActiveVipFragment.newInstance());
                break;
            case UserPublic.MYINFO_FRAGMENT_CREDIT:
                ft.replace(R.id.userContentFrame, CreditRepayFragment.newInstance());
                break;
            case UserPublic.MYINFO_FRAGMENT_MAIN:
            default:
                ft.replace(R.id.userContentFrame, MyInfoFragment.newInstance());
                break;
        }
        ft.commit();
    }
}
