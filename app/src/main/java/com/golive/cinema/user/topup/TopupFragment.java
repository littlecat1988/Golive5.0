package com.golive.cinema.user.topup;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_TOP_UP;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_USER_CENTER;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.user.usercenter.UserPublic;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.TopupRechargeItem;
import com.golive.network.entity.Wallet;
import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.initialjie.log.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class TopupFragment extends MvpFragment implements TopupContract.View {
    private TopupContract.Presenter mPresenter;
    private TextView mHaveMoneyTv;
    private TextView phoneTv;
    private GridView mGridView;
    private TopupAdapter mListAdapter;
    //    private CircleView circleView;
    private ProgressDialog mProgressDialog;
    private long mIntoTime;
    private String mHaveMoney = "0.00";
    private DecimalFormat mDecimalFormat;
    private final List<TopupRechargeItem> mList = new ArrayList<>();

    public static TopupFragment newInstance() {
        return new TopupFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        RxBus.get().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_topup, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.d("onActivityCreated");
//        Intent intent = getActivity().getIntent();

        Context context = getContext().getApplicationContext();
        mPresenter = new TopupPresenter(this,
                Injection.provideGetTopupPriceListUseCase(context),
                Injection.provideGetUserWalletUseCase(context),
                Injection.provideGetClientServiceUseCase(context));

        FragmentActivity activity = getActivity();
        mGridView = (GridView) activity.findViewById(R.id.topup_list);
        mHaveMoneyTv = (TextView) activity.findViewById(R.id.topup_money_num_tv);
        String rmbStr = getString(R.string.RMB);
        mHaveMoneyTv.setText(
                getString(R.string.user_center_info_account) + " : " + "0.00" + rmbStr);
//        circleView =(CircleView)getActivity().findViewById(R.id.my_money_circleimage);
        phoneTv = (TextView) activity.findViewById(R.id.topup_vg_bottom_phonetv);
        initListView();
        if (getPresenter() != null) {
            getPresenter().start();
        }
        mIntoTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(getContext()).reportEnterActivity(VIEW_CODE_TOP_UP,
                getString(R.string.topup), VIEW_CODE_USER_CENTER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String time = String.valueOf((System.currentTimeMillis() - mIntoTime) / 1000);
        StatisticsHelper.getInstance(getActivity()).reportExitActivity(VIEW_CODE_TOP_UP, "充值", "",
                time);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        RxBus.get().unregister(this);
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
    public void showLoadingFailed(String errMsg) {
        if (!isActive()) {
            return;
        }
        String text = getString(R.string.loading_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            text += errMsg;
        }
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setPresenter(TopupContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    @Override
    public TopupContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showResultListView(List<TopupRechargeItem> lists) {
        Logger.d("showResultListView, list : " + lists);
        if (mList != null && lists.size() > 0) {
            mList.clear();
            Logger.d("showVipListView size=" + lists.size());
            mList.addAll(lists);
        } else {
            //for debug
            String[] arrname = {
                    "200", "100", "50", "20", "10", "5"
            };
            if (mList != null) {
                mList.clear();
            }
            for (String anArrname : arrname) {
                TopupRechargeItem item = new TopupRechargeItem();
                item.setPrice(anArrname);
                mList.add(item);
            }
//        setGridView(vipPackagesList.size());
//            mGridView.setNumColumns(3); // 设置列数量=列表集合数
        }

        checkListPriceFomat();

        setGridView(getHorizLineItem());
        mListAdapter.notifyDataSetChanged();

        mGridView.requestFocus();
        mGridView.setSelection(0);
        if (mList.size() <= 6) {
            float paddingL = 8.3f * 1.5f;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mGridView.animate().translationX(paddingL).setDuration(0).start();
            }
        }
    }

    /** 设置GirdView参数，绑定数据 */
    private void setGridView(int size) {
        if (null == mGridView) {
            return;
        }
        float item_w = getResources().getDimension(R.dimen.topup_item_w);
        float HoriPadding = getResources().getDimension(R.dimen.topup_item_horizontal_spacing);

//        DisplayMetrics dm = new DisplayMetrics();
//        this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = 1;//dm.density;
        int gridviewWidth = (int) (size * (item_w + HoriPadding) * density);
        int itemdensityWidth = (int) (item_w * density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                gridviewWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        mGridView.setLayoutParams(params); // 设置GirdView布局参数,横向布局的关键
        mGridView.setColumnWidth(itemdensityWidth); // 设置列表项宽
        mGridView.setHorizontalSpacing((int) HoriPadding); // 设置列表项水平间距
//        mGridView.setVerticalSpacing(39);
        mGridView.setStretchMode(GridView.NO_STRETCH);//columnWidth
        mGridView.setNumColumns(size); // 设置列数量=列表集合数
    }

    private void initListView() {
        mListAdapter = new TopupAdapter(getContext(), mList);
        mGridView.setAdapter(mListAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Logger.d("onItemClick position=" + position);
                Intent intent = new Intent(getActivity(), TopupActivity.class);
                intent.putExtra(UserPublic.KEY_USER_FRAGMENT,
                        UserPublic.TOPUP_FRAGMENT_QRCAODE_PAY);
                intent.putExtra(UserPublic.KEY_PAY_PRICE, mList.get(position).getPrice());
                String name = String.format(getActivity().getString(R.string.topup_please_pay_yuan),
                        mList.get(position).getPrice());
                intent.putExtra(UserPublic.KEY_PAY_NAME, name);
                intent.putExtra(UserPublic.KEY_PAY_PAGE, "topup");
                getActivity().startActivityForResult(intent, position);

                StatisticsHelper.getInstance(getActivity()).reportClickUserCenterTopUp(
                        mList.get(position).getPrice(), mHaveMoney, "3");
            }
        });
    }

    @Override
    public void setWalletInfo(Wallet wallet) {
        if (null == wallet) {
            return;
        }
        mHaveMoney = wallet.getValue();
        mHaveMoneyTv.setText(
                getString(R.string.user_center_info_account) + " : " + mHaveMoney + getString(
                        R.string.RMB));
    }

    @Override
    public void setServicePhoneInfo(String phone) {
        if (phone != null) {
            if (phone.contains("(")) {
                phone = phone.replace("(", " ( ");
            }
            if (phone.contains(")")) {
                phone = phone.replace(")", " ) ");
            }
            phoneTv.setText(String.format(getString(R.string.topup_help_call), phone));
        }
    }

    private int getHorizLineItem() {
        int linenumber = 3;
        int arrSize = mList.size();
        if (arrSize == 4) {
            linenumber = 2;
        } else if (arrSize == 7 || arrSize == 8 || arrSize >= 12) {
            linenumber = 4;
        }
        return linenumber;
    }

    private void checkListPriceFomat() {
        for (int i = 0; i < mList.size(); i++) {
            String price = mList.get(i).getPrice();
            if (StringUtils.isNullOrEmpty(price)) {
                continue;
            }
            if (null == mDecimalFormat) {
                mDecimalFormat = new DecimalFormat("#0.00");
            }
            try {
                double value = Double.valueOf(price);
                String strprice = mDecimalFormat.format(value);
                final String target = ".00";
                if (strprice.endsWith(target)) {
                    strprice = strprice.replace(target, "");
                }
                mList.get(i).setPrice(strprice);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

    }

    @Subscribe(
            tags = {@Tag(Constants.EventType.TAG_UPDATE_WALLET)}
    )
    public void onUpdateWallet(Object obj) {
        Logger.d("onUpdateWallet");
        TopupContract.Presenter presenter = getPresenter();
        if (presenter != null) {
            presenter.getUserWallet();
        }
    }
}
