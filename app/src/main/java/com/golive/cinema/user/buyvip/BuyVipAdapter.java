package com.golive.cinema.user.buyvip;

/**
 * Created by Mowl on 2016/11/1.
 */


import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.golive.cinema.R;
import com.golive.cinema.user.usercenter.UserPublic;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.VipCombo;

import java.util.List;


class BuyVipAdapter extends RecyclerView.Adapter<BuyVipAdapter.SimpleViewHolder> {
    private final Context mContext;
    private final List<VipCombo> mList;
    private final boolean mIsVip;
    private final SparseArray<SimpleViewHolder> map = new SparseArray<>();

    public BuyVipAdapter(Context context, List<VipCombo> list, boolean isVip) {
        this.mContext = context;
        this.mList = list;
        mIsVip = isVip;
    }

    public class SimpleViewHolder extends RecyclerView.ViewHolder {

        final ImageView tvBenefitIcon;
        final ImageView imageBg;//igv,
        final TextView tvLeft;
        final TextView tvBottom;
        final TextView tvBenefit;
        final TextView tvPrice;
        final TextView tvOldPrice;

        public SimpleViewHolder(View view) {
            super(view);
            tvLeft = (TextView) view.findViewById(R.id.tv_left);
            tvBottom = (TextView) view.findViewById(R.id.tv_bottom);
            tvBenefit = (TextView) view.findViewById(R.id.tv_benefit);
            tvBenefitIcon = (ImageView) view.findViewById(R.id.tv_benefit_icon);
            tvPrice = (TextView) view.findViewById(R.id.vip_item_tv_price);
            imageBg = (ImageView) view.findViewById(R.id.image_bg);
            tvOldPrice = (TextView) view.findViewById(R.id.vip_item_tv_oldprice);
        }
    }

    @Override
    public BuyVipAdapter.SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.user_buyvip_list_item,
                parent, false);
        return new BuyVipAdapter.SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {
        VipCombo vipPackage = (VipCombo) getItem(position);
        if (vipPackage != null) {
            holder.tvLeft.setText(vipPackage.getName());

            String oldPrice = vipPackage.getPrice();
            String vipPrice = vipPackage.getVipPrice();
            String curPrice = vipPackage.getCurPrice();

            String rmbStr = mContext.getString(R.string.RMB);
            String originalPrice = mContext.getString(R.string.original_price);
            if (mIsVip) {
                holder.tvPrice.setText(vipPrice + rmbStr);
                if (UserPublic.isTwoPriceSame(vipPrice, oldPrice)) {//比较价格
                    holder.tvOldPrice.setVisibility(View.GONE);
                } else {
                    holder.tvOldPrice.setVisibility(View.VISIBLE);
                    holder.tvOldPrice.setText(originalPrice + oldPrice + rmbStr);
                    holder.tvOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                }

            } else {
                if (UserPublic.isTwoPriceSame(curPrice, oldPrice)) {//比较价格
                    holder.tvPrice.setText(oldPrice + rmbStr);
                    holder.tvOldPrice.setVisibility(View.GONE);
                } else {
                    holder.tvPrice.setText(curPrice + rmbStr);
                    holder.tvOldPrice.setVisibility(View.VISIBLE);
                    holder.tvOldPrice.setText(originalPrice + oldPrice + rmbStr);
                    holder.tvOldPrice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                }

            }

            //角标信息
            String benefitName = vipPackage.getBenefitName();
            if (StringUtils.isNullOrEmpty(benefitName) || benefitName.equals("null")) {
                holder.tvBenefit.setVisibility(View.GONE);
                holder.tvBenefitIcon.setVisibility(View.GONE);
            } else {
                benefitName = " " + benefitName;
                holder.tvBenefit.setVisibility(View.VISIBLE);
                holder.tvBenefitIcon.setVisibility(View.VISIBLE);
                holder.tvBenefit.setText(benefitName);
            }

            //理财信息
            String financeName = vipPackage.getFinanceName();
            if (StringUtils.isNullOrEmpty(financeName) || financeName.equals("null")) {
                holder.tvBottom.setText("");
            } else {
                holder.tvBottom.setText(financeName);
            }
        }

        if (map.indexOfKey(position) < 0) {
            map.put(position, holder);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private Object getItem(int position) {
        if (mList != null) {
            return mList.get(position);
        }
        return null;
    }
}