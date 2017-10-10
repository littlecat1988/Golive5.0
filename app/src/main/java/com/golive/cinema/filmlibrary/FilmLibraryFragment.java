package com.golive.cinema.filmlibrary;

import static com.golive.cinema.Constants.INCLUDE_TOPICS_DEFAULT;
import static com.golive.cinema.Constants.SCALE_DURATION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_FILM_LIB;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MAIN_ACTIVITY;

import android.animation.ObjectAnimator;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.adapter.RecyclerViewAdapterListener;
import com.golive.cinema.filmdetail.FilmDetailActivity;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.topic.past.PastTopicAdapter;
import com.golive.cinema.topic.past.PastTopicFragment;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.cinema.views.TabTextView;
import com.golive.cinema.views.metroviews.widget.TvRecyclerView;
import com.golive.network.entity.FilmTopic;
import com.golive.network.response.FilmLibListResponse;
import com.golive.network.response.FilmLibTabResponse;
import com.initialjie.log.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Wangzj on 2017/5/25.
 */

public class FilmLibraryFragment extends MvpFragment<FilmLibraryContract.Presenter> implements
        FilmLibraryContract.View, View.OnClickListener, PastTopicFragment.OnPastItemSelectListener {

    private static final int MSG_HIDE_SCROLLBAR = 0;
    private static final int MSG_SHOW_SCROLLBAR = 1;
    private static final int MSG_REFRESH_VIEW = 2;
    private static final int MSG_UPDATE_BACKGROUND = 3;
    private static final int MSG_FOCUSED_POSTER = 4;
    private static final int MSG_SIMULATE_KEY = 5;
    private static final int MSG_SCROLLBAR_FOCUS = 6;
    private static final int MSG_SCROLL_PREPARE = 7;
    private static final int MSG_ANIMA_ALPHA = 8;
    private static final int MSG_START_UPDATE_BACKGROUND = 9;

    private static final float SCALE_UP_VALUE = 1.1f;
    private static final float SCALE_DOWN_VALUE = 1f;

    private FilmLibraryContract.Presenter mPresenter;
    private View mLastSelectedView;
    private View mFilmInfoVg;
    private View mTopicView;
    private View mFilmLibVg;
    private TabTextView mSelectedTabView;
    private ImageView mBgIv, mLineViewIv;
    private ImageView mScrollbarsIv, mImageBorderIv;
    private final ImageView[] mImageStarIvs = new ImageView[5];
    private TextView mIndexTextTv, mFilmNameTv, mFilmInfoTv;
    private TvRecyclerView mRecyclerView;
    private FilmLibraryAdapter mLayoutAdapter;
    private LinearLayout mTabLayout;
    private RelativeLayout mScrollbarLayout;
    private ProgressDialog mProgressDialog;
    private int mScreenWidth, mScreenHeight;
    //    private int mTabSelectId;
    private int mCurrentItem;
    private String mCurrentFilmTabId;
    private int mSelectPos, mFilmCount, mPosVar;
    private int mScrollbarWidth, mY, mScrollKey;
    private int mDefaultPos = Integer.MAX_VALUE / 2;
    private long mStartTime;
    private boolean mIsKeyDown, mScrollFlag, mPosFlag = true;
    private boolean mFirst = true;
    private boolean mManualScale;
    private ObjectAnimator mBgAnim;
    private Instrumentation mInst;
    private Bitmap mGlideDrawable;
    private List<FilmLibListResponse.Content> mFilmList;
    private AsyncTask<Bitmap, Void, Palette> mPaletteTask;
    private Subscription mAnalogSendKeySubscription;
    private PastTopicFragment mPastTopicFragment;
    private int mTabSize;
    private boolean mIsKeyListener;
    private boolean mIncludeTopics = INCLUDE_TOPICS_DEFAULT;
    private boolean mShowTopics;

    public static FilmLibraryFragment newInstance(boolean includeTopics, boolean showTopics) {
        FilmLibraryFragment fragment = new FilmLibraryFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(Constants.EXTRA_INCLUDE_TOPICS, includeTopics);
        bundle.putBoolean(Constants.EXTRA_SHOW_TOPICS, showTopics);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mIncludeTopics = bundle.getBoolean(Constants.EXTRA_INCLUDE_TOPICS,
                    INCLUDE_TOPICS_DEFAULT);
            mShowTopics = bundle.getBoolean(Constants.EXTRA_SHOW_TOPICS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.film_libiary, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;

        mScrollbarWidth = mScreenWidth;
        mY = mScreenHeight * 26 / 1000;

        initView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mPresenter != null) {
            mPresenter.start();
        }
        Context context = getContext().getApplicationContext();
        mStartTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(context).reportEnterActivity(VIEW_CODE_FILM_LIB, "片库",
                VIEW_CODE_MAIN_ACTIVITY);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.unsubscribe();
        }
        if (mLayoutAdapter != null) {
            mLayoutAdapter.clearData();
        }
        if (mPaletteTask != null) {
            mPaletteTask.cancel(true);
        }
        if (mAnalogSendKeySubscription != null) {
            mAnalogSendKeySubscription.unsubscribe();
        }
        removeAllMessages();

        String duration = String.valueOf((System.currentTimeMillis() - mStartTime) / 1000);
        StatisticsHelper.getInstance(getContext()).reportExitActivity(VIEW_CODE_FILM_LIB, "片库", "",
                duration);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mProgressDialog) {
                mProgressDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.history_getlist_waitting));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
    public void showTabs(List<FilmLibTabResponse.Content> contents) {
        if (null == contents || contents.isEmpty()) {
            return;
        }

        int textSize = getResources().getDimensionPixelSize(R.dimen.film_library_tab_text_size);
        int textColor = ContextCompat.getColor(getContext(), R.color.film_lib_tab_text_color);
        int left = mScreenWidth * 124 / 1000;
        int bottom = mScreenWidth * 30 / 1000;
        int size = contents.size();
        mTabSize = size;
        Logger.d("include topics : " + mIncludeTopics);
        int tmpSize = mIncludeTopics ? size + 1 : size;
        for (int i = 0; i < tmpSize; i++) {
            TabTextView textView = new TabTextView(getContext());
            textView.setTabIndex(i);
            textView.setLineView_left(left);
            textView.setLineView_bottom(bottom);
            textView.setTextColor(textColor);
            textView.setTextSize(textSize);
            textView.setNormalColor();
            if (mIncludeTopics && i == size) {
                textView.setText(R.string.topic_old);
            } else {
                FilmLibTabResponse.Content content = contents.get(i);
                textView.setText(content.getTitle());
                textView.setTag(content.getId());
            }
            textView.addToParentView(mTabLayout, i);
            textView.setOnClickListener(this);
            if (i == 0 || mIncludeTopics && mShowTopics && i == size) {
//                mTabSelectId = textView.getId();
                mCurrentItem = i;
                mSelectedTabView = textView;
                textView.requestFocus();
            }
        }

        if (mIncludeTopics) {
            mTopicView.setVisibility(View.INVISIBLE);
            mPastTopicFragment = PastTopicFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.content_past,
                    mPastTopicFragment).commit();
            mPastTopicFragment.setOnPastItemSelectListener(this);
        }
    }

    @Override
    public void showFilm() {
        if (null == mTabLayout || mTabLayout.getChildCount() <= 0
                || mCurrentItem < 0 || mCurrentItem >= mTabLayout.getChildCount()) {
            return;
        }

//        String tagId = null;
//        TabTextView tabTextView = (TabTextView) mTabLayout.getChildAt(mCurrentItem);
//        if (tabTextView != null && tabTextView.getTag() != null) {
//            tagId = tabTextView.getTag().toString();
//        }
//        loadFilmsByTabId(tagId);

        TabTextView tabTextView = (TabTextView) mTabLayout.getChildAt(mCurrentItem);
        if (tabTextView != null) {
            onSelectTab(tabTextView.getTabIndex());
        }
    }

    @Override
    public void showFilms(final String tabId, List<FilmLibListResponse.Content> filmContents) {
        // not current film tab
        if (isCurrentFilmTab(tabId)) {
            return;
        }

        if (null == filmContents || filmContents.isEmpty()) {
            hideFilms();
            Toast.makeText(getContext(), R.string.film_library_no_film_of_type,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mFilmList = filmContents;
        mFilmCount = filmContents.size();

        mScrollbarWidth = mScreenWidth / mFilmCount;
        setScrollbarWidth(mScrollbarWidth);

        mPosVar = 0;
        mPosFlag = true;
        mManualScale = true;
        updateRecyclerViewAdapter(filmContents);

        mSelectPos = 0;
        mHandler.sendEmptyMessageDelayed(MSG_REFRESH_VIEW, SCALE_DURATION);

        if (mRecyclerView.getVisibility() != View.VISIBLE) {
            mRecyclerView.setVisibility(View.VISIBLE);
//            mImageBorderIv.setVisibility(View.VISIBLE);
            mFilmInfoVg.setVisibility(View.VISIBLE);
        }
    }

    private boolean isCurrentFilmTab(String tabId) {
        return !StringUtils.isNullOrEmpty(mCurrentFilmTabId) && !StringUtils.isNullOrEmpty(tabId)
                && tabId.equals(mCurrentFilmTabId);
    }

    private void hideFilms() {
        mIndexTextTv.setText("");
        mRecyclerView.setVisibility(View.INVISIBLE);
        mImageBorderIv.setVisibility(View.INVISIBLE);
        mFilmInfoVg.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showError(String errMsg) {
        String text = getString(R.string.film_library_load_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            text += ", " + errMsg;
        }
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoadFilmsByIdFailed(String tabId, String errMsg) {
        if (!isCurrentFilmTab(tabId)) {
            return;
        }
        String text = getString(R.string.film_library_get_films_by_type_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            text += ", " + errMsg;
        }
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TabTextView) {
            int tabIndex = ((TabTextView) v).getTabIndex();
            if (mCurrentItem != tabIndex) {
                onSelectTab(tabIndex);
            }
        }
    }

    private void onSelectTab(int tabIndex) {
        mCurrentItem = tabIndex;
        if (mTabLayout != null && mTabLayout.getChildCount() > 0) {
            // clear last selected tab view
            if (mSelectedTabView != null) {
                mSelectedTabView.setSelectedOrPressed(false, mLineViewIv);
            }
            TabTextView tabView = ((TabTextView) mTabLayout.getChildAt(tabIndex));
            mSelectedTabView = tabView;
            tabView.setSelectedOrPressed(true, mLineViewIv);
            mLineViewIv.setVisibility(View.INVISIBLE);
            if (mScrollbarsIv != null) {
                if (mScrollbarsIv.isFocusable()) {
                    mHandler.sendEmptyMessage(MSG_HIDE_SCROLLBAR);
                }
            } else {
                return;
            }
            //控制pastTopic  show还是hide
            if (mIncludeTopics && tabIndex == mTabSize) {
                mTopicView.setVisibility(View.VISIBLE);
                hideOrShowView(View.INVISIBLE);
                return;
            }
            mTopicView.setVisibility(View.INVISIBLE);
            hideOrShowView(View.VISIBLE);
            hideFilms();
            String tabId = tabView.getTag().toString();
            loadFilmsByTabId(tabId);
        }
    }

    private void loadFilmsByTabId(String tabId) {
        if (!StringUtils.isNullOrEmpty(tabId)) {
            mCurrentFilmTabId = tabId;
            mPresenter.loadFilmsByTabId(tabId);
        }
    }

    @Override
    protected FilmLibraryContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setPresenter(FilmLibraryContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private void initView() {
        View view = getView();
        mBgIv = (ImageView) view.findViewById(R.id.film_library_bg);
        mFilmLibVg = view.findViewById(R.id.library_list_layout);
        mFilmInfoVg = view.findViewById(R.id.film_info_layout);
        mFilmNameTv = (TextView) view.findViewById(R.id.film_name);
        mFilmInfoTv = (TextView) view.findViewById(R.id.film_info);
        mIndexTextTv = (TextView) view.findViewById(R.id.index_text);
        mTabLayout = (LinearLayout) view.findViewById(R.id.tab_layout);
        mLineViewIv = (ImageView) view.findViewById(R.id.tab_panel_line);
        mImageBorderIv = (ImageView) view.findViewById(R.id.library_image_border);
        mTopicView = view.findViewById(R.id.content_past);
        int imageStarId[] = {R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5};
        for (int i = 0; i < 5; i++) {
            mImageStarIvs[i] = (ImageView) view.findViewById(imageStarId[i]);
        }

        initScrollBar();
        initRecyclerView();

        //背景色透明度渐变动画
        mBgAnim = ObjectAnimator.ofFloat(mBgIv, "alpha", 1f, 0.8f, 1f);
        mBgAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        mBgAnim.setDuration(1000);

//        setRecyclerViewAdapter();
    }

    private void initScrollBar() {
        final View view = getView();
        mScrollbarsIv = (ImageView) view.findViewById(R.id.scrollbars);
        if (mFilmCount > 0) {
            mScrollbarWidth = mScreenWidth / mFilmCount;
            setScrollbarWidth(mScrollbarWidth);
        }

        mScrollbarLayout = (RelativeLayout) view.findViewById(R.id.scrollbar_layout);
        mScrollbarsIv.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (event.getRepeatCount() == 0)) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            if (!mScrollFlag) {
                                mScrollFlag = true;
                                mScrollKey = keyCode;
                                if (Build.VERSION.SDK_INT
                                        >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                    if (mLastSelectedView != null) {
                                        mLastSelectedView.animate()
                                                .scaleX(SCALE_DOWN_VALUE)
                                                .scaleY(SCALE_DOWN_VALUE)
                                                .translationY(0f)
                                                .setDuration(SCALE_DURATION)
                                                .start();
                                    }
                                }
                                mHandler.removeMessages(MSG_SCROLL_PREPARE);
                                mHandler.sendEmptyMessageDelayed(MSG_SCROLL_PREPARE,
                                        SCALE_DURATION);
                            }
                            return true;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
//                            view.findViewById(mTabSelectId).requestFocus();
                            if (mSelectedTabView != null) {
                                mSelectedTabView.requestFocus();
                            }
                            return true;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            return mScrollFlag;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        mScrollbarsIv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                Logger.d("onFocusChange, mScrollbarsIv , hasFocus : " + hasFocus);
                if (!hasFocus) {
                    mHandler.sendEmptyMessageDelayed(MSG_HIDE_SCROLLBAR, 2000);
                }
            }
        });
    }

    private void initRecyclerView() {
        final View view = getView();
        mRecyclerView = (TvRecyclerView) view.findViewById(R.id.film_library_list);
//        mRecyclerView.setDrawInOrder(true);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.setOnItemListener(new TvRecyclerView.OnItemListener() {

            @Override
            public void onItemPreSelected(TvRecyclerView parent, final View itemView,
                    int position) {
//                Logger.d("onItemPreSelected, position : " + position);
                if (!mIsKeyDown) {
//                if (!mIsKeyDown && !mScrollFlag) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        itemView.clearAnimation();
                        itemView.animate()
                                .scaleX(SCALE_DOWN_VALUE)
                                .scaleY(SCALE_DOWN_VALUE)
                                .translationY(0f)
                                .setDuration(SCALE_DURATION)
                                .start();
                    }
                }
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
//                Logger.d("onItemSelected, position : " + position + ", view : " + itemView);
                mLastSelectedView = itemView;
                mDefaultPos = position;
                mSelectPos = convertPos(position);
                itemView.setOnKeyListener(mKeyListener);
                if (!mIsKeyDown) {
                    if (!mScrollFlag) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            itemView.clearAnimation();
                            itemView.animate()
                                    .scaleX(SCALE_UP_VALUE)
                                    .scaleY(SCALE_UP_VALUE)
                                    .translationY(-mY)
                                    .setDuration(SCALE_DURATION)
                                    .start();
                        }
                        mHandler.removeMessages(MSG_START_UPDATE_BACKGROUND);
                        mHandler.removeMessages(MSG_UPDATE_BACKGROUND);
//                        mHandler.removeMessages(MSG_REFRESH_VIEW);
                        mHandler.sendEmptyMessageDelayed(MSG_REFRESH_VIEW, SCALE_DURATION);
                    }
                } else {
                    mIsKeyDown = false;
                }
            }

            @Override
            public void onReviseFocusFollow(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                String filmId = mFilmList.get(mSelectPos).getReleaseid();
                FilmDetailActivity.jumpToFilmDetailActivity(getContext(), filmId,
                        VIEW_CODE_FILM_LIB, false, 0);
            }
        });

        mRecyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!mScrollFlag) {
                    UIHelper.setViewVisible(mImageBorderIv, hasFocus);
                    if (!hasFocus && !mIsKeyDown) {
                        mIsKeyDown = true;
                    }

                    if (hasFocus) {
                        mLineViewIv.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        final int spacing = (int) getResources().getDimension(R.dimen.film_library_item_space);
        mRecyclerView.setSpacingWithMargins(-spacing, 0);
//        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
//            @Override
//            public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
//                    RecyclerView.State state) {
//                super.getItemOffsets(outRect, view, parent, state);
//                if (parent.getChildLayoutPosition(view) != 0) {
//                    outRect.left = -spacing;
//                }
//            }
//        });
    }

    private void updateRecyclerViewAdapter(List<FilmLibListResponse.Content> filmList) {
        mFilmList = filmList;
        if (null == mLayoutAdapter) {
            mLayoutAdapter = new FilmLibraryAdapter(this);
            mLayoutAdapter.setAdapterListener(new RecyclerViewAdapterListener() {
                @Override
                public void onViewRecycled(RecyclerView.ViewHolder holder) {
                    if (!mManualScale) {
                        return;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        View itemView = holder.itemView;
                        itemView.clearAnimation();
                        itemView.animate()
                                .scaleX(SCALE_DOWN_VALUE)
                                .scaleY(SCALE_DOWN_VALUE)
                                .translationY(0f)
                                .setDuration(0)
                                .start();
                    }
                }

                @Override
                public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
                    // need manual scale && current pos == last select pos
                    if (mManualScale && holder.getAdapterPosition() == mDefaultPos) {
                        mManualScale = false;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            View itemView = holder.itemView;
                            itemView.clearAnimation();
                            itemView.animate()
                                    .scaleX(SCALE_UP_VALUE)
                                    .scaleY(SCALE_UP_VALUE)
                                    .translationY(-mY)
                                    .setDuration(0)
                                    .start();
                        }
                    }
                }

                @Override
                public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
                }
            });
            mLayoutAdapter.setData(filmList);
            mRecyclerView.setAdapter(mLayoutAdapter);
            mRecyclerView.setDefaultSelected(mDefaultPos);
            mRecyclerView.scrollToPosition(mDefaultPos);
            mHandler.sendEmptyMessageDelayed(MSG_FOCUSED_POSTER, 100);
        } else {
            mLayoutAdapter.setData(filmList);
        }
    }

    private int convertPos(final int position) {
        if (mPosFlag) {
            mPosFlag = false;
            if (mFilmCount > 2) {
                while (((position + mPosVar) % mFilmCount) != 0) {
                    if (position + mPosVar >= Integer.MAX_VALUE) {
                        mPosVar = 0;
                    } else {
                        mPosVar++;
                    }
                }
            }
        }

        return (position + mPosVar) % mFilmCount;
    }

    private void refreshView() {
        FilmLibListResponse.Content content = mFilmList.get(mSelectPos);
        if (content != null) {
            //刷新左上文字，页码
            mIndexTextTv.setText(mSelectPos + 1 + "/" + mFilmCount);
            mFilmNameTv.setText(content.getName());
            mFilmInfoTv.setText(content.getIntroduction());

            //刷新五颗星
            String str = content.getScore();
            if (!StringUtils.isNullOrEmpty(str)) {
                float score = Float.parseFloat(str);
                mImageStarIvs[0].setImageResource(score < 1 ? R.drawable.star_empty
                        : (score < 2 ? R.drawable.star_half : R.drawable.star_full));
                mImageStarIvs[1].setImageResource(score < 3 ? R.drawable.star_empty
                        : (score < 4 ? R.drawable.star_half : R.drawable.star_full));
                mImageStarIvs[2].setImageResource(score < 5 ? R.drawable.star_empty
                        : (score < 6 ? R.drawable.star_half : R.drawable.star_full));
                mImageStarIvs[3].setImageResource(score < 7 ? R.drawable.star_empty
                        : (score < 8 ? R.drawable.star_half : R.drawable.star_full));
                mImageStarIvs[4].setImageResource(score < 9 ? R.drawable.star_empty
                        : (score < 10 ? R.drawable.star_half : R.drawable.star_full));
            }
        }

        //刷新进度条
        if (!mFirst && mFilmCount > 7) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mScrollbarsIv.animate().translationX(mScrollbarWidth * mSelectPos).setDuration(
                        SCALE_DURATION).start();
            }
        }

        //刷新背景
//        refreshBg();
        mHandler.removeMessages(MSG_START_UPDATE_BACKGROUND);
        mHandler.sendEmptyMessageDelayed(MSG_START_UPDATE_BACKGROUND, SCALE_DURATION);
    }

    private void refreshBg() {
        FilmLibListResponse.Content content = mLayoutAdapter.getItem(mSelectPos);
        if (content != null && !StringUtils.isNullOrEmpty(content.getBigposter())) {
            String url = content.getBigposter();
            refreshBg(url);
        }
    }

    private void refreshBg(String url) {
        if (StringUtils.isNullOrEmpty(url)) {
            return;
        }
        Glide.clear(mSimpleTarget);
        Glide.with(this)
                .load(url)
                .asBitmap()
                .priority(Priority.HIGH)
                .into(mSimpleTarget);
    }

    SimpleTarget<Bitmap> mSimpleTarget = new SimpleTarget<Bitmap>() {
        @Override
        public void onResourceReady(Bitmap resource,
                GlideAnimation<? super Bitmap> glideAnimation) {
            mGlideDrawable = resource;
            if (mGlideDrawable != null) {
                //背景透明度渐变动画
                if (!mFirst) {
                    mHandler.removeMessages(MSG_ANIMA_ALPHA);
                    mHandler.sendEmptyMessageDelayed(MSG_ANIMA_ALPHA, 0);
                }
                mHandler.removeMessages(MSG_UPDATE_BACKGROUND);
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE_BACKGROUND, mFirst ? 10 : 400);
            }
        }
    };

    private void setScrollbarWidth(int width) {
        ViewGroup.LayoutParams lp = mScrollbarsIv.getLayoutParams();
        lp.width = width;
        mScrollbarsIv.setLayoutParams(lp);
    }

    public void updateBackground(Bitmap bitmap) {
        if (mPaletteTask != null) {
            // cancel last task
            mPaletteTask.cancel(true);
        }

        mPaletteTask = new Palette.Builder(bitmap).maximumColorCount(24).generate(
                mPaletteAsyncListener);
    }

    Palette.PaletteAsyncListener mPaletteAsyncListener = new Palette.PaletteAsyncListener() {
        @Override
        public void onGenerated(Palette palette) {
            Palette.Swatch swatch = palette.getMutedSwatch();
            Palette.Swatch darkSwatch = palette.getDarkMutedSwatch();

            if (null == swatch || null == darkSwatch) {
                swatch = palette.getVibrantSwatch();
                darkSwatch = palette.getDarkVibrantSwatch();
            }

            // If we have a color
            if (swatch != null && darkSwatch != null) {
                // generate gradient mGlideDrawable
                int[] colors = new int[]{darkSwatch.getRgb(), swatch.getRgb()};
                GradientDrawable gd = new GradientDrawable(
                        GradientDrawable.Orientation.TOP_BOTTOM, colors);

                // change background
                mBgIv.setImageDrawable(gd);
            }
        }
    };

    private final View.OnKeyListener mKeyListener = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN)
                    && (event.getRepeatCount() == 0)) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        mIsKeyDown = true;
                        if (mScrollbarsIv.isFocusable()) {
                            mScrollbarsIv.requestFocus();
                        } else {
//                            getView().findViewById(mTabSelectId).requestFocus();
                            if (mSelectedTabView != null) {
                                mSelectedTabView.requestFocus();
                            }
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if (mFilmCount > 7) {
                            mHandler.sendEmptyMessage(MSG_SHOW_SCROLLBAR);
                        }
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
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_HIDE_SCROLLBAR:
                    if (mScrollbarsIv.isFocusable() && !mScrollbarsIv.isFocused()) {
                        mScrollbarsIv.setFocusable(false);
                        ObjectAnimator bgAnim = ObjectAnimator.ofFloat(
                                mScrollbarLayout, "alpha", 1f, 0f);
                        bgAnim.setDuration(500);
                        bgAnim.start();
                    }
                    break;
                case MSG_SHOW_SCROLLBAR:
                    if (!mScrollbarsIv.isFocusable()) {
                        mScrollbarsIv.setFocusable(true);
                        ObjectAnimator bgAnim = ObjectAnimator.ofFloat(
                                mScrollbarLayout, "alpha", 0f, 1f);
                        bgAnim.setDuration(500);
                        bgAnim.start();
                    }

                    mHandler.removeMessages(MSG_HIDE_SCROLLBAR);
                    mHandler.sendEmptyMessageDelayed(MSG_HIDE_SCROLLBAR, 2000);
                    break;
                case MSG_REFRESH_VIEW:
                    refreshView();
                    break;
                case MSG_UPDATE_BACKGROUND:
                    if (mGlideDrawable != null) {
                        updateBackground(mGlideDrawable);
                        if (mFirst) {
                            mFirst = false;
                            ObjectAnimator.ofFloat(mBgIv, "alpha", 0f, 1f).setDuration(
                                    1000).start();
                        }
                    }
                    break;
                case MSG_START_UPDATE_BACKGROUND:
                    refreshBg();
                    break;
                case MSG_FOCUSED_POSTER:
                    mRecyclerView.requestFocus();
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mImageBorderIv.setVisibility(View.VISIBLE);
                    ((TabTextView) mTabLayout.getChildAt(mCurrentItem)).setSelectedOrPressed(true,
                            mLineViewIv);
                    break;
                case MSG_SIMULATE_KEY:
                    analogSendKey();
                    break;
                case MSG_SCROLLBAR_FOCUS:
                    mScrollbarsIv.setBackgroundResource(R.drawable.scrollbar_selector);
                    mScrollbarsIv.requestFocus();
                    mScrollFlag = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        if (mLastSelectedView != null) {
                            mLastSelectedView.animate()
                                    .scaleX(SCALE_UP_VALUE)
                                    .scaleY(SCALE_UP_VALUE)
                                    .translationY(-mY)
                                    .setDuration(SCALE_DURATION)
                                    .start();
                        }
                    }
                    break;
                case MSG_SCROLL_PREPARE:
                    mImageBorderIv.setVisibility(View.INVISIBLE);
                    mScrollbarsIv.setBackgroundResource(R.drawable.scrollbar_white_border);
                    mRecyclerView.requestFocus();
                    mHandler.sendEmptyMessage(MSG_SIMULATE_KEY);
                    break;
                case MSG_ANIMA_ALPHA:
                    mBgAnim.start();
                    break;
                default:
                    break;
            }
        }
    };

    private void analogSendKey() {
        if (mAnalogSendKeySubscription != null) {
            mAnalogSendKeySubscription.unsubscribe();
        }
        BaseSchedulerProvider schedulerProvider = Injection.provideSchedulerProvider();
        mAnalogSendKeySubscription = Observable.interval(0, 200, TimeUnit.MILLISECONDS)
                .take(5)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (null == mInst) {
                            mInst = new Instrumentation();
                        }
                    }
                })
                .doOnNext(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        mInst.sendKeyDownUpSync(mScrollKey);
                    }
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {
                        if (!isUnsubscribed()) {
                            mHandler.sendEmptyMessage(MSG_SCROLLBAR_FOCUS);
                            mHandler.sendEmptyMessageDelayed(MSG_REFRESH_VIEW, SCALE_DURATION);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.w(e, "analogSendKey, onError : ");
                    }

                    @Override
                    public void onNext(Long aLong) {
                    }
                });
    }

    private void removeAllMessages() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void hideOrShowView(int state) {
        mFilmInfoVg.setVisibility(state);
        mIndexTextTv.setVisibility(state);
        mFilmLibVg.setVisibility(state);

    }

    @Override
    public void onItemSelect(FilmTopic filmTopic, int mDataSize,
            PastTopicAdapter.PastTopicViewHolder viewHolder) {
        if (filmTopic != null) {
            refreshBg(filmTopic.getCoverposter());
        }
        if (viewHolder != null) {
            int position = viewHolder.getAdapterPosition();
            mIsKeyListener = isSetOnkeyListener(mDataSize, position);
            viewHolder.getView().setOnKeyListener(mPastTopicKeyListener);
        }
    }

    private boolean isSetOnkeyListener(int mDataSize, int position) {
        if (mDataSize == 1) {
            return true;
        }
        if (position % 2 != 0) return true;
        return false;
    }

    private final View.OnKeyListener mPastTopicKeyListener = new View.OnKeyListener() {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (event.getRepeatCount() == 0)) {
                if (!mIsKeyListener) return false;
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (mScrollbarsIv.isFocusable()) {
                            mScrollbarsIv.requestFocus();
                        } else {
                            if (mSelectedTabView != null) {
                                mSelectedTabView.requestFocus();
                            }
                        }
                        return true;
                    default:
                        break;

                }
            }
            return false;
        }
    };
}
