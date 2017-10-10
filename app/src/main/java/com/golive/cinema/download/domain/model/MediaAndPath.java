package com.golive.cinema.download.domain.model;

/**
 * Created by Wangzj on 2016/11/23.
 */

public class MediaAndPath {
    private final String mMediaId;
    private final String mPath;

    public MediaAndPath(String mediaId, String path) {
        mMediaId = mediaId;
        mPath = path;
    }

    public String getMediaId() {
        return mMediaId;
    }

    public String getPath() {
        return mPath;
    }
}
