package com.golive.cinema.user.custom;

import android.content.Context;
import android.os.Bundle;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.util.ActivityUtils;

public class CustomActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_center_base);

        CustomFragment mFragment = (CustomFragment) getSupportFragmentManager().findFragmentById(
                R.id.userContentFrame);
        if (null == mFragment) {
            // Create the fragment
            mFragment = CustomFragment.newInstance();
            Context context = getApplicationContext();
            new CustomPresenter(mFragment,
                    Injection.provideGetClientServiceUseCase(context),
                    Injection.provideGetMainConfigUseCase(context),
                    Injection.provideGetKdmVersionUseCase(context));
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mFragment,
                    R.id.userContentFrame);
        }
    }
}
