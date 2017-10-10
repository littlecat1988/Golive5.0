package com.golive.cinema;

import static com.golive.cinema.Constants.ADVERT_REQUEST_CODE;
import static com.golive.cinema.Constants.ADVERT_STARTMODE_HOME;
import static com.golive.cinema.Constants.CLASSES_MAPS;
import static com.golive.cinema.Constants.FIRST_FOCUS_RECOMMEND;
import static com.golive.cinema.Constants.FIRST_FOCUS_USER;
import static com.golive.cinema.Constants.PAGE_INDEX_ADVERT;
import static com.golive.cinema.Constants.PAGE_INDEX_FILM_LIB;
import static com.golive.cinema.Constants.PAGE_INDEX_RECOMMEND;
import static com.golive.cinema.Constants.PAGE_INDEX_THEATRE;
import static com.golive.cinema.Constants.PAGE_INDEX_USER_CENTER;

import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.golive.cinema.adapter.FragmentAdapter;
import com.golive.cinema.advert.AdvertHelper;
import com.golive.cinema.filmlibrary.FilmLibraryActivity;
import com.golive.cinema.init.MainContract;
import com.golive.cinema.init.MainPresenter;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.user.message.MessageActivity;
import com.golive.cinema.user.usercenter.UserCenterFragment;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.ToastUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.cinema.util.schedulers.BaseSchedulerProvider;
import com.golive.cinema.views.AlwaysMarqueeTextView;
import com.golive.cinema.views.TabTextView;
import com.golive.cinema.views.ViewPagerExt;
import com.golive.network.entity.CreditOperation;
import com.golive.network.entity.FinanceMessage;
import com.golive.network.entity.ServerMessage;
import com.golive.network.helper.UserInfoHelper;
import com.golive.network.response.ApplicationPageResponse;
import com.hwangjr.rxbus.RxBus;
import com.initialjie.log.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

/**
 * chgang
 */
public class MainFragment extends MvpFragment implements View.OnClickListener, MainContract.View {
    public static final String FRAGMENT_TAG = "main_fragment";

    public static boolean isBackClick, isTheater, isRecommend, isUserCenter;
    public static int tabSelectId = 0;
    //viewpager选项页
    private int mIndex = 0;
    // 当前选中的Tab的位置
    private int mCurrentItem = 0;
    private int pageCount = 0;
    private boolean repeatClick = false;

    private RelativeLayout mStatusLayout;
    private LinearLayout mTabLayout;
    private ImageView mLineView;
    private ViewPagerExt mViewPager;
    private AlwaysMarqueeTextView mMarqueeTextView;
    private FragmentAdapter mFragmentPagerAdapter = null;

    private String mMarqueeText;
    private final List<ServerMessage> mTipMessages = new ArrayList<>();
    private MainContract.Presenter mPresenter;
    private Subscription mAnalogSendKeySubscription;
    private LocalBroadcastManager mLocalBroadcastManager;
    private BroadcastReceiver receiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mTabLayout = (LinearLayout) view.findViewById(R.id.tab_panel_layout);
        mLineView = (ImageView) view.findViewById(R.id.tab_panel_line);
        mLineView.setVisibility(View.GONE);
        mViewPager = (ViewPagerExt) view.findViewById(R.id.fl_viewpager);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("onActivityCreated");
        setTitleHeader();
        isBackClick = false;
        isTheater = false;
        isRecommend = false;
        isUserCenter = false;
//        RxBus.get().register(this);

        Context context = getContext().getApplicationContext();
        MainPresenter mainPresenter = new MainPresenter(this,
                Injection.provideAppPageUseCase(context),
                Injection.provideCreditOperationUseCase(context),
                Injection.provideFinanceMessageUseCase(context),
                Injection.provideServerStatusUseCase(context),
                Injection.provideSchedulerProvider());
        if (mPresenter != null) {
            mPresenter.start();
        }

        initBroadcastReceiver();
    }

    private void initBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (pageCount > 0 && !StringUtils.isNullOrEmpty(action)) {
                    if (Constants.GOTO_PAGE_USER.equals(action)) {
                        mViewPager.setCurrentItem(pageCount - 1, true);
                    } else if (Constants.GOTO_PAGE_THEATER.equals(action)) {
                        mViewPager.setCurrentItem(0, true);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.GOTO_PAGE_USER);
        filter.addAction(Constants.GOTO_PAGE_THEATER);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        mLocalBroadcastManager.registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        mLocalBroadcastManager.unregisterReceiver(receiver);
        mStatusHandler.removeCallbacksAndMessages(null);
//        RxBus.get().unregister(this);
        if (mAnalogSendKeySubscription != null) {
            mAnalogSendKeySubscription.unsubscribe();
        }
        super.onDestroy();
    }

    private void setTitleHeader() {
        FragmentActivity activity = getActivity();
        mMarqueeTextView = (AlwaysMarqueeTextView) activity.findViewById(R.id.message_marquee_tv);
        mStatusLayout = (RelativeLayout) activity.findViewById(R.id.user_status_bar_layout);
        TextView userIdTv = (TextView) activity.findViewById(R.id.golive_user_name);
        userIdTv.setText(UserInfoHelper.getUserId(activity));
        ImageView userHeader = (ImageView) activity.findViewById(R.id.status_user_header_iv);
        String headUrl = UserInfoHelper.getUserHeadUrl(activity);
        if (!StringUtils.isNullOrEmpty(headUrl)) {
            Glide.with(activity).load(headUrl).into(userHeader);
        }
        mStatusHandler.sendEmptyMessageDelayed(HEAD_STATUS_SHOW, 1000);
    }

    private static final int HEAD_STATUS_SHOW = 0;
    private static final int HEAD_STATUS_HIDE = 1;
    private static final int CLICK_REPEAT_FLAG = 2;
    private static final int STATUSBAR_MESSAGE_RUN = 20;
    private final Handler mStatusHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HEAD_STATUS_SHOW:
                    if (mStatusLayout != null
                            && mStatusLayout.getVisibility() != View.VISIBLE) {
                        UIHelper.addViewAnimTranslate(mStatusLayout, true);
                        mStatusLayout.setVisibility(View.VISIBLE);
                    }
                    break;
                case HEAD_STATUS_HIDE:
                    if (mStatusLayout != null
                            && mStatusLayout.getVisibility() == View.VISIBLE) {
                        UIHelper.addViewAnimTranslate(mStatusLayout, false);
                        mStatusLayout.setVisibility(View.INVISIBLE);
                    }
                    break;
                case STATUSBAR_MESSAGE_RUN:
                    if (!StringUtils.isNullOrEmpty(mMarqueeText)) {
                        //float widthL =getActivity().getResources().getDimension(R.dimen
                        // .main_message_marquee_text_max_width);
                        int m = 0;
                        StringBuilder buffer = new StringBuilder(mMarqueeText);
                        while (m++ < 40) {
                            buffer.insert(0, "    ");
                        }
                        mMarqueeTextView.setText(buffer.toString());
                        //mMarqueeTextView.setLineWidth((int)widthL);
                        //mMarqueeTextView.startScrollShow();
                        //Logger.d("startScrollShow");
                    }
                    break;
                case CLICK_REPEAT_FLAG:
                    repeatClick = false;
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        if (!repeatClick && view instanceof TabTextView) {
            repeatClick = true;
            mStatusHandler.sendEmptyMessageDelayed(CLICK_REPEAT_FLAG, 500);
            int pageTag = ((TabTextView) view).getTabPage();
            if (mCurrentItem != pageTag) {
                changeTab(pageTag);
                view.requestFocus();
            } else {
                mLocalBroadcastManager.sendBroadcast(new Intent(FIRST_FOCUS_RECOMMEND));
                mLocalBroadcastManager.sendBroadcast(new Intent(FIRST_FOCUS_USER));
                BaseSchedulerProvider schedulerProvider = Injection.provideSchedulerProvider();
                if (mAnalogSendKeySubscription != null) {
                    mAnalogSendKeySubscription.unsubscribe();
                }
                mAnalogSendKeySubscription = Observable.interval(100, TimeUnit.MILLISECONDS)
                        .take(1)
                        .doOnNext(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
                            }
                        })
                        .subscribeOn(schedulerProvider.io())
                        .subscribe();
            }
        }
    }

    /**
     * 选择卡页
     */
    private void changeTab(int pageTag) {
        if (pageTag == mCurrentItem) {
            return;
        }

        if (mTabLayout != null && mTabLayout.getChildCount() > 0) {
            TabTextView tabView = ((TabTextView) mTabLayout.getChildAt(pageTag));
            if (CLASSES_MAPS.indexOfKey(tabView.getAction()) >= 0
                    && CLASSES_MAPS.get(tabView.getAction()) instanceof Class) {
                int index = tabView.getTabIndex();
                if (index == mIndex) {
                    setTabPosition(true);
                } else {
                    int action = tabView.getAction();
                    if (action == PAGE_INDEX_THEATRE) {
                        isTheater = true;
                    } else if (action == PAGE_INDEX_RECOMMEND) {
                        isRecommend = true;
                    } else if (action == PAGE_INDEX_USER_CENTER) {
                        isUserCenter = true;
                    }

                    mIndex = index;
                    mViewPager.setCurrentItem(index, true);
                }
            } else {
                int action = tabView.getAction();
                FragmentActivity activity = getActivity();
                if (action == PAGE_INDEX_FILM_LIB) {
                    //片库
                    FilmLibraryActivity.navigateTo(getContext(), false);
                } else if (action == PAGE_INDEX_ADVERT) {
                    boolean ok = AdvertHelper.goAdvert(activity, ADVERT_REQUEST_CODE,
                            ADVERT_STARTMODE_HOME);
                    if (ok) {
                        String caller = "1";
                        RxBus.get().post(Constants.EventType.TAG_AD_ENTER, caller);
                        StatisticsHelper helper = StatisticsHelper.getInstance(
                                activity.getApplicationContext());
                        helper.reportEnterAd(caller);
                    } else {
                        Toast.makeText(activity, getString(R.string.advert_empty_tips),
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    @Override
    protected IBasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showPageError(String msg) {
        String message = getString(R.string.app_page_get_failed);
        if (!StringUtils.isNullOrEmpty(msg)) {
            message += msg;
        }
        ToastUtils.showToast(getContext(), message);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void showPageView(ApplicationPageResponse pageResponse) {
        if (mTabLayout != null) {
            mTabLayout.removeAllViews();
        }

        if (pageResponse != null && pageResponse.isOk()
                && pageResponse.getApplicationPage() != null
                && pageResponse.getApplicationPage().getBasePage() != null
                && pageResponse.getApplicationPage().getBasePage().getNavigation() != null) {
            List<ApplicationPageResponse.Data> dataList =
                    pageResponse.getApplicationPage().getBasePage().getNavigation().getDatas();
            if (dataList != null) {
                int size = dataList.size();
                if (size > 0) {
                    ApplicationPageResponse.Data data;
                    FragmentActivity activity = getActivity();
                    for (int i = 0; i < size; i++) {
                        data = dataList.get(i);
                        TabTextView textView = new TabTextView(activity);
                        textView.setNextFocusDownId(textView.getId());
                        textView.setData(data);
                        textView.addToParentView(mTabLayout, i);
                        textView.setOnClickListener(this);
                    }

                    int basePageId = pageResponse.getApplicationPage().getBasePage().getId();
                    initPagerAdapter(basePageId);
                }
            }
        }
    }

    private void initPagerAdapter(int basePageId) {
        if (mTabLayout != null && mTabLayout.getChildCount() > 0) {
            List<Class> CLASSES_TAGS = new ArrayList<>();
            int count = mTabLayout.getChildCount();
            TabTextView child;
            int action;
            int index = 0;
            for (int i = 0; i < count; i++) {
                child = (TabTextView) mTabLayout.getChildAt(i);
                child.setTabPage(i);
                if (i == 0) {
                    child.setNextFocusLeftId(mTabLayout.getChildAt(count - 1).getId());
                } else if (i == count - 1) {
                    child.setNextFocusRightId(mTabLayout.getChildAt(0).getId());
                }

                action = child.getAction();
                if (CLASSES_MAPS.indexOfKey(action) >= 0
                        && CLASSES_MAPS.get(action) instanceof Class) {
                    child.setTabIndex(index);
                    CLASSES_TAGS.add(index++, (Class) CLASSES_MAPS.get(action));
                }
            }

            if (!CLASSES_TAGS.isEmpty()) {
                pageCount = CLASSES_TAGS.size();
                mFragmentPagerAdapter = new FragmentAdapter(getChildFragmentManager(), CLASSES_TAGS,
                        basePageId);
                //		    mViewPager.setFocusCanMoveToNextPage(true);
                mViewPager.setAdapter(mFragmentPagerAdapter);
                mViewPager.addOnPageChangeListener(mOnPageChangeListener);
                mViewPager.setOffscreenPageLimit(3);

                final TabTextView childIndex1 = ((TabTextView) mTabLayout.getChildAt(mIndex));
                mCurrentItem = childIndex1.getTabPage();
                setTabPosition(false);
                mTabLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                if (childIndex1.getWidth() != 0
                                        && childIndex1.getMeasuredWidth() != 0) {
                                    setLineViewStatus();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        mTabLayout.getViewTreeObserver()
                                                .removeOnGlobalLayoutListener(
                                                        this);
                                    }
                                }
                            }
                        });
            }
        }
    }

    private final ViewPager.OnPageChangeListener mOnPageChangeListener =
            new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrolled(int position, float positionOffset,
                        int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    Fragment f = mFragmentPagerAdapter.getItem(mIndex);
                    if (f != null) {
                        f.onHiddenChanged(true);
                    }

                    f = mFragmentPagerAdapter.getItem(position);
                    if (f != null) {
                        f.onHiddenChanged(false);
                        if (f instanceof UserCenterFragment) {
                            mStatusHandler.sendEmptyMessage(HEAD_STATUS_HIDE);
                        } else {
                            mStatusHandler.removeMessages(HEAD_STATUS_SHOW);
                            mStatusHandler.sendEmptyMessage(HEAD_STATUS_SHOW);
                        }
                    }

                    mIndex = position;
                    setTabPosition(true);
                    setLineViewStatus();
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }

            };

    private synchronized void setTabPosition(boolean isIndex) {
        if (mTabLayout != null && mTabLayout.getChildCount() > 0) {
            TabTextView child;
            for (int i = 0; i < mTabLayout.getChildCount(); i++) {
                child = (TabTextView) mTabLayout.getChildAt(i);
                if (isIndex) {
                    if (mIndex == child.getTabIndex()) {
                        child.setSelectedOrPressed(true, mLineView);
                        mCurrentItem = child.getTabPage();
                        tabSelectId = child.getId();
                    } else {
                        child.setSelectedOrPressed(false, mLineView);
                    }
                } else {
                    if (i == mCurrentItem) {
                        child.setSelectedOrPressed(true, mLineView);
                        mCurrentItem = child.getTabPage();
                        tabSelectId = child.getId();
                    } else {
                        child.setSelectedOrPressed(false, mLineView);
                    }
                }
            }
        }
    }

    private void setLineViewStatus() {
        if (mTabLayout != null && mTabLayout.getChildCount() > 0) {
            View child = null;
            for (int i = 0; i < mTabLayout.getChildCount(); i++) {
                child = mTabLayout.getChildAt(i);
                if (child.isSelected() || child.isPressed()) break;
            }

            if (child != null && child.getMeasuredWidth() != 0) {
                int leftPadding = child.getPaddingLeft();
                int rightPadding = child.getPaddingRight();
                int space = leftPadding + rightPadding;
                int measuredWidth = child.getMeasuredWidth();
                int width = measuredWidth - space;
                int left = child.getLeft();
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.bottomMargin =
                        (int) getResources().getDimension(
                                R.dimen.main_tab_item_parent_margin_bottom);
                params.leftMargin = left + leftPadding;
                mLineView.setLayoutParams(params);
                if (!child.isFocused() && child.isSelected()) {
                    mLineView.setVisibility(View.VISIBLE);
                } else {
                    mLineView.setVisibility(View.GONE);
                }
            }
        }
    }

    public void onPerformClickButton(int action) {
        if (mTabLayout != null && mTabLayout.getChildCount() > 0) {
            if (action > 0) {
                TabTextView child;
                for (int i = 0; i < mTabLayout.getChildCount(); i++) {
                    child = (TabTextView) mTabLayout.getChildAt(i);

                    if (CLASSES_MAPS.indexOfKey(action) >= 0
                            && CLASSES_MAPS.get(action) instanceof Class
                            && action == child.getAction()) {
                        child.performClick();
                        return;
                    }
                }
            } else {
                isBackClick = true;
                mTabLayout.getChildAt(0).performClick();
            }
        }
    }

    public int getCurrentItem() {
        if (mTabLayout != null && mTabLayout.getChildCount() > 0) {
            View focusView = mTabLayout.getChildAt(0);
            if ((focusView.isSelected() || focusView.isPressed()) && !focusView.isFocused()) {
                focusView.requestFocus();
                return Integer.MAX_VALUE;
            }
        }
        return mCurrentItem;
    }

    @Override
    public Observable<?> showCreditOperationView(final CreditOperation creditOperation) {
        Logger.d("showCreditOperationView:");
        Observable.OnSubscribe<?> onSubscribe = new Observable.OnSubscribe<Object>() {

            @Override
            public void call(Subscriber<? super Object> subscriber) {
                if (creditOperation != null) {
                    Logger.d("creditOperation:" + creditOperation.toString());
                    ServerMessage serverMessage = new ServerMessage();
                    serverMessage.setType(ServerMessage.SERVER_MESSAGE_TYPE_CREDIT);
                    serverMessage.setServerTime(creditOperation.getError().getTime());
                    serverMessage.setCreditOperation(creditOperation);

                    String creditValue = creditOperation.getValue();
                    String totalCreditMoney = creditOperation.getCreditLine();
                    String deadLineDays = creditOperation.getCreditDeadLineDays();
                    String remain = creditOperation.getCreditRemain();
                    if (!StringUtils.isNullOrEmpty(totalCreditMoney)) {
                        UserInfoHelper.setMaxCredit(getContext(), totalCreditMoney);
                    }
                    if (creditValue != null && deadLineDays != null && remain != null) {
                        boolean isDue = (remain.startsWith("-")
                                && Double.valueOf(deadLineDays) > 0
                                && Double.valueOf(creditValue) < 0);
                        if (isDue) {//显示
                            mTipMessages.add(serverMessage);
                        }

                        //保存信用天数
                        if (Double.valueOf(deadLineDays) > 0) {
                            UserInfoHelper.setLineDayCredit(getContext(), deadLineDays);
                        } else {
                            UserInfoHelper.setLineDayCredit(getContext(), "0");
                        }
                    }
                }
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<?> showFinanceMessageView(final FinanceMessage financeMessage) {
        Logger.d("showFinanceMessageView:");
        Observable.OnSubscribe<?> onSubscribe = new Observable.OnSubscribe<Object>() {

            @Override
            public void call(Subscriber<? super Object> subscriber) {
                if (financeMessage != null) {
                    List<com.golive.network.entity.Message> messageList =
                            financeMessage.getMessageList();
                    if (messageList != null && messageList.size() > 0) {
                        ServerMessage serverMessage = new ServerMessage();
                        serverMessage.setType(ServerMessage.SERVER_MESSAGE_TYPE_FINANCE);
                        serverMessage.setServerTime(financeMessage.getError().getTime());
                        serverMessage.setMessageList(messageList);
                        mTipMessages.add(serverMessage);
                    }
                }
                subscriber.onNext(true);
                subscriber.onCompleted();
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public void showAllMessageView(List<ServerMessage> messageList) {
        if (messageList != null && messageList.size() > 0) {
            mTipMessages.clear();
            mTipMessages.addAll(messageList);
        }
    }

    @Override
    public void onCompleted() {
        if (mTipMessages != null && mTipMessages.size() > 0) {
            ServerMessage serverMessage;
            for (int i = 0; i < mTipMessages.size(); i++) {
                serverMessage = mTipMessages.get(i);
                String type = serverMessage.getType();
                if (!StringUtils.isNullOrEmpty(type) && (
                        ServerMessage.SERVER_MESSAGE_TYPE_NOTICE.equals(type)
                                || ServerMessage.SERVER_MESSAGE_TYPE_ACTIVITY.equals(type))) {
                    String displayTimes = serverMessage.getDisplaytimes();
                    if (!StringUtils.isNullOrEmpty(displayTimes)) {
                        //mMarqueeTextView.setCircleTimes(Integer.parseInt(serverMessage
                        // .getDisplaytimes()));
                        try {
                            mMarqueeTextView.setMarqueeRepeatLimit(Integer.parseInt(displayTimes));
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                    mMarqueeText = serverMessage.getContent();
                }

                String key = ServerMessage.getMessageKey(serverMessage);
                if (key != null) {
                    serverMessage.setKey(key);
                    FragmentActivity activity = getActivity();
                    String saveState = UserInfoHelper.getMessageState(activity, key);
                    if (!StringUtils.isNullOrEmpty(saveState)
                            && (ServerMessage.SERVER_MESSAGE_ID_KEY_NEW.equals(saveState)
                            || ServerMessage.SERVER_MESSAGE_ID_KEY_READED.equals(saveState))) {
                        serverMessage.setState(saveState);
                    } else {
                        serverMessage.setState(ServerMessage.SERVER_MESSAGE_ID_KEY_NEW);
                        UserInfoHelper.setMessageState(activity, key,
                                ServerMessage.SERVER_MESSAGE_ID_KEY_NEW);
                    }
                }
            }

            showTabMessagePointTips();//红点
            if (mStatusHandler != null) {
                mStatusHandler.sendEmptyMessageDelayed(STATUSBAR_MESSAGE_RUN, 2000);
            }
        }
    }

    public void openMessageDetails() {
        Intent intent = new Intent(getActivity(), MessageActivity.class);
        List<ServerMessage> mMessages = new ArrayList<>();

        if (!mTipMessages.isEmpty()) {
            for (int i = 0; i < mTipMessages.size(); i++) {
                if (mTipMessages.get(i).getId() == null) {
                    mMessages.add(mTipMessages.get(i));
                }
            }

            Bundle bundle = new Bundle();
            bundle.putSerializable(MessageActivity.MESSAGE_LIST_TAG, (Serializable) mMessages);
            intent.putExtras(bundle);
        }
        getActivity().startActivity(intent);
    }

    public int showTabMessagePointTips() {
        int count = 0;
        if (mTipMessages != null && mTipMessages.size() > 0) {
            FragmentActivity activity = getActivity();
            for (int i = 0; i < mTipMessages.size(); i++) {
                String key = mTipMessages.get(i).getKey();
                if (StringUtils.isNullOrEmpty(key)) {
                    continue;
                }
                String state = UserInfoHelper.getMessageState(activity, key);
                if (!StringUtils.isNullOrEmpty(state)
                        && ServerMessage.SERVER_MESSAGE_ID_KEY_NEW.equals(state)) {
                    count++;
                }
            }
        }

        if (mTabLayout != null && mTabLayout.getChildCount() > 0) {
            TabTextView child;
            for (int i = 0; i < mTabLayout.getChildCount(); i++) {
                child = (TabTextView) mTabLayout.getChildAt(i);
                if (child.getAction() == PAGE_INDEX_USER_CENTER) {
                    child.setMessagePointTips(count > 0);
                    break;
                }
            }
        }

        return count;
    }

    public int getTabCount() {
        if (mTabLayout != null) {
            return mTabLayout.getChildCount();
        }
        return 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.d("onActivityResult, requestCode : " + requestCode + ", resultCode : " + resultCode);
    }
}