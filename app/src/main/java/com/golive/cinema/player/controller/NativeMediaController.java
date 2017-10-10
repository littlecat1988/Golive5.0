package com.golive.cinema.player.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.golive.cinema.Constants;
import com.golive.cinema.R;
import com.golive.cinema.player.BasePlayerFragment;
import com.golive.cinema.player.IUserActionListener;
import com.golive.cinema.player.PlayerOperation;
import com.golive.cinema.player.PlayerState;
import com.golive.cinema.player.kdm.KdmPlayerFragment;
import com.golive.cinema.player.views.PlayerBusyingView;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.ToastUtils;
import com.golive.cinema.views.DragControlSeekBar;
import com.initialjie.log.Logger;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

/**
 * 自定义MediaController，用于原生的MediaPlayer，具有支持自定义UI的特性。 使用方法： 实现MediaControllerGenerator接口，该接口包含一个方法：
 * {@link MediaControllerGenerator#generateMediaController()}
 * 该方法从自定义的xml布局文件生成控制面板的布局，并且生成控制面板中的控件集合。
 */
public class NativeMediaController extends FrameLayout implements View.OnClickListener,
        View.OnFocusChangeListener {
//    private static final String TAG = NativeMediaController.class
//            .getSimpleName();

    private static final int DEFAULT_TIMEOUT = 3000;
    private static final int SHOW_FORWARD_BACKWARD_DELAY = 300;
    private static final int FADE_OUT = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int SHOW_FORWARD_BACKWARD = 3;
    private static final float THUMB_PRO = 5.0f;

    /** 快进快退值 */
    private static final int SPEED = 5;

    private final Context mContext;

    /**
     * 播放器操作接口
     */
    private PlayerOperation mPlayerOperation;

    /**
     * 用户操作监听
     */
    private IUserActionListener mUserActionListener;

    /**
     * 控制界面rootView
     */
    private View mRoot;
    private ImageView mThumb; // 进度条锚点
    private TextView mEndTime, mCurrentTime;
    private DragControlSeekBar mProgress;
    private ViewGroup mAnchor;
    private RelativeLayout mPlayClarityLayout;
    private TextView mPlayClarityHigh, mPlayClarityStandard, mPlayClaritySuper;
    private View mPlayerBusyView;
//    public TextView mTvNetworkSpeed;
//    public ImageView mSpeedLoadingView;
//    private JumpingBeans mJumpingBeans;

    private Formatter mFormatter;
    private StringBuilder mFormatBuilder;
    private final Handler mHandler = new MessageHandler(this);
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private boolean mShowing;
    private boolean mDragging;
    private boolean mIsKdmPlayer = false;

    private final boolean mUseFastForward;
    private boolean mFromXml;

    private float mPressPoint;
    private boolean mIsLongPress;
    private final Handler mThumbHandler = new Handler();
    private boolean mIsForwardBackwarding;

//    private View mFfwdButton;
//    private View mRewButton;
//    private View mNextButton;
//    private View mPrevButton;
//    private View mParent;
//    private ImageView mPauseButton;
//    private ImageView mFullscreenButton;
//    private String mLastJumpingBeansText; // 上一次缓冲时的提示字符串(JumpingBeans)

    //    private int mPauseBtnResId, mStartBtnResId, mFullScreenResId, mUnFullScreenResId;

    /**
     * 控件界面布局id
     */
//    private int mCtrlViewLayoutID = 0;
//    private boolean mListenersSet;
//    private OnClickListener mNextListener, mPrevListener;
    // private MediaPlayerControl mPlayer;
//    private int[] location = new int[2];

    /**
     * 公共接口：自定义控制条布局生成
     */
    public interface MediaControllerGenerator {
        /**
         * 从布局文件生成一个控制条的自定义布局
         *
         * @return BaseMediaControllerHolder对象，控制条控件的集合
         */
        MediaControllerHolder generateMediaController();
    }

    private MediaControllerGenerator mUIGenerator;

    private MediaControllerHolder mHolder;

    private boolean mTouchMode = true; // 是否触屏模式

//    private MediaControllerHolder.Controller mControllerPause;
//    private MediaControllerHolder.Controller mControllerSharpness;
//    private MediaControllerHolder.Controller mControllerSoundtrack;
//    private MediaControllerHolder.Controller mControllerSubtitle;

    public void setUIGenerator(MediaControllerGenerator generator) {
        this.mUIGenerator = generator;
    }

    public NativeMediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRoot = null;
        mContext = context;
        mUseFastForward = true;
        mFromXml = true;
    }

    public NativeMediaController(Context context, boolean useFastForward) {
        super(context);
        mContext = context;
        mUseFastForward = useFastForward;
    }

    public NativeMediaController(Context context, BasePlayerFragment baseFragment) {
        this(context, true);
        if (baseFragment != null && baseFragment instanceof KdmPlayerFragment) {
            mIsKdmPlayer = true;
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        if (mRoot != null) {
            initControllerView(mRoot);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
        mThumbHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Set the view that acts as the anchor for the control view. This can for example be a
     * VideoView, or your Activity's main view.
     *
     * @param view The view to which to anchor the controller when it is visible.
     */
    public void setAnchorView(ViewGroup view) {
        mAnchor = view;

        LayoutParams frameParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);
    }

    /**
     * Create the view that holds the widgets that control playback. Derived classes can override
     * this to create their own.
     *
     * @return The controller view.
     */
    protected View makeControllerView() {
        if (mHolder != null) {
            initControllerView(mHolder);
        } else {
            LayoutInflater inflate = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mRoot = inflate.inflate(R.layout.video_native_media_controller,
                    null);
            initControllerView(mRoot);
        }

        return mRoot;
    }

    private void initControllerView(MediaControllerHolder holder) {

        setTouchMode(false);

        mHolder = holder;
        mRoot = holder.parentLayout;
        // 当前播放时间
        mCurrentTime = holder.playTimeCurrent;
        // 电影总时间
        mEndTime = holder.playTimeTotal;
        // 网速
        mPlayerBusyView = holder.mPlayerBusyView;

        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mThumb = holder.thumb;

        // 播放进度条
        mProgress = (DragControlSeekBar) holder.seekBar;
        if (mProgress != null) {
            if (mProgress instanceof SeekBar) {
                SeekBar seeker = mProgress;
                seeker.setOnSeekBarChangeListener(mSeekListener);
            }
            mProgress.setMax(1000);
            setViewFocusAble(mProgress, true);
            mProgress.setOnKeyCallback(progressKeyCallback);
        }

        //清晰度 高/标准/超高
        initPlayClarity();

        //焦点控制
//        mProgress.setNextFocusUpId(mPlayClarityStandard.getId());
        if (mPlayClarityHigh != null) {
            mPlayClarityHigh.setNextFocusDownId(mProgress.getId());
        }
        if (mPlayClarityStandard != null) {
            mPlayClarityStandard.setNextFocusDownId(mProgress.getId());
        }

        if (mPlayClaritySuper != null) {
            mPlayClaritySuper.setNextFocusDownId(mProgress.getId());
        }

        // 显示
//        show();

        // add busy view
        LayoutParams tlp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        mAnchor.addView(mPlayerBusyView, tlp);
    }

    private void initPlayClarity() {
        mPlayClarityLayout = mHolder.playClarityLayout;
        if (mPlayClarityLayout != null && mPlayerOperation != null) {
            mPlayClarityLayout.removeAllViews();
            List<String> rankList = mPlayerOperation.getRanks();
            if (rankList != null && rankList.size() > 0) {
                String rank1 = null;
                String rank2 = null;
                String rank3 = null;
                int size = rankList.size();
                int playClarity = mPlayerOperation.getRank();//正在播的清晰度值
                for (int i = 0; i < size; i++) {
                    String rank = rankList.get(i);
                    if (!StringUtils.isNullOrEmpty(rank)
                            && playClarity == Integer.parseInt(rank)) {
                        rank1 = rank;
                        addClarityView(rank1,
                                (int) getResources().getDimension(
                                        R.dimen.player_clarity_button_middle_margin_right),
                                View.VISIBLE);
                    } else {
                        if (StringUtils.isNullOrEmpty(rank2)) {
                            rank2 = rank;
                        } else {
                            rank3 = rank;
                        }
                    }
                }

                if (!StringUtils.isNullOrEmpty(rank1)) {
                    if (!StringUtils.isNullOrEmpty(rank2)) {
                        addClarityView(rank2,
                                (int) getResources().getDimension(
                                        R.dimen.player_clarity_button_left_margin_right),
                                View.GONE);
                        if (!StringUtils.isNullOrEmpty(rank3)) {
                            addClarityView(rank3,
                                    (int) getResources().getDimension(
                                            R.dimen.player_clarity_button_right_margin_right),
                                    View.GONE);
                        }
                    }
                } else {
                    if (!StringUtils.isNullOrEmpty(rank2)) {
                        addClarityView(rank2,
                                (int) getResources().getDimension(
                                        R.dimen.player_clarity_button_middle_margin_right),
                                View.VISIBLE);
                        if (!StringUtils.isNullOrEmpty(rank3)) {
                            addClarityView(rank3,
                                    (int) getResources().getDimension(
                                            R.dimen.player_clarity_button_left_margin_right),
                                    View.GONE);
                        }
                    }
                }
                mPlayClarityLayout.setVisibility(View.VISIBLE);
                setPlayClarityViews();
            } else {
                mPlayClarityLayout.setVisibility(View.GONE);
            }


//            List<Media> mediaList = mPlayerOperation.getMediaList();
//            if (mediaList != null && mediaList.size() > 0) {
//                Media media1 = null;
//                Media media2 = null;
//                Media media3 = null;
//                int size = mediaList.size();
////                int defaultClarity = UserInfoHelper.getDefaultDefinition(mContext);//用户默认设置的清晰度值
//                int playClarity = mPlayerOperation.getRank();//正在播的清晰度值
//                for (int i = 0; i < size; i++) {
//                    Media media = mediaList.get(i);
//                    if (playClarity == Integer.parseInt(media.getRank())) {
//                        media1 = media;
//                        addClarityView(media1.getRank(), media1.getRankname(),
//                                (int) getResources().getDimension(
//                                        R.dimen.player_clarity_button_middle_margin_right),
//                                View.VISIBLE);
//                    } else {
//                        if (media2 == null) {
//                            media2 = media;
//                        } else {
//                            media3 = media;
//                        }
//                    }
//                }
//
//                if (media1 != null) {
//                    if (media2 != null) {
//                        addClarityView(media2.getRank(), media2.getRankname(),
//                                (int) getResources().getDimension(
//                                        R.dimen.player_clarity_button_left_margin_right),
//                                View.GONE);
//                        if (media3 != null) {
//                            addClarityView(media3.getRank(), media3.getRankname(),
//                                    (int) getResources().getDimension(
//                                            R.dimen.player_clarity_button_right_margin_right),
//                                    View.GONE);
//                        }
//                    }
//                } else {
//                    if (media2 != null) {
//                        addClarityView(media2.getRank(), media2.getRankname(),
//                                (int) getResources().getDimension(
//                                        R.dimen.player_clarity_button_middle_margin_right),
//                                View.VISIBLE);
//                        if (media3 != null) {
//                            addClarityView(media3.getRank(), media3.getRankname(),
//                                    (int) getResources().getDimension(
//                                            R.dimen.player_clarity_button_left_margin_right),
//                                    View.GONE);
//                        }
//                    }
//                }
//                mPlayClarityLayout.setVisibility(View.VISIBLE);
//                setPlayClarityViews();
//            } else {
//                mPlayClarityLayout.setVisibility(View.GONE);
//            }
        } else {
            if (mPlayClarityLayout != null) {
                mPlayClarityLayout.setVisibility(View.GONE);
            }
        }
    }

    private void addClarityView(String rank, int rightM, int visibility) {
        TextView textView = null;
        if (mPlayClaritySuper != null && mPlayClaritySuper.getTag() != null
                && rank.equals(mPlayClaritySuper.getTag())) {
            textView = mPlayClaritySuper;
        } else if (mPlayClarityHigh != null && mPlayClarityHigh.getTag() != null
                && rank.equals(mPlayClarityHigh.getTag().toString())) {
            textView = mPlayClarityHigh;
        } else if (mPlayClarityStandard != null && mPlayClarityStandard.getTag() != null
                && rank.equals(mPlayClarityStandard.getTag().toString())) {
            textView = mPlayClarityStandard;
        }

        if (null == textView) {
            textView = new TextView(mContext);
            textView.setTag(rank);
            textView.setText(getRankName(rank));
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(getResources().getColor(R.color.movie_name_color));
            textView.setTextSize(
                    getResources().getDimension(R.dimen.player_clarity_button_text_size));
            textView.setBackgroundResource(R.drawable.play_clarity_button_selector);
        }

        if (visibility == View.VISIBLE) {
            textView.setSelected(true);
        } else {
            textView.setSelected(false);
        }
        textView.setVisibility(visibility);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.player_clarity_button_width),
                (int) getResources().getDimension(R.dimen.player_clarity_button_height)
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.rightMargin = rightM;

        mPlayClarityLayout.addView(textView, params);
        setViewFocusAble(textView, true);
    }

    private String getRankName(String rank) {
        if (!StringUtils.isNullOrEmpty(rank)) {
            switch (rank) {
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

    private void setViewFocusAble(View view, boolean focus) {
        if (view != null) {
            view.setClickable(focus);
            view.setFocusable(focus);
            view.setFocusableInTouchMode(focus);
            view.setOnClickListener(this);
            view.setOnFocusChangeListener(this);
        }
    }

    private void setPlayClarityViews() {
        if (mPlayClarityLayout != null && mPlayClarityLayout.getChildCount() > 0) {
            for (int i = 0; i < mPlayClarityLayout.getChildCount(); i++) {
                TextView textView = (TextView) mPlayClarityLayout.getChildAt(i);
                String tag = textView.getTag().toString();
                if (!StringUtils.isNullOrEmpty(tag)) {
                    switch (tag) {
                        case Constants.PLAY_MEDIA_RANK_CLARITY_HIGH:
                            mPlayClarityHigh = textView;
                            break;
                        case Constants.PLAY_MEDIA_RANK_CLARITY_STANDARD:
                            mPlayClarityStandard = textView;
                            break;
                        case Constants.PLAY_MEDIA_RANK_CLARITY_SUPER:
                            mPlayClaritySuper = textView;
                            break;
                    }
                }
            }
        }
    }

    private static long lastClickTime = 0;
    private final static int SPACE_TIME = 500;

    private synchronized static boolean isNextClick() {
        long currentTime = System.currentTimeMillis();
        boolean isClick2;
        isClick2 = currentTime - lastClickTime <= SPACE_TIME;
        lastClickTime = currentTime;
        return isClick2;
    }

    /**
     * 切换分辨率
     */
    @Override
    public void onClick(View v) {
        if (v == mProgress) {
            if (isShowing() && !isNextClick()) {
                doPauseResume(true);
            } else {
                show();
            }
        } else if (v == mPlayClarityHigh || v == mPlayClarityStandard || v == mPlayClaritySuper) {
            if (null == v.getTag()) {
                return;
            }
            String tag = v.getTag().toString();
            if (mPlayerOperation != null && Integer.parseInt(tag) == mPlayerOperation.getRank()) {
                ToastUtils.showToast(mContext,
                        mContext.getString(R.string.theatre_play_clarity_selected));
                return;
            }

//            showPlayerBuffering(true, 100, null, false);
            hidePlayerBufferingView();
            boolean flash = mPlayerOperation.changeRankRestart(tag);
            if (flash) {
                initPlayClarity();
                mProgress.requestFocus();
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v == mProgress) {
            RelativeLayout.LayoutParams params;
            if (hasFocus) {
                params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        (int) getResources().getDimension(
                                R.dimen.player_progress_bar_height_focus));
            } else {
                params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        (int) getResources().getDimension(
                                R.dimen.player_progress_bar_height));
            }
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            mProgress.setPadding(0, 0, 0, 0);
            mProgress.setLayoutParams(params);
        } else if (v == mPlayClarityHigh || v == mPlayClarityStandard || v == mPlayClaritySuper) {
            if (mPlayClarityLayout != null && mPlayClarityLayout.getChildCount() > 0) {
                int size = mPlayClarityLayout.getChildCount();
                TextView textView;
                for (int i = 0; i < size; i++) {
                    textView = (TextView) mPlayClarityLayout.getChildAt(i);
                    if (hasFocus || textView.isFocused() || textView.isSelected()) {
                        textView.setVisibility(View.VISIBLE);
                    } else {
                        textView.setVisibility(View.GONE);
                    }
                }
            }
        }
        show();
    }

    /**
     * 快速拖动监听
     */
    private final DragControlSeekBar.OnKeyCallback progressKeyCallback =
            new DragControlSeekBar.OnKeyCallback() {

                private boolean longPressed;

                @Override
                public boolean getLongPressed() {
                    return longPressed;
                }

                @Override
                public void setLongPressed(boolean longPressed) {
                    this.longPressed = longPressed;
                }

                @Override
                public void doForward(int keyCode) {
                    if (!mIsKdmPlayer) {
                        onForwardOrBackward(false);
                    } else {
                        ToastUtils.showToast(mContext,
                                mContext.getString(R.string.theatre_play_current_kdm_text));
                    }
                }

                @Override
                public void doBackward(int keyCode) {
                    if (!mIsKdmPlayer) {
                        onForwardOrBackward(true);
                    } else {
                        ToastUtils.showToast(mContext,
                                mContext.getString(R.string.theatre_play_current_kdm_text));
                    }
                }

                @Override
                public void onStartTrackingTouch() {
                    if (!mIsKdmPlayer) {
                        NativeMediaController.this.onStartTrackingTouch();
                    }
                }

                @Override
                public void onStopTrackingTouch() {
                    if (!mIsKdmPlayer) {
                        NativeMediaController.this.onStopTrackingTouch();
                    }
                }

                @Override
                public void onLongPressStart(int keyCode) {
                    if (!mIsKdmPlayer) {
                        NativeMediaController.this.onLongPressStart(keyCode);
                    } else {
                        ToastUtils.showToast(mContext,
                                mContext.getString(R.string.theatre_play_current_kdm_text));
                    }
                }

                @Override
                public void onLongPressing(int keyCode) {
                    if (!mIsKdmPlayer) {
                        NativeMediaController.this.onLongPressing(keyCode);
                    }
                }

                @Override
                public void onLongPressStop(int keyCode) {
                    if (!mIsKdmPlayer) {
                        NativeMediaController.this.onLongPressStop(keyCode);
                    }
                }

            };

    private void onStartTrackingTouch() {
        //        show();
        mHandler.removeMessages(FADE_OUT);
        mHandler.removeMessages(SHOW_PROGRESS);
    }

    private void onStopTrackingTouch() {
        updateProgress();
        updatePausePlay();
        show();

        // Ensure that progress is properly updated in the future,
        // the call to show() does not guarantee this because it is a
        // no-op if we are already showing.
        requestUpdateProgress();
    }

    private void onForwardOrBackward(boolean backWard) {
        if (null == mPlayerOperation) {
            return;
        }

        int duration = mPlayerOperation.getDuration();
        float curWidth = 1f * mProgress.getProgress() / mProgress.getMax() * mProgress.getWidth();
        int speed = backWard ? -SPEED : SPEED;
        float pos = 1f * (curWidth + speed) / mProgress.getWidth() * duration;
        if (pos <= duration) {
            updateForwardOrBackward(backWard, false);
            mPlayerOperation.setToType(backWard ? "1" : "0");
            mPlayerOperation.seekTo((int) pos);
            updateProgress();
        }
    }

    private void onLongPressStart(int keyCode) {
        mIsLongPress = true;
        boolean backward = KeyEvent.KEYCODE_DPAD_LEFT == keyCode;
        updateForwardOrBackward(backward, true);
        int progress = mProgress.getProgress();
        int max = mProgress.getMax();
        mPressPoint = 0 == max ? 0 : 1f * progress / max * mProgress.getWidth();
    }

    private void onLongPressing(final int keyCode) {
        if (null == mPlayerOperation) {
            return;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            mThumbHandler.post(new Runnable() {
                @Override
                public void run() {
                    float progress =
                            1f * mThumb.getWidth() / mProgress.getWidth() * mProgress.getMax();
                    int total = mPlayerOperation.getDuration();
                    int position = 0;
                    MarginLayoutParams thumbParams = (MarginLayoutParams) mThumb.getLayoutParams();
                    if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        position = mPlayerOperation.getCurrentPosition() + (int) progress;
                        thumbParams.leftMargin = (int) mPressPoint;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        position = mPlayerOperation.getCurrentPosition() - (int) progress;
                        thumbParams.leftMargin = (int) mPressPoint - thumbParams.width;
                    }

                    if (mCurrentTime != null && position <= total) {
                        mCurrentTime.setText(stringForTime(position));
                    }

                    thumbParams.width += THUMB_PRO;
                    mThumb.setLayoutParams(thumbParams);
                    mThumbHandler.postDelayed(this, 50);
                }
            });
        }
    }

    private synchronized void onLongPressStop(final int keyCode) {
        if (null == mPlayerOperation) {
            return;
        }

        mThumbHandler.removeCallbacksAndMessages(null);
        mHolder.playStatusView.setImageResource(mHolder.mStartBtnResId);

        MarginLayoutParams thumbParams = (MarginLayoutParams) mThumb.getLayoutParams();
//        final float progress = 1f * mThumb.getWidth() / mProgress.getWidth() * mProgress.getMax();
        float progress = 0;
        int pos = 0;
        int thumbLeft = 0;
        if (mPlayerOperation != null) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                progress = thumbParams.leftMargin;
                thumbLeft = thumbParams.leftMargin - thumbParams.width;
//                pos = mPlayerOperation.getCurrentPosition() - (int) progress;
                mPlayerOperation.setToType("1");
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                progress = mThumb.getWidth() + thumbParams.leftMargin;
                thumbLeft = thumbParams.leftMargin + thumbParams.width;
//                pos = mPlayerOperation.getCurrentPosition() + (int) progress;
                mPlayerOperation.setToType("0");
            }
            pos = (int) (1f * progress / mProgress.getWidth() * mProgress.getMax());
            mPlayerOperation.seekTo(pos);
        }
        Logger.d("onLongPressStop---------pos:" + pos);

        thumbParams.leftMargin = thumbLeft;
        thumbParams.width = 0;
        mThumb.setLayoutParams(thumbParams);

        onStopTrackingTouch();
        mIsLongPress = false;
    }

    private void updateForwardOrBackward(boolean backward, boolean longPress) {
        int resId = backward ? mHolder.mBackwardResId : mHolder.mForwardResId;
        mHolder.playStatusView.setImageResource(resId);
        // not long press
        if (!longPress) {
            setForwardBackwarding(true);
            mHandler.removeMessages(SHOW_FORWARD_BACKWARD);
            mHandler.sendEmptyMessageDelayed(SHOW_FORWARD_BACKWARD, SHOW_FORWARD_BACKWARD_DELAY);
        }
    }

    /**
     * Show the controller on screen. It will go away automatically after 3 seconds of inactivity.
     */
    public void show() {
        show(DEFAULT_TIMEOUT);
    }

    /**
     * Show the controller on screen. It will go away automatically after 'timeout' milliseconds of
     * inactivity.
     *
     * @param timeout The timeout in milliseconds. Use 0 to show the controller until hide() is
     *                called.
     */
    public void show(int timeout) {
        mHandler.removeMessages(FADE_OUT);

        if (!mShowing && mAnchor != null) {
//            disableUnsupportedButtons();
            updateProgress();
//            drawThumb();
//            if (isTouchMode()) {
//                if (mPauseButton != null) {
//                    mPauseButton.requestFocus();
//                }
//            } else {
//                if (mControllerPause != null) {
//                    View view = mControllerPause.getView();
//                    view.requestFocus();
//                    view.requestFocusFromTouch();
//                }
//            }

//            updatePausePlay();
////            updateFullScreen();
//
//            // cause the progress bar to be updated even if mShowing
//            // was already true. This happens, for example, if we're
//            // paused with the progress bar showing the user hits play.
//            requestUpdateProgress();

            LayoutParams tlp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, Gravity.BOTTOM);

            mAnchor.addView(this, tlp);
            mShowing = true;

            mProgress.requestFocus();
        }

        updatePausePlay();
//            updateFullScreen();

        // cause the progress bar to be updated even if mShowing
        // was already true. This happens, for example, if we're
        // paused with the progress bar showing the user hits play.
        requestUpdateProgress();

        if (timeout != 0) {
            mHandler.removeMessages(FADE_OUT);
            mHandler.sendEmptyMessageDelayed(FADE_OUT, timeout);
        }
    }

    /**
     * 请求更新进度
     */
    public void requestUpdateProgress() {
        mHandler.removeMessages(SHOW_PROGRESS);
        mHandler.sendEmptyMessage(SHOW_PROGRESS);
    }

    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Remove the controller from the screen.
     */
    public void hide() {
        if (null == mAnchor) {
            return;
        }

        try {
            // setProgressVisible(false);
            mAnchor.removeView(this);
            mHandler.removeMessages(SHOW_PROGRESS);
        } catch (IllegalArgumentException ex) {
            Logger.w("MediaController", "already removed");
        }
        mShowing = false;
    }

    private String stringForTime(int timeMs) {
        if (timeMs <= 0) {
            timeMs = 0;
        }

        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
//        if (hours > 0) {
//            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
//        } else {
//            return mFormatter.format("00:%02d:%02d", minutes, seconds).toString();
//        }
        return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
    }

    private synchronized int updateProgress() {
        if (null == mPlayerOperation || mDragging || mIsLongPress) {
            return 0;
        }

        int position = mPlayerOperation.getCurrentPosition();
        int duration = mPlayerOperation.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                //long pos = 1000L * position / duration;
//                Logger.d("updatePlayerProgress, position : " + position
//                        + ", duration : " + duration + ", pos : " + position);
                mProgress.setProgress(position);
                mProgress.setMax(duration);
            }
            int percent = mPlayerOperation.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
        }

        if (mEndTime != null) {
            mEndTime.setText(stringForTime(duration));
        }
        if (mCurrentTime != null) {
            mCurrentTime.setText(stringForTime(position));
        }
//        if (mTvNetworkSpeed != null
//                && View.VISIBLE == mTvNetworkSpeed.getVisibility()) {
//            int networkSpeed = mPlayerOperation.getNetworkSpeed();
//            String speedStr = null;
//            if (networkSpeed >= 0) {
//                speedStr = android.text.format.Formatter.formatFileSize(
//                        mContext, networkSpeed);
//            }
//            mTvNetworkSpeed.setText(speedStr);
//        }

//        int millionSeconds = 0;
//        if (0 < duration) {
//            millionSeconds = duration - position;
//        } else if (0 < mPlayerOperation.getFakeDuration()) {
//            millionSeconds = mPlayerOperation.getFakeDuration() - position;
//        }
//        if (isTouchMode()) {
//        } else {
//            mHolder.getTimeLeft().setText(stringForTime(millionSeconds));
//        }

        return position;
    }

    public boolean isTouchMode() {
        return mTouchMode;
    }

    public void setTouchMode(boolean mTouchMode) {
        this.mTouchMode = mTouchMode;
    }

    private final OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar bar) {
            show(0);

            mDragging = true;

            // By removing these pending progress messages we make sure
            // that a) we won't update the progress while the user adjusts
            // the seekbar and b) once the user is done dragging the thumb
            // we will post one of these messages to the queue again and
            // this ensures that there will be exactly one message queued up.
            mHandler.removeMessages(SHOW_PROGRESS);

            if (getOnSeekBarChangeListener() != null) {
                getOnSeekBarChangeListener().onStartTrackingTouch(bar);
            }
        }

        @Override
        public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
            if (null == mPlayerOperation) {
                return;
            }

//            drawThumb();

            if (fromUser && progress >= 0) {
                // We're not interested in programmatically generated changes to
                // the progress bar's position.

                show();
                mPlayerOperation.setToType("");
                mPlayerOperation.seekTo(progress);
                if (mCurrentTime != null) {
                    mCurrentTime.setText(stringForTime(progress));
                }
            }

            if (getOnSeekBarChangeListener() != null) {
                getOnSeekBarChangeListener().onProgressChanged(bar, progress, fromUser);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar bar) {
            mDragging = false;
            updateProgress();
            show();

            requestUpdateProgress();

            if (getOnSeekBarChangeListener() != null) {
                getOnSeekBarChangeListener().onStopTrackingTouch(bar);
            }
        }
    };

    /**
     * update pause or play icon
     */
    public void updatePausePlay() {
//        Logger.d("updatePausePlay, getPlayerState : ");
        if (null == mRoot || null == mPlayerOperation) {
            return;
        }

        if (isForwardBackwarding()) {
            return;
        }

        Logger.d("updatePausePlay, player state : " + mPlayerOperation.getPlayerState());
        mHolder.playTitle.setText(mPlayerOperation.getMediaName());
//        if (PlayerState.STATE_PLAYING == mPlayerOperation.getPlayerState()) {
//            mHolder.playStatusView.setImageResource(mHolder.mStartBtnResId);
//        } else if (PlayerState.STATE_PAUSED == mPlayerOperation.getPlayerState()) {
//            mHolder.playStatusView.setImageResource(mHolder.mPauseBtnResId);
//            mHandler.removeMessages(FADE_OUT);
//            mHandler.sendEmptyMessageDelayed(FADE_OUT, DEFAULT_TIMEOUT);
//        }
        boolean isInPlaybackState = isInPlaybackState();
        int imgId = isInPlaybackState ? mHolder.mPauseBtnResId : mHolder.mStartBtnResId;
        mHolder.playStatusView.setImageResource(imgId);

//        if (!isInPlaybackState) {
//            mHandler.removeMessages(FADE_OUT);
//            mHandler.sendEmptyMessageDelayed(FADE_OUT, DEFAULT_TIMEOUT);
//        }
    }

    private boolean isInPlaybackState() {
        return mPlayerOperation != null
                && (PlayerState.STATE_PLAYING == mPlayerOperation.getPlayerState()
                || PlayerState.STATE_PREPARING == mPlayerOperation.getPlayerState()
                || PlayerState.STATE_PREPARED == mPlayerOperation.getPlayerState());
    }

    public boolean isPaused() {
        return mPlayerOperation != null
                && PlayerState.STATE_PAUSED == mPlayerOperation.getPlayerState();
    }

    private void doPauseResume(boolean isClick) {
        if (null == mPlayerOperation) {
            return;
        }

        show();

        int playerState = mPlayerOperation.getPlayerState();
        Logger.d("doPauseResume, player state : " + playerState);
        if (PlayerState.STATE_PLAYING == playerState
                || PlayerState.STATE_PAUSED == playerState) {
            mPlayerOperation.setPauseClick(isClick);
            if (PlayerState.STATE_PLAYING == playerState) {
                mPlayerOperation.pausePlayer();
            } else {
                mPlayerOperation.resumePlayer();
            }
        } else if (PlayerState.STATE_PREPARING != playerState
                && PlayerState.STATE_STOPPING != playerState) {
            Logger.d("doPauseResume : startPlayer");
            boolean newStart = mPlayerOperation.startPlayer();
            if (newStart) {
                IUserActionListener userActionListener = getUserActionListener();
                if (userActionListener != null) {
                    userActionListener.onUserStart();
                }
            }
        }
        updatePausePlay();
    }

    public void seekToPosition(int seek) {
        if (null == mPlayerOperation) {
            return;
        }
//        if (seek == 0) {
//            mPlayerOperation.startPlayer();
//        }
        mPlayerOperation.setToType("");
        mPlayerOperation.seekTo(seek);
    }

//    public void doFinish() {
//        if (null == mPlayerOperation) {
//            return;
//        }
//
//        mPlayerOperation.stopPlayer();
//        mPlayerOperation = null;
//    }

    private static class MessageHandler extends Handler {
        private final WeakReference<NativeMediaController> mView;

        MessageHandler(NativeMediaController view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            NativeMediaController view = mView.get();
            if (null == view || null == view.mPlayerOperation) {
                return;
            }

            int pos;
            switch (msg.what) {
                case FADE_OUT:
                    view.hide();
                    break;
                case SHOW_PROGRESS:
                    if (!view.mDragging && view.mShowing
                            && (PlayerState.STATE_PLAYING == view.mPlayerOperation.getPlayerState()
                            || PlayerState.STATE_PREPARING
                            == view.mPlayerOperation.getPlayerState())) {
                        pos = view.updateProgress();
                        msg = obtainMessage(SHOW_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                case SHOW_FORWARD_BACKWARD:
                    view.setForwardBackwarding(false);
                    // update pause or play icon
                    view.updatePausePlay();
                    break;
                default:
                    break;
            }
        }
    }

    public void showPlayerBuffering(boolean hasProgress, int progress, String speed,
            boolean isBufferedSupport) {
        if (mPlayerBusyView != null && mPlayerBusyView instanceof PlayerBusyingView) {
            boolean visible = !(hasProgress && progress >= 100);
            ((PlayerBusyingView) mPlayerBusyView).sendMessage(visible, progress, speed,
                    isBufferedSupport);
        }
    }

    public void updateBufferingPercent(int percent) {
        if (mProgress != null) {
            mProgress.setSecondaryProgress((int) (percent / 100.0f * mProgress.getMax()));
        }
    }

    public void hidePlayerBufferingView() {
        if (mPlayerBusyView != null) {
            mPlayerBusyView.setVisibility(View.INVISIBLE);
        }
    }

    public void onKeyBackRecommend() {
//        showPlayerBuffering(true, 100, null, false);
        hidePlayerBufferingView();
//        if (!isPaused()) {
//            doPauseResume(false);
//        }
        hide();
    }

    /**
     * 画进度条锚点
     */
    private void drawThumb() {
        if (null == mThumb) {
            return;
        }

        mThumb.post(mRunnableDrawThumb);
    }

    private final Runnable mRunnableDrawThumb = new Runnable() {

        @Override
        public void run() {
            View thumb = mThumb;
            if (View.VISIBLE != thumb.getVisibility()) {
                thumb.setVisibility(View.VISIBLE);
            }

            ProgressBar seekBar = mProgress;
            int seekBarX = seekBar.getLeft();
            int seekBarY = seekBar.getTop();
            int thumbW = thumb.getWidth();
            int thumbH = thumb.getHeight();

            int progress = seekBar.getProgress();
            int max = seekBar.getMax();
            float width = 0 == max ? 0 : 1f * progress
                    / max * seekBar.getWidth();
            int x = Math.round(seekBarX - (thumbW >> 1) + width);
            int y = seekBarY - thumbH;

            ViewGroup.MarginLayoutParams thumbParams = (ViewGroup.MarginLayoutParams) thumb
                    .getLayoutParams();
            thumbParams.leftMargin = x;
            thumbParams.topMargin = y;
            thumb.setLayoutParams(thumbParams);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        show();
        return super.onTouchEvent(event);
        //return true;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent ev) {
        show();
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        if (null == mPlayerOperation) {
//            return true;
//        }
//
//        int keyCode = event.getKeyCode();
//        final boolean uniqueDown = event.getRepeatCount() == 0
//                && event.getAction() == KeyEvent.ACTION_DOWN;
//        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
//                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
//                || keyCode == KeyEvent.KEYCODE_SPACE) {
//            if (uniqueDown) {
//                doPauseResume();
//                show();
//
//                if (isTouchMode()) {
////                    if (mPauseButton != null) {
////                        mPauseButton.requestFocus();
////                    }
//                } else {
////                    if (mControllerPause != null) {
////                        View view = mControllerPause.getView();
////                        view.requestFocus();
////                        view.requestFocusFromTouch();
////                    }
//                }
//            }
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
//            if (uniqueDown && PlayerState.STATE_PLAYING != mPlayerOperation.getPlayerState()) {
//                doPauseResume();
//                // mPlayer.start();
//                // updatePausePlay();
//                show();
//            }
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
//                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
//            if (uniqueDown && PlayerState.STATE_PLAYING == mPlayerOperation.getPlayerState()) {
//                doPauseResume();
//                // mPlayer.pause();
//                // updatePausePlay();
//                show();
//            }
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
//                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
//                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
//            // don't show the controls for volume adjustment
//            return super.dispatchKeyEvent(event);
//        } else if (keyCode == KeyEvent.KEYCODE_BACK
//                || keyCode == KeyEvent.KEYCODE_MENU) {
//            if (uniqueDown) {
//                hide();
//            }
//            return true;
//        }
//
//        if (uniqueDown) {
//            show();
//        }
        return super.dispatchKeyEvent(event);
    }


    /**
     * 清晰度
     */
//    private List<MediaControllerHolder.Controller> mSharpnessControllers;

//    /**
//     * 初始化分辨率按钮
//     *
//     * @param mediaNames
//     * @param selectIdx
//     */
//    public void initSharpnessViews(List<String> mediaNames, int selectIdx) {
//        Logger.d("initSharpnessViews, selectIdx : " + selectIdx);
//        if (null == mediaNames || 0 == mediaNames.size()) {
//            return;
//        }
//        if (selectIdx < 0 || selectIdx >= mediaNames.size()) {
//            selectIdx = 0;
//        }
//
//        mSharpnessControllers = new ArrayList<MediaControllerHolder.Controller>();
//        getPlayerOperation().setSharpness(selectIdx, false);
//
//        // 显示分辨率控件
//        final MediaControllerHolder controllerHolder = getControllerHolder();
//        final ViewGroup vgSharpness = controllerHolder.getVgSharpness();
//        vgSharpness.setVisibility(View.VISIBLE);
//        for (int i = 0; i < mediaNames.size(); i++) {
//            final MediaControllerHolder.Controller controller = controllerHolder
//                    .addSharpnessController(mContext, mediaNames.get(i),
//                            i == selectIdx);
//
//            mSharpnessControllers.add(controller);
//
//            controller.getViewFocus().setTag(i);
//            controller.getViewFocus().setOnClickListener(
//                    mOnSharpnessClickListener);
//            /*
//             * txt 2016.4.25
//			 * 强制第一个分辨率按钮的向左焦点为自己；
//			 * 强制最后一个分辨率按钮的向右焦点为自己；
//			 */
//            if (i == 0) {
//                controller.getViewFocus().setNextFocusLeftId(controller.getViewFocus().getId());
//            }
//            if (i == mediaNames.size() - 1) {
//                controller.getViewFocus().setNextFocusRightId(controller.getViewFocus().getId());
//            }
//            controller.getViewFocus().setNextFocusDownId(mThumb.getId());//强制分辨率向下焦点走向为游标
//        }
//    }

//    private OnClickListener mOnSharpnessClickListener = new BaseOnClickListener() {
//
//        @Override
//        public void onClick(View v) {
//
//            int index = (Integer) v.getTag();
//            final MediaControllerHolder controllerHolder = getControllerHolder();
//
//          //改变之前选中的按钮的背景
//            ImageView imageView = mSharpnessControllers.get(
//                    getPlayerOperation().getSharpness()).getIgv();
//            imageView.setImageResource(controllerHolder.viewBgResId);
//
//            // 改变现在选中的按钮的背景
//            imageView = mSharpnessControllers.get(index).getIgv();
//            imageView.setImageResource(controllerHolder.viewSelectBgResId);
//
//            IUserActionListener userActionListener = getUserActionListener();
//            if (userActionListener != null) {
//                userActionListener.onUserStart();
//            }
//            getPlayerOperation().setSharpness(index, true);
//            super.onClick(v);
//        }
//    };

//    private void thumbRequestFocus(int keyCode) {
//        if (mThumb != null && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
//                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
//            mThumb.requestFocus();
//        }
//    }
    private void initControllerView(View v) {
//
//        setTouchMode(true);
//
//        mPauseButton = (ImageButton) v.findViewById(R.id.pause);
//        if (mPauseButton != null) {
//            mPauseButton.requestFocus();
//            mPauseButton.setOnClickListener(mPauseListener);
//        }
//
//        mFullscreenButton = (ImageButton) v.findViewById(R.id.fullscreen);
//        if (mFullscreenButton != null) {
//            mFullscreenButton.requestFocus();
//            mFullscreenButton.setOnClickListener(mFullscreenListener);
//        }
//
//        mFfwdButton = (ImageButton) v.findViewById(R.id.ffwd);
//        if (mFfwdButton != null) {
//            mFfwdButton.setOnClickListener(mFfwdListener);
//            if (!mFromXml) {
//                mFfwdButton.setVisibility(mUseFastForward ? View.VISIBLE
//                        : View.GONE);
//            }
//        }
//
//        mRewButton = (ImageButton) v.findViewById(R.id.rew);
//        if (mRewButton != null) {
//            mRewButton.setOnClickListener(mRewListener);
//            if (!mFromXml) {
//                mRewButton.setVisibility(mUseFastForward ? View.VISIBLE
//                        : View.GONE);
//            }
//        }
//
//        // By default these are hidden. They will be enabled when
//        // setPrevNextListeners() is called
//        mNextButton = (ImageButton) v.findViewById(R.id.next);
//        if (mNextButton != null && !mFromXml && !mListenersSet) {
//            mNextButton.setVisibility(View.GONE);
//        }
//        mPrevButton = (ImageButton) v.findViewById(R.id.prev);
//        if (mPrevButton != null && !mFromXml && !mListenersSet) {
//            mPrevButton.setVisibility(View.GONE);
//        }
//
//        mProgress = (SeekBar) v.findViewById(R.id.mediacontroller_progress);
//        mProgress.setPadding(0, 0, 0, 0);
//        if (mProgress != null) {
//            if (mProgress instanceof SeekBar) {
//                SeekBar seeker = (SeekBar) mProgress;
//                seeker.setOnSeekBarChangeListener(mSeekListener);
//            }
//            mProgress.setMax(1000);
//        }
//
//        mEndTime = (TextView) v.findViewById(R.id.time);
//        mCurrentTime = (TextView) v.findViewById(R.id.time_current);
//        mFormatBuilder = new StringBuilder();
//        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
//
//        installPrevNextListeners();
//        show();
    }

    /**
     * Disable pause or seek buttons if the stream cannot be paused or seeked. This requires the
     * control interface to be a MediaPlayerControlExt
     */
//    private void disableUnsupportedButtons() {
//        if (null == mPlayerOperation) {
//            return;
//        }
//
//        try {
//            if (mPauseButton != null && !mPlayerOperation.canPause()) {
//                mPauseButton.setEnabled(false);
//            }
//            if (mRewButton != null && !mPlayerOperation.canSeekBackward()) {
//                mRewButton.setEnabled(false);
//            }
//            if (mFfwdButton != null && !mPlayerOperation.canSeekForward()) {
//                mFfwdButton.setEnabled(false);
//            }
//
//            //if (!isTouchMode())
//            {
//                if (mPlayerOperation.getSubtitleCount() <= 0) {
//                    mControllerSubtitle.setVisible(false);
//                }
//                if (mPlayerOperation.getSoundTrackCount() <= 0) {
//                    mControllerSoundtrack.setVisible(false);
//                }
//            }
//        } catch (IncompatibleClassChangeError ex) {
//            // We were given an old version of the interface, that doesn't have
//            // the canPause/canSeekXYZ methods. This is OK, it just means we
//            // assume the media can be paused and seeked, and so we don't
//            // disable
//            // the buttons.
//        }
//    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        show();
//        return super.onTouchEvent(event);
//        //return true;
//    }
//
//    @Override
//    public boolean onTrackballEvent(MotionEvent ev) {
//        show();
//        return false;
//    }
//
//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        if (null == mPlayerOperation) {
//            return true;
//        }
//
//        int keyCode = event.getKeyCode();
//        final boolean uniqueDown = event.getRepeatCount() == 0
//                && event.getAction() == KeyEvent.ACTION_DOWN;
//        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
//                || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
//                || keyCode == KeyEvent.KEYCODE_SPACE) {
//            if (uniqueDown) {
//                doPauseResume();
//                show();
//
//                if (isTouchMode()) {
//                    if (mPauseButton != null) {
//                        mPauseButton.requestFocus();
//                    }
//                } else {
////                    if (mControllerPause != null) {
////                        View view = mControllerPause.getView();
////                        view.requestFocus();
////                        view.requestFocusFromTouch();
////                    }
//                }
//            }
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
//            if (uniqueDown && PlayerState.STATE_PLAYING != mPlayerOperation.getPlayerState()) {
//                doPauseResume();
//                // mPlayer.start();
//                // updatePausePlay();
//                show();
//            }
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
//                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
//            if (uniqueDown && PlayerState.STATE_PLAYING == mPlayerOperation.getPlayerState()) {
//                doPauseResume();
//                // mPlayer.pause();
//                // updatePausePlay();
//                show();
//            }
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
//                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
//                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
//            // don't show the controls for volume adjustment
//            return super.dispatchKeyEvent(event);
//        } else if (keyCode == KeyEvent.KEYCODE_BACK
//                || keyCode == KeyEvent.KEYCODE_MENU) {
//            if (uniqueDown) {
//                hide();
//            }
//            return true;
//        }
//
//        if (uniqueDown) {
//            show();
//        }
//        return super.dispatchKeyEvent(event);
//    }

//    private OnClickListener mPauseListener = new BaseOnClickListener() {
//        public void onClick(View v) {
//            doPauseResume();
//            super.onClick(v);
//        }
//    };
//
//    private OnClickListener mFullscreenListener = new BaseOnClickListener() {
//        public void onClick(View v) {
//            doToggleFullscreen();
//            super.onClick(v);
//        }
//    };

//    public void updateFullScreen() {
//        if (mRoot == null || mFullscreenButton == null || null == mPlayerOperation) {
//            return;
//        }

//        int viewId = mPlayerOperation.isFullScreen() ? mUnFullScreenResId : mFullScreenResId;
//
//        if (isTouchMode()) {
//            mFullscreenButton.setImageResource(viewId);
//        } else {
//        }
//    }

//    @Override
//    public void setEnabled(boolean enabled) {
//        if (isTouchMode()) {
//            if (mPauseButton != null) {
//                mPauseButton.setEnabled(enabled);
//            }
//            if (mFfwdButton != null) {
//                mFfwdButton.setEnabled(enabled);
//            }
//            if (mRewButton != null) {
//                mRewButton.setEnabled(enabled);
//            }
//            if (mNextButton != null) {
//                mNextButton.setEnabled(enabled && mNextListener != null);
//            }
//            if (mPrevButton != null) {
//                mPrevButton.setEnabled(enabled && mPrevListener != null);
//            }
//            if (mProgress != null) {
//                mProgress.setEnabled(enabled);
//            }
//        } else {
//        }
////        disableUnsupportedButtons();
//        super.setEnabled(enabled);
//    }

//    public void setProgressVisible(boolean visible) {
//        View mProgressView = getControllerHolder().getVgProgress();
//        if (visible) {
//            mProgressView.setVisibility(View.VISIBLE);
//        } else {
//
//            // mVgProgress.setVisibility(View.GONE);
//
//            stopJumpingBeans();
//
//            getControllerHolder().getProgressText().setText(null);
//            mProgressView.setVisibility(View.GONE);
//
//            // if (tvBufferUnderflow != null) {
//            // tvBufferUnderflow.setVisibility(View.GONE);
//            // }
//
//            getControllerHolder().getTips().setVisibility(View.GONE);
//        }
//    }

//    public boolean isProgressVisible() {
//        View mProgressView = getControllerHolder().getVgProgress();
//        if (mProgressView != null) {
//            return View.VISIBLE == mProgressView.getVisibility();
//        }
//        return false;
//    }
//
//    /**
//     * 更新缓冲进度
//     *
//     * @param context
//     * @param hasProgress
//     * @param progress
//     *         小于0，表示处于连接状态；否则，代表处于缓冲加载状态，且此时hasProgress当为true时（处于缓冲加载状态）， 在界面显示progress数值
//     * @param tryTimes
//     *         已经重试的次数。当progress小于0时有效
//     * @param showTips
//     *         是否显示提示信息。当progress小于0时有效
//     */
//    public void updateProgress(Context context, boolean hasProgress,
//            int progress, int tryTimes, boolean showTips) {
//        Logger.d("updatePlayerProgress, hasProgress : " + hasProgress
//                + ", progress : " + progress);
//        TextView progressLabel = getControllerHolder().getProgressLabel();
//        if (null == progressLabel) {
//            return;
//        }
//
//        // 已经缓冲完毕
//        if (hasProgress && progress >= 100) {
//            setProgressVisible(false);
//            return;
//        }
//
//        TextView tips = getControllerHolder().getTips();
//        TextView progressText = getControllerHolder().getProgressText();
//
//        if (progress < 0) { // 小于0，表示处于连接状态
//            String text = null;
//
//            if (getPlayerOperation().isLocalPlay()) { // 本地播放
//                text = context.getString(R.string.player_txt_loading);
//            } else {// 在线播放
//                if (tryTimes > 0) {
//                    text = String.format(
//                            context.getString(R.string.player_txt_connecting_times),
//                            (tryTimes + 1));
//                } else {
//                    text = context.getString(R.string.player_txt_connecting);
//                }
//            }
//
//            // 与上一次缓冲时的提示字符串(JumpingBeans) 不同
//            if (StringUtils.isNullOrEmpty(mLastJumpingBeansText)
//                    || !mLastJumpingBeansText.equals(text)) {
//
//                stopJumpingBeans();
//                mLastJumpingBeansText = text;
//                progressLabel.setText(Html.fromHtml(text));
//                mJumpingBeans = JumpingBeans.with(progressLabel)
//                        .appendJumpingDots()
//                        .build();
//            }
//
//            if (showTips) {
//                tips.setVisibility(View.VISIBLE);
//            }
//
//            if (progressText != null) {
//                progressText.setVisibility(View.GONE);
//                progressText.setText(null);
//            }
//        } else { // 处于缓冲加载状态
//            if (null == progressText.getText()) {
//                String text = context.getString(R.string.player_txt_loading);
//
//                // 与上一次缓冲时的提示字符串(JumpingBeans) 不同
//                if (StringUtils.isNullOrEmpty(mLastJumpingBeansText)
//                        || !mLastJumpingBeansText.equals(text)) {
//
//                    stopJumpingBeans();
//                    mLastJumpingBeansText = text;
//                    progressLabel.setText(Html.fromHtml(text));
//                    mJumpingBeans = JumpingBeans.with(progressLabel)
//                            .appendJumpingDots()
//                            .build();
//                }
//            }
//
//            tips.setVisibility(View.GONE);
//
//            if (hasProgress && progressText != null) {
//                progressText.setText(Integer.toString(progress) + "%");
//                progressText.setVisibility(View.VISIBLE);
//            }
//        }
//
//        setProgressVisible(true);
//    }

//    private void stopJumpingBeans() {
//        // Append jumping dots
//        if (mJumpingBeans != null) {
//            mJumpingBeans.stopJumping();
//        }
//        mLastJumpingBeansText = null;
//    }

    //添加焦点变化监听
//    private OnFocusChangeListener mFocusChangeListener = new OnFocusChangeListener() {
//
//        @Override
//        public void onFocusChange(View v, boolean hasFocus) {
//            if (hasFocus) {
//                mThumb.setScaleX(1.5f);
//                mThumb.setScaleY(1.5f);
//            } else {
//                mThumb.setScaleX(1f);
//                mThumb.setScaleY(1f);
//            }
//        }
//    };

//    private OnClickListener mRewListener = new BaseOnClickListener() {
//        public void onClick(View v) {
//            if (null == mPlayerOperation) {
//                return;
//            }
//
//            int pos = mPlayerOperation.getCurrentPosition();
//            pos -= 5000; // milliseconds
//            mPlayerOperation.seekTo(pos);
//            updateProgress();
//
//            super.onClick(v);
//        }
//    };
//
//    private OnClickListener mFfwdListener = new BaseOnClickListener() {
//        public void onClick(View v) {
//            if (null == mPlayerOperation) {
//                return;
//            }
//
//            int pos = mPlayerOperation.getCurrentPosition();
//            pos += 15000; // milliseconds
//            mPlayerOperation.seekTo(pos);
//            updateProgress();
//
//            super.onClick(v);
//        }
//    };

//    private void installPrevNextListeners() {
//        if (mNextButton != null) {
//            mNextButton.setOnClickListener(mNextListener);
//            mNextButton.setEnabled(mNextListener != null);
//        }
//
//        if (mPrevButton != null) {
//            mPrevButton.setOnClickListener(mPrevListener);
//            mPrevButton.setEnabled(mPrevListener != null);
//        }
//    }

//    public void setPrevNextListeners(OnClickListener next, OnClickListener prev) {
//        mNextListener = next;
//        mPrevListener = prev;
//        mListenersSet = true;
//
//        if (mRoot != null) {
//            installPrevNextListeners();
//
//            if (mNextButton != null && !mFromXml) {
//                mNextButton.setVisibility(View.VISIBLE);
//            }
//            if (mPrevButton != null && !mFromXml) {
//                mPrevButton.setVisibility(View.VISIBLE);
//            }
//        }
//    }
    public PlayerOperation getPlayerOperation() {
        return mPlayerOperation;
    }

    public void setPlayerOperation(PlayerOperation playerOperation) {
        mPlayerOperation = playerOperation;
        updatePausePlay();
    }

    public boolean isForwardBackwarding() {
        return mIsForwardBackwarding;
    }

    public void setForwardBackwarding(boolean forwardBackwarding) {
        mIsForwardBackwarding = forwardBackwarding;
    }

    public IUserActionListener getUserActionListener() {
        return mUserActionListener;
    }

    public void setUserActionListener(IUserActionListener userActionListener) {
        this.mUserActionListener = userActionListener;
    }

//    public int getCtrlViewLayoutID() {
//        return mCtrlViewLayoutID;
//    }
//
//    public void setCtrlViewLayoutID(int ctrlViewLayoutID) {
//        this.mCtrlViewLayoutID = ctrlViewLayoutID;
//    }

    public MediaControllerHolder getControllerHolder() {
        return mHolder;
    }

    public void setControllerHolder(MediaControllerHolder mHolder) {
        this.mHolder = mHolder;
    }

    public OnSeekBarChangeListener getOnSeekBarChangeListener() {
        return mOnSeekBarChangeListener;
    }

    public void setOnSeekBarChangeListener(
            OnSeekBarChangeListener mOnSeekBarChangeListener) {
        this.mOnSeekBarChangeListener = mOnSeekBarChangeListener;
    }

    public interface MediaPlayerControl {
        void start();

        void pause();

        int getDuration();

        int getCurrentPosition();

        void seekTo(int pos);

        boolean isPlaying();

        int getBufferPercentage();

        boolean canPause();

        boolean canSeekBackward();

        boolean canSeekForward();

        boolean isFullScreen();

        void toggleFullScreen();
    }

//    private void doToggleFullscreen() {
//        if (null == mPlayerOperation) {
//            return;
//        }
//
//        mPlayerOperation.toggleFullScreen();
//    }

//
//
//    class BaseOnClickListener implements OnClickListener {
//        @Override
//        public void onClick(View v) {
//            show();
//        }
//    }
}
