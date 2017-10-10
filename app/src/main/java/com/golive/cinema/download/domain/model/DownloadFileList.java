package com.golive.cinema.download.domain.model;

import java.io.Serializable;

public class DownloadFileList implements Serializable, Cloneable {
    public String mFileUrl;
    public long mFileSize;
    public long mCompleteSize;

    public String mMd5FileMd5; //md5文件的md5
    public String mMd5FileUrl;
    public long mMd5FileSize;
    public long mMd5CompleteSize;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
