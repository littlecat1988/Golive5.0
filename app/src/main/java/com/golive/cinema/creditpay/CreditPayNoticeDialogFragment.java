package com.golive.cinema.creditpay;

import static com.golive.cinema.filmdetail.FilmWatchNoticeDialogFragment.NOTICE_TYPE_SHOUFA;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
public class CreditPayNoticeDialogFragment extends BaseDialog {

    public interface OnNoticeHideListener {
        void onHide();
    }

    private OnNoticeHideListener mOnNoticeHideListener;

    private double mLimit = NOTICE_TYPE_SHOUFA;
    private long mDeadlineDays = 0;

    public static CreditPayNoticeDialogFragment newInstance(double limit, int deadlineDays) {
        CreditPayNoticeDialogFragment fragment = new CreditPayNoticeDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putDouble(Constants.EXTRA_LIMIT, limit);
        bundle.putInt(Constants.EXTRA_DEAD_LINE_DAYS, deadlineDays);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mLimit = bundle.getDouble(Constants.EXTRA_LIMIT);
        mDeadlineDays = bundle.getInt(Constants.EXTRA_DEAD_LINE_DAYS);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        int layoutResId = R.layout.credit_pay_notice_frag;
        final View view = inflater.inflate(layoutResId, container, false);
        TextView limitTv = (TextView) view.findViewById(R.id.credit_pay_limit_tv);
        TextView daysTv = (TextView) view.findViewById(R.id.credit_pay_days_tv);
        View okView = view.findViewById(R.id.credit_pay_btn);

        String limitStr = String.format(getString(R.string.price_RMB), mLimit);
        limitTv.setText(String.format(getString(R.string.credit_pay_notice_limit), limitStr));

        String daysText;
        if (0 >= mDeadlineDays) {
            daysText = getString(R.string.credit_pay_notice_dead_line_forever);
        } else {
            daysText = String.format(getString(R.string.credit_pay_notice_dead_line_days),
                    mDeadlineDays);
        }
        daysTv.setText(daysText);

        okView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

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
        hide();
    }

    private void hide() {
        dismiss();
        if (mOnNoticeHideListener != null) {
            mOnNoticeHideListener.onHide();
        }
    }

    public void setOnNoticeHideListener(OnNoticeHideListener onNoticeHideListener) {
        mOnNoticeHideListener = onNoticeHideListener;
    }
}
