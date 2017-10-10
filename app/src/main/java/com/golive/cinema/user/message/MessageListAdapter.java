package com.golive.cinema.user.message;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.ServerMessage;

import java.util.List;

public class MessageListAdapter extends BaseAdapter {
    private final List<ServerMessage> mServerMessageList;
    private final LayoutInflater mInflater;

    public MessageListAdapter(Context context, List<ServerMessage> msgList) {
        this.mInflater = LayoutInflater.from(context);
        mServerMessageList = msgList;
    }

    @Override
    public int getCount() {
        if (mServerMessageList != null) {
            return mServerMessageList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        ServerMessage serverMessage;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.user_message_item, parent, false);
            holder.image = (ImageView) convertView.findViewById(R.id.message_item_image);
            holder.time = (TextView) convertView.findViewById(R.id.message_item_time);
            holder.title = (TextView) convertView.findViewById(R.id.message_item_name);
            holder.content = (TextView) convertView.findViewById(R.id.message_item_detail);
            holder.redIcon = (ImageView) convertView.findViewById(R.id.message_item_image_left);
//            holder.focusView = (Button) convertView.findViewById(R.id.message_item_focus);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        serverMessage = mServerMessageList.get(position);
        String name = serverMessage.getName();
        if (!StringUtils.isNullOrEmpty(name)) {
            holder.title.setText(name);
        }

        //优先显示彩色信息
        Spanned mSpanned = serverMessage.getSpannedContent();
        if (mSpanned != null) {
            holder.content.setText(mSpanned);
        } else {
            String msg = serverMessage.getContent();
            Spanned mSpanned2 = Html.fromHtml(msg);
            if (mSpanned2 != null) {
                holder.content.setText(mSpanned2);
            } else {
                holder.content.setText(msg);
            }
        }

//        String name = serverMessage.getName();
//        if(name != null && !"".equals(name)){
//            holder.title.setText(name);
//        }
//        else{
//            CreditOperation creditOperation = serverMessage.getCreditOperation();
//            if(creditOperation != null){
//                holder.title.setText(mContext.getString(R.string
// .credit_operation_commit_btn_text));
//            }else {
//                List<Message> messageList = serverMessage.getMessageList();
//                if(messageList != null && messageList.size() > 0){
//                    holder.title.setText(messageList.get(0).getTitle());
//                }else{
//                    holder.title.setText("");
//                }
//            }
//        }
//        String msg = serverMessage.getContent();
//        if(msg != null && !"".equals(msg)){
//            holder.content.setText(msg);
//        }
//        else{
//            CreditOperation creditOperation = serverMessage.getCreditOperation();
//            if(creditOperation != null){
//                Spanned mSpanned =Html.fromHtml(String.format(mContext.getString(R.string
// .init_credit_operation_alert_content_all), creditOperation.getCreditLine(), creditOperation
// .getCreditDeadLineDays()));
//                holder.content.setText(mSpanned);
//            }else {
//                List<Message> messageList = serverMessage.getMessageList();
//                if(messageList != null && messageList.size() > 0){
//                    holder.content.setText(messageList.get(0).getBody());
//                }else{
//                    holder.content.setText("");
//                }
//            }
//        }

        String serverTime = serverMessage.getServerTime();
        if (serverTime != null) {
            holder.time.setText(serverTime);
//            if(serverMessage.getType().equals(ServerMessage.SERVER_MESSAGE_TYPE_OTHER)){
//                holder.time.setText(StringUtils.stringFormatDateGMT(serverTime, UserPublic
// .DATE_FORMAT));
//            }else{
//                holder.time.setText(StringUtils.dateFormatToString(StringUtils
// .stringFormatToDate(serverTime, UserPublic.DATE_FORMAT), UserPublic.DATE_FORMAT));
//            }
        } else {
            holder.time.setText("");
        }

        String state = serverMessage.getState();
        String messageType = serverMessage.getType();//1-通知消息；2-停机维护信息；3-充值通知提示；4-活动消息
        if (!StringUtils.isNullOrEmpty(messageType) && messageType.equals(
                ServerMessage.SERVER_MESSAGE_TYPE_SHUTDOWN)) {
            holder.image.setImageResource(R.drawable.user_message_icon_system);
        } else {
            if (!StringUtils.isNullOrEmpty(state)
                    && ServerMessage.SERVER_MESSAGE_ID_KEY_READED.equals(state)) {
                holder.image.setImageResource(R.drawable.user_message_icon_open);
            } else {
                holder.image.setImageResource(R.drawable.user_message_icon_book);
            }
        }
        if (!StringUtils.isNullOrEmpty(state) && ServerMessage.SERVER_MESSAGE_ID_KEY_READED.equals(
                state)) {
            holder.redIcon.setVisibility(View.INVISIBLE);
        } else {
            holder.redIcon.setVisibility(View.VISIBLE);
        }

//        else if(messageType.equals(ServerMessage.SERVER_MESSAGE_TYPE_RECHARGE)
//                || messageType.equals(ServerMessage.SERVER_MESSAGE_TYPE_OTHER)){
//            holder.image.setImageResource(R.drawable.user_message_item_read_selector);
//        }else{
//            holder.image.setImageResource(R.drawable.user_message_item_read_tip_selector);
//        }

//        holder.focusView.setOnClickListener(new DetailButtonClickListener(serverMessage));
        return convertView;
    }

    class ViewHolder {
        TextView time;
        ImageView image;
        TextView title;
        TextView content;
        ImageView redIcon;
//        Button focusView;
    }

}


