package com.golive.cinema.adapter;

import static com.golive.cinema.Constants.EXTRA_BASE_PAGE_ID;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import com.golive.cinema.util.FragmentUtils;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by Administrator on 2016/9/23.
 */

public class FragmentAdapter extends FragmentPagerAdapter {

    private final List<Class> CLASSES_TAGS;
    private final int mBasePageId;
    private SparseArray<WeakReference<Fragment>> mFragments = new SparseArray<>();

    public FragmentAdapter(FragmentManager fm, List<Class> class_tags, int basePageId) {
        super(fm);
        this.CLASSES_TAGS = class_tags;
        this.mBasePageId = basePageId;
    }

    @Override
    public Fragment getItem(int position) {
//        Logger.d("getItem, position : " + position);
//        Fragment fragment = FragmentUtils.newFragment(CLASSES_TAGS.get(position));
//        Bundle bundle = new Bundle();
//        bundle.putInt(EXTRA_BASE_PAGE_ID, mBasePageId);
//        fragment.setArguments(bundle);

        Fragment fragment = null;
        // get fragment from cache
        WeakReference<Fragment> weakReference = mFragments.get(position);
        if (weakReference != null) {
            fragment = weakReference.get();
        }

        // no cached fragment
        if (null == fragment) {
            // new fragment
            fragment = FragmentUtils.newFragment(CLASSES_TAGS.get(position));
            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_BASE_PAGE_ID, mBasePageId);
            fragment.setArguments(bundle);

            if (weakReference != null) {
                weakReference.clear();
            }
            weakReference = new WeakReference<>(fragment);
            // cache the new fragment
            mFragments.put(position, weakReference);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        if (CLASSES_TAGS != null) {
            return CLASSES_TAGS.size();
        }

        return 0;
    }
}
