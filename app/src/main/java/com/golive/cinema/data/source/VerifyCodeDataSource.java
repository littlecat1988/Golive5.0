package com.golive.cinema.data.source;


import com.golive.network.entity.Response;

import rx.Observable;

/**
 * Created by chgang on 2016/11/9.
 */

public interface VerifyCodeDataSource {

    Observable<Response> getVerifyCode(String phone);

}
