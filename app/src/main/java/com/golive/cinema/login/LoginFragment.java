package com.golive.cinema.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.Login;
import com.hwangjr.rxbus.RxBus;

/**
 * Created by Wangzj on 2016/8/22.
 */
public class LoginFragment extends MvpFragment implements LoginContract.View {

    private ProgressDialog mProgressDialog;
    private LoginContract.Presenter mPresenter;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_frag, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getPresenter() != null) {
            getPresenter().start();
        }
    }

    @Override
    public void setLoadingIndicator(boolean active) {

        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mProgressDialog) {
                mProgressDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.login_please_wait));
            }

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } else {
            UIHelper.dismissDialog(mProgressDialog);
        }
    }

    @Override
    public void showLoginFailed() {
        String string = getString(R.string.login_failed);
        Toast.makeText(getContext(), string, Toast.LENGTH_SHORT).show();

        RxBus.get().post(LoginEvent.TAG_LOGIN_FAILED, string);
    }

    @Override
    public void showLogin(Login login) {
        Toast.makeText(getContext(), getString(R.string.login_success), Toast.LENGTH_SHORT).show();

        RxBus.get().post(LoginEvent.TAG_LOGIN_SUCCESS, new LoginEvent(login));

        Intent intent = new Intent();
        intent.putExtra(LoginEvent.KEY_LOGIN_USERID, login.getUserid());
        intent.putExtra(LoginEvent.KEY_LOGIN_LEVEL, login.getUserlevel());
        intent.putExtra(LoginEvent.KEY_LOGIN_STATUS, login.getStatus());
        getActivity().setResult(Activity.RESULT_OK, intent);

        // finish
        getActivity().finish();
    }

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    @Override
    protected LoginContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }
}
