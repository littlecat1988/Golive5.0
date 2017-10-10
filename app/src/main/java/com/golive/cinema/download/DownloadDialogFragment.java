package com.golive.cinema.download;

import static com.golive.cinema.util.FragmentUtils.removePreviousFragment;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.download.domain.model.DownloadFileList;
import com.golive.cinema.util.StorageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.initialjie.download.DownloadConstants;
import com.initialjie.log.Logger;

import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Wangzj on 2016/11/29.
 */

public class DownloadDialogFragment extends BaseDialog implements DownloadContract.View {
    private static final String SELECT_PATH_FRAG_TAG = "select_path_frag_tag";
    private static final String CONFIRM_RESTORE_DOWNLOAD_FRAG_TAG =
            "confirm_restore_download_frag_tag";
    private static final String SELECT_RESTORE_DOWNLOAD_FRAG_TAG =
            "select_restore_download_frag_tag";
    private static final String DOWNLOAD_ERROR_FRAG_TAG = "download_error_frag_tag";
    private static final int REQUEST_WRITE_STORAGE = 1;

    private DownloadContract.Presenter mPresenter;
    private ProgressDialog mDialog;
    private String mFilmId;
    private String mFilmName;
    private String mMediaId;
    private String mMediaUrl;
    private String mSavePath;
    private boolean mReDownload;

    private Runnable mRunOnResume;

    public static DownloadDialogFragment newInstance(String filmId, String filmName, String mediaId,
            String mediaUrl, @Nullable String path, boolean reDownload) {
        DownloadDialogFragment fragment = new DownloadDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_FILM_ID, filmId);
        arguments.putString(Constants.EXTRA_FILM_NAME, filmName);
        arguments.putString(Constants.EXTRA_MEDIA_ID, mediaId);
        arguments.putString(Constants.EXTRA_MEDIA_URL, mediaUrl);
        arguments.putString(Constants.EXTRA_FILE_PATH, path);
        arguments.putBoolean(Constants.EXTRA_REDOWNLOAD, reDownload);
        fragment.setArguments(arguments);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        mFilmId = arguments.getString(Constants.EXTRA_FILM_ID);
        mFilmName = arguments.getString(Constants.EXTRA_FILM_NAME);
        mMediaId = arguments.getString(Constants.EXTRA_MEDIA_ID);
        mMediaUrl = arguments.getString(Constants.EXTRA_MEDIA_URL);
        mSavePath = arguments.getString(Constants.EXTRA_FILE_PATH);
        mReDownload = arguments.getBoolean(Constants.EXTRA_REDOWNLOAD);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Logger.d("onActivityCreated");

        // create presenter
        Context context = getContext();
        DownloadContract.Presenter presenter = new DownloadPresenter(this, mFilmId, mMediaId,
                mMediaUrl, mSavePath,
                Injection.provideGetDownloadTaskInfoUseCase(context),
                Injection.provideAddDownloadTaskUseCase(context),
                Injection.provideResumeDownloadTaskUseCase(context),
                Injection.providePauseDownloadTaskUseCase(context),
                Injection.providePauseAllDownloadTaskUseCase(context),
                Injection.provideDeleteDownloadTaskUseCase(context),
                Injection.provideDownloadFileUseCase(context),
                Injection.provideSchedulerProvider());

        if (isStoragePermissionGranted()) {
            presenter.download(mReDownload);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        Logger.d("onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                final boolean isGranted = grantResults.length > 0
                        && PackageManager.PERMISSION_GRANTED == grantResults[0];
                if (isGranted) {
                    // granted
                    Logger.d("onRequestPermissionsResult, granted");

                    if (getPresenter() != null) {
                        getPresenter().download(mReDownload);
                    }
                } else {
                    // denied
                    Logger.d("onRequestPermissionsResult, denied");
                    Toast.makeText(getContext(), R.string.need_write_permission,
                            Toast.LENGTH_SHORT).show();
                }

                if (!isGranted) {
                    mRunOnResume = new Runnable() {
                        @Override
                        public void run() {
                            // Marshmallow bug!
                            // if called directly in onRequestPermissionsResult, it call raise a
                            // IllegalStateException
                            dismiss();
                        }
                    };
                }
                break;
            }

            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d("onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d("onResume");

        if (mRunOnResume != null) {
            Runnable runOnResume = mRunOnResume;
            mRunOnResume = null;
            runOnResume.run();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams windowParams = window.getAttributes();
            windowParams.dimAmount = 0.0f;
            window.setAttributes(windowParams);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
        if (getPresenter() != null) {
            getPresenter().unsubscribe();
        }
        if (mDialog != null) {
            UIHelper.dismissDialog(mDialog);
        }
    }

    @Override
    public void setDownloadingIndicator(boolean active) {
        if (active && isActive()) {
            if (null == mDialog) {
                mDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.please_wait));
                mDialog.setCancelable(true);
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        Logger.d("onKey");
                        if (KeyEvent.ACTION_DOWN == event.getAction()
                                || KeyEvent.KEYCODE_BACK == keyCode) {
                            Logger.d("onKey, KEYCODE_BACK");

                            // dismiss
                            dismiss();
                        }
                        return false;
                    }
                });
                mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Logger.d("DialogInterface, onCancel");
                        // dismiss
                        dismiss();
                    }
                });
            }

            if (!mDialog.isShowing()) {
                mDialog.show();
            }
        } else if (!active) {
            if (mDialog != null) {
                UIHelper.dismissDialog(mDialog);
            }

            // dismiss
            dismiss();
        }
    }

    @Override
    public void showDownloadError(String errMsg) {
        String txt = getString(R.string.download_error);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            txt += ", " + errMsg;
        }
        Toast.makeText(getContext(), txt, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showNoStorageDevice() {
        Toast.makeText(getContext(), R.string.download_error_no_storage_device,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public Observable<Boolean> showFileNotExist(final String filePath) {
        Logger.d("showFileNotExist, filePath : " + filePath);
        if (!isAdded()) {
            return Observable.empty();
        }

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {

                String fragTag = DOWNLOAD_ERROR_FRAG_TAG;
                removePreviousFragment(getFragmentManager(), fragTag);
                // Create and show the dialog.
//                String title = getString(R.string.download_error_io_file_not_exist);
                DownloadErrorFragment fragment = DownloadErrorFragment.newInstance(mFilmName,
                        filePath, DownloadConstants.ErrorCode.ERR_FILE_NOT_EXIST);
                fragment.setListener(new DownloadErrorFragment.OnDownloadErrorListener() {
                    @Override
                    public void onReDownload() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(true);
                        }
                    }

                    @Override
                    public void onCancel() {
                    }
                });
                fragment.setOnDialogDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
                fragment.show(getFragmentManager(), fragTag);
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<Boolean> showConfirmRestoreBackupDownload(
            final List<StorageUtils.StorageInfo> bakDevices,
            final List<List<DownloadFileList>> downloadFileList) {
        Logger.d("showConfirmRestoreBackupDownload");
        if (!isAdded()) {
            return Observable.empty();
        }

        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                String fragTag = CONFIRM_RESTORE_DOWNLOAD_FRAG_TAG;
                removePreviousFragment(getFragmentManager(), fragTag);
                ConfirmRestoreDownloadFragment fragment =
                        ConfirmRestoreDownloadFragment.newInstance(bakDevices, downloadFileList);
                fragment.setListener(
                        new ConfirmRestoreDownloadFragment.OnConfirmRestoreDownloadListener() {
                            @Override
                            public void onSelect(boolean restoreDownload) {
                                if (!subscriber.isUnsubscribed()) {
                                    subscriber.onNext(restoreDownload);
                                }
                            }

                            @Override
                            public void onCancel() {
                                // user cancel
                            }
                        });
                fragment.setOnDialogDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
                fragment.show(getFragmentManager(), fragTag);
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<String> showSelectNewDownloadPath(
            List<StorageUtils.StorageInfo> devices, final long fileSize) {
        Logger.d("showSelectNewDownloadPath, fileSize : " + fileSize);
        if (!isAdded()) {
            return Observable.empty();
        }

        Observable.OnSubscribe<String> onSubscribe = new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {

                String fragTag = SELECT_PATH_FRAG_TAG;
                removePreviousFragment(getFragmentManager(), fragTag);

                // Create and show the dialog.
                SelectDownloadMediaAndPathFragment fragment =
                        SelectDownloadMediaAndPathFragment.newInstance(mFilmId, mMediaId,
                                fileSize);
                fragment.setListener(new SelectDownloadMediaAndPathFragment
                        .OnSelectDownloadMediaAndPathListener() {

                    @Override
                    public void onSelectedResult(String mediaId, String path) {
                        Logger.d(
                                "showSelectNewDownloadPath, onSelectedResult, "
                                        + "mediaId : " + mediaId + ", path : " + path);
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(path);
                        }
                    }

                    @Override
                    public void onCancel() {
                        Logger.d("showSelectNewDownloadPath, onCancel");
                        // user cancel
                    }
                });
                fragment.setOnDialogDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
                fragment.show(getFragmentManager(), fragTag);
            }
        };

        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<String> showSelectRestoreBackupDownload(
            final List<StorageUtils.StorageInfo> bakDevices,
            final List<List<DownloadFileList>> downloadFileList) {
        Logger.d("showSelectRestoreBackupDownload, bakDevices : " + bakDevices);
        if (!isAdded()) {
            return Observable.empty();
        }

        Observable.OnSubscribe<String> onSubscribe = new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                String fragTag = SELECT_RESTORE_DOWNLOAD_FRAG_TAG;
                removePreviousFragment(getFragmentManager(), fragTag);
                SelectRestoreDownloadFragment fragment =
                        SelectRestoreDownloadFragment.newInstance(bakDevices, downloadFileList);
                fragment.setListener(
                        new SelectRestoreDownloadFragment.OnSelectRestoreDownloadListener() {
                            @Override
                            public void onSelect(String path) {
                                if (!subscriber.isUnsubscribed()) {
                                    subscriber.onNext(path);
                                }
                            }

                            @Override
                            public void onCancel() {
                                // user cancel
                            }
                        });
                fragment.setOnDialogDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
                fragment.show(getFragmentManager(), fragTag);
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public void setPresenter(DownloadContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private DownloadContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    /**
     * Check write storage permission
     */
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkPermission = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (PackageManager.PERMISSION_GRANTED == checkPermission) {
                Logger.d("Permission is granted");
                return true;
            } else {
                Logger.d("Permission is revoked");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Logger.d("Permission is granted");
            return true;
        }
    }
}
