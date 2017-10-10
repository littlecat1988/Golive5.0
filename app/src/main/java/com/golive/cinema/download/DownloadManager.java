package com.golive.cinema.download;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;

import com.golive.cinema.util.StringUtils;
import com.initialjie.download.aidl.DownloadTaskInfo;
import com.initialjie.download.aidl.IDownloadService;
import com.initialjie.download.service.DownloadService;
import com.initialjie.log.Logger;

import java.util.List;

/**
 * Created by Wangzj on 2016/11/22.
 */

public class DownloadManager {
    private static final int MAX_SYNC_TRY_TIMES = 50;
    private static final int SYNC_WAIT_TIMES = 50;
    private static DownloadManager INSTANCE = null;
    private final Object mWaitObj = new Object();

    public static DownloadManager getInstance(@NonNull Context context) {
        if (null == INSTANCE) {
            synchronized (DownloadManager.class) {
                if (null == INSTANCE) {
                    INSTANCE = new DownloadManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE.unInit();
        INSTANCE = null;
    }


    private final Context mContext;
    private final ServiceConnection mServiceConnection;
    private IDownloadService mIDownloadService;

    private DownloadManager(@NonNull Context context) {
        // we need application context otherwise it may raise a memory leak.
        mContext = checkNotNull(context).getApplicationContext();
        mServiceConnection = new DownloadServiceConnection();
        init();
    }

    private void init() {
        Logger.d("init begin");
        bindDownloadService();
        Logger.d("init ok");
    }

    private void unInit() {
        Logger.d("unInit");
        pauseAllDownloadTask();
        unBindDownloadService();
        mContext.stopService(new Intent(mContext, DownloadService.class));
    }

    public DownloadTaskInfo getDownloadTaskInfo(@NonNull final String filmId,
            @NonNull final String mediaId) {
        checkNotNull(filmId);
        checkNotNull(mediaId);
        DownloadTaskInfo taskInfo = null;
        String downloadId = DownloadUtils.getDownloadId(filmId, mediaId);
        checkBindDownloadService();
        if (null != getIDownloadService()) {
            try {
                taskInfo = getIDownloadService().getDownloadTaskInfo(downloadId,
                        true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return taskInfo;
    }

    public List<String> getAllDownloadingTaskIds() {
        List<String> idList = null;
        checkBindDownloadService();
        if (null != getIDownloadService()) {
            try {
                idList = getIDownloadService().getAllDownloadingTaskIds();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return idList;
    }

    public boolean addDownloadTask(@NonNull final String filmId, @NonNull final String mediaId,
            @NonNull final String savePath, final String reserve, @NonNull final String[] urls,
            @NonNull final long[] fileSizes) {
        Logger.d(
                "addDownloadTask, filmId : " + filmId + ", mediaId : " + mediaId + ", savePath : "
                        + savePath);
        checkNotNull(filmId);
        checkNotNull(mediaId);
        checkNotNull(savePath);
        checkNotNull(urls);
        checkNotNull(fileSizes);
        String result = null;
        String downloadId = DownloadUtils.getDownloadId(filmId, mediaId);
        checkBindDownloadService();
        if (null != getIDownloadService()) {
            try {
                int downloadTaskType = 0;
                result = getIDownloadService().addByUrl(downloadId, downloadTaskType,
                        savePath, reserve, urls, fileSizes);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return !StringUtils.isNullOrEmpty(result);
    }

    public boolean resumeDownloadTask(@NonNull final String filmId,
            @NonNull final String mediaId) {
        Logger.d("resumeDownloadTask, filmId : " + filmId + ", mediaId : " + mediaId);
        checkNotNull(filmId);
        checkNotNull(mediaId);
        boolean success = false;
        String downloadId = DownloadUtils.getDownloadId(filmId, mediaId);
        checkBindDownloadService();
        if (null != getIDownloadService()) {
            try {
                success = getIDownloadService().resume(downloadId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public boolean pauseDownloadTask(@NonNull final String filmId,
            @NonNull final String mediaId) {
        Logger.d("pauseDownloadTask, filmId : " + filmId + ", mediaId : " + mediaId);
        checkNotNull(filmId);
        checkNotNull(mediaId);
        boolean success = false;
        String downloadId = DownloadUtils.getDownloadId(filmId, mediaId);
        checkBindDownloadService();
        if (null != getIDownloadService()) {
            try {
                success = getIDownloadService().pause(downloadId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public void pauseAllDownloadTask() {
        checkBindDownloadService();
        if (null != getIDownloadService()) {
            try {
                getIDownloadService().pauseAllTask();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean deleteDownloadTask(@NonNull final String filmId,
            @NonNull final String mediaId, final boolean deleteFiles) {
        Logger.d("deleteDownloadTask, filmId : " + filmId + ", mediaId : " + mediaId
                + ", deleteFiles : " + deleteFiles);
        checkNotNull(filmId);
        checkNotNull(mediaId);
        boolean success = false;
        String downloadId = DownloadUtils.getDownloadId(filmId, mediaId);
        checkBindDownloadService();
        if (null != getIDownloadService()) {
            try {
                success = getIDownloadService().remove(downloadId, deleteFiles);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return success;
    }


    /**
     * Check whether the download is finished truly by checking the files.
     *
     * @param filmId  film id
     * @param mediaId media id
     */
    public boolean isDownloadFinish(@NonNull final String filmId, @NonNull final String mediaId) {
        boolean isFinish = false;
        checkNotNull(filmId);
        checkNotNull(mediaId);
        String downloadId = DownloadUtils.getDownloadId(filmId, mediaId);
        checkBindDownloadService();
        if (null != getIDownloadService()) {
            try {
                isFinish = getIDownloadService().isFinish(downloadId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return isFinish;
    }

    private void checkBindDownloadService() {
        if (null == getIDownloadService()) {
            // bind to download service
            bindDownloadServiceSync();
        }
    }

    /**
     * bind to DownloadService
     */
    private synchronized void bindDownloadService() {
        Logger.d("bindDownloadService");
        Intent intent = new Intent(mContext, DownloadService.class);
        mContext.startService(intent);
        unBindDownloadService();
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * bind to DownloadService until DownloadService get bound or time out.
     */
    private synchronized void bindDownloadServiceSync() {
        Logger.d("bindDownloadServiceSync");
        bindDownloadService();
        int leftTime = MAX_SYNC_TRY_TIMES;
        try {
            while (null == getIDownloadService() && leftTime-- > 0) {
                synchronized (mWaitObj) {
                    mWaitObj.wait(SYNC_WAIT_TIMES);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * un-bind DownloadService
     */
    private synchronized void unBindDownloadService() {
        if (getIDownloadService() != null && mServiceConnection != null) {
            Logger.d("unBindDownloadService");
            setIDownloadService(null);
            mContext.unbindService(mServiceConnection);
        }
    }


    private synchronized IDownloadService getIDownloadService() {
        return mIDownloadService;
    }

    private synchronized void setIDownloadService(IDownloadService IDownloadService) {
        mIDownloadService = IDownloadService;
    }

    private class DownloadServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d("onServiceConnected");
            IDownloadService downloadService = IDownloadService.Stub.asInterface(service);
            setIDownloadService(downloadService);
            synchronized (mWaitObj) {
                mWaitObj.notifyAll();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d("onServiceDisconnected");
            setIDownloadService(null);
            synchronized (mWaitObj) {
                mWaitObj.notifyAll();
            }
        }
    }
}
