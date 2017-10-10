package com.golive.cinema.data;

import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.network.entity.ActivityImage;
import com.golive.network.entity.BootImage;
import com.golive.network.entity.DrainageInfo;
import com.golive.network.entity.GuideTypeInfo;
import com.golive.network.entity.RepeatMac;
import com.golive.network.entity.ServerMessage;
import com.golive.network.entity.Upgrade;
import com.golive.network.response.ApplicationPageResponse;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by Wangzj on 2016/11/5.
 */

public class FakeServerInitDataSource implements ServerInitDataSource {

    private static FakeServerInitDataSource INSTANCE;
    private final List<ServerMessage> mServerMessageList;
    private final ServerMessage mServerMessage;
    private final BootImage mBootImage;
    private final Upgrade mUpgrade;
    private final RepeatMac mRepeatMac;
    private final ActivityImage mActivityImage;

    private FakeServerInitDataSource() {
        mServerMessageList = new ArrayList<>();
        mServerMessage = new ServerMessage();
        mBootImage = new BootImage();
        mUpgrade = new Upgrade();
        mRepeatMac = new RepeatMac();
        mActivityImage = new ActivityImage();
    }

    public static FakeServerInitDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FakeServerInitDataSource();
        }
        return INSTANCE;
    }

    @Override
    public Observable<List<ServerMessage>> queryServerMessages() {
        return Observable.just(mServerMessageList);
    }

    @Override
    public Observable<ServerMessage> getErrorServerMessage() {
        return Observable.just(mServerMessage);
    }

    @Override
    public Observable<BootImage> queryBootImage() {
        return Observable.just(mBootImage);
    }

    @Override
    public Observable<Upgrade> upgrade(String code, String name) {
        return Observable.just(mUpgrade);
    }

    @Override
    public Observable<RepeatMac> queryRepeatMacStatus(String phone) {
        return Observable.just(mRepeatMac);
    }

    @Override
    public Observable<ActivityImage> queryActivityPoster() {
        return Observable.just(mActivityImage);
    }

    @Override
    public Observable<ApplicationPageResponse> queryApplicationPageAction() {
        return Observable.just(new ApplicationPageResponse());
    }

    @Override
    public Observable<GuideTypeInfo> queryGuideTypeInfo() {
        return Observable.empty();
    }

    @Override
    public Observable<DrainageInfo> queryDrainageInfo() {
        return Observable.empty();
    }
}
