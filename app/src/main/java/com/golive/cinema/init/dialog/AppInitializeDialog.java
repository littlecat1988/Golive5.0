package com.golive.cinema.init.dialog;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.golive.cinema.BaseDialog;
import com.golive.cinema.R;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.views.CircleLayoutView;
import com.initialjie.log.Logger;

/**
 * APP启动图片页面
 *
 * @author chengang
 */
public class AppInitializeDialog extends BaseDialog implements CircleLayoutView.OnFinishCallback {

    /**
     * 展示类型: 1- 打开APK； 2- 进入影片详情页； 3- 进入充值页； 4- 进入套餐购买页； 5- 进入精彩片花； 6- 进入我的信息；
     * 7- 进入我的钱包； 8- 进入问题反馈； 9- 进入帮助；
     */
    public static final int OPEN_APK = 1;
    public static final int OPEN_DETAIL = 2;
    public static final int OPEN_CHARGE = 3;
    public static final int OPEN_BUYVIP = 4;
    public static final int OPEN_TRAILER = 5;
    public static final int OPEN_MYINFO = 6;
    public static final int OPEN_MYWALLET = 7;
    public static final int OPEN_FEEDBACK = 8;
    public static final int OPEN_HELP = 9;

//	private static final String APP_IMAGE_FILE_NAME = "/appInitImage";
//	private static final String ACTIVITY_PREF_NUMBER = "activity_pref_num";
//	private static final String ACTIVITY_PREF_DATE = "activity_pref_date";
//	private static final String DATE_FORMAT = "yyyy-MM-dd";

    private CircleLayoutView circleLayoutView;
    private ImageView initImageView;
    private TextView initTextView;

    private AppInitializeCallback appInitializeCallback = null;

    public interface AppInitializeCallback {
        void dialogDismiss(int cate);
    }

    public AppInitializeDialog setAppInitializeCallback(
            AppInitializeCallback appInitializeCallback) {
        this.appInitializeCallback = appInitializeCallback;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.init_boot_image_dialog, container, false);
        initImageView = (ImageView) view.findViewById(R.id.init_image_iv);
        circleLayoutView = (CircleLayoutView) view.findViewById(R.id.count_down_view);
        circleLayoutView.setOnFinishCallback(this);
        circleLayoutView.setVisibility(View.GONE);
        initTextView = (TextView) view.findViewById(R.id.init_image_text);
        initTextView.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        initImages();
        super.onViewCreated(view, savedInstanceState);
    }

    private void initImages() {
        final int category = Integer.parseInt(getArguments().getString("emerge_category", "1"));
        switch (category) {
            case OPEN_DETAIL:
                initTextView.setText(R.string.activity_layout_describle_text_detail);
                break;
            case OPEN_FEEDBACK:
                initTextView.setText(R.string.activity_layout_describle_text_feedback);
                break;
            case OPEN_HELP:
                initTextView.setText(R.string.activity_layout_describle_text_help);
                break;
            case OPEN_MYINFO:
                initTextView.setText(R.string.activity_layout_describle_text_myinfo);
                break;
            case OPEN_MYWALLET:
                initTextView.setText(R.string.activity_layout_describle_text_mywallet);
                break;
            case OPEN_TRAILER:
                initTextView.setText(R.string.activity_layout_describle_text_trailer);
                break;
            case OPEN_CHARGE:
                initTextView.setText(R.string.activity_layout_describle_text_charge);
                break;
            case OPEN_BUYVIP:
                initTextView.setText(R.string.activity_layout_describle_text_buyvip);
                break;
            default:
                break;

        }

        String posterUrl = null;
        if (getArguments() != null) {
            posterUrl = getArguments().getString("poster_url");
        }
        Glide.with(this)
                .load(posterUrl)
                .crossFade(200)
                .error(R.drawable.header_logo)
                .centerCrop()
                .priority(Priority.HIGH)
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource,
                            GlideAnimation<? super GlideDrawable> glideAnimation) {
//                        Logger.d("onResourceReady--------------1");
                        initImageView.setImageDrawable(resource);
                        String prompt = getArguments().getString("prompt_time", "15");
                        if (!StringUtils.isNullOrEmpty(prompt)) {
                            circleLayoutView.startCircle(Integer.parseInt(prompt));
                        }
                        circleLayoutView.setVisibility(View.VISIBLE);
                        initTextView.setVisibility(View.VISIBLE);

                        setCancelable(true);
                        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {

                            @Override
                            public boolean onKey(DialogInterface dialog, int keyCode,
                                    KeyEvent event) {

                                if ((keyCode == KeyEvent.KEYCODE_BACK
                                        || keyCode == KeyEvent.KEYCODE_ESCAPE)
                                        && event.getRepeatCount() == 0) {
                                    initImageView = null;
                                    circleLayoutView = null;
                                    dismiss();
                                    if (appInitializeCallback != null) {
                                        appInitializeCallback.dialogDismiss(OPEN_APK);
                                    }
                                } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                                        || keyCode == KeyEvent.KEYCODE_ENTER) {
                                    initImageView = null;
                                    circleLayoutView = null;
                                    dismiss();
                                    if (appInitializeCallback != null) {
                                        appInitializeCallback.dialogDismiss(category);
                                    }
                                }

                                return true;
                            }
                        });

                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        Logger.w(e, "initImages, onLoadFailed : ");
//                        Logger.d("onLoadFailed--------------1");
                        initImageView = null;
                        circleLayoutView = null;
                        dismiss();
                        if (appInitializeCallback != null) {
                            appInitializeCallback.dialogDismiss(
                                    OPEN_APK);
                        }
                    }
                });
    }

    @Override
    public void onFinish() {
        initImageView = null;
        circleLayoutView = null;
        if (isResumed()) {
            dismiss();
        }
        if (appInitializeCallback != null) appInitializeCallback.dialogDismiss(OPEN_APK);
    }
}
