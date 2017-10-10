package com.golive.cinema.data.source.remote;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.data.source.HttpDataSource;
import com.golive.network.net.GoLiveRestApi;

import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Created by Wangzj on 2017/2/22.
 */

public class HttpRepository implements HttpDataSource {

    private static HttpRepository INSTANCE;

    private final GoLiveRestApi mGoLiveRestApi;

    public static HttpRepository getInstance(@NonNull GoLiveRestApi goLiveRestApi) {
        checkNotNull(goLiveRestApi);
        if (null == INSTANCE) {
            INSTANCE = new HttpRepository(goLiveRestApi);
        }
        return INSTANCE;
    }

    private HttpRepository(GoLiveRestApi goLiveRestApi) {
        mGoLiveRestApi = goLiveRestApi;
    }

    @Override
    public Observable<ResponseBody> get(String url) {
        return mGoLiveRestApi.get(url);
    }
}
