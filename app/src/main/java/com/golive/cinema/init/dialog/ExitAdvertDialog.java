package com.golive.cinema.init.dialog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.R;

/**
 * Created by chgang on 2016/12/13.
 */

public class ExitAdvertDialog extends BaseDialog implements View.OnClickListener {

    public static final String FRAGMENT_TAG = "ExitAdvertDialog_Tag";
    private static final String ADVERT_EXIT_STATUS_COMMIT = "2";//强制引流
    private Button init_exit_dialog_advert_commit;
    private String guideType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            guideType = getArguments().getString(Constants.EXIT_GUIDE_TYPE);
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.exit_dialog_advert, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        init_exit_dialog_advert_commit =
                (Button) view.findViewById(R.id.init_exit_dialog_advert_commit);
//        init_exit_dialog_advert_commit.setText(
//                String.format(getString(R.string.init_exit_dialog_advert_commit_text),
//                        String.valueOf(mJumpTime)));
        init_exit_dialog_advert_commit.setOnClickListener(this);
        View exitView = view.findViewById(R.id.init_exit_dialog_advert_cancel);
        if (!TextUtils.isEmpty(guideType) && guideType.equals(ADVERT_EXIT_STATUS_COMMIT)) {
            exitView.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.init_exit_dialog_advert_button_width),
                    (int) getResources().getDimension(
                            R.dimen.init_exit_dialog_advert_button_height));
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            init_exit_dialog_advert_commit.setLayoutParams(params);
        } else {
            exitView.setVisibility(View.VISIBLE);
            exitView.setOnClickListener(this);
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCountDownTimer != null) {
            mCountDownTimer.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.init_exit_dialog_advert_commit) {
            if (mExitAppCallback != null) {
                mExitAppCallback.resultDismiss(false);
            }
        } else if (v.getId() == R.id.init_exit_dialog_advert_cancel) {
            if (mExitAppCallback != null) {
                mExitAppCallback.resultDismiss(true);
            }
        }
        dismiss();
    }

    CountDownTimer mCountDownTimer = new CountDownTimer(15000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            init_exit_dialog_advert_commit.setText(
                    String.format(getString(R.string.init_exit_dialog_advert_commit_text),
                            String.valueOf(millisUntilFinished / 1000)));
        }

        @Override
        public void onFinish() {
            init_exit_dialog_advert_commit.performClick();
        }
    };

    private ExitAppCallback mExitAppCallback = null;

    public interface ExitAppCallback {
        void resultDismiss(boolean isExit);
    }

    public void setExitAppCallback(ExitAppCallback callback) {
        mExitAppCallback = callback;
    }
}
