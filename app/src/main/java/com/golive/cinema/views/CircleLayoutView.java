package com.golive.cinema.views;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.golive.cinema.R;


/**
 * 倒计时
 *
 * @author chengang
 */
public class CircleLayoutView extends View {

//    private static String name_space = "http://schemas.android.com/apk/res-auto";

    private static final String INSTANCE_PREFIX = "s";
    private static final String INSTANCE_STATE = "saved_instance";
    private static final String INSTANCE_FINISHED_STROKE_COLOR = "finished_stroke_color";
    private static final String INSTANCE_UNFINISHED_STROKE_COLOR = "unfinished_stroke_color";
    private static final String INSTANCE_MAX = "mMax";
    private static final String INSTANCE_PROGRESS = "mProgress";
    private static final String INSTANCE_FINISHED_STROKE_WIDTH = "finished_stroke_width";
    private static final String INSTANCE_UNFINISHED_STROKE_WIDTH = "unfinished_stroke_width";
    private static final String INSTANCE_BACKGROUND_COLOR = "inner_background_color";
    private static final String INSTANCE_STARTING_DEGREE = "starting_degree";

    private Paint mFinishedPaint;
    private Paint mUnfinishedPaint;
    private Paint mInnerCirclePaint;
    private Paint mTimeTextPaint;
    private Paint mPerTextPaint;

    private final RectF mFinishedOuterRect = new RectF();
    private final RectF mUnfinishedOuterRect = new RectF();

    private int mTimeText;
    private float mTimeTextSize;
    private int mTimeTextColor;
    private int mProgress;
    private int mMax;
    private int mFinishedStrokeColor;
    private int unfinishedStrokeColor;
    private int mStartingDegree;
    private float mEndDegree;
    private float mFinishedStrokeWidth;
    private float mUnfinishedStrokeWidth;
    private int mInnerBackgroundColor;
    private float mPerTextSize;
    private int mPerTextColor;
    private String perText = null;

    private final float DEFAULT_STROKE_WIDTH;
    private final int DEFAULT_FINISHED_COLOR = Color.WHITE;
    private final int DEFAULT_UNFINISHED_COLOR = Color.rgb(204, 204, 204);
    private final int DEFAULT_TIME_TEXT_COLOR = Color.rgb(66, 145, 241);
    private final int DEFAULT_PER_TEXT_COLOR = Color.rgb(66, 145, 241);
    private final int DEFAULT_INNER_BACKGROUND_COLOR = Color.TRANSPARENT;
    private final float DEFAULT_TIME_TEXT_SIZE;
    private final float DEFAULT_PER_TEXT_SIZE;
    private final int DEFAULT_MAX = 15;
    private final int DEFAULT_STARTINGDEGREE = 0;
    private final int MIN_SIZE;

    private OnFinishCallback mOnFinishCallback = null;
    private final Handler mHandler = new Handler();
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (--mTimeText >= 0) {
                setProgress(mTimeText, true);
                if (mTimeText == 0) {
                    mHandler.postDelayed(this, 200);
                } else {
                    mHandler.postDelayed(this, 1000);
                }
            } else {
//                Logger.d("disms");
                if (mOnFinishCallback != null) {
                    mOnFinishCallback.onFinish();
                }
                circleClear();
            }
        }
    };

    public interface OnFinishCallback {
        void onFinish();
    }

    public void setOnFinishCallback(OnFinishCallback callback) {
        this.mOnFinishCallback = callback;
    }

    public CircleLayoutView(Context context) {
        this(context, null);
    }

    public CircleLayoutView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float scale = getResources().getDisplayMetrics().density;
        DEFAULT_TIME_TEXT_SIZE = sp2px(getResources(), 30);
        DEFAULT_PER_TEXT_SIZE = sp2px(getResources(), 18);
        DEFAULT_STROKE_WIDTH = (int) (5 * scale + 0.5f);
        MIN_SIZE = (int) dp2px(getResources(), 100);

        final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CircleLayoutView, defStyleAttr, 0);
        initByAttributes(attributes);
//        initByAttributes(attrs);
//        attributes.recycle();

        initPainters();
    }

    private void initByAttributes(AttributeSet attrs) {
//        mFinishedStrokeColor = attrs.getAttributeResourceValue(name_space,
// "circle_finished_color", DEFAULT_FINISHED_COLOR);
//        unfinishedStrokeColor = attrs.getAttributeResourceValue(name_space,
// "circle_unfinished_color", DEFAULT_UNFINISHED_COLOR);
//
//        Logger.d("mFinishedStrokeColor:"+mFinishedStrokeColor+":unfinishedStrokeColor
// "+unfinishedStrokeColor);
//
//        setMax(attrs.getAttributeIntValue(name_space, "circle_max", DEFAULT_MAX));
//        setProgress(attrs.getAttributeIntValue(name_space, "circle_progress", 0));
//
//        mFinishedStrokeWidth = attrs.getAttributeResourceValue(name_space,
// "circle_finished_stroke_width", 0);
//        mUnfinishedStrokeWidth = attrs.getAttributeResourceValue(name_space,
// "circle_unfinished_stroke_width", 0);
//
//        mTimeTextColor = attrs.getAttributeResourceValue(name_space, "circle_time_text_color",
// DEFAULT_TIME_TEXT_COLOR);
//        mTimeTextSize = attrs.getAttributeResourceValue(name_space, "circle_time_text_size", 0);
//        mTimeText = attrs.getAttributeIntValue(name_space, "circle_time_text", 0);
//
//        mInnerBackgroundColor = attrs.getAttributeResourceValue(name_space,
// "circle_background_color", DEFAULT_INNER_BACKGROUND_COLOR);
//
//        mPerTextSize = attrs.getAttributeResourceValue(name_space, "circle_per_text_size", 0);
//        mPerTextColor = attrs.getAttributeResourceValue(name_space, "circle_per_text_color",
// DEFAULT_PER_TEXT_COLOR);
//        perText = attrs.getAttributeValue(name_space, "circle_per_text");
//        mEndDegree = 0.0f;
    }

    private static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    private static float sp2px(Resources resources, float sp) {
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    private void initByAttributes(TypedArray attributes) {
        mFinishedStrokeColor = attributes.getColor(
                R.styleable.CircleLayoutView_circle_finished_color, DEFAULT_FINISHED_COLOR);
        unfinishedStrokeColor = attributes.getColor(
                R.styleable.CircleLayoutView_circle_unfinished_color, DEFAULT_UNFINISHED_COLOR);

        setMax(attributes.getInt(R.styleable.CircleLayoutView_circle_max, DEFAULT_MAX));
        setProgress(attributes.getInt(R.styleable.CircleLayoutView_circle_progress, 0), false);

        mFinishedStrokeWidth = attributes.getDimension(
                R.styleable.CircleLayoutView_circle_finished_stroke_width, DEFAULT_STROKE_WIDTH);
        mUnfinishedStrokeWidth = attributes.getDimension(
                R.styleable.CircleLayoutView_circle_unfinished_stroke_width, DEFAULT_STROKE_WIDTH);

        mStartingDegree = attributes.getInt(R.styleable.CircleLayoutView_circle_starting_degree,
                DEFAULT_STARTINGDEGREE);
        mTimeTextColor = attributes.getColor(R.styleable.CircleLayoutView_circle_time_text_color,
                DEFAULT_TIME_TEXT_COLOR);
        mTimeTextSize = attributes.getDimension(R.styleable.CircleLayoutView_circle_time_text_size,
                DEFAULT_TIME_TEXT_SIZE);
        mTimeText = attributes.getInt(R.styleable.CircleLayoutView_circle_time_text, 0);

        mInnerBackgroundColor = attributes.getColor(
                R.styleable.CircleLayoutView_circle_background_color,
                DEFAULT_INNER_BACKGROUND_COLOR);

        mPerTextSize = attributes.getDimension(R.styleable.CircleLayoutView_circle_per_text_size,
                DEFAULT_PER_TEXT_SIZE);
        mPerTextColor = attributes.getColor(R.styleable.CircleLayoutView_circle_per_text_color,
                DEFAULT_PER_TEXT_COLOR);
        perText = attributes.getString(R.styleable.CircleLayoutView_circle_per_text);
        mEndDegree = 0.0f;
    }

    private void initPainters() {
        mFinishedPaint = new Paint();
        mFinishedPaint.setColor(mFinishedStrokeColor);
        mFinishedPaint.setStyle(Paint.Style.STROKE);
        mFinishedPaint.setAntiAlias(true);
        mFinishedPaint.setStrokeWidth(mFinishedStrokeWidth);

        mUnfinishedPaint = new Paint();
        mUnfinishedPaint.setColor(unfinishedStrokeColor);
        mUnfinishedPaint.setStyle(Paint.Style.STROKE);
        mUnfinishedPaint.setAntiAlias(true);
        mUnfinishedPaint.setStrokeWidth(mUnfinishedStrokeWidth);

        mTimeTextPaint = new TextPaint();
        mTimeTextPaint.setColor(mTimeTextColor);
        mTimeTextPaint.setTextSize(mTimeTextSize);
        mTimeTextPaint.setAntiAlias(true);

        mPerTextPaint = new TextPaint();
        mPerTextPaint.setColor(mPerTextColor);
        mPerTextPaint.setTextSize(mPerTextSize);
        mPerTextPaint.setAntiAlias(true);

        mInnerCirclePaint = new Paint();
        mInnerCirclePaint.setColor(mInnerBackgroundColor);
        mInnerCirclePaint.setAntiAlias(true);
    }


    @Override
    public void invalidate() {
        initPainters();
        super.invalidate();
    }

    public float getFinishedStrokeWidth() {
        return mFinishedStrokeWidth;
    }

    public void setFinishedStrokeWidth(float finishedStrokeWidth) {
        this.mFinishedStrokeWidth = finishedStrokeWidth;
        this.invalidate();
    }

    public float getUnfinishedStrokeWidth() {
        return mUnfinishedStrokeWidth;
    }

    public void setUnfinishedStrokeWidth(float unfinishedStrokeWidth) {
        this.mUnfinishedStrokeWidth = unfinishedStrokeWidth;
        this.invalidate();
    }

    public synchronized int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        if (this.mProgress > getMax()) {
            this.mProgress %= getMax();
        }
        invalidate();
    }

    public synchronized void setProgress(int pro, boolean anim) {
        this.mProgress = pro;

        if (this.mProgress > getMax()) {
            this.mProgress %= getMax();
        }

        if (anim) {
//        	setAnimation(pointRotationAnima());
        }

        this.mEndDegree = mProgress / (float) mMax * 360f;

        this.postInvalidate();
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int max) {
        if (max > 0) {
            this.mMax = max;
            invalidate();
        }
    }

    public void circleClear() {
        mProgress = 0;
        mTimeText = 0;
        mMax = 0;
        mEndDegree = 0.0f;
        mHandler.removeCallbacksAndMessages(null);
        this.setVisibility(GONE);
    }

    private void setTimeText(int timeText) {
        if (timeText > 0) {
            this.mMax = timeText;
        }
        this.mTimeText = mMax;
    }

    public int getTimeText() {
        return mTimeText;
    }

    public void startCircle(int timeText) {
        setTimeText(timeText);
        setProgress(mMax, true);
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, 1000);
    }

    private boolean isPaused = false;

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public void pauseCircle() {
        if (!isPaused) {
            setPaused(true);
            mHandler.removeCallbacks(mRunnable);
        }
    }

    public void resumeCircle(int timeText) {
        if (isPaused) {
            setPaused(false);
            this.mTimeText = timeText;
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, 1000);
        }
    }

    /**
     * 进度标注点的动画
     */
    private Animation pointRotationAnima() {
//    	ObjectAnimator mObjectAnimator = ObjectAnimator.ofFloat(target, propertyName, values)

        RotateAnimation animation = new RotateAnimation(mEndDegree - 360, getProgressAngle() - 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(1000);
        animation.setRepeatCount(1);
        animation.setFillAfter(true);
        return animation;
    }

    public int getFinishedStrokeColor() {
        return mFinishedStrokeColor;
    }

    public void setFinishedStrokeColor(int finishedStrokeColor) {
        this.mFinishedStrokeColor = finishedStrokeColor;
    }

    public int getUnfinishedStrokeColor() {
        return unfinishedStrokeColor;
    }

    public void setUnfinishedStrokeColor(int unfinishedStrokeColor) {
        this.unfinishedStrokeColor = unfinishedStrokeColor;
    }

    public int getInnerBackgroundColor() {
        return mInnerBackgroundColor;
    }

    public void setInnerBackgroundColor(int innerBackgroundColor) {
        this.mInnerBackgroundColor = innerBackgroundColor;
    }

    public int getStartingDegree() {
        return mStartingDegree;
    }

    public void setStartingDegree(int startingDegree) {
        this.mStartingDegree = startingDegree;
    }

    private float getProgressAngle() {
        return getProgress() / (float) mMax * 360f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec));
    }

    private int measure(int measureSpec) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = MIN_SIZE;
            if (mode == MeasureSpec.AT_MOST) {
                result = Math.min(result, size);
            }
        }
        return result;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float delta = Math.max(mFinishedStrokeWidth, mUnfinishedStrokeWidth);
        mFinishedOuterRect.set(delta, delta, getWidth() - delta, getHeight() - delta);
        mUnfinishedOuterRect.set(delta, delta, getWidth() - delta, getHeight() - delta);

        float innerCircleRadius = (getWidth() - Math.min(mFinishedStrokeWidth,
                mUnfinishedStrokeWidth)
                + Math.abs(mFinishedStrokeWidth - mUnfinishedStrokeWidth)) / 2f;
        canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, innerCircleRadius,
                mInnerCirclePaint);

        //Logger.d("getStartingDegree():" + getStartingDegree() + "getProgressAngle():"
        //        + getProgressAngle());

        canvas.drawArc(mFinishedOuterRect, getStartingDegree(), getProgressAngle(), false,
                mFinishedPaint);
        canvas.drawArc(mUnfinishedOuterRect, getStartingDegree() + getProgressAngle(),
                360 - getProgressAngle(), false, mUnfinishedPaint);

        if (mTimeText >= 0) {
            float textHeight = mTimeTextPaint.descent() + mTimeTextPaint.ascent();
            if (mTimeText > 9) {
                canvas.drawText(String.valueOf(mTimeText),
                        (getWidth() / 1.5f) - mTimeTextPaint.measureText(String.valueOf(mTimeText)),
                        (getWidth() - textHeight) / 2.0f, mTimeTextPaint);
            } else {
                canvas.drawText(String.valueOf(mTimeText), (getWidth() / 2.0f)
                                - mTimeTextPaint.measureText(String.valueOf(mTimeText)) / 1.5f,
                        (getWidth() - textHeight) / 2.0f, mTimeTextPaint);
            }
        }

        String text = this.perText != null ? this.perText : INSTANCE_PREFIX;
        if (!TextUtils.isEmpty(text)) {
            float textHeight = mTimeTextPaint.descent() + mTimeTextPaint.ascent();
            canvas.drawText(text, (getWidth() + mPerTextPaint.measureText(text) * 2) / 2.0f,
                    (getWidth() - textHeight) / 2.0f, mPerTextPaint);
        }

//      Drawable image = getResources().getDrawable(R.drawable.volume_switch_on);
//      Rect srcRect = new Rect(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
//      Rect dstRect = new Rect(srcRect);
//      Bitmap bitmap = Bitmap.createBitmap(image.getIntrinsicWidth(), image.getIntrinsicHeight()
// , Bitmap.Config.ALPHA_8);
//      canvas.drawBitmap(bitmap, srcRect, dstRect, null);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(INSTANCE_FINISHED_STROKE_COLOR, getFinishedStrokeColor());
        bundle.putInt(INSTANCE_UNFINISHED_STROKE_COLOR, getUnfinishedStrokeColor());
        bundle.putInt(INSTANCE_MAX, getMax());
        bundle.putInt(INSTANCE_STARTING_DEGREE, getStartingDegree());
        bundle.putInt(INSTANCE_PROGRESS, getProgress());
        bundle.putFloat(INSTANCE_FINISHED_STROKE_WIDTH, getFinishedStrokeWidth());
        bundle.putFloat(INSTANCE_UNFINISHED_STROKE_WIDTH, getUnfinishedStrokeWidth());
        bundle.putInt(INSTANCE_BACKGROUND_COLOR, getInnerBackgroundColor());
//        bundle.putInt(INSTANCE_TEXT_COLOR, getTextColor());
//        bundle.putFloat(INSTANCE_TEXT_SIZE, getTextSize());
//        bundle.putFloat(INSTANCE_INNER_BOTTOM_TEXT_SIZE, getInnerBottomTextSize());
//        bundle.putFloat(INSTANCE_INNER_BOTTOM_TEXT_COLOR, getInnerBottomTextColor());
//        bundle.putString(INSTANCE_INNER_BOTTOM_TEXT, getInnerBottomText());
//        bundle.putInt(INSTANCE_INNER_BOTTOM_TEXT_COLOR, getInnerBottomTextColor());
//        bundle.putString(INSTANCE_SUFFIX, getSuffixText());
//        bundle.putString(INSTANCE_PREFIX, getPrefixText());
//        bundle.putString(INSTANCE_TEXT, getText());
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
//            textColor = bundle.getInt(INSTANCE_TEXT_COLOR);
//            textSize = bundle.getFloat(INSTANCE_TEXT_SIZE);
//            innerBottomTextSize = bundle.getFloat(INSTANCE_INNER_BOTTOM_TEXT_SIZE);
//            innerBottomText = bundle.getString(INSTANCE_INNER_BOTTOM_TEXT);
//            innerBottomTextColor = bundle.getInt(INSTANCE_INNER_BOTTOM_TEXT_COLOR);
//            prefixText = bundle.getString(INSTANCE_PREFIX);
//            suffixText = bundle.getString(INSTANCE_SUFFIX);
//            text = bundle.getString(INSTANCE_TEXT);
            mFinishedStrokeColor = bundle.getInt(INSTANCE_FINISHED_STROKE_COLOR);
            unfinishedStrokeColor = bundle.getInt(INSTANCE_UNFINISHED_STROKE_COLOR);
            mFinishedStrokeWidth = bundle.getFloat(INSTANCE_FINISHED_STROKE_WIDTH);
            mUnfinishedStrokeWidth = bundle.getFloat(INSTANCE_UNFINISHED_STROKE_WIDTH);
            mInnerBackgroundColor = bundle.getInt(INSTANCE_BACKGROUND_COLOR);
            initPainters();
            setMax(bundle.getInt(INSTANCE_MAX));
            setStartingDegree(bundle.getInt(INSTANCE_STARTING_DEGREE));
            setProgress(bundle.getInt(INSTANCE_PROGRESS), false);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE_STATE));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mHandler.removeCallbacksAndMessages(null);
    }
}
