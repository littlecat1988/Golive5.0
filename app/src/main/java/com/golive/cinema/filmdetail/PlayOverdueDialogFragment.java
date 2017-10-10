package com.golive.cinema.filmdetail;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.R;

/**
 * Created by Wangzj on 2017/3/23.
 */

public class PlayOverdueDialogFragment extends BaseDialog implements View.OnClickListener {

    public static interface onSelectResultListener {
        void onSelectResult(boolean ok);
    }

    public static PlayOverdueDialogFragment newInstance(String filmName) {
        PlayOverdueDialogFragment fragment = new PlayOverdueDialogFragment();
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_FILM_NAME, filmName);
        fragment.setArguments(args);
        return fragment;
    }

    private String mFilmName;
    private onSelectResultListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFilmName = getArguments().getString(Constants.EXTRA_FILM_NAME);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.play_overdue_frag, container, false);
        TextView titleTv = (TextView) view.findViewById(R.id.title_tv);
        view.findViewById(R.id.ok_btn).setOnClickListener(this);
        view.findViewById(R.id.cancel_btn).setOnClickListener(this);
        titleTv.setText(String.format(getString(R.string.play_overdue_title), mFilmName));
        return view;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mListener != null) {
            mListener.onSelectResult(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_btn:
            case R.id.cancel_btn:
                if (mListener != null) {
                    mListener.onSelectResult(R.id.ok_btn == v.getId());
                }
                dismiss();
                break;
            default:
                break;
        }
    }

    public void setListener(onSelectResultListener listener) {
        mListener = listener;
    }
}
