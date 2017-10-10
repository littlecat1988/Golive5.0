package com.golive.cinema.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.BaseActivity;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.util.ActivityUtils;

/**
 * Created by Wangzj on 2016/8/22.
 */

public class LoginActivity extends BaseActivity {

    private LoginPresenter mLoginPresenter;

    public static void navigateToLoginActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_act);

        LoginFragment loginFragment =
                (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (null == loginFragment) {
            // Create the fragment
            loginFragment = LoginFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), loginFragment,
                    R.id.contentFrame);
        }

        // Create the presenter
        mLoginPresenter =
                new LoginPresenter(loginFragment, Injection.provideLogin(getApplicationContext()));
    }
}
