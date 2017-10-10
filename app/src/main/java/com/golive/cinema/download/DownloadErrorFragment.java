package com.golive.cinema.download;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.util.StorageUtils;
import com.golive.cinema.util.UIHelper;
import com.initialjie.download.DownloadConstants;

/**
 * Created by Wangzj on 2016/12/5.
 */

public class DownloadErrorFragment extends BaseDialog implements View.OnClickListener {

    public interface OnDownloadErrorListener {
        void onReDownload();

        void onCancel();
    }

    private String mFilmName;
    private String mFilePath;
    private int mErrCode;

    private boolean mCustomError;
    private String mErrTitle;
    private String mErrMsg;
    private boolean mReDownload;

    private OnDownloadErrorListener mListener;

    public static DownloadErrorFragment newInstance(String filmName, String filePath, int errCode) {
        DownloadErrorFragment fragment = new DownloadErrorFragment();
        Bundle argument = new Bundle();
        argument.putString(Constants.EXTRA_FILM_NAME, filmName);
        argument.putString(Constants.EXTRA_FILE_PATH, filePath);
        argument.putInt(Constants.EXTRA_ERR_CODE, errCode);
        fragment.setArguments(argument);
        return fragment;
    }

    public static DownloadErrorFragment newInstance(String errTitle, String errMsg,
            boolean reDownload) {
        DownloadErrorFragment fragment = new DownloadErrorFragment();
        Bundle argument = new Bundle();
        argument.putString(Constants.EXTRA_ERR_TITLE, errTitle);
        argument.putString(Constants.EXTRA_ERR_MESSAGE, errMsg);
        argument.putBoolean(Constants.EXTRA_REDOWNLOAD, reDownload);
        fragment.setArguments(argument);
        fragment.mCustomError = true;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle argument = getArguments();
        mFilmName = argument.getString(Constants.EXTRA_FILM_NAME);
        mFilePath = argument.getString(Constants.EXTRA_FILE_PATH);
        mErrCode = argument.getInt(Constants.EXTRA_ERR_CODE);
        mErrTitle = argument.getString(Constants.EXTRA_ERR_TITLE);
        mErrMsg = argument.getString(Constants.EXTRA_ERR_MESSAGE);
        mReDownload = argument.getBoolean(Constants.EXTRA_REDOWNLOAD);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.download_err_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView titleTv = (TextView) view.findViewById(R.id.title_tv);
        TextView descriptionTv = (TextView) view.findViewById(R.id.description_tv);
        Button okView = (Button) view.findViewById(R.id.ok_btn);
        View reDownloadView = view.findViewById(R.id.redownload_btn);

        String errTitle;
        String errMsg;
        boolean showReDownload = false;
        if (mCustomError) {
            errTitle = mErrTitle;
            errMsg = mErrMsg;
            showReDownload = mReDownload;
        } else {
            errTitle = String.format(getString(R.string.download_error_title), mFilmName);
            errMsg = DownloadUtils.getDownloadErrorDescription(getResources(), mErrCode);
            switch (mErrCode) {
                case DownloadConstants.ErrorCode.ERR_FILE_COULD_NOT_CREATE:
                case DownloadConstants.ErrorCode.ERR_FILE_COULD_NOT_READ:
                case DownloadConstants.ErrorCode.ERR_FILE_COULD_NOT_WRITE:
                case DownloadConstants.ErrorCode.ERR_FILE_ERROR:
                    String path = StorageUtils.getMountPath(mFilePath);
                    errMsg += ", " + getString(R.string.download_error_file_need_format)
                            + Constants.LINE_SEPARATOR + path;
//                    no break !
                case DownloadConstants.ErrorCode.ERR_FILE_NOT_EXIST:
                case DownloadConstants.ErrorCode.ERR_STORAGE_DEVICE_NOT_EXIST:
                case DownloadConstants.ErrorCode.ERR_FILE_TOO_LARGE:
                case DownloadConstants.ErrorCode.ERR_MD5_ERROR:
                    showReDownload = true;
                    break;
                default:
                    break;
            }
            errMsg = String.format(getString(R.string.download_error_description),
                    errMsg);
        }
        titleTv.setText(errTitle);
        descriptionTv.setText(errMsg);

        UIHelper.setViewVisibleOrGone(reDownloadView, showReDownload);
        if (showReDownload) {
            okView.setText(R.string.cancel);
        }
        okView.setOnClickListener(this);
        reDownloadView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_btn:
                if (mListener != null) {
                    mListener.onCancel();
                }

                // dismiss
                dismiss();
                break;
            case R.id.redownload_btn:
                if (mListener != null) {
                    mListener.onReDownload();
                }
                // dismiss
                dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (mListener != null) {
            mListener.onCancel();
        }
        super.onCancel(dialog);
    }

    public void setListener(OnDownloadErrorListener listener) {
        mListener = listener;
    }
}
