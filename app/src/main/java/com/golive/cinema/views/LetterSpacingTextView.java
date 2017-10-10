package com.golive.cinema.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.widget.TextView;

import com.golive.cinema.R;
import com.initialjie.log.Logger;

import java.math.BigDecimal;

/**
 * Text view that allows changing the letter spacing of the text.
 *
 * @author Pedro Barros (pedrobarros.dev at gmail.com)
 * @since May 7, 2013
 */
public class LetterSpacingTextView extends android.support.v7.widget.AppCompatTextView {

    private static String nameSpace = "http://schemas.android.com/apk/res-auto";

    private static final boolean DEBUG = false;

    private float letterSpacing = LetterSpacing.NORMAL;
    private CharSequence originalText = "";
    private int maxLines = -1;
    private boolean autoMaxLines;
    private String mText;

    private char lineSeparator;

    private boolean defineLetterSpace = false;

    public LetterSpacingTextView(Context context) {
        super(context);
        // Logger.d("LetterSpacingTextView, 1");
    }

    public LetterSpacingTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Logger.d("LetterSpacingTextView, 2");
        init(context, attrs);
    }

    public LetterSpacingTextView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
        // Logger.d("LetterSpacingTextView, 3");
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        lineSeparator = System.getProperty("line.separator").charAt(0);

        // 获取字符间隔
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.LetterSpacingTextView);
        letterSpacing = a.getFloat(
                R.styleable.LetterSpacingTextView_letter_spacing, 0);

        // setLetterSpacing(letterSpacing);

        defineLetterSpace = 0 != BigDecimal.ZERO.compareTo(new BigDecimal(letterSpacing));

        maxLines = a.getInt(R.styleable.LetterSpacingTextView_max_lines, -1);
        mText = a.getString(R.styleable.LetterSpacingTextView_text);
        if (DEBUG) {
            Logger.d("init, mText : " + mText);
        }
        if (!TextUtils.isEmpty(mText)) {
            setText(mText);
        }

        autoMaxLines = a.getBoolean(R.styleable.LetterSpacingTextView_auto_max_lines, false);
        a.recycle();
    }

//    @Override
//    public boolean isFocused() {
//        // TODO Auto-generated method stub
//
//        // 为了能一直跑马灯（假设设置要跑马灯）
//        return true;
//    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
            Rect previouslyFocusedRect) {
        // TODO Auto-generated method stub
        if (focused) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
        if (focused) {
            super.onWindowFocusChanged(focused);
        }
    }

    public interface OnLayoutListener {
        void onLayouted(TextView view);
    }

    private OnLayoutListener mOnLayoutListener;

    private boolean newText = true;

    public void setOnLayoutListener(OnLayoutListener listener) {
        mOnLayoutListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!newText) {
            return;
        }

        if (mOnLayoutListener != null) {
            mOnLayoutListener.onLayouted(this);
        }

        // if (DEBUG) {
        // Logger.d("onLayout, text : " + getText()
        // + ", lines : " + getLineCount());
        // }

        if (TextUtils.isEmpty(getText()) && !TextUtils.isEmpty(mText)) {
            setText(mText);
        }

        if (TextUtils.isEmpty(getText())) {
            return;
        }

        if (maxLines > 0 || autoMaxLines) {
            // 最多可以显示多少行
            // int maxL = (int) Math.round(1.0 * getHeight()
            // / getLineHeight());
            int maxL = (int) (1.0 * getHeight() / getLineHeight());

            if (autoMaxLines) {
                maxLines = maxL;
            } else {
                if (-1 == maxLines || maxL < maxLines) {
                    maxLines = maxL;
                }
            }

            if (DEBUG) {
                Logger.d("onLayout, changed : " + changed + ", text : "
                        + getText() + ", tv height : " + getHeight()
                        + ", line height : " + getLineHeight()
                        + ", max line : " + maxLines + ", line count : "
                        + getLineCount());
            }
            if (maxLines >= 1 && getLineCount() > maxLines) {
                int lineEndIndex = getLayout().getLineEnd(maxLines - 1);
                // Logger.d("onGlobalLayout, tv : " + getText()
                // + "\ntv length: " + length()
                // + ", lineEndIndex : " + lineEndIndex);

                // 有定义字间隔
                if (defineLetterSpace) {
                    lineEndIndex >>= 1;
                    lineEndIndex += 1;
                }
                String text = getText().subSequence(0, lineEndIndex - 3)
                        + "...";
                setText(text);
                newText = false;
            }
        }
        // else {
        // if (!TextUtils.isEmpty(mText)) {
        // setText(mText);
        // }
        // }

        newText = false;
    }

    public float getLetterSpacing() {
        return letterSpacing;
    }

    public void setLetterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
        defineLetterSpace = Math.abs(letterSpacing - LetterSpacing.NORMAL) >= 10e-6;
        applyLetterSpacing();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        newText = true;

        super.setText(text, type);
        // Logger.d("setText, text : " + text);
        originalText = text;
        applyLetterSpacing();
    }

    @Override
    public CharSequence getText() {
        return originalText;
    }

    private void applyLetterSpacing() {
        if (TextUtils.isEmpty(originalText)) {
            return;
        }

        if (!defineLetterSpace) {
            return;
            // TypedArray a = getContext().obtainStyledAttributes(
            // R.styleable.LetterSpacingTextView);
            // letterSpacing = a.getFloat(
            // R.styleable.LetterSpacingTextView_letter_spacing, 0);
            // a.recycle();
            //
            // // Logger.d("applyLetterSpacing, letterSpacing : " +
            // // letterSpacing
            // // + ", text :" + getText());
            // if (LetterSpacing.NORMAL == letterSpacing) {
            // return;
            // }
        }
        if (DEBUG) {
            Logger.d("applyLetterSpacing, originalText : " + originalText);
        }
        SpannableString finalText = addSpace();
        super.setText(finalText, BufferType.SPANNABLE);
    }

    private SpannableString addSpace() {
//        long sTime = SystemClock.currentThreadTimeMillis();

        // 插入空格
        StringBuilder builder = new StringBuilder(originalText.length() << 1);
        int length = originalText.length();
        char c;
        for (int i = 0; i < length; i++) {
            c = originalText.charAt(i);
            builder.append(c);
            if (i + 1 < length && c != lineSeparator) {

                // 前后有英文
                if (isEn(originalText.charAt(i))
                        || isEn(originalText.charAt(i + 1))) {
                    builder.append("\u00A0"); // no-break space
                } else {
                    builder.append(" "); // simple space
                }

            }
        }

        // 字符间隔缩放
        float proportion = letterSpacing;
        String text = builder.toString();
        SpannableString finalText = new SpannableString(text);
        length = text.length();
        if (length > 1) {
            for (int i = 1; i < length; ) {
                c = text.charAt(i);
                // 不是添加的空格，继续找下一个
                if (c != ' ' && c != '\u00A0') {
                    ++i;
                } else {
                    // 缩放添加的空格
                    finalText.setSpan(new ScaleXSpan(proportion), i, i + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    // 跳跃一个字符位置
                    i += 2;
                }
            }
        }

//        long eTime = SystemClock.currentThreadTimeMillis();
        // Logger.d("addSpace, times : " + (eTime - sTime) + "ms");
        return finalText;
    }

    private boolean isEn(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
    }

    public int getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;

    }

    public class LetterSpacing {
        public final static float NORMAL = 0;
        public final static float NORMALBIG = (float) 0.025;
        public final static float BIG = (float) 0.05;
        public final static float BIGGEST = (float) 0.25;
    }
}