package com.golive.cinema.user.message;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.ServerMessage;

/**
 * Created by chgang on 2016/11/22.
 */

public class MessageDetailDialog extends BaseDialog {

    public static final String DIALOG_FRAGMENT_TAG = "MessageDetailDialog_Tag";
    private ServerMessage mServerMessage;

    private ImageView mMsgDetailIgv;
    private TextView mMsgDetailNameTv, mMsgDetailTimeTv, mMsgDetailContentTv;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mServerMessage = (ServerMessage) getArguments().getSerializable(DIALOG_FRAGMENT_TAG);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        setCancelable(true);
        return inflater.inflate(R.layout.user_message_detail_page, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(DIALOG_FRAGMENT_TAG, mServerMessage.toString());
        mMsgDetailIgv = (ImageView) view.findViewById(R.id.message_detail_image);
        mMsgDetailNameTv = (TextView) view.findViewById(R.id.message_detail_name);
        mMsgDetailTimeTv = (TextView) view.findViewById(R.id.message_detail_time);
        mMsgDetailContentTv = (TextView) view.findViewById(R.id.message_detail_content);
        setViewValues();
    }

    private void setViewValues() {
//        mMsgDetailNameTv
        mMsgDetailNameTv.getPaint().setFakeBoldText(true);
        String name = mServerMessage.getName();
        if (!StringUtils.isNullOrEmpty(name)) {
            mMsgDetailNameTv.setText(name);
        }
        /*
        else{
            CreditOperation creditOperation = mServerMessage.getCreditOperation();
            if(creditOperation != null){
                mMsgDetailNameTv.setText(getString(R.string.credit_operation_commit_btn_text));
            }else {
                List<Message> messageList = mServerMessage.getMessageList();
                if(messageList != null && messageList.size() > 0){
                    mMsgDetailNameTv.setText(messageList.get(0).getTitle());
                }else{
                    mMsgDetailNameTv.setText("");
                }
            }
        }*/

//        mMsgDetailTimeTv
        String serverTime = mServerMessage.getServerTime();
        if (!StringUtils.isNullOrEmpty(serverTime)) {
            mMsgDetailTimeTv.setText(serverTime);
//            if(mServerMessage.getType().equals(ServerMessage.SERVER_MESSAGE_TYPE_OTHER)){
//                mMsgDetailTimeTv.setText(StringUtils.stringFormatDateGMT(serverTime,
// UserPublic.DATE_FORMAT));
//            }else{
//                mMsgDetailTimeTv.setText(StringUtils.dateFormatToString(
//                        StringUtils.stringFormatToDate(serverTime, UserPublic.DATE_FORMAT),
// UserPublic.DATE_FORMAT));
//            }
        } else {
            mMsgDetailTimeTv.setText("");
        }

//        mMsgDetailContentTv
        //优先显示彩色信息
        Spanned mSpanned = mServerMessage.getSpannedContent();
        if (mSpanned != null) {
            mMsgDetailContentTv.setText(mSpanned);
        } else {
            String msg = mServerMessage.getContent();
            if (!StringUtils.isNullOrEmpty(msg)) {
                mMsgDetailContentTv.setText(msg);
            }
        }
        /*
        String msg = mServerMessage.getContent();
        if(msg != null && !"".equals(msg)){
            mMsgDetailContentTv.setText(msg);
        }else{
            CreditOperation creditOperation = mServerMessage.getCreditOperation();
            if(creditOperation != null){
                mMsgDetailContentTv.setText(Html.fromHtml(String.format(
                        getString(R.string.init_credit_operation_alert_content_all),
                        creditOperation.getCreditLine(),
                        creditOperation.getCreditDeadLineDays())));
            }else {
                List<Message> messageList = mServerMessage.getMessageList();
                if(messageList != null && messageList.size() > 0){
                    mMsgDetailContentTv.setText(messageList.get(0).getBody());
                }else{
                    mMsgDetailContentTv.setText("");
                }
            }
        }*/

//        mMsgDetailIgv
        String messageType = mServerMessage.getType();
        if (!StringUtils.isNullOrEmpty(messageType) && messageType.equals(
                ServerMessage.SERVER_MESSAGE_TYPE_SHUTDOWN)) {
            mMsgDetailIgv.setImageResource(R.drawable.user_message_icon_system);
        } else {
            mMsgDetailIgv.setImageResource(R.drawable.user_message_icon_open);
        }
//        if(messageType.equals(ServerMessage.SERVER_MESSAGE_TYPE_RECHARGE)
//                || messageType.equals(ServerMessage.SERVER_MESSAGE_TYPE_OTHER)){
//            mMsgDetailIgv.setImageResource(R.drawable.user_message_item_read_selector);
//        }else{
//            mMsgDetailIgv.setImageResource(R.drawable.user_message_item_read_tip_selector);
//        }

    }
}
