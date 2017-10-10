package com.golive.cinema;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Wangzj on 2016/12/8.
 */

public class CommonAlertDialogFragment extends BaseDialog implements View.OnClickListener {

    private String mTitle;
    private String mContent;
    private int mCountDownTime;
    private CountDownTimer mCountDownTimer;

    public static CommonAlertDialogFragment newInstance(String title, String content,
            int countdownTimeMillis) {
        CommonAlertDialogFragment fragment = new CommonAlertDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_TITLE, title);
        arguments.putString(Constants.EXTRA_CONTENT, content);
        arguments.putInt(Constants.EXTRA_COUNTDOWN_TIME, countdownTimeMillis);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        mTitle = arguments.getString(Constants.EXTRA_TITLE);
        mContent = arguments.getString(Constants.EXTRA_CONTENT);
        mCountDownTime = arguments.getInt(Constants.EXTRA_COUNTDOWN_TIME);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.common_alert_dlg_frag, container,
                false);
        TextView titleTv = (TextView) view.findViewById(R.id.title_tv);
        TextView contentTv = (TextView) view.findViewById(R.id.content_tv);
        final TextView btn = (TextView) view.findViewById(R.id.ok_btn);

        titleTv.setText(mTitle);
        contentTv.setText(mContent);
        btn.setOnClickListener(this);

        // count time > 0
        if (mCountDownTime > 0) {
            final String text = getString(R.string.ok_counting);
            mCountDownTimer = new CountDownTimer(mCountDownTime, 1000) {
                public void onTick(long millisUntilFinished) {
                    btn.setText(String.format(text, millisUntilFinished / 1000));
                }

                public void onFinish() {
                    dismiss();
                }
            }.start();
        }
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    public void onClick(View v) {
        dismiss();
    }
}
