package com.golive.cinema.download.domain.exception;

import java.io.FileNotFoundException;

/**
 * Created by Wangzj on 2016/10/28.
 */

public class DownloadTaskFoundException extends FileNotFoundException {
    private final String mFilmId;
    private final String mMediaId;

    public DownloadTaskFoundException(String filmId, String mediaId) {
        mFilmId = filmId;
        mMediaId = mediaId;
    }

    public DownloadTaskFoundException(String detailMessage, String filmId, String mediaId) {
        super(detailMessage);
        mFilmId = filmId;
        mMediaId = mediaId;
    }

    public String getFilmId() {
        return mFilmId;
    }

    public String getMediaId() {
        return mMediaId;
    }
}
