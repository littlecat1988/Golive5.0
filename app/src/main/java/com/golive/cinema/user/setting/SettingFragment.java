package com.golive.cinema.user.setting;

import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_SETTING;
import static com.golive.cinema.statistics.StatisticsContract.ReportUserBehaviorConstants
        .VIEW_CODE_USER_CENTER;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.golive.cinema.BaseDialog;
import com.golive.cinema.Constants;
import com.golive.cinema.Injection;
import com.golive.cinema.R;
import com.golive.cinema.init.dialog.UpgradeDialog;
import com.golive.cinema.statistics.StatisticsHelper;
import com.golive.cinema.util.PackageUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.cinema.util.ToastUtils;
import com.golive.cinema.util.UIHelper;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.Upgrade;
import com.golive.network.helper.UserInfoHelper;
import com.initialjie.log.Logger;

import java.util.ArrayList;
import java.util.List;


public class SettingFragment extends BaseDialog implements SettingContract.View {

    private static final String FRAG_TAG_ABOUT = "frag_tag_about";
    private static final String KEY_LANGUAGE = "setting_language_key";
    private static final String KEY_HD = "setting_hd_key";
    private static final String KEY_VERSION = "setting_version_key";
    private static final String KEY_ABOUT = "setting_about_key";

    private ProgressDialog mProgressDialog;
    private SettingContract.Presenter mPresenter;
    private ListView mListView;
    private SettingResultAdapter mListAdapter;
    private int mIsNeedUpgrade;
    private long mEnterTime;
    private Upgrade mUpgradeInfo;
    private List<String> mClarityList;
    private List<SettingResultItem> mSettingList;

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (null == mClarityList) {
            mClarityList = new ArrayList<>();
            mClarityList.add(Constants.PLAY_MEDIA_RANK_CLARITY_STANDARD);
            mClarityList.add(Constants.PLAY_MEDIA_RANK_CLARITY_HIGH);
            mClarityList.add(Constants.PLAY_MEDIA_RANK_CLARITY_SUPER);
            mClarityList.add(Constants.PLAY_MEDIA_RANK_CLARITY_1080);
        }

        if (null == mSettingList) {
            mSettingList = new ArrayList<>();
            mSettingList.add(new SettingResultItem(KEY_LANGUAGE,
                    getString(R.string.setting_language) + " :",
                    getString(R.string.setting_chinese),
                    ResourcesCompat.getDrawable(getResources(),
                            R.drawable.sel_user_setting_icon_language, null)));
            String clarityName = getClarityName(String.valueOf(
                    UserInfoHelper.getDefaultDefinition(getContext())));
            mSettingList.add(new SettingResultItem(KEY_HD,
                    getString(R.string.setting_default_hd) + " :",
                    clarityName,
                    ResourcesCompat.getDrawable(getResources(), R.drawable.sel_user_setting_icon_hd,
                            null)));
            mSettingList.add(new SettingResultItem(KEY_VERSION,
                    getString(R.string.setting_version_update) + " :",
                    getString(R.string.setting_version_top_new),
                    ResourcesCompat.getDrawable(getResources(),
                            R.drawable.sel_user_setting_icon_update,
                            null)));
            mSettingList.add(new SettingResultItem(KEY_ABOUT,
                    getString(R.string.setting_about) + " :",
                    getString(R.string.setting_about_into_detail),
                    ResourcesCompat.getDrawable(getResources(),
                            R.drawable.sel_user_setting_icon_about,
                            null)));
        }

        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.user_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = (ListView) view.findViewById(R.id.user_setting_lv_result);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                launchSettingPage(position);
            }
        });
        mListAdapter = new SettingResultAdapter(LayoutInflater.from(getContext()), mSettingList);
        mListView.setAdapter(mListAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mEnterTime = System.currentTimeMillis();
        StatisticsHelper.getInstance(getContext()).reportEnterActivity(
                VIEW_CODE_SETTING, getString(R.string.user_center_tab_set_system),
                VIEW_CODE_USER_CENTER);
        mIsNeedUpgrade = 0;

        Context context = getContext().getApplicationContext();
        mPresenter = new SettingPresenter(this,
                Injection.provideGetMainConfigUseCase(context),
                Injection.provideUpgradeUseCase(context),
                Injection.provideGetKdmVersionUseCase(context),
                Injection.provideSchedulerProvider());
        if (getPresenter() != null) {
            String packageName = context.getPackageName();
            int versionCode = PackageUtils.getVersionCode(context, packageName);
            String versionName = PackageUtils.getVersionName(context, packageName);
            getPresenter().checkUpgrade(context, versionCode, versionName);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d("onDestroy ");
        if (getPresenter() != null) {
            getPresenter().unsubscribe();
        }
        String time = String.valueOf((System.currentTimeMillis() - mEnterTime) / 1000);
        StatisticsHelper.getInstance(getActivity()).reportExitActivity(VIEW_CODE_SETTING
                , getString(R.string.user_center_tab_set_system), "", time);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (!isAdded()) {
            return;
        }

        if (active) {
            if (null == mProgressDialog) {
                mProgressDialog = UIHelper.generateSimpleProgressDialog(getContext(), null,
                        getString(R.string.please_wait));
            }

            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        } else {
            if (mProgressDialog != null) {
                UIHelper.dismissDialog(mProgressDialog);
            }
        }
    }

    @Override
    public void setCheckingUpgradeIndicator(boolean active) {
        if (!isAdded()) {
            return;
        }

        SettingResultItem resultItem = getTheSettingItem(KEY_VERSION);
        if (resultItem != null) {
            resultItem.setCheckingUpgrade(active);
            if (active) {
                resultItem.setResult(getString(R.string.setting_version_checking));
            }
            if (mListAdapter != null) {
                mListAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void setMainConfig(MainConfig cfg) {
        //no need
    }

    @Override
    public void setKdmVersion(String version, String platform) {
        //no need
    }

    @Override
    public void showUpgradeView(final Upgrade upgrade, final int upgradeType) {
        Logger.d("showUpgradeView,upgradeType:" + upgradeType);
        mUpgradeInfo = upgrade;
        SettingResultItem settingItem = getTheSettingItem(KEY_VERSION);
        if (null == settingItem) {
            return;
        }

        String oldresult = settingItem.getResult();
        String topNew = getString(R.string.setting_version_top_new);
        String canUgrade = getString(R.string.setting_version_have_new_upgrade);
        if (Constants.UPGRADE_TYPE_NO_UPGRADE == upgradeType) { // 无需升级
            if (null == oldresult || !topNew.equals(oldresult)) {
                settingItem.setResult(topNew);
                mListAdapter.notifyDataSetChanged();
            }
            mIsNeedUpgrade = 0;
        } else { //强制或可选都进这
            if (null == oldresult || !canUgrade.equals(oldresult)) {
                settingItem.setResult(canUgrade);
                mListAdapter.notifyDataSetChanged();
            }
            mIsNeedUpgrade = 1;
        }

    }

    @Override
    public void setChangeServerKey(String key) {

    }

    @Override
    public void setPresenter(SettingContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @NonNull
    protected SettingContract.Presenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    private void launchSettingPage(int position) {
        if (null == mSettingList || mSettingList.isEmpty()) {
            return;
        }

        String pageId = mSettingList.get(position).getPageId();
        if (StringUtils.isNullOrEmpty(pageId)) {
            return;
        }

        if (KEY_LANGUAGE.equals(pageId)) {
            setLanguageDialog();
        } else if (KEY_HD.equals(pageId)) {
            setHighDefinitionDialog(position);
        } else if (KEY_VERSION.equals(pageId)) {
            SettingResultItem item = mSettingList.get(position);
            if (item != null && item.isCheckingUpgrade()) {
                ToastUtils.showToast(getContext(), getString(R.string.setting_version_checking));
                return;
            }

            if (1 == mIsNeedUpgrade) {
                startDownloadUpgrade(mUpgradeInfo, Constants.UPGRADE_TYPE_AUTO_OPTIONAL_REMOTE);
            } else {
                checkUpgradeDialog();
            }

        } else if (KEY_ABOUT.equals(pageId)) {
            AboutFragment.newInstance().show(getFragmentManager(), FRAG_TAG_ABOUT);
        }
    }

    private void startDownloadUpgrade(Upgrade upgrade, int upgradeType) {
        if (null == upgrade) {
            return;
        }

        UpgradeDialog fragment = UpgradeDialog.newInstance(upgrade.getUrl(), upgradeType);
        fragment.setOnUpgradeListener(new UpgradeDialog.OnUpgradeListener() {
            @Override
            public void onCompleted() {//升级
                Logger.d("startDownloadUpgrade onCompleted");
                mIsNeedUpgrade = 0;
            }

            @Override
            public void onCancel() {
                Logger.d("startDownloadUpgrade onCancel");
            }

            @Override
            public void onExit() {
                Logger.d("startDownloadUpgrade onExit");
            }
        });
        fragment.show(getFragmentManager(), "upgrade_fragment");
    }

    private void setLanguageDialog() {
        final Dialog aDialog = getSettingDialog(getContext(), R.layout.user_setting_language);
        TextView title = (TextView) aDialog.findViewById(R.id.user_language_title_tv);
        title.setText(getActivity().getString(R.string.setting_language_choice));
        ListView languageLv = (ListView) aDialog.findViewById(R.id.user_lv_setting_language);
        List<String> list = new ArrayList<>();
        list.add(getActivity().getString(R.string.setting_chinese));
        LanguageAdapter listadpter = new LanguageAdapter(getContext(), list, false);
        languageLv.setAdapter(listadpter);
        languageLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                getTheSettingItem(KEY_LANGAGE).setResult("中  文");
                aDialog.dismiss();
            }
        });
        languageLv.setPadding(0, 80, 0, 0);
        aDialog.show();
    }

    private void setHighDefinitionDialog(final int position) {
        final Dialog aDialog = getSettingDialog(getContext(), R.layout.user_setting_language);
        TextView title = (TextView) aDialog.findViewById(R.id.user_language_title_tv);
        title.setText(getActivity().getString(R.string.setting_language_default_hd));
        ListView languageLv = (ListView) aDialog.findViewById(R.id.user_lv_setting_language);
        LanguageAdapter lAdapter = new LanguageAdapter(getContext(), mClarityList, true);
        languageLv.setAdapter(lAdapter);
        int clarity = UserInfoHelper.getDefaultDefinition(getContext());
        languageLv.setSelection(getClarityPosition(String.valueOf(clarity)));
        languageLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserInfoHelper.setDefaultDefinition(getActivity(),
                        Integer.parseInt(mClarityList.get(position)));
                String clarityName = getClarityName(mClarityList.get(position));
                SettingResultItem settingItem = getTheSettingItem(KEY_HD);
                if (settingItem != null) {
                    settingItem.setResult(clarityName);
                }
                mListAdapter.notifyDataSetChanged();
                aDialog.dismiss();
            }
        });
        aDialog.show();
    }

    private void checkUpgradeDialog() {
        Context context = getContext();
        final Dialog aDialog = getSettingDialog(context, R.layout.user_setting_dl_no_upgrade);
        TextView title = (TextView) aDialog.findViewById(R.id.user_setting_dl_no_upgrade_title_tv);
        title.setText(getString(R.string.setting_version_update));
        TextView version = (TextView) aDialog.findViewById(R.id.user_setting_dl_no_upgrade_tv1);
        version.setText(getString(R.string.setting_version_top_new));
        TextView detail1 = (TextView) aDialog.findViewById(R.id.user_setting_dl_no_upgrade_tv2);
        String versionName = PackageUtils.getVersionName(context, context.getPackageName());
        detail1.setText(String.format(getString(R.string.setting_version_curr), versionName));
        Button btn = (Button) aDialog.findViewById(R.id.user_setting_dl_no_upgrade_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aDialog.dismiss();
            }
        });
        aDialog.show();
    }

    private Dialog getSettingDialog(Context context, final int layoutId) {
        Dialog aDialog = new Dialog(context, R.style.style_dialog_base);
        aDialog.setContentView(layoutId);
        if (aDialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams params = aDialog.getWindow().getAttributes();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            params.width = wm.getDefaultDisplay().getWidth();
            params.height = wm.getDefaultDisplay().getHeight() + 60;
            params.x = 0;
            params.y = 0;
            aDialog.getWindow().setAttributes(params);
            aDialog.getWindow().setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        }
        return aDialog;
    }

    private SettingResultItem getTheSettingItem(final String pageId) {
        if (null == mSettingList || mSettingList.isEmpty()
                || StringUtils.isNullOrEmpty(pageId)) {
            return null;
        }

        SettingResultItem item;
        for (int i = 0; i < mSettingList.size(); i++) {
            item = mSettingList.get(i);
            String id = item.getPageId();
            if (!StringUtils.isNullOrEmpty(id) && pageId.equals(id)) {
                return item;
            }
        }

        return null;
    }

    private int getClarityPosition(String clarity) {
        if (StringUtils.isNullOrEmpty(clarity)) {
            return 0;
        }
        if (null == mClarityList || mClarityList.isEmpty()) {
            return 0;
        }

        for (int i = 0; i < mClarityList.size(); i++) {
            String c = mClarityList.get(i);
            if (!StringUtils.isNullOrEmpty(c) && clarity.equals(c)) {
                return i;
            }
        }
        return 0;
    }

    private String getClarityName(String clarity) {
        if (!StringUtils.isNullOrEmpty(clarity)) {
            switch (clarity) {
                case Constants.PLAY_MEDIA_RANK_CLARITY_SUPER:
                    return getString(R.string.theatre_play_clarity_super_text);
                case Constants.PLAY_MEDIA_RANK_CLARITY_STANDARD:
                    return getString(R.string.theatre_play_clarity_standard_text);
                case Constants.PLAY_MEDIA_RANK_CLARITY_HIGH:
                    return getString(R.string.theatre_play_clarity_high_text);
                case Constants.PLAY_MEDIA_RANK_CLARITY_1080:
                    return getString(R.string.theatre_play_clarity_1080p_text);
            }
        }

        return getString(R.string.theatre_play_clarity_high_text);
    }
}
