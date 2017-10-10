package com.golive.cinema.theater.sync;

import static com.golive.cinema.Constants.FIRST_FOCUS_RECOMMEND;
import static com.golive.cinema.Constants.FIRST_FOCUS_THEATER;
import static com.golive.cinema.Constants.LAST_FOCUS_THEATER;
import static com.golive.cinema.Constants.LAST_FOCUS_USER;
import static com.golive.cinema.Constants.SCALE_DURATION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MAIN_ACTIVITY;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MAIN_FILM_LIST;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.MainActivity;
import com.golive.cinema.MainFragment;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.filmdetail.FilmDetailActivity;
import com.golive.cinema.films.FilmsContract;
import com.golive.cinema.films.FilmsPresenter;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.views.metroviews.widget.TvRecyclerView;
import com.golive.network.entity.Film;
import com.golive.network.response.FilmListResponse;

import java.util.List;

/**
 * Created by Administrator on 2016/9/23.
 * 在线院线
 */

public class TheaterSyncFragment extends MvpFragment implements FilmsContract.View {
    private static final int MSG_BORDER_FOCUSED = 0;
    private static final int MSG_BORDER_UNFOCUSED = 1;
    private static final int MSG_BORDER_VISIBLE = 2;
    private static final int MSG_BORDER_INVISIBLE = 3;
    private static final int MSG_POSTER_ANIMATION = 4;
    private static final int MSG_POSTER_FOCUSED = 5;
    private static final int MSG_GLOW_INVISIBLE = 6;
    private static final int MSG_INDEX_INVISIBLE = 7;
    private static final int MSG_VIEW_RESET = 8;
    private static final int MSG_LEFT_KEY_PRESSED = 9;

    private FilmsContract.Presenter mPresenter;
    private TvRecyclerView mRecyclerView;
    private TheaterSyncPageAdapter mLayoutAdapter;
    private ImageView mImageLeftGlowIv, mImageBorderIv;
    //private final String CURRENT_FILTERING_KEY = "CURRENT_FILTERING_KEY";

    private int mRepeatCount;
    private int mFilmCount, mSelectPos = -1, mPosterOffset;
    private long mStartTime;
    private FilmListResponse mFilmListResponse;
    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver mReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.theater_sync_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mPosterOffset = metrics.widthPixels * 20 / 1000;
        mRecyclerView = (TvRecyclerView) view.findViewById(R.id.theater_sync_list);
        mImageBorderIv = (ImageView) view.findViewById(R.id.image_border);
        mImageLeftGlowIv = (ImageView) view.findViewById(R.id.left_glow);
        initView();
    }

    private void initView() {
        mRecyclerView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                if (mRepeatCount > 0) {
                    mHandler.removeMessages(MSG_LEFT_KEY_PRESSED);
                    mRepeatCount = 0;
                }
                mLayoutAdapter.setIndexNum(position, false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    itemView.clearAnimation();
                    itemView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(SCALE_DURATION).start();
                }
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                if (MainFragment.isBackClick) {
                    MainFragment.isBackClick = false;
                    return;
                }

                mSelectPos = position;
                mLayoutAdapter.setIndexNum(position, true);
                mHandler.removeMessages(MSG_INDEX_INVISIBLE);
                mHandler.sendEmptyMessageDelayed(MSG_INDEX_INVISIBLE, 3100);
                mHandler.sendEmptyMessage((position > 1 && position < mFilmCount - 2)
                        ? MSG_BORDER_FOCUSED : MSG_BORDER_UNFOCUSED);
                mRecyclerView.setSelectedItemAtCentered(
                        position > 0 && position < mFilmCount - 1);

                itemView.setOnKeyListener(mKeyListener);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    itemView.clearAnimation();
                    itemView.animate().scaleX(1.25f).scaleY(1.25f).setDuration(SCALE_DURATION).start();
                }

                if (position == 0) {
                    mHandler.sendEmptyMessage(MSG_LEFT_KEY_PRESSED);
                }
            }

            @Override
            public void onReviseFocusFollow(TvRecyclerView parent, View itemView, int position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    itemView.clearAnimation();
                    itemView.animate().scaleX(1.25f).scaleY(1.25f).setDuration(SCALE_DURATION).start();
                }
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                String filmId = mFilmListResponse.getData().getContent().get(
                        position).getReleaseid();
                if (!StringUtils.isNullOrEmpty(filmId)) {
                    getPresenter().openFilmDetail(filmId);
                }
            }
        });

        mRecyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mHandler.sendEmptyMessage(MSG_BORDER_UNFOCUSED);
                    if (mRepeatCount > 0) {
                        mHandler.removeMessages(MSG_LEFT_KEY_PRESSED);
                        mRepeatCount = 0;
                    }
                }
            }
        });

        int spacing = (int) getResources().getDimension(R.dimen.theater_sync_recycler_view_spacing);
        mRecyclerView.setSpacingWithMargins(-spacing, 0);
    }

    private final View.OnKeyListener mKeyListener = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getRepeatCount() == 0)) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        mHandler.sendEmptyMessage(MSG_BORDER_UNFOCUSED);
                        if (isAdded()) {
                            View view = getActivity().findViewById(MainFragment.tabSelectId);
                            if (view != null) {
                                view.requestFocus();
                            }
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (mSelectPos == 0) {
                            /*if (mImageLeftGlowIv.getVisibility() != View.VISIBLE) {
                                mHandler.sendEmptyMessage(MSG_POSTER_ANIMATION);
                            }*/
                            mLocalBroadcastManager.sendBroadcast(
                                    new Intent(Constants.GOTO_PAGE_USER));
                        } else if (mSelectPos == 2) {
                            mRecyclerView.setSelectedItemAtCentered(false);
                            mRecyclerView.setSelectedItemOffset(mPosterOffset, mPosterOffset);
                        } else if (mSelectPos == mFilmCount - 2) {
                            mRecyclerView.setSelectedItemAtCentered(true);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if (mSelectPos == 1) {
                            mRecyclerView.setSelectedItemAtCentered(true);
                        } else if (mSelectPos == mFilmCount - 3) {
                            mRecyclerView.setSelectedItemAtCentered(false);
                            mRecyclerView.setSelectedItemOffset(mPosterOffset, mPosterOffset);
                        }
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        mHandler.sendEmptyMessage(MSG_BORDER_UNFOCUSED);
                        break;
                    default:
                        break;
                }
            }
            return false;
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BORDER_FOCUSED:
                    if (mImageBorderIv.getVisibility() != View.VISIBLE) {
                        mHandler.sendEmptyMessageDelayed(MSG_BORDER_VISIBLE, SCALE_DURATION);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            mImageBorderIv.animate().scaleX(1.25f).scaleY(1.25f).setDuration(
                                    SCALE_DURATION).start();
                        }
                    }
                    break;
                case MSG_BORDER_UNFOCUSED:
                    if (mImageBorderIv.getVisibility() == View.VISIBLE) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            mImageBorderIv.animate().scaleX(1.0f).scaleY(1.0f).setDuration(
                                    SCALE_DURATION).start();
                        }
                        mHandler.sendEmptyMessageDelayed(MSG_BORDER_INVISIBLE, SCALE_DURATION);
                        mImageBorderIv.setVisibility(View.INVISIBLE);
                    }
                    break;
                case MSG_BORDER_VISIBLE:
                    mImageBorderIv.setVisibility(View.VISIBLE);
                    break;
                case MSG_BORDER_INVISIBLE:
                    mImageBorderIv.setVisibility(View.INVISIBLE);
                    break;
                case MSG_POSTER_ANIMATION:
                    //海报阻尼动画
                    mImageLeftGlowIv.setVisibility(View.VISIBLE);
                    mHandler.sendEmptyMessageDelayed(MSG_GLOW_INVISIBLE, 400);

                    AnimatorSet posterAnimation = new AnimatorSet();
                    posterAnimation.playSequentially(
                            ObjectAnimator.ofFloat(mRecyclerView, "translationX", 0, mPosterOffset),
                            ObjectAnimator.ofFloat(mRecyclerView, "translationX", mPosterOffset,
                                    0));
                    posterAnimation.setDuration(200);
                    posterAnimation.start();
                    break;
                case MSG_POSTER_FOCUSED:
                    //使海报获取焦点
                    if (mSelectPos == -1) {
                        mRecyclerView.requestFocus();
                        mHandler.sendEmptyMessageDelayed(MSG_POSTER_FOCUSED, 500);
                    }
                    break;
                case MSG_GLOW_INVISIBLE:
                    mImageLeftGlowIv.setVisibility(View.INVISIBLE);
                    break;
                case MSG_INDEX_INVISIBLE:
                    mLayoutAdapter.setIndexNum(mSelectPos, false);
                    break;
                case MSG_VIEW_RESET:
                    MainFragment.isBackClick = false;
                    mRecyclerView.setDefaultSelected(0);
                    mRecyclerView.scrollToPosition(0);
                    mRecyclerView.smoothScrollToPosition(0);
                    break;
                case MSG_LEFT_KEY_PRESSED:
                    if (MainActivity.isLeftKeyPressed) {
                        mRepeatCount = 0;
                        mLocalBroadcastManager.sendBroadcast(new Intent(Constants.GOTO_PAGE_USER));
                    } else if (mRepeatCount++ < 10) {
                        mHandler.sendEmptyMessageDelayed(MSG_LEFT_KEY_PRESSED, 100);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Create the presenter
        Context context = getContext().getApplicationContext();
        mPresenter = new FilmsPresenter(this, Injection.provideGetFilms(context)
                , Injection.provideGetFilmList(context));
        mPresenter.loadFilms();

        initBroadcastReceiver();
        // Load previously saved state, if available.
        /*if (savedInstanceState != null) {
            FilmsFilterType currentFiltering =
                    (FilmsFilterType) savedInstanceState.getSerializable(CURRENT_FILTERING_KEY);
            mPresenter.setFiltering(currentFiltering);
        }*/
        //mPresenter.start();
        mStartTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(getContext()).reportEnterActivity(VIEW_CODE_MAIN_FILM_LIST,
                "首页海报圈", VIEW_CODE_MAIN_ACTIVITY);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Context context = getContext();
        if (isVisibleToUser) {
            if (MainFragment.isBackClick) {
                MainFragment.isTheater = false;
                mImageBorderIv.setVisibility(View.INVISIBLE);
                mHandler.sendEmptyMessageDelayed(MSG_VIEW_RESET, 100);
            } else if (MainFragment.isTheater) {
                MainFragment.isTheater = false;
                mRecyclerView.setDefaultSelected(0);
                mRecyclerView.scrollToPosition(0);
                mRecyclerView.smoothScrollToPosition(0);
            }

            if (mLocalBroadcastManager != null) {
                mLocalBroadcastManager.sendBroadcast(new Intent(FIRST_FOCUS_RECOMMEND));
                mLocalBroadcastManager.sendBroadcast(new Intent(LAST_FOCUS_USER));
            }

            if (mStartTime == 0 && context != null) {
                mStartTime = System.currentTimeMillis();
                StatisticsHelper.getInstance(context).reportEnterActivity(VIEW_CODE_MAIN_FILM_LIST,
                        "首页海报圈", VIEW_CODE_MAIN_ACTIVITY);
            }
        } else if (mStartTime != 0 && context != null) {
            String duration = String.valueOf((System.currentTimeMillis() - mStartTime) / 1000);
            StatisticsHelper.getInstance(context).reportExitActivity(VIEW_CODE_MAIN_FILM_LIST,
                    "首页海报圈", "", duration);
            mStartTime = 0;
        }
    }

    @Override
    public void onDestroy() {
        removeAllMessage();
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void removeAllMessage() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void initBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mFilmCount > 0) {
                    String action = intent.getAction();
                    if (FIRST_FOCUS_THEATER.equals(action)) {
                        if (mRecyclerView.getOldSelectedPosition() != 0) {
                            mRecyclerView.setDefaultSelected(0);
                            mRecyclerView.smoothScrollToPosition(0);
                        }
                    } else if (LAST_FOCUS_THEATER.equals(action)) {
                        if (mRecyclerView.getOldSelectedPosition() != mFilmCount - 1) {
                            mRecyclerView.setDefaultSelected(mFilmCount - 1);
                            mRecyclerView.smoothScrollToPosition(mFilmCount - 1);
                        }
                    } else {
                        mHandler.sendEmptyMessageDelayed(MSG_POSTER_FOCUSED, 500);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("poster_default_focus");
        filter.addAction(Constants.INIT_SPLASH_BROADCAST_ACTION);
        filter.addAction(FIRST_FOCUS_THEATER);
        filter.addAction(LAST_FOCUS_THEATER);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

    @Override
    public void showFilms(List<Film> films) {

    }

    @Override
    public void showFilms(FilmListResponse films) {
        if (films.getData() != null && films.getData().getContent() != null) {
            List<FilmListResponse.Content> content = films.getData().getContent();
//            content.addAll(content);
//            content.addAll(content);
            mFilmCount = content.size();
            if (mFilmCount > 0) {
                mFilmListResponse = films;
                mLayoutAdapter = new TheaterSyncPageAdapter(this, films.getData().getContent());
                mRecyclerView.setAdapter(mLayoutAdapter);
                mHandler.sendEmptyMessageDelayed(MSG_POSTER_FOCUSED, 500);
            }
        }
    }

    @Override
    public void showFilmDetailUi(@NonNull String filmId) {
        FilmDetailActivity.jumpToFilmDetailActivity(getContext(), filmId, VIEW_CODE_MAIN_FILM_LIST,
                false, 0);
    }

    @Override
    public void showLoadingFilmsError() {

    }

    @Override
    public void showAllFilterLabel() {

    }

    @Override
    public void showOnNowFilterLabel() {

    }

    @Override
    public void showUpComingFilterLabel() {

    }

    @Override
    public void showEndingFilterLabel() {

    }

    @Override
    public void showNoFilms() {

    }

    @Override
    public void showNoOnNowFilms() {

    }

    @Override
    public void showNoUpcomingFilms() {

    }

    @Override
    public void showEndingFilms() {

    }

    @Override
    public void setLoadingIndicator(boolean active) {

    }

    @Override
    public void setPresenter(FilmsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    protected FilmsContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }
}
