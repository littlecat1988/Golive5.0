package com.golive.cinema;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.initialjie.log.Logger;

/**
 * Created by Administrator on 2016/10/31.
 */

public class BaseDialog extends DialogFragment {

    public interface OnDismissListener {
        void onDialogDismiss(boolean exit);
    }

    public OnDismissListener mOnDismissListener;

    private DialogInterface.OnCancelListener mOnDialogCancelListener;
    private DialogInterface.OnDismissListener mOnDialogDismissListener;

    private int mBackgroundResId = -1;

    public void setDismissListener(OnDismissListener dismissListener) {
        this.mOnDismissListener = dismissListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.dialog_fullscreen_base);
    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
//        int resId = getBackgroundRes();
//        dialog.getWindow().setBackgroundDrawableResource(resId);
//        return dialog;
//    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int resId = getBackgroundRes();
        view.setBackgroundResource(resId);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Logger.d("onCancel");
        if (mOnDialogCancelListener != null) {
            mOnDialogCancelListener.onCancel(dialog);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Logger.d("onDismiss");
        if (mOnDialogDismissListener != null) {
            mOnDialogDismissListener.onDismiss(dialog);
        }
    }

    protected void setBackgroundResId(int backgroundResId) {
        mBackgroundResId = backgroundResId;
    }

    private int getBackgroundRes() {
        return mBackgroundResId != -1 ? mBackgroundResId : R.drawable.homg_bg;
    }

    public void setOnDialogCancelListener(
            DialogInterface.OnCancelListener onDialogCancelListener) {
        mOnDialogCancelListener = onDialogCancelListener;
    }

    public void setOnDialogDismissListener(
            DialogInterface.OnDismissListener onDialogDismissListener) {
        mOnDialogDismissListener = onDialogDismissListener;
    }
}
