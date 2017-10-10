package com.golive.cinema.user.consumption;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_TRADE_RECORD;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_USER_CENTER;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.WalletOperationItem;
import com.initialjie.log.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class ConsumptionFragment extends MvpFragment implements ConsumptionContract.View {
    private ConsumptionContract.Presenter mPresenter;
    private View mNoItemView;
    private ListView mListView;
    private TextView mNoticeTv;
    private View mNoticeIgv;
    private ProgressDialog mProgressDialog;
    private ConsumptionListAdapter mListAdapter;
    private long mEnterTime;
    private final List<UserConsumptionItem> mList = new ArrayList<>();

    public static ConsumptionFragment newInstance() {
        return new ConsumptionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.user_consumption, container, false);
        mListView = (ListView) root.findViewById(R.id.user_lv_consumtion);
        mNoItemView = root.findViewById(R.id.user_consumption_no_item_view);
        mNoticeTv = (TextView) mNoItemView.findViewById(R.id.notice_tv);
        mNoticeTv.setText(R.string.consumption_no_history);
        mNoticeIgv = mNoItemView.findViewById(R.id.notice_igv);
        mNoticeIgv.setBackgroundResource(R.drawable.consumption_no_history);
        mNoItemView.setVisibility(View.GONE);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initListView();
        mPresenter.start();

        mEnterTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(getContext()).reportEnterActivity(VIEW_CODE_TRADE_RECORD,
                "交易记录", VIEW_CODE_USER_CENTER);
    }

    private void initListView() {
        mListAdapter = new ConsumptionListAdapter(LayoutInflater.from(getContext()), mList);
        mListView.setAdapter(mListAdapter);
        //        setGridView(vipPackagesList.size());
        /*mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // 当前焦点在listview中item位置
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });*/
    }

    @Override
    public void setPresenter(ConsumptionContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    @Override
    public ConsumptionContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
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
    public void showGetListView(List<WalletOperationItem> lists) {
        Logger.d("showGetListView");
        mList.clear();

        if (lists != null && !lists.isEmpty()) {
            //List<UserConsumptionItem> boardList = new ArrayList<>();
            //List<UserConsumptionItem> shopList = new ArrayList<>();
            for (WalletOperationItem operation : lists) {
                String value = operation.getValue();
                String name = operation.getReason();
                String type = StringUtils.getDefaultStringIfEmpty(operation.getType());
                UserConsumptionItem Item = new UserConsumptionItem(name, operation.getCreateTime(),
                        value, type);
                Item.setPayDetail(operation.getPayDetail());
                Item.setCredit(operation.getCredit());
                switch (type) {
                    case WalletOperationItem.TYPE_ADVERT: // 广告
                        Item.setName(getString(R.string.user_see_advice));
                        //boardList.add(Item);
                        break;
                    case WalletOperationItem.TYPE_EXCHANGE: // 商城兑换
                        Item.setName(getString(R.string.user_exchange_shop));
                        //shopList.add(Item);
                        break;
                    default:
//                        if (!isTheDateDigit(value)) {//优惠推广type.equals("42")
//                        } else {
//                            String value_ok = getOkValuePriceString(value);
//                            Item.setPayPrice(value_ok);
//                        }
                        String value_ok = getOkValuePriceString(value);
                        Item.setPayPrice(value_ok);
                        break;
                }
                mList.add(Item);
            }
            //checkConsumeListInDate(boardList);
            //checkConsumeListInDate(shopList);

//            initGetCreditInfo();//获取信用支付的接口
            //comparatorReorderList();
        }
        checkNoItemView();
        mListAdapter.notifyDataSetChanged();
    }

    @Override
    public void showGetError(String errMsg) {
        String text = getString(R.string.consumption_get_failed);
        if (!StringUtils.isNullOrEmpty(errMsg)) {
            text += ", " + errMsg;
        }
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void checkNoItemView() {
        mNoItemView.setVisibility(null == mList || mList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    /*private void comparatorReorderList() {
        // "2014-07-25 09:44:25"
        final SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        Collections.sort(mList, new Comparator<UserConsumptionItem>() {

            @Override
            public int compare(UserConsumptionItem arg0,
                               UserConsumptionItem arg1) {
                Date d1 = getItemDate(arg1);
                Date d2 = getItemDate(arg0);
                if (null == d1 && null == d2) {
                    return 0;
                }
                if (d1 == null) {
                    return -1;
                }
                if (d2 == null) {
                    return 1;
                }
                return d1.compareTo(d2);
            }

            private Date getItemDate(UserConsumptionItem item) {
                if (null == item || null == item.getTime()) {
                    return null;
                }
                try {
                    return sdf.parse(item.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        int size = mList.size();
        final int MAX = 30;
        if (size > MAX) {
            for (int i = (size - 1); i > MAX; i--) {
                mList.remove(i);
            }
        }
    }*/

    private boolean isTheDateDigit(String str) {
        return str.matches("-?[0-9]+.*[0-9]*");
    }

    private String getOkValuePriceString(String price) {
        String moneyStr = getString(R.string.price_RMB);
        if (StringUtils.isNullOrEmpty(price)) {
            return String.format(moneyStr, 0);
        }

        try {
            double priceF = Double.parseDouble(price);
            String str = String.format(moneyStr, priceF);
            if (new BigDecimal(price).compareTo(BigDecimal.ZERO) > 0) {
                str = "+" + str;
            }
            return str;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return price;
    }

    /*private String formatPriceString(String price) {
        if (StringUtils.isNullOrEmpty(price)) {
            return "0.00";
        }
        try {
            double ff = Double.parseDouble(price);
            return mZeroDf.format(ff);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return price;
    }*/

//    private void checkBoardHaveInDate(List<UserConsumptionItem> shoplist) {
//        if (null == shoplist || shoplist.isEmpty()) {
//            return;
//        }
//        List<UserConsumptionItem> mShopList = new ArrayList<UserConsumptionItem>();
//        mShopList.add(shoplist.get(0));
//        for (int i = 1; i < shoplist.size(); i++) {
//            UserConsumptionItem item_i = shoplist.get(i);
//            String date1 = getDateFromTime(item_i.getTime());
//            String price1 = item_i.getPayPrice();
//            for (int j = 0; j < mShopList.size(); j++) {
//                UserConsumptionItem item_j = mShopList.get(j);
//                String date2 = getDateFromTime(item_j.getTime());
//                String price2 = item_j.getPayPrice();
//                if (!StringUtils.isNullOrEmpty(date1) && !StringUtils.isNullOrEmpty(date2)
//                        && date1.equals(date2)) {
//                    UserConsumptionItem item = item_j;
//                    String add = addTwoPrice(price1, price2);
//                    if (add != null) {
//                        item.setPayPrice(add);
//                    }
//                    mShopList.set(j, item);
//                    break;
//                } else if (j == (mShopList.size() - 1)) {
//                    mShopList.add(item_i);
//                    break;
//                }
//            }
//        }
//        UserConsumptionItem aitem;
//        for (int k = 0; k < mShopList.size(); k++) {
//            aitem = mShopList.get(k);
//            String value_ok = getOkValuePriceString(aitem.getPayPrice());
//            aitem.setPayPrice(value_ok);
//            mList.add(aitem);
//        }
//    }

    // 商城消费当天累加
    /*private void checkConsumeListInDate(List<UserConsumptionItem> consumeList) {
        if (null == consumeList || consumeList.isEmpty()) {
            return;
        }
        List<UserConsumptionItem> mShopList = new ArrayList<>();
        mShopList.add(consumeList.get(0));
        for (int i = 1; i < consumeList.size(); i++) {
            UserConsumptionItem item_i = consumeList.get(i);
            String date1 = getDateFromTime(item_i.getTime());
            String price1 = item_i.getPayPrice();
            for (int j = 0; j < mShopList.size(); j++) {
                UserConsumptionItem item_j = mShopList.get(j);
                String date2 = getDateFromTime(item_j.getTime());
                String price2 = item_j.getPayPrice();
                if (!StringUtils.isNullOrEmpty(date1) && !StringUtils.isNullOrEmpty(date2)
                        && date1.equals(date2)) {
                    UserConsumptionItem item = item_j;
                    String add = addTwoPrice(price1, price2);
                    if (add != null) {
                        item.setPayPrice(add);
                    }
                    mShopList.set(j, item);
                    break;
                } else if (j == (mShopList.size() - 1)) {
                    mShopList.add(item_i);
                    break;
                }
            }
        }
        UserConsumptionItem aitem;
        for (int k = 0; k < mShopList.size(); k++) {
            aitem = mShopList.get(k);
            String value_ok = getOkValuePriceString(aitem.getPayPrice());
            aitem.setPayPrice(value_ok);
            mList.add(mShopList.get(k));
        }
    }*/

    /*private String getDateFromTime(String time) {
        if (time == null) {
            return null;
        }
        int end = time.indexOf(':') - 3;
        return time.substring(0, end);
    }

    private String addTwoPrice(String price1, String price2) {
        if (price1 == null || price2 == null) {
            return null;
        }
        try {
            float ff1 = Float.parseFloat(price1);
            float ff2 = Float.parseFloat(price2);
            float ff3 = ff1 + ff2;
            return mZeroDf.format(ff3);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
        String time = String.valueOf((System.currentTimeMillis() - mEnterTime) / 1000);
        StatisticsHelper.getInstance(getActivity()).reportExitActivity(VIEW_CODE_TRADE_RECORD,
                "交易记录", "", time);
    }
}
