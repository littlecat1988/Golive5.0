package com.golive.cinema.download;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.cinema.download.domain.model.DownloadFileList;
import com.golive.cinema.util.StorageUtils;

import java.util.List;

import rx.Observable;

/**
 * Created by Wangzj on 2016/11/29.
 */

public interface DownloadContract {
    interface View extends IBaseView<Presenter> {

        void setDownloadingIndicator(boolean active);

        void showDownloadError(String errMsg);

        void showNoStorageDevice();


        Observable<Boolean> showFileNotExist(String filePath);

        Observable<String> showSelectNewDownloadPath(final List<StorageUtils.StorageInfo> devices,
                final long fileSize);

        Observable<Boolean> showConfirmRestoreBackupDownload(
                final List<StorageUtils.StorageInfo> bakDevices,
                final List<List<DownloadFileList>> downloadFileList);

        Observable<String> showSelectRestoreBackupDownload(
                final List<StorageUtils.StorageInfo> bakDevices,
                final List<List<DownloadFileList>> downloadFileList);
    }

    interface Presenter extends IBasePresenter<View> {
        void download(boolean reDownload);
    }
}
