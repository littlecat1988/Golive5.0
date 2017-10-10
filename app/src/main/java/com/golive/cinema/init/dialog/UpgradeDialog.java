package com.golive.cinema.init.dialog;

import static com.golive.cinema.Constants.EXTRA_UPGRADE_CODE;
import static com.golive.cinema.Constants.EXTRA_UPGRADE_URL;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.PackageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.ToastUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.cinema.views.UpgradeDownloadImageView;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.log.Logger;

import java.io.File;

/**
 * Created by chgang on 2016/11/3.
 */

public class UpgradeDialog extends BaseDialog implements View.OnClickListener,
        UpgradeDownloadTask.OnDownloadListener {

    public static final String DIALOG_FRAGMENT_TAG = "upgrade_fragment_tag";
    private static final String upgradeDownloadId = "upgrade_download_id";
    //    private SharedPreferences sharedPreferences;
    private Button mCommitBtn, mCancelBtn;
    private TextView mUpgradeContentTv, mTitleTv, mServiceTv;
    private RelativeLayout mUpgradeClickRl;
    private UpgradeDownloadImageView mDownloadImageView;
    private int mUpgradeType;
    private String mUpgradeUrl;
    private File mAppFile;
    private UpgradeDownloadTask mDownloadTask;

    private OnUpgradeListener onUpgradeListener = null;

    public interface OnUpgradeListener {
        void onCompleted();

        void onCancel();

        void onExit();
    }

    public void setOnUpgradeListener(OnUpgradeListener onUpgradeListener) {
        this.onUpgradeListener = onUpgradeListener;
    }

    public static UpgradeDialog newInstance(String upgradeUrl, int upgradeCode) {
        UpgradeDialog fragment = new UpgradeDialog();
        Bundle args = new Bundle();
        args.putString(EXTRA_UPGRADE_URL, upgradeUrl);
        args.putInt(EXTRA_UPGRADE_CODE, upgradeCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        mUpgradeUrl = arguments.getString(EXTRA_UPGRADE_URL);
//        mUpgradeUrl = "http://sw.bos.baidu
// .com/sw-search-sp/software/0c2f474b78238/jre-8u111-windows-i586_8.0.1110.14.exe";
        mUpgradeType = arguments.getInt(EXTRA_UPGRADE_CODE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.upgrade_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.setCancelable(false);

        mUpgradeContentTv = (TextView) view.findViewById(R.id.upgrade_content_tv);
        mCommitBtn = (Button) view.findViewById(R.id.commit_btn);
        mCancelBtn = (Button) view.findViewById(R.id.cancel_btn);
        mCommitBtn.setOnClickListener(this);
        mCancelBtn.setOnClickListener(this);
        mDownloadImageView = (UpgradeDownloadImageView) view.findViewById(R.id.app_download_layout);
        mUpgradeClickRl = (RelativeLayout) view.findViewById(R.id.upgrade_click_layout);
        mTitleTv = (TextView) view.findViewById(R.id.upgrade_title);
        mServiceTv = (TextView) view.findViewById(R.id.service_phone);
        String phone = UserInfoHelper.getServicePhone(getContext());
        String qq = UserInfoHelper.getServiceQQ(getContext());
        if (!StringUtils.isNullOrEmpty(phone) && !StringUtils.isNullOrEmpty(qq)) {
            mServiceTv.setText(
                    String.format(getString(R.string.init_service_phone_qq), phone, qq));
        }

        mDownloadTask = new UpgradeDownloadTask(getActivity(), mUpgradeUrl);
//        if (MyApplication.isTCL && Constants.UPGRADE_TYPE_OPTIONAL_REMOTE == mUpgradeType) {
//            mUpgradeContentTv.setText(getString(R.string.dialog_update_info_normal1));
//            mCancelBtn.setVisibility(View.GONE);
//            RelativeLayout.LayoutParams params =
//                    (RelativeLayout.LayoutParams) mCommitBtn.getLayoutParams();
//            params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            params.addRule(RelativeLayout.CENTER_IN_PARENT);
//            mCommitBtn.setLayoutParams(params);
//            mCommitBtn.setText(getString(R.string.dialog_update_btn_txt_yes1));
//            mCommitBtn.setOnClickListener(this);
//            mCommitBtn.setTag(false);
//            mCancelBtn.setTag(false);
//        } else {
        if (mUpgradeType == Constants.UPGRADE_TYPE_AUTO_OPTIONAL_FORCE) {
            mTitleTv.setText(R.string.dialog_update_install_title);
            mUpgradeContentTv.setText(R.string.content_install);
            mCommitBtn.setTag(false);
//            mCommitBtn.setText(R.string.btn_install);
            mCommitBtn.setText(R.string.dialog_update_btn_download);
            mCancelBtn.setTag(true);
            mCancelBtn.setText(R.string.btn_exit);
        } else {
            String filePath = getContext().getFilesDir().getAbsolutePath();
            File localFile = new File(filePath, Constants.UPGRADE_FILE_NAME);
            if (localFile.exists()) {
                localFile.delete();
            }

//            if (!localFile.exists()) {
            mTitleTv.setText(R.string.dialog_update_info_title);
            mCommitBtn.setText(R.string.dialog_update_btn_download);
            mCommitBtn.setTag(false);
            mCancelBtn.setText(R.string.btn_later);
            mUpgradeContentTv.setText(R.string.dialog_update_info_normal);
//            } else {
//                mTitleTv.setText(R.string.dialog_update_install_title);
//                mCommitBtn.setText(R.string.btn_install);
//                mCommitBtn.setTag(true);
//                mCancelBtn.setText(R.string.btn_later);
//                mUpgradeContentTv.setText(R.string.content_install);
//            }
            mCancelBtn.setTag(false);
                /*RelativeLayout.LayoutParams contentParams =
                        (RelativeLayout.LayoutParams) mUpgradeContentTv.getLayoutParams();
                contentParams.topMargin = 408;
                mUpgradeContentTv.setLayoutParams(contentParams);

                RelativeLayout.LayoutParams layoutParam =
                        (RelativeLayout.LayoutParams) mUpgradeClickRl.getLayoutParams();
                layoutParam.topMargin = 174;
                mUpgradeClickRl.setLayoutParams(layoutParam);
                mUpgradeClickRl.setVisibility(View.VISIBLE);*/
        }
//        }
    }

    @Override
    public void onClick(View v) {
        Logger.d("tag:" + v.getTag().toString());

        if (v.getId() == R.id.commit_btn) {
//            if (MyApplication.isTCL && Constants.UPGRADE_TYPE_OPTIONAL_REMOTE == mUpgradeType) {
//                dismiss();
//                if(onUpgradeListener != null){
//                    onUpgradeListener.onCancel();
//                }
//            }
//            else {
            if (Boolean.parseBoolean(v.getTag().toString())) {
                //安装
                if (mAppFile == null) {
                    ToastUtils.showToast(getActivity(), getString(R.string.tip_download));
                } else {
                    Logger.d("filePath:" + mAppFile.getAbsolutePath());
//                    String path = filePath.substring("file://".length());
//                    Logger.d("path:" + path);
//                    File file = new File(path);
//                    if (file.exists()) {
//                    }
                    ProgressDialog progressDialog = UIHelper.generateSimpleProgressDialog(
                            getContext(), null,
                            getString(R.string.content_install_ongoing));
                    progressDialog.show();
                    PackageUtils.installApp(getActivity(), mAppFile);
                }
            } else {
                if (!StringUtils.isNullOrEmpty(mUpgradeUrl)) {

//                    String filePath = getContext().getFilesDir().getAbsolutePath();
//                    File localFile = new File(filePath, Constants.UPGRADE_FILE_NAME);
//                    File localFile = null;
//                    long fileId = sharedPreferences.getLong(upgradeDownloadId, 0);
//                    if (fileId != 0) {
//                        filePath = mDownloadTask.getExistApkPath(fileId);
//                        if (!StringUtils.isNullOrEmpty(filePath)) {
//                            String subPath = filePath.substring("file://".length());
//                            localFile = new File(subPath);
//                        }
//                    }

//                    if (localFile == null || !localFile.exists()) {
                    mTitleTv.setText(R.string.tip_download);
                    mUpgradeContentTv.setText(R.string.content_install);
                    mCommitBtn.setText(R.string.btn_install);
                    mCommitBtn.setTag(true);
                    mCancelBtn.setText(R.string.btn_exit);
                    mCancelBtn.setTag(true);
                    mUpgradeClickRl.setVisibility(View.GONE);
                    mDownloadImageView.setVisibility(View.VISIBLE);
                    mDownloadTask.setOnDownloadListener(UpgradeDialog.this);
                    mDownloadTask.execute();
//                    } else {
//                            /*RelativeLayout.LayoutParams contentParams =
//                                    (RelativeLayout.LayoutParams) mUpgradeContentTv
// .getLayoutParams();
//                            contentParams.topMargin = 408;
//                            mUpgradeContentTv.setLayoutParams(contentParams);*/
//                        mUpgradeContentTv.setText(R.string.content_install);
//
//                            /*RelativeLayout.LayoutParams layoutParam =
//                                    (RelativeLayout.LayoutParams) mUpgradeClickRl
// .getLayoutParams();
//                            layoutParam.topMargin = 174;
//                            mUpgradeClickRl.setLayoutParams(layoutParam);*/
//                        mUpgradeClickRl.setVisibility(View.VISIBLE);
//                        mTitleTv.setText(R.string.dialog_update_install_title);
//                        mCommitBtn.setText(R.string.btn_install);
//                        mCommitBtn.setTag(true);
//                        mCancelBtn.setText(R.string.btn_exit);
//                        mCancelBtn.setTag(true);
//                        mDownloadImageView.setVisibility(View.GONE);
//                    }
                }
//                }
            }
        } else if (v.getId() == R.id.cancel_btn) {
            if (Boolean.parseBoolean(v.getTag().toString())) {
                if (onUpgradeListener != null) {
                    if (mDownloadTask != null) {
                        mDownloadTask.setOnDownloadListener(null);
                        mDownloadTask = null;
                    }
                    dismiss();
                    onUpgradeListener.onExit();
                }
            } else {
                dismiss();
                if (onUpgradeListener != null) {
                    onUpgradeListener.onCancel();
                }
            }
        }
    }

    @Override
    public void onDownloading() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUpgradeContentTv.setText(R.string.content_install);
            }
        });
    }

    @Override
    public void onDownloadCompleted(File file) {
        Logger.d("onDownloadCompleted---------------file:" + file.getAbsolutePath());
        this.mAppFile = file;
        /*RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) mUpgradeContentTv.getLayoutParams();
        params.topMargin = 400;
        mUpgradeContentTv.setLayoutParams(params);*/
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDownloadImageView.setVisibility(View.GONE);
                mTitleTv.setText(R.string.dialog_update_install_title);
                mUpgradeContentTv.setText(R.string.content_update_downloaded);
                mUpgradeClickRl.setVisibility(View.VISIBLE);
                if (mUpgradeType == Constants.UPGRADE_TYPE_AUTO_OPTIONAL_FORCE) {
                    mCancelBtn.setText(R.string.btn_exit);
                } else {
                    mCancelBtn.setText(R.string.btn_later);
                    mCancelBtn.setTag(false);
                }
                mCommitBtn.setText(R.string.btn_install);
                mCommitBtn.requestFocus();
            }
        });

    }

    @Override
    public void onDownloadProgress(long current, long total) {
        Logger.d("registerDownloadObServer---------------total:" + total + ",current:" + current);
        if (mDownloadImageView != null) {
            mDownloadImageView.onProgressMessage(current, total);
        }
    }

    @Override
    public void onDownloadError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTitleTv.setText(R.string.dialog_update_install_title);
                mUpgradeContentTv.setText(R.string.dialog_update_info_undown);
                mDownloadImageView.setVisibility(View.GONE);
                mUpgradeClickRl.setVisibility(View.VISIBLE);
                mCommitBtn.setText(R.string.retry_btn_text);
                mCommitBtn.setTag(false);
                mCancelBtn.setText(R.string.btn_exit);
                mCancelBtn.setTag(true);
                StatisticsHelper.getInstance(getContext()).reportAppException("", "", "升级包下载异常",
                        "1");
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
        }
    }
}
