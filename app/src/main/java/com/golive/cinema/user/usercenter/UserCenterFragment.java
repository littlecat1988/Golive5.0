package com.golive.cinema.user.usercenter;

import static com.golive.cinema.Constants.EXTRA_BASE_PAGE_ID;
import static com.golive.cinema.Constants.FIRST_FOCUS_THEATER;
import static com.golive.cinema.Constants.FIRST_FOCUS_USER;
import static com.golive.cinema.Constants.LAST_FOCUS_RECOMMEND;
import static com.golive.cinema.Constants.LAST_FOCUS_USER;
import static com.golive.cinema.Constants.SCALE_DURATION;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MAIN_ACTIVITY;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_USER_CENTER;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.MainActivity;
import com.golive.cinema.MainFragment;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.user.myinfo.MyInfoActivity;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.views.metroviews.widget.TvRecyclerView;
import com.golive.network.entity.UserHead;
import com.golive.network.entity.UserInfo;
import com.golive.network.entity.Wallet;
import com.golive.network.helper.UserInfoHelper;
import com.golive.network.response.RecommendResponse;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.initialjie.log.Logger;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016/9/23.
 * 用户中心卡页
 */

public class UserCenterFragment extends MvpFragment implements View.OnClickListener,
        UserCenterContract.View {
    private final int MSG_HEADER_VIEW_FOCUSABLE = 0;
    private final int MSG_RIGHT_KEY_PRESSED = 1;
    private TvRecyclerView mRecyclerView;
    private UserCenterPageAdapter mLayoutAdapter;
    private UserCenterContract.Presenter mUserCenterPresenter;
    private TextView mVipNameTv;
    private TextView mVipNumberTv;
    private TextView mRemaintimeTv;
    private TextView mMMoneyTvL, mMoneyTvR, mMoneyTvUnit;
    private ImageView mUserHeader;
    private View mUserHeaderView;
    private List<RecommendResponse.Items> mItemsList;
    private final List<UserPageItem> mUserPageList = new ArrayList<>();
    private BroadcastReceiver mReceiver;
    private int mScreenHeight;
    private int mSelectPos, mCount;
    private int mRepeatCount;
    private long mEnterTime;
    private boolean mTabFocus, mIsVisibleToUser;
    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        RxBus.get().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_center, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = (TvRecyclerView) view.findViewById(R.id.user_center_list);
        mVipNameTv = (TextView) view.findViewById(R.id.user_vip_name_text);
        mVipNumberTv = (TextView) view.findViewById(R.id.user_vip_number_text);
        mRemaintimeTv = (TextView) view.findViewById(R.id.user_vip_remaintime_text);
        mMMoneyTvL = (TextView) view.findViewById(R.id.user_info_account_num_l);
        mMoneyTvR = (TextView) view.findViewById(R.id.user_info_account_num_r);
        mMoneyTvUnit = (TextView) view.findViewById(R.id.user_info_account_num_unit);
        mUserHeader = (ImageView) view.findViewById(R.id.user_center_header_image);
        mUserHeaderView = view.findViewById(R.id.user_center_header_big_view);
        mRemaintimeTv.setText(getString(R.string.user_info_ipname_normol_ts));
        mUserHeaderView.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Context context = getContext().getApplicationContext();
        initClassArr();
        String userId = UserInfoHelper.getUserId(context);
        setVipNumber(userId);
        setVipName(getString(R.string.user_info_ipname_normol));
        initBroadcastReceiver();
        mUserCenterPresenter = new UserCenterPresenter(this,
                Injection.provideGetRecommendUseCase(context),
                Injection.provideGetUserInfoUseCase(context),
                Injection.provideGetUserWalletUseCase(context),
                Injection.provideGetUserHeadUseCase(context));
        mUserCenterPresenter.start();
        int pageId = getArguments().getInt(EXTRA_BASE_PAGE_ID, 1);
        mUserCenterPresenter.getTemplateData(String.valueOf(pageId));
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (UserPublic.Flag_Reflash_VipInfo != 0) {
//            mHandler.sendEmptyMessageDelayed(MSG_REFRESH_VIP_INFO, 600);
//        } else if (UserPublic.Flag_Reflash_MoneyInfo != 0) {
//            mHandler.sendEmptyMessageDelayed(MSG_REFRESH_MONEY_INFO, 600);
//        }
        onResumeMessageTip();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        RxBus.get().unregister(this);
    }

    private void onResumeMessageTip() {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        MainFragment mainFragment = (MainFragment) fragmentManager.findFragmentByTag(
                MainFragment.FRAGMENT_TAG);
        if (mainFragment != null) {
            int mCount = mainFragment.showTabMessagePointTips();
            if (mLayoutAdapter != null) {
                mLayoutAdapter.setMessageTips(mCount);
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.mIsVisibleToUser = isVisibleToUser;
        mHandler.sendEmptyMessageDelayed(MSG_HEADER_VIEW_FOCUSABLE, 100);

        Context context = getContext();
        if (isVisibleToUser) {
            onResumeMessageTip();
            if (MainFragment.isUserCenter) {
                MainFragment.isUserCenter = false;
                if (mLayoutAdapter != null && mLayoutAdapter.mTabFocusPosition != -1) {
                    mRecyclerView.setDefaultSelected(mLayoutAdapter.mTabFocusPosition);
                    mRecyclerView.smoothScrollToPosition(0);
                }
            }

            mLocalBroadcastManager.sendBroadcast(new Intent(LAST_FOCUS_RECOMMEND));
            mLocalBroadcastManager.sendBroadcast(new Intent(FIRST_FOCUS_THEATER));

            if (context != null) {
                mEnterTime = System.currentTimeMillis();
                StatisticsHelper.getInstance(context).reportEnterActivity(VIEW_CODE_USER_CENTER,
                        "用户中心", VIEW_CODE_MAIN_ACTIVITY);
            }
        } else if (mEnterTime != 0 && context != null) {
            String duration = String.valueOf((System.currentTimeMillis() - mEnterTime) / 1000);
            StatisticsHelper.getInstance(context).reportExitActivity(VIEW_CODE_USER_CENTER, "用户中心",
                    "", duration);
        }
    }

    private void initBroadcastReceiver() {
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (mCount > 0 && !StringUtils.isNullOrEmpty(action)) {
                    if (FIRST_FOCUS_USER.equals(action)) {
                        if (mRecyclerView.getOldSelectedPosition() != 0) {
                            mRecyclerView.setDefaultSelected(0);
                            mRecyclerView.smoothScrollToPosition(0);
                        }
                    } else if (LAST_FOCUS_USER.equals(action)) {
                        if (mRecyclerView.getOldSelectedPosition()
                                != mLayoutAdapter.mLastFocusPosition) {
                            mRecyclerView.setDefaultSelected(mLayoutAdapter.mLastFocusPosition);
                            mRecyclerView.smoothScrollToPosition(mLayoutAdapter.mLastFocusPosition);
                        }
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(FIRST_FOCUS_USER);
        filter.addAction(LAST_FOCUS_USER);
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

    private void ActionClickLaunch(int position) {
        String actionContent = mItemsList.get(position).getActionContent();
        if (!StringUtils.isNullOrEmpty(actionContent)) {
            int size = mUserPageList.size();
            for (int i = 0; i < size; i++) {
                String name = mUserPageList.get(i).getName();
                if (!StringUtils.isNullOrEmpty(name) && actionContent.equals(name)) {
                    FragmentActivity activity = getActivity();
                    Intent intent = new Intent(activity, mUserPageList.get(i).getIntoClass());
                    intent.putExtra("USER_CLASS", name);
                    activity.startActivityForResult(intent, UserPublic.KEY_LAUNCHER_USER_PAGE);
                    break;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.user_center_header_big_view) {
            FragmentActivity activity = getActivity();
            Intent intent = new Intent(activity, MyInfoActivity.class);
            intent.putExtra(UserPublic.KEY_USER_FRAGMENT, UserPublic.MYINFO_FRAGMENT_INFO);
            activity.startActivityForResult(intent, 0);
        }
    }

    private void initClassArr() {
        Class[] CLASSES_TAGS = new Class[8];//可重复一样的类名
        CLASSES_TAGS[0] = com.golive.cinema.user.buyvip.BuyVipActivity.class;
        CLASSES_TAGS[1] = com.golive.cinema.user.topup.TopupActivity.class;
        CLASSES_TAGS[2] = com.golive.cinema.user.myinfo.MyInfoActivity.class;
        CLASSES_TAGS[3] = com.golive.cinema.user.history.HistoryActivity.class;
        CLASSES_TAGS[4] = com.golive.cinema.user.consumption.ConsumptionActivity.class;
        CLASSES_TAGS[5] = com.golive.cinema.user.setting.SettingActivity.class;
        CLASSES_TAGS[6] = com.golive.cinema.user.message.MessageActivity.class;
        CLASSES_TAGS[7] = com.golive.cinema.user.custom.CustomActivity.class;

        // 此内容和对应关系是根据接口文档配置的，与上面的CLASSES_TAGS顺序也要对应
        String[] nameArr = {
                "1",//开通会员
                "2",//充值
                "3",//我的账户
                "4",//观影记录
                "5",//交易记录
                "6",//系统设置
                "7",//消息
                "8"//客服
        };

        /*int[] bgArrId = {
                R.drawable.user_buyvip,
                R.drawable.user_myinfo,
                R.drawable.user_topup,
                R.drawable.user_history,
                R.drawable.user_setting,
                R.drawable.user_consumption,
                R.drawable.user_message,
                R.drawable.user_custom,
        };*/


        for (int i = 0; i < 8; i++) {
            UserPageItem item = new UserPageItem();
            item.setName(nameArr[i]);
            item.setIntoClass(CLASSES_TAGS[i]);
            //item.setDrawable(getActivity().getResources().getDrawable(bgArrId[i]));
            mUserPageList.add(item);
        }
    }

    private void initRecyclerView() {
        mCount = mItemsList.size();
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mRecyclerView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                if (mRepeatCount > 0) {
                    mHandler.removeMessages(MSG_RIGHT_KEY_PRESSED);
                    mRepeatCount = 0;
                }
                if (isAdded()) {
                    mLayoutAdapter.setItemImage(false, position);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    itemView.clearAnimation();
                    itemView.animate().scaleX(1.0f).scaleY(1.0f).translationY(0f).setDuration(
                            SCALE_DURATION).start();
                }
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                mSelectPos = position;
                if (isAdded()) {
                    mLayoutAdapter.setItemImage(true, position);
                }
                int y = mScreenHeight * 5 / 1000;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    itemView.clearAnimation();
                    itemView.animate().scaleX(1.05f).scaleY(1.05f).translationY(-y).setDuration(
                            SCALE_DURATION).start();
                }
                itemView.setOnKeyListener(mRecyclerViewKeyListener);
                mTabFocus = mScreenHeight / 3 < itemView.getBottom();

                if (position == mLayoutAdapter.mLastFocusPosition || position == mCount - 1) {
                    mHandler.sendEmptyMessage(MSG_RIGHT_KEY_PRESSED);
                }
            }

            @Override
            public void onReviseFocusFollow(TvRecyclerView parent, View itemView, int position) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    itemView.clearAnimation();
                    itemView.animate().scaleX(1.05f).scaleY(1.05f).setDuration(SCALE_DURATION).start();
                }
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                ActionClickLaunch(position);
            }
        });

        mRecyclerView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && mRepeatCount > 0) {
                    mHandler.removeMessages(MSG_RIGHT_KEY_PRESSED);
                    mRepeatCount = 0;
                }
            }
        });

        //设置格子间距
        int spacing = (int) getResources().getDimension(R.dimen.recycler_view_item_space);
        mRecyclerView.setSpacingWithMargins(-spacing, -spacing);

        mLayoutAdapter = new UserCenterPageAdapter(this, mItemsList);
        mRecyclerView.setAdapter(mLayoutAdapter);
        mRecyclerView.setDefaultSelected(mLayoutAdapter.mLastFocusPosition);
    }

    private final View.OnKeyListener mRecyclerViewKeyListener = new View.OnKeyListener() {

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
                            return mLayoutAdapter.isFocusPrefer(mSelectPos, keyCode);
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        return mLayoutAdapter.isFocusPrefer(mSelectPos, keyCode);
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        if ((mSelectPos == mLayoutAdapter.mLastFocusPosition)
                                || (mSelectPos == mCount - 1)) {
                            mLocalBroadcastManager.sendBroadcast(
                                    new Intent(Constants.GOTO_PAGE_THEATER));
                        } else {
                            return mLayoutAdapter.isFocusPrefer(mSelectPos, keyCode);
                        }
                    default:
                        break;
                }
            }
            return false;
        }
    };

    private void setVipName(String val) {
        mVipNameTv.setText(val);
    }

    private void setVipNumber(String val) {
        mVipNumberTv.setText(val);
    }

    @Override
    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

        if (mReceiver != null) {
            mLocalBroadcastManager.unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void setLoadingIndicator(boolean active) {

    }

    @Override
    public void setUserInfo(UserInfo userInfo) {
        if (null == userInfo) {
            return;
        }
        Logger.d("setUserInfo getEffectivetime" + userInfo.getEffectivetime() + ",getTotalcoin"
                + userInfo.getTotalcoin()
                + ",getVipendtime" + userInfo.getVipendtime());

        if (userInfo.isVIP()) {
            setVipName(getString(R.string.user_info_ipname_golive));
            int dateTime = getValitDate(userInfo.getEffectivetime());
            if (dateTime < 0) {
                dateTime = 0;
            }
            String detailError = String.format(
                    getString(R.string.user_center_vip_remain_date), "" + dateTime);
            Spanned spanned = Html.fromHtml(detailError);
            mRemaintimeTv.setText(spanned);
        } else {
            setVipName(getString(R.string.user_info_ipname_normol));
            mRemaintimeTv.setText(getString(R.string.user_info_ipname_normol_ts));
        }

        UserInfoHelper.setUserVip(getContext(), userInfo.getUserlevel());
    }

    @Override
    public void setWalletInfo(Wallet wallet) {
        if (null == wallet || StringUtils.isNullOrEmpty(wallet.getValue())) {
            return;
        }
        Logger.d("setWalletInfo getValue" + wallet.getValue());
        String money = wallet.getValue();//UserPublic.getFormatMoneyFenToYuan(wallet.getValue());
        int point = money.indexOf(".");
        String money_l;
        String money_r;
        if (point > 0) {
            money_l = money.substring(0, point);
            money_r = money.substring(point);
        } else {
            money_l = money;
            money_r = ".00";
        }
        mMMoneyTvL.setText(money_l);
        mMoneyTvR.setText(money_r);
    }

    @Override
    public void showUserHead(UserHead head) {
        if (null == head) {
            return;
        }
        Logger.d("showUserHead getIconName=" + head.getIconName() + ",geticonUrl="
                + head.geticonUrl());
        String url = head.geticonUrl();
        if (url != null) {
            UserInfoHelper.setUserHeadUrl(getContext(), url);
            Glide.with(this).load(url).into(mUserHeader);
            ImageView userHeaderStatus = (ImageView) getActivity().findViewById(
                    R.id.status_user_header_iv);
            if (userHeaderStatus != null) {
                Glide.with(this).load(url).into(userHeaderStatus);
            }
        }
    }

    @Override
    public void showTemplateView(RecommendResponse response) {
        if (response.getLayout() != null
                && response.getLayout().getItems() != null
                && !response.getLayout().getItems().isEmpty()) {
            mItemsList = response.getLayout().getItems();
            initRecyclerView();
        }
    }

    @Override
    public void showGetTemplateFailed(String errMsg) {
        String text = getString(R.string.user_center_template_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            text += errMsg;
        }
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setPresenter(UserCenterContract.Presenter presenter) {
        this.mUserCenterPresenter = presenter;
    }

    @Override
    protected UserCenterContract.Presenter getPresenter() {
        return this.mUserCenterPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private int getValitDate(String effectivetime) {
        int dateTime = 0;
        try {
            double diff = Double.parseDouble(effectivetime);
            double days = diff / (60 * 60 * 24);
            dateTime = (int) days;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateTime;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (!isAdded()) {
                return;
            }
            switch (msg.what) {
                case MSG_HEADER_VIEW_FOCUSABLE:
                    if (mUserHeaderView != null) {
                        mUserHeaderView.setFocusable(mIsVisibleToUser);
                        mUserHeaderView.setFocusableInTouchMode(mIsVisibleToUser);
                    }
                    break;
                case MSG_RIGHT_KEY_PRESSED:
                    if (MainActivity.isRightKeyPressed) {
                        mRepeatCount = 0;
                        mLocalBroadcastManager.sendBroadcast(
                                new Intent(Constants.GOTO_PAGE_THEATER));
                    } else if (mRepeatCount++ < 10) {
                        mHandler.sendEmptyMessageDelayed(MSG_RIGHT_KEY_PRESSED, 100);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Subscribe(
            tags = {@Tag(Constants.EventType.TAG_UPDATE_USER_INFO)}
    )
    public void onUpdateUserInfo(Object obj) {
        Logger.d("onUpdateUserInfo");
        UserCenterContract.Presenter presenter = getPresenter();
        if (presenter != null) {
            presenter.getUserInfo(true);
            presenter.getUserHead(true);
        }
    }

    @Subscribe(
            tags = {@Tag(Constants.EventType.TAG_UPDATE_WALLET)}
    )
    public void onUpdateWallet(Object obj) {
        Logger.d("onUpdateWallet");
        UserCenterContract.Presenter presenter = getPresenter();
        if (presenter != null) {
            presenter.getTheUserWallet(true);
        }
    }
}
