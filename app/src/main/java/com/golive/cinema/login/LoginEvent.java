package com.golive.cinema.login;

import com.golive.network.entity.Login;

/**
 * Created by Wangzj on 2016/8/24.
 */

public class LoginEvent {

    public final static String TAG_LOGIN_SUCCESS = "tag_login_success";
    public final static String TAG_LOGIN_FAILED = "login_failed";

    public final static String KEY_LOGIN_USERID = "key_login_userid";
    public final static String KEY_LOGIN_LEVEL = "key_login_level";
    public final static String KEY_LOGIN_STATUS = "key_login_status";

    private final Login mLogin;

    public LoginEvent(Login mLogin) {
        this.mLogin = mLogin;
    }

    public Login getLogin() {
        return mLogin;
    }
}
