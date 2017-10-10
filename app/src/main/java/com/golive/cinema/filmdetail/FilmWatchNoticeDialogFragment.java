package com.golive.cinema.filmdetail;

import static com.golive.cinema.Constants.DEFAULT_WATCH_LIMIT_TIME;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.R;

/**
 * Created by Wangzj
 */
public class FilmWatchNoticeDialogFragment extends BaseDialog implements View.OnClickListener {

    public static final int NOTICE_TYPE_SHOUFA = 0;
    public static final int NOTICE_TYPE_TONGBU = 1;

    public interface OnNoticeHideListener {
        void onHide(boolean hideAlways);
    }

    private OnNoticeHideListener mOnNoticeHideListener;

    private int mNoticeType = NOTICE_TYPE_SHOUFA;
    private long mLimitTime = 0;

    public static FilmWatchNoticeDialogFragment newInstance(int noticeType, long limitTime) {
        FilmWatchNoticeDialogFragment fragment = new FilmWatchNoticeDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.EXTRA_NOTICE_TYPE, noticeType);
        bundle.putLong(Constants.EXTRA_LIMIT_TIME, limitTime);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mNoticeType = bundle.getInt(Constants.EXTRA_NOTICE_TYPE, NOTICE_TYPE_SHOUFA);
        mLimitTime = bundle.getLong(Constants.EXTRA_LIMIT_TIME, DEFAULT_WATCH_LIMIT_TIME);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        int layoutResId = NOTICE_TYPE_SHOUFA == mNoticeType ? R.layout.film_watch_notice_frag_shoufa
                : R.layout.film_watch_notice_frag_tongbu;
        final View view = inflater.inflate(layoutResId, container,
                false);
//        TextView titleTv = (TextView) view.findViewById(R.id.film_watch_notice_title_tv);
        TextView contentTv = (TextView) view.findViewById(R.id.film_watch_notice_content_tv);
        View hideView = view.findViewById(R.id.film_watch_notice_hide);
        View hideAlwaysView = view.findViewById(R.id.film_watch_notice_hide_always);

        hideView.setOnClickListener(this);
        hideAlwaysView.setOnClickListener(this);

//        int titleResId =
//                NOTICE_TYPE_SHOUFA == mNoticeType ? R.string.film_watch_notice_title_shoufa :
//                        R.string.film_watch_notice_title_tongbu;
        int contentResId =
                NOTICE_TYPE_SHOUFA == mNoticeType ? R.string.film_watch_notice_content_shoufa
                        : R.string.film_watch_notice_content_tongbu;
        String text = getString(contentResId);
        if (NOTICE_TYPE_SHOUFA == mNoticeType) {
            long time = mLimitTime;
            if (time <= 0) {
                time = DEFAULT_WATCH_LIMIT_TIME;
            }
            // hour
            text = String.format(text, time / 3600000L);
        }
//        titleTv.setText(titleResId);
        contentTv.setText(Html.fromHtml(text));
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        hide(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.film_watch_notice_hide:
                hide(false);
                break;
            case R.id.film_watch_notice_hide_always:
                hide(true);
                break;
            default:
                break;
        }
    }

    private void hide(boolean hideAlways) {
        dismiss();
        if (mOnNoticeHideListener != null) {
            mOnNoticeHideListener.onHide(hideAlways);
        }
    }

    public void setOnNoticeHideListener(
            OnNoticeHideListener onNoticeHideListener) {
        mOnNoticeHideListener = onNoticeHideListener;
    }
}
