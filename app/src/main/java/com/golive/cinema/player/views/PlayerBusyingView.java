package com.golive.cinema.player.views;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.golive.cinema.R;
import com.golive.cinema.util.NetworkUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.UIHelper;
import com.initialjie.log.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by chgang on 2016/12/22.
 */

public class PlayerBusyingView extends FrameLayout {
    private static final int PLAYER_BUSYING_STATUS_VISIBLE_UNSPEED = 0;//不需要获取本地网速
    private static final int PLAYER_BUSYING_STATUS_VISIBLE_GET_SPEED = 1;//需要获取
    private static final int PLAYER_BUSYING_STATUS_INVISIBLE_ALL = 2;//隐藏
    private static final int CHECK_NET_SPEED_TIME = 500;

    private TextView mTvNetworkSpeed;
    //    private ImageView mSpeedLoadingView;
    private Context mContext;

    private long mOldBytes;
    private long mTimeStamp;
    private String mLocalSpeed;
    private ScheduledExecutorService mUpdateNetWorkTask;

    public String getLocalSpeed() {
        return mLocalSpeed;
    }

    private void setLocalSpeed(String localSpeed) {
        this.mLocalSpeed = localSpeed;
    }

    public PlayerBusyingView(Context context) {
        this(context, null);
    }

    public PlayerBusyingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlayerBusyingView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private void init(Context context) {
        this.mContext = context;
        getSpeedProgressText();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTvNetworkSpeed = (TextView) this.findViewById(R.id.theatre_play_tv_network_speed);
        ImageView mSpeedLoadingView = (ImageView) this.findViewById(
                R.id.theatre_play_network_speed_bar);
        AnimationDrawable speedLoadingViewDrawable =
                (AnimationDrawable) mSpeedLoadingView.getDrawable();
        speedLoadingViewDrawable.start();
        getSpeedProgressText();
        this.setVisibility(INVISIBLE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
        stopSpeedProgressText();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PLAYER_BUSYING_STATUS_VISIBLE_UNSPEED:
                case PLAYER_BUSYING_STATUS_VISIBLE_GET_SPEED:
                    if (msg.obj != null) {
                        mTvNetworkSpeed.setText(msg.obj.toString());
                    }
                    UIHelper.setViewVisibleOrGone(mTvNetworkSpeed,
                            msg.obj != null && !StringUtils.isNullOrEmpty(msg.obj.toString()));
                    if (getVisibility() != VISIBLE) {
                        setVisibility(VISIBLE);
                    }
                    break;

                case PLAYER_BUSYING_STATUS_INVISIBLE_ALL:
                    UIHelper.setViewVisibleOrGone(mTvNetworkSpeed, false);
                    if (getVisibility() == VISIBLE) {
                        setVisibility(INVISIBLE);
                    }
                    break;

                default:
                    break;
            }
        }
    };

    public void sendMessage(boolean visible, int progress, String speed, boolean isBufferSupport) {
        Logger.d("sendMessage, visible : " + visible + ", progress : " + progress
                + ", speed : " + speed + ", isBufferSupport :" + isBufferSupport);
        Message message = mHandler.obtainMessage();
        if (!visible || progress >= 100) {
            message.what = PLAYER_BUSYING_STATUS_INVISIBLE_ALL;
        } else {
            if (!StringUtils.isNullOrEmpty(speed) && isBufferSupport) {
                message.what = PLAYER_BUSYING_STATUS_VISIBLE_UNSPEED;
                message.obj = speed;
            } else {
                message.what = PLAYER_BUSYING_STATUS_VISIBLE_GET_SPEED;
                message.obj = getLocalSpeed();
            }
        }

        message.sendToTarget();
    }

    public void stopSpeedProgressText() {
        if (mUpdateNetWorkTask != null) {
            mUpdateNetWorkTask.shutdownNow();
            mUpdateNetWorkTask = null;
        }
    }

    private void getSpeedProgressText() {
        if (mUpdateNetWorkTask == null || mUpdateNetWorkTask.isShutdown()) {
            mUpdateNetWorkTask = Executors.newSingleThreadScheduledExecutor();
            mUpdateNetWorkTask.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    long speed = checkSpeed();
                    final StringBuilder sb = new StringBuilder();
                    sb.setLength(0);
                    sb.append(android.text.format.Formatter.formatFileSize(mContext, speed));
                    sb.append("/S");

//                    Logger.d("sb:" + sb.toString());
                    setLocalSpeed(sb.toString());
                }
            }, 0, CHECK_NET_SPEED_TIME, TimeUnit.MILLISECONDS);
        }
    }

    private long checkSpeed() {
        long netSpeed = 0;
        long now = System.currentTimeMillis();
        long currBytesDown = 0;

        long newRxBytes = NetworkUtils.getTotalDataBytes(true);
        long newBytes = newRxBytes - mOldBytes;
        mOldBytes = newRxBytes;

        if (0 != mTimeStamp) {
            if (currBytesDown == TrafficStats.UNSUPPORTED) {
            } else {
                long byteSpan = 0;
                // byteSpan = currBytesDown - bytesDown;
                byteSpan = newBytes;
                long timeSpan = now - mTimeStamp;
                if (timeSpan != 0) {
                    netSpeed = (long) Math.ceil(1000.0 * byteSpan / timeSpan);
                }
            }
        }

        mTimeStamp = now;
        return netSpeed;
    }
}
