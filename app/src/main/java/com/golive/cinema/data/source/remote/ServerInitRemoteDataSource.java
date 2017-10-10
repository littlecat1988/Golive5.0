package com.golive.cinema.data.source.remote;


import static com.golive.cinema.util.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.golive.cinema.Constants;
import com.golive.cinema.data.source.MainConfigDataSource;
import com.golive.cinema.data.source.ServerInitDataSource;
import com.golive.cinema.restapi.RestApiErrorCheckFlatMap;
import com.golive.cinema.util.DeviceUtils;
import com.golive.cinema.util.StringUtils;
import com.golive.network.entity.ActivityImage;
import com.golive.network.entity.BootImage;
import com.golive.network.entity.DrainageInfo;
import com.golive.network.entity.GuideTypeInfo;
import com.golive.network.entity.MainConfig;
import com.golive.network.entity.RepeatMac;
import com.golive.network.entity.ServerMessage;
import com.golive.network.entity.ServerStatus;
import com.golive.network.entity.Upgrade;
import com.golive.network.net.GoLiveRestApi;
import com.golive.network.response.AdvertResponse;
import com.golive.network.response.ApplicationPageResponse;
import com.initialjie.log.Logger;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by chgang on 2016/11/1.
 */

public class ServerInitRemoteDataSource implements ServerInitDataSource {

    private static ServerInitRemoteDataSource INSTANCE = null;
    private final GoLiveRestApi mGoLiveRestApi;
    private final MainConfigDataSource mMainConfigDataSource;
    private ApplicationPageResponse mCachedApplicationPageResponse;
    private Observable<ApplicationPageResponse> mGetApplicationPageActionObs;

    private ServerInitRemoteDataSource(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        mGoLiveRestApi = goLiveRestApi;
        mMainConfigDataSource = mainConfigDataSource;
    }

    public static ServerInitRemoteDataSource getInstance(@NonNull GoLiveRestApi goLiveRestApi,
            @NonNull MainConfigDataSource mainConfigDataSource) {
        checkNotNull(goLiveRestApi);
        checkNotNull(mainConfigDataSource);
        if (INSTANCE == null) {
            INSTANCE = new ServerInitRemoteDataSource(goLiveRestApi, mainConfigDataSource);
        }
        return INSTANCE;
    }

    @Override
    public Observable<List<ServerMessage>> queryServerMessages() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<ServerStatus>>() {
                    @Override
                    public Observable<ServerStatus> call(MainConfig mainConfig) {
                        return mGoLiveRestApi.queryServerMessages(mainConfig.getGetmsglist());
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<ServerStatus>())
                .map(new Func1<ServerStatus, List<ServerMessage>>() {

                    @Override
                    public List<ServerMessage> call(ServerStatus serverStatus) {
                        String time = null;
                        if (serverStatus.getError() != null) {
                            time = serverStatus.getError().getServertime();
                        }
                        List<ServerMessage> messageList = serverStatus.getMsgList();
                        List<ServerMessage> serverMessages = new ArrayList<>();
                        if (messageList != null && messageList.size() > 0) {
                            for (int i = 0; i < messageList.size(); i++) {
                                ServerMessage message = messageList.get(i);
                                String type = message.getType();
                                if (StringUtils.isNullOrEmpty(type)) {
                                    continue;
                                }
//                                if (!ServerMessage.SERVER_MESSAGE_TYPE_SHUTDOWN.equals(type))
//                                {
                                message.setServerTime(time);
                                serverMessages.add(message);
//                                }
                            }
                        }
                        return serverMessages;
                    }
                });
    }

    @Override
    public Observable<BootImage> queryBootImage() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<BootImage>>() {
                    @Override
                    public Observable<BootImage> call(MainConfig mainConfig) {
                        return mGoLiveRestApi.queryBootImage(mainConfig.getBootImage());
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<BootImage>());
    }

    @Override
    public Observable<Upgrade> upgrade(final String code, final String name) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<Upgrade>>() {
                    @Override
                    public Observable<Upgrade> call(MainConfig mainConfig) {
                        return mGoLiveRestApi.upgrade(mainConfig.getUpgrade(), code, name);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<Upgrade>());
    }

    @Override
    public Observable<RepeatMac> queryRepeatMacStatus(final String phone) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<RepeatMac>>() {
                    @Override
                    public Observable<RepeatMac> call(MainConfig mainConfig) {
                        return mGoLiveRestApi.queryRepeatMacStatus(mainConfig.getRepeat(), phone);
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<RepeatMac>());
    }

    @Override
    public Observable<ActivityImage> queryActivityPoster() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<ActivityImage>>() {
                    @Override
                    public Observable<ActivityImage> call(MainConfig mainConfig) {
                        Logger.d("" + mainConfig.toString());
                        return mGoLiveRestApi.queryActivityPoster(mainConfig.getLayerActivity_40());
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<ActivityImage>());
    }

    @Override
    public Observable<ApplicationPageResponse> queryApplicationPageAction() {
        if (mCachedApplicationPageResponse != null) {
            return Observable.just(mCachedApplicationPageResponse);
        }

        if (null == mGetApplicationPageActionObs) {
            synchronized (this) {
                if (null == mGetApplicationPageActionObs) {
                    mGetApplicationPageActionObs = mMainConfigDataSource.getMainConfig()
                            .flatMap(new Func1<MainConfig, Observable<ApplicationPageResponse>>() {
                                @Override
                                public Observable<ApplicationPageResponse> call(
                                        MainConfig mainConfig) {
                                    String url = mainConfig.getQueryApplicationPage();
                                    return mGoLiveRestApi.queryApplicationPageAction(url);
                                }
                            })
                            .flatMap(new RestApiErrorCheckFlatMap<ApplicationPageResponse>())
                            .doOnNext(new Action1<ApplicationPageResponse>() {
                                @Override
                                public void call(ApplicationPageResponse applicationPageResponse) {
                                    mCachedApplicationPageResponse = applicationPageResponse;
                                }
                            });
                }
            }
        }
        return mGetApplicationPageActionObs;
    }

    @Override
    public Observable<GuideTypeInfo> queryGuideTypeInfo() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<GuideTypeInfo>>() {
                    @Override
                    public Observable<GuideTypeInfo> call(MainConfig mainConfig) {
                        return mGoLiveRestApi.queryGuideTypeInfo(mainConfig.getGetGuideType());
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<GuideTypeInfo>());
    }

    @Override
    public Observable<DrainageInfo> queryDrainageInfo() {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<DrainageInfo>>() {
                    @Override
                    public Observable<DrainageInfo> call(MainConfig mainConfig) {
                        return mGoLiveRestApi.queryDrainageInfo(mainConfig.getGetDrainageUrl());
                    }
                })
                .flatMap(new RestApiErrorCheckFlatMap<DrainageInfo>());
    }

    @Override
    public Observable<AdvertResponse> queryOtherAdvert(final int adType, final String filmId,
            final String filmName, final String isFree, final String hostIp) {
        return mMainConfigDataSource.getMainConfig()
                .flatMap(new Func1<MainConfig, Observable<AdvertResponse>>() {
                    @Override
                    public Observable<AdvertResponse> call(MainConfig mainConfig) {
                        String zoneCode = "";
                        if (adType == Constants.AD_REQUEST_TYPE_BOOT) {
                            zoneCode = mainConfig.getAdvertPositionBootimage();
                        } else if (adType == Constants.AD_REQUEST_TYPE_PLAYER) {
                            zoneCode = mainConfig.getAdvertPositionSection();
                        }
                        String url = mainConfig.getAdvertRequestUrl();
                        String appName = mainConfig.getAdvertAppName();
                        String appCode = mainConfig.getAdvertAppCode();
                        String channelCode = DeviceUtils.convertPartnerId(
                                mainConfig.getPartnerid());

                        return mGoLiveRestApi.queryOtherAdvert(url, adType, filmId, filmName,
                                isFree, zoneCode, channelCode, appName, appCode, hostIp);
                    }
                });
    }

}
