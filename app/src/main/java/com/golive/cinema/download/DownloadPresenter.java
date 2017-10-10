package com.golive.cinema.download;

import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.golive.cinema.BasePresenter;
import com.golive.cinema.Constants;
import com.golive.cinema.download.domain.model.BackupDownloadInfo;
import com.golive.cinema.download.domain.model.DownloadFileList;
import com.golive.cinema.download.domain.model.DownloadInfo;
import com.golive.cinema.download.domain.usecase.AddDownloadTaskUseCase;
import com.golive.cinema.download.domain.usecase.DeleteDownloadTaskUseCase;
import com.golive.cinema.download.domain.usecase.DownloadFileUseCase;
import com.golive.cinema.download.domain.usecase.GetDownloadTaskInfoUseCase;
import com.golive.cinema.download.domain.usecase.PauseAllDownloadTaskUseCase;
import com.golive.cinema.download.domain.usecase.PauseDownloadTaskUseCase;
import com.golive.cinema.download.domain.usecase.ResumeDownloadTaskUseCase;
import com.golive.cinema.util.EspressoIdlingResource;
import com.golive.cinema.util.FileUtils;
import com.golive.cinema.util.StorageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.initialjie.download.DownloadConstants;
import com.initialjie.download.aidl.DownloadTaskInfo;
import com.initialjie.log.Logger;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;

/**
 * Created by Wangzj on 2016/11/29.
 */

public class DownloadPresenter extends BasePresenter<DownloadContract.View> implements
        DownloadContract.Presenter {

    private final String mFilmId;
    private final String mMediaId;
    private final String mMediaUrl;
    private final String mSavePath;
//    private final boolean mReDownload;

    private final GetDownloadTaskInfoUseCase mGetDownloadTaskInfoUseCase;
    private final AddDownloadTaskUseCase mAddDownloadTaskUseCase;
    private final ResumeDownloadTaskUseCase mResumeDownloadTaskUseCase;
    private final PauseDownloadTaskUseCase mPauseDownloadTaskUseCase;
    private final PauseAllDownloadTaskUseCase mPauseAllDownloadTaskUseCase;
    private final DeleteDownloadTaskUseCase mDeleteDownloadTaskUseCase;
    private final DownloadFileUseCase mDownloadFileUseCase;
    private final BaseSchedulerProvider mSchedulerProvider;

    public DownloadPresenter(@NonNull DownloadContract.View view, @NonNull String filmId,
            @NonNull String mediaId, @NonNull String mediaUrl, @Nullable String savePath,
            @NonNull GetDownloadTaskInfoUseCase getDownloadTaskInfoUseCase,
            @NonNull AddDownloadTaskUseCase addDownloadTaskUseCase,
            @NonNull ResumeDownloadTaskUseCase resumeDownloadTaskUseCase,
            @NonNull PauseDownloadTaskUseCase pauseDownloadTaskUseCase,
            @NonNull PauseAllDownloadTaskUseCase pauseAllDownloadTaskUseCase,
            @NonNull DeleteDownloadTaskUseCase deleteDownloadTaskUseCase,
            @NonNull DownloadFileUseCase downloadFileUseCase,
            @NonNull BaseSchedulerProvider schedulerProvider) {
        mFilmId = checkNotNull(filmId, "filmId cannot be null!");
        mMediaId = checkNotNull(mediaId, "mediaId cannot be null!");
        mMediaUrl = checkNotNull(mediaUrl, "mediaUrl cannot be null!");
        mSavePath = savePath;
//        mReDownload = reDownload;
        mGetDownloadTaskInfoUseCase = checkNotNull(getDownloadTaskInfoUseCase,
                "GetDownloadTaskInfoUseCase cannot be null!");
        mAddDownloadTaskUseCase = checkNotNull(addDownloadTaskUseCase,
                "AddDownloadTaskUseCase cannot be null!");
        mResumeDownloadTaskUseCase = checkNotNull(resumeDownloadTaskUseCase,
                "ResumeDownloadTaskUseCase cannot be null!");
        mPauseDownloadTaskUseCase = checkNotNull(pauseDownloadTaskUseCase,
                "PauseDownloadTaskUseCase cannot be null!");
        mPauseAllDownloadTaskUseCase = checkNotNull(pauseAllDownloadTaskUseCase,
                "PauseAllDownloadTaskUseCase cannot be null!");
        mDeleteDownloadTaskUseCase = checkNotNull(deleteDownloadTaskUseCase,
                "DeleteDownloadTaskUseCase cannot be null!");
        mDownloadFileUseCase = checkNotNull(downloadFileUseCase,
                "DownloadFileUseCase cannot be null!");
        mSchedulerProvider = checkNotNull(schedulerProvider,
                "BaseSchedulerProvider cannot be null!");
        attachView(checkNotNull(view, "view cannot be null!"));
        view.setPresenter(this);
    }

    @Override
    public void start() {
        super.start();

//        download(mFilmId, mMediaId, mReDownload);
    }

    @Override
    public void download(final boolean reDownload) {
        final String filmId = mFilmId;
        final String mediaId = mMediaId;
        Logger.d("download, filmId : " + filmId + ", mediaId : " + mediaId + ", reDownload : "
                + reDownload);

        // 1. get all storage devices, if not device, show no storage device UI and return.
        // 2. get download info
        // 2.1 if not exist, try get history backup download info.
        // 2.1.2 show confirm restore history download? if restore, show select restore download UI.
        //       if not, go create new download task(re-download).
        // 2.2. if download info exist, auto resume/pause the download task. if resume, check
        //      whether storage exist, if not exist, show confirm re-download UI.

        DownloadContract.View view = getView();
        if (null == view || !view.isActive()) {
            return;
        }

        List<StorageUtils.StorageInfo> storageInfos = StorageUtils.getStorageList();

        // if not device
        if (null == storageInfos || storageInfos.isEmpty()) {
            // show no storage device UI and return.
            view.showNoStorageDevice();
            return;
        }

        view.setDownloadingIndicator(true);

        EspressoIdlingResource.increment();

        // get download info
        Subscription subscription = getDownloadTaskObs(filmId, mediaId)
//                .observeOn(mSchedulerProvider.io())
                .flatMap(new Func1<DownloadTaskInfo, Observable<?>>() {
                    @Override
                    public Observable<?> call(DownloadTaskInfo downloadTaskInfo) {
                        DownloadContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        Logger.d("getDownloadTaskObs, downloadTaskInfo : " + downloadTaskInfo);

                        if (null == downloadTaskInfo) { // download task not exist
                            return tryRestoreBackupDownload(filmId, mediaId, null);
                        }

                        final String path = downloadTaskInfo.getFilePath();

                        // re-download
                        if (reDownload) {
                            return tryRestoreBackupDownload(filmId, mediaId, path);
                        }

                        // download file not exist
                        if (!new File(path).exists()) {
                            // show file not exist UI
                            return view.showFileNotExist(path)
                                    .subscribeOn(mSchedulerProvider.ui())
                                    // filter re-download
                                    .filter(new Func1<Boolean, Boolean>() {
                                        @Override
                                        public Boolean call(Boolean aBoolean) {
                                            return aBoolean != null && aBoolean;
                                        }
                                    })
                                    .flatMap(new Func1<Boolean, Observable<?>>() {
                                        @Override
                                        public Observable<?> call(Boolean aBoolean) {
                                            return tryRestoreBackupDownload(filmId, mediaId, path);
                                        }
                                    });
                        }

                        // downloading or pending
                        if (downloadTaskInfo.isDownloading()
                                || DownloadConstants.Status.PENDING
                                == downloadTaskInfo.getStatus()) {
                            // pause download
                            return pauseDownloadTaskObs(filmId, mediaId);
                        } else {
                            // resume download
                            return resumeDownloadTaskObs(filmId, mediaId);
                        }


                        //
                        //
                        //
                        //
                        //
                        //
                        //
//                        if (null == downloadTaskInfo) { // download task not exist
//                            return tryRestoreBackupDownload(filmId, mediaId, null);
//                        } else { // download task exist
//                            String path = downloadTaskInfo.getFilePath();
//                            // download file not exist
//                            if (!new File(path).exists()) {
//                                // show file not exist UI
//                                return view.showFileNotExist(path)
//                                        .subscribeOn(mSchedulerProvider.ui())
//                                        // filter re-download
//                                        .filter(new Func1<Boolean, Boolean>() {
//                                            @Override
//                                            public Boolean call(Boolean aBoolean) {
//                                                return aBoolean != null && aBoolean;
//                                            }
//                                        })
//                                        .flatMap(new Func1<Boolean, Observable<?>>() {
//                                            @Override
//                                            public Observable<?> call(Boolean aBoolean) {
//                                                // new download
//                                                return newDownload(filmId, mediaId, null, true);
//                                            }
//                                        });
//                            }
//
//                            // downloading or pending
//                            if (downloadTaskInfo.isDownloading()
//                                    || DownloadConstants.Status.PENDING
//                                    == downloadTaskInfo.getStatus()) {
//                                // pause download
//                                return pauseDownloadTaskObs(filmId, mediaId);
//                            } else {
//                                // resume download
//                                return resumeDownloadTaskObs(filmId, mediaId);
//                            }
//                        }
                    }
                })
                .observeOn(mSchedulerProvider.ui())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        Logger.d("onCompleted");
                        EspressoIdlingResource.decrement();
                        DownloadContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setDownloadingIndicator(false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        EspressoIdlingResource.decrement();
                        Logger.e(e, "download, onError : ");
                        DownloadContract.View view = getView();
                        if (view != null && view.isActive()) {
                            view.setDownloadingIndicator(false);
                            view.showDownloadError(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(Object o) {
                        Logger.d("onNext");
                    }
                });

        addSubscription(subscription);
    }

    /**
     * Add new download task
     *
     * @param filmId     film id
     * @param mediaId    media id
     * @param reDownload Whether re-download
     */
    private Observable<?> newDownload(final String filmId, final String mediaId,
            final String filePath, final boolean reDownload) {
        Logger.d("newDownload, filePath : " + filePath + ", reDownload : " + reDownload);
        // parse download files
        return parseDownloadFiles()
                .flatMap(new Func1<List<DownloadFileList>, Observable<?>>() {
                    @Override
                    public Observable<?> call(final List<DownloadFileList> downloadFileLists) {
                        DownloadContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        // not given save filePath
                        if (StringUtils.isNullOrEmpty(filePath)) {
                            long mFileSize = 0;
                            for (DownloadFileList fileList : downloadFileLists) {
                                mFileSize += fileList.mFileSize + fileList.mMd5FileSize;
                            }
                            // show select download filePath
                            return view.showSelectNewDownloadPath(StorageUtils.getStorageList(),
                                    mFileSize)
                                    .subscribeOn(mSchedulerProvider.ui())
                                    .flatMap(new Func1<String, Observable<?>>() {
                                        @Override
                                        public Observable<?> call(String path) {
                                            return addNewDownloadTaskObs(filmId, mediaId,
                                                    downloadFileLists, path, reDownload);
                                        }
                                    });
                        } else {
                            return addNewDownloadTaskObs(filmId, mediaId, downloadFileLists,
                                    filePath, reDownload);
                        }
                    }
                });
    }

    /**
     * add new download
     *
     * @param filmId      film id
     * @param mediaId     media id
     * @param dlFileList  download file list
     * @param path        path
     * @param newDownload Whether new download
     */
    private Observable<Boolean> addNewDownloadTaskObs(final String filmId, final String mediaId,
            final List<DownloadFileList> dlFileList, final String path, final boolean newDownload) {
        return mDeleteDownloadTaskUseCase.run(new DeleteDownloadTaskUseCase
                .RequestValues(filmId, mediaId, newDownload))
                .flatMap(new Func1<DeleteDownloadTaskUseCase.ResponseValue, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(
                            DeleteDownloadTaskUseCase.ResponseValue responseValue) {

                        // get download path
                        File file = new File(path, Constants.DOWNLOAD_FILE_NAME);
                        String downloadPath = DownloadUtils.getDownloadPath(file.getAbsolutePath(),
                                filmId, mediaId);
                        Logger.d("addNewDownloadTaskObs, downloadPath : " + downloadPath);

                        // 创建文件夹
                        File f = new File(downloadPath);

                        // new download
                        if (newDownload) {
                            // delete old files if exist
                            Logger.d("delete old files if exist");
                            FileUtils.deleteRecursively(f);
                        }

                        if (!f.exists()) {
                            boolean success = f.mkdirs();
                            Logger.d("addNewDownloadTaskObs, file not exists, then mkdirs : "
                                    + success);
                            if (!success) {
                                Logger.w("canRead : " + f.canRead());
                                Logger.w("canWrite : " + f.canWrite());
                            }
                        }

//                        ArrayList<DownloadTaskInfo> taskInfos = new ArrayList<>();
//                        for (DownloadFileList dlFile : dlFileList) {
//                            String fileName;
//                            String filePath;
//                            String url = dlFile.mFileUrl;
//                            fileName = NetworkUtils.getFileNameFromUrl(url);
//                            filePath = downloadPath + File.pathSeparator + fileName;
//                            DownloadTaskInfo task = new DownloadTaskInfo();
//                            // add task
//                            taskInfos.add(task);
//                            task.setUrl(url);
//                            task.setFilePath(filePath);
//                            task.setTotalSize((dlFile.mFileSize));
//                            if (!newDownload) {
//                                task.setCompleteSize(dlFile.mCompleteSize);
//                            }
//                            // has md5 file
//                            String md5FileUrl = dlFile.mMd5FileUrl;
//                            if (!StringUtils.isNullOrEmpty(md5FileUrl)) {
//                                task.setMd5FileTaskId(DownloadTaskInfo.getChildTaskID(
//                                        filmId, md5FileUrl));
//                                // make it hidden
//                                fileName = "." + NetworkUtils.getFileNameFromUrl(md5FileUrl);
//                                filePath = downloadPath + File.pathSeparator + fileName;
//                                DownloadTaskInfo md5Task = new DownloadTaskInfo();
//                                md5Task.setUrl(md5FileUrl);
//                                md5Task.setMd5(dlFile.mMd5FileMd5);
//                                md5Task.setFilePath(filePath);
//                                md5Task.setTotalSize(dlFile.mMd5FileSize);
//                                if (!newDownload) {
//                                    md5Task.setCompleteSize(dlFile.mMd5CompleteSize);
//                                }
//                                // add task
//                                taskInfos.add(md5Task);
//                            }
//                        }

                        List<String> urlList = new ArrayList<>();
                        List<Long> fileSizeList = new ArrayList<>();
                        for (DownloadFileList list : dlFileList) {
                            urlList.add(list.mFileUrl);
                            fileSizeList.add(list.mFileSize);
                            if (!StringUtils.isNullOrEmpty(list.mMd5FileUrl)) {
                                urlList.add(list.mMd5FileUrl);
                                fileSizeList.add(list.mMd5FileSize);
                            }
                        }

                        String[] urls = new String[urlList.size()];
                        urls = urlList.toArray(urls);

                        int size = fileSizeList.size();
                        long[] fileSizes = new long[size];
                        for (int i = 0; i < size; i++) {
                            fileSizes[i] = fileSizeList.get(i);
                        }

                        ArrayList<DownloadFileList> fileLists = new ArrayList<>(dlFileList);
                        DownloadInfo downloadInfo = new DownloadInfo();
                        downloadInfo.mDownloadFileLists = fileLists;
                        downloadInfo.mUrl = mMediaUrl;

                        //save backup download info
                        DownloadUtils.saveBackUpDownloadFileList(downloadInfo, downloadPath);

                        String reserve = null;
                        return addDownloadTaskObs(filmId, mediaId, downloadPath, reserve, urls,
                                fileSizes);
                    }
                });
    }

    /**
     * Try restore backup download task.
     *
     * @param filmId     film id
     * @param mediaId    media id
     * @param ignorePath path to ignore when scan history backup downloa
     */
    private Observable<?> tryRestoreBackupDownload(final String filmId, final String mediaId,
            final @Nullable String ignorePath) {
        Logger.d("tryRestoreBackupDownload, ignorePath : " + ignorePath);
        return getBackupDownloadInfo(filmId, mediaId, ignorePath)
                .observeOn(mSchedulerProvider.ui())
                .flatMap(new Func1<BackupDownloadInfo, Observable<?>>() {
                    @Override
                    public Observable<?> call(final BackupDownloadInfo info) {
                        DownloadContract.View view = getView();
                        if (null == view || !view.isActive()) {
                            return Observable.empty();
                        }

                        Logger.d("getBackupDownloadInfo, info : " + info);

                        // no backup download info
                        if (null == info || null == info.getStorageInfoList()
                                || info.getStorageInfoList().isEmpty()) {
                            // new download
                            return newDownload(filmId, mediaId, mSavePath, true);
                        }

                        final List<StorageUtils.StorageInfo> storageInfos =
                                info.getStorageInfoList();
                        final List<List<DownloadFileList>> fileList = info.getDownloadFileList();

//                        // need to exclude the ignore device
//                        if (!StringUtils.isNullOrEmpty(ignorePath)) {
//                            Iterator<StorageUtils.StorageInfo> storageInfoIterator =
//                                    storageInfos.iterator();
//                            Iterator<List<DownloadFileList>> fileListIterator =
//                                    fileList.iterator();
//                            while (storageInfoIterator.hasNext()) {
//                                fileListIterator.next();
//                                StorageUtils.StorageInfo storageInfo =
//                                        storageInfoIterator.next();
//                                String path = storageInfo.path;
//                                // find the ignore device
//                                if (ignorePath.startsWith(path)) {
//                                    // remove this device
//                                    storageInfoIterator.remove();
//                                    fileListIterator.remove();
//                                    break;
//                                }
//                            }
//                        }
//                        // no backup download info
//                        if (null == info || null == info.getStorageInfoList()
//                                || info.getStorageInfoList().isEmpty()) {
//                            // new download
//                            return newDownload(filmId, mediaId, mSavePath, true);
//                        }

                        // show confirm restore backup download
                        return view.showConfirmRestoreBackupDownload(storageInfos, fileList)
                                .subscribeOn(mSchedulerProvider.ui())
                                .flatMap(new Func1<Boolean, Observable<?>>() {
                                    @Override
                                    public Observable<?> call(Boolean aBoolean) {
                                        DownloadContract.View view = getView();
                                        if (null == view || !view.isActive()) {
                                            return Observable.empty();
                                        }

                                        // not restore backup download
                                        if (null == aBoolean || !aBoolean) {
                                            // new download
                                            return newDownload(filmId, mediaId, mSavePath, true);
                                        }

                                        // show restore backup download ui
                                        return view.showSelectRestoreBackupDownload(
                                                storageInfos, fileList)
                                                .subscribeOn(mSchedulerProvider.ui())
                                                .flatMap(new Func1<String, Observable<?>>() {
                                                    @Override
                                                    public Observable<?> call(String path) {
                                                        // new download
                                                        return newDownload(filmId, mediaId, path,
                                                                false);
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .subscribeOn(mSchedulerProvider.io());
    }

    @NonNull
    private Observable<List<DownloadFileList>> parseDownloadFiles() {
        return downloadFile(mMediaUrl)
                .observeOn(mSchedulerProvider.io())
                .concatMap(new Func1<ResponseBody, Observable<? extends List<DownloadFileList>>>() {
                    @Override
                    public Observable<? extends List<DownloadFileList>> call(
                            ResponseBody responseBody) {
                        try {
                            return Observable.just(_parseDownloadFiles(responseBody));
                        } catch (Exception e) {
//                            e.printStackTrace();
                            Logger.e(e, "parseDownloadFiles, Exception : ");
                            return Observable.error(e);
                        }
                    }
                })
                .concatMap(new Func1<List<DownloadFileList>, Observable<List<DownloadFileList>>>() {
                    @Override
                    public Observable<List<DownloadFileList>> call(
                            List<DownloadFileList> downloadFileLists) {
                        return getFilesSize(downloadFileLists);
                    }
                });
    }

    @Nullable
    private List<DownloadFileList> _parseDownloadFiles(ResponseBody responseBody)
            throws XmlPullParserException, IOException {
        if (null == responseBody) {
            return null;
        }

        Logger.d("_parseDownloadFiles");
        List<DownloadFileList> dlFileList = null;
        InputStream is = responseBody.byteStream();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory
                    .newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(is, null);

            dlFileList = new ArrayList<>();
            DownloadFileList fileList = null;
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String name = xpp.getName();
                    if (!StringUtils.isNullOrEmpty(name)) {
                        if ("asset".equalsIgnoreCase(name)) {
                            fileList = new DownloadFileList();
                        } else if ("url".equalsIgnoreCase(name)) {
                            if (xpp.next() == XmlPullParser.TEXT) {
                                String text = xpp.getText();
                                // remove all tabs, returns, newlines, vertical
                                // tabs and spaces
                                text = text.replaceAll("\\s+", "");
                                if (fileList != null) {
                                    fileList.mFileUrl = text;
                                }
                            }
                        } else if ("md5".equalsIgnoreCase(name)) {
                            if (fileList != null) {
                                fileList.mMd5FileMd5 = xpp.getAttributeValue(null, "value");
                                if (xpp.next() == XmlPullParser.TEXT) {
                                    String text = xpp.getText();
                                    // remove all tabs, returns, newlines, vertical
                                    // tabs and spaces
                                    text = text.replaceAll("\\s+", "");
                                    fileList.mMd5FileUrl = xpp.getText();
                                }
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String name = xpp.getName();
                    if (!StringUtils.isNullOrEmpty(name) && "asset".equalsIgnoreCase(name)) {
                        dlFileList.add(fileList);
                    }
                }
                eventType = xpp.next();
            }
        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
        finally {
            if (responseBody != null) {
                try {
                    responseBody.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        return dlFileList;
    }

    private Observable<List<DownloadFileList>> getFilesSize(List<DownloadFileList> list) {
        Logger.d("getFilesSize, list : " + list);
        return Observable.from(list).concatMap(
                new Func1<DownloadFileList, Observable<DownloadFileList>>() {
                    @Override
                    public Observable<DownloadFileList> call(final DownloadFileList fileList) {
                        String url = fileList.mFileUrl;

                        // file url is null
                        if (StringUtils.isNullOrEmpty(url)) {
                            return Observable.just(fileList);
                        }

                        // get file size
                        return getContentLength(url)
                                .concatMap(new Func1<Long, Observable<DownloadFileList>>() {
                                    @Override
                                    public Observable<DownloadFileList> call(Long aLong) {
                                        fileList.mFileSize = aLong;
                                        String md5Url = fileList.mMd5FileUrl;

                                        // md5 file url is null
                                        if (StringUtils.isNullOrEmpty(md5Url)) {
                                            return Observable.just(fileList);
                                        }

                                        // get md5 file size
                                        return getContentLength(md5Url).flatMap(
                                                new Func1<Long, Observable<DownloadFileList>>() {
                                                    @Override
                                                    public Observable<DownloadFileList> call(
                                                            Long aLong) {
                                                        fileList.mMd5FileSize = aLong;
                                                        return Observable.just(fileList);
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .toList();
    }

    private Observable<Long> getContentLength(String url) {
        return downloadFile(url)
                .observeOn(mSchedulerProvider.io())
                .map(new Func1<ResponseBody, Long>() {
                    @Override
                    public Long call(ResponseBody responseBody) {
                        long l = responseBody.contentLength();
                        try {
                            responseBody.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return l;
                    }
                });
    }

    private Observable<ResponseBody> downloadFile(String url) {
        Logger.d("downloadFile, url : " + url);
        return mDownloadFileUseCase.run(new DownloadFileUseCase.RequestValues(url))
                .map(new Func1<DownloadFileUseCase.ResponseValue, ResponseBody>() {
                    @Override
                    public ResponseBody call(DownloadFileUseCase.ResponseValue responseValue) {
                        return responseValue.getResponseBody();
                    }
                });
    }

    private Observable<BackupDownloadInfo> getBackupDownloadInfo(final String filmId,
            final String mediaId, final String ignorePath) {
        Observable.OnSubscribe<BackupDownloadInfo> onSubscribe =
                new Observable.OnSubscribe<BackupDownloadInfo>() {
                    @Override
                    public void call(Subscriber<? super BackupDownloadInfo> subscriber) {
                        Logger.d("getBackupDownloadInfo, thread id : "
                                + Thread.currentThread().getId());
                        BackupDownloadInfo info = DownloadUtils.getBackupDownloads(filmId, mediaId);

                        if (info != null) {
                            final List<StorageUtils.StorageInfo> storageInfos =
                                    info.getStorageInfoList();
                            final List<List<DownloadFileList>> fileList =
                                    info.getDownloadFileList();
                            // need to exclude the ignore device
                            if (!StringUtils.isNullOrEmpty(ignorePath)
                                    && storageInfos != null && !storageInfos.isEmpty()) {
                                Iterator<StorageUtils.StorageInfo> storageInfoIterator =
                                        storageInfos.iterator();
                                Iterator<List<DownloadFileList>> fileListIterator =
                                        fileList.iterator();
                                while (storageInfoIterator.hasNext()) {
                                    fileListIterator.next();
                                    StorageUtils.StorageInfo storageInfo =
                                            storageInfoIterator.next();
                                    String path = storageInfo.path;
                                    // find the ignore device
                                    if (ignorePath.startsWith(path)) {
                                        // remove this device
                                        storageInfoIterator.remove();
                                        fileListIterator.remove();
                                        break;
                                    }
                                }
                            }
                        }

                        subscriber.onNext(info);
                        subscriber.onCompleted();
                    }
                };
        return Observable.create(onSubscribe);
    }

    private Observable<Boolean> addDownloadTaskObs(String filmId, String mediaId,
            String downloadPath, String reserve, String[] urls, long[] fileSizes) {
        AddDownloadTaskUseCase.RequestValues requestValues =
                new AddDownloadTaskUseCase.RequestValues(filmId, mediaId, downloadPath, reserve,
                        urls, fileSizes);
        return mAddDownloadTaskUseCase.run(requestValues)
                .map(new Func1<AddDownloadTaskUseCase.ResponseValue, Boolean>() {
                    @Override
                    public Boolean call(AddDownloadTaskUseCase.ResponseValue responseValue) {
                        return responseValue.isSuccess();
                    }
                });
    }

    private Observable<Boolean> pauseDownloadTaskObs(String filmId, String mediaId) {
        PauseDownloadTaskUseCase.RequestValues requestValues =
                new PauseDownloadTaskUseCase.RequestValues(filmId, mediaId);
        return mPauseDownloadTaskUseCase.run(requestValues)
                .map(new Func1<PauseDownloadTaskUseCase.ResponseValue, Boolean>() {
                    @Override
                    public Boolean call(PauseDownloadTaskUseCase.ResponseValue responseValue) {
                        return responseValue.isSuccess();
                    }
                });
    }

    private Observable<Boolean> resumeDownloadTaskObs(String filmId, String mediaId) {
        ResumeDownloadTaskUseCase.RequestValues requestValues =
                new ResumeDownloadTaskUseCase.RequestValues(filmId, mediaId);
        return mResumeDownloadTaskUseCase.run(requestValues)
                .map(new Func1<ResumeDownloadTaskUseCase.ResponseValue, Boolean>() {
                    @Override
                    public Boolean call(ResumeDownloadTaskUseCase.ResponseValue responseValue) {
                        return responseValue.isSuccess();
                    }
                });
    }

    private Observable<DownloadTaskInfo> getDownloadTaskObs(String filmId, String mediaId) {
        GetDownloadTaskInfoUseCase.RequestValues requestValues =
                new GetDownloadTaskInfoUseCase.RequestValues(filmId, mediaId);
        return mGetDownloadTaskInfoUseCase.run(requestValues).map(
                new Func1<GetDownloadTaskInfoUseCase.ResponseValue, DownloadTaskInfo>() {
                    @Override
                    public DownloadTaskInfo call(GetDownloadTaskInfoUseCase.ResponseValue
                            responseValue) {
                        return responseValue.getDownloadTaskInfo();
                    }
                });
    }
}
