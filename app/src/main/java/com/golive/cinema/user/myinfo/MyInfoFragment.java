package com.golive.cinema.user.myinfo;


import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MY_ACCOUNT;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_USER_CENTER;
import static com.golive.cinema.util.FragmentUtils.removePreviousFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.golive.cinema.IBasePresenter;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.user.usercenter.UserPublic;
import com.initialjie.log.Logger;

import java.util.ArrayList;
import java.util.List;


public class MyInfoFragment extends MvpFragment {
    private static final String FRAG_TAG_PAY_AGREEMENT = "FRAG_TAG_PAY_AGREEMENT";
    private final String MYINFO_TAG_INFO = "info";
    private final String MYINFO_TAG_VIP = "vip";
    private final String MYINFO_TAG_TOPUP = "topup";
    private final String MYINFO_TAG_CREDIT = "credit";
    private final String MYINFO_TAG_COSUMTION = "cosumtion";
    private final String MYINFO_TAG_VIEW_HISTORY = "view_history";
    private final String MYINFO_TAG_ACTIVE = "active";
    private final String MYINFO_TAG_KOWN = "kown";
    private final String MYINFO_TAG_SERVICE = "service";

    private ListView mListView;
    private final List<MyinfoItem> mList = new ArrayList<>();
    private long mEnterTime;

    public static MyInfoFragment newInstance() {
        return new MyInfoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.user_myinfo, container, false);
        mListView = (ListView) root.findViewById(R.id.user_info_lv_result);
        initListView();
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("onActivityCreated");
        mEnterTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(getActivity()).reportEnterActivity(VIEW_CODE_MY_ACCOUNT,
                "我的账户", VIEW_CODE_USER_CENTER);
    }

    private void initListView() {
        //for debug
        mList.clear();
        mList.add(new MyinfoItem(MYINFO_TAG_INFO, getString(R.string.user_my_info),
                ResourcesCompat.getDrawable(getResources(), R.drawable.sel_user_info_icon_info,
                        null)));
        String tagVip = getString(R.string.user_my_info_vip);
        mList.add(new MyinfoItem(MYINFO_TAG_VIP, tagVip,
                ResourcesCompat.getDrawable(getResources(), R.drawable.sel_user_info_icon_vip,
                        null)));
        mList.add(new MyinfoItem(MYINFO_TAG_TOPUP, getString(R.string.user_my_info_to_pup),
                ResourcesCompat.getDrawable(getResources(), R.drawable.sel_user_info_icon_topup,
                        null)));
        mList.add(new MyinfoItem(MYINFO_TAG_CREDIT, getString(R.string.user_my_info_credit),
                ResourcesCompat.getDrawable(getResources(), R.drawable.sel_user_info_icon_credit,
                        null)));
        mList.add(new MyinfoItem(MYINFO_TAG_COSUMTION, getString(R.string.user_my_info_record),
                ResourcesCompat.getDrawable(getResources(), R.drawable.sel_user_info_icon_cosumtion,
                        null)));
        mList.add(new MyinfoItem(MYINFO_TAG_VIEW_HISTORY, getString(R.string.user_my_info_history),
                ResourcesCompat.getDrawable(getResources(), R.drawable.sel_user_info_icon_history,
                        null)));
        mList.add(new MyinfoItem(MYINFO_TAG_ACTIVE, getString(R.string.user_my_info_vip_active),
                ResourcesCompat.getDrawable(getResources(), R.drawable.sel_user_info_icon_active,
                        null)));
        mList.add(new MyinfoItem(MYINFO_TAG_KOWN, getString(R.string.user_my_info_know),
                ResourcesCompat.getDrawable(getResources(), R.drawable.sel_user_info_icon_kown,
                        null)));
        mList.add(new MyinfoItem(MYINFO_TAG_SERVICE, getString(R.string.user_my_info_agreement),
                ResourcesCompat.getDrawable(getResources(), R.drawable.sel_user_info_icon_service,
                        null)));

        mListView.setAdapter(new MyinfoListAdpter(getContext(), mList));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String key = mList.get(position).getKeyId();
                Logger.d("initListView OnItemClickListener position=" + position + ",key=" + key);
                Intent intent;
                switch (key) {
                    case MYINFO_TAG_VIP:
                        intent = new Intent(getActivity(),
                                com.golive.cinema.user.buyvip.BuyVipActivity.class);
                        getActivity().startActivityForResult(intent, 1 + position);
                        break;
                    case MYINFO_TAG_TOPUP:
                        intent = new Intent(getActivity(),
                                com.golive.cinema.user.topup.TopupActivity.class);
                        getActivity().startActivityForResult(intent, 1 + position);
                        break;
                    case MYINFO_TAG_CREDIT:
                        intent = new Intent(getActivity(),
                                com.golive.cinema.user.myinfo.MyInfoActivity.class);
                        intent.putExtra(UserPublic.KEY_USER_FRAGMENT,
                                UserPublic.MYINFO_FRAGMENT_CREDIT);
                        getActivity().startActivityForResult(intent, 1 + position);
                        break;
                    case MYINFO_TAG_COSUMTION:
                        intent = new Intent(getActivity(),
                                com.golive.cinema.user.consumption.ConsumptionActivity.class);
                        getActivity().startActivityForResult(intent, 1 + position);
                        break;
                    case MYINFO_TAG_VIEW_HISTORY:
                        intent = new Intent(getActivity(),
                                com.golive.cinema.user.history.HistoryActivity.class);
                        getActivity().startActivityForResult(intent, 1 + position);
                        break;
                    case MYINFO_TAG_ACTIVE:
                        intent = new Intent(getActivity(),
                                com.golive.cinema.user.myinfo.MyInfoActivity.class);
                        intent.putExtra(UserPublic.KEY_USER_FRAGMENT,
                                UserPublic.MYINFO_FRAGMENT_VIPACTIVE);
                        getActivity().startActivityForResult(intent, 1 + position);
                        break;
                    case MYINFO_TAG_KOWN:
                        UserPublic.setNeedKnowDialog(getActivity());
                        break;
                    case MYINFO_TAG_SERVICE:
//                        UserPublic.setServiceKownDialog(getActivity());
                        PayServiceAgreementFragment fragment =
                                PayServiceAgreementFragment.newInstance(
                                        PayServiceAgreementContract.AGREEMENT_TYPE_SIGN);
                        String fragTag = FRAG_TAG_PAY_AGREEMENT;
                        removePreviousFragment(getFragmentManager(), fragTag);
                        fragment.show(getFragmentManager(), fragTag);
                        break;
                    case MYINFO_TAG_INFO:
                    default:
                        intent = new Intent(getActivity(),
                                com.golive.cinema.user.myinfo.MyInfoActivity.class);
                        intent.putExtra(UserPublic.KEY_USER_FRAGMENT,
                                UserPublic.MYINFO_FRAGMENT_INFO);
                        getActivity().startActivityForResult(intent, 0);
                        break;
                }
            }
        });
    }

    @Override
    protected IBasePresenter getPresenter() {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String time = String.valueOf((System.currentTimeMillis() - mEnterTime) / 1000);
        StatisticsHelper.getInstance(getActivity()).reportExitActivity(VIEW_CODE_MY_ACCOUNT, "我的账户",
                "", time);
    }
}
