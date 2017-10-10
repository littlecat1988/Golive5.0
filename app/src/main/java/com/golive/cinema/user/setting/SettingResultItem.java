package com.golive.cinema.user.setting;

import android.graphics.drawable.Drawable;

/**
 * Created by Mowl on 2016/11/4.
 */

public class SettingResultItem {
    private String name;
    private String result;
    private String pageId;
    private Drawable icon;
    private boolean checkingUpgrade;


    public SettingResultItem(String id, String name, String result, Drawable drawable) {
        this.pageId = id;
        this.name = name;
        this.result = result;
        this.icon = drawable;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String val) {
        this.pageId = val;
    }

    public Drawable getDrawable() {
        return icon;
    }

    public void setDrawable(Drawable val) {
        this.icon = val;
    }

    public boolean isCheckingUpgrade() {
        return checkingUpgrade;
    }

    public void setCheckingUpgrade(boolean checkingUpgrade) {
        this.checkingUpgrade = checkingUpgrade;
    }
}
