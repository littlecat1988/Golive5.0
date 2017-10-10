package com.golive.cinema;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.initialjie.log.Logger;

public class MountReceiver extends BroadcastReceiver {

    public enum MountState {
        Mount, UnMount, Removed, BadRemove, EJECT;

        public static boolean isRemove(MountState state) {
            return state != null
                    && (MountState.UnMount == state || MountState.BadRemove == state
                    || MountState.Removed == state || MountState.EJECT == state);
        }
    }

    public static interface OnMountStateChangeListener {
        void onMountStateChange(String path, MountState state);
    }

    private final IntentFilter mFilter;
    private final OnMountStateChangeListener onMountListener;

    public MountReceiver(OnMountStateChangeListener onMountListener) {
        mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        mFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        mFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        mFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        mFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        mFilter.addDataScheme("file");
        this.onMountListener = onMountListener;
    }

    public void registerReceiver(Context context) {
        if (context != null) {
            context.registerReceiver(this, mFilter);
        }
    }

    public void unRegisterReceiver(Context context) {
        if (context != null) {
            context.unregisterReceiver(this);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Logger.d("SDCardBroadCastReceiver, onReceive");
        String action = intent.getAction();

        switch (action) {
            case Intent.ACTION_MEDIA_MOUNTED: { // 设备挂载
                String path = intent.getData().getPath();

                Logger.d(
                        "SDCardBroadCastReceiver, ACTION_MEDIA_MOUNTED, sd path : "
                                + path);

                if (onMountListener != null) {
                    onMountListener.onMountStateChange(path, MountState.Mount);
                }
                break;
            }
            case Intent.ACTION_MEDIA_EJECT: { // 设备卸载
                String path = intent.getData().getPath();
                Logger.d(
                        "SDCardBroadCastReceiver, ACTION_MEDIA_EJECT, sd path : "
                                + path);
                if (onMountListener != null) {
                    onMountListener.onMountStateChange(path, MountState.EJECT);
                }
                break;
            }
            case Intent.ACTION_MEDIA_UNMOUNTED: { // 设备卸载
                String path = intent.getData().getPath();
                Logger.d(
                        "SDCardBroadCastReceiver, ACTION_MEDIA_UNMOUNTED, sd path : "
                                + path);

                if (onMountListener != null) {
                    onMountListener.onMountStateChange(path, MountState.UnMount);
                }
                break;
            }
            case Intent.ACTION_MEDIA_REMOVED: { // 设备从卡槽移除
                String path = intent.getData().getPath();
                Logger.d(
                        "SDCardBroadCastReceiver, ACTION_MEDIA_REMOVED, sd path : "
                                + path);

                if (onMountListener != null) {
                    onMountListener.onMountStateChange(path, MountState.Removed);
                }
                break;
            }
            case Intent.ACTION_MEDIA_BAD_REMOVAL: { // 设备强行移除
                String path = intent.getData().getPath();
                Logger.d(
                        "SDCardBroadCastReceiver, ACTION_MEDIA_BAD_REMOVAL, sd path : "
                                + path);

                if (onMountListener != null) {
                    onMountListener.onMountStateChange(path, MountState.BadRemove);
                }
                break;
            }
        }

    }

}
