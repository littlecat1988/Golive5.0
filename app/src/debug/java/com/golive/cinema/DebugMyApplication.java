package com.golive.cinema;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by Wangzj on 2017/1/4.
 */

public class DebugMyApplication extends MyApplication {
    private static final String TAG = DebugMyApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        long startTime = SystemClock.elapsedRealtime();
        initializeStetho(this);
        long elapsed = SystemClock.elapsedRealtime() - startTime;
        Log.i(TAG, "Stetho initialized in " + elapsed + " ms");
    }

    @Override
    protected RefWatcher installLeakCanary() {
        // Build a customized RefWatcher
        return LeakCanary.refWatcher(this)
//                .watchDelay(10, TimeUnit.SECONDS)
                .listenerServiceClass(LeakUploadService.class)
                .buildAndInstall();
    }

    private void initializeStetho(final Context context) {
        Stetho.initializeWithDefaults(context);
    }
}
