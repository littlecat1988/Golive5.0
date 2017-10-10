package com.golive.cinema.download.domain.model;

/**
 * Created by Wangzj on 2017/6/5.
 */

public class DownloadMedia {
    public final String mediaId;
    public final String mediaSharpness;
    public final long mediaSize;
    public final long estimatedDownloadTime;
    public final boolean isRecommend;

    public DownloadMedia(String mediaId, String mediaSharpness, long mediaSize,
            long estimatedDownloadTime, boolean isRecommend) {
        this.mediaId = mediaId;
        this.mediaSharpness = mediaSharpness;
        this.mediaSize = mediaSize;
        this.estimatedDownloadTime = estimatedDownloadTime;
        this.isRecommend = isRecommend;
    }
}
