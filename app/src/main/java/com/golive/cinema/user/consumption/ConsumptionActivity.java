package com.golive.cinema.user.consumption;

import android.os.Bundle;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.util.ActivityUtils;

public class ConsumptionActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_center_base);

        ConsumptionFragment fragment = ConsumptionFragment.newInstance();
        ConsumptionPresenter presenter = new ConsumptionPresenter(fragment,
                Injection.provideGetWalletOperationListUseCase(getApplicationContext()));
        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                fragment, R.id.userContentFrame);
    }
}
