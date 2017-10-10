package com.golive.cinema.user.topup;

/**
 * Created by Mowl on 2016/11/1.
 */


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.TopupRechargeItem;

import java.util.List;

class TopupAdapter extends BaseAdapter {
    private final List<TopupRechargeItem> mList;
    private final LayoutInflater mInflater;
    private final Context mContext;

    public TopupAdapter(Context context, List<TopupRechargeItem> list) {
        mContext = context;
        mList = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mList != null) {
            return mList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        if (null == convertView) {
            convertView = mInflater.inflate(R.layout.user_topup_list_item, null);
            holder = new Holder();
//            holder.igv = (ImageView) convertView.findViewById(R.id.igv_select_icon);
//            holder.tvLeft = (TextView) convertView.findViewById(R.id.tv_left);
            holder.tvBottom = (TextView) convertView.findViewById(R.id.tv_bottom);
            holder.tvBenefit = (TextView) convertView.findViewById(R.id.tv_benefit);
            holder.tvBenefitIcon = (ImageView) convertView.findViewById(R.id.tv_benefit_icon);
            holder.tvPrice = (TextView) convertView.findViewById(R.id.tv_price);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        TopupRechargeItem DateItem = (TopupRechargeItem) getItem(position);
        if (DateItem != null) {
//            holder.tvLeft.setText(vipPackage.getName());
            holder.tvPrice.setText(DateItem.getPrice() + mContext.getString(R.string.RMB));
            String benefitName = DateItem.getBenefitName();
            if (StringUtils.isNullOrEmpty(benefitName) || "null".equals(benefitName)) {
                holder.tvBenefit.setVisibility(View.INVISIBLE);
                holder.tvBenefitIcon.setVisibility(View.INVISIBLE);
            } else {
                benefitName = " " + benefitName;
                holder.tvBenefit.setVisibility(View.VISIBLE);
                holder.tvBenefitIcon.setVisibility(View.VISIBLE);
                holder.tvBenefit.setText(benefitName);
            }
        }
        return convertView;
    }

    class Holder {
        ImageView tvBenefitIcon;//igv,
        TextView tvBottom, tvBenefit, tvPrice;
    }
}
