package com.golive.cinema.user.myinfo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.golive.cinema.R;

import java.util.List;

/**
 * Created by Mowl on 2016/11/8.
 */

public class MyinfoListAdpter extends BaseAdapter {

    private final List<MyinfoItem> mMyinfoItems;
    private final LayoutInflater mInflater;

    public MyinfoListAdpter(Context context, List<MyinfoItem> list) {
        this.mMyinfoItems = list;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (null == mMyinfoItems) {
            return 0;
        }
        return mMyinfoItems.size();
    }

    @Override
    public Object getItem(int arg0) {
        if (mMyinfoItems != null) {
            return mMyinfoItems.get(arg0);
        }
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.user_myinfo_item, null);
            holder.image = (ImageView) convertView.findViewById(R.id.myinfo_item_image);
            holder.tvName = (TextView) convertView.findViewById(R.id.myinfo_item_name);
//            holder.image_r = (ImageView) convertView.findViewById(R.id.myinfo_item_image);
            holder.image_r = holder.image;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String aname = mMyinfoItems.get(position).getName();
//            String aresult=mMyinfoItems.get(position).getResult();
        setNameText(holder.tvName, aname);
        Drawable draw = mMyinfoItems.get(position).getIcon();
        if (draw != null) {
            holder.image_r.setImageDrawable(draw);
        }
//            setResultText(holder.tvResult,aresult);
        return convertView;
    }

    class ViewHolder {
        TextView tvName, tvResult;
        ImageView image, image_r;
    }

    private void setNameText(TextView tv, String name) {
        tv.setText(name);
    }
}
