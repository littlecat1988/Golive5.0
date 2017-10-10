package com.golive.cinema.download;

import static java.io.File.separator;

import android.content.res.Resources;

import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.download.domain.model.BackupDownloadInfo;
import com.golive.cinema.download.domain.model.DownloadFileList;
import com.golive.cinema.download.domain.model.DownloadInfo;
import com.golive.cinema.util.NetworkUtils;
import com.golive.cinema.util.SerializerHelper;
import com.golive.cinema.util.StorageUtils;
import com.golive.cinema.util.StringUtils;
import com.initialjie.download.DownloadConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangzj on 2016/11/22.
 */

public class DownloadUtils {

    private static final String SEPARATOR = "-^_^-";

    /**
     * Get download id
     *
     * @param filmID  Film id
     * @param mediaId Media id
     */
    public static String getDownloadId(String filmID, String mediaId) {
        if (!StringUtils.isNullOrEmpty(mediaId)) {

            return filmID + SEPARATOR + mediaId;
        } else {
            return filmID;
        }
    }

    /**
     * Get film id from download id
     *
     * @param downloadId download id
     */
    public static String getFilmIdFromDownloadId(String downloadId) {
        if (!StringUtils.isNullOrEmpty(downloadId)) {
            int index = downloadId.lastIndexOf(SEPARATOR);
            if (index >= 0) {
                return downloadId.substring(0, index);
            }
        }
        return downloadId;
    }

    /**
     * Get media id from download id
     *
     * @param downloadId download id
     */
    public static String getMediaIdFromDownloadId(String downloadId) {
        if (StringUtils.isNullOrEmpty(downloadId)) {
            return null;
        }
        int index = downloadId.lastIndexOf(SEPARATOR);
        if (index >= 0 && index < downloadId.length() - SEPARATOR.length()) {
            return downloadId.substring(index + SEPARATOR.length());
        }
        return null;
    }

    /**
     * Get download path
     *
     * @param path    Base download path
     * @param filmId  Film id
     * @param mediaId Media id
     */
    public static String getDownloadPath(final String path, final String filmId,
            final String mediaId) {
        return path + separator + filmId + separator + mediaId;
    }

    /**
     * Get backup download files include the corresponding storage device.
     *
     * @param filmId  Film id
     * @param mediaId Media id
     */
    public static BackupDownloadInfo getBackupDownloads(final String filmId, final String mediaId) {
        final List<StorageUtils.StorageInfo> storageList = StorageUtils.getStorageList();
        if (null == storageList || storageList.isEmpty()) {
            return null;
        }

        BackupDownloadInfo backupDownloadInfo = null;
        List<StorageUtils.StorageInfo> bakDevices = null;
        List<List<DownloadFileList>> bakFileList = null;
        for (StorageUtils.StorageInfo storageInfo : storageList) {
            String basePath = storageInfo.path + separator + Constants.DOWNLOAD_FILE_NAME;
            // download path
            String filePath = getDownloadPath(basePath, filmId, mediaId);

            List<DownloadFileList> downloadFileLists = null;
            // load backup download files
            downloadFileLists = loadBackUpDownloadFileList(filePath);

            // has backup download files
            if (downloadFileLists != null && !downloadFileLists.isEmpty()) {
                // add this storage device
                if (null == bakDevices) {
                    bakDevices = new ArrayList<>();
                }
                bakDevices.add(storageInfo);

                if (null == bakFileList) {
                    bakFileList = new ArrayList<>();
                }
                bakFileList.add(downloadFileLists);
            }
        }

        if (bakDevices != null && !bakDevices.isEmpty()) {
            backupDownloadInfo = new BackupDownloadInfo(bakDevices, bakFileList);
        }

        return backupDownloadInfo;
    }

    /**
     * Load backup download files list from the file path
     *
     * @param path download path
     */
    public static List<DownloadFileList> loadBackUpDownloadFileList(String path) {
        File file = new File(path, Constants.DOWNLOAD_BACKUP);
        if (!file.exists()) {
            return null;
        }

        String backupFilePath = file.getAbsolutePath();
        SerializerHelper<DownloadInfo> helper = new SerializerHelper<>();
        DownloadInfo downloadInfo = helper.loadObject(backupFilePath);
        if (null == downloadInfo) {
            return null;
        }

        ArrayList<DownloadFileList> list = downloadInfo.mDownloadFileLists;
        if (list != null) {
            refreshDownloadFileList(path, list);
        }

        return list;
    }

    /**
     * Save backup download info
     *
     * @param downloadInfo backup download info
     * @param path         download path
     */
    public static boolean saveBackUpDownloadFileList(DownloadInfo downloadInfo, String path) {
        SerializerHelper<DownloadInfo> helper = new SerializerHelper<>();
        File file = new File(path, Constants.DOWNLOAD_BACKUP);
        return helper.saveObject(downloadInfo, file.getAbsolutePath());
    }

    /**
     * 根据本地文件更新下载文件列表进度
     */
    private static List<DownloadFileList> refreshDownloadFileList(String path,
            List<DownloadFileList> srcList) {
        for (DownloadFileList downloadFileList : srcList) {
            String urlString;
            String fileName;
            String filePath;
            File file;

            urlString = downloadFileList.mFileUrl;
            fileName = NetworkUtils.getFileNameFromUrl(urlString);
            filePath = path + File.separatorChar + fileName;
            file = new File(filePath);
            if (file.exists()) {
                downloadFileList.mCompleteSize = file.length();
//                hasFile = true;
            } else {
                downloadFileList.mCompleteSize = 0;
            }

            // md5文件
            String md5FileUrl = downloadFileList.mMd5FileUrl;
            if (!StringUtils.isNullOrEmpty(md5FileUrl)) {
//                fileName = "." + NetworkUtils.getFileNameFromUrl(md5FileUrl);
                fileName = NetworkUtils.getFileNameFromUrl(md5FileUrl);
                filePath = path + File.separatorChar + fileName;
                file = new File(filePath);
                if (file.exists()) {
                    downloadFileList.mMd5CompleteSize = file.length();
//                    hasFile = true;
                } else {
                    downloadFileList.mMd5CompleteSize = 0;
                }
            }
        }
        return srcList;
    }

    /**
     * Get download error description.
     *
     * @param resources Application Resources
     * @param errCode   Download error code
     * @return Error description
     */
    public static String getDownloadErrorDescription(Resources resources, int errCode) {
        String errMsg = null;
        switch (errCode) {
            case DownloadConstants.ErrorCode.ERR_FAILED:
                errMsg = resources.getString(R.string.download_error);
                break;
            case DownloadConstants.ErrorCode.ERR_NETWORK_NO_AVAIALBE:
                errMsg = resources.getString(R.string.download_error_io_network_not_available);
                break;
            case DownloadConstants.ErrorCode.ERR_NETWORK_ERROR:
                errMsg = resources.getString(R.string.download_error_io_network_error);
                break;
            case DownloadConstants.ErrorCode.ERR_SOCKET_TIME_OUT:
                errMsg = resources.getString(R.string.download_error_io_socket_time_out);
                break;
            case DownloadConstants.ErrorCode.ERR_URL_INVALID:
                errMsg = resources.getString(R.string.download_error_io_url_invalid);
                break;
            // case DownloadConstants.ErrorCode.ERR_SERVER_UNAVAILABLE:
            // errMsg = context
            // .getString(R.string.download_error_io_server_not_reach);
            // break;
            case DownloadConstants.ErrorCode.ERR_SERVER_UNAVAILABLE:
                errMsg = resources.getString(R.string.download_error_io_server_unavailable);
                break;
            case DownloadConstants.ErrorCode.ERR_CONNECTION_CLOSED:
                errMsg = resources.getString(R.string.download_error_connection_closed);
                break;
            case DownloadConstants.ErrorCode.ERR_GET_FILE_INFO_FAILED:
                errMsg = resources.getString(R.string.download_error_get_file_info_failed);
                break;
            case DownloadConstants.ErrorCode.ERR_DISK_NOT_ENOUGH_SPACE:
                errMsg = resources.getString(R.string.download_error_io_space_not_enouth);
                break;
            case DownloadConstants.ErrorCode.ERR_FILE_TOO_LARGE:
                errMsg = resources.getString(R.string.download_error_io_file_too_large);
                break;
            case DownloadConstants.ErrorCode.ERR_FILE_COULD_NOT_READ:
                errMsg = resources.getString(R.string.download_error_io_file_read_failed);
                errMsg += ", " + resources.getString(R.string.download_error_file_need_format);
                break;
            case DownloadConstants.ErrorCode.ERR_FILE_COULD_NOT_WRITE:
                errMsg = resources.getString(R.string.download_error_io_file_write_failed);
                errMsg += ", " + resources.getString(R.string.download_error_file_need_format);
                break;
            case DownloadConstants.ErrorCode.ERR_FILE_COULD_NOT_CREATE:
                errMsg = resources.getString(R.string.download_error_io_file_create_failed);
                errMsg += ", " + resources.getString(R.string.download_error_file_need_format);
                break;
            case DownloadConstants.ErrorCode.ERR_FILE_ERROR:
                errMsg = resources.getString(R.string.download_error_io_file_error);
                errMsg += ", " + resources.getString(R.string.download_error_file_need_format);
                break;
            case DownloadConstants.ErrorCode.ERR_FILE_NOT_EXIST:
                errMsg = resources.getString(R.string.download_error_io_file_not_exist);
                break;
            case DownloadConstants.ErrorCode.ERR_STORAGE_DEVICE_NOT_EXIST:
                errMsg = resources.getString(R.string.download_error_io_storage_device_not_exist);
                break;
            case DownloadConstants.ErrorCode.ERR_STORAGE_DEVICE_BAD_REMOVE:
                errMsg = resources.getString(R.string.download_error_io_storage_device_not_exist);
                break;
            case DownloadConstants.ErrorCode.ERR_IO_ERROR:
                errMsg = resources.getString(R.string.download_error_io_IO_error);
                break;
            case DownloadConstants.ErrorCode.ERR_MD5_ERROR:
                errMsg = resources.getString(R.string.download_error_io_md5_error);
                break;
            case DownloadConstants.ErrorCode.ERR_TASK_DOWNLOADING_ALREADY:
                errMsg = resources.getString(R.string.download_error_task_downloading_already);
                break;
            case DownloadConstants.ErrorCode.ERR_TASK_PAUSING_ALREADY:
                errMsg = resources.getString(R.string.download_error_task_pausing_already);
                break;
            case DownloadConstants.ErrorCode.ERR_TASK_COMPLETE_ALREADY:
                errMsg = resources.getString(R.string.download_error_task_complete_already);
                break;
            case DownloadConstants.ErrorCode.ERR_TASK_EXIST_ALREADY:
                errMsg = resources.getString(R.string.download_error_task_exist_already);
                break;
            case DownloadConstants.ErrorCode.ERR_TASK_HAS_PAUSE:
                errMsg = resources.getString(R.string.download_error_task_has_pause);
                break;
            case DownloadConstants.ErrorCode.ERR_TASK_NOT_EXIST:
                errMsg = resources.getString(R.string.download_error_task_not_exist);
                break;
            case DownloadConstants.ErrorCode.ERR_TASK_DELETEING_ALREADY:
                errMsg = resources.getString(R.string.download_error_task_deleting_already);
                break;
            case DownloadConstants.ErrorCode.ERR_TASK_HAS_DELETE:
                errMsg = resources.getString(R.string.download_error_task_deleting_already);
                break;
            case DownloadConstants.ErrorCode.ERR_MAX_TASK:
                errMsg = resources.getString(R.string.download_error_max_task_reach);
                break;
            case DownloadConstants.ErrorCode.ERR_TASK_WAITING_LAST_ACTION:
                errMsg = resources.getString(R.string.download_error_task_waiting_last_action);
                break;
            case DownloadConstants.ErrorCode.ERR_FATAL_ERROR:
                errMsg = resources.getString(R.string.download_error_fatal_error);
                break;
            default:
                // Client Error 4xx
                if (errCode >= 400 && errCode < 500) {
                    errMsg = resources.getString(R.string.download_error_io_network_error);
                }
                // Server Error 5xx
                else if (errCode >= 500 && errCode < 600) {
                    errMsg = resources.getString(R.string.download_error_io_server_error);
                }
                // other status code
                else {
                    errMsg = resources.getString(R.string.download_error_unknown);
                }
                break;
        }
        return errMsg;
    }
}
