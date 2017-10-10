package com.golive.cinema.advert;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.views.CircleLayoutView;
import com.initialjie.log.Logger;

/**
 * Created by chgang on 2016/12/28.
 */

public class AdvertImageDialog extends AdvertDialog {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentView(R.layout.play_advert_image_dialog);
        mAdvertImageView = (ImageView) view.findViewById(R.id.play_advert_image_iv);
        mCircleLayoutView = (CircleLayoutView) view.findViewById(R.id.play_advert_count_down_view);
        mCircleLayoutView.setOnFinishCallback(this);
        mCircleLayoutView.setVisibility(View.GONE);
        initImages();
    }

    private void initImages() {
        if (null == mAdvert || StringUtils.isNullOrEmpty(mAdvert.getUrl())) {
            hide();
            return;
        }

        String url = mAdvert.getUrl().trim();
        Glide.with(this)
                .load(url)
                .crossFade(200)
                .error(R.drawable.header_logo)
                .centerCrop()
                .priority(Priority.HIGH)
                .into(new GlideDrawableImageViewTarget(mAdvertImageView) {
                    public void onResourceReady(GlideDrawable resource,
                            GlideAnimation<? super GlideDrawable> glideAnimation) {
                        startCircleView();
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        Logger.w(e, "onLoadFailed");
                        hide();
                    }
                });
    }
}
