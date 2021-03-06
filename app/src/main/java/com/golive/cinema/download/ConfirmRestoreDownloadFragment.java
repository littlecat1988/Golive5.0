package com.golive.cinema.download;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.download.domain.model.DownloadFileList;
import com.golive.cinema.util.MathExtend;
import com.golive.cinema.util.StorageUtils;
import com.golive.cinema.util.UIHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Wangzj on 2016/12/2.
 */

public class ConfirmRestoreDownloadFragment extends BaseDialog implements View.OnClickListener {

    public interface OnConfirmRestoreDownloadListener {

        void onSelect(boolean restoreDownload);

        void onCancel();
    }

    private ArrayList<StorageUtils.StorageInfo> mStorageInfos;
    private ArrayList<ArrayList<DownloadFileList>> mFileList;
    private ViewGroup mStoragesVg;

    private OnConfirmRestoreDownloadListener mListener;

    public static ConfirmRestoreDownloadFragment newInstance(
            List<StorageUtils.StorageInfo> storageInfos,
            List<List<DownloadFileList>> fileList) {

        ArrayList<StorageUtils.StorageInfo> tmpStorages = new ArrayList<>(storageInfos);
        ArrayList<ArrayList<DownloadFileList>> tmpFileList = new ArrayList<>();
        for (Collection<DownloadFileList> list : fileList) {
            ArrayList<DownloadFileList> tmpList = new ArrayList<>(list);
            tmpFileList.add(tmpList);
        }

        ConfirmRestoreDownloadFragment fragment = new ConfirmRestoreDownloadFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.EXTRA_STORAGES, tmpStorages);
        bundle.putSerializable(Constants.EXTRA_FILES, tmpFileList);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mStorageInfos =
                (ArrayList<StorageUtils.StorageInfo>) bundle.getSerializable(
                        Constants.EXTRA_STORAGES);
        mFileList =
                (ArrayList<ArrayList<DownloadFileList>>) bundle.getSerializable(
                        Constants.EXTRA_FILES);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.confrim_restore_dl_frag, container, false);
        mStoragesVg = (ViewGroup) view.findViewById(R.id.storage_vg);
        View restoreDlView = view.findViewById(R.id.restore_dl_btn);
        View newDlView = view.findViewById(R.id.new_dl_btn);
        initView(inflater);
        restoreDlView.setOnClickListener(this);
        newDlView.setOnClickListener(this);
        restoreDlView.requestFocus();
        restoreDlView.requestFocusFromTouch();
        return view;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mListener != null) {
            mListener.onCancel();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restore_dl_btn:
                if (mListener != null) {
                    mListener.onSelect(true);
                }
                dismiss();
                break;
            case R.id.new_dl_btn:
                if (mListener != null) {
                    mListener.onSelect(false);
                }
                dismiss();
                break;
            default:
                break;
        }
    }

    private void initView(LayoutInflater inflater) {
        int size = mStorageInfos.size();
        for (int i = 0; i < size; i++) {
            View view = inflater.inflate(R.layout.restore_storage_btn, null);
            mStoragesVg.addView(view);
            if (1 == mStoragesVg.getChildCount()) {
                view.requestFocus();
                view.requestFocusFromTouch();
            } else if (2 == mStoragesVg.getChildCount()) {
                ViewGroup.MarginLayoutParams params =
                        (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                params.leftMargin = (int) getResources().getDimension(
                        R.dimen.download_sel_media_btn_storage_vg_margin_left);
                view.setLayoutParams(params);
            }

            View storageView = view.findViewById(R.id.btn);
            TextView nameTv = (TextView) view.findViewById(R.id.storage_name_tv);
            TextView capacityTv = (TextView) view.findViewById(R.id.storage_capacity_tv);
            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressbar);

            String txt = getString(R.string.download_sel_media_storage_num);
            txt = String.format(txt, String.valueOf(mStoragesVg.getChildCount()));
            nameTv.setText(txt);

            // storage view not focusable, no clickable
            storageView.setFocusable(false);
            storageView.setFocusableInTouchMode(false);
            storageView.setClickable(false);
//            storageView.setBackgroundResource(R.drawable.selector_bg_btn_storage_disable);

            ArrayList<DownloadFileList> fileLists = mFileList.get(i);
            long totalSize = 0;
            long completeSize = 0;
            for (DownloadFileList list : fileLists) {
                totalSize += list.mFileSize + list.mMd5FileSize;
                completeSize += list.mCompleteSize + list.mMd5CompleteSize;
            }

            String storageTxt = "";
            if (totalSize > 0) {
                if (completeSize == totalSize) {
                    storageTxt = getString(R.string.download_finish);
                } else {
                    double progress = MathExtend.divide(completeSize, totalSize * 0.01, 2);
                    storageTxt = String.format(
                            getString(R.string.download_restore_storage_already_progress),
                            progress + "%");
                    progressBar.setMax(100);
                    progressBar.setProgress((int) progress);
                    UIHelper.setViewVisible(progressBar, true);
                }
            }
            capacityTv.setText(Html.fromHtml(storageTxt));
        }
    }

    public void setListener(
            OnConfirmRestoreDownloadListener listener) {
        mListener = listener;
    }
}
