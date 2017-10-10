package com.golive.cinema.download.domain.model;

import com.golive.cinema.util.StorageUtils;

import java.util.List;

/**
 * Created by Wangzj on 2016/11/23.
 */

public class BackupDownloadInfo {
    private final List<StorageUtils.StorageInfo> mStorageInfoList;
    private final List<List<DownloadFileList>> mDownloadFileList;

    public BackupDownloadInfo(List<StorageUtils.StorageInfo> storageInfoList,
            List<List<DownloadFileList>> downloadFileList) {
        mStorageInfoList = storageInfoList;
        mDownloadFileList = downloadFileList;
    }

    public List<StorageUtils.StorageInfo> getStorageInfoList() {
        return mStorageInfoList;
    }

    public List<List<DownloadFileList>> getDownloadFileList() {
        return mDownloadFileList;
    }
}
