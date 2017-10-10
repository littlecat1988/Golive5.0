package com.golive.cinema.user.message;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_MESSAGE;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_USER_CENTER;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.user.usercenter.UserPublic;
import com.golive.cinema.util.DateHelper;
import com.golive.cinema.util.FragmentUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.CreditOperation;
import com.golive.network.entity.FinanceMessage;
import com.golive.network.entity.Message;
import com.golive.network.entity.ServerMessage;
import com.golive.network.helper.UserInfoHelper;
import com.golive.pay.PayManager;

import java.util.ArrayList;
import java.util.List;


public class MessageFragment extends MvpFragment implements MessageContract.View {
    private MessageContract.Presenter mPresenter;
    private ListView mListView;
    private View mNoItemView;
    private TextView mTitle;
    private TextView mNoticeTv;
    private View mNoticeIgv;
    private MessageListAdapter mListAdapter;
    private ProgressDialog mProgressDialog;
    private long mEnterTime;
    private String mUserId;
    private PayManager mPayManager;
    private final List<ServerMessage> mList = new ArrayList<>();

    public static MessageFragment newInstance() {
        return new MessageFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_message, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTitle = (TextView) view.findViewById(R.id.user_message_title_tv);
        mListView = (ListView) view.findViewById(R.id.user_message_lv);
        mNoItemView = view.findViewById(R.id.user_message_no_item_view);
        mNoticeTv = (TextView) mNoItemView.findViewById(R.id.notice_tv);
        mNoticeTv.setText(R.string.message_you_have_no_message);
        mNoticeIgv = mNoItemView.findViewById(R.id.notice_igv);
        mNoticeIgv.setBackgroundResource(R.drawable.msg_no_history);
        mNoItemView.setVisibility(View.GONE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUserId = UserInfoHelper.getUserId(getActivity());
        mPayManager = new PayManager(getActivity());
        if (getPresenter() != null) {
            getPresenter().start();
        }

        mEnterTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(getContext()).reportEnterActivity(VIEW_CODE_MESSAGE, "消息",
                VIEW_CODE_USER_CENTER);
    }

    private void initListView() {
        mListView.setItemsCanFocus(true);
        mListAdapter = new MessageListAdapter(getContext(), mList);
        mListView.setAdapter(mListAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ServerMessage serverMessage = mList.get(position);
                if (serverMessage != null) {
                    setMessageDialog(serverMessage);
                    saveMessageOpen(serverMessage);
                    mList.get(position).setState(ServerMessage.SERVER_MESSAGE_ID_KEY_READED);
                    mListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void setPresenter(MessageContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    @Override
    protected MessageContract.Presenter getPresenter() {
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
                        getString(R.string.message_loading));
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
    public void showAllMessageView() {
        if (null == mList || mList.isEmpty()) {
            mTitle.setVisibility(View.GONE);
            mNoItemView.setVisibility(View.VISIBLE);
        } else {
            mTitle.setVisibility(View.VISIBLE);
            mNoItemView.setVisibility(View.GONE);
            initListKey();
            initListView();
        }
    }

    @Override
    public void copyServerToMessage(List<ServerMessage> messageList) {
        if (null == messageList || messageList.isEmpty()) {
            return;
        }

        for (int i = 0; i < messageList.size(); i++) {
            ServerMessage server = messageList.get(i);
            if (server != null) {
                String serverTime = server.getServerTime();
                String time = DateHelper.dateFormatToString(
                        DateHelper.stringFormatToDate(serverTime, UserPublic.DATE_FORMAT),
                        UserPublic.DATE_FORMAT);
                server.setServerTime(time);
                mList.add(server);
            }
        }
    }

    @Override
    public void copyCreditToMessage(CreditOperation credit) {
        if (null == credit) {
            return;
        }
        ServerMessage serverMessage = new ServerMessage();
        serverMessage.setType(ServerMessage.SERVER_MESSAGE_TYPE_CREDIT);
        if (credit.getError() != null) {
            String time = DateHelper.stringFormatDateGMT(credit.getError().getTime(),
                    UserPublic.DATE_FORMAT);
            serverMessage.setServerTime(time);
        }
        serverMessage.setName(getActivity().getString(R.string.credit_operation_commit_btn_text));

        //CreditOperation{value='290.99', currency='RMB', creditLine='20.00',
        // creditDeadLineDays='180', creditRemain='9223372036854775807'}
        String creditValue = credit.getValue();
        if (creditValue != null) {
            double payPriceDb = Double.valueOf(creditValue);
            if (payPriceDb >= 0) {
                creditValue = "0.00";
            }
        }
        String totalCreditMoney = credit.getCreditLine();
        String deadLineDays = credit.getCreditDeadLineDays();
        String remain = credit.getCreditRemain();

        if (!StringUtils.isNullOrEmpty(totalCreditMoney)) {
            UserInfoHelper.setMaxCredit(getContext(), totalCreditMoney);
        }

        if (totalCreditMoney != null && deadLineDays != null && remain != null
                && creditValue != null) {
            String value = String.format(
                    getActivity().getString(R.string.init_credit_operation_alert_content_all),
                    deadLineDays,
                    creditValue,
                    totalCreditMoney);
            Spanned mSpanned = Html.fromHtml(value);
            if (mSpanned != null) {
                serverMessage.setSpannedContent(mSpanned);
            }
            serverMessage.setContent(value);
            serverMessage.setCreditOperation(credit);

            boolean isDue = (remain.startsWith("-")
                    && Double.valueOf(deadLineDays) > 0
                    && Double.valueOf(creditValue) < 0);
            if (isDue) {//显示
                mList.add(serverMessage);
            }
        }
    }

    @Override
    public void copyFinanceToMessage(FinanceMessage finance) {
        if (null == finance) {
            return;
        }
        List<Message> messageList = finance.getMessageList();
        if (messageList != null && messageList.size() > 0) {
            Message oneMessage = messageList.get(0);
            if (oneMessage != null) {
                ServerMessage serverMessage = new ServerMessage();
                serverMessage.setType(ServerMessage.SERVER_MESSAGE_TYPE_FINANCE);
                if (finance.getError() != null) {
                    String time = DateHelper.stringFormatDateGMT(finance.getError().getTime(),
                            UserPublic.DATE_FORMAT);
                    serverMessage.setServerTime(time);
                }

                serverMessage.setName(oneMessage.getTitle());

                String contentStr = oneMessage.getBody();
                if (contentStr.startsWith("<![CDATA[") && contentStr.endsWith("]]")) {
                    int start = "<![CDATA[".length();
                    int end = contentStr.length() - "]]".length() - 1;
                    contentStr = contentStr.substring(start, end);
                }
                if (contentStr.contains("<br />")) {
                    contentStr = contentStr.replace("<br />", "\r\n");
                }
                if (contentStr.contains("<br>")) {
                    contentStr = contentStr.replace("<br>", "\r\n");
                }


                Spanned mSpanned = Html.fromHtml(contentStr);
                if (mSpanned != null) {
                    serverMessage.setSpannedContent(mSpanned);
                }
                serverMessage.setContent(contentStr);

                serverMessage.setDisplaytime(oneMessage.getCreateTime());

                serverMessage.setMessageList(messageList);
                mList.add(serverMessage);
            }
        }
    }

    private void setMessageDialog(ServerMessage message) {
        MessageDetailDialog dialog = FragmentUtils.newFragment(MessageDetailDialog.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(MessageDetailDialog.DIALOG_FRAGMENT_TAG, message);
        dialog.setArguments(bundle);
        dialog.show(getActivity().getSupportFragmentManager(),
                MessageDetailDialog.DIALOG_FRAGMENT_TAG);
    }

    private void initListKey() {
        for (int i = 0; i < mList.size(); i++) {
            ServerMessage serverMessage = mList.get(i);
            serverMessage.setState(ServerMessage.SERVER_MESSAGE_ID_KEY_NEW);
            String key = ServerMessage.getMessageKey(serverMessage);
            if (key != null) {
                serverMessage.setKey(key);
                String saveState = UserInfoHelper.getMessageState(getContext(), key);
                if (!StringUtils.isNullOrEmpty(saveState)
                        && (ServerMessage.SERVER_MESSAGE_ID_KEY_NEW.equals(saveState)
                        || ServerMessage.SERVER_MESSAGE_ID_KEY_READED.equals(saveState))) {
                    serverMessage.setState(saveState);
                } else {
                    serverMessage.setState(ServerMessage.SERVER_MESSAGE_ID_KEY_NEW);
                    UserInfoHelper.setMessageState(getContext(), key,
                            ServerMessage.SERVER_MESSAGE_ID_KEY_NEW);
                }
            }
        }
    }

    private void saveMessageOpen(ServerMessage message) {
        String key = ServerMessage.getMessageKey(message);
        if (key != null) {
            UserInfoHelper.setMessageState(getContext(), key,
                    ServerMessage.SERVER_MESSAGE_ID_KEY_READED);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String time = String.valueOf((System.currentTimeMillis() - mEnterTime) / 1000);
        StatisticsHelper.getInstance(getActivity()).reportExitActivity(VIEW_CODE_MESSAGE, "消息", "",
                time);
        if (mPayManager != null) {
            mPayManager.destory();
        }
    }
}
