package com.golive.cinema.user.buyvip;

import android.content.Context;
import android.os.Bundle;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.util.ActivityUtils;


public class BuyVipActivity extends BaseActivity {
    public static final String REQUEST_CODE_EXTRA_KEY = "request_extra_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_center_base);

        BuyVipFragment fragment = BuyVipFragment.newInstance();
        Context applicationContext = getApplicationContext();
        BuyVipPresenter mPresenter = new BuyVipPresenter(fragment,
                Injection.provideGetVipListUseCase(applicationContext),
                Injection.provideGetUserWalletUseCase(applicationContext),
                Injection.provideGetUserCreditWalletUseCase(applicationContext),
                Injection.provideGetUserInfoUseCase(applicationContext),
                Injection.provideGetVipMonthlyStatusUseCase(applicationContext));
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fragment,
                R.id.userContentFrame);
    }

    @Override
    public void onBackPressed() {
        if (getIntent().getBooleanExtra(REQUEST_CODE_EXTRA_KEY, false)) {
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }
}
