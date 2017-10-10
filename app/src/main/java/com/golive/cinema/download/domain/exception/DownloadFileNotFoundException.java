package com.golive.cinema.download.domain.exception;

import java.io.FileNotFoundException;

/**
 * Created by Wangzj on 2016/10/28.
 */

public class DownloadFileNotFoundException extends FileNotFoundException {
    private final String mFilmId;
    private final String mMediaId;
    private final String mFilePath;

    public DownloadFileNotFoundException(String filmId, String mediaId, String filePath) {
        mFilmId = filmId;
        mMediaId = mediaId;
        mFilePath = filePath;
    }

    public DownloadFileNotFoundException(String detailMessage, String filmId, String mediaId,
            String filePath) {
        super(detailMessage);
        mFilmId = filmId;
        mMediaId = mediaId;
        mFilePath = filePath;
    }

    public String getFilmId() {
        return mFilmId;
    }

    public String getMediaId() {
        return mMediaId;
    }

    public String getFilePath() {
        return mFilePath;
    }
}
