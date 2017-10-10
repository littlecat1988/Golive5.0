package com.golive.cinema.init;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.golive.cinema.Constants;
import com.golive.cinema.MvpFragment;
import com.golive.cinema.R;
import com.golive.cinema.advert.AdvertDialog;
import com.golive.cinema.advert.AdvertImageDialog;
import com.golive.cinema.advert.AdvertMediaDialog;
import com.golive.cinema.init.dialog.CommonAlertDialog;
import com.golive.cinema.init.dialog.RepeatMacDialog;
import com.golive.cinema.init.dialog.UpgradeDialog;
import com.golive.cinema.util.ActivityUtils;
import com.golive.cinema.util.FragmentUtils;
import com.golive.cinema.util.NetworkUtils;
import com.golive.cinema.util.PackageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.ToastUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.Ad;
import com.golive.network.entity.Login;
import com.golive.network.entity.ServerMessage;
import com.golive.network.entity.Upgrade;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.hw.util.DeviceUtil;
import com.initialjie.log.Logger;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by chgang on 2016/10/28.
 */

public class InitFragment extends MvpFragment implements InitContract.View {

    private final static String FRAG_TAG_AD_MEDIA = "frag_tag_ad_media";
    private final static String FRAG_TAG_AD_IMAGE = "frag_tag_ad_image";

    private InitContract.Presenter mPresenter;

//    private static final int DELAYED_TIME = 1000;

    private boolean mLoginSuccess;
    private boolean mShowingAdvert;

    private BroadcastReceiver mBroadcastReceiver = null;
    private BroadcastReceiver mRestartBroadcastReceiver = null;

    private View mAdvertFragmentLayout;
    private RelativeLayout mSurfaceLayout;
    private ProgressDialog mPlayerUpgradingDlg;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Logger.d("onCreate:");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.init_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAdvertFragmentLayout = view.findViewById(R.id.advert_video_fragment_layout);
        mAdvertFragmentLayout.setVisibility(View.INVISIBLE);
        mSurfaceLayout = (RelativeLayout) view.findViewById(R.id.parent_init_video_frag);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (NetworkUtils.isNetworkAvailable(getContext())) {
            View decorView = getActivity().getWindow().getDecorView();
            decorView.post(new Runnable() {
                @Override
                public void run() {
                    if (!isActive()) {
                        return;
                    }

                    InitContract.Presenter presenter = getPresenter();
                    if (presenter != null) {
                        presenter.init();
                    }
                }
            });
        } else {
            initBroadcastReceiver();
            CommonAlertDialog fragment = FragmentUtils.newFragment(CommonAlertDialog.class);
            Bundle bundle = new Bundle();
            bundle.putInt(CommonAlertDialog.DIALOG_TAG, CommonAlertDialog.MESSAGE_TYPE_NETWORK);
            fragment.setArguments(bundle);
            fragment.setCommonCallback(new CommonAlertDialog.CommonCallback() {
                @Override
                public void resultDismiss(boolean isExit) {
                    if (isExit) {
                        exit();
                    }
                }
            });
            String tag = CommonAlertDialog.DIALOG_FRAGMENT_TAG;
            showFragment(fragment, tag);
            Logger.d("no net work!!!!!");
        }

        initRestartBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy");
        if (mRestartBroadcastReceiver != null) {
            getContext().unregisterReceiver(mRestartBroadcastReceiver);
            mRestartBroadcastReceiver = null;
        }

        if (mBroadcastReceiver != null) {
            getContext().unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    @Override
    public Observable<Boolean> showConfirmAdvertView(final Ad ad) {
        Logger.d("showConfirmAdvertView");
        if (null == ad) {
            disms();
            return Observable.just(false);
        }

        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                AdvertDialog advertDialog = null;
                String tag = null;
                Bundle bundle = new Bundle();
                String type = ad.getType();
                if (!StringUtils.isNullOrEmpty(type)) {
                    if (Constants.ADVER_TYPE_IMAGE.equals(type)) {
                        tag = FRAG_TAG_AD_IMAGE;
                        if (null == advertDialog) {
                            advertDialog = FragmentUtils.newFragment(AdvertImageDialog.class);
                        }
                    } else if (Constants.ADVER_TYPE_VIDEO.equals(type)) {
                        tag = FRAG_TAG_AD_MEDIA;
                        if (null == advertDialog) {
                            // surfaceview bug
                            SurfaceView surfaceView = new SurfaceView(getActivity());
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                                    RelativeLayout.LayoutParams.WRAP_CONTENT);
                            params.addRule(RelativeLayout.CENTER_IN_PARENT);
                            mSurfaceLayout.addView(surfaceView, params);
                            advertDialog = FragmentUtils.newFragment(AdvertMediaDialog.class);
                        }
                    }
                }

                if (advertDialog != null && tag != null) {
                    showAdvertCompleted(false);
                    bundle.putInt(Constants.PLAYER_INTENT_BOOT_ADVERT,
                            Constants.AD_REQUEST_TYPE_BOOT);
                    bundle.putSerializable(Constants.PLAYER_INTENT_MEDIA_ADVERT, ad);
                    advertDialog.setArguments(bundle);
                    final View versionView = getActivity().findViewById(R.id.init_version_text);
                    advertDialog.setOnAdvertCallback(new AdvertDialog.AdvertCallback() {
                        @Override
                        public void onExit(boolean isExit, boolean isFinish) {
                            if (isExit) {
                                exit();
                            } else {
                                disms();
                            }
                            if (versionView != null) {
                                versionView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                    advertDialog.setOnDialogDismissListener(
                            new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    showAdvertCompleted(true);
                                    if (!subscriber.isUnsubscribed()) {
                                        subscriber.onNext(true);
                                        subscriber.onCompleted();
                                    }
                                }
                            });
                    FragmentUtils.removePreviousFragment(getFragmentManager(), tag);
                    advertDialog.show(getFragmentManager(), tag);
                    if (versionView != null) {
                        versionView.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    public void showAdvertCompleted(boolean active) {
        mShowingAdvert = active;
        if (active) {
            if (mAdvertFragmentLayout != null) {
                mAdvertFragmentLayout.setVisibility(View.VISIBLE);
            }
        } else {
            if (mAdvertFragmentLayout != null) {
                mAdvertFragmentLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public Observable<Boolean> showServerStop(final ServerMessage serverMessage) {
        //停机维护
        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {

            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                if (serverMessage != null && !StringUtils.isNullOrEmpty(
                        serverMessage.getContent())) {
                    CommonAlertDialog fragment = FragmentUtils.newFragment(CommonAlertDialog.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(CommonAlertDialog.DIALOG_TAG,
                            CommonAlertDialog.MESSAGE_TYPE_SERVER_STOP);
                    bundle.putString(CommonAlertDialog.DIALOG_MSG_TAG, serverMessage.getContent());
                    bundle.putString(CommonAlertDialog.DIALOG_TITLE_TAG, serverMessage.getName());
                    fragment.setArguments(bundle);
                    final String tag = CommonAlertDialog.DIALOG_FRAGMENT_TAG;
                    fragment.setCommonCallback(new CommonAlertDialog.CommonCallback() {
                        @Override
                        public void resultDismiss(boolean isExit) {
                            if (isExit) {
                                exit();
                            } else {
                                subscriber.onNext(false);
                                FragmentUtils.removePreviousFragment(getFragmentManager(),
                                        tag);
                            }
                            subscriber.onCompleted();
                        }
                    });

                    showFragment(fragment, tag);
                } else {
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                }
            }
        };

        return Observable.create(onSubscribe);
    }

    @Override
    public Observable<Boolean> showUpgradeView(final Upgrade upgrade, final int upgradeType) {
        Logger.d("showUpgradeView:" + upgradeType);
        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }
                if (upgradeType == Constants.UPGRADE_TYPE_OPTIONAL_FORCE
                        || upgradeType == Constants.UPGRADE_TYPE_OPTIONAL_REMOTE) {
                    final CommonAlertDialog aDialog = FragmentUtils.newFragment(
                            CommonAlertDialog.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(CommonAlertDialog.DIALOG_TAG,
                            CommonAlertDialog.MESSAGE_TYPE_UPGRADE_SHOP);
                    bundle.putString(CommonAlertDialog.DIALOG_MSG_TAG, String.valueOf(upgradeType));
                    aDialog.setArguments(bundle);
                    final String dialogFragmentTag = CommonAlertDialog.DIALOG_FRAGMENT_TAG;
                    aDialog.setCommonCallback(new CommonAlertDialog.CommonCallback() {
                        @Override
                        public void resultDismiss(boolean isExit) {
                            if (isExit) {
                                exit();
                                subscriber.onNext(true);
                            } else {
                                subscriber.onNext(false);
                                FragmentUtils.removePreviousFragment(getFragmentManager(),
                                        dialogFragmentTag);
                            }
                            subscriber.onCompleted();
                        }
                    });
                    showFragment(aDialog, dialogFragmentTag);
                } else if (upgradeType == Constants.UPGRADE_TYPE_AUTO_OPTIONAL_REMOTE
                        || upgradeType == Constants.UPGRADE_TYPE_AUTO_OPTIONAL_FORCE) {
                    UpgradeDialog fragment = UpgradeDialog.newInstance(upgrade.getUrl(),
                            upgradeType);
                    final String dialogFragmentTag = UpgradeDialog.DIALOG_FRAGMENT_TAG;
                    fragment.setOnUpgradeListener(new UpgradeDialog.OnUpgradeListener() {
                        @Override
                        public void onCompleted() {
                            //升级
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(true);
                                subscriber.onCompleted();
                            }
                            FragmentUtils.removePreviousFragment(getFragmentManager(),
                                    dialogFragmentTag);
                        }

                        @Override
                        public void onCancel() {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(false);
                                subscriber.onCompleted();
                            }
                            FragmentUtils.removePreviousFragment(getFragmentManager(),
                                    dialogFragmentTag);

                        }

                        @Override
                        public void onExit() {
                            exit();
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onCompleted();
                            }
                        }
                    });
                    ActivityUtils.addFragmentToActivity(getFragmentManager(), fragment,
                            R.id.advert_video_fragment_layout, dialogFragmentTag);
                } else {
                    subscriber.onNext(false);
                    subscriber.onCompleted();
                }
            }
        };

        return Observable.create(onSubscribe);
    }

    @Override
    public String getMacAddress() {
        return DeviceUtil.getMacAddress(getContext());
    }

    @Override
    public Observable<Boolean> showMacInvalidView(final String status) {
        Observable.OnSubscribe<Boolean> onSubscribe = new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(final Subscriber<? super Boolean> subscriber) {
                RepeatMacDialog frag = FragmentUtils.newFragment(RepeatMacDialog.class);
                Bundle bundle = new Bundle();
                bundle.putString(RepeatMacDialog.PHONE_MAC_STATUS, status);
                frag.setArguments(bundle);
                final String tag = RepeatMacDialog.FRAGMENT_TAG;
                frag.setOnHiddenListener(new RepeatMacDialog.HiddenListener() {
                    @Override
                    public void onCompleted() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(true);
                            subscriber.onCompleted();
                        }
                        FragmentUtils.removePreviousFragment(getFragmentManager(),
                                tag);
                    }

                    @Override
                    public void onExit() {
                        exit();
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                });
                showFragment(frag, tag);
            }
        };
        return Observable.create(onSubscribe);
    }

    @Override
    public void showLoginView(Login login) {
        mLoginSuccess = login != null && login.isOk();
    }

    @Override
    public void showLoginFailedView(String note) {
        if (!StringUtils.isNullOrEmpty(note)) {
            CommonAlertDialog fragment = FragmentUtils.newFragment(CommonAlertDialog.class);
            Bundle bundle = new Bundle();
            bundle.putInt(CommonAlertDialog.DIALOG_TAG, CommonAlertDialog.MESSAGE_TYPE_LOGIN_ERROR);
            bundle.putString(CommonAlertDialog.DIALOG_MSG_TAG, note);
            fragment.setArguments(bundle);
            fragment.setCommonCallback(new CommonAlertDialog.CommonCallback() {
                @Override
                public void resultDismiss(boolean isExit) {
                    exit();
                }
            });
            String tag = CommonAlertDialog.DIALOG_FRAGMENT_TAG;
            showFragment(fragment, tag);

            mLoginSuccess = false;
        }
    }

    @Override
    public void showCompleted() {
        Logger.d("mShowingAdvert : " + mShowingAdvert + ", mLoginSuccess : " + mLoginSuccess);
        if (!mShowingAdvert && mLoginSuccess) {
            ((SplashActivity) getActivity()).close();
            mLoginSuccess = false;
        }
    }

    @Override
    public void showInitFailed(String msg) {
        Logger.w("showInitFailed, msg:" + msg);
//        String message = getString(R.string.init_failed);
//        if (!StringUtils.isNullOrEmpty(msg)) {
//            message += ", " + msg;
//        }
//        ToastUtils.showToast(getContext(), message);

//        showAdvertCompleted(true);
        CommonAlertDialog fragment = FragmentUtils.newFragment(CommonAlertDialog.class);
        Bundle bundle = new Bundle();
        bundle.putInt(CommonAlertDialog.DIALOG_TAG, CommonAlertDialog.MESSAGE_TYPE_INIT_FAILED);
        bundle.putString(CommonAlertDialog.DIALOG_MSG_TAG, msg);
        fragment.setCommonCallback(new CommonAlertDialog.CommonCallback() {
            @Override
            public void resultDismiss(boolean isExit) {
                exit();
            }
        });
        fragment.setArguments(bundle);
        String tag = CommonAlertDialog.DIALOG_FRAGMENT_TAG;
        showFragment(fragment, tag);
    }

    @Override
    public void showServerTimeout() {
//        showAdvertCompleted(true);
        CommonAlertDialog fragment = FragmentUtils.newFragment(CommonAlertDialog.class);
        Bundle bundle = new Bundle();
        if (NetworkUtils.isNetworkAvailable(getActivity())) {
            bundle.putInt(CommonAlertDialog.DIALOG_TAG,
                    CommonAlertDialog.MESSAGE_TYPE_NETWORK_TIMEOUT);
        } else {
            bundle.putInt(CommonAlertDialog.DIALOG_TAG, CommonAlertDialog.MESSAGE_TYPE_NETWORK);
        }
        fragment.setArguments(bundle);
        fragment.setCommonCallback(new CommonAlertDialog.CommonCallback() {
            @Override
            public void resultDismiss(boolean isExit) {
                exit();
            }
        });
        String tag = CommonAlertDialog.DIALOG_FRAGMENT_TAG;
        showFragment(fragment, tag);
    }

    @Override
    public void showClearUserCache() {
        Logger.e("showClearUserCache");
        UserInfoHelper.clearUserInfoCache(getContext());
    }

    @Override
    public int getVersionCode() {
        return PackageUtils.getVersionCode(getActivity(), getActivity().getPackageName());
    }

    @Override
    public String getVersionName() {
        return PackageUtils.getVersionName(getActivity(), getActivity().getPackageName());
    }

    @Override
    public void setPlayerUpgrading(boolean active) {
        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mPlayerUpgradingDlg) {
                mPlayerUpgradingDlg = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.player_upgrading));
            }

            if (!mPlayerUpgradingDlg.isShowing()) {
                mPlayerUpgradingDlg.show();
            }
        } else {
            if (mPlayerUpgradingDlg != null) {
                UIHelper.dismissDialog(mPlayerUpgradingDlg);
            }
        }
    }

    @Override
    public void showPlayerUpgradeSuccess() {
        ToastUtils.showToast(getContext(), getString(R.string.player_upgrade_success));
    }

    @Override
    public void showPlayerUpgradeFailed(int errCode) {
        String msg = getString(R.string.player_upgrade_failed) + ", " + errCode;
        ToastUtils.showToast(getContext(), msg);
    }

    @Override
    public void showKdmType(String kdmType) {
        Logger.d("showKdmType, kdmType : " + kdmType);
        UserInfoHelper.setProductType(getContext(), kdmType);
    }

    @Override
    public void setPresenter(InitContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    protected InitContract.Presenter getPresenter() {
        return mPresenter;
    }

    private void initRestartBroadcastReceiver() {
        if (mRestartBroadcastReceiver == null) {
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(Constants.INIT_NETWORK_RESTART_BROADCAST_ACTION);
            mRestartBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (!StringUtils.isNullOrEmpty(action)
                            && Constants.INIT_NETWORK_RESTART_BROADCAST_ACTION.equals(action)) {
                        FragmentUtils.removePreviousFragment(getFragmentManager(),
                                CommonAlertDialog.DIALOG_FRAGMENT_TAG);
                        InitContract.Presenter presenter = getPresenter();
                        if (presenter != null) {
                            presenter.init();
                        }
                    }
                }
            };
            getContext().registerReceiver(mRestartBroadcastReceiver, mIntentFilter);
        }
    }

    private void initBroadcastReceiver() {
        if (mBroadcastReceiver == null) {
            IntentFilter mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (!StringUtils.isNullOrEmpty(action) &&
                            ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                        ConnectivityManager cm = NetworkUtils.getConnectivityManager(getContext());
                        NetworkInfo info = cm.getActiveNetworkInfo();
                        // network is available
                        if (info != null && info.isAvailable()) {
                            InitContract.Presenter presenter = getPresenter();
                            if (presenter != null) {
                                presenter.init();
                            }
                        }
                    }
                }
            };
            getContext().registerReceiver(mBroadcastReceiver, mIntentFilter);
        }
    }

    private void showFragment(Fragment fragment, String tag) {
        if (mAdvertFragmentLayout != null) {
            mAdvertFragmentLayout.setVisibility(View.VISIBLE);
        }
        // replaceFragment
        FragmentUtils.replace(getFragmentManager(),
                R.id.advert_video_fragment_layout,
                fragment,
                tag
        );
    }

    private synchronized void disms() {
        Logger.d("disms");
//        showAdvertCompleted(true);
        mShowingAdvert = false;
        if (mLoginSuccess) {
            if (getActivity() instanceof SplashActivity) {
                SplashActivity sActivity = ((SplashActivity) getActivity());
                sActivity.getWindow().setBackgroundDrawable(null);
                sActivity.close();
            }
        }
    }

    private void exit() {
        if (getActivity() != null && getActivity() instanceof SplashActivity) {
            ((SplashActivity) getActivity()).exit();
        }
    }

}
