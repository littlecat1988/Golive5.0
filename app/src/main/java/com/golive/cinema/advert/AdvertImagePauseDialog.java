package com.golive.cinema.advert;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.golive.cinema.R;
import com.initialjie.log.Logger;

/**
 * Created by chgang on 2017/3/13.
 */

public class AdvertImagePauseDialog extends AdvertDialog implements View.OnClickListener {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentView(R.layout.play_advert_image_pause_dialog);
        mAdvertImagePauseView = (ImageView) view.findViewById(R.id.play_advert_image_pause_iv);
        mAdvertImagePauseView.setFocusable(true);
        mAdvertImagePauseView.setFocusableInTouchMode(true);
        mAdvertImagePauseView.setClickable(true);
        mAdvertImagePauseView.requestFocus();
        mAdvertImagePauseView.setOnClickListener(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initImages();
    }

    private void initImages() {
        if (mAdvert == null || mAdvert.getUrl() == null) {
            hide();
            return;
        }

        Glide.with(this)
                .load(mAdvert.getUrl().trim())
                .crossFade(200)
                .error(R.drawable.header_logo)
                .fitCenter()
                .priority(Priority.HIGH)
                .into(new GlideDrawableImageViewTarget(mAdvertImagePauseView) {
                    public void onResourceReady(GlideDrawable resource,
                            GlideAnimation<? super GlideDrawable> glideAnimation) {
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        Logger.w(e, "onLoadFailed");
                        hide();
                    }
                });
    }

    @Override
    public void onClick(View v) {
        Logger.d("onClick");
        hide();
    }
}
