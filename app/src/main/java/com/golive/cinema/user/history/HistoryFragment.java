package com.golive.cinema.user.history;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_USER_CENTER;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_WATCH_RECORD;
import static com.golive.cinema.user.usercenter.UserPublic.TipsDialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.golive.cinema.Constants;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.filmdetail.FilmDetailActivity;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.cinema.views.metroviews.widget.TvRecyclerView;
import com.golive.network.entity.HistoryMovie;
import com.golive.network.entity.MovieRecommendFilm;
import com.initialjie.log.Logger;

import java.util.ArrayList;
import java.util.List;


public class HistoryFragment extends MvpFragment implements HistoryContract.View {
    //private static final String TAG = HistoryFragment.class.getSimpleName();
    private static final int SCALE_DURATION = Constants.SCALE_DURATION;
    private static final float SCALE_FACTOR = Constants.SCALE_FACTOR;
    private static final int HANDLE_REFRESH_EDIT = 1;
    private static final int HANDLE_REFRESH_DEL = 2;
    private static final int HANDLE_FOCUS_HISTORY = 3;
    private static final int HANDLE_FOCUS_RECOMMEND = 4;
    private static final int HANDLE_REFRESH_DEL_FOCUS = 5;
    private static final int PAGE_MAIN = 0;
    private static final int PAGE_WAIT = 1;
    private static final int PAGE_NOITEM = 2;

    private HistoryContract.Presenter mPresenter;
    //    private CenterSelectListView mHlistView;
    private TvRecyclerView mHlistView;
    private TvRecyclerView mPosterRecyclerView;
    private UserMoviesScrollAdapter mAdapter;
    private Button mEditButton;
    private TextView mIntimeNumTv, mOuttimeNumTv, mAllfilmNumTv;
    private ProgressDialog mProgressDialog;
    private View mView1, mView2, mView3;
    private View mViewMain, mViewNoitem, mViewWait;
    private boolean mEditMode;
    private int mIntimeNum, mOuttimeNum, mAllfilmNum;
    private int mSaveDeletePosition;
    private int mSaveCurrFocus;
    private int mSaveCurrX;
    private int currShowPage;
    private long mEnterTime;
    private final List<HistoryFilm> mFilmList = new ArrayList<>();
    private final List<MovieRecommendFilm> mNoitemList = new ArrayList<>();
    //private int selectPos = 0;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mView1 = view.findViewById(R.id.user_history_img1);
        mView2 = view.findViewById(R.id.user_history_img2);
        mView3 = view.findViewById(R.id.user_history_img3);
        mViewMain = view.findViewById(R.id.user_history_main_view);
        mViewNoitem = view.findViewById(R.id.user_history_have_no_item_view);
        mViewWait = view.findViewById(R.id.user_history_waiting_view);
//        mTitleTv = (TextView) view.findViewById(R.id.user_history_title_tv);
        mPosterRecyclerView = (TvRecyclerView) view.findViewById(
                R.id.user_history_noitem_recommend_listview);
//        mHlistView = (CenterSelectListView) getActivity().findViewById(R.id
// .hListView_user_movies);
        mHlistView = (TvRecyclerView) view.findViewById(R.id.hListView_user_movies);
        mIntimeNumTv = (TextView) view.findViewById(R.id.tv_user_movies_unexpired_num);
        mOuttimeNumTv = (TextView) view.findViewById(R.id.user_tv_movies_expired_num);
        mAllfilmNumTv = (TextView) view.findViewById(R.id.user_tv_movies_all_num);
        mEditButton = (Button) view.findViewById(R.id.user_history_edit_btn);
        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditMode = !mEditMode;
                if (mAdapter != null) {
                    mAdapter.setEditMode(mEditMode);
                }
                myHandleMessage(HANDLE_REFRESH_EDIT, 1);
//                setEditShow(mEditMode);
//                setEditButtonShow(mEditMode);
            }
        });
        setCurrPageOnShow(PAGE_WAIT);
        initListView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("onActivityCreated");
        initVal();
        if (getPresenter() != null) {
            getPresenter().start();
        }

        mEnterTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(getContext()).reportEnterActivity(VIEW_CODE_WATCH_RECORD,
                "观影记录", VIEW_CODE_USER_CENTER);
    }

    private void setCurrPageOnShow(int page) {
        if (page == PAGE_WAIT) {
            mViewMain.setVisibility(View.GONE);
            mViewWait.setVisibility(View.VISIBLE);
            mViewNoitem.setVisibility(View.GONE);
        } else if (page == PAGE_NOITEM) {
            mViewMain.setVisibility(View.GONE);
            mViewWait.setVisibility(View.GONE);
            mViewNoitem.setVisibility(View.VISIBLE);
        } else {//PAGE_MAIN
            mViewMain.setVisibility(View.VISIBLE);
            mViewWait.setVisibility(View.GONE);
            mViewNoitem.setVisibility(View.GONE);
        }
        currShowPage = page;
        if (page == PAGE_NOITEM) {
            mEditMode = false;
        }
    }

    private void initVal() {
        mIntimeNum = 0;
        mOuttimeNum = 0;
        mAllfilmNum = 0;
        mEditMode = false;
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mProgressDialog) {
                mProgressDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.loading));
            }

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } else {
            if (mProgressDialog != null) {
                UIHelper.dismissDialog(mProgressDialog);
            }
        }
    }

    @Override
    public void setPresenter(HistoryContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    @Override
    public HistoryContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private void initListView() {
        mHlistView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.setSelected(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    itemView.clearAnimation();
                    itemView.animate().scaleX(1.0f).scaleY(1.0f).translationX(0f).setDuration(
                            SCALE_DURATION).start();
                }
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.setSelected(true);
                itemView.clearAnimation();
                //selectPos = position;
                int count = mHlistView.getChildCount();
                if (position == 0 && count > 5) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        itemView.animate().scaleX(SCALE_FACTOR).scaleY(SCALE_FACTOR).translationX(
                                6f).setDuration(SCALE_DURATION).start();
                    }
                } else if (position == (count - 1) && count > 5) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        itemView.animate().scaleX(SCALE_FACTOR).scaleY(SCALE_FACTOR).translationX(
                                -6f).setDuration(SCALE_DURATION).start();
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        itemView.animate().scaleX(SCALE_FACTOR).scaleY(SCALE_FACTOR).setDuration(
                                SCALE_DURATION).start();
                    }
                }
            }

            @Override
            public void onReviseFocusFollow(TvRecyclerView parent, View itemView, int position) {
                setItemViewBigSize(itemView, true);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                if (mEditMode) {
//                    saveClickPosition =position;
                    saveCurrFocusPositionX();
                    RemoveItemFromServer(position);
                } else {
                    String filmid = mFilmList.get(position).getFilmid();
                    if (!StringUtils.isNullOrEmpty(filmid)) {
                        showFilmDetailUI(filmid);
                    } else {
                        Toast.makeText(getContext(), R.string.film_detail_missing_film,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        float padding = getResources().getDimension(R.dimen.user_history_spading_l_r);
        mHlistView.setSpacingWithMargins((int) padding, 0);

        mAdapter = new UserMoviesScrollAdapter(this, mFilmList, mEditMode);
        mHlistView.setAdapter(mAdapter);
    }

    private void showFilmDetailUI(String filmid) {
        FilmDetailActivity.jumpToFilmDetailActivity(getContext(), filmid, VIEW_CODE_WATCH_RECORD,
                false, 0);
    }

    private void setItemViewBigSize(View view, boolean hasFocus) {
        if (null == view) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (hasFocus) {
                view.clearAnimation();
                view.animate().scaleX(1.129f).scaleY(1.108f).setDuration(SCALE_DURATION).start();
            } else {
                view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(SCALE_DURATION).start();
            }
        }
    }

    @Override
    public void showResultListView(List<HistoryMovie> lists) {
        if (lists != null && lists.size() > 0) {
            Logger.d("showVipListView size=" + lists.size());
            mFilmList.clear();
            for (HistoryMovie mOrder : lists) {
                if (mOrder != null) {
                    mFilmList.add(copyOrderToFilm(mOrder));
                }
            }

            setCurrPageOnShow(PAGE_MAIN);
            reflashListAdapter();
            float paddingL = 0;//getResources().getDimension(R.dimen.user_history_margin_l);
            int listsize = mFilmList.size();
            if (listsize <= 5) {
                float itemW = getResources().getDimension(R.dimen.user_history_item_w);
                paddingL = (itemW * 0.485f) * ((5 - listsize)) + itemW * 0.12f;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    mHlistView.clearAnimation();
                    mHlistView.animate().translationX(paddingL).setDuration(0).start();
                }
            }

            mMainHandler.sendEmptyMessageDelayed(HANDLE_FOCUS_HISTORY, 100);
        } else {
            setCurrPageOnShow(PAGE_NOITEM);
            initNoItemView();
        }
    }

    @Override
    public void showResultError(String errorCode) {
        Toast.makeText(getActivity(), errorCode, Toast.LENGTH_SHORT).show();
        setFailedGetListDialog(errorCode);
        setCurrPageOnShow(PAGE_NOITEM);
        initNoItemView();
    }

    private void reflashListAdapter() {
        Logger.d("reflashListAdapter");
        if (getActivity().isFinishing()) {
            return;
        }
        reflashNumber();
        mAdapter.notifyDataSetChanged();
    }

    private void reflashNumber() {
        mAllfilmNum = mFilmList.size();
        mOuttimeNum = getOutTimeNumber();
        mIntimeNum = mAllfilmNum - mOuttimeNum;
        mIntimeNumTv.setText(String.valueOf(mIntimeNum));
        mOuttimeNumTv.setText(String.valueOf(mOuttimeNum));
        mAllfilmNumTv.setText(String.valueOf(mAllfilmNum));
    }

    private int getOutTimeNumber() {
        int num = 0;
        if (mFilmList != null) {
            for (int i = 0; i < mFilmList.size(); i++) {
                HistoryFilm film = mFilmList.get(i);
                String active = film.getActive();
                if (!StringUtils.isNullOrEmpty(active) && "0".equals(active) || film.isTimeOut()) {
                    num++;
                }
            }
        }

        return num;
    }

    private void setEditButtonShow(boolean isshow) {
        if (isshow) {
            mEditButton.setText(getActivity().getString(R.string.history_finish_edit));
        } else {
            mEditButton.setText(getActivity().getString(R.string.history_into_edit));
        }
    }

    private View getViewFromPosition(int poition) {
        int first = mHlistView.getFirstVisiblePosition();
        int last = mHlistView.getLastVisiblePosition();
        if (last < first) {
            last = first;
        }

        if (poition < mFilmList.size() && poition <= last
                && poition >= first) {// mHlistView.getCount() poition < last
            return mHlistView.getChildAt(poition - first);
        }
        return null;
    }

    private void myHandleMessage(int msgId, int value) {
        if (mMainHandler != null) {
            Message msg = mMainHandler.obtainMessage();
            msg.what = msgId;
            msg.arg1 = value;
            mMainHandler.sendMessage(msg);
        }
    }

    private final Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_REFRESH_EDIT:
                    setEditButtonShow(mEditMode);
                    mAdapter.notifyDataSetChanged();
                    mMainHandler.sendEmptyMessageDelayed(HANDLE_FOCUS_HISTORY, 50);
                    break;
                case HANDLE_REFRESH_DEL:
                    if (mFilmList.size() == 0) {
                        setCurrPageOnShow(PAGE_NOITEM);
                        initNoItemView();
                    } else {
                        reflashListAdapter();
                        mMainHandler.sendEmptyMessageDelayed(HANDLE_REFRESH_DEL_FOCUS,
                                200);//setFocusListEnd();
                    }
                    break;
                case HANDLE_FOCUS_HISTORY:
                    mHlistView.requestFocus();
                    break;
                case HANDLE_FOCUS_RECOMMEND:
                    mPosterRecyclerView.requestFocus();
                    break;
                case HANDLE_REFRESH_DEL_FOCUS:
                    setFocusListEnd();
                    break;
                default:
                    break;
            }
        }
    };

    //推荐页
    @Override
    public void showMovieRecommend(List<MovieRecommendFilm> content) {
        if (null == content) {
            return;
        }
        int size = content.size();
        Logger.d("showMovieRecommend,size=" + size);
        if (size > 0) {
            mNoitemList.clear();
            mNoitemList.addAll(content);
        }

        if (currShowPage == PAGE_NOITEM) {
            initNoItemView();
        }
    }

    private void initNoItemView() {
        int size = mNoitemList.size();
        mView1.setVisibility(size == 1 ? View.VISIBLE : View.GONE);
        mView2.setVisibility(size == 2 ? View.VISIBLE : View.GONE);
        mView3.setVisibility(size == 3 ? View.VISIBLE : View.GONE);

        //mPosterRecyclerView.setSpacingWithMargins(10, 0);
        mPosterRecyclerView.setOnItemListener(new TvRecyclerView.OnItemListener() {

            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.setSelected(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    itemView.clearAnimation();
                    itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(
                            SCALE_DURATION).start();
                }
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.setSelected(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    itemView.clearAnimation();
                    itemView.animate().scaleX(SCALE_FACTOR).scaleY(SCALE_FACTOR).setDuration(
                            SCALE_DURATION).start();
                }
            }

            @Override
            public void onReviseFocusFollow(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                String filmId = mNoitemList.get(position).getReleaseid();
                if (!StringUtils.isNullOrEmpty(filmId)) {
                    showFilmDetailUI(filmId);
                } else {
                    Toast.makeText(getContext(), R.string.film_detail_missing_film,
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        mPosterRecyclerView.setAdapter(new RecommendPostersAdapter(this, mNoitemList));
        mMainHandler.sendEmptyMessageDelayed(HANDLE_FOCUS_RECOMMEND, 100);
    }

    private HistoryFilm copyOrderToFilm(HistoryMovie mOrder) {
        HistoryFilm afilm = new HistoryFilm();
        afilm.setPayType(mOrder.getproductType());
        afilm.setFilmid(mOrder.getproductId());
        afilm.setName(mOrder.getmoiveName());
        afilm.setBigcover(mOrder.getposterUrl());
//            afilm.setStart_time(mOrder.get);
        afilm.setExpirationTime(mOrder.getexpirationTime());
//            afilm.setOrderstatus(mOrder.getStatus());
        afilm.setOrderSerial(mOrder.getserial());
        afilm.setOrderMediaId(mOrder.getmediaResourceId());
        afilm.setActive(mOrder.getIsOnline());
        afilm.setExpired(mOrder.getendTime());

//            if (Order.STATUS_ORDER_FINISH.equals(mOrder.getStatus())){
//                afilm.setValiTime("0");
//            }else if (Order.STATUS_PAY_SUCCESS.equals(mOrder.getStatus())){
//                afilm.setValiTime("--");
//            }else{
        String vali = checkValiTimeRemain(mOrder.getremain());
        afilm.setValiTime(vali);
//            }

        return afilm;
    }

    private String checkValiTimeRemain(String remain) {
        if (StringUtils.isNullOrEmpty(remain)) {
            return "";
        }

        if (remain.startsWith("-") || remain.equals("0")) {
            return "0";
        }

        String vali = "";
        try {
            float ms = Float.parseFloat(remain);
            if (ms <= 0) {
                return "0";
            }
            float ss = ms / 1000;
            float hh = ss / 3600;
            int hh_int = (int) hh + 1;
            /*if (hh_int > 9999) {
                hh_int = 9999;
            }*/
            vali = String.valueOf(hh_int);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return vali;
    }

    private void saveCurrFocusPositionX() {
        if (mHlistView.getChildCount() <= 0) {
            return;
        }
        mSaveCurrFocus = mHlistView.getSelectedPosition();
        mSaveCurrX = 0;
        View pview = getViewFromPosition(mSaveCurrFocus);
        if (pview != null) {
            mSaveCurrX = pview.getLeft();
        }
    }

    //删除刷新
    private void setFocusListEnd() {
        final int maxPosition = mFilmList.size() - 1;//mHlistView.getCount()-1
        if (maxPosition >= 0) {
            if (mSaveDeletePosition >= maxPosition) {
                mHlistView.setSelection(maxPosition);
            } else {
                if (maxPosition < 5) {
                    mHlistView.setSelection(mSaveDeletePosition);
                } else {
                    if (mSaveCurrFocus >= 0
                            && mSaveCurrFocus <= maxPosition) {//mSaveCurrX >=0
                        mHlistView.setSelection(mSaveCurrFocus);
//                        mHlistView.setSelectionFromLeft(mSaveCurrFocus, mSaveCurrX);
                    }
                }
            }
        } else {
            //tab focus
        }
    }

    private void RemoveItemFromServer(int position) {
        Logger.d("RemoveItemFromServer position=" + position);
        if (position >= 0 && position < mFilmList.size()) {
            HistoryFilm film = mFilmList.get(position);
            if (film != null) {
                HistoryContract.Presenter presenter = getPresenter();
                if (presenter != null) {
                    presenter.deleteHistory(position, film);
                }
            }
        }
    }

    @Override
    public void reFlashDelete(int position, boolean seccuss) {
        Logger.d("RemoveItemInView position=" + position);
        if (seccuss) {
            if (position < mFilmList.size()) {
                saveCurrFocusPositionX();
                mSaveDeletePosition = position;
                mFilmList.remove(position);
                myHandleMessage(HANDLE_REFRESH_DEL, 0);
            }
        } else {
            Toast.makeText(getContext(), R.string.history_watch_delete_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void setFailedGetListDialog(final String errorCode) {
        final Dialog aDialog = TipsDialog(getContext(), R.layout.user_topup_successfull_tips);
        TextView title = (TextView) aDialog
                .findViewById(R.id.tv_user_pay_dlg_title);
        TextView detail = (TextView) aDialog
                .findViewById(R.id.tv_user_pay_dlg_detail);
        String titleText, detailText;
        titleText = getActivity().getString(R.string.history_getlist_failed);
        detailText = String.format(getString(R.string.active_vip_failed_reason), errorCode);

        title.setText(titleText);
        detail.setText(detailText);

        Button okButton = (Button) aDialog.findViewById(R.id.user_dialog_pay_bt_yes);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.dismiss();
//                getActivity().finish();
            }
        });

        aDialog.show();
    }

    @Override
    public void onDestroy() {
        Logger.d("onDestroy ");
        String time = String.valueOf((System.currentTimeMillis() - mEnterTime) / 1000);
        StatisticsHelper.getInstance(getActivity()).reportExitActivity(VIEW_CODE_WATCH_RECORD,
                "观影记录", "", time);
        mMainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
