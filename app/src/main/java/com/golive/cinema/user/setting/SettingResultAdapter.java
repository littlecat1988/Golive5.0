package com.golive.cinema.user.setting;
/**
 * Created by Mowl on 2016/11/4.
 */

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.golive.cinema.R;
import com.golive.cinema.util.UIHelper;

import java.util.List;


public class SettingResultAdapter extends BaseAdapter {

    private final List<SettingResultItem> list;
    private final LayoutInflater mInflater;

    public SettingResultAdapter(LayoutInflater layoutInflater, List<SettingResultItem> list) {
        this.list = list;
        mInflater = layoutInflater;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.user_settings_result_item, null);
            holder.image = (ImageView) convertView.findViewById(R.id.setting_item_image);
            holder.tvName = (TextView) convertView.findViewById(R.id.setting_item_name);
            holder.tvResult = (TextView) convertView.findViewById(R.id.setting_item_result_tv);
            holder.mViewStub = (ViewStub) convertView.findViewById(R.id.setting_item_vs);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SettingResultItem settingResultItem = list.get(position);
        String name = settingResultItem.getName();
        String result = settingResultItem.getResult();
        Drawable drawable = settingResultItem.getDrawable();
        if (drawable != null) {
            holder.image.setImageDrawable(drawable);
        }
        holder.tvName.setText(name);
        holder.tvResult.setText(result);
        boolean checkingUpgrade = settingResultItem.isCheckingUpgrade();
        if (checkingUpgrade) {
            if (null == holder.mLoadingView) {
                holder.mLoadingView = holder.mViewStub.inflate();
            }
        }
        if (holder.mLoadingView != null) {
            UIHelper.setViewVisibleOrGone(holder.mLoadingView, checkingUpgrade);
        }
        return convertView;
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

    class ViewHolder {
        TextView tvName, tvResult;
        ImageView image;
        ViewStub mViewStub;
        View mLoadingView;
    }
}


