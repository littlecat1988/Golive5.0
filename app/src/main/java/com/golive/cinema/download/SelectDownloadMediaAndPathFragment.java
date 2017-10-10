package com.golive.cinema.download;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.adapter.RecyclerViewAdapterListener;
import com.golive.cinema.download.domain.model.DownloadMedia;
import com.golive.cinema.download.domain.model.DownloadStorage;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.Media;
import com.initialjie.log.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangzj on 2016/11/23.
 */

public class SelectDownloadMediaAndPathFragment extends BaseDialog implements
        SelectDownloadMediaAndPathContract.View {

    public interface OnSelectDownloadMediaAndPathListener {

        /**
         * Called when select media and path
         *
         * @param mediaId Media id
         * @param path    path
         */
        void onSelectedResult(String mediaId, String path);

        /**
         * Called when user cancel.
         */
        void onCancel();
    }

    private SelectDownloadMediaAndPathContract.Presenter mPresenter;
    private String mFilmId;
    private String mMediaId;
    private long mMediaSize;
    private boolean mOnlySelectPath;
    private ArrayList<Media> mMedias;
    //    private ArrayList<StorageUtils.StorageInfo> mStorageInfos;
    private ProgressDialog mLoadingDialog;
    private RecyclerView mMediasRv;
    private RecyclerView mStoragesRv;
    //    private ViewGroup mMediasVg;
//    private ViewGroup mStoragesVg;
    private View mNaviLeftView;
    private View mNaviRightView;

    private String mSelctedMediaId;
    private String mSelctedPath;
    private OnSelectDownloadMediaAndPathListener mListener;

    private List<DownloadStorage> mDownloadStorages;
    //    private List<String> mShowStorages;
    private CountDownTimer mCountDownTimer;
    private DownloadStoragesAdapter mStoragesAdapter;

    public static SelectDownloadMediaAndPathFragment newInstance(String filmId,
            @NonNull ArrayList<Media> mediaList
//            ,@NonNull ArrayList<StorageUtils.StorageInfo> storageList
    ) {
        SelectDownloadMediaAndPathFragment fragment = new SelectDownloadMediaAndPathFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.EXTRA_FILM_ID, filmId);
        bundle.putSerializable(Constants.EXTRA_MEDIAS, mediaList);
//        bundle.putSerializable(Constants.EXTRA_STORAGES, storageList);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static SelectDownloadMediaAndPathFragment newInstance(String filmId, String mediaId,
            long mediaSize) {
        SelectDownloadMediaAndPathFragment fragment = new SelectDownloadMediaAndPathFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.EXTRA_FILM_ID, filmId);
        bundle.putSerializable(Constants.EXTRA_MEDIA_ID, mediaId);
        bundle.putLong(Constants.EXTRA_MEDIA_SIZE, mediaSize);
        fragment.mOnlySelectPath = true;
        fragment.mSelctedMediaId = mediaId;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mFilmId = bundle.getString(Constants.EXTRA_FILM_ID);
        mMedias = (ArrayList<Media>) bundle.getSerializable(Constants.EXTRA_MEDIAS);
//        mStorageInfos =
//                (ArrayList<StorageUtils.StorageInfo>) bundle.getSerializable(
//                        Constants.EXTRA_STORAGES);
        mMediaId = bundle.getString(Constants.EXTRA_MEDIA_ID);
        mMediaSize = bundle.getLong(Constants.EXTRA_MEDIA_SIZE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.sel_dl_media_path_frag, container, false);
        mNaviLeftView = view.findViewById(R.id.download_sel_media_navigation_left);
        mNaviRightView = view.findViewById(R.id.download_sel_media_navigation_right);
//        mMediasVg = (ViewGroup) view.findViewById(R.id.download_sel_medias_vg);
        mMediasRv = (RecyclerView) view.findViewById(R.id.download_sel_medias_rv);
        mStoragesRv = (RecyclerView) view.findViewById(R.id.download_sel_storages_rv);
        mMediasRv.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        mMediasRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            private final int MARGIN = (int) getResources().getDimension(
                    R.dimen.download_sel_media_btn_margin_left);

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                    RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildAdapterPosition(view) != 0) {
                    outRect.left = MARGIN;
                }
            }
        });
        mStoragesRv.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false));
        mStoragesRv.addItemDecoration(new RecyclerView.ItemDecoration() {
            private final int MARGIN = (int) getResources().getDimension(
                    R.dimen.download_sel_media_btn_storage_vg_margin_left);

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                    RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildAdapterPosition(view) != 0) {
                    outRect.left = MARGIN;
                }
            }
        });
//        mStoragesVg = (ViewGroup) view.findViewById(R.id.download_sel_storages_vg);

        mNaviLeftView.setSelected(true);

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (KeyEvent.ACTION_DOWN == event.getAction() && KeyEvent.KEYCODE_BACK == keyCode) {
                    if (!mOnlySelectPath && mNaviRightView.isSelected()) {
                        switchView(0);
                        return true;
                    }
                }
                return false;
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SelectDownloadMediaAndPathContract.Presenter presenter;
        if (mOnlySelectPath) {
            presenter = new SelectDownloadMediaAndPathPresenter(this, mFilmId, mMediaId,
                    mMediaSize);
        } else {
            presenter = new SelectDownloadMediaAndPathPresenter(this, mFilmId, mMedias);
        }
        presenter.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
        if (getPresenter() != null) {
            getPresenter().unsubscribe();
        }
        if (mLoadingDialog != null) {
            UIHelper.dismissDialog(mLoadingDialog);
        }
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Logger.d("onCancel");
        if (mListener != null) {
            mListener.onCancel();
        }
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mLoadingDialog) {
                mLoadingDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.please_wait));
            }

            if (!mLoadingDialog.isShowing()) {
                mLoadingDialog.show();
            }
        } else {
            if (mLoadingDialog != null) {
                UIHelper.dismissDialog(mLoadingDialog);
            }
        }
    }

    @Override
    public void showDownloadMedias(final List<DownloadMedia> downloadMedias) {
        if (!isAdded()) {
            return;
        }

        DownloadMediasAdapter adapter = new DownloadMediasAdapter(getContext(), downloadMedias);
        adapter.setOnItemClickListener(new RecyclerViewAdapterListener.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Logger.d("onItemClicked, position : " + position);
                onSelectMedia(downloadMedias.get(position).mediaId);
            }
        });
        mMediasRv.setAdapter(adapter);
        mMediasRv.requestFocus();
//        mMediasRv.requestFocusFromTouch();

        // only one media
        if (downloadMedias != null && 1 == downloadMedias.size()) {
            // select it
            onSelectMedia(downloadMedias.get(0).mediaId);
        }
    }

    @Override
    public void showDownloadStorages(final List<DownloadStorage> downloadStorages) {
        if (!isAdded()) {
            return;
        }

        switchView(1);
        if (null == mStoragesAdapter) {
            mStoragesAdapter = new DownloadStoragesAdapter(getContext(),
                    downloadStorages);
            mStoragesAdapter.setOnItemClickListener(
                    new RecyclerViewAdapterListener.OnItemClickListener() {
                        @Override
                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                            Logger.d("onItemClicked, position : " + position);
                            if (mCountDownTimer != null) {
                                mCountDownTimer.cancel();
                            }
                            final DownloadStorage downloadStorage = downloadStorages.get(position);
                            if (downloadStorage.isCapacityEnough) {
                                onSelectStorage(downloadStorage.path);
                            }
                        }
                    });
            mStoragesAdapter.setOnItemSelectedListener(
                    new RecyclerViewAdapterListener.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(RecyclerView recyclerView, int position,
                                final View v) {
                            Logger.d("onItemSelected, position : " + position);
                            final DownloadStorage downloadStorage = downloadStorages.get(position);
                            // is the first position
                            if (0 == position) {
                                // recommend || capacity enough
                                if (null == mCountDownTimer && (downloadStorage.isRecommend
                                        || downloadStorage.isCapacityEnough)) {
                                    final DownloadStoragesAdapter.DownloadStoragesHolder holder =
                                            (DownloadStoragesAdapter.DownloadStoragesHolder)
                                                    mStoragesRv.getChildViewHolder(v);
                                    mCountDownTimer = new CountDownTimer(10000, 1000) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                            String str = getString(
                                                    R.string.download_sel_media_storage_auto_select);
                                            final String text = String.format(str,
                                                    millisUntilFinished / 1000);
                                            holder.autoSelTv.setVisibility(View.VISIBLE);
                                            holder.autoSelTv.setText(text);
                                        }

                                        @Override
                                        public void onFinish() {
                                            if (downloadStorage.isRecommend
                                                    || downloadStorage.isCapacityEnough) {
                                                onSelectStorage(downloadStorage.path);
                                            }
                                        }
                                    };
                                    mCountDownTimer.start();
                                }
                            } else {
                                if (mCountDownTimer != null) {
                                    mCountDownTimer.cancel();
                                }
                            }
                        }
                    });
            mStoragesRv.setAdapter(mStoragesAdapter);
        } else {
            mStoragesAdapter.replaceData(downloadStorages);
        }
        mStoragesRv.requestFocus();
//        mStoragesRv.requestFocusFromTouch();

        mDownloadStorages = downloadStorages;
    }

    @Override
    public void showAvailableStoragesCount(int count) {
        Logger.d("showAvailableStoragesCount, count : " + count);
        // only one available storage
        if (1 == count && mDownloadStorages != null && !mDownloadStorages.isEmpty()) {
            onSelectStorage(mDownloadStorages.get(0).path);
        }
    }

    @Override
    public void clearStorageDevices() {
//        mStoragesVg.removeAllViews();
    }

    @Override
    public void setPresenter(SelectDownloadMediaAndPathContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private SelectDownloadMediaAndPathContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private void onSelectMedia(String mediaId) {
        switchView(1);
        mSelctedMediaId = mediaId;
//        if (mShowStorages != null) {
//            // clear cache storages
//            mShowStorages.clear();
//        }
        getPresenter().selectMedia(mediaId);
    }

    private void onSelectStorage(String path) {
        if (!isAdded()) {
            return;
        }

        Logger.d("onSelectStorage, path : " + path);
        mSelctedPath = path;
        if (mListener != null) {
            mListener.onSelectedResult(mSelctedMediaId, mSelctedPath);
        }

        if (getView() != null) {
            getView().post(new Runnable() {
                @Override
                public void run() {
                    // dismiss
                    dismiss();
                }
            });
        }
    }

    private void switchView(int step) {
        boolean showStorage = 1 == step;
        mNaviLeftView.setSelected(!showStorage);
        mNaviRightView.setSelected(showStorage);
//        UIHelper.setViewVisibleOrGone(mMediasVg, !showStorage);
//        UIHelper.setViewVisibleOrGone(mStoragesVg, showStorage);
        UIHelper.setViewVisibleOrGone(mMediasRv, !showStorage);
        UIHelper.setViewVisibleOrGone(mStoragesRv, showStorage);
        if (!showStorage) {
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
            }
        }
    }

    public void setListener(OnSelectDownloadMediaAndPathListener listener) {
        mListener = listener;
    }
}
