package com.golive.cinema.download.domain.exception;

/**
 * Created by Wangzj on 2016/10/28.
 */

public class DownloadNotFinishException extends Exception {
    private final String mFilmId;
    private final String mMediaId;

    public DownloadNotFinishException(String filmId, String mediaId) {
        mFilmId = filmId;
        mMediaId = mediaId;
    }

    public DownloadNotFinishException(String detailMessage, String filmId, String mediaId) {
        super(detailMessage);
        mFilmId = filmId;
        mMediaId = mediaId;
    }

    public DownloadNotFinishException(String detailMessage, Throwable throwable,
            String filmId, String mediaId) {
        super(detailMessage, throwable);
        mFilmId = filmId;
        mMediaId = mediaId;
    }

    public DownloadNotFinishException(Throwable throwable, String filmId, String mediaId) {
        super(throwable);
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
