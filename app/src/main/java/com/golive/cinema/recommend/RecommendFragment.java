package com.golive.cinema.recommend;

import static com.golive.cinema.Constants.ADVERT_REQUEST_CODE;
import static com.golive.cinema.Constants.ADVERT_STARTMODE_GUIDE_RECOMMEND;
import static com.golive.cinema.Constants.EXTRA_BASE_PAGE_ID;
import static com.golive.cinema.Constants.FIRST_FOCUS_RECOMMEND;
import static com.golive.cinema.Constants.FIRST_FOCUS_USER;
import static com.golive.cinema.Constants.LAST_FOCUS_RECOMMEND;
import static com.golive.cinema.Constants.LAST_FOCUS_THEATER;
import static com.golive.cinema.Constants.SCALE_DURATION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MAIN_ACTIVITY;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MAIN_RECOMMEND;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.golive.cinema.Constants;
import com.golive.cinema.IBasePresenter;
import com.golive.cinema.Injection;
import com.golive.cinema.MainFragment;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.advert.AdvertHelper;
import com.golive.cinema.filmdetail.FilmDetailActivity;
import com.golive.cinema.filmlibrary.FilmLibraryActivity;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.user.history.HistoryActivity;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.views.metroviews.widget.TvRecyclerView;
import com.golive.network.response.RecommendResponse;
import com.hwangjr.rxbus.RxBus;

import java.util.List;

/**
 * Created by Administrator on 2016/9/23.
 * 推荐
 */

public class RecommendFragment extends MvpFragment implements RecommendContract.View {
    private TextView mTitleTv;
    private TvRecyclerView mRecyclerView;
    private RecommendPageAdapter mLayoutAdapter;
    private int mSelectPos, mCount;
    private int mScreenWidth, mScreenHeight, mTranX;
    private long mStartTime;
    private boolean mTabFocus;
    private boolean mIsMoreGrid;
    private List<RecommendResponse.Items> mItemsList;
    private RecommendContract.Presenter mPresenter;
    private BroadcastReceiver mReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recommend_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mTranX = mScreenWidth * 125 / 2000;
        mTitleTv = (TextView) view.findViewById(R.id.recommend_title);
        mRecyclerView = (TvRecyclerView) view.findViewById(R.id.recommend_list);
    }

    private void initView() {
        mCount = mItemsList.size();
        mRecyclerView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    itemView.clearAnimation();
                    itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(
                            SCALE_DURATION).start();
                }
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.clearAnimation();
                if (mIsMoreGrid) {
                    int x = mItemsList.get(position).getLocation().getX();
                    int w = mItemsList.get(position).getLocation().getW();
                    if ((x == 5 || (x < 5 && (x + w > 5))) && (mRecyclerView.getTranslationX()
                            > 0)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            mRecyclerView.animate().translationX(0).setDuration(
                                    SCALE_DURATION).start();
                        }
                    } else if (x == 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            mRecyclerView.animate().translationX(mTranX).setDuration(
                                    SCALE_DURATION).start();
                        }
                    } else if ((x == 2 || (x < 2 && (x + w > 2))) && (
                            mRecyclerView.getTranslationX() < 0)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            mRecyclerView.animate().translationX(0).setDuration(
                                    SCALE_DURATION).start();
                        }
                    } else if (position == mCount - 1) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            mRecyclerView.animate().translationX(-mTranX).setDuration(
                                    SCALE_DURATION)
                                    .start();
                        }
                    }
                }

                mSelectPos = position;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    itemView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(
                            SCALE_DURATION).start();
                }
                itemView.setOnKeyListener(mKeyListener);
                mTabFocus = mScreenHeight >> 1 < itemView.getBottom();
            }

            @Override
            public void onReviseFocusFollow(TvRecyclerView parent, View itemView, int position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    itemView.clearAnimation();
                    itemView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(
                            SCALE_DURATION).start();
                }
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                clickAction(mItemsList.get(position));
            }
        });

        int spacing = (int) getResources().getDimension(R.dimen.recycler_view_item_space);
        mRecyclerView.setSpacingWithMargins(-spacing, -spacing);
        mLayoutAdapter = new RecommendPageAdapter(this, mItemsList);
        mRecyclerView.setAdapter(mLayoutAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            mRecyclerView.animate().translationX(mTranX).setDuration(SCALE_DURATION).start();
        }
        mIsMoreGrid = mLayoutAdapter.mIsMore;
    }

    private void clickAction(RecommendResponse.Items item) {
        String actionContent = item.getActionContent();
        if (StringUtils.isNullOrEmpty(actionContent)) {
            return;
        }

        Context context = getContext();
        if (1 == item.getActionType() && !"-1".equals(actionContent)) {
            FilmDetailActivity.jumpToFilmDetailActivity(context, actionContent,
                    VIEW_CODE_MAIN_RECOMMEND, false, 0);
        } else {
            FragmentActivity activity = getActivity();
            if (2 == item.getActionType() && "1".equals(actionContent)) {
                //片库
                FilmLibraryActivity.navigateTo(getContext(), false);
            } else if (2 == item.getActionType() && "2".equals(actionContent)) {
                //观影记录
                Intent intent = new Intent(activity, HistoryActivity.class);
                intent.putExtra("USER_CLASS", "");
                activity.startActivityForResult(intent, 2);
            } else if (3 == item.getActionType() && "1".equals(actionContent)) {
                //天天赚钱
                boolean ok = AdvertHelper.goAdvert(activity, ADVERT_REQUEST_CODE,
                        ADVERT_STARTMODE_GUIDE_RECOMMEND);
                if (ok) {
                    String caller = "4";
                    RxBus.get().post(Constants.EventType.TAG_AD_ENTER, caller);
                    StatisticsHelper helper = StatisticsHelper.getInstance(
                            activity.getApplicationContext());
                    helper.reportEnterAd(caller);
                } else {
                    Toast.makeText(context, getString(R.string.advert_empty_tips),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(context, R.string.loading_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int id = getArguments().getInt(EXTRA_BASE_PAGE_ID, 1);
        Context applicationContext = getContext().getApplicationContext();
        mPresenter = new RecommendPresenter(this,
                Injection.provideGetRecommendUseCase(applicationContext));
        mPresenter.loadRecommend(String.valueOf(id));
        initBroadcastReceiver();
//        RxBus.get().register(this);
    }

    @Override
    public void onDestroy() {
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
//        RxBus.get().unregister(this);
        super.onDestroy();
    }

    private final View.OnKeyListener mKeyListener = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getRepeatCount() == 0)) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (mTabFocus) {
                            View view = getActivity().findViewById(MainFragment.tabSelectId);
                            if (view != null) {
                                view.requestFocus();
                            }
                        } else {
//                            return mLayoutAdapter.isFocusPrefer(mSelectPos, keyCode);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
//                        mRecyclerView.smoothScrollToPosition();
//                        return mLayoutAdapter.isFocusPrefer(mSelectPos, keyCode);
                    default:
                        break;
                }
            }
            return false;
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Context context = getContext();
        if (isVisibleToUser) {
            if (MainFragment.isRecommend) {
                MainFragment.isRecommend = false;
                if (mLayoutAdapter != null && mLayoutAdapter.mTabFocusPosition != -1) {
                    mRecyclerView.setDefaultSelected(mLayoutAdapter.mTabFocusPosition);
                    if (mIsMoreGrid) {
                        mRecyclerView.smoothScrollToPosition(0);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            mRecyclerView.animate().translationX(mTranX).setDuration(
                                    SCALE_DURATION).start();
                        }
                    }
                }
            }

            mLocalBroadcastManager.sendBroadcast(new Intent(LAST_FOCUS_THEATER));
            mLocalBroadcastManager.sendBroadcast(new Intent(FIRST_FOCUS_USER));

            if (context != null) {
                mStartTime = System.currentTimeMillis();
                StatisticsHelper.getInstance(context).reportEnterActivity(VIEW_CODE_MAIN_RECOMMEND,
                        "首页推荐", VIEW_CODE_MAIN_ACTIVITY);
            }
        } else if (mStartTime != 0 && context != null) {
            String duration = String.valueOf((System.currentTimeMillis() - mStartTime) / 1000);
            StatisticsHelper.getInstance(context).reportExitActivity(VIEW_CODE_MAIN_RECOMMEND,
                    "首页推荐", "", duration);
        }
    }

    private void initBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (mCount > 0) {
                    if (FIRST_FOCUS_RECOMMEND.equals(action)) {
                        if (mRecyclerView.getOldSelectedPosition() != 0) {
                            mRecyclerView.setDefaultSelected(0);
                            if (mIsMoreGrid) {
                                mRecyclerView.smoothScrollToPosition(0);
                                if (Build.VERSION.SDK_INT
                                        >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                    mRecyclerView.animate().translationX(mTranX).setDuration(
                                            SCALE_DURATION).start();
                                }
                            }
                        }
                    } else if (LAST_FOCUS_RECOMMEND.equals(action)) {
                        if (mRecyclerView.getOldSelectedPosition()
                                != mLayoutAdapter.mLastFocusPosition) {
                            mRecyclerView.setDefaultSelected(mLayoutAdapter.mLastFocusPosition);
                            if (mIsMoreGrid) {
                                mRecyclerView.smoothScrollToPosition(
                                        mLayoutAdapter.mLastFocusPosition);
                                if (Build.VERSION.SDK_INT
                                        >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                    mRecyclerView.animate().translationX(-mTranX).setDuration(
                                            SCALE_DURATION).start();
                                }
                            }
                        }
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(FIRST_FOCUS_RECOMMEND);
        filter.addAction(LAST_FOCUS_RECOMMEND);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

    @Override
    protected IBasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setPresenter(RecommendContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showView(RecommendResponse response) {
        if (response.getLayout() != null
                && response.getLayout().getItems() != null
                && !response.getLayout().getItems().isEmpty()) {
            mTitleTv.setText(response.getLayout().getTitle());
            mItemsList = response.getLayout().getItems();
            initView();
        }
    }

    @Override
    public void showError(String msg) {
        String text = String.format(getString(R.string.recommend_get_failed), msg);
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }
}
