package com.golive.cinema.user.myinfo;

import android.graphics.drawable.Drawable;

/**
 * Created by Mowl on 2016/11/4.
 */

public class MyinfoItem {
    private String name;
    private String result;
    private String keyId;
    private Drawable icon;


    MyinfoItem(String key, String name, Drawable drawable) {
        this.keyId = key;
        this.name = name;
        this.icon = drawable;
    }

    public MyinfoItem() {

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

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String val) {
        this.keyId = val;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable val) {
        this.icon = val;
    }


}
