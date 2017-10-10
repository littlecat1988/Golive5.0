package com.golive.cinema.user.setting;

import android.os.Bundle;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.R;
import com.golive.cinema.util.ActivityUtils;

public class SettingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_center_base);

        ActivityUtils.addFragmentToActivity(getSupportFragmentManager(),
                SettingFragment.newInstance(), R.id.userContentFrame);
    }
}
