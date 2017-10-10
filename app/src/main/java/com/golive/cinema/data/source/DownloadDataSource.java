package com.golive.cinema.data.source;

import android.support.annotation.NonNull;

import com.initialjie.download.aidl.DownloadTaskInfo;

import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;

/**
 * Created by Wangzj on 2016/10/27.
 */

public interface DownloadDataSource {

    Observable<ResponseBody> downloadFile(long start, @NonNull String url);

    Observable<DownloadTaskInfo> getDownloadTaskInfo(@NonNull String filmId,
            @NonNull String mediaId);

    Observable<List<String>> getAllDownloadingTaskIds();

    Observable<Boolean> addDownloadTask(@NonNull String filmId, @NonNull String mediaId,
            @NonNull String savePath, String reserve, @NonNull String[] urls, long[] fileSizes);

    Observable<Boolean> resumeDownloadTask(@NonNull String filmId, @NonNull String mediaId);

    Observable<Boolean> pauseDownloadTask(@NonNull String filmId, @NonNull String mediaId);

    Observable<Void> pauseAllDownloadTask();

    Observable<Boolean> deleteDownloadTask(@NonNull String filmId, @NonNull String mediaId,
            boolean deleteFiles);

    Observable<Boolean> isDownloadFinished(@NonNull final String filmId,
            @NonNull final String mediaId);
}
