package com.golive.cinema.download;

import static com.golive.cinema.util.Preconditions.checkNotNull;
import static com.golive.cinema.util.StorageUtils.getStorageList;

import android.support.annotation.NonNull;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.download.domain.model.BackupDownloadInfo;
import com.golive.cinema.download.domain.model.DownloadFileList;
import com.golive.cinema.download.domain.model.DownloadMedia;
import com.golive.cinema.download.domain.model.DownloadStorage;
import com.golive.cinema.util.StorageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.Media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Wangzj on 2016/11/24.
 */

public class SelectDownloadMediaAndPathPresenter extends
        BasePresenter<SelectDownloadMediaAndPathContract.View> implements
        SelectDownloadMediaAndPathContract.Presenter {

    private final String mFilmId;
    private List<Media> mMedias;

    private String mMediaId;
    private long mMediaSize;
    private boolean mOnlySelectPath;

    public SelectDownloadMediaAndPathPresenter(@NonNull SelectDownloadMediaAndPathContract.View
            view, @NonNull String filmId, @NonNull List<Media> medias) {
        mFilmId = checkNotNull(filmId, "filmId cannot be null!");
        mMedias = checkNotNull(medias, "medias cannot be null!");
        attachView(checkNotNull(view, "filmDetailView cannot be null!"));
        view.setPresenter(this);
    }

    public SelectDownloadMediaAndPathPresenter(@NonNull SelectDownloadMediaAndPathContract.View
            view, @NonNull String filmId, @NonNull String mediaId, long mediaSize) {
        mOnlySelectPath = true;
        mFilmId = checkNotNull(filmId, "filmId cannot be null!");
        mMediaId = checkNotNull(mediaId, "mediaId cannot be null!");
        mMediaSize = mediaSize;
        attachView(checkNotNull(view, "filmDetailView cannot be null!"));
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();
        if (mOnlySelectPath) {
            selectMedia(mMediaId);
        } else {
            showMedias();
        }
    }

    @Override
    public void selectMedia(String mediaId) {
        _selectMedia(mediaId);
    }

    @Override
    public void showMedias() {
        SelectDownloadMediaAndPathContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

        if (null == mMedias || mMedias.isEmpty()) {
            return;
        }

        List<Media> tmpMedias = new LinkedList<>(mMedias);
        Collections.sort(tmpMedias, new Comparator<Media>() {
            @Override
            public int compare(Media lhs, Media rhs) {
                long lMediaSize = 0;
                long rMediaSize = 0;
                if (!StringUtils.isNullOrEmpty(lhs.getSize())) {
                    try {
                        lMediaSize = Long.parseLong(lhs.getSize());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                if (!StringUtils.isNullOrEmpty(rhs.getSize())) {
                    try {
                        rMediaSize = Long.parseLong(rhs.getSize());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                return (int) (lMediaSize - rMediaSize);
            }
        });

        int size = tmpMedias.size();
        List<DownloadMedia> downloadMedias = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            Media media = tmpMedias.get(i);
            long mediaSize = 0;
            if (!StringUtils.isNullOrEmpty(media.getSize())) {
                try {
                    mediaSize = Long.parseLong(media.getSize());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            // choose the minimized media to recommend
            boolean recommend = 0 == i;

            downloadMedias.add(new DownloadMedia(media.getId(), media.getRankname(), mediaSize, -1,
                    recommend));
        }

        // show medias
        view.showDownloadMedias(downloadMedias);
    }

    private void _selectMedia(String mediaId) {
        SelectDownloadMediaAndPathContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

        view.clearStorageDevices();

        List<StorageUtils.StorageInfo> storageInfos = getStorageList();
        // no storage
        if (null == storageInfos || storageInfos.isEmpty()) {
            return;
        }

        List<StorageUtils.StorageInfo> finishStorages = new ArrayList<>();
        List<StorageUtils.StorageInfo> capacityEnoughStorages = new ArrayList<>();
        Map<String, StorageUtils.StorageInfo> alreadyShownDevices = new HashMap<>();

        // get backup download
        BackupDownloadInfo backupDownloadInfo = DownloadUtils.getBackupDownloads(mFilmId, mediaId);

        // has backup download
        if (backupDownloadInfo != null && backupDownloadInfo.getStorageInfoList() != null
                && !backupDownloadInfo.getStorageInfoList().isEmpty()) {
            List<List<DownloadFileList>> downloadFileList =
                    backupDownloadInfo.getDownloadFileList();
            for (int i = 0; i < downloadFileList.size(); i++) {
                List<DownloadFileList> downloadFileLists = downloadFileList.get(i);
                long completeSize = 0;
                long totalSize = 0;
                for (DownloadFileList downloadFile : downloadFileLists) {
                    completeSize += downloadFile.mCompleteSize + downloadFile.mMd5CompleteSize;
                    totalSize += downloadFile.mFileSize + downloadFile.mMd5FileSize;
                }
                StorageUtils.StorageInfo storageInfo =
                        backupDownloadInfo.getStorageInfoList().get(i);
                long needSize = totalSize - completeSize;
                // download finish
                if (0 >= needSize) {
                    finishStorages.add(storageInfo);
                } else {
                    long availableCapacity = StorageUtils.getAvailableCapacity(storageInfo.path);
                    if (availableCapacity >= needSize) {
                        capacityEnoughStorages.add(storageInfo);
                    } else {
//                            otherStorages.add(storageInfo);
                    }
                }
            }
        }

        boolean recommend = false;

        int availableCount = 0;

        List<DownloadStorage> downloadStorages  = new ArrayList<>();

        // finish storages
        for (StorageUtils.StorageInfo storage : finishStorages) {
            String path = storage.path;
            long capacity = StorageUtils.getAvailableCapacity(path);
            // show Storage Device
//            view.showStorageDevice(storage.device, path, capacity, true, 0, 0, recommend);
            alreadyShownDevices.put(path, storage);
            availableCount++;

            downloadStorages.add(new DownloadStorage(storage.device, path, capacity, true, 0, 0, recommend));
        }

        // capacity enough storages
        for (StorageUtils.StorageInfo storage : capacityEnoughStorages) {
            String path = storage.path;
            long capacity = StorageUtils.getAvailableCapacity(path);
            // show Storage Device
//            view.showStorageDevice(storage.device, path, capacity, true, 0, 0, recommend);
            alreadyShownDevices.put(path, storage);
            availableCount++;

            downloadStorages.add(new DownloadStorage(storage.device, path, capacity, true, 0, 0, recommend));
        }

        // no available Storage || Storage Device counts < 2
        if (alreadyShownDevices.size() < 2) {
            long mediaSize = 0;
            if (mOnlySelectPath) {
                mediaSize = mMediaSize;
            } else {
                Media media = filterMedia(mMedias, mediaId);
                if (media != null) {
                    String size = media.getSize();
                    if (!StringUtils.isNullOrEmpty(size)) {
                        try {
                            mediaSize = Long.parseLong(size);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // sort storages by capacity
            Collections.sort(storageInfos, new Comparator<StorageUtils.StorageInfo>() {
                @Override
                public int compare(StorageUtils.StorageInfo lhs, StorageUtils.StorageInfo rhs) {
                    long lCapacity = StorageUtils.getAvailableCapacity(lhs.path);
                    long rCapacity = StorageUtils.getAvailableCapacity(rhs.path);
                    // reverseOrder
                    return Long.valueOf(rCapacity).compareTo(lCapacity);
                }
            });

            // try show other Storage Device
            for (StorageUtils.StorageInfo storage : storageInfos) {
                // already show more than 2 storages
                if (alreadyShownDevices.size() >= 2) {
                    break;
                }

                String path = storage.path;
                // already show this device
                if (alreadyShownDevices.containsKey(path)) {
                    continue;
                }

                long capacity = StorageUtils.getAvailableCapacity(path);
                // show Storage Device
                boolean isCapacityEnough = capacity >= mediaSize;
//                view.showStorageDevice(storage.device, storage.path, capacity, isCapacityEnough, 0,
//                        0, false);
                alreadyShownDevices.put(storage.path, storage);
                if (isCapacityEnough) {
                    availableCount++;
                }

                downloadStorages.add(new DownloadStorage(storage.device, storage.path, capacity, isCapacityEnough, 0,
                        0, false));
            }
        }

        view.showDownloadStorages(downloadStorages);
        view.showAvailableStoragesCount(availableCount);
    }

    private Media filterMedia(@NonNull final List<Media> mediaList, @NonNull final String mediaId) {
        checkNotNull(mediaList);
        checkNotNull(mediaId);
        for (Media media : mediaList) {
            String id = media.getId();
            if (!StringUtils.isNullOrEmpty(id) && mediaId.equals(id)) {
                return media;
            }
        }
        return null;
    }
}
