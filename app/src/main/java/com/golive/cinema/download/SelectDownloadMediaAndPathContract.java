package com.golive.cinema.download;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.IBaseView;
import com.golive.cinema.download.domain.model.DownloadMedia;
import com.golive.cinema.download.domain.model.DownloadStorage;

import java.util.List;

/**
 * Created by Wangzj on 2016/11/24.
 */

public interface SelectDownloadMediaAndPathContract {

    interface View extends IBaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void showDownloadMedias(List<DownloadMedia> downloadMedias);

        void showDownloadStorages(List<DownloadStorage> downloadStorages);

        void showAvailableStoragesCount(int count);

        void clearStorageDevices();
    }

    interface Presenter extends IBasePresenter<View> {
        void showMedias();

        void selectMedia(String mediaId);
    }
}
