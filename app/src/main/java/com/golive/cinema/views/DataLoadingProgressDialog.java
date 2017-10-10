package com.golive.cinema.views;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.golive.cinema.R;

/**
 * Created by Administrator on 2017/6/7.
 */

public class DataLoadingProgressDialog extends ProgressDialog {
    private Context mContext;
    private TextView mTextView;
    private String mMessage;
    private View mProgressBar;

    public DataLoadingProgressDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
    }

    public DataLoadingProgressDialog(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(mContext);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.data_loading_dialog_layout, null, false);
        mTextView = (TextView) view.findViewById(R.id.textView_message);
        mProgressBar = view.findViewById(R.id.progressBar);
        if (!TextUtils.isEmpty(mMessage)) {
            mTextView.setText(mMessage);
        }
        setContentView(view);

        Animation operatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.loading_anim);
        if (operatingAnim != null) {
            LinearInterpolator lin = new LinearInterpolator();
            operatingAnim.setInterpolator(lin);
            mProgressBar.startAnimation(operatingAnim);
        }
    }

    public void setTextMessage(String message) {
        this.mMessage = message;
    }
}
