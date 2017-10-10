package com.golive.cinema.views;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.golive.cinema.R;

/**
 * Created by chgang on 2016/11/4.
 */

public class UpgradeDownloadImageView extends LinearLayout {
//    private static final String TAG = UpgradeDownloadImageView.class.getSimpleName();

    private ImageView mDownloadProgress;
    private TextView mDownloadProgressPer;
    private FrameLayout mDownloadBG;

    public UpgradeDownloadImageView(Context context) {
        super(context);
    }

    public UpgradeDownloadImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UpgradeDownloadImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void setProgress(int progress, int total) {
        FrameLayout.LayoutParams lp =
                (FrameLayout.LayoutParams) mDownloadProgress.getLayoutParams();
        lp.width = progress;
        mDownloadProgress.setLayoutParams(lp);
        mDownloadProgressPer.setText(total + "%");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDownloadBG = (FrameLayout) this.findViewById(R.id.progress_layout);
        mDownloadProgress = (ImageView) this.findViewById(R.id.sv_progressbar_speed);
        mDownloadProgressPer = (TextView) this.findViewById(R.id.upgrade_progress_per);
        setVisibility(View.GONE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
    }

    private int getDownloadProgressWidth() {
        return mDownloadBG.getMeasuredWidth();
    }

    public void onProgressMessage(long current, long total) {
        Message message = mHandler.obtainMessage();
        message.what = (int) current;
        message.obj = total;
        message.sendToTarget();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what > 0 && msg.obj != null) {
                int current = msg.what;
                int total = Integer.parseInt(msg.obj.toString());

                int mTotalWidth = getDownloadProgressWidth();
                long width = current * mTotalWidth / total;

                setProgress((int) width, (int) Math.floor((width * 100f / mTotalWidth)));
            }
        }
    };

}
