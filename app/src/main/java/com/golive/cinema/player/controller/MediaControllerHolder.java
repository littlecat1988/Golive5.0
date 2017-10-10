package com.golive.cinema.player.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.golive.cinema.R;

/**
 */
public class MediaControllerHolder {

    public View parentLayout; // 父控件
    public ImageView playStatusView;
    public RelativeLayout seekBarLayout;
    public ProgressBar seekBar;
    public TextView playTitle;
    public TextView playTimeCurrent, playTimeTotal;
    //    public TextView playClarityHigh, playClarityStandard, playClaritySuper;
    public RelativeLayout playClarityLayout;
    public int mPauseBtnResId, mStartBtnResId, mForwardResId, mBackwardResId;//播放暂停快进快退
    public View mPlayerBusyView;

    public static MediaControllerHolder getDefaultHolder(Context context) {
        MediaControllerHolder holder = new MediaControllerHolder();
        View controlView = LayoutInflater.from(context).inflate(R.layout.player_control, null);

        holder.parentLayout = controlView;

        holder.mStartBtnResId = R.drawable.media_play;
        holder.mPauseBtnResId = R.drawable.media_pause;
        holder.mForwardResId = R.drawable.media_forward;
        holder.mBackwardResId = R.drawable.media_backward;

        holder.playStatusView = (ImageView) controlView.findViewById(R.id.play_status);
        holder.seekBar = (ProgressBar) controlView.findViewById(R.id.theatre_play_progress);
        holder.seekBarLayout = (RelativeLayout) controlView.findViewById(R.id.seek_bar_parent);
        holder.playTitle = (TextView) controlView.findViewById(R.id.theatre_play_title);
        holder.playTimeCurrent = (TextView) controlView.findViewById(
                R.id.theatre_play_tv_time_current);
        holder.playTimeTotal = (TextView) controlView.findViewById(R.id.theatre_play_tv_time_total);
        holder.playClarityLayout = (RelativeLayout) controlView.findViewById(
                R.id.theatre_play_clarity_layout);
        holder.thumb = (ImageView) controlView.findViewById(R.id.theatre_play_progress_thumb);

//        holder.mPlayerBusyView = controlView.findViewById(R.id.player_busying);
        holder.mPlayerBusyView = LayoutInflater.from(context).inflate(R.layout.player_busying,
                null);
//        holder.playClarityHigh = (TextView) controlView.findViewById(R.id
// .theatre_play_clarity_high);
//        holder.playClarityStandard = (TextView) controlView.findViewById(R.id
// .theatre_play_clarity_standard);
//        holder.playClaritySuper = (TextView) controlView.findViewById(R.id
// .theatre_play_clarity_super);
        return holder;
    }


    public static class Controller {
        private View mView;
        private View mViewFocus;
        private ImageView mIgv;

        public void setVisible(boolean visible) {
            if (mView != null) {
                mView.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }

        public View getView() {
            return mView;
        }

        public void setView(View mView) {
            this.mView = mView;
        }

        public View getViewFocus() {
            return mViewFocus;
        }

        public void setViewFocus(View mViewFocus) {
            this.mViewFocus = mViewFocus;
        }

        public ImageView getIgv() {
            return mIgv;
        }

        public void setIgv(ImageView mIgv) {
            this.mIgv = mIgv;
        }
    }

    /****************************************************************************************************************/
    /****************************************************************************************************************/
    /****************************************************************************************************************/
    /****************************************************************************************************************/
    /****************************************************************************************************************/
    /****************************************************************************************************************/

//    public View parentLayout; // 父控件
    public View nextButton; // 下一个按钮
    public View preButton; // 上一个按钮
    public View forwardButton; // 快进按钮
    public View backwardButton; // 后退按钮
    //    public View likeButton; // 收藏
//    public View imageViewBack; // 反悔按钮
//    public View imageViewShare;// 分享
//    public View mViewPause; // 开始/暂停控件
//    public View mViewPauseFocus; // 开始/暂停控件焦点控件
    public ImageView thumb; // 进度条锚点
    public TextView totalTimeView; // 视频总长度
    public TextView currentTimeView; // 当前播放时间
    //    public TextView tvFilmName; // 视频标题
//    public ImageView mIgvPause; // 开始/暂停图片控件
//    public ImageView stopButton; // 停止按钮
//    public ImageView fullScreenButton; // 全屏按钮
    public ProgressBar seekbar; // 进度条
    //
    private Controller mControllerPause;
    //    private Controller mControllerSharpness;
    private Controller mControllerSubtitle;
    private Controller mControllerSoundtrack;
//
//    /**
//     * 清晰度控件
//     */
//    private ViewGroup mVgSharpness;
//
//    /**
//     * 字幕按钮控件
//     */
//    private View mViewSubtitle;
//    private View mViewSubtitleFocus;
//
//    /**
//     * 音轨按钮控件
//     */
//    private View mViewSoundtrack;
//    private View mViewSoundtrackFocus;
//
//    /**
//     * 字幕控件
//     */
//    private TextView mTvSubtitle;
//
//    /**
//     * 网速控件Vg
//     */
//    private View mVgNetworkSpeed;
//
//    /**
//     * 网速控件
//     */
//    private TextView mTvNetworkSpeed;
//
//    /**
//     * 剩余时间控件
//     */
//    private TextView mTvTimeLeft;
//
//    private View mVgProgress;
//    private TextView mTvProgressLabel;
//    private TextView mTvProgressText;
//    private TextView mTvTips;
//

    public int startResId; // 开始按钮图片资源ID
    public int pauseResId; // 暂停按钮图片资源ID
    //    public int viewBgResId; // 按钮背景资源ID
//    public int viewSelectBgResId; // 按钮选中背景资源ID
    public int fullscreenResId; // 全屏按钮图片资源ID
    public int unfullscreenResId; // 取消全屏按钮图片资源ID
//
//    private boolean hasStopped; // 视频是否已停止，可能是播放结束，也可能是手动停止
//
//    private List<View> views; // 支持一系列自定义的视图，该列表视图实现显示与隐藏
//
//    public static MediaControllerHolder getDefaultHolder(Context context,
//            View viewProgress) {
//        LayoutInflater inflater = LayoutInflater.from(context);
//
//        View view = inflater.inflate(R.layout.player_control, null);
//        // View view = viewRoot.findViewById(R.id.fl_control);
//
//        MediaControllerHolder holder = new MediaControllerHolder();
//        holder.parentLayout = view;
//
//        holder.viewBgResId = R.drawable.media_play_view_bg;
//        holder.viewSelectBgResId = R.drawable.media_play_view_select_bg;
//
//        holder.tvFilmName = (TextView) view
//                .findViewById(R.id.theatre_play_tv_film_name);
//
//        // 播放暂停
//        holder.startResId = R.drawable.play;
//        holder.pauseResId = R.drawable.pause;
//        holder.mControllerPause = new Controller();
//        initController(holder.mControllerPause, view,
//                R.id.theatre_play_control_play_pause);
//
//        // 音轨
//        holder.mControllerSoundtrack = new Controller();
//        initController(holder.mControllerSoundtrack, view,
//                R.id.theatre_play_control_stereo);
//
//        // 字幕
//        holder.mControllerSubtitle = new Controller();
//        initController(holder.mControllerSubtitle, view,
//                R.id.theatre_play_control_subtitle);
//        // 字幕显示控件
//        holder.mTvSubtitle = (TextView) view.findViewById(R.id.subtitleView);
//
//        // 清晰度
//        holder.mVgSharpness = (ViewGroup) view
//                .findViewById(R.id.theatre_play_sharpness_vg);
//
//        // 进度条
//        holder.seekbar = (DragControlSeekBar) view
//                .findViewById(R.id.theatre_play_progress);
//        holder.thumb = view.findViewById(R.id.theatre_play_progress_thumb);
//
//        // 网速vg
//        holder.mVgNetworkSpeed = view.findViewById(R.id.theatre_play_speed_vg);
//
//        // 网速
//        holder.mTvNetworkSpeed = (TextView) view
//                .findViewById(R.id.theatre_play_tv_network_speed);
//
//        // 当前播放时间
//        holder.currentTimeView = (TextView) view
//                .findViewById(R.id.theatre_play_tv_time_current);
//        // 电影总时间
//        holder.totalTimeView = (TextView) view
//                .findViewById(R.id.theatre_play_tv_time_total);
//        // 剩余时间
//        holder.mTvTimeLeft = (TextView) view
//                .findViewById(R.id.theatre_play_tv_time_left);
//
//        if (viewProgress != null) {
//            holder.mVgProgress = viewProgress.findViewById(R.id.ProgressLayout);
//            holder.mTvProgressLabel = (TextView) viewProgress
//                    .findViewById(R.id.ProgressText);
//            holder.mTvProgressText = (TextView) viewProgress
//                    .findViewById(R.id.DownloadProgressText);
//            holder.mTvTips = (TextView) viewProgress.findViewById(R.id.tv_tips);
//            holder.mTvTips.setText(Html.fromHtml(context.getResources().getString(
//                    R.string.player_tips)));
//        }
//
//        holder.mPlayerBusyView = view.findViewById(R.id.player_busying);
//
//        return holder;
//    }
//
//    /**
//     * @param c      TODO
//     * @param viewId TODO
//     */
//    private static Controller initController(Controller c, View view, int viewId) {
//        c.setView(view.findViewById(viewId));
//        c.setViewFocus(c.getView().findViewById(R.id.v_focus));
//        c.setIgv((ImageView) c.getView().findViewById(R.id.igv));
//
//        int resId = 0;
//        View vg = null;
//        TextView tv = null;
//
//        switch (viewId) {
//            case R.id.theatre_play_control_play_pause:
//                c.getIgv().setVisibility(View.VISIBLE);
//                break;
//            case R.id.theatre_play_control_stereo:
//                vg = view.findViewById(R.id.theatre_play_control_stereo);
//                resId = R.string.player_txt_Stereo;
//                break;
//            case R.id.theatre_play_control_subtitle:
//                vg = view.findViewById(R.id.theatre_play_control_subtitle);
//                resId = R.string.player_txt_Subtitle;
//                break;
//
//            default:
//                break;
//        }
//
//        if (vg != null) {
//            tv = (TextView) vg.findViewById(R.id.tv);
//            tv.setText(resId);
//            tv.setVisibility(View.VISIBLE);
//        }
//        return c;
//    }
//
//    public Controller addSharpnessController(Context context, String mediaName,
//            boolean selected) {
//        Controller controller = new Controller();
//        LayoutInflater inflater = LayoutInflater.from(context);
//        View vg = inflater.inflate(R.layout.include_player_ctrol, null);
//        ImageView igv = (ImageView) vg.findViewById(R.id.igv_bg);
//        vg.setTag(igv);
//        TextView textView = (TextView) vg.findViewById(R.id.tv);
//        textView.setText(mediaName);
//        textView.setVisibility(View.VISIBLE);
//        // 选中的资源
//        if (selected) {
//            igv.setImageResource(viewSelectBgResId);
//        }
//        mVgSharpness.addView(vg);
//        View vFocus = vg.findViewById(R.id.v_focus);
//
//        controller.setView(vg);
//        controller.setIgv(igv);
//        controller.setViewFocus(vFocus);
//        return controller;
//    }
//
//    /**
//     * 设置控件的显示状态
//     *
//     * @param visibility 控件是否显示
//     */
//    public void setVisibility(int visibility) {
//        if (parentLayout != null) {
//            parentLayout.setVisibility(visibility);
//        }
//        if (mIgvPause != null) {
//            mIgvPause.setVisibility(visibility);
//        }
//        if (totalTimeView != null) {
//            totalTimeView.setVisibility(visibility);
//        }
//        if (currentTimeView != null) {
//            currentTimeView.setVisibility(visibility);
//        }
//        if (seekbar != null) {
//            seekbar.setVisibility(visibility);
//        }
//        if (tvFilmName != null) {
//            tvFilmName.setVisibility(visibility);
//        }
//        if (fullScreenButton != null) {
//            fullScreenButton.setVisibility(visibility);
//        }
//        if (stopButton != null) {
//            stopButton.setVisibility(visibility);
//        }
//        if (nextButton != null) {
//            nextButton.setVisibility(visibility);
//        }
//        if (preButton != null) {
//            preButton.setVisibility(visibility);
//        }
//        if (views != null) {
//            for (View view : views) {
//                if (view != null) {
//                    view.setVisibility(visibility);
//                }
//            }
//        }
//    }
//
//    public void setPauseButton(ImageButton pauseButton) {
//        this.mIgvPause = pauseButton;
//    }
//
//    public void setTotalTimeView(TextView totalTimeView) {
//        this.totalTimeView = totalTimeView;
//    }
//
//    public void setCurrentTimeView(TextView currentTimeView) {
//        this.currentTimeView = currentTimeView;
//    }
//
//    public void setProgress(DragControlSeekBar seekbar) {
//        this.seekbar = seekbar;
//    }
//
//    public void setFullScreenButton(ImageButton fullScreenButton) {
//        this.fullScreenButton = fullScreenButton;
//    }
//
//    public void setParentLayout(View parentLayout) {
//        this.parentLayout = parentLayout;
//    }
//
//    public void setStopButton(ImageButton stopButton) {
//        this.stopButton = stopButton;
//    }
//
//    public void setNextButton(ImageButton nextButton) {
//        this.nextButton = nextButton;
//    }
//
//    public void setPreButton(ImageButton preButton) {
//        this.preButton = preButton;
//    }
//
//    public void setStartResId(int startResId) {
//        this.startResId = startResId;
//    }
//
//    public void setSeekbar(DragControlSeekBar seekbar) {
//        this.seekbar = seekbar;
//    }
//
//    public void setPauseResId(int pauseResId) {
//        this.pauseResId = pauseResId;
//    }
//
//    public void setHasStopped(boolean hasStopped) {
//        this.hasStopped = hasStopped;
//    }
//
//    public boolean isHasStopped() {
//        return hasStopped;
//    }
//
//    public void addCustomView(View view) {
//        if (views == null) {
//            views = new ArrayList<View>();
//        }
//        views.add(view);
//    }
//
//    public void pauseButtonRequestFocus() {
//        if (mIgvPause != null) {
//            mIgvPause.requestFocus();
//        }
//    }
//
//    /**
//     * 为开始/暂停按钮设置ICON显示
//     *
//     * @param isPlaying 是否正在播放
//     */
//    public void setPauseButtonImage(boolean isPlaying) {
//        if (mIgvPause != null) {
//            if (isPlaying) {
//                mIgvPause.setImageResource(pauseResId);
//            } else {
//                mIgvPause.setImageResource(startResId);
//            }
//        }
//    }
//
//    public void setPauseButtonEnabled(boolean enabled) {
//        if (mIgvPause != null) {
//            mIgvPause.setEnabled(enabled);
//        }
//    }
//
//    public void setSeekbarEnabled(boolean enabled) {
//        if (seekbar != null) {
//            seekbar.setEnabled(enabled);
//        }
//    }
//
//    public void setOnFullScreenListener(View.OnClickListener l) {
//        if (fullScreenButton != null) {
//            fullScreenButton.setOnClickListener(l);
//        }
//    }
//
//    public void setOnPauseListener(View.OnClickListener l) {
//        if (mIgvPause != null) {
//            mIgvPause.setOnClickListener(l);
//        }
//    }
//
//    public void setOnStopListener(View.OnClickListener l) {
//        if (stopButton != null) {
//            stopButton.setOnClickListener(l);
//        }
//    }
//
//    public void setOnPreListener(View.OnClickListener l) {
//        if (preButton != null) {
//            preButton.setOnClickListener(l);
//        }
//    }
//
//    public void setOnNextListener(View.OnClickListener l) {
//        if (nextButton != null) {
//            nextButton.setOnClickListener(l);
//        }
//    }

    public Controller getControllerPause() {
        return mControllerPause;
    }

//    public void setControllerPause(Controller mControllerPause) {
//        this.mControllerPause = mControllerPause;
//    }
//
//    public Controller getControllerSharpness() {
//        return mControllerSharpness;
//    }
//
//    public void setControllerSharpness(Controller mControllerSharpness) {
//        this.mControllerSharpness = mControllerSharpness;
//    }

    public Controller getControllerSubtitle() {
        return mControllerSubtitle;
    }

//    public void setControllerSubtitle(Controller mControllerSubtitle) {
//        this.mControllerSubtitle = mControllerSubtitle;
//    }

    public Controller getControllerSoundtrack() {
        return mControllerSoundtrack;
    }

    //    public void setmControllerSoundtrack(Controller mControllerSoundtrack) {
//        this.mControllerSoundtrack = mControllerSoundtrack;
//    }
//
//    public void setNetworkSpeedVisible(boolean visible) {
//        View networkSpeed = mVgNetworkSpeed;
//        if (networkSpeed != null) {
//            networkSpeed.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
//        }
//    }
//
//    public boolean isNetworkSpeedVisible() {
//        View networkSpeed = mVgNetworkSpeed;
//        if (networkSpeed != null) {
//            return View.VISIBLE == networkSpeed.getVisibility();
//        }
//        return false;
//    }
//
//
//
//    public TextView getTimeLeft() {
//        return mTvTimeLeft;
//    }
//
//    public void setTimeLeft(TextView mTvTimeLeft) {
//        this.mTvTimeLeft = mTvTimeLeft;
//    }
//
//    public TextView getProgressLabel() {
//        return mTvProgressLabel;
//    }
//
//    public void setProgressLabel(TextView progressLabel) {
//        mTvProgressLabel = progressLabel;
//    }
//
//    public TextView getProgressText() {
//        return mTvProgressText;
//    }
//
//    public void setProgressText(TextView progressText) {
//        mTvProgressText = progressText;
//    }
//
//    public TextView getTips() {
//        return mTvTips;
//    }
//
//    public void setTips(TextView tvTips) {
//        mTvTips = tvTips;
//    }
//
//    public View getVgProgress() {
//        return mVgProgress;
//    }
//
//    public void setVgProgress(View mVgProgress) {
//        this.mVgProgress = mVgProgress;
//    }
//
//    public TextView getFilmName() {
//        return tvFilmName;
//    }
//
//    public void setFilmName(TextView tvFilmName) {
//        this.tvFilmName = tvFilmName;
//    }
//
//    public ViewGroup getVgSharpness() {
//        return mVgSharpness;
//    }
//
//    public void setVgSharpness(ViewGroup mVgSharpness) {
//        this.mVgSharpness = mVgSharpness;
//    }
//
    public View getPlayerBusyView() {
        return mPlayerBusyView;
    }

//    public TextView getNetworkSpeed() {
//        return mTvNetworkSpeed;
//    }

}
