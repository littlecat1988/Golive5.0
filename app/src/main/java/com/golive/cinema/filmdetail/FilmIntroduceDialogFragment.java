package com.golive.cinema.filmdetail;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.golive.cinema.Constants;
import com.golive.cinema.R;

/**
 * Created by Wangzj on 2016/11/19.
 */

public class FilmIntroduceDialogFragment extends DialogFragment {

    private String mIntroduce;

    public static FilmIntroduceDialogFragment newInstance(String text) {
        Bundle arguments = new Bundle();
        arguments.putString(Constants.EXTRA_FILM_INTRODUCE, text);
        FilmIntroduceDialogFragment fragment = new FilmIntroduceDialogFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.dialog_fullscreen_base);
        Bundle arguments = getArguments();
        mIntroduce = arguments.getString(Constants.EXTRA_FILM_INTRODUCE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.film_detail_introduce_frag, container, false);
        TextView tv = (TextView) view.findViewById(R.id.film_detail_introduce_tv);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv.setText(mIntroduce);
        return view;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.color.film_detail_introduce_bg);
        }
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (KeyEvent.ACTION_DOWN == event.getAction()) {
                    if (KeyEvent.KEYCODE_ENTER == keyCode
                            || KeyEvent.KEYCODE_DPAD_CENTER == keyCode) {
                        dismiss();
                    }
                }
                return false;
            }
        });
        return dialog;
    }
}
