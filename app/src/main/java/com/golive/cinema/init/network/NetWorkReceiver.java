package com.golive.cinema.init.network;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import com.golive.cinema.util.NetworkUtils;
import com.initialjie.log.Logger;


/**
 * Created by chgang on 2016/11/22.
 */

public class NetWorkReceiver extends BroadcastReceiver {

    public interface NetworkCallback {
        void callNetworkStatus(Boolean status);
    }

    private static final IntentFilter sIntentFilter = new IntentFilter(
            ConnectivityManager.CONNECTIVITY_ACTION);

    private boolean mNetworkStatus;
    private NetworkCallback mNetworkCallback = null;

    public NetWorkReceiver registerReceiver(@NonNull Context context) {
        checkNotNull(context);
        context.registerReceiver(this, sIntentFilter);
        return this;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            ConnectivityManager manager = NetworkUtils.getConnectivityManager(context);
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                String name = info.getTypeName();
                Logger.d("onReceive, 当前网络名称：" + name);
                mNetworkStatus = true;
            } else {
                mNetworkStatus = false;
            }
            if (mNetworkCallback != null) {
                mNetworkCallback.callNetworkStatus(isNetworkStatus());
            }
        } catch (Exception e) {
            Logger.e(e, "onReceive check network error : ");
            e.printStackTrace();
        }
    }

    public boolean isNetworkStatus() {
        return mNetworkStatus;
    }

    public NetWorkReceiver setNetworkCallback(NetworkCallback networkCallback) {
        mNetworkCallback = networkCallback;
        return this;
    }
}
