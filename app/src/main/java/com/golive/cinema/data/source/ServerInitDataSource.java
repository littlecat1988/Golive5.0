package com.golive.cinema.data.source;

import com.golive.network.entity.ActivityImage;
import com.golive.network.entity.BootImage;
import com.golive.network.entity.DrainageInfo;
import com.golive.network.entity.GuideTypeInfo;
import com.golive.network.entity.RepeatMac;
import com.golive.network.entity.ServerMessage;
import com.golive.network.entity.Upgrade;
import com.golive.network.response.AdvertResponse;
import com.golive.network.response.ApplicationPageResponse;

import java.util.List;

import rx.Observable;

/**
 * Created by chgang on 2016/10/31.
 */

public interface ServerInitDataSource {

    Observable<List<ServerMessage>> queryServerMessages();

    Observable<BootImage> queryBootImage();

    Observable<Upgrade> upgrade(String code, String name);

    Observable<RepeatMac> queryRepeatMacStatus(String phone);

    Observable<ActivityImage> queryActivityPoster();

    Observable<ApplicationPageResponse> queryApplicationPageAction();

    Observable<GuideTypeInfo> queryGuideTypeInfo();

    Observable<DrainageInfo> queryDrainageInfo();

    Observable<AdvertResponse> queryOtherAdvert(int adType, String filmId,
            String filmName, String isFree, String hostIp);
}
