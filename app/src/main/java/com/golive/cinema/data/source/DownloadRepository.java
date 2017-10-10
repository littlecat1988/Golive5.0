package com.golive.cinema.data.source;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.download.DownloadManager;
import com.golive.network.net.GoLiveRestApi;
import com.initialjie.download.aidl.DownloadTaskInfo;
import com.initialjie.log.Logger;

import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by Wangzj on 2016/10/27.
 */

public class DownloadRepository implements DownloadDataSource {

    private static DownloadRepository INSTANCE = null;

    private final GoLiveRestApi mGoLiveRestApi;
    private final DownloadManager mDownloadManager;

    public static DownloadRepository getInstance(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull DownloadManager downloadManager) {
        if (null == INSTANCE) {
            synchronized (DownloadRepository.class) {
                if (null == INSTANCE) {
                    INSTANCE = new DownloadRepository(goLiveRestApi, downloadManager);
                }
            }
        }
        return INSTANCE;
    }

    public static synchronized void destroyInstance() {
        if (INSTANCE != null) {
            INSTANCE.unInit();
            INSTANCE = null;
        }
    }

    private DownloadRepository(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull DownloadManager downloadManager) {
        mGoLiveRestApi = checkNotNull(goLiveRestApi);
        mDownloadManager = checkNotNull(downloadManager);
        init();
    }

    private void init() {
        Logger.d("init");
    }

    private void unInit() {
        Logger.d("unInit");
        mDownloadManager.pauseAllDownloadTask();
        DownloadManager.destroyInstance();
    }

    @Override
    public Observable<ResponseBody> downloadFile(long start, @NonNull String url) {
        return mGoLiveRestApi.download(start, url);
    }

    @Override
    public Observable<DownloadTaskInfo> getDownloadTaskInfo(@NonNull final String filmId,
            @NonNull final String mediaId) {
        checkNotNull(filmId);
        checkNotNull(mediaId);
        Observable.OnSubscribe<DownloadTaskInfo> onSubscribe =
                new Observable.OnSubscribe<DownloadTaskInfo>() {
                    @Override
                    public void call(Subscriber<? super DownloadTaskInfo> subscriber) {
                        DownloadTaskInfo taskInfo = mDownloadManager.getDownloadTaskInfo(filmId,
                                mediaId);
                        subscriber.onNext(taskInfo);
                        subscriber.onCompleted();
                    }
                };

        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<List<String>> getAllDownloadingTaskIds() {
        Observable.OnSubscribe<List<String>> onSubscribe =
                new Observable.OnSubscribe<List<String>>() {
                    @Override
                    public void call(Subscriber<? super List<String>> subscriber) {
                        List<String> idList = mDownloadManager.getAllDownloadingTaskIds();
                        subscriber.onNext(idList);
                        subscriber.onCompleted();
                    }
                };

        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<Boolean> addDownloadTask(@NonNull final String filmId,
            @NonNull final String mediaId, @NonNull final String savePath, final String reserve,
            @NonNull final String[] urls, @NonNull final long[] fileSizes) {
        Logger.d(
                "addDownloadTask, filmId : " + filmId + ", mediaId : " + mediaId + ", savePath : "
                        + savePath);
        checkNotNull(filmId);
        checkNotNull(mediaId);
        checkNotNull(savePath);
        checkNotNull(urls);
        checkNotNull(fileSizes);
        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean success = mDownloadManager.addDownloadTask(filmId, mediaId, savePath,
                        reserve, urls, fileSizes);
                subscriber.onNext(success);
                subscriber.onCompleted();
            }
        };

        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<Boolean> resumeDownloadTask(@NonNull final String filmId,
            @NonNull final String mediaId) {
        Logger.d("resumeDownloadTask, filmId : " + filmId + ", mediaId : " + mediaId);
        checkNotNull(filmId);
        checkNotNull(mediaId);
        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean success = mDownloadManager.resumeDownloadTask(filmId, mediaId);
                subscriber.onNext(success);
                subscriber.onCompleted();
            }
        };

        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<Boolean> pauseDownloadTask(@NonNull final String filmId,
            @NonNull final String mediaId) {
        Logger.d("pauseDownloadTask, filmId : " + filmId + ", mediaId : " + mediaId);
        checkNotNull(filmId);
        checkNotNull(mediaId);
        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean success = mDownloadManager.pauseDownloadTask(filmId, mediaId);
                subscriber.onNext(success);
                subscriber.onCompleted();
            }
        };

        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<Void> pauseAllDownloadTask() {
        Observable.OnSubscribe<Void> onSubscribe = new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                mDownloadManager.pauseAllDownloadTask();
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<Boolean> deleteDownloadTask(@NonNull final String filmId,
            @NonNull final String mediaId, final boolean deleteFiles) {
        checkNotNull(filmId);
        checkNotNull(mediaId);
        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean success = mDownloadManager.deleteDownloadTask(filmId, mediaId, deleteFiles);
                subscriber.onNext(success);
                subscriber.onCompleted();
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<Boolean> isDownloadFinished(@NonNull final String filmId,
            @NonNull final String mediaId) {
        checkNotNull(filmId);
        checkNotNull(mediaId);
        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                boolean success = mDownloadManager.isDownloadFinish(filmId, mediaId);
                subscriber.onNext(success);
                subscriber.onCompleted();
            }
        };
        return Observable.create(onSubscribe);
    }
}
