package com.golive.cinema.data.source;

import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Created by Wangzj on 2017/2/22.
 */

public interface HttpDataSource {

    Observable<ResponseBody> get(String url);
}
