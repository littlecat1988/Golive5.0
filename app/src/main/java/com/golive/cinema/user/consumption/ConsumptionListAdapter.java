package com.golive.cinema.user.consumption;

/**
 * Created by Mowl on 2016/11/3.
 */


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;

import java.util.List;


public class ConsumptionListAdapter extends BaseAdapter {

    private final List<UserConsumptionItem> list;
    private final LayoutInflater mInflater;

    public ConsumptionListAdapter(LayoutInflater layoutInflater, List<UserConsumptionItem> list) {
        this.list = list;
        mInflater = layoutInflater;
    }

    @Override
    public int getCount() {
        if (null == list) {
            return 0;
        }
        return list.size();
    }

    @Override
    public Object getItem(int arg0) {
        if (list != null) {
            return list.get(arg0);
        }
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    public void addItem(UserConsumptionItem it) {
        list.add(it);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.user_consumption_list, parent, false);
            //holder.iv = (ImageView)convertView.findViewById(R.id.iv_update_info_img);
            holder.tvName = (TextView) convertView.findViewById(R.id.user_tv_csp_name);
            holder.tvGold = (TextView) convertView.findViewById(R.id.user_tv_csp_gold);
            holder.tvTime = (TextView) convertView.findViewById(R.id.user_tv_csp_time);
            //holder.image = (ImageView)convertView.findViewById(R.id.user_wallet_image_icon);
            holder.tvPay = (TextView) convertView.findViewById(R.id.user_tv_credit_pay);
            holder.lineView = convertView.findViewById(R.id.user_wallet_header_line);
            holder.endlineView = convertView.findViewById(R.id.user_wallet_bottom_line);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //ImageLoader.getInstance().displayImage("", holder.iv);
        String name = list.get(position).getName();
        String price = list.get(position).getPayPrice();
        String time = list.get(position).getTime();
//        String type = list.get(position).getType();
        String isCridit = list.get(position).getCredit();
        boolean isAdd = isGetGold(price);
        setNameText(holder.tvName, name, isAdd);

//		if(!price.startsWith("-") && !price.startsWith("+")){
//			price ="+"+price;
//		}
        setGoldNumber(holder.tvGold, price, isAdd);
        setTimeText(holder.tvTime, time);
        //setImageView(holder.image,type);
        if (!StringUtils.isNullOrEmpty(isCridit) && "1".equals(isCridit)) {
            holder.tvPay.setVisibility(View.VISIBLE);
        } else {
            holder.tvPay.setVisibility(View.GONE);
        }
        if (position == 0) {
            holder.lineView.setVisibility(View.VISIBLE);
        } else {
            holder.lineView.setVisibility(View.GONE);
        }
        if (position == (list.size() - 1)) {
            holder.endlineView.setVisibility(View.VISIBLE);
        } else {
            holder.endlineView.setVisibility(View.GONE);
        }
        return convertView;
    }

    class ViewHolder {
        TextView tvName, tvGold, tvTime, tvPay;
        ImageView image;
        View lineView, endlineView;
    }

    private boolean isGetGold(String value) {
        return !(!StringUtils.isNullOrEmpty(value) && value.startsWith("-"));
    }

    public void setNameText(TextView tv, String name, boolean color) {
        //ColorStateList clr;
        tv.setText(name);
//        if (color){
//            clr = context.getResources().getColorStateList(R.color
// .sel_color_wallet_huang_white);//user_csp_yellow
//        }else{
//            clr = context.getResources().getColorStateList(R.color.user_text_focus);
// user_csp_white user_text_focus
//        }
//        tv.setTextColor(clr);
    }

    public void setGoldNumber(TextView tv, String value, boolean isadd) {
        //ColorStateList clr;
        tv.setText(value);
//        if (isadd){
//            clr = context.getResources().getColorStateList(R.color.sel_color_wallet_lv_white);
// user_csp_green
//        }else{
//            clr = context.getResources().getColorStateList(R.color.sel_color_wallet_red_white);
// user_csp_red
//        }
//        tv.setTextColor(clr);

    }

    public void setTimeText(TextView tv, String time) {
        //2014-07-07 17:38:43
        if (null == time) {
            return;
        }

        String famattime = time.substring(0, 10);
        String dotTime = famattime.replace("-", ".");
        tv.setText(dotTime);
    }
}


