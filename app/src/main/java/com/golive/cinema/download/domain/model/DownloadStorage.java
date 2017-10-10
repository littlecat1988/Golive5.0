package com.golive.cinema.download.domain.model;

/**
 * Created by Wangzj on 2017/6/5.
 */

public class DownloadStorage {
    public final String deviceName;
    public final String path;
    public final long availableCapacity;
    public final boolean isCapacityEnough;
    public final long mediaSize;
    public final long completeSize;
    public final boolean isRecommend;

    public DownloadStorage(String deviceName, String path, long availableCapacity,
            boolean isCapacityEnough, long mediaSize, long completeSize, boolean isRecommend) {
        this.deviceName = deviceName;
        this.path = path;
        this.availableCapacity = availableCapacity;
        this.isCapacityEnough = isCapacityEnough;
        this.mediaSize = mediaSize;
        this.completeSize = completeSize;
        this.isRecommend = isRecommend;
    }
}
