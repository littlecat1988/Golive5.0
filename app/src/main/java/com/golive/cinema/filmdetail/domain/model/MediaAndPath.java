package com.golive.cinema.filmdetail.domain.model;

import com.golive.network.entity.Media;

/**
 * Created by Wangzj on 2016/11/23.
 */

public class MediaAndPath {
    private final Media mMedia;
    private final String mPath;

    public MediaAndPath(Media media, String path) {
        mMedia = media;
        mPath = path;
    }

    public Media getMedia() {
        return mMedia;
    }

    public String getPath() {
        return mPath;
    }
}
