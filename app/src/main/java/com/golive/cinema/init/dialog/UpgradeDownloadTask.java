package com.golive.cinema.init.dialog;

import android.content.Context;
import android.os.AsyncTask;

import com.golive.cinema.Constants;
import com.golive.cinema.util.StringUtils;
import com.initialjie.log.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class UpgradeDownloadTask extends AsyncTask<Void, Void, Void> {

    private final Context mContext;
    private final String mFileUrl;
    private final String mFilePath;

    public UpgradeDownloadTask(Context context, String fileUrl) {
        this.mContext = context;
        this.mFileUrl = fileUrl;
        this.mFilePath = mContext.getFilesDir().getAbsolutePath();
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!StringUtils.isNullOrEmpty(mFileUrl)) {
            downloadFile(mFileUrl, mFilePath, mOnDownloadListener);
        }
        return null;
    }

    private OnDownloadListener mOnDownloadListener = null;

    public void setOnDownloadListener(OnDownloadListener reqProgressCallBack) {
        mOnDownloadListener = reqProgressCallBack;
    }

    public interface OnDownloadListener {
        void onDownloading();

        void onDownloadCompleted(File file);

        void onDownloadProgress(long current, long total);

        void onDownloadError();
    }

    /**
     * 下载文件
     *
     * @param fileUrl  文件url
     * @param filePath 存储目标目录
     */
    private void downloadFile(String fileUrl, final String filePath,
            final OnDownloadListener callBack) {
        final File file = new File(filePath, Constants.UPGRADE_FILE_NAME);
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            if (callBack != null) {
                callBack.onDownloadError();
            }
            e.printStackTrace();
        }

        OkHttpClient mOkHttpClient = new OkHttpClient();
        final Request request = new Request.Builder().url(fileUrl).build();
        final Call call = mOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.e(e.toString());
                if (callBack != null) {
                    callBack.onDownloadError();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[4096];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    long total = response.body().contentLength();
                    Logger.e("total------>" + total);
                    long current = 0;
                    is = response.body().byteStream();
                    fos = mContext.openFileOutput(Constants.UPGRADE_FILE_NAME,
                            Context.MODE_WORLD_READABLE);
                    if (callBack != null) {
                        callBack.onDownloading();
                    }
                    while ((len = is.read(buf)) != -1) {
                        current += len;
                        fos.write(buf, 0, len);
                        if (callBack != null) {
                            callBack.onDownloadProgress(current / 1000, total / 1000);
                        }
                    }
                    fos.flush();
                    if (callBack != null) {
                        callBack.onDownloadCompleted(file);
                    }
                } catch (IOException e) {
                    Logger.e(e.toString());
                    if (callBack != null) {
                        callBack.onDownloadError();
                    }
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        Logger.e(e.toString());
                    }
                }
            }
        });
    }

}
