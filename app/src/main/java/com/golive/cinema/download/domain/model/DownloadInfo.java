package com.golive.cinema.download.domain.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Wangzj on 2016/11/23.
 */

public class DownloadInfo implements Serializable {
    public String mUrl;
    public ArrayList<DownloadFileList> mDownloadFileLists;
}
