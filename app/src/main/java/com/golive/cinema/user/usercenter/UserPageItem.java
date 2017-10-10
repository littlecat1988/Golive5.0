package com.golive.cinema.user.usercenter;

import android.graphics.drawable.Drawable;

/**
 * Created by Mowl on 2016/11/2.
 */

public class UserPageItem {
    private String name;
    private Class intoClass;
    private Drawable drawable;

    public void setName(String val) {
        this.name = val;
    }

    public String getName() {
        return this.name;
    }

    public void setIntoClass(Class val) {
        this.intoClass = val;
    }

    public Class<?> getIntoClass() {
        return this.intoClass;
    }

    public void setDrawable(Drawable val) {
        this.drawable = val;
    }

    public Drawable getDrawable() {
        return this.drawable;
    }


}
