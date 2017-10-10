package com.golive.cinema.user.setting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class LanguageAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<String> mList = new ArrayList<>();
    private final LayoutInflater mInflater;
    private final boolean mIsClarity;

    public LanguageAdapter(Context context, List<String> list, boolean isClarity) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mIsClarity = isClarity;

        if (!mList.isEmpty()) {
            mList.clear();
        }
        this.mList.addAll(list);
    }

    @Override
    public int getCount() {
        if (null == mList) {
            return 0;
        }
        return mList.size();
    }

    @Override
    public Object getItem(int arg0) {
        if (mList != null) {
            return mList.get(arg0);
        }
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.user_setting_language_item, null);
            holder.tvName = (TextView) convertView.findViewById(R.id.setting_language_name_btn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (mIsClarity) {
            holder.tvName.setText(getClarityName(mList.get(position)));
        } else {
            holder.tvName.setText(mList.get(position));
        }
        return convertView;
    }

    class ViewHolder {
        TextView tvName;
    }

    private String getClarityName(String clarity) {
        if (!StringUtils.isNullOrEmpty(clarity)) {
            switch (clarity) {
                case Constants.PLAY_MEDIA_RANK_CLARITY_SUPER:
                    return mContext.getString(R.string.theatre_play_clarity_super_text);
                case Constants.PLAY_MEDIA_RANK_CLARITY_STANDARD:
                    return mContext.getString(R.string.theatre_play_clarity_standard_text);
                case Constants.PLAY_MEDIA_RANK_CLARITY_HIGH:
                    return mContext.getString(R.string.theatre_play_clarity_high_text);
                case Constants.PLAY_MEDIA_RANK_CLARITY_1080:
                    return mContext.getString(R.string.theatre_play_clarity_1080p_text);
            }
        }

        return mContext.getString(R.string.theatre_play_clarity_high_text);
    }
}



